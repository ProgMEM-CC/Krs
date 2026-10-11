package com.instrumentalist.krs.hacks.features.player;

/**
 * Dependency-free tower state and motion from the root Scaffold.java reference.
 * Session updates and motion updates are separate because the source runs them
 * in pre-update and post-input respectively. No physics is simulated here.
 */
public final class ScaffoldTowerState {
    /** The source's TOWER_OPTIONS, in its saved-profile order. */
    public static final String[] MODES = {"Vanilla", "Sync", "Hypixel", "Apex", "Sync Burst",
            "Sync Early Burst", "Raven 1 Tick", "Raven Prediction", "OnGround", "OnGround Early",
            "OnGround Pulse", "OnGround Prediction", "Yuri NCP", "Raven Pull Sync", "Raven Pull",
            "Raven Predict Early", "Raven Predict Turbo", "Raven Prediction Swift",
            "Raven Prediction Rapid", "Polar", "Intave"};

    public record Input(double x, double y, double z, double motionY,
                        boolean onGround, boolean jumpPotion) {
    }

    /** Source isSolidSupport, not collision-shape or top-face support. */
    @FunctionalInterface
    public interface Support {
        boolean isSolidSupport(int x, int y, int z);
    }

    /** A motion assignment also writes horizontal motion to zero. */
    public record Output(boolean motionChanged, double motionY, boolean positionChanged,
                         double y, boolean spoofGround) {
    }

    /** Every persistent tower field, with the source's names and types. */
    public record State(int towerTick, int towerDelay, int towerLastPlacedY,
                        double towerPrevMotionY, boolean towerWasActive, int towerCycle,
                        double towerLaunchY, boolean towerSpoofGroundThisTick) {
    }

    private int towerTick;
    private int towerDelay;
    private int towerLastPlacedY = Integer.MIN_VALUE;
    private double towerPrevMotionY;
    private boolean towerWasActive;
    private int towerCycle;
    private double towerLaunchY;
    private boolean towerSpoofGroundThisTick;

    public ScaffoldTowerState() {
    }

    /** Allows an exact source-state snapshot to be replayed in parity tests. */
    public ScaffoldTowerState(State state) {
        towerTick = state.towerTick();
        towerDelay = state.towerDelay();
        towerLastPlacedY = state.towerLastPlacedY();
        towerPrevMotionY = state.towerPrevMotionY();
        towerWasActive = state.towerWasActive();
        towerCycle = state.towerCycle();
        towerLaunchY = state.towerLaunchY();
        towerSpoofGroundThisTick = state.towerSpoofGroundThisTick();
    }

    public State state() {
        return new State(towerTick, towerDelay, towerLastPlacedY, towerPrevMotionY,
                towerWasActive, towerCycle, towerLaunchY, towerSpoofGroundThisTick);
    }

    /** Source onPreMotion guard, lines 1111-1113, with fresh activation/mode. */
    public boolean shouldSpoofGround(String mode, boolean active) {
        return towerSpoofGroundThisTick && active && mode.startsWith("OnGround");
    }

    /** Tower portion of source resetRushState(), lines 4086-4093. */
    public void reset() {
        towerTick = 0;
        towerDelay = 0;
        towerLastPlacedY = Integer.MIN_VALUE;
        towerPrevMotionY = 0.0D;
        towerWasActive = false;
        towerCycle = 0;
        towerLaunchY = 0.0D;
        towerSpoofGroundThisTick = false;
    }

    /**
     * Source updateTowerSession(), lines 4358-4376. A null input represents a
     * missing player. Returns true on the source's rushPlannedPlacement clear.
     * Selecting another tower mode does not itself reset the session.
     */
    public boolean updateTowerSession(boolean active, Input player) {
        boolean clearPlannedPlacement = false;
        if (active && !towerWasActive) {
            towerTick = 0;
            towerDelay = 0;
            towerLastPlacedY = Integer.MIN_VALUE;
            towerPrevMotionY = player == null ? 0.0D : player.motionY();
            towerCycle = 0;
            towerLaunchY = player == null ? 0.0D : player.y();
        } else if (!active && towerWasActive) {
            towerTick = 0;
            towerDelay = 0;
            towerLastPlacedY = Integer.MIN_VALUE;
            towerPrevMotionY = 0.0D;
            towerCycle = 0;
            towerLaunchY = 0.0D;
            clearPlannedPlacement = true;
        }
        towerWasActive = active;
        return clearPlannedPlacement;
    }

    /** Source successful active-tower UP-placement bookkeeping, line 6218. */
    public void onPlaced(int targetY) {
        if (towerWasActive) towerLastPlacedY = Math.max(towerLastPlacedY, targetY);
    }

