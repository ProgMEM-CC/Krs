package com.instrumentalist.mixin.injector;

import com.instrumentalist.krs.hacks.features.player.ChatBypass;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Transforms the common UI/programmatic send inputs before signing; never edits signed packets.
 * ChatScreenMixin still handles client-only Chat Commands before these methods are reached.
 */
@Mixin(ClientPacketListener.class)
public abstract class ChatBypassNetworkMixin {
    @ModifyVariable(method = "sendChat(Ljava/lang/String;)V", at = @At("HEAD"),
            argsOnly = true, ordinal = 0, require = 1)
    private String krs$chatBypassMessage(String message) {
        return ChatBypass.transformOutgoing(message, false);
    }

    @ModifyVariable(method = "sendCommand(Ljava/lang/String;)V", at = @At("HEAD"),
            argsOnly = true, ordinal = 0, require = 1)
    private String krs$chatBypassCommand(String command) {
        return ChatBypass.transformOutgoing(command, true);
    }

    // Minecraft 26.2 has sendUnattendedCommand, not the older sendUnsignedCommand method.
    // Its unsigned branches send directly, without calling sendCommand, so they need this hook.
    @ModifyVariable(method = "sendUnattendedCommand(Ljava/lang/String;Lnet/minecraft/client/gui/screens/Screen;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 1)
    private String krs$chatBypassUnattendedCommand(String command,
                                                @Share("krs$chatBypassOriginalCommand") LocalRef<String> originalCommand) {
        originalCommand.set(command);
        return ChatBypass.transformOutgoing(command, true);
    }

    // A signature-required unattended command is only suggested/copied for later submission.
    // Keep that draft original: its eventual sendCommand call performs the transformation once.
    // The shared value is invocation-local, so no persistent guard or recursive resend is needed.
    @ModifyArg(method = "sendUnattendedCommand(Ljava/lang/String;Lnet/minecraft/client/gui/screens/Screen;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;openSignedCommandSendConfirmationWindow(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/client/gui/screens/Screen;)V"),
            index = 0, require = 1)
    private String krs$chatBypassOriginalSignedDraft(String command,
                                                  @Share("krs$chatBypassOriginalCommand") LocalRef<String> originalCommand) {
        return originalCommand.get();
    }
}
