package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.Client;
import com.instrumentalist.krs.utils.entity.PlayerUtil;
import com.instrumentalist.krs.utils.move.InputUtil;
import com.instrumentalist.krs.utils.packet.PacketUtil;
import net.minecraft.client.player.ClientInput;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Direct gameplay translation of root Scaffold.java. Method names and source line ranges
 * are retained for review; only API edges are delegated to RavenScaffoldAdapter.
 * HUD/editor/debug output and mode keybind UI are not part of this gameplay translation.
 * Tower motion is delegated to ScaffoldTowerController in the source post-input phase.
 */
final class RavenScaffoldCore extends RavenScaffoldAdapter {
    static final String REPLACEABLE_BLOCK_NAMES = ",air,water,flowing_water,lava,flowing_lava,fire,tallgrass,deadbush,snow_layer,double_plant,vine,sapling,yellow_flower,red_flower,brown_mushroom,red_mushroom,wheat,carrots,potatoes,nether_wart,reeds,";
    private static final String UNPLACEABLE_BLOCK_NAMES = ",snow_layer,daylight_detector,beacon,end_portal_frame,end_portal,lever,double_plant,waterlily,tripwire_hook,tallgrass,ladder,gravel,clay,sand,soul_sand,chest,trapped_chest,ender_chest,furnace,lit_furnace,jukebox,enchanting_table,dropper,dispenser,hopper,anvil,noteblock,crafting_table,mob_spawner,brewing_stand,bed,";
    private static final String UNPLACEABLE_BLOCK_TYPES = ",BlockSnow,BlockWeb,BlockSapling,BlockDaylightDetector,BlockBeacon,BlockBanner,BlockEndPortalFrame,BlockEndPortal,BlockLever,BlockButton,BlockSkull,BlockLiquid,BlockCactus,BlockDoublePlant,BlockLilyPad,BlockCarpet,BlockTripWire,BlockTripWireHook,BlockTallGrass,BlockFlower,BlockFlowerPot,BlockSign,BlockLadder,BlockTorch,BlockRedstoneTorch,BlockStairs,BlockSlab,BlockFence,BlockPane,BlockStainedGlassPane,BlockGravel,BlockClay,BlockSand,BlockSoulSand,BlockRailBase,BlockFenceGate,BlockBasePressurePlate,BlockAnvil,BlockEnchantmentTable,BlockChest,BlockTrapDoor,BlockDoor,BlockJukebox,BlockEnderChest,BlockBrewingStand,BlockBed,BlockDropper,BlockDispenser,BlockHopper,BlockNote,BlockWorkbench,BlockFurnace,BlockMobSpawner,";
    private static final double ROTATION_INCREMENT = (double) 0.0096f;
    private static final float TAKEOFF_YAW_MIN = 90F, TAKEOFF_YAW_MAX = 95F, AIRBORNE_YAW_MIN = 30F, AIRBORNE_YAW_MAX = 35F;
    private static final float LONG_TELLY_SMOOTH_TICKS = 9F, GROUND_YAW_HOLD_ANGLE = 112.5F, SPRINT_JUMP_ALIGNMENT = 12F;
    private static final int LOOK_DIAGONAL_BUCKETS = 1, GROUND_BUCKET_SEARCH_RADIUS = 1, GROUND_DIAGONAL_BUCKETS = 1;
    private static final int SPRINT_JUMP_HOLD_TICKS = 1, LOOK_LOCK_MISS_LIMIT = 4;
    private static final float YAW_BUCKET_STEP = 1F, ACTIVATION_SMOOTH_TICKS = 3F, ACTIVATION_YAW_LAG = 0.72F;
    private static final float SPRINT_MAX_YAW_OFFSET = 50F, MOUSE_ROTATION_INCREMENT = 0.023437500F;
    private static final float MIN_YAW_JITTER = 0.11F, MAX_YAW_JITTER = 0.46F;
    private static final double[] FACE_SAMPLE_OFFSETS = {0.03125, 0.09375, 0.15625, 0.21875, 0.28125, 0.34375, 0.40625, 0.46875, 0.53125, 0.59375, 0.65625, 0.71875, 0.78125, 0.84375, 0.90625, 0.96875};
    private static final double[] FACE_MIN_OFFSET = {0}, FACE_MAX_OFFSET = {1}, TOP_FACE_SAMPLES = {0.5, 0.25, 0.75}, SIDE_FACE_SAMPLES = {0.5, 0.8, 0.2};
    private static final EnumFacing[] HORIZONTAL_FACES = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
    private static final EnumFacing[] PLACEMENT_FACES = {EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
    private static final float[] HYPIXEL_YAW_OFFSETS = {
            0,-1,1,-2,2,-3,3,-4,4,-5,5,-6,6,-7,7,-8,8,-9,9,-10,10,-11,11,-12,12,-13,13,-14,14,-15,15,-16,16,-17,17,-18,18,-19,19,-20,20,
            -22,22,-24,24,-26,26,-28,28,-30,30,-32,32,-34,34,-36,36,-38,38,-40,40,-42,42,-44,44,-46,46,-48,48,-50,50,
            -52,52,-54,54,-56,56,-58,58,-60,60,-62,62,-64,64,-66,66,-68,68,-70,70,-72,72,-74,74,-76,76,-78,78,-80,80,-82,82,-84,84,-86,86,-88,88,-90,90};
    private static final float[] HYPIXEL_PITCH_OFFSETS = {0,-1,1,-2,2,-3,3,-4,4,-5,5,-6,6,-7,7,-8,8,-9,9,-10,10,-11,11,-12,12,-13,13,-14,14,-15,15,-16,16,-17,17,-18,18,-19,19,-20,20};

    private final Setting multiplace = new Setting(ScaffoldModeController.multiPlace), iceThreshold = new Setting(ScaffoldModeController.iceThreshold), clickSpeed = new Setting(ScaffoldModeController.clickSpeed);
    private final Setting autoSwap = new Setting(ScaffoldModeController.autoSwap), disableInGui = new Setting(ScaffoldModeController.disableInGui), disableOnJumpPotion = new Setting(ScaffoldModeController.disableOnJumpPotion);
    private final Setting keepYOnRightClick = new Setting(ScaffoldModeController.keepYOnRightClick), tellyOnJump = new Setting(ScaffoldModeController.tellyOnJump), alwaysSprint = new Setting(ScaffoldModeController.alwaysSprint);
    private final Setting silentSwing = new Setting(ScaffoldModeController.silentSwing), spoofItem = new Setting(ScaffoldModeController.spoofItem);
    private final Setting ninjaEdgeDistance = new Setting(ScaffoldModeController.ninjaEdgeDistance), ninjaMinDelay = new Setting(ScaffoldModeController.ninjaMinDelay), ninjaMaxDelay = new Setting(ScaffoldModeController.ninjaMaxDelay), ninjaSneakOnJump = new Setting(ScaffoldModeController.ninjaSneakOnJump);
    private final Setting ninjaRequireSneakKey = new Setting(ScaffoldModeController.ninjaRequireSneakKey), ninjaRequireBlocks = new Setting(ScaffoldModeController.ninjaRequireBlocks), ninjaRequireLookingDown = new Setting(ScaffoldModeController.ninjaRequireLookingDown), ninjaDisableForward = new Setting(ScaffoldModeController.ninjaDisableForward), ninjaAvoidDoubleSneaking = new Setting(ScaffoldModeController.ninjaAvoidDoubleSneaking);
    private final Setting breezilyAutoStrafe = new Setting(ScaffoldModeController.breezilyAutoStrafe), breezilyOnlyStrafeOnGround = new Setting(ScaffoldModeController.breezilyOnlyStrafeOnGround), breezilyGroundWaitTicks = new Setting(ScaffoldModeController.breezilyGroundWaitTicks), breezilySwitchSpeed = new Setting(ScaffoldModeController.breezilySwitchSpeed);
    private final Setting rushLookAhead = new Setting(ScaffoldModeController.rushLookAhead), rushChain = new Setting(ScaffoldModeController.rushChain), rushAutoHop = new Setting(ScaffoldModeController.rushAutoHop);
    private final Setting sourceScaffoldMode = new Setting(ScaffoldModeController.sourceScaffoldMode), sourceSprintMode = new Setting(ScaffoldModeController.sourceSprintMode), sourceTellyWatchdog = new Setting(ScaffoldModeController.sourceTellyWatchdog), sourceTellyStraightTicks = new Setting(ScaffoldModeController.sourceTellyStraightTicks), sourceTellyDiagonalTicks = new Setting(ScaffoldModeController.sourceTellyDiagonalTicks), sourceTellyJumpDownTicks = new Setting(ScaffoldModeController.sourceTellyJumpDownTicks), sourcePlaceDelay = new Setting(ScaffoldModeController.sourcePlaceDelay);
    private final Setting sourceAutoJump = new Setting(ScaffoldModeController.sourceAutoJump), sourceSneak = new Setting(ScaffoldModeController.sourceSneak), sourceSneakEvery = new Setting(ScaffoldModeController.sourceSneakEvery);
    private final Setting hopMode = new Setting(ScaffoldModeController.hopMode), hopSpeed = new Setting(ScaffoldModeController.hopSpeed), hopAutoSprint = new Setting(ScaffoldModeController.hopAutoSprint);

    String selectedMode = "God Bridge", selectedTowerMode = "Vanilla";
    boolean towerEnabled = true;
    private final ScaffoldTowerController tower;
    private final Consumer<net.minecraft.core.BlockPos> placementObserver;
    private final Runnable legacyNcpTower;
    private int originalSlot = -1, placementSlot = -1, heldBlockCount = -1, keepYStage, keepYStartHeight = 256;
    private boolean hasSwapped, physicalSlotSwapped, useCurrentHeight;
    private float ninjaBridgeYaw = Float.NaN, ninjaBridgePitch = 80F, ninjaBaseYaw = Float.NaN, ninjaAppliedYaw = Float.NaN, ninjaAimAnchorYaw = Float.NaN;
    private int ninjaBridgeInputMask, ninjaDiagonalSide, ninjaLastDiagonalSide, ninjaAimMissTicks, ninjaAimRecoveryCooldown, ninjaSneakSafeTicks;
    private boolean ninjaPitchRecoveryUsed, ninjaSneaking, ninjaJumpKeyWasDown;
    private long ninjaReleaseAt, ninjaJumpSneakUntil;
    private BlockPos ninjaTrackedSupportBlock, ninjaEdgePulseSupport;
    private int breezilyTickCounter, breezilyGroundWaitRemaining;
    private boolean breezilyUseLeft = true;
    private float breezilyLockedYaw = Float.NaN, breezilyViewYaw = Float.NaN, breezilyViewPitch = Float.NaN;
    private BlockPos rushLastPlacedTarget, rushWarmTarget;
    private Placement rushPlannedPlacement;
    private boolean rushTieXNext = true;
    private int rushHeadingX, rushHeadingZ, rushMissStreak, rushWarmTicks, rushLaneAge;
    private EnumFacing rushWarmFace;
    private float rushLaneYaw = Float.NaN, rushLanePitch = Float.NaN;
    private boolean towerWasActive;
    private int towerLastPlacedY = Integer.MIN_VALUE;
    private int hypixelPlaceTimer, sourceAirTicks, sourceGroundTicks, sourcePlaceDelayTicks, sourceBlocksPlaced, sourceGodBridgeJumpBlocks, sourceSneakBlocks, sourceDirectionalChange;
    private float hypixelSourceYaw = Float.NaN, hypixelSourcePitch = 90F, snapPlusLastMovementYaw = Float.NaN;
    private double sourceStartY;
    private int hopAirTicks;
    private boolean hopLaunchRequested, snapReturnPending, snapSkipPlacementThisTick;
    private int snapDiagonalHoldTicks;
    private float snapLastPlacementYaw = Float.NaN;
    private int spoofServerSlot = -1;
    private boolean sendingSpoofSlotPacket;
    private String lastScaffoldMode = "", lastSourceScaffoldMode = "";
    boolean sprintSuppressed;
    private boolean sprintWasPressed, sprintStopRequested;
    private int sprintToggleCooldown;
    private final Vec3[] predictedPositions = new Vec3[5];
    private boolean hasPrediction;
    private EntityPlayerSP predictionInputPlayer;
    private float predictionInputYaw = Float.NaN;
    private final Placement[] candidates = new Placement[16];
    private BlockPos neededBlock, verificationBlock;
    private int longTellyMissTicks, verificationDelayTicks, revertedPlacementCount, longTellyYawBucket;
    private boolean longTellySprintJump = true, longTellyWasAirborne, longTellyNeedsSideUpdate = true, longTellyWasEngaged;
    private double previousVerticalMotion;
    private int lastGroundX, lastGroundZ, ticksSinceGrounded = 99, longTellyRowY, longTellySide = 1;
    private boolean hasLongTellyOrigin;
    private int longTellyOriginX, longTellyOriginZ, longTellyForwardX, longTellyForwardZ, longTellyLateralX, longTellyLateralZ;
    // Source has eight storage cells; buildLongTellyQueue intentionally populates three.
    private final BlockPos[] queuedBlocks = new BlockPos[8];
    private final int[] queueRetryTicks = new int[8];
    private int queuedBlockCount, pendingQueueIndex = -1, queueCursor, strafeTapCooldown, queueStallTicks, skippedQueueMask;
    private float longTellyAimYaw, longTellyAimPitch;
    private Placement pendingPlacement;
    private boolean pendingPlacementReady;
    private int placementDelayTicks;
    private float yaw = -180F, pitch;
    private boolean canRotate, tellyJumping, takeoffLatched, placedThisTick;
    private int placementAttemptsThisTick;
    private float lastSentYaw, lastSentPitch, previousSentPitch, takeoffPitch;
    private int activationTicks, jumpHoldTicks, groundPlacementSide = 1;
    private boolean movingCardinally;
    private int lookSide = 1;
    private boolean lookingStraightBack;
    private float activationYawAccumulator;
    boolean rotationSentThisTick;
    private boolean rotationSentLastTick, movementFixActive;
    private float rotationEaseRate = 1F;
    private int cardinalSide = 1;
    private float continuousCameraYaw;
    private boolean hasCameraYaw;
    private float yawGridBase;
    private int groundMissTicks;
    private boolean rotationEaseStarted;
    private float rotationEaseSlowdown;
    private int lastYawBucket;
    private boolean diagonalMovement;
    private float targetYaw, targetPitch, pendingYawJitter, lastBaseYaw, lastBasePitch, yawJitterAmplitude = 0.28F;
    private boolean sendingPlacement, sendingAutoClick;
    private long nextClickAt, clickWindowStart;
    private int clickCount;
    private float measuredClickCps;
    private char clickState = 'D';
    private int debugAimSelection, debugAimScore = -1, debugCandidateCount;
    private double debugOverhang;
    private char debugLongTellyStatus = '-', debugPlacementStatus = '-', debugMissingPlacementReason = 'n';
    private float debugSnappedYaw, debugVerifiedYaw, debugSentYaw, debugPlacementPitchDelta, debugMaxPlacementPitchDelta;
    private Vec3 lastPlacementHit;
    private long lastPlacementTime;

    RavenScaffoldCore(ScaffoldTowerController tower, Consumer<net.minecraft.core.BlockPos> placementObserver, Runnable legacyNcpTower) {
        this.tower = tower; this.placementObserver = placementObserver; this.legacyNcpTower = legacyNcpTower;
    }

    // Source 456-558. Fields not reset by Raven retain their values across re-enables.
    void onEnable() {
        mc.sync();
        originalSlot = mc.thePlayer == null ? -1 : mc.thePlayer.inventory.currentItem;
        placementSlot = -1; hasSwapped = false; physicalSlotSwapped = false; spoofServerSlot = -1;
        sendingSpoofSlotPacket = false; heldBlockCount = -1; placementDelayTicks = 3;
        yaw = -180F; pitch = 0F; canRotate = false; rotationSentThisTick = false; rotationSentLastTick = false;
        movementFixActive = false; resetMovementState(); cardinalSide = 1; sprintSuppressed = false; sprintWasPressed = false;
        neededBlock = null; debugAimSelection = 0; debugOverhang = 0; lastPlacementHit = null; lastPlacementTime = 0;
        longTellySide = 1; keepYStage = 0; keepYStartHeight = 256; useCurrentHeight = false;
        placedThisTick = false; placementAttemptsThisTick = 0; resetNinjaState(); resetBreezilyState(); resetRushState();
        breezilyViewYaw = breezilyViewPitch = Float.NaN;
        snapReturnPending = snapSkipPlacementThisTick = false; snapDiagonalHoldTicks = 0; snapLastPlacementYaw = Float.NaN;
        hypixelPlaceTimer = 0; hypixelSourceYaw = Float.NaN; hypixelSourcePitch = 90F; snapPlusLastMovementYaw = Float.NaN;
        sourceAirTicks = sourceGroundTicks = sourcePlaceDelayTicks = sourceBlocksPlaced = sourceGodBridgeJumpBlocks = sourceSneakBlocks = sourceDirectionalChange = 0;
        sourceStartY = mc.thePlayer == null ? 0 : Math.floor(mc.thePlayer.posY);
        hopAirTicks = 0; hopLaunchRequested = false; lastScaffoldMode = getScaffoldMode(); lastSourceScaffoldMode = "";
    }
    void onDisable() {
        // Never restore a previous world's slot to a new player.
        if (mc.thePlayer != null && mc.thePlayer.raw == nativeMc.player) restoreAutoSwapSlot();
        originalSlot = placementSlot = -1; hasSwapped = physicalSlotSwapped = false;
        rotations.forceMovementFix = false;
        if (sprintSuppressed) mc.gameSettings.keyBindSprint.setDown(sprintWasPressed || physicalDown(mc.gameSettings.keyBindSprint));
        sprintSuppressed = false; resetMovementState(); resetNinjaState(); resetBreezilyState(); resetRushState();
        breezilyViewYaw = breezilyViewPitch = Float.NaN;
        snapReturnPending = snapSkipPlacementThisTick = false; snapDiagonalHoldTicks = 0; snapLastPlacementYaw = Float.NaN;
        hypixelPlaceTimer = 0; hypixelSourceYaw = Float.NaN; hypixelSourcePitch = 90F; snapPlusLastMovementYaw = Float.NaN;
        sourceAirTicks = sourceGroundTicks = sourcePlaceDelayTicks = sourceBlocksPlaced = sourceGodBridgeJumpBlocks = sourceSneakBlocks = sourceDirectionalChange = 0;
        hopAirTicks = 0; hopLaunchRequested = false; preInputMovement = null;
        rotationSentThisTick = rotationSentLastTick = false;
        Client.rotationManager.stopRotation();
    }

    // Source 559-979: preserve ordering, including late Rush/tower overrides and final quantization.
    void onPreUpdate() {
        mc.sync();
        if (!Utils.nullCheck()) return;
        if (shouldPauseForGui()) { pauseForGui(); return; }
        handleModeTransition(); handleSourceStyleTransition();
        Vec3 position = mc.thePlayer.getPositionVector();
        beginTick();
        if (isNinjaMode() && !isTowerActive()) { updateTowerSession(false); updateNinjaClassicPlacement(); return; }
        if (isNinjaMode() && isTowerActive()) resetNinjaPlacementAim();
        if (hypixelPlaceTimer > 0) hypixelPlaceTimer--;
        if (sourcePlaceDelayTicks > 0) sourcePlaceDelayTicks--;
        if (mc.thePlayer.onGround) { sourceGroundTicks++; sourceAirTicks = 0; }
        else { sourceAirTicks++; sourceGroundTicks = 0; }
        updateKeepYState(mc.thePlayer, position);
        selectPlacementStack(mc.thePlayer);
        boolean longTellyEngaged = updateLongTellyState(mc.thePlayer, position);
        float cameraYaw = updateCameraYaw(mc.thePlayer.rotationYaw);
        boolean breezilyGodBridgeJump = isBreezilyGodBridgeJump(), snapGodBridgeJump = isSnapGodBridgeJump();
        if (snapGodBridgeJump) { snapReturnPending = false; snapSkipPlacementThisTick = false; snapDiagonalHoldTicks = 0; snapLastPlacementYaw = Float.NaN; }
        if (isBreezilyMode() || isHypixelMode() && getSourceScaffoldMode().equals("Breezily")) {
            breezilyViewYaw = cameraYaw; breezilyViewPitch = mc.thePlayer.rotationPitch;
        } else { breezilyViewYaw = Float.NaN; breezilyViewPitch = Float.NaN; }
        float movementYaw = getMovementYaw(cameraYaw, getForwardInput(), getStrafeInput());
        float referenceServerYaw = getServerYaw();
        float backwardYaw = unwrapYaw(movementYaw - 180F, referenceServerYaw);
        float diagonalYaw = isDiagonalMovement(movementYaw) ? backwardYaw : unwrapYaw(movementYaw - 135F * updateCardinalSide(movementYaw), referenceServerYaw);
        if (!canRotate) { if (yaw == -180F && pitch == 0F) pitch = quantizeRotation(85F); yaw = quantizeRotation(diagonalYaw); }
        yawGridBase = cameraYaw;
        boolean flatGround = mc.thePlayer.onGround && !tellyJumping && !isTellyTakeoff();
        if (flatGround) { yaw = quantizeRotation(diagonalYaw); canRotate = true; }
        boolean shortDiagonal = isShortDiagonalJump();
        Placement placement = flatGround ? null : findPlacement();
        debugPlacementStatus = flatGround ? 'F' : placement == null ? debugMissingPlacementReason : 'g';
        aimAtPlacement(placement, referenceServerYaw, mc.thePlayer, position);
        boolean tellyRotating = tellyJumping || isTellyTakeoff();
        Vec3 motion = new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
        float previousYaw = rotationSentLastTick ? lastSentYaw : cameraYaw;
        float previousPitch = rotationSentLastTick ? lastSentPitch : mc.thePlayer.rotationPitch;
        boolean urgent = !rotationSentLastTick || isUnsupportedAt(position.x, position.z)
                || isUnsupportedAt(position.x + motion.x * 2, position.z + motion.z * 2)
                || isUnsupportedAt(position.x + motion.x * 3.5, position.z + motion.z * 3.5)
                || isUnsupportedAt(position.x + motion.x * 5, position.z + motion.z * 5) || groundMissTicks >= 3;
        updateTargetRotations(position, motion, previousYaw, previousPitch, urgent, shortDiagonal);
        int lookBucket = updateLookBucket(position, cameraYaw);
        boolean cameraAligned = alignTakeoffRotations(tellyRotating, cameraYaw);
        snapGroundYaw(tellyRotating, position, motion, cameraYaw);
        aimYawWithJitter(targetYaw, nextYawJitter()); debugSnappedYaw = targetYaw;
        aimPitch(RotationUtils.clampPitch(targetPitch));
        int groundCandidateIndex = flatGround ? aimGroundPlacement(mc.thePlayer, position, urgent, previousPitch) : -1;
        boolean placementAllowed = isPlacementAllowed(placement);
        Vec3 verifiedHit = placement != null && placementAllowed ? verifyPlacementAim(placement, tellyRotating, shortDiagonal, lookBucket, cameraAligned) : null;
        if (isBreezilyMode() && !breezilyGodBridgeJump) {
            float fixedBreezilyYaw = getBreezilyLockedYaw(cameraYaw);
            if (flatGround) {
                int breezilyCandidateCount = buildCandidates(MathHelper.floor_double(position.y) - 1);
                debugCandidateCount = breezilyCandidateCount;
                groundCandidateIndex = aimBreezilyGroundCandidate(breezilyCandidateCount, fixedBreezilyYaw); verifiedHit = null;
            } else if (placement != null && placementAllowed) verifiedHit = aimBreezilyPlacement(placement, fixedBreezilyYaw);
            else { targetYaw = fixedBreezilyYaw; targetPitch = quantizeRotation(78F); }
        }
        if (isSnapPlusMode()) {
            groundCandidateIndex = -1; verifiedHit = null; pendingPlacementReady = false; pendingPlacement = null;
            updateHypixelSourceIdleRotation(movementYaw);
            boolean sourceCanPlace = isHypixelSourceCanPlace(position);
            Placement snapPlusPlacement = sourceCanPlace ? findPlacement() : null;
            if (snapPlusPlacement != null && isPlacementAllowed(snapPlusPlacement)) {
                Vec3 snapPlusHit = aimHypixelPlacement(snapPlusPlacement, movementYaw);
                if (snapPlusHit != null) { placement = snapPlusPlacement; verifiedHit = snapPlusHit; }
                else placement = null;
            } else {
                placement = null;
                if (!Float.isNaN(hypixelSourceYaw)) { targetYaw = hypixelSourceYaw; targetPitch = hypixelSourcePitch; pendingYawJitter = 0; }
            }
        }
        if (isHypixelMode()) {
            groundCandidateIndex = -1; verifiedHit = null; pendingPlacementReady = false; pendingPlacement = null;
            String sourceStyle = getSourceScaffoldMode();
            if (sourceStyle.equals("Hypixel")) updateHypixelSourceIdleRotation(movementYaw);
            else if (sourceStyle.equals("Telly")) updateSourceTellyIdleRotation(cameraYaw);
            boolean sourceCanPlace = shouldSourceScaffoldPlace(position, sourceStyle);
            Placement sourcePlacement = sourceCanPlace && sourcePlaceDelayTicks <= 0 ? findPlacement() : null;
            if (sourcePlacement != null && isPlacementAllowed(sourcePlacement)) {
                Vec3 sourceHit = aimSourceScaffoldPlacement(sourcePlacement, sourceStyle, movementYaw, cameraYaw);
                if (sourceHit != null) { placement = sourcePlacement; verifiedHit = sourceHit; } else placement = null;
            } else {
                placement = null;
                if (sourceStyle.equals("Hypixel") && !Float.isNaN(hypixelSourceYaw)) { targetYaw = hypixelSourceYaw; targetPitch = hypixelSourcePitch; pendingYawJitter = 0; }
            }
            if (sourceStyle.equals("God Bridge")) updateSourceGodBridgeRotationState();
            String sourceSprint = sourceSprintMode.getSelectedOption();
            if ("Universal".equals(sourceSprint) && sourceBlocksPlaced >= 3) {
                float fromYaw = rotationSentLastTick ? lastSentYaw : RotationUtils.serverRotations[0];
                targetYaw = quantizeMouseRotation(unwrapYaw(getPlayerYaw(), fromYaw), fromYaw); sourceBlocksPlaced = 0;
            }
        }
        debugVerifiedYaw = targetYaw; debugLongTellyStatus = 'N';
        if (longTellyEngaged && verifiedHit == null && groundCandidateIndex < 0 && queuedBlockCount > 0
                && !(mc.thePlayer.onGround && longTellySprintJump) && findLongTellyPlacement())
            aimLongTellyPlacement(previousYaw, previousPitch, lookBucket, cameraAligned);
        if (verifiedHit == null && groundCandidateIndex < 0 && !pendingPlacementReady && !placedThisTick && !isHypixelMode() && !isSnapPlusMode())
            easeActivationRotation(mc.thePlayer, previousYaw, previousPitch, lookBucket, cameraAligned);
        boolean snapPlacementTick = isSnapPlacementReady(groundCandidateIndex, verifiedHit, placement);
        if (isSnapRotationActive() && snapReturnPending) {
            if (!snapPlacementTick && isSafeSnapReturn(position, motion)) snapReturnPending = false;
            else if (snapPlacementTick) snapReturnPending = false;
        }
        applySnapModeRotations(cameraYaw, snapPlacementTick);
        float serverYaw = RotationUtils.serverRotations[0];
        targetYaw = serverYaw + MathHelper.wrapAngleTo180_float(targetYaw - serverYaw);
        targetYaw = quantizeMouseRotation(targetYaw, lastSentYaw);
        targetPitch = RotationUtils.clampPitch(quantizeMouseRotation(targetPitch, lastSentPitch));
        if (shortDiagonal && placement != null && placementAllowed && placementDelayTicks <= 0) {
            verifiedHit = raycastFace(placement, targetYaw, targetPitch);
            if (verifiedHit == null) verifiedHit = aimShortDiagonalFace(placement);
        }
        if (isSnapRotationActive() && snapPlacementTick) {
            Placement snapPlacement = null;
            if (groundCandidateIndex >= 0 && groundCandidateIndex < candidates.length) snapPlacement = candidates[groundCandidateIndex];
            else if (verifiedHit != null && placement != null) snapPlacement = placement;
            else if (pendingPlacementReady) snapPlacement = pendingPlacement;
            if (snapPlacement != null) {
                Vec3 snapHit = aimSnapFace(snapPlacement, cameraYaw);
                if (snapHit == null) { if (groundCandidateIndex >= 0) groundCandidateIndex = -1; if (placement == snapPlacement) verifiedHit = null; }
                else if (placement == snapPlacement) verifiedHit = snapHit;
            }
        }
        boolean towering = isTowerActive(); updateTowerSession(towering); rushPlannedPlacement = null;
        if (towering) {
            groundCandidateIndex = -1; verifiedHit = null; placement = null; pendingPlacementReady = false; pendingPlacement = null;
            Placement towerPlacement = findTowerPlacement(position);
            if (towerPlacement != null) {
                Vec3 towerHit = aimTowerFace(towerPlacement, cameraYaw);
                if (towerHit != null) { placement = towerPlacement; verifiedHit = towerHit; rushPlannedPlacement = towerPlacement; }
            } else {
                float currentServerYaw = RotationUtils.serverRotations[0];
                targetYaw = quantizeMouseRotation(unwrapYaw(cameraYaw, currentServerYaw), lastSentYaw);
                targetPitch = RotationUtils.clampPitch(quantizeMouseRotation(84F, lastSentPitch)); pendingYawJitter = 0;
            }
        } else if (isRushMode()) {
            int baseGroundCandidateIndex = groundCandidateIndex;
            Vec3 baseVerifiedHit = verifiedHit; Placement basePlacement = placement;
            boolean basePendingReady = pendingPlacementReady; Placement basePendingPlacement = pendingPlacement;
            float baseTargetYaw = targetYaw, baseTargetPitch = targetPitch, baseYawJitter = pendingYawJitter;
            Placement rushPlacement = planRushPlacement(position, cameraYaw);
            boolean baseReady = baseGroundCandidateIndex >= 0 || baseVerifiedHit != null || basePendingReady;
            if (rushPlacement != null) {
                Vec3 leasedHit = rotationSentLastTick ? raycastFace(rushPlacement, lastSentYaw, lastSentPitch) : null;
                boolean leased = leasedHit != null && isPlacementWithinReach(rushPlacement, 4.05D);
                if (leased) {
                    groundCandidateIndex = -1; verifiedHit = leasedHit; placement = rushPlacement;
                    pendingPlacementReady = false; pendingPlacement = null; targetYaw = lastSentYaw; targetPitch = lastSentPitch;
                    pendingYawJitter = 0; rushPlannedPlacement = rushPlacement; rushWarmTarget = null; rushWarmFace = null; rushWarmTicks = 0; rushMissStreak = 0;
                } else if (baseReady) {
                    groundCandidateIndex = baseGroundCandidateIndex; verifiedHit = baseVerifiedHit; placement = basePlacement;
                    pendingPlacementReady = basePendingReady; pendingPlacement = basePendingPlacement;
                    targetYaw = baseTargetYaw; targetPitch = baseTargetPitch; pendingYawJitter = baseYawJitter;
                    rushWarmTarget = null; rushWarmFace = null; rushWarmTicks = 0; rushMissStreak = 0;
                } else {
                    groundCandidateIndex = -1; verifiedHit = null; placement = null; pendingPlacementReady = false; pendingPlacement = null;
                    Vec3 preparedHit = aimRushPlacement(rushPlacement, cameraYaw);
                    if (preparedHit != null) {
                        BlockPos target = rushPlacement.getTarget();
                        boolean sameWarm = rushWarmTarget != null && rushWarmTarget.equals(target) && rushWarmFace == rushPlacement.face;
                        rushWarmTarget = target; rushWarmFace = rushPlacement.face; rushWarmTicks = sameWarm ? Math.min(3, rushWarmTicks + 1) : 1; rushMissStreak = 0;
                    } else {
                        groundCandidateIndex = baseGroundCandidateIndex; verifiedHit = baseVerifiedHit; placement = basePlacement;
                        pendingPlacementReady = basePendingReady; pendingPlacement = basePendingPlacement;
                        targetYaw = baseTargetYaw; targetPitch = baseTargetPitch; pendingYawJitter = baseYawJitter; rushMissStreak++;
                    }
                }
            } else {
                groundCandidateIndex = baseGroundCandidateIndex; verifiedHit = baseVerifiedHit; placement = basePlacement;
                pendingPlacementReady = basePendingReady; pendingPlacement = basePendingPlacement;
                targetYaw = baseTargetYaw; targetPitch = baseTargetPitch; pendingYawJitter = baseYawJitter; rushMissStreak++;
            }
            if (rushMissStreak >= 4) { rushLastPlacedTarget = null; rushWarmTarget = null; rushWarmFace = null; rushWarmTicks = 0; rushMissStreak = 0; }
        }
        sendRotations(); placeSelectedBlock(groundCandidateIndex, verifiedHit, placement);
        if (!towering && isRushMode()) runRushVerifiedChain(position, cameraYaw);
        if (neededBlock != null && !placedThisTick && mc.thePlayer.onGround && !tellyJumping) groundMissTicks++; else groundMissTicks = 0;
        if (!towering) updateKeepYPlacement(mc.thePlayer, position, motion);
        autoClick(mc.thePlayer);
    }

    // Source 980-1108 and 6657: input is processed AFTER this tick's rotations/placement.
    private MovementInput preInputMovement;
    void onPrePlayerInput(ClientInput input) {
        mc.sync();
        if (input == null || !Utils.nullCheck() || shouldPauseForGui()) return;
        var keys = input.keyPresses;
        MovementInput movement = new MovementInput((keys.forward() ? 1 : 0) - (keys.backward() ? 1 : 0),
                (keys.left() ? 1 : 0) - (keys.right() ? 1 : 0), keys.jump(), keys.shift());
        scaffoldPrePlayerInput(new PrePlayerInputEvent(movement));
        apply(input, movement, !sprintSuppressed && (keys.sprint() || mc.gameSettings.keyBindSprint.isDown()));
        preInputMovement = movement;
    }
    private void scaffoldPrePlayerInput(PrePlayerInputEvent e) {
        if (!Utils.nullCheck()) return;
        if (shouldPauseForGui()) { resetNinjaState(); return; }
        if (isHypixelMode()) applySourceSprintPolicy();
        else applySprintPolicy((isSnapPlusMode() || isHopMode()) ? false : shouldStopSprint());
        boolean airTap = isLongTellyEnabled() && tellyJumping && !mc.thePlayer.onGround;
        if (airTap && e.getStrafe() == 0 && strafeTapCooldown <= 0 && rotations.fixMovement()) {
            double cameraYawRadians = Math.toRadians(getPlayerYaw());
            double lateralMotion = mc.thePlayer.motionX * -Math.cos(cameraYawRadians) + mc.thePlayer.motionZ * -Math.sin(cameraYawRadians);
            if (Math.abs(lateralMotion) > 0.02) {
                e.setStrafe(lateralMotion > 0 ? 1F : -1F);
                strafeTapCooldown = 2 + (randomFloat(0, 1) < 0.4F ? 1 : 0);
            }
        }
        if (mc.thePlayer.onGround && isKeepYActive() && keepYStage > 0 && isMoving()) e.setJump(true);
        if (isTowerActive() && towerOwnsJump()) e.setJump(false);
        if (isRushMode() && rushAutoHop.isToggled() && !isTowerActive() && !isKeepYActive() && isMoving()) {
            applySprintPolicy(false); mc.thePlayer.setSprinting(true);
            if (mc.thePlayer.onGround && groundMissTicks == 0 && placementDelayTicks <= 0 && isValidBlock(getPlacementStack(mc.thePlayer))) e.setJump(true);
        }
        if (isHypixelMode() && !isTowerActive()) {
            String sourceStyle = getSourceScaffoldMode();
            boolean wantsAutoJump = sourceAutoJump.isToggled();
            if (sourceStyle.equals("Telly")) wantsAutoJump = true;
            if (wantsAutoJump && isMoving() && mc.thePlayer.onGround && isValidBlock(getPlacementStack(mc.thePlayer))) e.setJump(true);
            else if (sourceStyle.equals("God Bridge") && !wantsAutoJump && sourceGodBridgeJumpBlocks >= 8 && mc.thePlayer.onGround) {
                e.setJump(true); sourceGodBridgeJumpBlocks = 0;
            }
            if (sourceStyle.equals("God Bridge") && sourceSneak.isToggled()) {
                boolean shouldAutoSneak = sourceDirectionalChange <= 10;
                int every = Math.max(1, (int) Math.round(sourceSneakEvery.getInput()));
                if (sourceSneakBlocks >= every) { shouldAutoSneak = true; sourceSneakBlocks = 0; }
                if (shouldAutoSneak) e.setSneak(true);
            }
            if (sourceStyle.equals("Breezily")) updateSourceBreezilyMovement(e);
        }
        hopLaunchRequested = false;
        if (isHopMode() && !isTowerActive() && !isKeepYActive() && isMoving() && isValidBlock(getPlacementStack(mc.thePlayer))) {
            if (hopAutoSprint.isToggled()) { applySprintPolicy(false); mc.thePlayer.setSprinting(true); }
            if (mc.thePlayer.onGround && groundMissTicks == 0 && placementDelayTicks <= 0) { e.setJump(false); hopLaunchRequested = true; }
        }
        if (!mc.thePlayer.onGround) jumpHoldTicks = 0;
        else if (e.isJump() && isLongTellyEnabled() && longTellySprintJump && jumpHoldTicks < SPRINT_JUMP_HOLD_TICKS
                && Math.abs(MathHelper.wrapAngleTo180_float(lastSentYaw - getPlayerYaw())) > SPRINT_JUMP_ALIGNMENT) {
            e.setJump(false); jumpHoldTicks++;
        }
        if (isNinjaMode()) updateNinjaSneak(e); else resetNinjaState();
        if (isBreezilyMode()) {
            if (!isBreezilyGodBridgeJump()) updateBreezilyMovement(e); else breezilyTickCounter = 0;
        } else if (!(isHypixelMode() && getSourceScaffoldMode().equals("Breezily"))) resetBreezilyState();
    }
    void onPostPlayerInput(ClientInput input) {
        // With source movement-fix disabled the exact fractional weave survives native input rebuilding.
        if (preInputMovement != null && isBreezilyMode() && !isBreezilyGodBridgeJump() && !rotations.fixMovement())
            InputUtil.setMoveVector(input, new net.minecraft.world.phys.Vec2(preInputMovement.strafe, preInputMovement.forward));
        preInputMovement = null;
        mc.sync();
        if (!Utils.nullCheck() || shouldPauseForGui()) { predictionInputPlayer = null; return; }
        if ("KRS NCP".equals(selectedTowerMode)) { tower.reset(); legacyNcpTower.run(); }
        else tower.tickMotion(selectedTowerMode, isTowerActive());
        mc.sync(); // Native tower mutates the live player; refresh the detached API view.
        updateHopMotion();
        Float serverYaw = rotations.getServerYaw();
        predictionInputYaw = rotations.fixMovement() && serverYaw != null && !serverYaw.isNaN() && !serverYaw.isInfinite() ? serverYaw : mc.thePlayer.rotationYaw;
        predictionInputPlayer = mc.thePlayer;
    }

    // Source 1276-1286, 1334-1464, 1506-1679: inventory and silent-hotbar state machine.
    private boolean isSpoofItemEnabled() { return autoSwap.isToggled() && spoofItem.isToggled(); }
    int getTotalBlocks() {
        if (mc.thePlayer == null) return 0;
        int totalBlocks = 0;
        for (int i = 0; i < 36; i++) { ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i); if (isValidBlock(stack)) totalBlocks += stack.stackSize; }
        return totalBlocks;
    }
    private boolean shouldPauseForGui() { return disableInGui.isToggled() && mc.currentScreen != null; }
    private void pauseForGui() {
        resetNinjaState(); resetBreezilyState(); rotationSentThisTick = rotationSentLastTick = false;
        rotations.forceMovementFix = false; movementFixActive = false; Client.rotationManager.stopRotation();
        restoreAutoSwapSlot(); originalSlot = placementSlot = -1; hasSwapped = physicalSlotSwapped = false;
        if (sprintSuppressed) { mc.gameSettings.keyBindSprint.setDown(sprintWasPressed || physicalDown(mc.gameSettings.keyBindSprint)); sprintSuppressed = false; }
        resetMovementState(); snapReturnPending = snapSkipPlacementThisTick = false; snapDiagonalHoldTicks = 0;
        snapLastPlacementYaw = Float.NaN; breezilyViewYaw = breezilyViewPitch = Float.NaN;
    }
    private void setSpoofServerSlot(int slot) {
        if (mc.thePlayer == null || slot < 0 || slot > 8 || spoofServerSlot == slot) return;
        sendingSpoofSlotPacket = true;
        try { PacketUtil.sendPacket(new ServerboundSetCarriedItemPacket(slot)); spoofServerSlot = slot; }
        finally { sendingSpoofSlotPacket = false; }
    }
    private void restoreSpoofServerSlot() {
        if (mc.thePlayer == null) { spoofServerSlot = -1; sendingSpoofSlotPacket = false; return; }
        int visibleSlot = inventory.getSlot();
        if (visibleSlot < 0 || visibleSlot > 8) visibleSlot = mc.thePlayer.inventory.currentItem;
        if (spoofServerSlot >= 0 && visibleSlot >= 0 && visibleSlot <= 8 && spoofServerSlot != visibleSlot) {
            sendingSpoofSlotPacket = true;
            try { PacketUtil.sendPacket(new ServerboundSetCarriedItemPacket(visibleSlot)); }
            finally { sendingSpoofSlotPacket = false; }
        }
        spoofServerSlot = -1;
    }
    void updateSpoofedItem() {
        if (mc.thePlayer == null) { spoofServerSlot = -1; sendingSpoofSlotPacket = false; return; }
        if (isSpoofItemEnabled()) {
            if (physicalSlotSwapped && originalSlot >= 0 && originalSlot <= 8 && mc.thePlayer.inventory.currentItem != originalSlot) {
                inventory.setSlot(originalSlot); mc.thePlayer.inventory.currentItem = originalSlot;
            }
            physicalSlotSwapped = false;
            if (hasSwapped && placementSlot >= 0 && placementSlot <= 8 && placementSlot != mc.thePlayer.inventory.currentItem) setSpoofServerSlot(placementSlot);
            return;
        }
        if (spoofServerSlot >= 0) restoreSpoofServerSlot();
        if (hasSwapped && !physicalSlotSwapped && placementSlot >= 0 && placementSlot <= 8 && placementSlot != mc.thePlayer.inventory.currentItem) {
            originalSlot = mc.thePlayer.inventory.currentItem; inventory.setSlot(placementSlot);
            mc.thePlayer.inventory.currentItem = placementSlot; mc.thePlayer.inventory.sync(); physicalSlotSwapped = true;
        }
    }
    void onPrePlayerInteract() {
        mc.sync();
        if (!shouldPauseForGui()) updateSpoofedItem();
    }
    private void restoreAutoSwapSlot() {
        if (mc.thePlayer == null) { placementSlot = -1; hasSwapped = physicalSlotSwapped = false; spoofServerSlot = -1; sendingSpoofSlotPacket = false; return; }
        if (spoofServerSlot >= 0) restoreSpoofServerSlot();
        if (physicalSlotSwapped && originalSlot >= 0 && originalSlot <= 8 && originalSlot != mc.thePlayer.inventory.currentItem) {
            inventory.setSlot(originalSlot); mc.thePlayer.inventory.currentItem = originalSlot; mc.thePlayer.inventory.sync();
        }
        placementSlot = -1; hasSwapped = physicalSlotSwapped = false; originalSlot = mc.thePlayer.inventory.currentItem;
    }
    private ItemStack getPlacementStack(EntityPlayerSP player) {
        if (player == null) return null;
        if (autoSwap.isToggled() && hasSwapped && placementSlot >= 0 && placementSlot <= 8) {
            ItemStack stack = player.inventory.getStackInSlot(placementSlot); if (isValidBlock(stack)) return stack;
        }
        return player.getHeldItem();
    }
    private int getPlacementSlot(EntityPlayerSP player) {
        if (player == null) return -1;
        return autoSwap.isToggled() && hasSwapped && placementSlot >= 0 && placementSlot <= 8 ? placementSlot : player.inventory.currentItem;
    }
    private boolean isValidBlock(ItemStack stack) {
        if (stack == null || stack.stackSize <= 0 || !stack.isBlock || stack.name == null) return false;
        String name = stack.name.toLowerCase(java.util.Locale.ROOT), type = stack.type == null ? "" : stack.type;
        if (UNPLACEABLE_BLOCK_TYPES.contains("," + type + ",")) return false;
        return !UNPLACEABLE_BLOCK_NAMES.contains("," + name + ",");
    }
    private void swapSlot(int slot) {
        if (mc.thePlayer == null || slot < 0 || slot > 8 || !isValidBlock(mc.thePlayer.inventory.getStackInSlot(slot))) return;
        int visibleSlot = mc.thePlayer.inventory.currentItem;
        if (!hasSwapped || isSpoofItemEnabled()) originalSlot = visibleSlot;
        placementSlot = slot;
        if (slot == visibleSlot) {
            if (spoofServerSlot >= 0) restoreSpoofServerSlot();
            hasSwapped = false; physicalSlotSwapped = false; return;
        }
        hasSwapped = true;
        if (isSpoofItemEnabled()) { physicalSlotSwapped = false; setSpoofServerSlot(slot); return; }
        inventory.setSlot(slot); mc.thePlayer.inventory.currentItem = slot; mc.thePlayer.inventory.sync(); physicalSlotSwapped = true;
    }
    private boolean selectPlacementStack(EntityPlayerSP player) {
        if (player == null) return false;
        if (!autoSwap.isToggled()) {
            if (hasSwapped) restoreAutoSwapSlot();
            heldBlockCount = getBlockCount(player.getHeldItem()); return heldBlockCount > 0;
        }
        updateSpoofedItem();
        if (isSpoofItemEnabled() && !physicalSlotSwapped) originalSlot = player.inventory.currentItem;
        ItemStack activeStack = getPlacementStack(player); heldBlockCount = getBlockCount(activeStack);
        if (heldBlockCount <= 0) {
            int startSlot = getPlacementSlot(player); if (startSlot < 0) startSlot = player.inventory.currentItem;
            int slot = startSlot - 1;
            for (int i = slot; i > slot - 9; i--) {
                int hotbarSlot = ((i % 9) + 9) % 9;
                ItemStack candidateStack = player.inventory.getStackInSlot(hotbarSlot);
                if (isValidBlock(candidateStack)) { swapSlot(hotbarSlot); activeStack = getPlacementStack(player); heldBlockCount = getBlockCount(activeStack); break; }
            }
        }
        if (isIceSelectionEnabled() && isKeepYActive() && (tellyJumping || isTellyTakeoff() || keepYStage > 0 || isMoving())) {
            int preferredSlot = findPreferredIceSlot(player);
            if (preferredSlot >= 0 && preferredSlot != getPlacementSlot(player)) {
                swapSlot(preferredSlot); activeStack = getPlacementStack(player); heldBlockCount = getBlockCount(activeStack);
            }
        }
        return heldBlockCount > 0;
    }
    private int findPreferredIceSlot(EntityPlayerSP player) {
        int iceSlot = -1, normalSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i); if (!isValidBlock(stack)) continue;
            String blockName = stack.name == null ? "" : stack.name.toLowerCase(java.util.Locale.ROOT);
            if (blockName.equals("packed_ice") || blockName.equals("ice")) { if (iceSlot < 0) iceSlot = i; }
            else if (normalSlot < 0) normalSlot = i;
        }
        if (iceSlot < 0) return -1;
        boolean wantIce = player.motionY < iceThreshold.getInput() || player.onGround;
        return wantIce || normalSlot < 0 ? iceSlot : normalSlot;
    }
    private int getBlockCount(ItemStack stack) { return isValidBlock(stack) ? stack.stackSize : 0; }

    // Source 1681-1935.
    private void resetMovementState() {
        predictionInputPlayer = null; predictionInputYaw = Float.NaN; activationTicks = 0; groundPlacementSide = 1;
        movingCardinally = false; lookSide = 1; lookingStraightBack = false; jumpHoldTicks = 0; activationYawAccumulator = 0;
        rotationEaseRate = 1; rotationEaseStarted = false; groundMissTicks = 0; hasCameraYaw = false; rotationEaseSlowdown = 0;
        tellyJumping = takeoffLatched = sprintStopRequested = false; sprintToggleCooldown = lastYawBucket = 0; diagonalMovement = false;
        resetNinjaState(); resetBreezilyState(); longTellySprintJump = true; longTellyWasAirborne = false; longTellyNeedsSideUpdate = true;
        longTellyWasEngaged = false; previousVerticalMotion = 0; ticksSinceGrounded = 99; hasLongTellyOrigin = false;
        resetLongTellyQueue(); strafeTapCooldown = 0; pendingPlacementReady = false; verificationDelayTicks = 0;
        hypixelPlaceTimer = 0; hypixelSourceYaw = Float.NaN; hypixelSourcePitch = 90;
    }
    private void resetLongTellyQueue() {
        queuedBlockCount = 0; pendingQueueIndex = -1; skippedQueueMask = 0; Arrays.fill(queueRetryTicks, 0); queueCursor = queueStallTicks = 0;
    }
    private void autoClick(EntityPlayerSP player) {
        ItemStack placementStack = getPlacementStack(player);
        if (player == null || !isValidBlock(placementStack)) return;
        float configuredCps = (float) clickSpeed.getInput();
        if (configuredCps < 1F) { clickState = 'D'; return; }
        if (mc.currentScreen != null) { clickState = 'S'; return; }
        if (placementAttemptsThisTick > 0) { clickState = 'P'; return; }
        long now = client.time();
        if (now < nextClickAt) { clickState = 'w'; return; }
        if (nextClickAt < now - 500L) nextClickAt = now;
        clickState = 'C'; sendingAutoClick = true;
        try {
            // Modern equivalent of C08 with direction 255: sequenced use-item in air.
            // There is no 26.2 packet carrying the old arbitrary ItemStack/sentinel block.
            nativeMc.gameMode.startPrediction(nativeMc.level, sequence -> new ServerboundUseItemPacket(
                    InteractionHand.MAIN_HAND, sequence, rotations.serverRotations[0], rotations.serverRotations[1]));
        } catch (Throwable t) { clickState = 'E'; }
        finally { sendingAutoClick = false; }
        float cps = configuredCps * randomFloat(0.88F, 1.12F); if (cps < 1F) cps = 1F;
        float clickInterval = 1000F / cps;
        if (randomFloat(0, 1) < 0.10F) clickInterval *= randomFloat(1.5F, 2.4F);
        nextClickAt += (long) clickInterval; if (nextClickAt < now) nextClickAt = now + (long) clickInterval;
        clickCount++;
        if (now - clickWindowStart >= 1000L) {
            measuredClickCps = clickCount * 1000F / Math.max(1L, now - clickWindowStart); clickCount = 0; clickWindowStart = now;
        }
    }
    private float updateCameraYaw(float rawCameraYaw) {
        if (!hasCameraYaw) { continuousCameraYaw = rawCameraYaw; hasCameraYaw = true; }
        else continuousCameraYaw += MathHelper.wrapAngleTo180_float(rawCameraYaw - continuousCameraYaw);
        return continuousCameraYaw;
    }
    private void beginTick() {
        placedThisTick = false; placementAttemptsThisTick = 0; snapSkipPlacementThisTick = false;
        if (snapDiagonalHoldTicks > 0) snapDiagonalHoldTicks--;
        hasPrediction = false; updateYawJitter();
        rotationEaseRate = MathHelper.clamp_float(rotationEaseRate + randomFloat(-0.01F, 0.01F), 0.97F, 1.03F);
        if (activationTicks < 1000) activationTicks++;
        debugAimSelection = 0; debugAimScore = -1; pendingPlacementReady = false; skippedQueueMask = 0;
        for (int q = 0; q < queuedBlockCount; q++) if (queueRetryTicks[q] > 0 && --queueRetryTicks[q] > 0) skippedQueueMask |= 1 << q;
        rotationSentLastTick = rotationSentThisTick; rotationSentThisTick = false;
        if (placementDelayTicks > 0) placementDelayTicks--;
    }
    private void updateKeepYState(EntityPlayerSP player, Vec3 position) {
        if (!isKeepYActive()) { keepYStage = 0; useCurrentHeight = false; keepYStartHeight = MathHelper.floor_double(position.y); }
        if (player.onGround) {
            if (keepYStage != 0) keepYStage += keepYStage > 0 ? -1 : 1;
            if (keepYStage == 0 && isKeepYActive() && isMoving() && (!disableOnJumpPotion.isToggled() || !hasJumpPotion()) && !isJumpPressed()) keepYStage = 1;
            keepYStartHeight = useCurrentHeight ? keepYStartHeight : MathHelper.floor_double(position.y);
            if (getActiveKeepYMode().equals("Telly A")) useCurrentHeight = false;
            else if (!getActiveKeepYMode().equals("Telly B")) useCurrentHeight = false;
            tellyJumping = false;
        }
    }
    private boolean updateLongTellyState(EntityPlayerSP player, Vec3 position) {
        if (tellyOnJump.isToggled() && isTellyModeSelected()) {
            if (!isJumpPressed() && !(isKeepYActive() && keepYStage > 0)) { tellyJumping = false; takeoffLatched = false; }
            else if (isJumpPressed() && player.onGround && isMoving()) takeoffLatched = false;
        }
        if (isLongTellyEnabled() && !player.onGround && (longTellyWasEngaged || tellyJumping)) tellyJumping = true;
        boolean longTellyEngaged = isLongTellyEnabled() && (tellyJumping || isManualTellyTakeoff());
        if (longTellyEngaged && !player.onGround) longTellyMissTicks++; else longTellyMissTicks = 0;
        if (verificationDelayTicks > 0 && --verificationDelayTicks == 0 && isReplaceableAt(verificationBlock)) {
            revertedPlacementCount++; skippedQueueMask = 0; Arrays.fill(queueRetryTicks, 0); queueStallTicks = 0;
        }
        if (player.onGround) { lastGroundX = MathHelper.floor_double(position.x); lastGroundZ = MathHelper.floor_double(position.z); ticksSinceGrounded = 0; }
        else if (ticksSinceGrounded < 99) ticksSinceGrounded++;
        if (longTellyEngaged && !longTellyWasEngaged) {
            longTellySprintJump = true; longTellyWasAirborne = false; longTellyNeedsSideUpdate = true; hasLongTellyOrigin = false; queuedBlockCount = 0; pendingQueueIndex = -1;
        }
        longTellyWasEngaged = longTellyEngaged;
        if (strafeTapCooldown > 0) strafeTapCooldown--;
        Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
        if (longTellyEngaged) {
            if (player.onGround && longTellyWasAirborne) {
                longTellyWasAirborne = false; longTellySprintJump = !longTellySprintJump; longTellyNeedsSideUpdate = longTellySprintJump;
                buildLongTellyQueue(motion); if (!longTellySprintJump) placementDelayTicks = 0;
            }
            if (!player.onGround) longTellyWasAirborne = true;
            if (motion.y > 0.3 && previousVerticalMotion <= 0.05) buildLongTellyQueue(motion);
        } else { queuedBlockCount = 0; pendingQueueIndex = -1; }
        previousVerticalMotion = motion.y; return longTellyEngaged;
    }

    // Source 1937-2450: face sampling, quantization, ground candidates and long-Telly aim.
    private void aimAtPlacement(Placement placement, float referenceServerYaw, EntityPlayerSP player, Vec3 position) {
        if (placement == null) return;
        BlockPos blockPosition = placement.support;
        EnumFacing face = placement.face;
        double[] xOffsets = getFaceOffsets(face.getFrontOffsetX()), yOffsets = getFaceOffsets(face.getFrontOffsetY()), zOffsets = getFaceOffsets(face.getFrontOffsetZ());
        float[] bestRotation = null;
        float bestRotationDifference = 0;
        float baseYaw = unwrapYaw(yaw, referenceServerYaw);
        float referenceYaw = rotationSentLastTick ? lastSentYaw : baseYaw, referencePitch = rotationSentLastTick ? lastSentPitch : pitch;
        for (double dx : xOffsets) for (double dy : yOffsets) for (double dz : zOffsets) {
            double relX = blockPosition.getX() + dx - position.x;
            double relY = blockPosition.getY() + dy - position.y - player.getEyeHeight();
            double relZ = blockPosition.getZ() + dz - position.z;
            float[] rotation = getRotationsToOffset(relX, relY, relZ, baseYaw, pitch);
            float rotationDifference = Math.abs(MathHelper.wrapAngleTo180_float(rotation[0] - referenceYaw)) + Math.abs(rotation[1] - referencePitch);
            if (bestRotation != null && rotationDifference >= bestRotationDifference) continue;
            Vec3 hit = raycastFace(placement, rotation[0], rotation[1]);
            if (hit != null) { bestRotation = rotation; bestRotationDifference = rotationDifference; }
        }
        if (bestRotation != null) { yaw = bestRotation[0]; pitch = bestRotation[1]; canRotate = true; }
    }
    private double[] getFaceOffsets(int direction) { return direction < 0 ? FACE_MIN_OFFSET : direction > 0 ? FACE_MAX_OFFSET : FACE_SAMPLE_OFFSETS; }
    private void aimYaw(float value) { targetYaw = value; pendingYawJitter = 0; }
    private void aimYawWithJitter(float base, float yawJitter) { targetYaw = base + yawJitter; pendingYawJitter = yawJitter; }
    private void aimPitch(float value) { targetPitch = value; }
    private void updateTargetRotations(Vec3 position, Vec3 motion, float previousYaw, float previousPitch, boolean urgent, boolean shortDiagonal) {
        if (!urgent) { float[] eased = easeRotations(previousYaw, previousPitch, yaw, pitch); aimYaw(eased[0]); aimPitch(eased[1]); }
        else { aimYaw(yaw); aimPitch(pitch); rotationEaseStarted = false; rotationEaseSlowdown = 0; }
        if (tellyJumping && (motion.y > 0 || position.y > keepYStartHeight + 1)) {
            float yawDiff = MathHelper.wrapAngleTo180_float(yaw - previousYaw);
            float rotationLimit = placementDelayTicks >= 2 ? randomFloat(TAKEOFF_YAW_MIN, TAKEOFF_YAW_MAX) : randomFloat(AIRBORNE_YAW_MIN, AIRBORNE_YAW_MAX);
            if (Math.abs(yawDiff) > rotationLimit) {
                aimYaw(previousYaw + clampRotationDelta(yawDiff, rotationLimit));
                if (!shortDiagonal && !(isLongTellyEnabled() && queuedBlockCount > 0)) placementDelayTicks = Math.max(placementDelayTicks, 1);
            }
        }
    }
    private int updateLookBucket(Vec3 position, float cameraYaw) {
        int backBucket = Math.round(MathHelper.wrapAngleTo180_float(cameraYaw + 180F - cameraYaw) / 45F);
        int currentBucket = Math.round(MathHelper.wrapAngleTo180_float(lastSentYaw - cameraYaw) / 45F);
        while (backBucket - currentBucket > 4) backBucket -= 8;
        while (backBucket - currentBucket < -4) backBucket += 8;
        double blockCenterX = Math.floor(position.x) + 0.5, blockCenterZ = Math.floor(position.z) + 0.5;
        double lateralOffset = (position.x - blockCenterX) * -longTellyForwardZ + (position.z - blockCenterZ) * longTellyForwardX;
        if (lateralOffset > 0.12) lookSide = 1; else if (lateralOffset < -0.12) lookSide = -1;
        if (currentBucket == backBucket) lookingStraightBack = true;
        return lookingStraightBack ? backBucket : backBucket + LOOK_DIAGONAL_BUCKETS * lookSide;
    }
    private boolean alignTakeoffRotations(boolean tellyRotating, float cameraYaw) {
        boolean cameraAligned = false;
        if (tellyRotating && isTellyTakeoff() && (!isLongTellyEnabled() || longTellySprintJump)) {
            cameraAligned = true; lookingStraightBack = false; aimYawWithJitter(cameraYaw, jitter(0.12F));
            if (takeoffPitch < 30F || takeoffPitch > 89.5F) takeoffPitch = RotationUtils.clampPitch(lastSentPitch);
            takeoffPitch += randomFloat(-0.5F, 0.5F); takeoffPitch = MathHelper.clamp_float(takeoffPitch, 30F, 89F);
            takeoffPitch += (RotationUtils.clampPitch(lastSentPitch) - takeoffPitch) * 0.25F; aimPitch(takeoffPitch);
            if (isManualTellyTakeoff()) { placementDelayTicks = 2; takeoffLatched = true; }
            else if (!takeoffLatched) { placementDelayTicks = Math.max(placementDelayTicks, 2); takeoffLatched = true; }
            tellyJumping = true;
        }
        if (!cameraAligned) takeoffLatched = false;
        return cameraAligned;
    }
    private void snapGroundYaw(boolean tellyRotating, Vec3 position, Vec3 motion, float cameraYaw) {
        if (!tellyRotating) {
            float snapBase = targetYaw;
            double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            if (horizontalSpeed >= 0.02) {
                float travelYaw = (float) Math.toDegrees(Math.atan2(-motion.x, motion.z));
                float cardinalOffset = Math.abs(MathHelper.wrapAngleTo180_float(travelYaw - Math.round(travelYaw / 90F) * 90F));
                if (movingCardinally) { if (cardinalOffset > 27F) movingCardinally = false; }
                else if (cardinalOffset < 18F) movingCardinally = true;
            } else movingCardinally = false;
            if (horizontalSpeed >= 0.02 && movingCardinally) {
                double blockCenterX = Math.floor(position.x) + 0.5, blockCenterZ = Math.floor(position.z) + 0.5;
                double lateralOffset = ((position.x - blockCenterX) * -motion.z + (position.z - blockCenterZ) * motion.x) / horizontalSpeed;
                if (lateralOffset > 0.12) groundPlacementSide = 1; else if (lateralOffset < -0.12) groundPlacementSide = -1;
                snapBase = (float) Math.toDegrees(Math.atan2(motion.x, -motion.z)) + GROUND_DIAGONAL_BUCKETS * groundPlacementSide * 45F;
            } else if (horizontalSpeed < 0.02) snapBase = cameraYaw + lastYawBucket * 45F;
            float holdAngle = GROUND_YAW_HOLD_ANGLE;
            if (!movingCardinally) holdAngle = 29.5F; else if (horizontalSpeed >= 0.02 && groundMissTicks >= 2) holdAngle = 22.5F;
            float snapDelta = MathHelper.wrapAngleTo180_float(snapBase - cameraYaw);
            int bucket = Math.round(snapDelta / 45F);
            if (bucket != lastYawBucket && Math.abs(MathHelper.wrapAngleTo180_float(snapDelta - lastYawBucket * 45F)) <= holdAngle) bucket = lastYawBucket;
            lastYawBucket = bucket; aimYaw(cameraYaw + bucket * 45F);
        } else lastYawBucket = Math.round(MathHelper.wrapAngleTo180_float(targetYaw - cameraYaw) / 45F);
    }
    private int aimGroundPlacement(EntityPlayerSP player, Vec3 position, boolean urgent, float previousPitch) {
        float cameraYaw = yawGridBase;
        int groundY = MathHelper.floor_double(position.y) - 1;
        findNeededBlock(groundY);
        int candidateCount = buildCandidates(groundY); debugCandidateCount = candidateCount;
        if (neededBlock != null) for (int corner = 0; corner < 4 && !urgent; corner++) {
            double cornerX = position.x + ((corner & 1) == 0 ? -0.3 : 0.3), cornerZ = position.z + ((corner & 2) == 0 ? -0.3 : 0.3);
            if (MathHelper.floor_double(cornerX) == neededBlock.getX() && MathHelper.floor_double(cornerZ) == neededBlock.getZ()) urgent = true;
        }
        if (candidateCount == 0) return -1;
        float bestPitch = Float.NaN, bestScanPitch = 0;
        int bestScore = -1; debugAimSelection = 0; debugAimScore = -1;
        boolean holdingPitch = false;
        if (rotationSentLastTick) {
            int previousCandidateIndex = matchCandidate(candidateCount, lastBaseYaw, lastBasePitch);
            if (previousCandidateIndex >= 0 && getCandidateScore(previousCandidateIndex) >= 2
                    && matchCandidate(candidateCount, lastBaseYaw + 0.5F, lastBasePitch) >= 0
                    && matchCandidate(candidateCount, lastBaseYaw - 0.5F, lastBasePitch) >= 0
                    && matchCandidate(candidateCount, lastBaseYaw, RotationUtils.clampPitch(lastBasePitch - 0.35F)) >= 0
                    && matchCandidate(candidateCount, lastBaseYaw, RotationUtils.clampPitch(lastBasePitch + 0.35F)) >= 0) {
                aimYawWithJitter(lastBaseYaw, nextYawJitter()); holdingPitch = true; bestPitch = lastBasePitch;
                bestScore = 2; bestScanPitch = 0; debugAimSelection = 6; debugAimScore = 2;
            }
        }
        int heldCandidateIndex = holdingPitch ? -1 : matchCandidate(candidateCount, targetYaw, previousPitch);
        if (heldCandidateIndex >= 0 && getCandidateScore(heldCandidateIndex) >= 2
                && matchCandidate(candidateCount, targetYaw, RotationUtils.clampPitch(previousPitch - 0.35F)) >= 0
                && matchCandidate(candidateCount, targetYaw, RotationUtils.clampPitch(previousPitch + 0.35F)) >= 0) {
            holdingPitch = true; bestPitch = previousPitch; bestScore = 2; bestScanPitch = 0; debugAimSelection = 5; debugAimScore = 2;
        }
        if (neededBlock != null && !holdingPitch) {
            int[] yawBuckets = getGroundYawBuckets(player, position, cameraYaw);
            analytic:
            for (int pass = 2; pass >= 1; pass--) for (int bucketIndex = 0; bucketIndex < yawBuckets.length; bucketIndex++) {
                float yawJitter = bucketIndex == 0 ? pendingYawJitter : nextYawJitter();
                float candidateYaw = bucketIndex == 0 ? targetYaw : cameraYaw + yawBuckets[bucketIndex] * 45F + yawJitter;
                for (int i = 0; i < candidateCount; i++) {
                    if (getCandidateScore(i) != pass) continue;
                    float candidatePitch = findFacePitch(candidates[i], candidateYaw);
                    if (!Float.isNaN(candidatePitch)) {
                        bestPitch = candidatePitch; bestScore = pass;
                        if (bucketIndex > 0) { aimYawWithJitter(candidateYaw - yawJitter, yawJitter); lastYawBucket = yawBuckets[bucketIndex]; }
                        debugAimSelection = 1; debugAimScore = pass; break analytic;
                    }
                }
            }
        }
        int maxScore = neededBlock != null ? 2 : 0;
        float scanPitch = 60F;
        while (Float.isNaN(bestPitch) || bestScore < maxScore) {
            float candidatePitch = Math.min(scanPitch, 90F);
            int matchedIndex = matchCandidate(candidateCount, targetYaw, quantizeRotation(candidatePitch));
            if (matchedIndex >= 0) {
                int score = getCandidateScore(matchedIndex);
                if (score > bestScore || score == bestScore && candidatePitch < bestScanPitch) {
                    bestPitch = candidatePitch; bestScanPitch = candidatePitch; bestScore = score; debugAimSelection = 2; debugAimScore = score;
                }
            }
            if (scanPitch >= 90F) break;
            float pitchStep = scanPitch >= 84F ? 0.3F + randomFloat(0, 0.08F) : 1F + randomFloat(-0.38F, 0.38F);
            scanPitch += pitchStep;
        }
        if (holdingPitch) { pitch = previousPitch; aimPitch(RotationUtils.clampPitch(previousPitch)); }
        else if (!Float.isNaN(bestPitch)) {
            pitch = quantizeRotation(bestPitch);
            if (!urgent) aimPitch(easeRotations(targetYaw, previousPitch, targetYaw, pitch)[1]); else aimPitch(pitch);
            aimPitch(RotationUtils.clampPitch(targetPitch));
        }
        int groundCandidateIndex = matchCandidate(candidateCount, targetYaw, targetPitch);
        if (groundCandidateIndex < 0 && !Float.isNaN(bestPitch)) { aimPitch(RotationUtils.clampPitch(bestPitch)); groundCandidateIndex = matchCandidate(candidateCount, targetYaw, targetPitch); }
        return groundCandidateIndex;
    }
    private int[] getGroundYawBuckets(EntityPlayerSP player, Vec3 position, float cameraYaw) {
        int[] allBuckets = {lastYawBucket, lastYawBucket - 1, lastYawBucket + 1, lastYawBucket - 2, lastYawBucket + 2, lastYawBucket - 3, lastYawBucket + 3, lastYawBucket + 4};
        Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
        double horizontalSpeed = Utils.getHorizontalSpeed(player);
        int preferredBucket = lastYawBucket;
        if (horizontalSpeed >= 0.02) {
            double blockCenterX = Math.floor(position.x) + 0.5, blockCenterZ = Math.floor(position.z) + 0.5;
            double lateralOffset = ((position.x - blockCenterX) * -motion.z + (position.z - blockCenterZ) * motion.x) / horizontalSpeed;
            if (lateralOffset > 0.12) groundPlacementSide = 1; else if (lateralOffset < -0.12) groundPlacementSide = -1;
            float backwardYaw = (float) Math.toDegrees(Math.atan2(motion.x, -motion.z));
            float preferredYaw = backwardYaw + GROUND_DIAGONAL_BUCKETS * groundPlacementSide * 45F;
            preferredBucket = Math.round(MathHelper.wrapAngleTo180_float(preferredYaw - cameraYaw) / 45F);
        }
        int searchRadius = GROUND_BUCKET_SEARCH_RADIUS;
        if (!movingCardinally || groundMissTicks >= 4) searchRadius = 9; else if (groundMissTicks >= 2) searchRadius = 2;
        int bucketCount = 1;
        for (int index = 1; index < allBuckets.length; index++) if (Math.abs(allBuckets[index] - lastYawBucket) <= searchRadius) allBuckets[bucketCount++] = allBuckets[index];
        int[] yawBuckets = Arrays.copyOf(allBuckets, bucketCount);
        for (int first = 0; first < yawBuckets.length; first++) for (int second = first + 1; second < yawBuckets.length; second++) {
            if (Math.abs(yawBuckets[second] - preferredBucket) < Math.abs(yawBuckets[first] - preferredBucket)) {
                int previousBucket = yawBuckets[first]; yawBuckets[first] = yawBuckets[second]; yawBuckets[second] = previousBucket;
            }
        }
        return yawBuckets;
    }
    private boolean isPlacementAllowed(Placement placement) {
        BlockPos target = placement == null ? null : placement.getTarget();
        int placementRow = target == null ? 0 : target.getY();
        if (queuedBlockCount > 0 && placementRow > longTellyRowY + (longTellySprintJump ? 0 : 1)) return false;
        if (target != null && isAbovePredictedPath(target.getX(), target.getY(), target.getZ())) { debugPlacementStatus = 'h'; return false; }
        return true;
    }
    private Vec3 verifyPlacementAim(Placement placement, boolean tellyRotating, boolean shortDiagonal, int lookBucket, boolean cameraAligned) {
        float cameraYaw = yawGridBase; debugPlacementStatus = 'y';
        if (rotationSentLastTick && !tellyRotating && hasStableFaceAim(placement)) { aimYawWithJitter(lastBaseYaw, nextYawJitter()); aimPitch(lastBasePitch); }
        Vec3 verifiedHit = raycastFace(placement, targetYaw, targetPitch);
        if (verifiedHit == null && tellyRotating) {
            float fallbackPitch = RotationUtils.clampPitch(Math.max(30F, pitch));
            Vec3 fallbackHit = raycastFace(placement, targetYaw, fallbackPitch);
            if (fallbackHit != null) { aimPitch(fallbackPitch); verifiedHit = fallbackHit; }
        }
        if (verifiedHit == null && (!tellyRotating || shortDiagonal && placementDelayTicks <= 0)) {
            int baseBucket = Math.round(MathHelper.wrapAngleTo180_float(yaw - cameraYaw) / 45F);
            int[] yawBuckets = {baseBucket, baseBucket - 1, baseBucket + 1, baseBucket - 2, baseBucket + 2, baseBucket - 3, baseBucket + 3, baseBucket + 4};
            if (isLookLocked() && isLongTellyEnabled() && queuedBlockCount > 0 && !cameraAligned && baseBucket != lookBucket)
                yawBuckets = new int[]{lookBucket, baseBucket, baseBucket - 1, baseBucket + 1, baseBucket - 2, baseBucket + 2, baseBucket - 3, baseBucket + 3, baseBucket + 4};
            for (int i = 0; i < yawBuckets.length && verifiedHit == null; i++) {
                float yawJitter = jitter(0.2F), candidateYaw = cameraYaw + yawBuckets[i] * 45F + yawJitter;
                float candidatePitch = findFacePitch(placement, candidateYaw); if (Float.isNaN(candidatePitch)) continue;
                Vec3 candidateHit = raycastFace(placement, candidateYaw, candidatePitch);
                if (candidateHit != null) {
                    aimYawWithJitter(candidateYaw - yawJitter, yawJitter); aimPitch(candidatePitch); verifiedHit = candidateHit; lastYawBucket = yawBuckets[i]; debugAimSelection = 3; debugAimScore = 2;
                }
            }
        }
        return verifiedHit;
    }
    private void aimLongTellyPlacement(float previousYaw, float previousPitch, int lookBucket, boolean cameraAligned) {
        float cameraYaw = yawGridBase;
        float rotationLimit = longTellySprintJump ? randomFloat(AIRBORNE_YAW_MIN, AIRBORNE_YAW_MAX) : randomFloat(TAKEOFF_YAW_MIN, TAKEOFF_YAW_MAX);
        float yawDelta = MathHelper.wrapAngleTo180_float(longTellyAimYaw - previousYaw), pitchDelta = longTellyAimPitch - previousPitch;
        float easeFraction = 1F - (float) Math.pow(0.05, 1.0 / LONG_TELLY_SMOOTH_TICKS);
        easeFraction = Math.min(1F, easeFraction * rotationEaseScale(Math.max(Math.abs(yawDelta), Math.abs(pitchDelta))));
        aimYawWithJitter(previousYaw + clampRotationDelta(yawDelta * easeFraction, rotationLimit), jitter(0.1F));
        aimPitch(RotationUtils.clampPitch(previousPitch + clampRotationDelta(pitchDelta * easeFraction, rotationLimit)));
        lastYawBucket = Math.round(MathHelper.wrapAngleTo180_float(targetYaw - cameraYaw) / 45F); debugAimSelection = 4; debugAimScore = 2;
        Vec3 verifiedHit = null;
        if (rotationSentLastTick && !cameraAligned && hasStableFaceAim(pendingPlacement)) {
            aimYawWithJitter(lastBaseYaw, jitter(0.1F)); aimPitch(RotationUtils.clampPitch(lastBasePitch));
            lastYawBucket = Math.round(MathHelper.wrapAngleTo180_float(targetYaw - cameraYaw) / 45F);
            verifiedHit = raycastFace(pendingPlacement, targetYaw, targetPitch);
        }
        float nearestBucket = Math.round(MathHelper.wrapAngleTo180_float(targetYaw - cameraYaw) / 45F);
        float[] yawBuckets = isLookLocked() && isLongTellyEnabled() && queuedBlockCount > 0 && !cameraAligned && nearestBucket != lookBucket
                ? new float[]{lookBucket, nearestBucket} : new float[]{nearestBucket};
        for (int bucketIndex = 0; bucketIndex < yawBuckets.length && verifiedHit == null; bucketIndex++) {
            float bucket = yawBuckets[bucketIndex], bucketTolerance = bucket == lookBucket ? Math.max(rotationLimit, 46F) : rotationLimit;
            float yawJitter = jitter(0.1F), candidateYaw = cameraYaw + bucket * 45F + yawJitter;
            if (Math.abs(MathHelper.wrapAngleTo180_float(candidateYaw - targetYaw)) <= bucketTolerance) {
                float facePitch = findNearestFacePitch(pendingPlacement, candidateYaw, previousPitch);
                if (!Float.isNaN(facePitch)) {
                    float candidatePitch = RotationUtils.clampPitch(facePitch);
                    Vec3 candidateHit = raycastFace(pendingPlacement, candidateYaw, targetPitch);
                    float selectedPitch = targetPitch;
                    if (candidateHit == null) { selectedPitch = candidatePitch; candidateHit = raycastFace(pendingPlacement, candidateYaw, candidatePitch); }
                    if (candidateHit != null) {
                        aimYawWithJitter(candidateYaw - yawJitter, yawJitter); aimPitch(selectedPitch); lastYawBucket = Math.round(bucket); verifiedHit = candidateHit;
                    }
                }
            }
        }
        if (verifiedHit == null) verifiedHit = raycastFace(pendingPlacement, targetYaw, targetPitch);
        if (verifiedHit == null && Math.abs(yawDelta) <= rotationLimit && Math.abs(pitchDelta) <= rotationLimit) {
            float adjustedPitch = findNearestFacePitch(pendingPlacement, targetYaw, targetPitch);
            if (!Float.isNaN(adjustedPitch)) { aimPitch(RotationUtils.clampPitch(adjustedPitch)); verifiedHit = raycastFace(pendingPlacement, targetYaw, targetPitch); }
        }
        if (verifiedHit != null) { pendingPlacementReady = true; queueStallTicks = 0; debugLongTellyStatus = 'P'; }
        else {
            debugLongTellyStatus = 'R';
            if (pendingQueueIndex >= 0 && ++queueStallTicks >= 3) {
                queueRetryTicks[pendingQueueIndex] = 2; skippedQueueMask |= 1 << pendingQueueIndex; queueStallTicks = 0; pendingQueueIndex = -1;
            }
        }
    }

    // Source 2451-2878: all Hypixel substyles, sprint policies and Hop variants.
    private boolean isHypixelSourceCanPlace(Vec3 position) {
        return position != null && isReplaceableAt(MathHelper.floor_double(position.x), MathHelper.floor_double(position.y) - 1, MathHelper.floor_double(position.z));
    }
    private boolean isHypixelSourceDiagonal() {
        float delta = getPlayerYaw() % 90F; if (delta < 0) delta += 90F;
        return delta > 20F && delta < 70F || Utils.isBindDown(mc.gameSettings.keyBindLeft) || Utils.isBindDown(mc.gameSettings.keyBindRight);
    }
    private void updateHypixelSourceIdleRotation(float movementYaw) {
        if (Float.isNaN(hypixelSourceYaw)) { hypixelSourceYaw = getPlayerYaw() - 180F; hypixelSourcePitch = 90F; }
        float offsetYaw = movementYaw + 50F, yawDelta = Math.abs(MathHelper.wrapAngleTo180_float(offsetYaw - hypixelSourceYaw));
        int sourceTick = yawDelta > 60F ? 8 : 9;
        if (hypixelPlaceTimer == sourceTick && !isJumpPressed() && mc.thePlayer.onGround) {
            float fromYaw = rotationSentLastTick ? lastSentYaw : getServerYaw(), desiredYaw = unwrapYaw(offsetYaw, fromYaw);
            float smoothedYaw = fromYaw + clampRotationDelta(MathHelper.wrapAngleTo180_float(desiredYaw - fromYaw), getYuriRotationStep());
            hypixelSourceYaw = quantizeMouseRotation(smoothedYaw, fromYaw);
        }
        targetYaw = hypixelSourceYaw; targetPitch = RotationUtils.clampPitch(hypixelSourcePitch); pendingYawJitter = 0;
    }
    private float getYuriRotationStep() { return isHypixelSourceDiagonal() || isJumpPressed() ? 117F : 86.4F; }
    private float[] findYuriWatchdogTarget(Placement placement, float movementYaw) {
        if (placement == null) return null;
        float yawBase = movementYaw + 90F, pitchBase = 90F;
        for (int yawIndex = 0; yawIndex < HYPIXEL_YAW_OFFSETS.length; yawIndex++) {
            float testYaw = MathHelper.wrapAngleTo180_float(yawBase + HYPIXEL_YAW_OFFSETS[yawIndex]);
            for (int pitchIndex = 0; pitchIndex < HYPIXEL_PITCH_OFFSETS.length; pitchIndex++) {
                float testPitch = MathHelper.clamp_float(pitchBase + HYPIXEL_PITCH_OFFSETS[pitchIndex], -90F, 90F);
                if (raycastFace(placement, testYaw, testPitch) != null) return new float[]{testYaw, testPitch};
            }
        }
        return null;
    }
    private Vec3 getHypixelSourceHitVec(Placement placement, Vec3 rayHit) {
        if (placement == null || rayHit == null) return rayHit;
        BlockPos support = placement.support; EnumFacing face = placement.face;
        if (support == null || face == null) return rayHit;
        double jitter = 0.03D, x = rayHit.x, y = rayHit.y, z = rayHit.z;
        if (face != EnumFacing.EAST && face != EnumFacing.WEST) x = Math.max(support.getX() + 0.10D, Math.min(support.getX() + 0.90D, x + (Math.random() - 0.5D) * jitter * 2));
        if (face != EnumFacing.UP && face != EnumFacing.DOWN) y = Math.max(support.getY() + 0.10D, Math.min(support.getY() + 0.90D, y + (Math.random() - 0.5D) * jitter * 2));
        if (face != EnumFacing.NORTH && face != EnumFacing.SOUTH) z = Math.max(support.getZ() + 0.10D, Math.min(support.getZ() + 0.90D, z + (Math.random() - 0.5D) * jitter * 2));
        return new Vec3(x, y, z);
    }
    private Vec3 aimHypixelPlacement(Placement placement, float movementYaw) {
        if (placement == null) return null;
        float fromYaw = rotationSentLastTick ? lastSentYaw : RotationUtils.serverRotations[0];
        float fromPitch = rotationSentLastTick ? lastSentPitch : mc.thePlayer.rotationPitch;
        float[] watchdogTarget = findYuriWatchdogTarget(placement, movementYaw);
        if (watchdogTarget == null) return aimSourceNormalPlacement(placement, movementYaw, getPlayerYaw());
        float desiredYaw = unwrapYaw(watchdogTarget[0], fromYaw), desiredPitch = watchdogTarget[1], maxChange = getYuriRotationStep();
        float smoothedYaw = fromYaw + clampRotationDelta(MathHelper.wrapAngleTo180_float(desiredYaw - fromYaw), maxChange);
        float smoothedPitch = fromPitch + clampRotationDelta(desiredPitch - fromPitch, maxChange);
        smoothedYaw = quantizeMouseRotation(smoothedYaw, fromYaw);
        smoothedPitch = RotationUtils.clampPitch(quantizeMouseRotation(smoothedPitch, fromPitch));
        targetYaw = smoothedYaw; targetPitch = smoothedPitch; hypixelSourceYaw = smoothedYaw; hypixelSourcePitch = smoothedPitch; pendingYawJitter = 0;
        return raycastFace(placement, smoothedYaw, smoothedPitch);
    }
    private boolean shouldSourceScaffoldPlace(Vec3 position, String style) {
        if (position == null || style == null || !isHypixelSourceCanPlace(position)) return false;
        if (!style.equals("Telly")) return true;
        if (sourceTellyWatchdog.isToggled()) return sourceAirTicks > 0;
        int threshold;
        if (isJumpPressed()) threshold = Math.max(0, (int) Math.round(sourceTellyJumpDownTicks.getInput()));
        else if (isHypixelSourceDiagonal()) threshold = Math.max(0, (int) Math.round(sourceTellyDiagonalTicks.getInput()));
        else threshold = Math.max(0, (int) Math.round(sourceTellyStraightTicks.getInput()));
        return sourceAirTicks >= threshold;
    }
    private Vec3 aimSourceScaffoldPlacement(Placement placement, String style, float movementYaw, float cameraYaw) {
        if (placement == null || style == null) return null;
        if (style.equals("Hypixel")) return aimHypixelPlacement(placement, movementYaw);
        if (style.equals("Telly")) {
            if (sourceTellyWatchdog.isToggled()) return aimHypixelPlacement(placement, movementYaw);
            return aimSourceNormalPlacement(placement, movementYaw, cameraYaw);
        }
        if (style.equals("Breezily")) {
            Vec3 hit = aimYuriBreezilyPlacement(placement); if (hit != null) return hit;
            return aimSourceNormalPlacement(placement, movementYaw, cameraYaw);
        }
        return aimSourceNormalPlacement(placement, movementYaw, cameraYaw);
    }
    private Vec3 aimYuriBreezilyPlacement(Placement placement) {
        if (placement == null) return null;
        float desiredYaw = targetYaw, desiredPitch;
        if (placement.face == EnumFacing.UP) desiredPitch = 90F;
        else {
            desiredYaw = (float) (Math.toDegrees(Math.atan2(placement.face.getFrontOffsetZ(), placement.face.getFrontOffsetX())) % 360D - 90D);
            desiredPitch = 80F;
        }
        float fromYaw = rotationSentLastTick ? lastSentYaw : RotationUtils.serverRotations[0];
        desiredYaw = quantizeMouseRotation(unwrapYaw(desiredYaw, fromYaw), fromYaw);
        desiredPitch = RotationUtils.clampPitch(quantizeMouseRotation(desiredPitch, lastSentPitch));
        Vec3 hit = raycastFace(placement, desiredYaw, desiredPitch);
        if (hit != null) { targetYaw = desiredYaw; targetPitch = desiredPitch; pendingYawJitter = 0; return hit; }
        float facePitch = findNearestFacePitch(placement, desiredYaw, desiredPitch);
        if (!Float.isNaN(facePitch)) {
            facePitch = RotationUtils.clampPitch(quantizeMouseRotation(facePitch, lastSentPitch));
            hit = raycastFace(placement, desiredYaw, facePitch);
            if (hit != null) { targetYaw = desiredYaw; targetPitch = facePitch; pendingYawJitter = 0; return hit; }
        }
        return null;
    }
    private Vec3 aimSourceNormalPlacement(Placement placement, float movementYaw, float cameraYaw) {
        if (placement == null || mc.thePlayer == null) return null;
        double faceX = placement.support.getX() + 0.5D + placement.face.getFrontOffsetX() * 0.5D;
        double faceZ = placement.support.getZ() + 0.5D + placement.face.getFrontOffsetZ() * 0.5D;
        double dx = faceX - mc.thePlayer.posX, dz = faceZ - mc.thePlayer.posZ;
        float centerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz)), serverYaw = RotationUtils.serverRotations[0];
        float[] yawChoices = {centerYaw, movementYaw + 180F, movementYaw + 135F, movementYaw - 135F, targetYaw, lastSentYaw, cameraYaw + 180F, cameraYaw + 90F, cameraYaw - 90F};
        for (int i = 0; i < yawChoices.length; i++) {
            float candidateYaw = quantizeMouseRotation(unwrapYaw(yawChoices[i], serverYaw), lastSentYaw);
            float candidatePitch = findNearestFacePitch(placement, candidateYaw, targetPitch);
            if (Float.isNaN(candidatePitch)) candidatePitch = findFacePitch(placement, candidateYaw);
            if (Float.isNaN(candidatePitch)) continue;
            candidatePitch = RotationUtils.clampPitch(quantizeMouseRotation(candidatePitch, lastSentPitch));
            Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
            if (hit != null) { targetYaw = candidateYaw; targetPitch = candidatePitch; pendingYawJitter = 0; return hit; }
        }
        return null;
    }
    private void applySourceSprintPolicy() {
        String style = getSourceScaffoldMode(), sprintMode = sourceSprintMode.getSelectedOption();
        if (sprintMode == null) sprintMode = "None";
        if (style.equals("Hypixel")) { applySprintPolicy(false); return; }
        if (sprintMode.equals("None")) { applySprintPolicy(true); if (mc.thePlayer != null) mc.thePlayer.setSprinting(false); return; }
        if (sprintMode.equals("NCP")) {
            applySprintPolicy(true);
            if (mc.thePlayer != null) { mc.thePlayer.setSprinting(false); if (isMoving()) client.setSpeed(0.2873D / 1.84D); }
            return;
        }
        applySprintPolicy(false);
        if (sprintMode.equals("Vanilla") && mc.thePlayer != null) mc.thePlayer.setSprinting(isMoving());
    }
    private int getYuriPlaceDelayTicks() { return (int) Math.ceil(Math.max(0, sourcePlaceDelay.getInput()) * 0.4D); }
    private void updateSourceTellyIdleRotation(float cameraYaw) {
        if (mc.thePlayer == null || !mc.thePlayer.onGround || !isMoving()) return;
        boolean sprinting = mc.thePlayer.raw != null && mc.thePlayer.raw.isSprinting();
        if (!sprinting || isJumpPressed()) {
            float fromYaw = rotationSentLastTick ? lastSentYaw : RotationUtils.serverRotations[0];
            targetYaw = quantizeMouseRotation(unwrapYaw(cameraYaw, fromYaw), fromYaw); pendingYawJitter = 0;
        }
    }
    private void updateSourceGodBridgeRotationState() {
        sourceDirectionalChange++;
        float serverYaw = rotationSentLastTick ? lastSentYaw : RotationUtils.serverRotations[0];
        if (Math.abs(MathHelper.wrapAngleTo180_float(targetYaw - serverYaw)) > 10F) sourceDirectionalChange = (int) (Math.random() * 4D);
    }
    private void updateSourceBreezilyMovement(PrePlayerInputEvent input) {
        if (input == null || mc.thePlayer == null || !canApplyBreezilyGroundStrafe(input) || input.getForward() <= 0 || input.isJump()) return;
        double yaw = getPlayerYaw() % 360D; if (yaw < 0) yaw += 360D;
        double fracX = mc.thePlayer.posX - Math.floor(mc.thePlayer.posX), fracZ = mc.thePlayer.posZ - Math.floor(mc.thePlayer.posZ);
        double off, speed;
        if (yaw >= 135D && yaw < 225D) { off = fracX; speed = mc.thePlayer.motionZ; }
        else if (yaw >= 225D && yaw < 315D) { off = fracZ; speed = mc.thePlayer.motionX; }
        else if (yaw >= 45D && yaw < 135D) { off = 1D - fracZ; speed = mc.thePlayer.motionX; }
        else { off = 1D - fracX; speed = mc.thePlayer.motionZ; }
        speed = Math.abs(speed);
        int yuriDelay = (int) Math.round(sourcePlaceDelay.getInput());
        if (speed < 0.086D && Math.abs(off - 0.5D) < 0.4D && yuriDelay <= 1) return;
        double threshold = 0.5D + (Math.random() - 0.5D) / 10D;
        input.setStrafe(off < threshold ? -1F : 1F);
    }
    private void updateHopMotion() {
        if (!Utils.nullCheck() || !isHopMode() || isTowerActive() || !isMoving()) { hopAirTicks = 0; hopLaunchRequested = false; return; }
        EntityPlayerSP player = mc.thePlayer;
        String mode = getHopMode();
        double speedMultiplier = MathHelper.clamp_double(hopSpeed.getInput(), 1D, 1.18D);
        if (hopLaunchRequested && player.onGround) {
            double launchY = mode.equals("Low") ? 0.34D : 0.42D, horizontalMultiplier = 1D;
            if (mode.equals("Boost")) horizontalMultiplier = speedMultiplier;
            else if (mode.equals("Burst")) horizontalMultiplier = Math.min(1.18D, speedMultiplier + 0.03D);
            else if (mode.equals("Adaptive")) {
                boolean safeAhead = !isUnsupportedAt(player.posX + player.motionX * 2.75D, player.posZ + player.motionZ * 2.75D);
                horizontalMultiplier = safeAhead ? Math.min(1.18D, speedMultiplier + 0.02D) : 1.02D;
            } else if (mode.equals("Low")) horizontalMultiplier = Math.min(1.10D, speedMultiplier);
            client.setMotion(player.motionX * horizontalMultiplier, launchY, player.motionZ * horizontalMultiplier);
            player.motionX *= horizontalMultiplier; player.motionY = launchY; player.motionZ *= horizontalMultiplier;
            hopAirTicks = 1; hopLaunchRequested = false; return;
        }
        if (player.onGround) { hopAirTicks = 0; return; }
        hopAirTicks++;
        if (mode.equals("Boost")) {
            if (hopAirTicks == 2) {
                double multiplier = Math.min(1.08D, 1D + (speedMultiplier - 1D) * 0.45D);
                client.setMotion(player.motionX * multiplier, player.motionY, player.motionZ * multiplier);
                player.motionX *= multiplier; player.motionZ *= multiplier;
            }
            return;
        }
        if (mode.equals("Burst")) {
            double newY = player.motionY; if (hopAirTicks == 3) newY -= 0.025D; if (hopAirTicks == 5) newY -= 0.040D;
            if (newY != player.motionY) { client.setMotion(player.motionX, newY, player.motionZ); player.motionY = newY; }
            return;
        }
        if (mode.equals("Adaptive")) {
            boolean safeAhead = !isUnsupportedAt(player.posX + player.motionX * 2.5D, player.posZ + player.motionZ * 2.5D);
            if (safeAhead && hopAirTicks == 3) { double newY = player.motionY - 0.030D; client.setMotion(player.motionX, newY, player.motionZ); player.motionY = newY; }
        }
    }

    // Source 2880-3045: activation stepping and Snap return/face search.
    private void easeActivationRotation(EntityPlayerSP player, float previousYaw, float previousPitch, int lookBucket, boolean cameraAligned) {
        float smoothTicks = ACTIVATION_SMOOTH_TICKS;
        boolean ramping = smoothTicks >= 1F && activationTicks <= smoothTicks;
        float easeFraction = ramping ? 1F - (float) Math.pow(0.05, 1.0 / smoothTicks) : 1F;
        if (ramping) {
            float distance = Math.max(Math.abs(MathHelper.wrapAngleTo180_float(targetYaw - previousYaw)), Math.abs(targetPitch - previousPitch));
            easeFraction = Math.min(1F, easeFraction * rotationEaseScale(distance));
        }
        float targetBucket = Math.round(MathHelper.wrapAngleTo180_float(targetYaw - yawGridBase) / 45F);
        if (isLookLocked() && isLongTellyEnabled() && queuedBlockCount > 0 && !cameraAligned) targetBucket = lookBucket;
        float currentBucket = Math.round(MathHelper.wrapAngleTo180_float(previousYaw - yawGridBase) / 45F);
        while (targetBucket - currentBucket > 4F) targetBucket -= 8F;
        while (targetBucket - currentBucket < -4F) targetBucket += 8F;
        float nextBucket = targetBucket, maxStep = YAW_BUCKET_STEP;
        boolean stepping = !player.onGround;
        if (ramping) {
            activationYawAccumulator += Math.abs(targetBucket - currentBucket) * easeFraction * ACTIVATION_YAW_LAG;
            maxStep = (float) Math.floor(activationYawAccumulator); activationYawAccumulator -= maxStep; stepping = true;
        } else activationYawAccumulator = 0;
        if (stepping && Math.abs(targetBucket - currentBucket) > maxStep) nextBucket = currentBucket + Math.signum(targetBucket - currentBucket) * maxStep;
        aimYawWithJitter(yawGridBase + nextBucket * 45F, jitter(0.18F));
        if (ramping) {
            float pitchDelta = targetPitch - previousPitch;
            if (Math.abs(pitchDelta) > 0.5F) aimPitch(RotationUtils.clampPitch(previousPitch + pitchDelta * easeFraction));
        }
    }
    private boolean isBreezilyGodBridgeJump() { return isBreezilyMode() && mc.thePlayer != null && (isJumpPressed() || !mc.thePlayer.onGround); }
    private boolean isSnapGodBridgeJump() { return isSnapMode() && mc.thePlayer != null && (isJumpPressed() || !mc.thePlayer.onGround) && isAnyMovementKeyDown(); }
    private boolean isSnapRotationActive() { return isSnapMode() && !isSnapGodBridgeJump(); }
    private boolean isSnapPlacementReady(int groundCandidateIndex, Vec3 verifiedHit, Placement placement) {
        if (!isSnapRotationActive() || placementDelayTicks > 0) return false;
        if (groundCandidateIndex >= 0 && groundCandidateIndex < candidates.length) {
            Placement groundPlacement = candidates[groundCandidateIndex];
            if (groundPlacement != null && isReplaceableAt(groundPlacement.getTarget())) return true;
        }
        if (verifiedHit != null && placement != null && isReplaceableAt(placement.getTarget())) return true;
        return pendingPlacementReady && pendingPlacement != null && isReplaceableAt(pendingPlacement.getTarget());
    }
    private boolean isSafeSnapReturn(Vec3 position, Vec3 motion) {
        if (position == null || motion == null || isUnsupportedAt(position.x, position.z)) return false;
        return !isUnsupportedAt(position.x + motion.x * 1.35, position.z + motion.z * 1.35);
    }
    private void applySnapModeRotations(float cameraYaw, boolean placementTick) {
        if (!isSnapRotationActive() || placementTick) return;
        if (isRawDiagonalInput() && !Float.isNaN(snapLastPlacementYaw)) {
            targetYaw = quantizeRotation(unwrapYaw(snapLastPlacementYaw, RotationUtils.serverRotations[0])); pendingYawJitter = 0; return;
        }
        float referenceYaw = RotationUtils.serverRotations[0];
        targetYaw = quantizeRotation(unwrapYaw(cameraYaw, referenceYaw)); pendingYawJitter = 0;
    }
    private Vec3 aimSnapFace(Placement placement, float cameraYaw) {
        if (placement == null) return null;
        EntityPlayerSP player = mc.thePlayer;
        double faceX = placement.support.getX() + 0.5 + placement.face.getFrontOffsetX() * 0.5;
        double faceY = placement.support.getY() + 0.5 + placement.face.getFrontOffsetY() * 0.5;
        double faceZ = placement.support.getZ() + 0.5 + placement.face.getFrontOffsetZ() * 0.5;
        double dx = faceX - player.posX, dz = faceZ - player.posZ;
        float centerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float movementYaw = getMovementYaw(cameraYaw, getForwardInput(), getStrafeInput()), backwardYaw = movementYaw - 180F;
        float[] yawChoices = isRawDiagonalInput()
                ? new float[]{backwardYaw, movementYaw - 135F, movementYaw + 135F, centerYaw, targetYaw, lastSentYaw, movementYaw - 90F, movementYaw + 90F, cameraYaw + 180F}
                : new float[]{targetYaw, centerYaw, backwardYaw, lastSentYaw, cameraYaw + 135F, cameraYaw - 135F, cameraYaw + 180F, cameraYaw + 90F, cameraYaw - 90F};
        for (int i = 0; i < yawChoices.length; i++) {
            float candidateYaw = quantizeMouseRotation(unwrapYaw(yawChoices[i], lastSentYaw), lastSentYaw);
            float candidatePitch = findNearestFacePitch(placement, candidateYaw, targetPitch);
            if (Float.isNaN(candidatePitch)) candidatePitch = findFacePitch(placement, candidateYaw);
            if (Float.isNaN(candidatePitch)) continue;
            candidatePitch = RotationUtils.clampPitch(quantizeMouseRotation(candidatePitch, lastSentPitch));
            Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
            if (hit != null) {
                targetYaw = candidateYaw; targetPitch = candidatePitch; pendingYawJitter = 0;
                lastYawBucket = Math.round(MathHelper.wrapAngleTo180_float(candidateYaw - cameraYaw) / 45F); return hit;
            }
        }
        return null;
    }

    // Source 3057-3435: Ninja lane/recovery and placement dispatch.
    private void resetNinjaPlacementAim() {
        ninjaBridgeYaw = Float.NaN; ninjaBridgePitch = 80F; ninjaBridgeInputMask = 0;
        ninjaBaseYaw = ninjaAppliedYaw = ninjaAimAnchorYaw = Float.NaN; ninjaDiagonalSide = ninjaAimMissTicks = ninjaAimRecoveryCooldown = 0; ninjaPitchRecoveryUsed = false;
    }
    private int getNinjaDirectionMask() {
        int mask = 0;
        if (Utils.isBindDown(mc.gameSettings.keyBindForward)) mask |= 1;
        if (Utils.isBindDown(mc.gameSettings.keyBindBack)) mask |= 2;
        if (Utils.isBindDown(mc.gameSettings.keyBindLeft)) mask |= 4;
        if (Utils.isBindDown(mc.gameSettings.keyBindRight)) mask |= 8;
        return mask;
    }
    private void updateNinjaBridgeLane(EntityPlayerSP player, int mask) {
        if (Float.isNaN(ninjaBaseYaw)) ninjaBaseYaw = player.rotationYaw;
        else if (Math.abs(MathHelper.wrapAngleTo180_float(player.rotationYaw - ninjaBaseYaw)) > 7F) { ninjaBaseYaw = player.rotationYaw; ninjaBridgeInputMask = -1; }
        if (mask == ninjaBridgeInputMask && !Float.isNaN(ninjaBridgeYaw)) return;
        int forward = getForwardInput(), strafe = getStrafeInput(); if (forward == 0 && strafe == 0) return;
        float motionYaw = getMovementYaw(ninjaBaseYaw, forward, strafe), backwardYaw = motionYaw - 180F;
        int diagonalSide = forward != 0 && strafe != 0 ? (strafe < 0 ? -1 : 1) : 0; ninjaDiagonalSide = diagonalSide;
        float sideAngle;
        if (diagonalSide != 0) sideAngle = diagonalSide * 9F;
        else if (forward < 0) sideAngle = (ninjaLastDiagonalSide != 0 ? ninjaLastDiagonalSide : -1) * 19F;
        else sideAngle = 0;
        if (diagonalSide != 0) ninjaLastDiagonalSide = diagonalSide;
        float reference = Float.isNaN(ninjaAppliedYaw) ? player.rotationYaw : ninjaAppliedYaw;
        ninjaBridgeYaw = quantizeMouseRotation(unwrapYaw(backwardYaw + sideAngle, reference), reference); ninjaAimAnchorYaw = ninjaBridgeYaw;
        if (Float.isNaN(ninjaAppliedYaw)) ninjaAppliedYaw = reference;
        if (ninjaBridgeInputMask == 0) {
            float cameraPitch = player.rotationPitch; ninjaBridgePitch = cameraPitch >= 75F && cameraPitch <= 84F ? cameraPitch : 80F;
        }
        ninjaBridgeInputMask = mask; ninjaAimMissTicks = 0; ninjaAimRecoveryCooldown = 0; ninjaPitchRecoveryUsed = false;
    }
    private boolean isNinjaLanePlacement(Placement placement, EntityPlayerSP player) {
        if (placement == null || player == null || Float.isNaN(ninjaBaseYaw)) return false;
        BlockPos target = placement.getTarget(); int rowY = MathHelper.floor_double(player.posY) - 1;
        if (target.getY() != rowY || !isReplaceableAt(target) || !isPlacementWithinReach(placement, 4.5D)
                || isInteractableAt(placement.support.getX(), placement.support.getY(), placement.support.getZ())) return false;
        float moveYaw = getMovementYaw(ninjaBaseYaw, getForwardInput(), getStrafeInput());
        double angle = Math.toRadians(moveYaw), moveX = -Math.sin(angle), moveZ = Math.cos(angle);
        double dx = target.getX() + 0.5D - player.posX, dz = target.getZ() + 0.5D - player.posZ;
        double ahead = dx * moveX + dz * moveZ, sideways = Math.abs(-dx * moveZ + dz * moveX);
        return ahead > (ninjaDiagonalSide == 0 ? -0.30D : -0.65D) && ahead <= 2.10D && sideways <= (ninjaDiagonalSide == 0 ? 0.90D : 1.42D);
    }
    private Placement getNinjaRayPlacement(EntityPlayerSP player, float yaw, float pitch) {
        MovingObjectPosition hit = RotationUtils.rayCastBlock(4.5D, yaw, pitch);
        if (hit == null || hit.getBlockPos() == null || hit.sideHit == null || hit.sideHit == EnumFacing.DOWN) return null;
        Placement candidate = new Placement(hit.getBlockPos(), hit.sideHit);
        if (!isNinjaLanePlacement(candidate, player)) return null;
        ItemStack stack = getPlacementStack(player);
        if (stack == null || !BlockUtils.canPlaceBlockOnSide(stack, candidate.support, candidate.face)) return null;
        return candidate;
    }
    private boolean hasNinjaGapAhead(EntityPlayerSP player) {
        if (player == null) return false;
        int y = MathHelper.floor_double(player.posY) - 1;
        float movement = getMovementYaw(ninjaBaseYaw, getForwardInput(), getStrafeInput());
        double rad = Math.toRadians(movement), dx = -Math.sin(rad), dz = Math.cos(rad);
        for (double d = 0.45D; d <= 1.45D; d += 0.5D) if (!hasCornerSupport(player.posX + dx * d, player.posZ + dz * d, y)) return true;
        return false;
    }
    private void recoverNinjaBridgeAim(EntityPlayerSP player) {
        if (ninjaAimRecoveryCooldown > 0 || ninjaAimMissTicks < 1 || Float.isNaN(ninjaAimAnchorYaw) || !hasNinjaGapAhead(player)) return;
        ninjaAimRecoveryCooldown = 4;
        float anchor = ninjaAimAnchorYaw, currentPitch = ninjaBridgePitch;
        float[] offsets = {0,-4,4,-8,8,-12,12,-18,18,-24,24,-30,30,-36,36};
        float[] pitches = {currentPitch,78,82,75,85,71};
        float winningYaw = Float.NaN, winningPitch = currentPitch;
        double bestScore = Double.POSITIVE_INFINITY;
        for (float candidatePitch : pitches) for (float offset : offsets) {
            float candidateYaw = quantizeMouseRotation(anchor + offset, anchor);
            Placement placement = getNinjaRayPlacement(player, candidateYaw, candidatePitch); if (placement == null) continue;
            Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch); if (hit == null) continue;
            double score = Math.abs(offset) * 0.65D + Math.abs(candidatePitch - currentPitch) * 1.5D + ninjaFaceCenterPenalty(placement, hit) * 8D;
            if (score < bestScore) { bestScore = score; winningYaw = candidateYaw; winningPitch = candidatePitch; }
        }
        Placement planned = findPlacement();
        if (planned != null && isNinjaLanePlacement(planned, player)) {
            double x = planned.support.getX() + 0.5D + planned.face.getFrontOffsetX() * 0.5D;
            double y = planned.support.getY() + 0.5D + planned.face.getFrontOffsetY() * 0.5D;
            double z = planned.support.getZ() + 0.5D + planned.face.getFrontOffsetZ() * 0.5D;
            double dx = x - player.posX, dz = z - player.posZ;
            float faceYaw = quantizeMouseRotation((float) Math.toDegrees(Math.atan2(-dx, dz)), anchor);
            float yawOffset = Math.abs(MathHelper.wrapAngleTo180_float(faceYaw - anchor));
            if (yawOffset <= 36F) {
                float facePitch = RotationUtils.clampPitch((float) Math.toDegrees(Math.atan2(player.posY + player.getEyeHeight() - y, Math.sqrt(dx * dx + dz * dz))));
                facePitch = quantizeRotation(facePitch);
                Vec3 hit = raycastFace(planned, faceYaw, facePitch);
                if (hit != null) {
                    double score = yawOffset * 0.65D + Math.abs(facePitch - currentPitch) * 1.5D + ninjaFaceCenterPenalty(planned, hit) * 8D;
                    if (score < bestScore) { bestScore = score; winningYaw = faceYaw; winningPitch = facePitch; }
                }
            }
        }
        if (!Float.isNaN(winningYaw)) { ninjaBridgeYaw = winningYaw; ninjaBridgePitch = winningPitch; ninjaAimMissTicks = 0; }
    }
    private double ninjaFaceCenterPenalty(Placement placement, Vec3 hit) {
        double x = hit.x - Math.floor(hit.x), y = hit.y - Math.floor(hit.y), z = hit.z - Math.floor(hit.z);
        if (placement.face == EnumFacing.UP) return Math.abs(x - 0.5D) + Math.abs(z - 0.5D);
        if (placement.face == EnumFacing.EAST || placement.face == EnumFacing.WEST) return Math.abs(y - 0.5D) + Math.abs(z - 0.5D);
        return Math.abs(x - 0.5D) + Math.abs(y - 0.5D);
    }
    private void updateNinjaClassicPlacement() {
        rotations.forceMovementFix = false; movementFixActive = false; rotationSentThisTick = false;
        EntityPlayerSP player = mc.thePlayer; if (player == null) return;
        selectPlacementStack(player); int mask = getNinjaDirectionMask();
        if (!isMoving() || !isValidBlock(getPlacementStack(player))) { resetNinjaPlacementAim(); return; }
        updateNinjaBridgeLane(player, mask); if (Float.isNaN(ninjaBridgeYaw)) return;
        if (ninjaAimRecoveryCooldown > 0) ninjaAimRecoveryCooldown--;
        if (placementDelayTicks <= 0 && !placedThisTick && getNinjaRayPlacement(player, ninjaBridgeYaw, ninjaBridgePitch) == null && hasNinjaGapAhead(player)) {
            ninjaAimMissTicks = Math.min(15, ninjaAimMissTicks + 1); recoverNinjaBridgeAim(player);
        }
        float delta = MathHelper.wrapAngleTo180_float(ninjaBridgeYaw - ninjaAppliedYaw), step = MathHelper.clamp_float(delta, -24F, 24F);
        ninjaAppliedYaw = quantizeMouseRotation(ninjaAppliedYaw + step, ninjaAppliedYaw);
        targetYaw = ninjaAppliedYaw; targetPitch = ninjaBridgePitch; lastSentYaw = targetYaw; lastSentPitch = targetPitch;
        rotations.setRotations(targetYaw, targetPitch); rotationSentThisTick = true;
        if (placementDelayTicks <= 0 && !placedThisTick) {
            Placement hit = getNinjaRayPlacement(player, targetYaw, targetPitch);
            boolean validAim = hit != null, placed = hit != null && placeBlock(hit);
            if (!placed) {
                Placement planned = findPlacement();
                if (planned != null && isNinjaLanePlacement(planned, player) && raycastFace(planned, targetYaw, targetPitch) != null) { validAim = true; placed = placeBlock(planned); }
            }
            if (placed) ninjaAimMissTicks = 0;
            else if (!validAim && hasNinjaGapAhead(player)) { ninjaAimMissTicks = Math.min(15, ninjaAimMissTicks + 1); recoverNinjaBridgeAim(player); }
            else if (!hasNinjaGapAhead(player)) ninjaAimMissTicks = 0;
        }
        autoClick(player);
    }
    private void sendRotations() {
        rotations.setRotations(targetYaw, targetPitch); debugSentYaw = targetYaw; previousSentPitch = lastSentPitch;
        lastSentYaw = targetYaw; lastSentPitch = targetPitch; lastBaseYaw = targetYaw - pendingYawJitter; lastBasePitch = targetPitch; rotationSentThisTick = true;
        if (isBreezilyMode() && !isBreezilyGodBridgeJump()) { rotations.forceMovementFix = false; movementFixActive = false; }
        else { rotations.forceMovementFix = true; movementFixActive = rotations.fixMovement(); }
    }
    private void placeSelectedBlock(int groundCandidateIndex, Vec3 verifiedHit, Placement placement) {
        if (isSnapMode() && snapSkipPlacementThisTick) return;
        if (pendingPlacementReady && placementDelayTicks > 0 && !isLongTellyEnabled()) debugLongTellyStatus = 'L';
        if (groundCandidateIndex >= 0 && placementDelayTicks <= 0) placeBlock(candidates[groundCandidateIndex]);
        else if (verifiedHit != null && placementDelayTicks <= 0) placeBlock(placement);
        else if (pendingPlacementReady && (isLongTellyEnabled() || placementDelayTicks <= 0)) {
            if (placeBlock(pendingPlacement)) { verificationBlock = pendingPlacement.getTarget(); verificationDelayTicks = 4; }
        }
    }
    private void updateKeepYPlacement(EntityPlayerSP player, Vec3 position, Vec3 motion) {
        if (getActiveKeepYMode().equals("Telly B") && keepYStage > 0 && !player.onGround) {
            int nextBlockY = MathHelper.floor_double(position.y + motion.y);
            if (nextBlockY <= keepYStartHeight && position.y > keepYStartHeight + 1) {
                useCurrentHeight = true;
                Placement placement = findPlacement();
                if (placement != null && placementDelayTicks <= 0 && !placedThisTick) {
                    Vec3 hit = raycastFace(placement, lastSentYaw, lastSentPitch); if (hit != null) placeBlock(placement);
                }
            }
        }
        if (isKeepYActive() && keepYStage > 0 && placementDelayTicks <= 0) {
            for (int placements = placementAttemptsThisTick; placements < getPlacementLimit(); placements++) if (!placeKeepYFromRay()) break;
        }
    }
    boolean allowPacket(Packet<?> packet) {
        mc.sync();
        if (shouldPauseForGui()) return true;
        if (packet instanceof ServerboundSetCarriedItemPacket) return !isSpoofItemEnabled() || spoofServerSlot < 0 || sendingSpoofSlotPacket;
        if (packet instanceof ServerboundUseItemOnPacket || packet instanceof ServerboundUseItemPacket)
            return sendingPlacement || sendingAutoClick || !(rotationSentThisTick || rotationSentLastTick);
        if (!(packet instanceof ServerboundPlayerActionPacket dig)) return true;
        var status = dig.getAction();
        if (status != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && status != ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK && status != ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) return true;
        EntityPlayerSP player = mc.thePlayer;
        return player == null || !isValidBlock(getPlacementStack(player)) || !(rotationSentThisTick || rotationSentLastTick);
    }
    boolean allowMouse(int button) { mc.sync(); return mc.currentScreen != null || button > 1; }

    // Source 3445-3657: candidate topology, prediction and unsupported-corner scoring.
    private int buildCandidates(int groundY) {
        EntityPlayerSP player = mc.thePlayer; Vec3 position = player.getPositionVector();
        int minX = MathHelper.floor_double(position.x - 0.3), maxX = MathHelper.floor_double(position.x + 0.3);
        int minZ = MathHelper.floor_double(position.z - 0.3), maxZ = MathHelper.floor_double(position.z + 0.3);
        double motionX = player.motionX, motionZ = player.motionZ, horizontalSpeed = Utils.getHorizontalSpeed(player);
        boolean forwardOnly = horizontalSpeed >= 0.02; int count = 0;
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if (!isSolidSupport(x, groundY, z) || isInteractableAt(x, groundY, z)) continue;
            for (int f = 0; f < HORIZONTAL_FACES.length && count < candidates.length; f++) {
                EnumFacing face = HORIZONTAL_FACES[f]; int targetX = x + face.getFrontOffsetX(), targetZ = z + face.getFrontOffsetZ();
                if (forwardOnly && !(neededBlock != null && targetX == neededBlock.getX() && targetZ == neededBlock.getZ())
                        && face.getFrontOffsetX() * motionX + face.getFrontOffsetZ() * motionZ < 0.15 * horizontalSpeed) continue;
                if (isReplaceableAt(targetX, groundY, targetZ)) { candidates[count] = new Placement(new BlockPos(x, groundY, z), face); count++; }
            }
        }
        return count;
    }
    private int matchCandidate(int candidateCount, float candidateYaw, float candidatePitch) {
        MovingObjectPosition hit = RotationUtils.rayCastBlock(4.5, candidateYaw, candidatePitch); if (hit == null) return -1;
        for (int i = 0; i < candidateCount; i++) if (candidates[i].matches(hit)) return i;
        return -1;
    }
    private void buildPrediction() {
        hasPrediction = true; EntityPlayerSP player = mc.thePlayer; Vec3 position = player.getPositionVector();
        Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ); Arrays.fill(predictedPositions, position);
        try {
            SimulatedPlayer simulatedPlayer = createSimulation();
            if (isShortDiagonalJump()) {
                simulatedPlayer.rotationYaw = player.rotationYaw;
                simulatedPlayer.movementInput.forward = getForwardInput(); simulatedPlayer.movementInput.strafe = getStrafeInput(); simulatedPlayer.movementInput.jump = false;
            }
            predictedPositions[0] = new Vec3(position.x, simulatedPlayer.getPos().y, position.z);
            for (int t = 1; t < predictedPositions.length; t++) { simulatedPlayer.tick(); predictedPositions[t] = simulatedPlayer.getPos(); }
        } catch (Throwable ex) {
            double verticalMotion = motion.y, y = position.y;
            for (int t = 1; t < predictedPositions.length; t++) {
                y += verticalMotion; verticalMotion = (verticalMotion - 0.08) * 0.98;
                predictedPositions[t] = new Vec3(position.x + motion.x * t, y, position.z + motion.z * t);
            }
        }
    }
    private boolean isAbovePredictedPath(int blockX, int blockY, int blockZ) {
        EntityPlayerSP player = mc.thePlayer; Vec3 position = player.getPositionVector();
        if (blockY + 1.0 <= position.y + 0.001) return false;
        if (!hasPrediction) buildPrediction();
        double dx = blockX + 0.5 - position.x, dz = blockZ + 0.5 - position.z, horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz)), quadrantAngle = Math.abs(targetYaw % 90F);
        boolean diagonal = quadrantAngle > 22.5F && quadrantAngle < 67.5F;
        int predictionIndex = (int) (horizontalDistance * (diagonal ? 1.5 : 1.0) + 0.7);
        predictionIndex = MathHelper.clamp_int(predictionIndex, 0, predictedPositions.length - 1);
        return blockY > predictedPositions[predictionIndex].y;
    }
    private Vec3 simulatePosition(int ticks, PrePlayerInputEvent input) {
        EntityPlayerSP player = mc.thePlayer; Vec3 position = player.getPositionVector();
        try {
            SimulatedPlayer simulatedPlayer = new SimulatedPlayer(player.movementInput);
            simulatedPlayer.rotationYaw = player.rotationYaw; simulatedPlayer.movementInput.forward = input.getForward();
            simulatedPlayer.movementInput.strafe = input.getStrafe(); simulatedPlayer.movementInput.jump = input.isJump(); simulatedPlayer.movementInput.sneak = false;
            for (int t = 0; t < ticks; t++) simulatedPlayer.tick();
            return simulatedPlayer.getPos();
        } catch (Throwable t) { return position.add(player.motionX * ticks, 0, player.motionZ * ticks); }
    }
    private double getSupportOverhang(double playerX, double playerZ) {
        EntityPlayerSP player = mc.thePlayer; int groundY = MathHelper.floor_double(player.getPositionVector().y) - 1;
        int centerX = MathHelper.floor_double(playerX), centerZ = MathHelper.floor_double(playerZ); double best = 999;
        for (int x = centerX - 1; x <= centerX + 1; x++) for (int z = centerZ - 1; z <= centerZ + 1; z++) {
            if (!isSolidSupport(x, groundY, z)) continue;
            double nearestX = MathHelper.clamp_double(playerX, x, x + 1.0), nearestZ = MathHelper.clamp_double(playerZ, z, z + 1.0);
            double distance = Math.max(Math.abs(playerX - nearestX), Math.abs(playerZ - nearestZ)); if (distance < best) best = distance;
        }
        return best;
    }
    private boolean isNearEdge(int simTicks, PrePlayerInputEvent input, double edgeLead) {
        Vec3 predictedPosition = simulatePosition(simTicks, input); EntityPlayerSP player = mc.thePlayer;
        Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ); double horizontalSpeed = Utils.getHorizontalSpeed(player);
        double probeX = predictedPosition.x, probeZ = predictedPosition.z;
        if (horizontalSpeed > 0.01) { double lead = Math.max(0, edgeLead); probeX += motion.x / horizontalSpeed * lead; probeZ += motion.z / horizontalSpeed * lead; }
        double overhang = getSupportOverhang(probeX, probeZ); if (simTicks == 1) debugOverhang = overhang;
        return overhang > 0;
    }
    private void findNeededBlock(int groundY) {
        neededBlock = null; EntityPlayerSP player = mc.thePlayer; Vec3 position = player.getPositionVector();
        try {
            SimulatedPlayer simulatedPlayer = createSimulation();
            for (int t = 0; t < 5 && neededBlock == null; t++) { simulatedPlayer.tick(); Vec3 predictedPosition = simulatedPlayer.getPos(); checkUnsupportedCorners(predictedPosition.x, predictedPosition.z, groundY); }
        } catch (Throwable t) {
            Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
            for (int k = 1; k <= 5 && neededBlock == null; k++) checkUnsupportedCorners(position.x + motion.x * k, position.z + motion.z * k, groundY);
        }
    }
    private SimulatedPlayer createSimulation() {
        SimulatedPlayer simulatedPlayer = new SimulatedPlayer(mc.thePlayer.movementInput);
        if (predictionInputPlayer == mc.thePlayer && !Float.isNaN(predictionInputYaw)) simulatedPlayer.rotationYaw = predictionInputYaw;
        simulatedPlayer.movementInput.sneak = false; return simulatedPlayer;
    }
    private void checkUnsupportedCorners(double playerX, double playerZ, int groundY) {
        Vec3 motion = new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ); double bestScore = -1.0E9;
        for (int corner = 0; corner < 4; corner++) {
            double offsetX = (corner & 1) == 0 ? -0.3 : 0.3, offsetZ = (corner & 2) == 0 ? -0.3 : 0.3;
            int blockX = MathHelper.floor_double(playerX + offsetX), blockZ = MathHelper.floor_double(playerZ + offsetZ);
            if (!isReplaceableAt(blockX, groundY, blockZ)) continue;
            double score = offsetX * motion.x + offsetZ * motion.z;
            double centerOffsetX = blockX + 0.5 - playerX, centerOffsetZ = blockZ + 0.5 - playerZ;
            float cellYaw = (float) Math.toDegrees(Math.atan2(-centerOffsetX, centerOffsetZ));
            float yawOffset = Math.abs(MathHelper.wrapAngleTo180_float(cellYaw - lastSentYaw));
            score += 0.01 * (180F - yawOffset) / 180.0;
            if (score > bestScore) { bestScore = score; neededBlock = new BlockPos(blockX, groundY, blockZ); }
        }
    }

    // Source 3659-4071: analytic face geometry and Ninja's unsneaked predictor/hysteresis.
    private FaceGeometry getFaceGeometry(Placement placement, float candidateYaw) {
        int blockX = placement.support.getX(), blockY = placement.support.getY(), blockZ = placement.support.getZ();
        EnumFacing face = placement.face; EntityPlayerSP player = mc.thePlayer; Vec3 position = player.getPositionVector();
        double eyeX = position.x, eyeY = position.y + player.getEyeHeight(), eyeZ = position.z;
        double directionX = -Math.sin(Math.toRadians(candidateYaw)), directionZ = Math.cos(Math.toRadians(candidateYaw));
        if (face == EnumFacing.UP) {
            double topY = blockY + 1.0; if (eyeY <= topY + 0.05) return null;
            double nearDistance = 0.001, farDistance = 4.0;
            if (Math.abs(directionX) < 1.0E-6) { if (eyeX < blockX + 0.05 || eyeX > blockX + 0.95) return null; }
            else {
                double minEdgeIntersection = (blockX + 0.05 - eyeX) / directionX, maxEdgeIntersection = (blockX + 0.95 - eyeX) / directionX;
                nearDistance = Math.max(nearDistance, Math.min(minEdgeIntersection, maxEdgeIntersection)); farDistance = Math.min(farDistance, Math.max(minEdgeIntersection, maxEdgeIntersection));
            }
            if (Math.abs(directionZ) < 1.0E-6) { if (eyeZ < blockZ + 0.05 || eyeZ > blockZ + 0.95) return null; }
            else {
                double minEdgeIntersection = (blockZ + 0.05 - eyeZ) / directionZ, maxEdgeIntersection = (blockZ + 0.95 - eyeZ) / directionZ;
                nearDistance = Math.max(nearDistance, Math.min(minEdgeIntersection, maxEdgeIntersection)); farDistance = Math.min(farDistance, Math.max(minEdgeIntersection, maxEdgeIntersection));
            }
            if (nearDistance >= farDistance) return null;
            return new FaceGeometry(true, nearDistance, farDistance, eyeY, topY);
        }
        if (face.getFrontOffsetY() != 0) return null;
        double distance, hitCoordinate, blockEdge;
        if (face.getFrontOffsetX() != 0) {
            double facePlane = blockX + (face.getFrontOffsetX() > 0 ? 1.0 : 0.0);
            if ((eyeX - facePlane) * face.getFrontOffsetX() <= 0 || Math.abs(directionX) < 1.0E-6) return null;
            distance = (facePlane - eyeX) / directionX; hitCoordinate = eyeZ + directionZ * distance; blockEdge = blockZ;
        } else {
            double facePlane = blockZ + (face.getFrontOffsetZ() > 0 ? 1.0 : 0.0);
            if ((eyeZ - facePlane) * face.getFrontOffsetZ() <= 0 || Math.abs(directionZ) < 1.0E-6) return null;
            distance = (facePlane - eyeZ) / directionZ; hitCoordinate = eyeX + directionX * distance; blockEdge = blockX;
        }
        if (distance <= 0 || distance > 4.0 || hitCoordinate < blockEdge + 0.05 || hitCoordinate > blockEdge + 0.95) return null;
        return new FaceGeometry(false, distance, distance, eyeY, blockY);
    }
    private float findFacePitch(Placement placement, float candidateYaw) {
        FaceGeometry geometry = getFaceGeometry(placement, candidateYaw); if (geometry == null) return Float.NaN;
        double[] samples = geometry.topFace ? TOP_FACE_SAMPLES : SIDE_FACE_SAMPLES;
        for (double sample : samples) {
            float candidatePitch;
            if (geometry.topFace) {
                double sampleDistance = geometry.nearDistance + (geometry.farDistance - geometry.nearDistance) * sample;
                candidatePitch = (float) Math.toDegrees(Math.atan2(geometry.eyeY - geometry.surfaceY, sampleDistance));
            } else candidatePitch = (float) Math.toDegrees(Math.atan2(geometry.eyeY - (geometry.surfaceY + sample), geometry.nearDistance));
            if (candidatePitch > 90F) candidatePitch = 90F;
            candidatePitch = quantizeRotation(candidatePitch);
            if (raycastFace(placement, candidateYaw, candidatePitch) != null) return candidatePitch;
        }
        return Float.NaN;
    }
    private float findNearestFacePitch(Placement placement, float candidateYaw, float currentPitch) {
        FaceGeometry geometry = getFaceGeometry(placement, candidateYaw); if (geometry == null) return Float.NaN;
        double minimumPitch, maximumPitch;
        if (geometry.topFace) {
            minimumPitch = Math.toDegrees(Math.atan2(geometry.eyeY - geometry.surfaceY, geometry.farDistance));
            maximumPitch = Math.toDegrees(Math.atan2(geometry.eyeY - geometry.surfaceY, geometry.nearDistance));
        } else {
            minimumPitch = Math.toDegrees(Math.atan2(geometry.eyeY - (geometry.surfaceY + 0.95), geometry.nearDistance));
            maximumPitch = Math.toDegrees(Math.atan2(geometry.eyeY - (geometry.surfaceY + 0.05), geometry.nearDistance));
        }
        double margin = Math.min(0.35, (maximumPitch - minimumPitch) * 0.25);
        double innerMinimum = minimumPitch + margin, innerMaximum = maximumPitch - margin;
        if (innerMinimum > innerMaximum) { innerMinimum = (minimumPitch + maximumPitch) * 0.5; innerMaximum = innerMinimum; }
        if (currentPitch >= innerMinimum && currentPitch <= innerMaximum && raycastFace(placement, candidateYaw, currentPitch) != null) return currentPitch;
        double desiredPitch = currentPitch, span = innerMaximum - innerMinimum, step = Math.min(0.45, span * 0.30);
        if (desiredPitch < innerMinimum) desiredPitch = innerMinimum + randomFloat(0F, (float) step);
        else if (desiredPitch > innerMaximum) desiredPitch = innerMaximum - randomFloat(0F, (float) step);
        float candidatePitch = quantizeRotation(RotationUtils.clampPitch((float) desiredPitch));
        if (raycastFace(placement, candidateYaw, candidatePitch) != null) return candidatePitch;
        double[] samples = {0.5 + randomFloat(-0.06F, 0.06F), 0.3 + randomFloat(-0.05F, 0.05F), 0.7 + randomFloat(-0.05F, 0.05F)};
        for (double sample : samples) {
            float samplePitch = quantizeRotation(RotationUtils.clampPitch((float) (minimumPitch + (maximumPitch - minimumPitch) * sample)));
            if (raycastFace(placement, candidateYaw, samplePitch) != null) return samplePitch;
        }
        return Float.NaN;
    }
    private int getCandidateScore(int index) {
        if (neededBlock == null) return 0;
        Placement candidate = candidates[index];
        int targetX = candidate.support.getX() + candidate.face.getFrontOffsetX(), targetZ = candidate.support.getZ() + candidate.face.getFrontOffsetZ();
        if (targetX == neededBlock.getX() && targetZ == neededBlock.getZ()) return 2;
        int distance = Math.abs(targetX - neededBlock.getX()) + Math.abs(targetZ - neededBlock.getZ()); return distance == 1 ? 1 : 0;
    }
    private void resetNinjaState() {
        resetNinjaPlacementAim(); ninjaLastDiagonalSide = 0; ninjaSneaking = false; ninjaReleaseAt = ninjaJumpSneakUntil = 0L;
        ninjaJumpKeyWasDown = false; ninjaTrackedSupportBlock = ninjaEdgePulseSupport = null; ninjaSneakSafeTicks = 0;
    }
    private BlockPos findNinjaSupportBlock(EntityPlayerSP player) {
        if (player == null) return null;
        int y = MathHelper.floor_double(player.posY - 0.01D), centerX = MathHelper.floor_double(player.posX), centerZ = MathHelper.floor_double(player.posZ);
        BlockPos best = null; double bestDistance = Double.POSITIVE_INFINITY;
        for (int x = centerX - 1; x <= centerX + 1; x++) for (int z = centerZ - 1; z <= centerZ + 1; z++) {
            if (!isSolidSupport(x, y, z)) continue;
            double nearX = MathHelper.clamp_double(player.posX, x, x + 1D), nearZ = MathHelper.clamp_double(player.posZ, z, z + 1D);
            double dx = player.posX - nearX, dz = player.posZ - nearZ, distance = dx * dx + dz * dz;
            if (distance < bestDistance) { bestDistance = distance; best = new BlockPos(x, y, z); }
        }
        return best;
    }
    private Vec3 simulateNinjaUnsneakedPosition(PrePlayerInputEvent input) {
        EntityPlayerSP player = mc.thePlayer; if (player == null || input == null) return null;
        try {
            RavenScaffoldSimulation sim = RavenScaffoldSimulation.create();
            float forward = input.getForward(), strafe = input.getStrafe();
            if (client.isSneak() && Math.abs(forward) <= 0.301F && Math.abs(strafe) <= 0.301F) { forward /= 0.3F; strafe /= 0.3F; }
            sim.setForward(MathHelper.clamp_float(forward, -1F, 1F)); sim.setStrafe(MathHelper.clamp_float(strafe, -1F, 1F));
            sim.setJump(input.isJump()); sim.setSneak(false); sim.tick();
            Vec3 result = sim.getPosition();
            if (result != null && !Double.isNaN(result.x) && !Double.isNaN(result.z) && !Double.isInfinite(result.x) && !Double.isInfinite(result.z)) return result;
        } catch (Exception ignored) { }
        return new Vec3(player.posX + player.motionX, player.posY, player.posZ + player.motionZ);
    }
    private double computeNinjaEdgeOffset(Vec3 predicted, Vec3 current) {
        if (predicted == null || current == null) return Double.NaN;
        int floorY = MathHelper.floor_double(current.y - 0.01D); double best = Double.NaN;
        double[] corners = {-0.30D, 0.30D};
        for (double cornerX : corners) for (double cornerZ : corners) {
            int bx = MathHelper.floor_double(current.x + cornerX), bz = MathHelper.floor_double(current.z + cornerZ);
            if (!isSolidSupport(bx, floorY, bz)) continue;
            double offX = Math.abs(predicted.x - (bx + (predicted.x < bx + 0.5D ? 0D : 1D)));
            double offZ = Math.abs(predicted.z - (bz + (predicted.z < bz + 0.5D ? 0D : 1D)));
            boolean xDiff = MathHelper.floor_double(predicted.x) != bx, zDiff = MathHelper.floor_double(predicted.z) != bz;
            double distance = xDiff ? (zDiff ? Math.max(offX, offZ) : offX) : (zDiff ? offZ : 0D);
            best = Double.isNaN(best) ? distance : Math.min(best, distance);
        }
        return best;
    }
    private long getNinjaRandomDelay() {
        double min = Math.max(0D, ninjaMinDelay.getInput()), max = Math.max(0D, ninjaMaxDelay.getInput());
        if (max < min) { double swap = min; min = max; max = swap; }
        return Math.round(min + Math.random() * (max - min));
    }
    private void updateNinjaSneak(PrePlayerInputEvent input) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || input == null) { resetNinjaState(); return; }
        boolean manualSneak = keybinds.sneakDown(), requireSneakKey = ninjaRequireSneakKey.isToggled();
        boolean moving = Math.abs(input.getForward()) > 1.0E-4F || Math.abs(input.getStrafe()) > 1.0E-4F;
        boolean jumping = input.isJump() || isJumpPressed(), jumpStarted = jumping && !ninjaJumpKeyWasDown; ninjaJumpKeyWasDown = jumping;
        if (isTowerActive() || !requireSneakKey && manualSneak || requireSneakKey && !manualSneak) {
            ninjaSneaking = false; ninjaReleaseAt = ninjaJumpSneakUntil = 0; ninjaTrackedSupportBlock = ninjaEdgePulseSupport = null; ninjaSneakSafeTicks = 0; return;
        }
        if (requireSneakKey && !moving) {
            ninjaSneaking = false; ninjaReleaseAt = ninjaJumpSneakUntil = 0; ninjaEdgePulseSupport = null; ninjaSneakSafeTicks = 0; input.setSneak(true); return;
        }
        boolean allowed = (!ninjaRequireBlocks.isToggled() || isValidBlock(getPlacementStack(player)))
                && (!ninjaRequireLookingDown.isToggled() || player.rotationPitch >= 70F) && (!ninjaDisableForward.isToggled() || input.getForward() <= 0F);
        if (!allowed) {
            ninjaSneaking = false; ninjaReleaseAt = ninjaJumpSneakUntil = 0; ninjaEdgePulseSupport = null; ninjaSneakSafeTicks = 0;
            if (requireSneakKey) input.setSneak(true); return;
        }
        long now = client.time(); double jumpSneakMs = Math.max(0D, ninjaSneakOnJump.getInput());
        if (jumpStarted && player.onGround && moving && jumpSneakMs > 0) { ninjaJumpSneakUntil = now + Math.round(jumpSneakMs); ninjaReleaseAt = 0; }
        Vec3 current = player.getPositionVector(), predicted = simulateNinjaUnsneakedPosition(input);
        double edgeOffset = computeNinjaEdgeOffset(predicted, current), threshold = MathHelper.clamp_double(ninjaEdgeDistance.getInput(), 0D, 0.30D);
        boolean edgeReached;
        if (Double.isNaN(edgeOffset)) edgeReached = player.onGround && isNearEdge(1, input, threshold);
        else { debugOverhang = edgeOffset; edgeReached = player.onGround && edgeOffset > threshold; }
        if (player.onGround && predicted != null && getForwardInput() != 0 && getStrafeInput() != 0) {
            int supportY = MathHelper.floor_double(player.posY - 0.01D); if (!hasCornerSupport(predicted.x, predicted.z, supportY)) edgeReached = true;
        }
        BlockPos support = findNinjaSupportBlock(player);
        boolean changedSupport = support != null && !support.equals(ninjaTrackedSupportBlock); ninjaTrackedSupportBlock = support;
        if (changedSupport) ninjaSneakSafeTicks = 0;
        boolean safePredicted = predicted != null && hasCornerSupport(predicted.x, predicted.z, MathHelper.floor_double(player.posY - 0.01D));
        boolean nearReleaseEdge = !Double.isNaN(edgeOffset) && edgeOffset > Math.max(0D, threshold - 0.05D);
        boolean samePulseSupport = support != null && support.equals(ninjaEdgePulseSupport);
        boolean shouldHold = edgeReached || ninjaJumpSneakUntil > now || ninjaSneaking && ninjaAvoidDoubleSneaking.isToggled() && samePulseSupport && !changedSupport && nearReleaseEdge;
        if (shouldHold) { ninjaSneaking = true; ninjaEdgePulseSupport = support; ninjaSneakSafeTicks = 0; ninjaReleaseAt = 0; }
        else if (ninjaSneaking) {
            ninjaSneakSafeTicks = Math.min(6, ninjaSneakSafeTicks + 1);
            boolean avoidDouble = ninjaAvoidDoubleSneaking.isToggled(), stable = safePredicted && (!avoidDouble || changedSupport || ninjaSneakSafeTicks >= 2);
            if (stable) {
                if (ninjaReleaseAt == 0) ninjaReleaseAt = now + getNinjaRandomDelay();
                if (now >= ninjaReleaseAt) { ninjaSneaking = false; ninjaReleaseAt = 0; ninjaSneakSafeTicks = 0; }
            } else ninjaReleaseAt = 0;
        } else { ninjaSneakSafeTicks = 0; ninjaEdgePulseSupport = null; }
        if (ninjaJumpSneakUntil != 0 && now >= ninjaJumpSneakUntil) ninjaJumpSneakUntil = 0;
        if (requireSneakKey) input.setSneak(ninjaSneaking); else if (ninjaSneaking) input.setSneak(true);
    }

    // Source 4073-4485: Rush topology/warming/verified chains and source tower placement.
    private void resetRushState() {
        rushLastPlacedTarget = null; rushPlannedPlacement = null; rushTieXNext = true; rushHeadingX = rushHeadingZ = rushMissStreak = 0;
        rushWarmTarget = null; rushWarmFace = null; rushWarmTicks = 0; rushLaneYaw = rushLanePitch = Float.NaN; rushLaneAge = 0;
        towerLastPlacedY = Integer.MIN_VALUE; towerWasActive = false; tower.reset();
    }
    private BlockPos findFrontSupport(Vec3 position, int supportY, int headingX, int headingZ) {
        if (position == null) return null;
        int minX = MathHelper.floor_double(position.x - 0.30D), maxX = MathHelper.floor_double(position.x + 0.30D);
        int minZ = MathHelper.floor_double(position.z - 0.30D), maxZ = MathHelper.floor_double(position.z + 0.30D);
        double norm = Math.sqrt((double) headingX * headingX + (double) headingZ * headingZ);
        double hx = norm <= 0 ? 0 : headingX / norm, hz = norm <= 0 ? 0 : headingZ / norm;
        BlockPos best = null; double bestScore = -1.0E12D;
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if (!isSolidSupport(x, supportY, z) || isInteractableAt(x, supportY, z)) continue;
            double overlapX = Math.max(0D, Math.min(position.x + 0.30D, x + 1D) - Math.max(position.x - 0.30D, x));
            double overlapZ = Math.max(0D, Math.min(position.z + 0.30D, z + 1D) - Math.max(position.z - 0.30D, z));
            double area = overlapX * overlapZ; if (area <= 0) continue;
            double front = (x + 0.5D - position.x) * hx + (z + 0.5D - position.z) * hz, score = area * 10D + front;
            if (best == null || score > bestScore) { bestScore = score; best = new BlockPos(x, supportY, z); }
        }
        return best;
    }
    private int[] getFrontHeading(float cameraYaw) {
        int forward = getForwardInput(), strafe = getStrafeInput(); if (forward == 0 && strafe == 0) return new int[]{0, 0};
        float movementYaw = getMovementYaw(cameraYaw, forward, strafe); int octant = Math.round(movementYaw / 45F);
        double radians = Math.toRadians(octant * 45D);
        int dx = (int) Math.round(-Math.sin(radians)), dz = (int) Math.round(Math.cos(radians));
        return new int[]{MathHelper.clamp_int(dx, -1, 1), MathHelper.clamp_int(dz, -1, 1)};
    }
    private EnumFacing getFrontFace(int stepX, int stepZ) {
        if (stepX > 0) return EnumFacing.EAST; if (stepX < 0) return EnumFacing.WEST;
        if (stepZ > 0) return EnumFacing.SOUTH; if (stepZ < 0) return EnumFacing.NORTH; return null;
    }
    private Placement makeFrontStep(BlockPos support, int stepX, int stepZ) {
        if (support == null) return null;
        EnumFacing face = getFrontFace(stepX, stepZ); if (face == null) return null;
        Placement placement = new Placement(support, face); return isReplaceableAt(placement.getTarget()) ? placement : null;
    }
    private Vec3 getFrontFaceCenter(Placement placement) {
        if (placement == null || placement.face == null) return null;
        return new Vec3(placement.support.getX() + 0.5D + placement.face.getFrontOffsetX() * 0.5D,
                placement.support.getY() + 0.5D + placement.face.getFrontOffsetY() * 0.5D,
                placement.support.getZ() + 0.5D + placement.face.getFrontOffsetZ() * 0.5D);
    }
    private double frontProjection(BlockPos block, Vec3 position, int dx, int dz) {
        double norm = Math.sqrt((double) dx * dx + (double) dz * dz);
        if (norm <= 0 || block == null || position == null) return 0;
        double hx = dx / norm, hz = dz / norm;
        return (block.getX() + 0.5D - position.x) * hx + (block.getZ() + 0.5D - position.z) * hz;
    }
    private BlockPos getFrontAnchor(Vec3 position, int supportY, int dx, int dz, BlockPos remembered, double maxDistance) {
        if (remembered != null && isSolidSupport(remembered.getX(), remembered.getY(), remembered.getZ())) {
            double rx = remembered.getX() + 0.5D - position.x, rz = remembered.getZ() + 0.5D - position.z;
            if (rx * rx + rz * rz <= maxDistance * maxDistance) return remembered;
        }
        return findFrontSupport(position, supportY, dx, dz);
    }
    private Placement planRushPlacement(Vec3 position, float cameraYaw) {
        if (position == null || mc.thePlayer == null || placementDelayTicks > 0) return null;
        int[] heading = getFrontHeading(cameraYaw); int dx = heading[0], dz = heading[1]; if (dx == 0 && dz == 0) return null;
        if (dx != rushHeadingX || dz != rushHeadingZ) {
            rushHeadingX = dx; rushHeadingZ = dz; rushLastPlacedTarget = null;
            rushTieXNext = Math.abs(mc.thePlayer.motionX) >= Math.abs(mc.thePlayer.motionZ);
            rushLaneYaw = rushLanePitch = Float.NaN; rushLaneAge = 0;
        }
        int floorY = MathHelper.floor_double(position.y);
        int supportY = (keepYStage != 0 && !useCurrentHeight ? Math.min(floorY, keepYStartHeight) : floorY) - 1;
        double requestedLook = rushLookAhead.getInput(), horizontalSpeed = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
        double safeLook = 1.55D + horizontalSpeed * 2.35D, look = Math.max(1.55D, Math.min(requestedLook, safeLook));
        BlockPos anchor = getFrontAnchor(position, supportY, dx, dz, rushLastPlacedTarget, 4.05D); if (anchor == null) return null;
        double norm = Math.sqrt((double) dx * dx + (double) dz * dz), hx = dx / norm, hz = dz / norm;
        double futureX = position.x + hx * look + mc.thePlayer.motionX * 0.18D, futureZ = position.z + hz * look + mc.thePlayer.motionZ * 0.18D;
        int targetX = MathHelper.floor_double(futureX), targetZ = MathHelper.floor_double(futureZ);
        for (int scan = 0; scan < 5; scan++) {
            int remX = targetX - anchor.getX(), remZ = targetZ - anchor.getZ(); if (remX == 0 && remZ == 0) return null;
            int stepX = 0, stepZ = 0, absX = Math.abs(remX), absZ = Math.abs(remZ);
            if (absX > absZ) stepX = remX > 0 ? 1 : -1;
            else if (absZ > absX) stepZ = remZ > 0 ? 1 : -1;
            else if (absX > 0) {
                if (rushTieXNext) stepX = remX > 0 ? 1 : -1; else stepZ = remZ > 0 ? 1 : -1;
                rushTieXNext = !rushTieXNext;
            }
            EnumFacing face = getFrontFace(stepX, stepZ); if (face == null) return null;
            BlockPos nextCell = anchor.offset(face);
            if (isSolidSupport(nextCell.getX(), nextCell.getY(), nextCell.getZ()) && !isInteractableAt(nextCell.getX(), nextCell.getY(), nextCell.getZ())) {
                anchor = nextCell; rushLastPlacedTarget = nextCell; continue;
            }
            Placement planned = makeFrontStep(anchor, stepX, stepZ);
            if (planned == null) {
                if (stepX != 0 && remZ != 0) planned = makeFrontStep(anchor, 0, remZ > 0 ? 1 : -1);
                else if (stepZ != 0 && remX != 0) planned = makeFrontStep(anchor, remX > 0 ? 1 : -1, 0);
            }
            if (planned == null || !isPlacementWithinReach(planned, 4.05D)) return null;
            double lead = frontProjection(planned.getTarget(), position, dx, dz); if (lead > look + 0.85D) return null;
            return planned;
        }
        return null;
    }
    private boolean isPlacementWithinReach(Placement placement, double reach) {
        if (placement == null || mc.thePlayer == null) return false;
        Vec3 hit = getFrontFaceCenter(placement); if (hit == null) return false;
        double dx = hit.x - mc.thePlayer.posX, dy = hit.y - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight()), dz = hit.z - mc.thePlayer.posZ;
        return dx * dx + dy * dy + dz * dz <= reach * reach;
    }
    private Vec3 aimRushPlacement(Placement placement, float cameraYaw) {
        if (placement == null || !isPlacementWithinReach(placement, 4.00D)) return null;
        EntityPlayerSP player = mc.thePlayer; Vec3 faceCenter = getFrontFaceCenter(placement);
        double dx = faceCenter.x - player.posX, dz = faceCenter.z - player.posZ;
        float centerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float moveYaw = getMovementYaw(cameraYaw, getForwardInput(), getStrafeInput()), laneBase = moveYaw + 180F;
        float[] yawChoices = {rushLaneYaw, lastSentYaw, laneBase, laneBase - 6F, laneBase + 6F, centerYaw, laneBase - 12F, laneBase + 12F};
        for (float rawYaw : yawChoices) {
            if (Float.isNaN(rawYaw)) continue;
            float candidateYaw = quantizeMouseRotation(unwrapYaw(rawYaw, lastSentYaw), lastSentYaw), pitchBase = Float.isNaN(rushLanePitch) ? 80F : rushLanePitch;
            float candidatePitch = findNearestFacePitch(placement, candidateYaw, pitchBase);
            if (Float.isNaN(candidatePitch)) candidatePitch = findFacePitch(placement, candidateYaw);
            if (Float.isNaN(candidatePitch)) continue;
            candidatePitch = RotationUtils.clampPitch(quantizeMouseRotation(candidatePitch, lastSentPitch));
            Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
            if (hit != null) {
                targetYaw = candidateYaw; targetPitch = candidatePitch; pendingYawJitter = 0; rushLaneYaw = candidateYaw; rushLanePitch = candidatePitch;
                rushLaneAge = Math.min(40, rushLaneAge + 1); return hit;
            }
        }
        rushLaneAge = 0; return null;
    }
    private void runRushVerifiedChain(Vec3 position, float cameraYaw) {
        if (!placedThisTick || mc.thePlayer == null || placementDelayTicks > 0) return;
        int desired = MathHelper.clamp_int((int) Math.round(rushChain.getInput()), 1, 3), limit = Math.min(3, desired);
        while (placementAttemptsThisTick < limit) {
            Placement next = planRushPlacement(mc.thePlayer.getPositionVector(), cameraYaw);
            if (next == null || !isPlacementWithinReach(next, 3.95D)) return;
            Vec3 hit = raycastFace(next, lastSentYaw, lastSentPitch); if (hit == null) return;
            rushPlannedPlacement = next; if (!placeBlock(next)) { rushPlannedPlacement = null; return; }
        }
    }
    private void updateTowerSession(boolean active) {
        if (active && !towerWasActive) towerLastPlacedY = Integer.MIN_VALUE;
        else if (!active && towerWasActive) { towerLastPlacedY = Integer.MIN_VALUE; rushPlannedPlacement = null; }
        if (tower.updateSession(active)) rushPlannedPlacement = null;
        towerWasActive = active;
    }
    boolean shouldSpoofGround() {
        mc.sync();
        return Utils.nullCheck() && !shouldPauseForGui() && tower.shouldSpoofGround(selectedTowerMode, isTowerActive());
    }
    boolean isTowerActive() {
        if (!towerEnabled || mc.thePlayer == null || !Utils.isBindDown(mc.gameSettings.keyBindJump) || isAnyMovementKeyDown()) return false;
        return isValidBlock(getPlacementStack(mc.thePlayer));
    }
    private boolean towerOwnsJump() { return isTowerActive() && ("KRS NCP".equals(selectedTowerMode) || ScaffoldTowerController.ownsJump(selectedTowerMode)); }
    private Placement findTowerPlacement(Vec3 position) {
        if (position == null || mc.thePlayer == null || placementDelayTicks > 0) return null;
        double px = position.x + mc.thePlayer.motionX * 0.30D, pz = position.z + mc.thePlayer.motionZ * 0.30D;
        int startY = MathHelper.floor_double(position.y - 0.01D), minX = MathHelper.floor_double(px - 0.29D), maxX = MathHelper.floor_double(px + 0.29D);
        int minZ = MathHelper.floor_double(pz - 0.29D), maxZ = MathHelper.floor_double(pz + 0.29D);
        Placement best = null; double bestScore = -1.0E12D;
        for (int y = startY; y >= startY - 3; y--) {
            for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                if (!isSolidSupport(x, y, z) || isInteractableAt(x, y, z)) continue;
                int targetY = y + 1;
                if (targetY <= towerLastPlacedY || !isReplaceableAt(x, targetY, z) || position.y < targetY + 1.001D) continue;
                double overlapX = Math.max(0D, Math.min(px + 0.30D, x + 1D) - Math.max(px - 0.30D, x));
                double overlapZ = Math.max(0D, Math.min(pz + 0.30D, z + 1D) - Math.max(pz - 0.30D, z));
                double area = overlapX * overlapZ; if (area <= 0) continue;
                Placement candidate = new Placement(new BlockPos(x, y, z), EnumFacing.UP); if (!isPlacementWithinReach(candidate, 4.35D)) continue;
                double centerDx = x + 0.5D - px, centerDz = z + 0.5D - pz, score = area * 20D - centerDx * centerDx - centerDz * centerDz + y * 0.01D;
                if (best == null || score > bestScore) { best = candidate; bestScore = score; }
            }
            if (best != null) break;
        }
        return best;
    }
    private Vec3 aimTowerFace(Placement placement, float cameraYaw) {
        if (placement == null || placement.face != EnumFacing.UP) return null;
        EntityPlayerSP player = mc.thePlayer; Vec3 center = getFrontFaceCenter(placement);
        double dx = center.x - player.posX, dz = center.z - player.posZ;
        float centerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float[] yawChoices = {cameraYaw, lastSentYaw, centerYaw, cameraYaw + 180F};
        for (float rawYaw : yawChoices) {
            float candidateYaw = quantizeMouseRotation(unwrapYaw(rawYaw, lastSentYaw), lastSentYaw), candidatePitch = findNearestFacePitch(placement, candidateYaw, 86F);
            if (Float.isNaN(candidatePitch)) candidatePitch = findFacePitch(placement, candidateYaw);
            if (Float.isNaN(candidatePitch)) continue;
            candidatePitch = RotationUtils.clampPitch(quantizeMouseRotation(candidatePitch, lastSentPitch));
            Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
            if (hit != null) { targetYaw = candidateYaw; targetPitch = candidatePitch; pendingYawJitter = 0; return hit; }
        }
        return null;
    }

    // Source 5013-5301: Breezily weave, sprint hysteresis and mode/style transitions.
    private void resetBreezilyState() { breezilyTickCounter = 0; breezilyGroundWaitRemaining = 0; breezilyUseLeft = true; breezilyLockedYaw = Float.NaN; }
    private int getBreezilyGroundWaitTicks() { return MathHelper.clamp_int((int) Math.round(breezilyGroundWaitTicks.getInput()), 1, 10); }
    private boolean canApplyBreezilyGroundStrafe(PrePlayerInputEvent input) {
        if (mc.thePlayer == null || !breezilyOnlyStrafeOnGround.isToggled()) { breezilyGroundWaitRemaining = 0; return true; }
        if (input != null && input.isJump() || !mc.thePlayer.onGround) { breezilyGroundWaitRemaining = getBreezilyGroundWaitTicks(); return false; }
        if (breezilyGroundWaitRemaining > 0) { breezilyGroundWaitRemaining--; return false; }
        return true;
    }
    private boolean isRawDiagonalInput() { return getForwardInput() != 0 && getStrafeInput() != 0; }
    private boolean hasBreezilyDirectionInput() { return getForwardInput() != 0 || getStrafeInput() != 0; }
    private float getBreezilyTravelYaw(float cameraYaw) {
        int forward = getForwardInput(), strafe = getStrafeInput();
        if (forward == 0 && strafe == 0) return !Float.isNaN(breezilyLockedYaw) ? breezilyLockedYaw - 180F : cameraYaw;
        return getMovementYaw(cameraYaw, forward, strafe);
    }
    private void updateBreezilyMovement(PrePlayerInputEvent input) {
        if (input == null || mc.thePlayer == null) return;
        if (!canApplyBreezilyGroundStrafe(input)) { breezilyTickCounter = 0; return; }
        ItemStack stack = getPlacementStack(mc.thePlayer); if (!isValidBlock(stack)) return;
        float forward = input.getForward(), strafe = input.getStrafe();
        if (Math.abs(forward) < 0.001F && Math.abs(strafe) < 0.001F) { breezilyTickCounter = 0; return; }
        double length = Math.sqrt((double) forward * forward + (double) strafe * strafe);
        float baseForward = (float) (forward / length), baseStrafe = (float) (strafe / length);
        if (!breezilyAutoStrafe.isToggled()) { input.setForward(baseForward); input.setStrafe(baseStrafe); return; }
        float weave = breezilyUseLeft ? 0.62F : -0.62F, outForward = baseForward - baseStrafe * weave, outStrafe = baseStrafe + baseForward * weave;
        float scale = Math.max(1F, Math.max(Math.abs(outForward), Math.abs(outStrafe)));
        input.setForward(outForward / scale); input.setStrafe(outStrafe / scale);
        int switchTicks = MathHelper.clamp_int((int) Math.round(breezilySwitchSpeed.getInput()), 2, 10);
        breezilyTickCounter++; if (breezilyTickCounter >= switchTicks) { breezilyTickCounter = 0; breezilyUseLeft = !breezilyUseLeft; }
    }
    private float getBreezilyLockedYaw(float cameraYaw) {
        if (hasBreezilyDirectionInput()) {
            float travelYaw = getBreezilyTravelYaw(cameraYaw), desiredServerYaw = travelYaw + 180F;
            float referenceYaw = Float.isNaN(breezilyLockedYaw) ? RotationUtils.serverRotations[0] : breezilyLockedYaw;
            breezilyLockedYaw = quantizeRotation(unwrapYaw(desiredServerYaw, referenceYaw));
        } else if (Float.isNaN(breezilyLockedYaw)) breezilyLockedYaw = quantizeRotation(unwrapYaw(cameraYaw + 180F, RotationUtils.serverRotations[0]));
        return breezilyLockedYaw;
    }
    private int aimBreezilyGroundCandidate(int candidateCount, float fixedYaw) {
        if (candidateCount <= 0) { targetPitch = quantizeRotation(78F); return -1; }
        int bestIndex = -1, bestScore = -1; float bestPitch = Float.NaN, bestDistance = Float.MAX_VALUE;
        for (int i = 0; i < candidateCount; i++) {
            Placement candidate = candidates[i]; if (candidate == null) continue;
            float candidatePitch = findNearestFacePitch(candidate, fixedYaw, 78F); if (Float.isNaN(candidatePitch)) continue;
            Vec3 hit = raycastFace(candidate, fixedYaw, candidatePitch); if (hit == null) continue;
            int score = getCandidateScore(i); float distance = Math.abs(candidatePitch - 78F);
            if (bestIndex < 0 || score > bestScore || score == bestScore && distance < bestDistance) { bestIndex = i; bestScore = score; bestPitch = candidatePitch; bestDistance = distance; }
        }
        if (bestIndex >= 0) { targetYaw = fixedYaw; targetPitch = RotationUtils.clampPitch(bestPitch); pitch = targetPitch; debugAimSelection = 8; debugAimScore = bestScore; }
        else { targetYaw = fixedYaw; targetPitch = quantizeRotation(78F); }
        return bestIndex;
    }
    private Vec3 aimBreezilyPlacement(Placement placement, float fixedYaw) {
        if (placement == null) return null;
        float candidatePitch = findNearestFacePitch(placement, fixedYaw, 78F);
        if (Float.isNaN(candidatePitch)) candidatePitch = findFacePitch(placement, fixedYaw);
        if (Float.isNaN(candidatePitch)) { targetYaw = fixedYaw; targetPitch = quantizeRotation(78F); return null; }
        targetYaw = fixedYaw; targetPitch = RotationUtils.clampPitch(candidatePitch); pitch = targetPitch;
        return raycastFace(placement, targetYaw, targetPitch);
    }
    private float getTravelYaw() {
        EntityPlayerSP player = mc.thePlayer; Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
        if (motion.x * motion.x + motion.z * motion.z > 0.0016) return (float) (Math.atan2(-motion.x, motion.z) * 180.0 / Math.PI);
        return getInputYaw();
    }
    private boolean shouldStopSprint() {
        if (alwaysSprint.isToggled() || tellyJumping || isTellyTakeoff()) { sprintStopRequested = false; return false; }
        float yawOffset = Math.abs(MathHelper.wrapAngleTo180_float(lastSentYaw - getTravelYaw()));
        if (sprintStopRequested) { if (yawOffset < SPRINT_MAX_YAW_OFFSET - 10F) sprintStopRequested = false; }
        else if (yawOffset > SPRINT_MAX_YAW_OFFSET + 5F) sprintStopRequested = true;
        return sprintStopRequested;
    }
    private void applySprintPolicy(boolean stop) {
        if (alwaysSprint.isToggled()) sprintToggleCooldown = 0;
        if (sprintToggleCooldown > 0) sprintToggleCooldown--;
        if (stop == sprintSuppressed || sprintToggleCooldown > 0) {
            if (sprintSuppressed) { mc.thePlayer.setSprinting(false); mc.gameSettings.keyBindSprint.setDown(false); }
            return;
        }
        sprintToggleCooldown = 4;
        if (stop) {
            sprintWasPressed = mc.gameSettings.keyBindSprint.isDown(); sprintSuppressed = true;
            mc.thePlayer.setSprinting(false); mc.gameSettings.keyBindSprint.setDown(false);
        } else {
            mc.gameSettings.keyBindSprint.setDown(sprintWasPressed || Utils.isBindDown(mc.gameSettings.keyBindSprint)); sprintSuppressed = false;
        }
    }
    private void handleSourceStyleTransition() {
        if (!isHypixelMode()) { lastSourceScaffoldMode = ""; return; }
        String current = getSourceScaffoldMode(); if (current.equals(lastSourceScaffoldMode)) return;
        sourceAirTicks = sourceGroundTicks = sourcePlaceDelayTicks = sourceBlocksPlaced = sourceGodBridgeJumpBlocks = sourceSneakBlocks = sourceDirectionalChange = 0;
        sourceStartY = mc.thePlayer == null ? 0 : Math.floor(mc.thePlayer.posY); hypixelPlaceTimer = 0;
        hypixelSourceYaw = getPlayerYaw() - 180F; hypixelSourcePitch = 90F; snapPlusLastMovementYaw = Float.NaN;
        resetBreezilyState(); lastSourceScaffoldMode = current;
    }
    private void handleModeTransition() {
        String currentMode = getScaffoldMode(); if (currentMode.equals(lastScaffoldMode)) return;
        tellyJumping = takeoffLatched = pendingPlacementReady = false; pendingPlacement = null; placementDelayTicks = Math.max(placementDelayTicks, 1);
        resetNinjaState(); resetBreezilyState(); resetRushState(); breezilyViewYaw = breezilyViewPitch = Float.NaN;
        snapReturnPending = snapSkipPlacementThisTick = false; snapDiagonalHoldTicks = 0; snapLastPlacementYaw = Float.NaN; hypixelPlaceTimer = 0;
        sourceAirTicks = sourceGroundTicks = sourcePlaceDelayTicks = sourceBlocksPlaced = sourceGodBridgeJumpBlocks = sourceSneakBlocks = sourceDirectionalChange = 0;
        sourceStartY = mc.thePlayer == null ? 0 : Math.floor(mc.thePlayer.posY); hopAirTicks = 0; hopLaunchRequested = false; snapPlusLastMovementYaw = Float.NaN;
        if (currentMode.equals("Snap+") || currentMode.equals("Hypixel")) { hypixelSourceYaw = getPlayerYaw() - 180F; hypixelSourcePitch = 90F; }
        else { hypixelSourceYaw = Float.NaN; hypixelSourcePitch = 90F; }
        lastScaffoldMode = currentMode;
    }

    // Source 5347-5855: mode predicates, rotation math, takeoff and the full long-Telly queue.
    private String getScaffoldMode() { return selectedMode == null || selectedMode.isEmpty() ? "God Bridge" : selectedMode; }
    private boolean isGodBridgeMode() { return getScaffoldMode().equals("God Bridge"); }
    private boolean isSnapMode() { return getScaffoldMode().equals("Snap"); }
    private boolean isSnapPlusMode() { return getScaffoldMode().equals("Snap+"); }
    private boolean isRushMode() { return getScaffoldMode().equals("Rush"); }
    private boolean isNinjaMode() { return getScaffoldMode().equals("Ninja"); }
    private boolean isBreezilyMode() { return getScaffoldMode().equals("Breezily"); }
    private boolean isHypixelMode() { return getScaffoldMode().equals("Hypixel"); }
    private boolean isHopMode() { return getScaffoldMode().equals("Hop"); }
    private String getSourceScaffoldMode() { String mode = sourceScaffoldMode.getSelectedOption(); return mode == null || mode.isEmpty() ? "Hypixel" : mode; }
    private String getHopMode() { String mode = hopMode.getSelectedOption(); return mode == null || mode.isEmpty() ? "Sprint" : mode; }
    private boolean isTellyModeSelected() { String mode = getScaffoldMode(); return mode.equals("Telly Short") || mode.equals("Telly Long"); }
    private String getTellyMode() {
        String mode = getScaffoldMode(); if (!isTellyModeSelected() || tellyOnJump.isToggled() && !isJumpPressed()) return "Disabled";
        return mode.equals("Telly Long") ? "Long" : "Short";
    }
    private String getKeepYMode() { String mode = ScaffoldModeController.keepY.get(); return mode.equals("Off") ? "Disabled" : mode; }
    private boolean isIceSelectionEnabled() { return iceThreshold.getInput() > -0.595; }
    private boolean isKeepYActive() { return !getKeepYMode().equals("Disabled") && (!keepYOnRightClick.isToggled() || keybinds.isMouseDown(1)); }
    private String getActiveKeepYMode() { return isKeepYActive() ? getKeepYMode() : "Disabled"; }
    private float randomFloat(float minimum, float maximum) { return (float) (minimum + Math.random() * (maximum - minimum)); }
    private float getPlayerYaw() { return mc.thePlayer.rotationYaw; }
    private float getServerYaw() { Float serverYaw = rotations.getServerYaw(); return serverYaw == null ? getPlayerYaw() : serverYaw; }
    private float quantizeMouseRotation(float value, float reference) {
        float delta = value - reference, snapped = Math.round(delta / MOUSE_ROTATION_INCREMENT) * MOUSE_ROTATION_INCREMENT; return reference + snapped;
    }
    private float unwrapYaw(float angle, float target) { return target + MathHelper.wrapAngleTo180_float(angle - target); }
    private float clampRotationDelta(float delta, float limit) { limit = MathHelper.clamp_float(limit, 0F, 180F); return MathHelper.clamp_float(delta, -limit, limit); }
    private float quantizeRotation(float angle) { return (float) ((double) angle - (double) angle % ROTATION_INCREMENT); }
    private float jitter(float maximumDegrees) {
        float offset = quantizeRotation(randomFloat(-maximumDegrees, maximumDegrees));
        if (offset == 0) offset = randomFloat(0, 1) < 0.5F ? (float) ROTATION_INCREMENT : (float) -ROTATION_INCREMENT;
        return offset;
    }
    private void updateYawJitter() { yawJitterAmplitude += randomFloat(-0.055F, 0.055F); yawJitterAmplitude = MathHelper.clamp_float(yawJitterAmplitude, MIN_YAW_JITTER, MAX_YAW_JITTER); }
    private float nextYawJitter() { return jitter(yawJitterAmplitude); }
    private float rotationEaseScale(float distance) {
        float influence = MathHelper.clamp_float((distance - 1F) / 5F, 0F, 1F); return 1F + (rotationEaseRate - 1F) * influence;
    }
    private float[] easeRotations(float fromYaw, float fromPitch, float toYaw, float toPitch) {
        float yawDelta = MathHelper.wrapAngleTo180_float(toYaw - fromYaw), pitchDelta = RotationUtils.clampPitch(toPitch) - fromPitch;
        float distance = (float) Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
        if (distance <= 1F) { rotationEaseSlowdown = 0; return new float[]{toYaw, RotationUtils.clampPitch(toPitch)}; }
        float speedLimit;
        if (!rotationEaseStarted) { speedLimit = 73F; rotationEaseStarted = true; rotationEaseSlowdown = 0; }
        else { speedLimit = 43F - rotationEaseSlowdown; if (rotationEaseSlowdown < 10F) rotationEaseSlowdown += randomFloat(3F, 4F); }
        speedLimit -= randomFloat(0.1F, 1F); speedLimit *= rotationEaseScale(distance);
        float step = Math.min(speedLimit, distance), scale = step / distance;
        return new float[]{fromYaw + yawDelta * scale, RotationUtils.clampPitch(fromPitch + pitchDelta * scale)};
    }
    private float smooth(float angle, float smoothingFactor) {
        float randomizedFactor = MathHelper.clamp_float(smoothingFactor + randomFloat(-0.1F, 0.1F), 0F, 1F);
        return angle * (0.5F + 0.5F * (1F - randomizedFactor));
    }
    private float[] getRotationsToOffset(double offsetX, double offsetY, double offsetZ, float currentYaw, float currentPitch) {
        double horizontalDistance = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        float yawDelta = MathHelper.wrapAngleTo180_float((float) (Math.atan2(offsetZ, offsetX) * 180.0 / Math.PI) - 90F - currentYaw);
        float pitchDelta = MathHelper.wrapAngleTo180_float((float) (-Math.atan2(offsetY, horizontalDistance) * 180.0 / Math.PI) - currentPitch);
        yawDelta = Math.abs(yawDelta) <= 1F ? 0F : smooth(clampRotationDelta(yawDelta, 180F), 0F);
        pitchDelta = Math.abs(pitchDelta) <= 1F ? 0F : smooth(clampRotationDelta(pitchDelta, 180F), 0F);
        return new float[]{quantizeRotation(currentYaw + yawDelta), quantizeRotation(currentPitch + pitchDelta)};
    }
    private float getMovementYaw(float movementYaw, float forward, float strafe) {
        if (forward < 0) movementYaw += 180F;
        if (strafe != 0) { float strafeScale = forward == 0 ? 1F : 0.5F * Math.signum(forward); movementYaw += -90F * strafeScale * Math.signum(strafe); }
        return movementYaw;
    }
    private int getForwardInput() {
        int forward = 0; if (Utils.isBindDown(mc.gameSettings.keyBindForward)) forward++; if (Utils.isBindDown(mc.gameSettings.keyBindBack)) forward--; return forward;
    }
    private int getStrafeInput() {
        int strafe = 0; if (Utils.isBindDown(mc.gameSettings.keyBindLeft)) strafe++; if (Utils.isBindDown(mc.gameSettings.keyBindRight)) strafe--; return strafe;
    }
    private boolean isAnyMovementKeyDown() {
        return Utils.isBindDown(mc.gameSettings.keyBindForward) || Utils.isBindDown(mc.gameSettings.keyBindBack) || Utils.isBindDown(mc.gameSettings.keyBindLeft) || Utils.isBindDown(mc.gameSettings.keyBindRight);
    }
    private boolean isMoving() {
        boolean forward = Utils.isBindDown(mc.gameSettings.keyBindForward), backward = Utils.isBindDown(mc.gameSettings.keyBindBack);
        boolean left = Utils.isBindDown(mc.gameSettings.keyBindLeft), right = Utils.isBindDown(mc.gameSettings.keyBindRight);
        return forward != backward || left != right;
    }
    private boolean isJumpPressed() { return Utils.isBindDown(mc.gameSettings.keyBindJump); }
    private int updateCardinalSide(float yaw) {
        float quadrantAngle = ((yaw + 180F) % 90F + 90F) % 90F;
        if (quadrantAngle > 8F && quadrantAngle < 37F) cardinalSide = 1; else if (quadrantAngle > 53F && quadrantAngle < 82F) cardinalSide = -1;
        return cardinalSide;
    }
    private boolean isDiagonalMovement(float yaw) {
        float quadrantAngle = Math.abs(yaw % 90F);
        if (diagonalMovement) { if (quadrantAngle < 16F || quadrantAngle > 74F) diagonalMovement = false; }
        else if (quadrantAngle > 24F && quadrantAngle < 66F) diagonalMovement = true;
        return diagonalMovement;
    }
    private float getInputYaw() { return MathHelper.wrapAngleTo180_float(getMovementYaw(getPlayerYaw(), getForwardInput(), getStrafeInput())); }
    private boolean isTellyTakeoff() {
        if (mc.thePlayer.onGround && isMoving() && !hasCeilingAbove()) {
            String keepYMode = getActiveKeepYMode(); boolean keepYEnabled = keepYMode.equals("Telly A") || keepYMode.equals("Telly B");
            boolean tellyEnabled = getTellyMode().equals("Short") || isLongTellyEnabled();
            return keepYEnabled && keepYStage > 0 || tellyEnabled && isJumpPressed();
        }
        return false;
    }
    private boolean isManualTellyTakeoff() {
        return mc.thePlayer.onGround && isMoving() && !hasCeilingAbove() && isJumpPressed() && (getTellyMode().equals("Short") || isLongTellyEnabled());
    }
    private boolean isLongTellyEnabled() { return getTellyMode().equals("Long"); }
    private boolean isShortDiagonalJump() {
        double speedX = Math.abs(mc.thePlayer.motionX), speedZ = Math.abs(mc.thePlayer.motionZ);
        return !mc.thePlayer.onGround && getTellyMode().equals("Short") && speedX * speedX + speedZ * speedZ > 0.0016 && Math.min(speedX, speedZ) > Math.max(speedX, speedZ) * 0.25;
    }
    private boolean isLookLocked() { return longTellyMissTicks < LOOK_LOCK_MISS_LIMIT; }
    private EnumFacing faceFromDelta(int dx, int dz) {
        if (dx > 0) return EnumFacing.EAST; if (dx < 0) return EnumFacing.WEST; if (dz > 0) return EnumFacing.SOUTH; return EnumFacing.NORTH;
    }
    private boolean intersectsPlayer(Vec3 position, int cx, int cy, int cz) {
        return cx + 1.0 > position.x - 0.3 && cx < position.x + 0.3 && cy + 1.0 > position.y && cy < position.y + 1.8 && cz + 1.0 > position.z - 0.3 && cz < position.z + 0.3;
    }
    private Placement findQueueSupport(int supportIndex, boolean upFirst, int cx, int cy, int cz) {
        int supportX, supportZ; EnumFacing supportFace;
        int directionIndex = upFirst ? (supportIndex == 0 ? 4 : supportIndex - 1) : supportIndex, supportY = cy;
        if (directionIndex == 0) { supportX = cx - longTellyForwardX; supportZ = cz - longTellyForwardZ; supportFace = faceFromDelta(longTellyForwardX, longTellyForwardZ); }
        else if (directionIndex == 1) { supportX = cx - longTellyLateralX; supportZ = cz - longTellyLateralZ; supportFace = faceFromDelta(longTellyLateralX, longTellyLateralZ); }
        else if (directionIndex == 2) { supportX = cx + longTellyLateralX; supportZ = cz + longTellyLateralZ; supportFace = faceFromDelta(-longTellyLateralX, -longTellyLateralZ); }
        else if (directionIndex == 3) { supportX = cx + longTellyForwardX; supportZ = cz + longTellyForwardZ; supportFace = faceFromDelta(-longTellyForwardX, -longTellyForwardZ); }
        else { supportX = cx; supportY = cy - 1; supportZ = cz; supportFace = EnumFacing.UP; }
        return isSolidSupport(supportX, supportY, supportZ) && !isInteractableAt(supportX, supportY, supportZ) ? new Placement(new BlockPos(supportX, supportY, supportZ), supportFace) : null;
    }
    private void buildLongTellyQueue(Vec3 motion) {
        resetLongTellyQueue(); if (ticksSinceGrounded > 3) return;
        Vec3 position = mc.thePlayer.getPositionVector(); longTellyRowY = MathHelper.floor_double(position.y) - 1;
        double speedX = Math.abs(motion.x), speedZ = Math.abs(motion.z); if (speedX * speedX + speedZ * speedZ < 0.01) return;
        int directionX = 0, directionZ = 0;
        if (speedX >= speedZ * 2.4) directionX = motion.x > 0 ? 1 : -1; else if (speedZ >= speedX * 2.4) directionZ = motion.z > 0 ? 1 : -1; else return;
        if (longTellySprintJump && longTellyNeedsSideUpdate) {
            longTellyNeedsSideUpdate = false;
            if (hasLongTellyOrigin) {
                int travelledX = lastGroundX - longTellyOriginX, travelledZ = lastGroundZ - longTellyOriginZ;
                if (travelledX * longTellyLateralX + travelledZ * longTellyLateralZ >= 1) longTellySide = -longTellySide;
            } else {
                double lean = motion.x * -directionZ + motion.z * directionX; if (lean > 0.02) longTellySide = 1; else if (lean < -0.02) longTellySide = -1;
            }
            longTellyOriginX = lastGroundX; longTellyOriginZ = lastGroundZ; hasLongTellyOrigin = true;
        }
        longTellyForwardX = directionX; longTellyForwardZ = directionZ; longTellyLateralX = -directionZ * longTellySide; longTellyLateralZ = directionX * longTellySide;
        for (int i = 0; i < 3; i++) {
            int forwardOffset = longTellySprintJump ? i + 1 : i;
            queuedBlocks[i] = new BlockPos(lastGroundX + forwardOffset * directionX, longTellyRowY + (longTellySprintJump ? 0 : 1), lastGroundZ + forwardOffset * directionZ);
        }
        queuedBlockCount = 3;
    }
    private boolean findLongTellyPlacement() {
        EntityPlayerSP player = mc.thePlayer; Vec3 position = player.getPositionVector(); float cameraYaw = yawGridBase;
        queueCursor = 0; while (queueCursor < queuedBlockCount && !isReplaceableAt(queuedBlocks[queueCursor])) queueCursor++;
        if (pendingQueueIndex >= 0 && (pendingQueueIndex < queueCursor || pendingQueueIndex >= queuedBlockCount)) { pendingQueueIndex = -1; queueStallTicks = 0; }
        for (int queueIndex = queueCursor; queueIndex < queuedBlockCount; queueIndex++) {
            if ((skippedQueueMask & 1 << queueIndex) != 0) continue;
            BlockPos queuedBlock = queuedBlocks[queueIndex]; int cx = queuedBlock.getX(), cy = queuedBlock.getY(), cz = queuedBlock.getZ();
            if (!isReplaceableAt(queuedBlock)) { if (queueIndex == pendingQueueIndex) pendingQueueIndex = -1; continue; }
            double distanceX = cx + 0.5 - position.x, distanceZ = cz + 0.5 - position.z;
            if (distanceX * distanceX + distanceZ * distanceZ > 18.0 || intersectsPlayer(position, cx, cy, cz)) continue;
            int firstPass = queueIndex == pendingQueueIndex ? 0 : 1;
            boolean upFirst = cy > longTellyRowY || !player.onGround && player.motionY > 0;
            for (int pass = firstPass; pass < 2; pass++) for (int supportIndex = 0; supportIndex < 5; supportIndex++) {
                Placement support = findQueueSupport(supportIndex, upFirst, cx, cy, cz); if (support == null) continue;
                float candidateYaw, candidatePitch;
                if (pass == 0) { candidateYaw = longTellyAimYaw; candidatePitch = findNearestFacePitch(support, candidateYaw, lastSentPitch); }
                else {
                    float[] candidateYaws = getLongTellyCandidateYaws(support, position, cameraYaw); candidateYaw = Float.NaN; candidatePitch = Float.NaN;
                    for (float baseYaw : candidateYaws) {
                        float jitteredYaw = baseYaw + nextYawJitter(), facePitch = findNearestFacePitch(support, jitteredYaw, lastSentPitch);
                        if (!Float.isNaN(facePitch)) { candidateYaw = jitteredYaw; candidatePitch = facePitch; break; }
                    }
                }
                if (Float.isNaN(candidatePitch)) continue;
                if (queueIndex != pendingQueueIndex) queueStallTicks = 0;
                pendingQueueIndex = queueIndex; pendingPlacement = support; longTellyAimYaw = candidateYaw; longTellyAimPitch = candidatePitch;
                longTellyYawBucket = Math.round(MathHelper.wrapAngleTo180_float(candidateYaw - cameraYaw) / 45F); return true;
            }
        }
        return false;
    }
    private float[] getLongTellyCandidateYaws(Placement placement, Vec3 position, float cameraYaw) {
        double faceCenterX = placement.support.getX() + 0.5 + placement.face.getFrontOffsetX() * 0.5;
        double faceCenterZ = placement.support.getZ() + 0.5 + placement.face.getFrontOffsetZ() * 0.5;
        float faceYaw = (float) Math.toDegrees(Math.atan2(-(faceCenterX - position.x), faceCenterZ - position.z));
        int faceBucket = Math.round(MathHelper.wrapAngleTo180_float(faceYaw - cameraYaw) / 45F); float heldYaw = cameraYaw + longTellyYawBucket * 45F;
        float[] candidateYaws = {heldYaw, cameraYaw + faceBucket * 45F, cameraYaw + (faceBucket - 1) * 45F, cameraYaw + (faceBucket + 1) * 45F, faceYaw};
        float[] yawScores = new float[candidateYaws.length];
        float backYaw = (float) Math.toDegrees(Math.atan2(longTellyForwardX, -longTellyForwardZ)) + (lookingStraightBack ? 0F : LOOK_DIAGONAL_BUCKETS * lookSide * 45F);
        for (int first = 0; first < candidateYaws.length; first++) {
            yawScores[first] = isLookLocked() ? Math.abs(MathHelper.wrapAngleTo180_float(candidateYaws[first] - backYaw))
                    + 0.05F * Math.abs(MathHelper.wrapAngleTo180_float(candidateYaws[first] - heldYaw)) : Math.abs(MathHelper.wrapAngleTo180_float(candidateYaws[first] - heldYaw));
        }
        for (int first = 0; first < candidateYaws.length; first++) for (int second = first + 1; second < candidateYaws.length; second++) {
            if (yawScores[second] < yawScores[first]) {
                float previousYaw = candidateYaws[first]; candidateYaws[first] = candidateYaws[second]; candidateYaws[second] = previousYaw;
                float previousScore = yawScores[first]; yawScores[first] = yawScores[second]; yawScores[second] = previousScore;
            }
        }
        return candidateYaws;
    }

    // Source 5857-6280: source target search, face selection and verified placement.
    private boolean hasCeilingAbove() {
        Vec3 position = mc.thePlayer.getPositionVector(); double minY = position.y + 1.0, maxY = position.y + 1.0 + 1.8;
        int y0 = MathHelper.floor_double(minY), y1 = MathHelper.floor_double(maxY - 1.0E-6);
        for (int y = y0; y <= y1; y++) if (hasCornerSupport(position.x, position.z, y)) return true;
        return false;
    }
    private boolean hasJumpPotion() { return nativeMc.player != null && nativeMc.player.hasEffect(MobEffects.JUMP_BOOST); }
    private boolean isReplaceable(IBlockState state) {
        if (state == null || state.block == null || state.block.name == null) return true;
        String name = state.block.name.toLowerCase(java.util.Locale.ROOT);
        if (name.equals("snow_layer")) return state.block.variant <= 0;
        return REPLACEABLE_BLOCK_NAMES.contains("," + name + ",");
    }
    private boolean isReplaceableAt(int x, int y, int z) { return isReplaceableAt(new BlockPos(x, y, z)); }
    private boolean isReplaceableAt(BlockPos position) { return isReplaceable(BlockUtils.getBlockState(position)); }
    private boolean isSolidSupport(int x, int y, int z) { return !isReplaceableAt(x, y, z); }
    private boolean isInteractableAt(int x, int y, int z) { return BlockUtils.isInteractable(BlockUtils.getBlock(x, y, z)); }
    private Placement findPlacement() {
        debugMissingPlacementReason = 'n'; EntityPlayerSP player = mc.thePlayer; Vec3 position = player.getPositionVector();
        boolean shortDiagonal = isShortDiagonalJump(); BlockPos target = findPlacementTarget(player, position, shortDiagonal);
        return target == null ? null : findClosestPlacement(target, position, shortDiagonal);
    }
    private BlockPos findPlacementTarget(EntityPlayerSP player, Vec3 position, boolean shortDiagonal) {
        int playerBlockX = MathHelper.floor_double(position.x), playerBlockY = MathHelper.floor_double(position.y), playerBlockZ = MathHelper.floor_double(position.z);
        int targetY = (keepYStage != 0 && !useCurrentHeight ? Math.min(playerBlockY, keepYStartHeight) : playerBlockY) - 1;
        int targetX = playerBlockX, targetZ = playerBlockZ;
        if (shortDiagonal) {
            if (!hasPrediction) buildPrediction();
            int ahead = 1; while (ahead < 3 && predictedPositions[ahead].y > targetY + 1.0) ahead++;
            if (hasCornerSupport(predictedPositions[ahead].x, predictedPositions[ahead].z, targetY)) { debugMissingPlacementReason = 'u'; return null; }
            targetX = MathHelper.floor_double(predictedPositions[ahead].x); targetZ = MathHelper.floor_double(predictedPositions[ahead].z);
        } else if (!isReplaceableAt(targetX, targetY, targetZ)) {
            if (!player.onGround && !(isKeepYActive() && keepYStage > 0)) { debugMissingPlacementReason = 'u'; return null; }
            Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ); debugMissingPlacementReason = 'r';
            double horizontalSpeed = Utils.getHorizontalSpeed(player); if (horizontalSpeed < 0.01) return null;
            double directionX = motion.x / horizontalSpeed, directionZ = motion.z / horizontalSpeed;
            int previousX = playerBlockX, previousZ = playerBlockZ; boolean foundGap = false;
            for (double distance = 0.25; distance <= 2.0 && !foundGap; distance += 0.25) {
                int nextX = MathHelper.floor_double(position.x + directionX * distance), nextZ = MathHelper.floor_double(position.z + directionZ * distance);
                if (nextX == previousX && nextZ == previousZ) continue;
                nextX = previousX + MathHelper.clamp_int(nextX - previousX, -1, 1); nextZ = previousZ + MathHelper.clamp_int(nextZ - previousZ, -1, 1);
                if (nextX != previousX && nextZ != previousZ && !isSolidSupport(previousX, targetY, nextZ) && !isSolidSupport(nextX, targetY, previousZ)) {
                    if (isReplaceableAt(previousX, targetY, nextZ)) { targetX = previousX; targetZ = nextZ; }
                    else if (isReplaceableAt(nextX, targetY, previousZ)) { targetX = nextX; targetZ = previousZ; }
                    else return null;
                    foundGap = true;
                } else if (isReplaceableAt(nextX, targetY, nextZ)) { targetX = nextX; targetZ = nextZ; foundGap = true; }
                else if (isSolidSupport(nextX, targetY, nextZ)) { previousX = nextX; previousZ = nextZ; }
                else return null;
            }
            if (!foundGap) return null;
        }
        return new BlockPos(targetX, targetY, targetZ);
    }
    private Placement findClosestPlacement(BlockPos target, Vec3 position, boolean shortDiagonal) {
        int targetX = target.getX(), targetY = target.getY(), targetZ = target.getZ(); double reachSquared = 4.5 * 4.5;
        Placement bestPlacement = null; double bestDistance = 0;
        for (int dx = -4; dx <= 4; dx++) for (int dy = -4; dy <= 0; dy++) for (int dz = -4; dz <= 4; dz++) {
            int supportX = targetX + dx, supportY = targetY + dy, supportZ = targetZ + dz;
            if (!(keepYStage == 0 || useCurrentHeight || supportY < keepYStartHeight)) continue;
            double playerOffsetX = supportX + 0.5 - position.x, playerOffsetY = supportY + 0.5 - position.y, playerOffsetZ = supportZ + 0.5 - position.z;
            if (playerOffsetX * playerOffsetX + playerOffsetY * playerOffsetY + playerOffsetZ * playerOffsetZ > reachSquared) continue;
            double targetOffsetX = (double) supportX - targetX, targetOffsetY = (double) supportY - targetY, targetOffsetZ = (double) supportZ - targetZ;
            double targetDistanceSquared = targetOffsetX * targetOffsetX + targetOffsetY * targetOffsetY + targetOffsetZ * targetOffsetZ;
            if (!shortDiagonal && bestPlacement != null && targetDistanceSquared >= bestDistance) continue;
            BlockPos support = new BlockPos(supportX, supportY, supportZ); IBlockState supportState = BlockUtils.getBlockState(support);
            if (isReplaceable(supportState) || BlockUtils.isInteractable(supportState.getBlock())) continue;
            EnumFacing side = findBestPlacementFace(supportX, supportY, supportZ, targetX, targetY, targetZ); if (side == null) continue;
            if (shortDiagonal) {
                if (intersectsPlayer(position, supportX + side.getFrontOffsetX(), supportY + side.getFrontOffsetY(), supportZ + side.getFrontOffsetZ())) continue;
                targetOffsetX += side.getFrontOffsetX(); targetOffsetY += side.getFrontOffsetY(); targetOffsetZ += side.getFrontOffsetZ();
                targetDistanceSquared = targetOffsetX * targetOffsetX + targetOffsetY * targetOffsetY + targetOffsetZ * targetOffsetZ;
            }
            if (bestPlacement == null || targetDistanceSquared < bestDistance) { bestDistance = targetDistanceSquared; bestPlacement = new Placement(support, side); }
        }
        if (bestPlacement == null) { debugMissingPlacementReason = 'n'; return null; }
        return bestPlacement;
    }
    private EnumFacing findBestPlacementFace(int supportX, int supportY, int supportZ, int targetX, int targetY, int targetZ) {
        EnumFacing bestFace = null; double bestDistance = 0;
        for (EnumFacing face : PLACEMENT_FACES) {
            int blockX = supportX + face.getFrontOffsetX(), blockY = supportY + face.getFrontOffsetY(), blockZ = supportZ + face.getFrontOffsetZ();
            if (blockY <= targetY && isReplaceableAt(blockX, blockY, blockZ) && (!(isKeepYActive() && keepYStage > 0) || blockY == targetY)) {
                double offsetX = (double) blockX - targetX, offsetY = (double) blockY - targetY, offsetZ = (double) blockZ - targetZ;
                double distanceSquared = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;
                if (bestFace == null || distanceSquared < bestDistance || distanceSquared == bestDistance && face == EnumFacing.UP) { bestDistance = distanceSquared; bestFace = face; }
            }
        }
        return bestFace;
    }
    private Vec3 raycastFace(Placement placement, float candidateYaw, float candidatePitch) {
        MovingObjectPosition hit = RotationUtils.rayCastBlock(4.5, candidateYaw, candidatePitch); return placement.matches(hit) ? hit.hitVec : null;
    }
    private boolean hasStableFaceAim(Placement placement) {
        return raycastFace(placement, lastBaseYaw, lastBasePitch) != null && raycastFace(placement, lastBaseYaw + 0.5F, lastBasePitch) != null
                && raycastFace(placement, lastBaseYaw - 0.5F, lastBasePitch) != null
                && raycastFace(placement, lastBaseYaw, RotationUtils.clampPitch(lastBasePitch - 0.35F)) != null
                && raycastFace(placement, lastBaseYaw, RotationUtils.clampPitch(lastBasePitch + 0.35F)) != null;
    }
    private Vec3 aimShortDiagonalFace(Placement placement) {
        EntityPlayerSP player = mc.thePlayer;
        double dx = placement.support.getX() + 0.5 + placement.face.getFrontOffsetX() * 0.5 - player.posX;
        double dz = placement.support.getZ() + 0.5 + placement.face.getFrontOffsetZ() * 0.5 - player.posZ;
        float centerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz)); float[] yaws = {targetYaw, lastSentYaw, centerYaw};
        for (float desiredYaw : yaws) {
            float candidateYaw = quantizeMouseRotation(lastSentYaw + MathHelper.wrapAngleTo180_float(desiredYaw - lastSentYaw), lastSentYaw);
            for (int pass = 0; pass < 2; pass++) {
                float candidatePitch = pass == 0 ? findNearestFacePitch(placement, candidateYaw, targetPitch) : findFacePitch(placement, candidateYaw);
                if (Float.isNaN(candidatePitch)) continue;
                candidatePitch = RotationUtils.clampPitch(quantizeMouseRotation(candidatePitch, lastSentPitch));
                Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
                if (hit != null) { aimYaw(candidateYaw); aimPitch(candidatePitch); lastYawBucket = Math.round(MathHelper.wrapAngleTo180_float(candidateYaw - yawGridBase) / 45F); return hit; }
            }
        }
        return null;
    }
    private boolean placeKeepYFromRay() {
        MovingObjectPosition hit = RotationUtils.rayCastBlock(4.5, lastSentYaw, lastSentPitch); if (hit == null || hit.sideHit == EnumFacing.DOWN) return false;
        EntityPlayerSP player = mc.thePlayer; BlockPos support = hit.getBlockPos(), target = BlockUtils.offsetPos(hit);
        int floorY = MathHelper.floor_double(player.posY), placeY = (useCurrentHeight ? floorY : Math.min(floorY, keepYStartHeight)) - 1;
        if (target.getY() != placeY || !isSolidSupport(support.getX(), support.getY(), support.getZ())
                || isInteractableAt(support.getX(), support.getY(), support.getZ()) || !isReplaceableAt(target.getX(), target.getY(), target.getZ())) return false;
        double dx = target.getX() + 0.5 - player.posX, dz = target.getZ() + 0.5 - player.posZ, leadX = player.motionX * 2.0, leadZ = player.motionZ * 2.0;
        double leadSq = leadX * leadX + leadZ * leadZ, along = leadSq > 0.0001 ? MathHelper.clamp_double((dx * leadX + dz * leadZ) / leadSq, 0, 1) : 0;
        double offsetX = dx - leadX * along, offsetZ = dz - leadZ * along;
        if (offsetX * offsetX + offsetZ * offsetZ > 1.0) return false;
        return placeBlock(new Placement(support, hit.sideHit)) && !isReplaceableAt(target.getX(), target.getY(), target.getZ());
    }
    private int getPlacementLimit() {
        int base = MathHelper.clamp_int((int) multiplace.getInput(), 1, 3);
        if (isRushMode()) return Math.max(base, MathHelper.clamp_int((int) Math.round(rushChain.getInput()), 1, 3));
        return base;
    }
    private boolean placeBlock(Placement placement) {
        if (placementAttemptsThisTick >= getPlacementLimit()) return false;
        EnumFacing side = placement.face; EntityPlayerSP player = mc.thePlayer;
        if (!Utils.nullCheck() || side == null) return false;
        BlockPos support = placement.support, target = placement.getTarget(); IBlockState supportState = BlockUtils.getBlockState(support);
        if (isReplaceable(supportState) || BlockUtils.isInteractable(supportState.getBlock()) || !isReplaceable(BlockUtils.getBlockState(target))) return false;
        Vec3 exactHit = raycastFace(placement, lastSentYaw, lastSentPitch); if (exactHit == null || !selectPlacementStack(player)) return false;
        ItemStack heldStack = getPlacementStack(player); if (!BlockUtils.canPlaceBlockOnSide(heldStack, support, side)) return false;
        Vec3 clickHit = isSnapPlusMode() || isHypixelMode() ? getHypixelSourceHitVec(placement, exactHit) : exactHit;
        boolean placed; int visibleSlot = player.inventory.currentItem;
        boolean temporarySpoof = isSpoofItemEnabled() && hasSwapped && placementSlot >= 0 && placementSlot <= 8 && placementSlot != visibleSlot;
        placementAttemptsThisTick++; sendingPlacement = true;
        if (temporarySpoof) {
            setSpoofServerSlot(placementSlot); inventory.setSlot(placementSlot); player.inventory.currentItem = placementSlot;
            heldStack = player.inventory.getStackInSlot(placementSlot);
        }
        try {
            placed = nativeMc.gameMode.useItemOn(nativeMc.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(clickHit, side.nativeFace, support.nativePos(), false)).consumesAction();
        } finally {
            if (temporarySpoof) { inventory.setSlot(visibleSlot); player.inventory.currentItem = visibleSlot; physicalSlotSwapped = false; originalSlot = visibleSlot; }
            sendingPlacement = false;
        }
        placed = placed && BlockUtils.check(target, null); heldBlockCount = getBlockCount(getPlacementStack(player));
        if (placed) {
            placedThisTick = true;
            if (towerWasActive && side == EnumFacing.UP) { towerLastPlacedY = Math.max(towerLastPlacedY, target.getY()); tower.onPlaced(target.nativePos()); }
            if (isSnapPlusMode()) hypixelPlaceTimer = 10;
            if (isHypixelMode()) {
                if (getSourceScaffoldMode().equals("Hypixel")) hypixelPlaceTimer = 10;
                sourcePlaceDelayTicks = getYuriPlaceDelayTicks(); sourceBlocksPlaced++; sourceGodBridgeJumpBlocks++; sourceSneakBlocks++;
            }
            if (rushPlannedPlacement != null && target.equals(rushPlannedPlacement.getTarget())) {
                if (rushPlannedPlacement.face == EnumFacing.UP) { towerLastPlacedY = target.getY(); tower.onPlannedUpPlacement(target.nativePos()); }
                else rushLastPlacedTarget = target;
                rushPlannedPlacement = null; rushMissStreak = 0;
            }
            if (isSnapRotationActive()) { snapReturnPending = true; snapLastPlacementYaw = lastSentYaw; if (isRawDiagonalInput()) snapDiagonalHoldTicks = 2; }
            lastPlacementHit = clickHit; lastPlacementTime = client.time(); longTellyMissTicks = 0;
            debugPlacementPitchDelta = rotationSentLastTick ? lastSentPitch - previousSentPitch : 0;
            if (Math.abs(debugPlacementPitchDelta) > Math.abs(debugMaxPlacementPitchDelta)) debugMaxPlacementPitchDelta = debugPlacementPitchDelta;
            if (!silentSwing.isToggled()) mc.thePlayer.swingItem(); else PlayerUtil.INSTANCE.sendSwingPacket(InteractionHand.MAIN_HAND);
            placementObserver.accept(target.nativePos());
        }
        return placed;
    }
    private boolean isUnsupportedAt(double px, double pz) {
        EntityPlayerSP player = mc.thePlayer; int y = MathHelper.floor_double(player.getPositionVector().y) - 1; return !hasCornerSupport(px, pz, y);
    }
    private boolean hasCornerSupport(double x, double z, int y) {
        return isSolidSupport(MathHelper.floor_double(x - 0.3), y, MathHelper.floor_double(z - 0.3))
                || isSolidSupport(MathHelper.floor_double(x + 0.3), y, MathHelper.floor_double(z - 0.3))
                || isSolidSupport(MathHelper.floor_double(x - 0.3), y, MathHelper.floor_double(z + 0.3))
                || isSolidSupport(MathHelper.floor_double(x + 0.3), y, MathHelper.floor_double(z + 0.3));
    }
    private static final class Placement {
        private final BlockPos support;
        private final EnumFacing face;
        private Placement(BlockPos support, EnumFacing face) { this.support = support; this.face = face; }
        private BlockPos getTarget() { return support.offset(face); }
        private boolean matches(MovingObjectPosition hit) { return hit != null && support.equals(hit.getBlockPos()) && face == hit.sideHit; }
    }
    private static final class FaceGeometry {
        private final boolean topFace;
        private final double nearDistance, farDistance, eyeY, surfaceY;
        private FaceGeometry(boolean topFace, double nearDistance, double farDistance, double eyeY, double surfaceY) {
            this.topFace = topFace; this.nearDistance = nearDistance; this.farDistance = farDistance; this.eyeY = eyeY; this.surfaceY = surfaceY;
        }
    }
}