    /** Source matching rushPlannedPlacement UP-placement assignment, line 6235. */
    public void onPlannedUpPlacement(int targetY) {
        towerLastPlacedY = targetY;
    }

    /**
     * Source updateTowerMotion(), lines 4496-4563. The caller supplies the
     * source isTowerActive result and canonical mode name. Unlike a session
     * update, an inactive motion update resets only tick, delay and previous Y
     * motion (and clears this update's ground-spoof flag).
     */
    public Output updateTowerMotion(String mode, boolean active, Input input, Support support) {
        Frame player = input == null ? null : new Frame(input, support);
        towerSpoofGroundThisTick = false;
        if (!active || player == null) {
            towerTick = 0;
            towerDelay = 0;
            towerPrevMotionY = 0.0D;
            return output(player);
        }

        if (mode.equals("Vanilla")) {
            towerTick = 0;
            towerDelay = 0;
            towerPrevMotionY = player.motionY;
            return output(player);
        }

        if (player.onGround && towerTick == 0) {
            towerCycle++;
            towerLaunchY = player.posY;
        }

        if (mode.equals("Sync")) {
            runSyncTower(player, false);
        } else if (mode.equals("Hypixel")) {
            runHypixelTower(player);
        } else if (mode.equals("Apex")) {
            runApexTower(player);
        } else if (mode.equals("Sync Burst")) {
            if (towerCycle % 3 == 0) runPulseTower(player, false);
            else runSyncTower(player, false);
        } else if (mode.equals("Sync Early Burst")) {
            if (towerCycle % 3 == 0) runPulseVariant(player, 0.64D, 1.015D, 0.13D, -0.10D);
            else runSyncTower(player, false);
        } else if (mode.equals("Raven 1 Tick")) {
            runRaven1Tower(player);
        } else if (mode.equals("Raven Prediction")) {
            runRavenPredictionTower(player);
        } else if (mode.equals("OnGround")) {
            runOnGroundTower(player);
        } else if (mode.equals("OnGround Early")) {
            runOnGroundEarlyTower(player);
        } else if (mode.equals("OnGround Pulse")) {
            runOnGroundPulseTower(player);
        } else if (mode.equals("OnGround Prediction")) {
            runOnGroundPredictionTower(player);
        } else if (mode.equals("Yuri NCP")) {
            runYuriNCPTower(player);
        } else if (mode.equals("Raven Pull Sync")) {
            runRavenPullSyncTower(player);
        } else if (mode.equals("Raven Pull")) {
            runRavenPullTower(player);
        } else if (mode.equals("Raven Predict Early")) {
            runRavenPredictionAcceleratedTower(player, false);
        } else if (mode.equals("Raven Predict Turbo")) {
            runRavenPredictionAcceleratedTower(player, true);
        } else if (mode.equals("Raven Prediction Swift")) {
            runRavenPredictionVariant(player, 0.10D, -0.38D, 1.04D);
        } else if (mode.equals("Raven Prediction Rapid")) {
            runRavenPredictionVariant(player, 0.17D, -0.42D, 1.005D);
        } else if (mode.equals("Polar")) {
            runPolarTower(player);
        } else if (mode.equals("Intave")) {
            runIntaveTower(player);
        }

        towerPrevMotionY = player.motionY;
        return output(player);
    }

    private Output output(Frame player) {
        return player == null ? new Output(false, 0.0D, false, 0.0D, towerSpoofGroundThisTick)
                : new Output(player.motionChanged, player.motionY, player.positionChanged,
                player.posY, towerSpoofGroundThisTick);
    }

    private static final class Frame {
        private final double posX;
        private double posY;
        private final double posZ;
        private double motionY;
        private final boolean onGround;
        private final boolean jumpPotion;
        private final Support support;
        private boolean motionChanged;
        private boolean positionChanged;

        private Frame(Input input, Support support) {
            posX = input.x();
            posY = input.y();
            posZ = input.z();
            motionY = input.motionY();
            onGround = input.onGround();
            jumpPotion = input.jumpPotion();
            this.support = support;
        }
    }

    private void setTowerMotion(Frame player, double motionY) {
        player.motionY = motionY;
        player.motionChanged = true;
    }

    private double towerFraction(Frame player) {
        double fraction = player.posY - Math.floor(player.posY);
        return fraction < 0.0D ? fraction + 1.0D : fraction;
    }

    /** Source MathHelper.floor_double, including its narrowing/overflow behavior. */
    private static int floor(double value) {
        int integer = (int) value;
        return value < (double) integer ? integer - 1 : integer;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return value < minimum ? minimum : (value > maximum ? maximum : value);
    }

