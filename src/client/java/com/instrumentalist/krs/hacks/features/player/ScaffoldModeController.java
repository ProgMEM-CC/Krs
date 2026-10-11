package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.hacks.ModuleManager;
import com.instrumentalist.krs.utils.value.BooleanValue;
import com.instrumentalist.krs.utils.value.FloatValue;
import com.instrumentalist.krs.utils.value.IntValue;
import com.instrumentalist.krs.utils.value.ListValue;
import com.instrumentalist.krs.utils.value.KeyBindValue;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.player.ClientInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import java.util.function.Consumer;

/** Settings/config boundary. All imported gameplay lives in the translated Raven core. */
public final class ScaffoldModeController {
    public static final String[] MODES = {"God Bridge", "Telly Short", "Telly Long", "Snap", "Rush", "Ninja", "Breezily", "Snap+", "Hypixel", "Hop"};
    public static final KeyBindValue[] modeKeybinds = modeKeybinds();
    public static final KeyBindValue[] keepYKeybinds = {
            new KeyBindValue("Source Keep Y Off Key", GLFW.GLFW_KEY_UNKNOWN, ScaffoldModeController::visible),
            new KeyBindValue("Source Keep Y Telly A Key", GLFW.GLFW_KEY_UNKNOWN, ScaffoldModeController::visible),
            new KeyBindValue("Source Keep Y Telly B Key", GLFW.GLFW_KEY_UNKNOWN, ScaffoldModeController::visible)};
    public static final ListValue keepY = new ListValue("Source Keep Y", new String[]{"Off", "Telly A", "Telly B"}, "Off", ScaffoldModeController::visible);
    public static final IntValue multiPlace = new IntValue("Source Multi-place", 1, 1, 3, ScaffoldModeController::visible);
    public static final BooleanValue tellyOnJump = new BooleanValue("Telly On Jump", false, () -> visible("Telly Short", "Telly Long"));
    public static final BooleanValue keepYOnRightClick = new BooleanValue("Keep Y On RMB", false, () -> visible() && !keepY.get().equals("Off"));
    public static final BooleanValue disableOnJumpPotion = new BooleanValue("Keep Y Jump Potion Check", false, () -> visible() && !keepY.get().equals("Off"));
    public static final BooleanValue alwaysSprint = new BooleanValue("Source Always Sprint", false, ScaffoldModeController::visible);
    public static final FloatValue iceThreshold = new FloatValue("Source Ice Threshold", -0.60f, -0.60f, 0.60f, () -> visible() && !keepY.get().equals("Off"));
    public static final FloatValue clickSpeed = new FloatValue("Source Click Speed", 0f, 0f, 20f, "CPS", ScaffoldModeController::visible);
    public static final BooleanValue autoSwap = new BooleanValue("Source Auto Swap", true, ScaffoldModeController::visible);
    public static final BooleanValue disableInGui = new BooleanValue("Source Disable In GUI", true, ScaffoldModeController::visible);
    public static final BooleanValue spoofItem = new BooleanValue("Source Spoof Item", false, ScaffoldModeController::visible);
    public static final BooleanValue silentSwing = new BooleanValue("Source Silent Swing", false, ScaffoldModeController::visible);
    public static final FloatValue ninjaEdgeDistance = new FloatValue("Ninja Edge Distance", 0.08f, 0f, 0.30f, "m", () -> visible("Ninja"));
    public static final IntValue ninjaMinDelay = new IntValue("Ninja Min Sneak Delay", 45, 0, 300, "ms", () -> visible("Ninja"));
    public static final IntValue ninjaMaxDelay = new IntValue("Ninja Max Sneak Delay", 85, 0, 300, "ms", () -> visible("Ninja"));
    public static final IntValue ninjaSneakOnJump = new IntValue("Ninja Sneak On Jump", 0, 0, 500, "ms", () -> visible("Ninja"));
    public static final BooleanValue ninjaRequireSneakKey = new BooleanValue("Ninja Require Sneak Key", false, () -> visible("Ninja"));
    public static final BooleanValue ninjaRequireBlocks = new BooleanValue("Ninja Holding Blocks", false, () -> visible("Ninja"));
    public static final BooleanValue ninjaRequireLookingDown = new BooleanValue("Ninja Looking Down", false, () -> visible("Ninja"));
    public static final BooleanValue ninjaDisableForward = new BooleanValue("Ninja Not Moving Forward", false, () -> visible("Ninja"));
    public static final BooleanValue ninjaAvoidDoubleSneaking = new BooleanValue("Ninja Avoid Double Sneaking", true, () -> visible("Ninja"));
    public static final BooleanValue breezilyAutoStrafe = new BooleanValue("Breezily Auto Strafe", true, () -> visible("Breezily"));
    public static final BooleanValue breezilyOnlyStrafeOnGround = new BooleanValue("Breezily Ground Only", false, ScaffoldModeController::breezilyVisible);
    public static final IntValue breezilyGroundWaitTicks = new IntValue("Breezily Ground Wait", 2, 1, 10, "ticks", () -> breezilyVisible() && breezilyOnlyStrafeOnGround.get());
    public static final IntValue breezilySwitchSpeed = new IntValue("Breezily Switch Speed", 4, 2, 10, "ticks", () -> visible("Breezily") && breezilyAutoStrafe.get());
    public static final FloatValue rushLookAhead = new FloatValue("Rush Look Ahead", 2.10f, 1.20f, 3f, "m", () -> visible("Rush"));
    public static final IntValue rushChain = new IntValue("Rush Verified Chain", 2, 1, 3, () -> visible("Rush"));
    public static final BooleanValue rushAutoHop = new BooleanValue("Rush Sprint Hop", true, () -> visible("Rush"));
    public static final ListValue sourceScaffoldMode = new ListValue("Hypixel Style", new String[]{"Normal", "Telly", "Hypixel", "Breezily", "God Bridge"}, "Hypixel", () -> visible("Hypixel"));
    public static final ListValue sourceSprintMode = new ListValue("Hypixel Sprint", new String[]{"None", "Vanilla", "Legit", "NCP", "Universal"}, "None", () -> visible("Hypixel"));
    public static final BooleanValue sourceTellyWatchdog = new BooleanValue("Hypixel Telly Watchdog", true, () -> sourceVisible("Telly"));
    public static final IntValue sourceTellyStraightTicks = new IntValue("Hypixel Telly Straight", 6, 0, 8, "ticks", () -> sourceVisible("Telly") && !sourceTellyWatchdog.get());
    public static final IntValue sourceTellyDiagonalTicks = new IntValue("Hypixel Telly Diagonal", 4, 0, 8, "ticks", () -> sourceVisible("Telly") && !sourceTellyWatchdog.get());
    public static final IntValue sourceTellyJumpDownTicks = new IntValue("Hypixel Telly Jump", 1, 0, 8, "ticks", () -> sourceVisible("Telly") && !sourceTellyWatchdog.get());
    public static final IntValue sourcePlaceDelay = new IntValue("Hypixel Place Delay", 0, 0, 10, "x20ms", () -> visible("Hypixel"));
    public static final BooleanValue sourceAutoJump = new BooleanValue("Hypixel Auto Jump", false, () -> visible("Hypixel") && !sourceVisible("Telly"));
    public static final BooleanValue sourceSneak = new BooleanValue("Hypixel GodBridge Sneak", false, () -> sourceVisible("God Bridge"));
    public static final IntValue sourceSneakEvery = new IntValue("Hypixel Sneak Every", 1, 1, 16, "blocks", () -> sourceVisible("God Bridge") && sourceSneak.get());
    public static final ListValue hopMode = new ListValue("Hop Movement", new String[]{"Sprint", "Low", "Boost", "Burst", "Adaptive"}, "Sprint", () -> visible("Hop"));
    public static final FloatValue hopSpeed = new FloatValue("Hop Speed", 1.08f, 1f, 1.18f, "x", () -> visible("Hop") && !hopMode.get().equals("Sprint"));
    public static final BooleanValue hopAutoSprint = new BooleanValue("Hop Auto Sprint", true, () -> visible("Hop"));

