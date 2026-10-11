package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.hacks.Module;
import com.instrumentalist.krs.hacks.ModuleCategory;
import com.instrumentalist.krs.hacks.ModuleManager;
import com.instrumentalist.krs.utils.value.BooleanValue;
import org.lwjgl.glfw.GLFW;

public class ChatBypass extends Module {
    @Setting
    public final BooleanValue filterKnownWords = new BooleanValue("Only filter known words", true);

    public ChatBypass() {
        super("Chat Bypass", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    /** Called only on listener inputs, before Minecraft parses commands or signs chat. */
    public static String transformOutgoing(String message, boolean commandInput) {
        ChatBypass module = ModuleManager.getModule(ChatBypass.class);
        if (module == null || !module.tempEnabled || mc.player == null || mc.level == null)
            return message;

        return commandInput
                ? ChatBypassText.transformCommandInput(message, module.filterKnownWords.get())
                : ChatBypassText.transform(message, module.filterKnownWords.get());
    }
}