    // Source lines 4570-4644, in source method order.
    private void runSyncTower(Frame player, boolean unused) {
        double fraction = towerFraction(player);
        if (player.onGround && towerTick == 0) {
            setTowerMotion(player, 0.42D);
            towerTick = 1;
        } else if (towerTick > 0 && player.motionY <= 0.0D && fraction <= 0.12D) {
            setTowerMotion(player, -0.08D);
            towerTick = 0;
        }
    }

    private void runPulseTower(Frame player, boolean unused) {
        runPulseVariant(player, 0.68D, 1.02D, 0.14D, -0.12D);
    }

    private void runHypixelTower(Frame player) {
        if (player.jumpPotion) {
            towerTick = 0;
            towerDelay = 0;
            return;
        }
        if (player.onGround) {
            setTowerMotion(player, 0.42D);
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
            setTowerMotion(player, player.motionY - 0.03D);
        } else if (towerDelay == 5) {
            setTowerMotion(player, player.motionY - 0.50D);
        }
    }

    private void runPulseVariant(Frame player, double triggerFraction, double targetHeight,
                                 double resetFraction, double catchMotion) {
        double fraction = towerFraction(player);
        if (player.onGround && towerTick == 0) {
            setTowerMotion(player, 0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= triggerFraction && player.motionY > 0.0D) {
            setTowerMotion(player, Math.max(0.12D, targetHeight - fraction));
            towerTick = 2;
        } else if (towerTick == 2 && fraction <= resetFraction) {
            setTowerMotion(player, catchMotion);
            towerTick = 0;
        }
    }

    private void runApexTower(Frame player) {
        if (player.onGround && towerTick == 0) {
            setTowerMotion(player, 0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && towerPrevMotionY > 0.0D && player.motionY <= 0.0D) {
            setTowerMotion(player, -0.20D);
            towerTick = 2;
        } else if (towerTick == 2 && player.onGround) {
            towerTick = 0;
        }
    }

    private void runRaven1Tower(Frame player) {
        if (player.onGround) towerTick = 0;
        int tick = towerTick++;
        double fraction = towerFraction(player);
        if (tick == 0) setTowerMotion(player, 0.42D);
        else if (tick == 1) setTowerMotion(player, 0.33D);
        else if (tick == 2) setTowerMotion(player, Math.max(0.005D, 1.0D - fraction));
        else if (tick == 3) setTowerMotion(player, 0.005D);
    }

    // Source lines 4649-4715. Polar intentionally uses y + 1, not a shape top.
    private void runPolarTower(Frame player) {
        if (player == null) return;
        if (player.onGround) {
            setTowerMotion(player, 0.42D);
            towerTick = 1;
            return;
        }
        if (player.motionY >= 0.0D) return;
        double feetY = player.posY;
        int firstY = floor(feetY - 0.01D);
        for (int y = firstY; y >= firstY - 3; y--) {
            if (!hasCornerSupport(player, player.posX, player.posZ, y)) continue;
            double distanceToGround = feetY - (y + 1.0D);
            if (distanceToGround >= 0.0D && distanceToGround < 1.26D)
                setTowerMotion(player, player.motionY - 0.091F);
            return;
        }
    }

    private void runIntaveTower(Frame player) {
        if (player == null) return;
        if (player.onGround) {
            towerLaunchY = Math.floor(player.posY + 1.0E-4D);
            towerTick = 1;
            towerDelay = 0;
            setTowerMotion(player, 0.42D);
            return;
        }
        if (towerTick <= 0) return;

        double rise = player.posY - towerLaunchY;
        int supportY = floor(towerLaunchY);
        boolean placedSupport = towerLastPlacedY >= supportY
                && hasCornerSupport(player, player.posX, player.posZ, supportY);

        if (towerTick == 1 && rise >= 0.65D && rise < 0.99D && player.motionY > 0.0D) {
            double liftMotion = clamp(1.045D - rise, 0.14D, 0.38D);
            setTowerMotion(player, liftMotion);
            towerTick = 2;
            return;
        }

        if (placedSupport && rise >= 1.001D && rise < 1.6D && towerTick <= 2) {
            setTowerMotion(player, -0.48D);
            towerTick = 3;
            return;
        }

        if (rise < -0.25D || player.motionY < -0.75D) {
            towerTick = 4;
        }
    }

    // Source lines 4734-4842. Swift/Rapid consult placement history alone.
    private void runRavenPredictionTower(Frame player) {
        if (player.onGround) {
            setTowerMotion(player, 0.42D);
            towerTick = 1;
        } else if (towerTick > 0 && player.motionY <= 0.0D && player.motionY >= -0.09D) {
            setTowerMotion(player, -0.38D);
            towerTick = 0;
        }
    }

    private void runRavenPredictionAcceleratedTower(Frame player, boolean turbo) {
        if (player.onGround) {
            towerLaunchY = player.posY;
            setTowerMotion(player, 0.42D);
            towerTick = 1;
            return;
        }
        if (towerTick != 1) return;

        final double gained = player.posY - towerLaunchY;
        final int expectedBlockY = floor(towerLaunchY + 1.0E-4D);
        final boolean blockReady = towerLastPlacedY >= expectedBlockY
                && hasCornerSupport(player, player.posX, player.posZ, expectedBlockY);

        final double minimumHeight = turbo ? 1.001D : 1.105D;
        final double earliestMotion = turbo ? 0.28D : 0.17D;
        if (blockReady && gained >= minimumHeight
                && player.motionY <= earliestMotion
                && player.motionY >= -0.09D) {
            setTowerMotion(player, turbo ? -0.46D : -0.39D);
            towerTick = 2;
            return;
        }

        if (player.motionY <= 0.0D && player.motionY >= -0.09D) {
            setTowerMotion(player, -0.38D);
            towerTick = 2;
        }
    }

    private void runRavenPredictionVariant(Frame player, double earlyMotionLimit, double earlyPull,
                                           double minRise) {
        if (player.onGround) {
            towerLaunchY = player.posY;
            setTowerMotion(player, 0.42D);
            towerTick = 1;
            return;
        }
        if (towerTick <= 0) return;

        double motion = player.motionY;
        double rise = player.posY - towerLaunchY;
        int neededBlockY = floor(towerLaunchY);
        boolean blockPlacedThisLevel = towerLastPlacedY >= neededBlockY;

        if (blockPlacedThisLevel && rise >= minRise
                && motion <= earlyMotionLimit && motion >= -0.09D) {
            setTowerMotion(player, earlyPull);
            towerTick = 0;
            return;
        }

        if (motion <= 0.0D && motion >= -0.09D) {
            setTowerMotion(player, -0.38D);
            towerTick = 0;
        }
    }

    private void setTowerPositionY(Frame player, double y) {
        // The modern adapter's LocalPlayer is the source's non-null raw player.
        if (player == null) return;
        player.posY = y;
        player.positionChanged = true;
    }

    private void runYuriNCPTower(Frame player) {
        double fraction = towerFraction(player);

        if (towerTick <= 0 || player.onGround) {
            double baseY = Math.floor(player.posY + 1.0E-4D);
            setTowerPositionY(player, baseY);
            towerLaunchY = baseY;
            setTowerMotion(player, 0.41998D);
            towerTick = 1;
            return;
        }

        double gained = player.posY - towerLaunchY;
        if (gained >= 0.985D && (fraction <= 0.08D || fraction >= 0.985D)) {
            double nextY = Math.floor(player.posY + 0.02D);
            if (nextY <= towerLaunchY) nextY = towerLaunchY + 1.0D;
            setTowerPositionY(player, nextY);
            towerLaunchY = nextY;
            setTowerMotion(player, 0.41998D);
            towerTick = 1;
        }
    }

    // Source lines 4848-4930. Preserve the early returns and repeated pulls.
    private void runRavenPullSyncTower(Frame player) {
        double fraction = towerFraction(player);

        if (player.onGround || towerTick <= 0) {
            towerLaunchY = player.posY;
            setTowerMotion(player, 0.42D);
            towerTick = 1;
            return;
        }

        if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.50D) {
            setTowerMotion(player, 0.33D);
            towerTick = 2;
            return;
        }

        if (towerTick == 2 && fraction >= 0.71D) {
            setTowerMotion(player, Math.max(0.035D, 1.025D - fraction));
            towerTick = 3;
            return;
        }

        if (towerTick >= 3 && (fraction < 0.12D || player.posY >= towerLaunchY + 0.985D)) {
            int supportY = floor(player.posY) - 1;
            if (hasCornerSupport(player, player.posX, player.posZ, supportY)) {
                towerLaunchY = Math.floor(player.posY);
                setTowerMotion(player, 0.42D);
                towerTick = 1;
            } else {
                setTowerMotion(player, -0.0784000015258789D);
                towerTick = 4;
            }
            return;
        }

        if (towerTick >= 4 && player.motionY <= 0.10D && player.motionY >= -0.10D) {
            setTowerMotion(player, -0.0784000015258789D);
        }

        if (towerTick >= 4 && player.onGround) {
            towerTick = 0;
        }
    }

    private void runRavenPullTower(Frame player) {
        double fraction = towerFraction(player);

        if (player.onGround || towerTick <= 0) {
            towerLaunchY = player.posY;
            setTowerMotion(player, 0.42D);
            towerTick = 1;
            return;
        }

        if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.50D) {
            setTowerMotion(player, 0.33D);
            towerTick = 2;
            return;
        }

        if (towerTick == 2 && fraction >= 0.71D) {
            setTowerMotion(player, Math.max(0.03D, 1.005D - fraction));
            towerTick = 3;
            return;
        }

        if (towerTick >= 3) {
            if (fraction < 0.10D || player.posY >= towerLaunchY + 0.98D) {
                setTowerMotion(player, -0.08D);
                towerTick = 4;
                return;
            }
            if (player.motionY <= 0.10D && player.motionY >= -0.10D) {
                setTowerMotion(player, -0.22D);
                towerTick = 4;
                return;
            }
        }

        if (towerTick >= 4 && player.onGround) {
            towerTick = 0;
        }
    }