    private RavenScaffoldCore core;
    private boolean active;
    private final boolean[] modeWasDown = new boolean[MODES.length], keepYWasDown = new boolean[3];
    private static KeyBindValue[] modeKeybinds() {
        KeyBindValue[] result = new KeyBindValue[MODES.length];
        for (int i = 0; i < result.length; i++) result[i] = new KeyBindValue("Source " + MODES[i] + " Key", GLFW.GLFW_KEY_UNKNOWN, ScaffoldModeController::visible);
        return result;
    }
    private boolean bindDown(KeyBindValue bind) {
        return bind.get() >= 0 && InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), bind.get());
    }
    /** Source 5303-5345: edge-triggered polling in source option order; GUI just syncs latches. */
    public void pollKeybinds(boolean syncOnly) {
        boolean modeConsumed = false, keepConsumed = false;
        for (int i = 0; i < modeKeybinds.length; i++) {
            boolean down = bindDown(modeKeybinds[i]);
            if (!syncOnly && !modeConsumed && down && !modeWasDown[i]) { Scaffold.selectImportedMode(MODES[i]); modeConsumed = true; }
            // A consumed source loop breaks; preserve later key latches until the next poll.
            if (modeConsumed) { modeWasDown[i] = down; break; }
            modeWasDown[i] = down;
        }
        String[] keeps = {"Off", "Telly A", "Telly B"};
        for (int i = 0; i < keepYKeybinds.length; i++) {
            boolean down = bindDown(keepYKeybinds[i]);
            if (!syncOnly && !keepConsumed && down && !keepYWasDown[i]) { keepY.set(keeps[i]); keepConsumed = true; }
            if (keepConsumed) { keepYWasDown[i] = down; break; }
            keepYWasDown[i] = down;
        }
    }
    public static boolean handles(String mode) {
        if (mode != null) for (String candidate : MODES) if (candidate.equalsIgnoreCase(mode)) return true;
        return false;
    }
    private static boolean visible(String... modes) {
        Scaffold scaffold = ModuleManager.getModule(Scaffold.class);
        if (scaffold == null) return false;
        if (modes.length == 0) return handles(scaffold.tag());
        for (String candidate : modes) if (candidate.equalsIgnoreCase(scaffold.tag())) return true;
        return false;
    }
    private static boolean sourceVisible(String style) { return visible("Hypixel") && sourceScaffoldMode.get().equals(style); }
    private static boolean breezilyVisible() { return visible("Breezily") || sourceVisible("Breezily"); }

    public void reset() {
        if (core != null && active) core.onDisable();
        active = false;
        pollKeybinds(true);
    }
    public void preUpdate(String mode, boolean towerEnabled, String towerMode, ScaffoldTowerController tower,
                          Consumer<BlockPos> placed, Runnable legacyNcpTower) {
        if (core == null) {
            core = new RavenScaffoldCore(tower, placed, legacyNcpTower);
        }
        if (!active) {
            core.selectedMode = mode;
            core.onEnable();
            active = true;
        }
        core.selectedMode = mode;
        core.towerEnabled = towerEnabled;
        core.selectedTowerMode = towerMode;
        core.onPreUpdate();
    }
    public void applyInput(ClientInput input) { if (active) core.onPrePlayerInput(input); }
    public void finishInput(ClientInput input) { if (active) core.onPostPlayerInput(input); }
    public boolean suppressSprint(String mode) { return active && core.sprintSuppressed; }
    public boolean forceMovementFix() { return active && core.rotations.forceMovementFix; }
    public boolean bypassMovementFix() { return active && !core.rotations.forceMovementFix; }
    public boolean ownsRotations() { return active && core.rotationSentThisTick; }
    public boolean allowPacket(Packet<?> packet) { return !active || core.allowPacket(packet); }
    public boolean allowMouse(int button) { return !active || core.allowMouse(button); }
    public void beforeInteract() { if (active) core.onPrePlayerInteract(); }
    public int blockCount() { return active ? core.getTotalBlocks() : 0; }
    public boolean isTowerActive() { return active && core.isTowerActive(); }
    public boolean shouldSpoofGround() { return active && core.shouldSpoofGround(); }
}
