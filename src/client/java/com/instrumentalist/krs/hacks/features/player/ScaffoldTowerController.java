package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.utils.IMinecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.SnowLayerBlock;

/**
 * Minecraft adapter for the source tower sequences in {@link ScaffoldTowerState}.
 * For source event order, call {@link #updateSession(boolean)} before planning
 * and {@link #tickMotion(String, boolean)} from post-input. The combined
 * {@link #tick(String, boolean)} remains available for existing callers.
 */
public final class ScaffoldTowerController implements IMinecraft {
    private static final String[] MODE_NAMES = ScaffoldTowerState.MODES.clone();

    /** Source order; KRS's separate legacy "NCP" mode is not handled here. */
    public static final String[] MODES = MODE_NAMES.clone();

    private final ScaffoldTowerState state = new ScaffoldTowerState();
    private LocalPlayer sessionPlayer;
    private ClientLevel sessionLevel;

    public static boolean handles(String mode) {
        return findMode(mode) != null;
    }

    /** A mode predicate: the input hook also checks source isTowerActive. */
    public static boolean ownsJump(String mode) {
        String selected = findMode(mode);
        return selected != null && !selected.equals("Vanilla");
    }

    private static String findMode(String name) {
        if (name == null) return null;
        for (String candidate : MODE_NAMES) {
            if (candidate.equalsIgnoreCase(name)) return candidate;
        }
        return null;
    }

    public void reset() {
        state.reset();
        sessionPlayer = null;
        sessionLevel = null;
    }

    /**
     * Combined session/motion update for existing callers. The caller supplies
     * physical-jump, movement and inventory gating. Source-timed callers use
     * updateSession in pre-update and tickMotion in post-input instead.
     */
    public void tick(String selectedMode, boolean requestedActive) {
        if (!handles(selectedMode)) {
            reset();
            return;
        }
        updateSession(requestedActive);
        tickMotion(selectedMode, requestedActive);
    }

    /**
     * Source updateTowerSession, before placement. A true result requests that
     * the placement owner clear rushPlannedPlacement, as the source does when
     * leaving a tower session. Does not clear the per-motion ground-spoof flag.
     */
    public boolean updateSession(boolean requestedActive) {
        if (!synchronizePlayer()) return false;
        return state.updateTowerSession(requestedActive, input());
    }

    /**
     * Source updateTowerMotion, after input. Inactivity here does not end the
     * pre-update session or erase placement history. No ground-start, collision,
     * headroom, or position-validation gates are added by this adapter.
     */
    public void tickMotion(String selectedMode, boolean requestedActive) {
        String selected = findMode(selectedMode);
        if (selected == null) {
            reset();
            return;
        }
        if (!synchronizePlayer()) return;
        ScaffoldTowerState.Output output = state.updateTowerMotion(selected, requestedActive,
                input(), this::isSolidSupport);
        // Yuri writes position before assigning motion in the source.
        if (output.positionChanged())
            sessionPlayer.setPos(sessionPlayer.getX(), output.y(), sessionPlayer.getZ());
        if (output.motionChanged())
            sessionPlayer.setDeltaMovement(0.0D, output.motionY(), 0.0D);
    }

    /** Caller reports a source-confirmed successful UP-face tower placement. */
    public void onPlaced(BlockPos target) {
        if (!isActive() || target == null) return;
        state.onPlaced(target.getY());
    }

    /** Call only for a successful UP placement matching rushPlannedPlacement. */
    public void onPlannedUpPlacement(BlockPos target) {
        if (target == null || !synchronizePlayer()) return;
        state.onPlannedUpPlacement(target.getY());
    }

    /** Forward only to the pre-motion packet's onGround field, not the entity. */
    public boolean shouldSpoofGround() {
        return isActive() && state.state().towerSpoofGroundThisTick();
    }

    /** Source pre-motion guard, using the current physical activation and mode. */
    public boolean shouldSpoofGround(String selectedMode, boolean requestedActive) {
        String selected = findMode(selectedMode);
        return selected != null && mc.player != null && mc.level != null
                && sessionPlayer == mc.player && sessionLevel == mc.level
                && state.shouldSpoofGround(selected, requestedActive);
    }

    public boolean isActive() {
        return state.state().towerWasActive() && mc.player != null && mc.level != null
                && sessionPlayer == mc.player && sessionLevel == mc.level;
    }

    /** The source placement planner compares candidate height to this value. */
    public int lastPlacedY() {
        return state.state().towerLastPlacedY();
    }

    private boolean synchronizePlayer() {
        if (mc.player == null || mc.level == null) {
            reset();
            return false;
        }
        if (sessionPlayer != mc.player || sessionLevel != mc.level) {
            reset();
            sessionPlayer = mc.player;
            sessionLevel = mc.level;
        }
        return true;
    }

    private ScaffoldTowerState.Input input() {
        return new ScaffoldTowerState.Input(sessionPlayer.getX(), sessionPlayer.getY(),
                sessionPlayer.getZ(), sessionPlayer.getDeltaMovement().y,
                sessionPlayer.onGround(), sessionPlayer.hasEffect(MobEffects.JUMP_BOOST));
    }

    private boolean isSolidSupport(int x, int y, int z) {
        var blockState = sessionLevel.getBlockState(new BlockPos(x, y, z));
        String name = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath();
        int variant = blockState.getBlock() instanceof SnowLayerBlock
                ? blockState.getValue(SnowLayerBlock.LAYERS) - 1 : 0;
        return !ScaffoldTowerSupport.isReplaceable(ScaffoldTowerSupport.legacyName(name), variant);
    }
}