    // Source lines 4932-5011. In Prediction the fraction branch wins over apex.
    private void runOnGroundTower(Frame player) {
        double fraction = towerFraction(player);
        if (player.onGround && towerTick == 0) {
            setTowerMotion(player, 0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.48D) {
            setTowerMotion(player, 0.33D);
            towerTick = 2;
        } else if (towerTick == 2 && fraction >= 0.72D) {
            setTowerMotion(player, Math.max(0.005D, 1.0D - fraction));
            towerSpoofGroundThisTick = true;
            towerTick = 3;
        } else if (towerTick >= 3 && (fraction < 0.08D || fraction > 0.98D)) {
            setTowerMotion(player, 0.005D);
            towerSpoofGroundThisTick = true;
            towerTick = 0;
        }
    }

    private void runOnGroundEarlyTower(Frame player) {
        double fraction = towerFraction(player);
        if (player.onGround && towerTick == 0) {
            setTowerMotion(player, 0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.48D) {
            setTowerMotion(player, 0.33D);
            towerSpoofGroundThisTick = true;
            towerTick = 2;
        } else if (towerTick == 2 && fraction >= 0.72D) {
            setTowerMotion(player, Math.max(0.005D, 1.0D - fraction));
            towerSpoofGroundThisTick = true;
            towerTick = 3;
        } else if (towerTick >= 3 && (fraction < 0.08D || fraction > 0.98D)) {
            setTowerMotion(player, 0.005D);
            towerTick = 0;
        }
    }

    private void runOnGroundPulseTower(Frame player) {
        double fraction = towerFraction(player);
        if (player.onGround && towerTick == 0) {
            setTowerMotion(player, 0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.48D) {
            setTowerMotion(player, 0.33D);
            towerTick = 2;
        } else if (towerTick == 2 && fraction >= 0.72D) {
            setTowerMotion(player, Math.max(0.040D, 1.040D - fraction));
            towerSpoofGroundThisTick = true;
            towerTick = 3;
        } else if (towerTick >= 3 && (fraction < 0.12D || fraction > 0.97D)) {
            setTowerMotion(player, -0.005D);
            towerSpoofGroundThisTick = true;
            towerTick = 0;
        }
    }

    private void runOnGroundPredictionTower(Frame player) {
        double fraction = towerFraction(player);
        if (player.onGround && towerTick == 0) {
            setTowerMotion(player, 0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.48D) {
            setTowerMotion(player, 0.33D);
            towerTick = 2;
        } else if (towerTick >= 2 && fraction >= 0.72D) {
            setTowerMotion(player, Math.max(0.005D, 1.0D - fraction));
            towerSpoofGroundThisTick = true;
            towerTick = 3;
        } else if (towerTick >= 3 && player.motionY <= 0.0D && player.motionY >= -0.09D) {
            setTowerMotion(player, -0.18D);
            towerSpoofGroundThisTick = true;
            towerTick = 0;
        }
    }

    /** Source four-corner query and short-circuit order, lines 6275-6279. */
    private boolean hasCornerSupport(Frame player, double x, double z, int y) {
        return player.support.isSolidSupport(floor(x - 0.3), y, floor(z - 0.3))
                || player.support.isSolidSupport(floor(x + 0.3), y, floor(z - 0.3))
                || player.support.isSolidSupport(floor(x - 0.3), y, floor(z + 0.3))
                || player.support.isSolidSupport(floor(x + 0.3), y, floor(z + 0.3));
    }
}
