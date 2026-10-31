package com.instrumentalist.mixin.injector;

import com.instrumentalist.krs.hacks.features.combat.Displace;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class DisplaceConnectionMixin {
    @Shadow
    private Channel channel;

    // This overload is reached after the native one-/two-argument send hooks. Buffer the
    // finalized packet once, including sends with a non-null completion listener.
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
            at = @At("HEAD"), cancellable = true)
    private void krs$displaceBufferOutbound(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        if (Displace.captureOutboundPacket((Connection) (Object) this, channel, packet, listener, flush))
            ci.cancel();
    }

    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"))
    private void krs$displaceDiscardOnDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        Displace.connectionClosed((Connection) (Object) this);
    }

    @Inject(method = "channelInactive", at = @At("HEAD"))
    private void krs$displaceDiscardOnChannelInactive(ChannelHandlerContext context, CallbackInfo ci) {
        Displace.connectionClosed((Connection) (Object) this);
    }
}
