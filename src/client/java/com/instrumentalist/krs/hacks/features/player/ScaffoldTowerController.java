package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.utils.IMinecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * The tower motion sequences from the supplied Raven Scaffold script.
 * Call {@link #tick(String, boolean)} once per update, before placement, and
 * report successful placements through {@link #onPlaced(BlockPos)}.
 */
public final class ScaffoldTowerController implements IMinecraft {

    private enum Mode {
        VANILLA("Vanilla"),
        SYNC("Sync"),
        HYPIXEL("Hypixel"),
        APEX("Apex"),
        SYNC_BURST("Sync Burst"),
        SYNC_EARLY_BURST("Sync Early Burst"),
        RAVEN_1_TICK("Raven 1 Tick"),
        RAVEN_PREDICTION("Raven Prediction"),
        ON_GROUND("OnGround"),
        ON_GROUND_EARLY("OnGround Early"),
        ON_GROUND_PULSE("OnGround Pulse"),
        ON_GROUND_PREDICTION("OnGround Prediction"),
        YURI_NCP("Yuri NCP"),
        RAVEN_PULL_SYNC("Raven Pull Sync"),
        RAVEN_PULL("Raven Pull"),
        RAVEN_PREDICT_EARLY("Raven Predict Early"),
        RAVEN_PREDICT_TURBO("Raven Predict Turbo"),
        RAVEN_PREDICTION_SWIFT("Raven Prediction Swift"),
        RAVEN_PREDICTION_RAPID("Raven Prediction Rapid"),
        POLAR("Polar"),
        INTAVE("Intave");

        private final String displayName;

        Mode(String displayName) {
            this.displayName = displayName;
        }
    }

    /** Source order; KRS's separate legacy "NCP" mode is not handled here. */
    public static final String[] MODES;

    static {
        Mode[] modes = Mode.values();
        MODES = new String[modes.length];
        for (int i = 0; i < modes.length; i++)
            MODES[i] = modes[i].displayName;
    }

    private static final double COLLISION_EPSILON = 1.0E-7D;
    private static final double GROUND_EPSILON = 1.0E-4D;

    private Mode mode;
    private LocalPlayer sessionPlayer;
    private ClientLevel sessionLevel;
    private boolean active;
    private boolean hasGroundStart;
    private int towerTick;
    private int towerDelay;
    private int towerLastPlacedY = Integer.MIN_VALUE;
    private double towerPrevMotionY;
    private int towerCycle;
    private double towerLaunchY;
    private boolean towerSpoofGroundThisTick;

    public static boolean handles(String mode) {
        return findMode(mode) != null;
    }

    /** A mode predicate: the input hook must also check its current activation conditions. */
    public static boolean ownsJump(String mode) {
        Mode selected = findMode(mode);
        return selected != null && selected != Mode.VANILLA;
    }

    private static Mode findMode(String name) {
        if (name == null) return null;
        for (Mode candidate : Mode.values()) {
            if (candidate.displayName.equalsIgnoreCase(name)) return candidate;
        }
        return null;
    }

    public void reset() {
        mode = null;
        sessionPlayer = null;
        sessionLevel = null;
        active = false;
        hasGroundStart = false;
        towerTick = 0;
        towerDelay = 0;
        towerLastPlacedY = Integer.MIN_VALUE;
        towerPrevMotionY = 0.0D;
        towerCycle = 0;
        towerLaunchY = 0.0D;
        towerSpoofGroundThisTick = false;
    }

    /**
     * The caller supplies physical-jump, movement, inventory and Tower When gating.
     * Passing false, an unsupported mode, or a missing player/world ends the session.
     */
    public void tick(String selectedMode, boolean requestedActive) {
        Mode selected = findMode(selectedMode);
        var player = mc.player;
        var level = mc.level;
        if (!requestedActive || selected == null || player == null || level == null) {
            reset();
            return;
        }

        if (!active || mode != selected || sessionPlayer != player || sessionLevel != level) {
            reset();
            active = true;
            mode = selected;
            sessionPlayer = player;
            sessionLevel = level;
            towerPrevMotionY = player.getDeltaMovement().y;
            towerLaunchY = player.getY();
        }
        towerSpoofGroundThisTick = false;

        // Source Vanilla deliberately leaves both motion and the jump input alone.
        if (mode == Mode.VANILLA) {
            towerTick = 0;
            towerDelay = 0;
            towerPrevMotionY = player.getDeltaMovement().y;
            return;
        }

        // A newly selected sequence must start on real collision support. This
        // also prevents initial Yuri/Pull/1-Tick launches when enabled in mid-air.
        if (player.onGround()) {
            if (!hasSupportAt(player.getY()) || !hasVerticalClearance(0.42D)) {
                hasGroundStart = false;
                towerTick = 0;
                towerDelay = 0;
                towerPrevMotionY = player.getDeltaMovement().y;
                return;
            }
            hasGroundStart = true;
        } else if (!hasGroundStart) {
            towerPrevMotionY = player.getDeltaMovement().y;
            return;
        }

        if (player.onGround() && towerTick == 0) {
            towerCycle++;
            towerLaunchY = player.getY();
        }

        switch (mode) {
            case SYNC -> runSyncTower();
            case HYPIXEL -> runHypixelTower();
            case APEX -> runApexTower();
            case SYNC_BURST -> {
                if (towerCycle % 3 == 0) runPulseVariant(0.68D, 1.02D, 0.14D, -0.12D);
                else runSyncTower();
            }
            case SYNC_EARLY_BURST -> {
                if (towerCycle % 3 == 0) runPulseVariant(0.64D, 1.015D, 0.13D, -0.10D);
                else runSyncTower();
            }
            case RAVEN_1_TICK -> runRaven1Tower();
            case RAVEN_PREDICTION -> runRavenPredictionTower();
            case ON_GROUND -> runOnGroundTower();
            case ON_GROUND_EARLY -> runOnGroundEarlyTower();
            case ON_GROUND_PULSE -> runOnGroundPulseTower();
            case ON_GROUND_PREDICTION -> runOnGroundPredictionTower();
            case YURI_NCP -> runYuriNCPTower();
            case RAVEN_PULL_SYNC -> runRavenPullSyncTower();
            case RAVEN_PULL -> runRavenPullTower();
            case RAVEN_PREDICT_EARLY -> runRavenPredictionAcceleratedTower(false);
            case RAVEN_PREDICT_TURBO -> runRavenPredictionAcceleratedTower(true);
            case RAVEN_PREDICTION_SWIFT -> runRavenPredictionVariant(0.10D, -0.38D, 1.04D);
            case RAVEN_PREDICTION_RAPID -> runRavenPredictionVariant(0.17D, -0.42D, 1.005D);
            case POLAR -> runPolarTower();
            case INTAVE -> runIntaveTower();
            case VANILLA -> { }
        }
        towerPrevMotionY = player.getDeltaMovement().y;
    }

    public void onPlaced(BlockPos target) {
        if (!isActive() || target == null) return;
        // A successful use-item result alone does not establish collision support.
        if (mc.level.getBlockState(target).getCollisionShape(mc.level, target,
                CollisionContext.of(mc.player)).isEmpty()) return;
        towerLastPlacedY = Math.max(towerLastPlacedY, target.getY());
    }

    /** Forward only to the pre-motion packet's onGround field, never to the entity. */
    public boolean shouldSpoofGround() {
        return isActive() && towerSpoofGroundThisTick;
    }

    public boolean isActive() {
        return active && mc.player != null && mc.level != null
                && sessionPlayer == mc.player && sessionLevel == mc.level;
    }

    private void setTowerMotion(double motionY) {
        // The source deliberately cancels horizontal drift when assigning a pulse.
        mc.player.setDeltaMovement(0.0D, motionY, 0.0D);
    }

    private double towerFraction() {
        double fraction = mc.player.getY() - Math.floor(mc.player.getY());
        return fraction < 0.0D ? fraction + 1.0D : fraction;
    }

    private boolean hasVerticalClearance(double motionY) {
        AABB sweptBox = mc.player.getBoundingBox().expandTowards(0.0D, motionY, 0.0D);
        return mc.level.noCollision(mc.player, sweptBox.deflate(COLLISION_EPSILON));
    }

    private boolean hasSupportAt(double feetY) {
        AABB box = mc.player.getBoundingBox();
        AABB probe = new AABB(box.minX + COLLISION_EPSILON, feetY - GROUND_EPSILON,
                box.minZ + COLLISION_EPSILON, box.maxX - COLLISION_EPSILON,
                feetY + COLLISION_EPSILON, box.maxZ - COLLISION_EPSILON);
        for (var shape : mc.level.getBlockCollisions(mc.player, probe)) {
            for (AABB support : shape.toAabbs()) {
                if (Math.abs(support.maxY - feetY) <= GROUND_EPSILON
                        && overlapsFootprint(support, box)) return true;
            }
        }
        return false;
    }

    private static boolean overlapsFootprint(AABB support, AABB feet) {
        return support.maxX > feet.minX + COLLISION_EPSILON
                && support.minX < feet.maxX - COLLISION_EPSILON
                && support.maxZ > feet.minZ + COLLISION_EPSILON
                && support.minZ < feet.maxZ - COLLISION_EPSILON;
    }

    /** The source's four-corner support check, using actual modern collision bounds. */
    private double supportTopAt(int blockY) {
        var player = mc.player;
        var level = mc.level;
        AABB box = player.getBoundingBox();
        double highest = Double.NEGATIVE_INFINITY;
        CollisionContext context = CollisionContext.of(player);
        for (int x = Mth.floor(box.minX + COLLISION_EPSILON);
             x <= Mth.floor(box.maxX - COLLISION_EPSILON); x++) {
            for (int z = Mth.floor(box.minZ + COLLISION_EPSILON);
                 z <= Mth.floor(box.maxZ - COLLISION_EPSILON); z++) {
                BlockPos pos = new BlockPos(x, blockY, z);
                var shape = level.getBlockState(pos).getCollisionShape(level, pos, context);
                for (AABB localBox : shape.toAabbs()) {
                    AABB support = localBox.move(pos);
                    if (support.maxY <= player.getY() + COLLISION_EPSILON
                            && overlapsFootprint(support, box)) {
                        highest = Math.max(highest, support.maxY);
                    }
                }
            }
        }
        return highest;
    }

    private boolean hasCornerSupport(int blockY) {
        return Double.isFinite(supportTopAt(blockY));
    }

    private boolean hasPlacedSupport(int blockY) {
        // Recheck the world every tick: a previously successful block can be rejected.
        return towerLastPlacedY >= blockY && hasCornerSupport(blockY);
    }

    private boolean setTowerPositionY(double y) {
        var player = mc.player;
        double offset = y - player.getY();
        AABB target = player.getBoundingBox().move(0.0D, offset, 0.0D);
        if (!hasSupportAt(y) || !hasVerticalClearance(offset)
                || !mc.level.noCollision(player, target.expandTowards(0.0D, 0.41998D, 0.0D)
                .deflate(COLLISION_EPSILON))) return false;

        player.setPos(player.getX(), y, player.getZ());
        return true;
    }

    private void runSyncTower() {
        double fraction = towerFraction();
        if (mc.player.onGround() && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick > 0 && mc.player.getDeltaMovement().y <= 0.0D && fraction <= 0.12D) {
            setTowerMotion(-0.08D);
            towerTick = 0;
        }
    }

    private void runHypixelTower() {
        if (mc.player.hasEffect(MobEffects.JUMP_BOOST)) {
            towerTick = 0;
            towerDelay = 0;
            return;
        }
        if (mc.player.onGround()) {
            setTowerMotion(0.42D);
            towerTick = 1;
            towerDelay = 0;
            return;
        }
        if (towerTick <= 0) {
            towerTick = 1;
            towerDelay = 0;
        }
        towerDelay++;
        if (towerDelay == 4) {
            setTowerMotion(mc.player.getDeltaMovement().y - 0.03D);
        } else if (towerDelay == 5) {
            setTowerMotion(mc.player.getDeltaMovement().y - 0.50D);
        }
    }

    private void runPulseVariant(double triggerFraction, double targetHeight,
                                 double resetFraction, double catchMotion) {
        double fraction = towerFraction();
        if (mc.player.onGround() && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= triggerFraction && mc.player.getDeltaMovement().y > 0.0D) {
            setTowerMotion(Math.max(0.12D, targetHeight - fraction));
            towerTick = 2;
        } else if (towerTick == 2 && fraction <= resetFraction) {
            setTowerMotion(catchMotion);
            towerTick = 0;
        }
    }

    private void runApexTower() {
        if (mc.player.onGround() && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && towerPrevMotionY > 0.0D && mc.player.getDeltaMovement().y <= 0.0D) {
            setTowerMotion(-0.20D);
            towerTick = 2;
        } else if (towerTick == 2 && mc.player.onGround()) {
            towerTick = 0;
        }
    }

    private void runRaven1Tower() {
        if (mc.player.onGround()) towerTick = 0;
        int tick = towerTick++;
        double fraction = towerFraction();
        if (tick == 0) setTowerMotion(0.42D);
        else if (tick == 1) setTowerMotion(0.33D);
        else if (tick == 2) setTowerMotion(Math.max(0.005D, 1.0D - fraction));
        else if (tick == 3) setTowerMotion(0.005D);
    }

    private void runPolarTower() {
        var player = mc.player;
        if (player.onGround()) {
            setTowerMotion(0.42D);
            towerTick = 1;
            return;
        }
        if (player.getDeltaMovement().y >= 0.0D) return;
        double feetY = player.getY();
        int firstY = Mth.floor(feetY - 0.01D);
        for (int y = firstY; y >= firstY - 3; y--) {
            double supportTop = supportTopAt(y);
            if (!Double.isFinite(supportTop)) continue;
            double distanceToGround = feetY - supportTop;
            if (distanceToGround >= 0.0D && distanceToGround < 1.26D)
                setTowerMotion(player.getDeltaMovement().y - 0.091F);
            return;
        }
    }

    private void runIntaveTower() {
        var player = mc.player;
        if (player.onGround()) {
            towerLaunchY = Math.floor(player.getY() + 1.0E-4D);
            towerTick = 1;
            towerDelay = 0;
            setTowerMotion(0.42D);
            return;
        }
        if (towerTick <= 0) return;

        double rise = player.getY() - towerLaunchY;
        int supportY = Mth.floor(towerLaunchY);
        boolean placedSupport = hasPlacedSupport(supportY);

        if (towerTick == 1 && rise >= 0.65D && rise < 0.99D && player.getDeltaMovement().y > 0.0D) {
            setTowerMotion(Mth.clamp(1.045D - rise, 0.14D, 0.38D));
            towerTick = 2;
            return;
        }

        // Wait for both placement and feet clearance before the one-time landing pull.
        if (placedSupport && rise >= 1.001D && rise < 1.6D && towerTick <= 2) {
            setTowerMotion(-0.48D);
            towerTick = 3;
            return;
        }
        if (rise < -0.25D || player.getDeltaMovement().y < -0.75D) {
            towerTick = 4;
        }
    }

    private void runRavenPredictionTower() {
        if (mc.player.onGround()) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick > 0 && mc.player.getDeltaMovement().y <= 0.0D
                && mc.player.getDeltaMovement().y >= -0.09D) {
            setTowerMotion(-0.38D);
            towerTick = 0;
        }
    }

    private void runRavenPredictionAcceleratedTower(boolean turbo) {
        var player = mc.player;
        if (player.onGround()) {
            towerLaunchY = player.getY();
            setTowerMotion(0.42D);
            towerTick = 1;
            return;
        }
        if (towerTick != 1) return;

        double gained = player.getY() - towerLaunchY;
        int expectedBlockY = Mth.floor(towerLaunchY + 1.0E-4D);
        boolean blockReady = hasPlacedSupport(expectedBlockY);
        double minimumHeight = turbo ? 1.001D : 1.105D;
        double earliestMotion = turbo ? 0.28D : 0.17D;
        double motion = player.getDeltaMovement().y;
        if (blockReady && gained >= minimumHeight
                && motion <= earliestMotion && motion >= -0.09D) {
            setTowerMotion(turbo ? -0.46D : -0.39D);
            towerTick = 2;
            return;
        }

        // The source retains Raven Prediction's apex fallback for delayed placement.
        if (motion <= 0.0D && motion >= -0.09D) {
            setTowerMotion(-0.38D);
            towerTick = 2;
        }
    }

    private void runRavenPredictionVariant(double earlyMotionLimit, double earlyPull,
                                           double minRise) {
        var player = mc.player;
        if (player.onGround()) {
            towerLaunchY = player.getY();
            setTowerMotion(0.42D);
            towerTick = 1;
            return;
        }
        if (towerTick <= 0) return;

        double motion = player.getDeltaMovement().y;
        double rise = player.getY() - towerLaunchY;
        int neededBlockY = Mth.floor(towerLaunchY);
        if (hasPlacedSupport(neededBlockY) && rise >= minRise
                && motion <= earlyMotionLimit && motion >= -0.09D) {
            setTowerMotion(earlyPull);
            towerTick = 0;
            return;
        }
        if (motion <= 0.0D && motion >= -0.09D) {
            setTowerMotion(-0.38D);
            towerTick = 0;
        }
    }

    private void runYuriNCPTower() {
        var player = mc.player;
        double fraction = towerFraction();
        if (towerTick <= 0 || player.onGround()) {
            double baseY = Math.floor(player.getY() + 1.0E-4D);
            if (!setTowerPositionY(baseY)) return;
            towerLaunchY = baseY;
            setTowerMotion(0.41998D);
            towerTick = 1;
            return;
        }

        double gained = player.getY() - towerLaunchY;
        if (gained >= 0.985D && (fraction <= 0.08D || fraction >= 0.985D)) {
            double nextY = Math.floor(player.getY() + 0.02D);
            if (nextY <= towerLaunchY) nextY = towerLaunchY + 1.0D;
            // Modern position changes bypass movement collision resolution, so
            // validate the swept box, landing support and launch headroom first.
            if (!setTowerPositionY(nextY)) return;
            towerLaunchY = nextY;
            setTowerMotion(0.41998D);
            towerTick = 1;
        }
    }

    private void runRavenPullSyncTower() {
        var player = mc.player;
        double fraction = towerFraction();
        if (player.onGround() || towerTick <= 0) {
            towerLaunchY = player.getY();
            setTowerMotion(0.42D);
            towerTick = 1;
            return;
        }

        if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.50D) {
            setTowerMotion(0.33D);
            towerTick = 2;
            return;
        }
        if (towerTick == 2 && fraction >= 0.71D) {
            setTowerMotion(Math.max(0.035D, 1.025D - fraction));
            towerTick = 3;
            return;
        }

        if (towerTick >= 3 && (fraction < 0.12D || player.getY() >= towerLaunchY + 0.985D)) {
            int supportY = Mth.floor(player.getY()) - 1;
            if (hasCornerSupport(supportY) && hasVerticalClearance(0.42D)) {
                towerLaunchY = Math.floor(player.getY());
                setTowerMotion(0.42D);
                towerTick = 1;
            } else {
                setTowerMotion(-0.0784000015258789D);
                towerTick = 4;
            }
            return;
        }

        if (towerTick >= 4 && player.getDeltaMovement().y <= 0.10D
                && player.getDeltaMovement().y >= -0.10D) {
            setTowerMotion(-0.0784000015258789D);
        }
        if (towerTick >= 4 && player.onGround()) {
            towerTick = 0;
        }
    }

    private void runRavenPullTower() {
        var player = mc.player;
        double fraction = towerFraction();
        if (player.onGround() || towerTick <= 0) {
            towerLaunchY = player.getY();
            setTowerMotion(0.42D);
            towerTick = 1;
            return;
        }

        if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.50D) {
            setTowerMotion(0.33D);
            towerTick = 2;
            return;
        }
        if (towerTick == 2 && fraction >= 0.71D) {
            setTowerMotion(Math.max(0.03D, 1.005D - fraction));
            towerTick = 3;
            return;
        }

        if (towerTick >= 3) {
            if (fraction < 0.10D || player.getY() >= towerLaunchY + 0.98D) {
                setTowerMotion(-0.08D);
                towerTick = 4;
                return;
            }
            if (player.getDeltaMovement().y <= 0.10D && player.getDeltaMovement().y >= -0.10D) {
                setTowerMotion(-0.22D);
                towerTick = 4;
                return;
            }
        }
        if (towerTick >= 4 && player.onGround()) {
            towerTick = 0;
        }
    }

    private void runOnGroundTower() {
        double fraction = towerFraction();
        if (mc.player.onGround() && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.48D) {
            setTowerMotion(0.33D);
            towerTick = 2;
        } else if (towerTick == 2 && fraction >= 0.72D) {
            setTowerMotion(Math.max(0.005D, 1.0D - fraction));
            towerSpoofGroundThisTick = true;
            towerTick = 3;
        } else if (towerTick >= 3 && (fraction < 0.08D || fraction > 0.98D)) {
            setTowerMotion(0.005D);
            towerSpoofGroundThisTick = true;
            towerTick = 0;
        }
    }

    private void runOnGroundEarlyTower() {
        double fraction = towerFraction();
        if (mc.player.onGround() && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.48D) {
            setTowerMotion(0.33D);
            towerSpoofGroundThisTick = true;
            towerTick = 2;
        } else if (towerTick == 2 && fraction >= 0.72D) {
            setTowerMotion(Math.max(0.005D, 1.0D - fraction));
            towerSpoofGroundThisTick = true;
            towerTick = 3;
        } else if (towerTick >= 3 && (fraction < 0.08D || fraction > 0.98D)) {
            setTowerMotion(0.005D);
            towerTick = 0;
        }
    }

    private void runOnGroundPulseTower() {
        double fraction = towerFraction();
        if (mc.player.onGround() && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.48D) {
            setTowerMotion(0.33D);
            towerTick = 2;
        } else if (towerTick == 2 && fraction >= 0.72D) {
            // Keep the source's 4 cm placement window above the next block.
            setTowerMotion(Math.max(0.040D, 1.040D - fraction));
            towerSpoofGroundThisTick = true;
            towerTick = 3;
        } else if (towerTick >= 3 && (fraction < 0.12D || fraction > 0.97D)) {
            setTowerMotion(-0.005D);
            towerSpoofGroundThisTick = true;
            towerTick = 0;
        }
    }

    private void runOnGroundPredictionTower() {
        double fraction = towerFraction();
        if (mc.player.onGround() && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.48D) {
            setTowerMotion(0.33D);
            towerTick = 2;
        } else if (towerTick >= 2 && fraction >= 0.72D) {
            setTowerMotion(Math.max(0.005D, 1.0D - fraction));
            towerSpoofGroundThisTick = true;
            towerTick = 3;
        } else if (towerTick >= 3 && mc.player.getDeltaMovement().y <= 0.0D
                && mc.player.getDeltaMovement().y >= -0.09D) {
            setTowerMotion(-0.18D);
            towerSpoofGroundThisTick = true;
            towerTick = 0;
        }
    }
}
