package com.instrumentalist.krs.hacks.features.combat;

import com.mojang.logging.LogUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.slf4j.Logger;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Owned outbound-only FIFO. Like native BlinkUtil it bounds the buffer at 2048 and bypasses
 * its own hook during release, but it pins the original connection/world/player and retains
 * send callbacks. Packet events already ran before this final send boundary, so replaying
 * the three-argument Connection.send avoids dispatching those events a second time.
 */
final class DisplaceBlinkBuffer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_BUFFERED_PACKETS = 2048;
    private final Minecraft mc = Minecraft.getInstance();
    private final ArrayDeque<PendingPacket> packets = new ArrayDeque<>();
    private final ThreadLocal<Boolean> replaying = ThreadLocal.withInitial(() -> false);
    private Connection connection;
    private ClientPacketListener listener;
    private ClientLevel world;
    private LocalPlayer player;
    private boolean holding;

    synchronized void arm(Connection sendingConnection, Packet<?> packet, boolean canStart) {
        if (replaying.get())
            return;
        if (holding && !isSameLiveSession())
            discard();
        if (holding || !canStart || !(packet instanceof ServerboundMovePlayerPacket))
            return;
        ClientPacketListener current = mc.getConnection();
        if (current == null || current.getConnection() != sendingConnection
                || current != sendingConnection.getPacketListener() || !sendingConnection.isConnected()
                || mc.player == null || mc.level == null)
            return;
        connection = sendingConnection;
        listener = current;
        world = mc.level;
        player = mc.player;
        holding = true;
    }

    synchronized boolean capture(Connection sendingConnection, Channel channel, Packet<?> packet,
                                 ChannelFutureListener callback, boolean flush, boolean canStart) {
        if (replaying.get() || packet == null)
            return false;
        if (holding && !isSameLiveSession())
            discard();
        if (holding && sendingConnection != connection)
            return false;

        if (packet.isTerminal()) {
            // A protocol transition must never be held behind gameplay packets.
            release();
            return false;
        }
        if (!holding) {
            // Fallback for callers using Connection's two-/three-argument overload directly.
            arm(sendingConnection, packet, canStart);
            if (!holding)
                return false;
        }
        if (sendingConnection != connection)
            return false;
        if (channel == null || !channel.isOpen()) {
            discard();
            return false;
        }

        // Includes the triggering movement packet unless an earlier native listener cancelled
        // it. ONLY_OUTBOUND includes following attack/swing and keepalives for one tick.
        packets.addLast(new PendingPacket(packet, once(callback), flush, channel));
        if (packets.size() >= MAX_BUFFERED_PACKETS)
            flushPackets();
        return true;
    }

    synchronized void release() {
        if (!holding && packets.isEmpty())
            return;
        if (isSameLiveSession())
            flushPackets();
        else
            discard();
        clearOwner();
    }

    synchronized void discardConnection(Connection closedConnection) {
        if (connection == closedConnection)
            discard();
    }

    synchronized void discard() {
        // Detach before completing callbacks, which are allowed to reenter the connection.
        ArrayDeque<PendingPacket> abandoned = new ArrayDeque<>(packets);
        packets.clear();
        clearOwner();
        failCallbacks(abandoned, new ClosedChannelException());
    }

    private boolean isSameLiveSession() {
        return connection != null && connection.isConnected() && listener != null
                && mc.getConnection() == listener && listener.getConnection() == connection
                && connection.getPacketListener() == listener
                && mc.level == world && world != null && mc.player == player && player != null;
    }

    private void flushPackets() {
        if (packets.isEmpty())
            return;
        replaying.set(true);
        try {
            while (!packets.isEmpty()) {
                if (!isSameLiveSession()) {
                    discard();
                    return;
                }
                PendingPacket pending = packets.removeFirst();
                try {
                    // Use the pinned connection, never PacketUtil's newly looked-up connection.
                    connection.send(pending.packet(), pending.callback(), pending.flush());
                } catch (RuntimeException failure) {
                    // Sending may already have scheduled work: retrying could duplicate a packet.
                    // Drop the rest and fail callbacks instead of leaking into a later session.
                    LOGGER.warn("Displace blink release failed; discarding remaining {} packets", packets.size(), failure);
                    ArrayDeque<PendingPacket> abandoned = new ArrayDeque<>(packets);
                    packets.clear();
                    abandoned.addFirst(pending);
                    clearOwner();
                    failCallbacks(abandoned, failure);
                    return;
                }
            }
        } finally {
            replaying.remove();
        }
    }

    private void clearOwner() {
        holding = false;
        connection = null;
        listener = null;
        world = null;
        player = null;
    }

    private static void failCallbacks(ArrayDeque<PendingPacket> abandoned, Throwable cause) {
        while (!abandoned.isEmpty()) {
            PendingPacket pending = abandoned.removeFirst();
            if (pending.callback() == null || pending.channel() == null)
                continue;
            try {
                // Netty schedules listeners on the channel's event loop, like a normal send.
                pending.channel().newFailedFuture(cause).addListener(pending.callback());
            } catch (RuntimeException callbackFailure) {
                LOGGER.warn("Displace discarded-packet callback failed", callbackFailure);
            }
        }
    }

    private static ChannelFutureListener once(ChannelFutureListener callback) {
        if (callback == null)
            return null;
        AtomicBoolean completed = new AtomicBoolean();
        return future -> {
            if (completed.compareAndSet(false, true))
                callback.operationComplete(future);
        };
    }

    private record PendingPacket(Packet<?> packet, ChannelFutureListener callback, boolean flush, Channel channel) {
    }
}
