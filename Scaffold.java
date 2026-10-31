final String REPLACEABLE_BLOCK_NAMES = ",air,water,flowing_water,lava,flowing_lava,fire,tallgrass,deadbush,snow_layer,double_plant,vine,sapling,yellow_flower,red_flower,brown_mushroom,red_mushroom,wheat,carrots,potatoes,nether_wart,reeds,";
final String UNPLACEABLE_BLOCK_NAMES = ",snow_layer,daylight_detector,beacon,end_portal_frame,end_portal,lever,double_plant,waterlily,tripwire_hook,tallgrass,ladder,gravel,clay,sand,soul_sand,chest,trapped_chest,ender_chest,furnace,lit_furnace,jukebox,enchanting_table,dropper,dispenser,hopper,anvil,noteblock,crafting_table,mob_spawner,brewing_stand,bed,";
final String[] UNPLACEABLE_BLOCK_CONTAINS = new String[0];
final String UNPLACEABLE_BLOCK_TYPES = ",BlockSnow,BlockWeb,BlockSapling,BlockDaylightDetector,BlockBeacon,BlockBanner,BlockEndPortalFrame,BlockEndPortal,BlockLever,BlockButton,BlockSkull,BlockLiquid,BlockCactus,BlockDoublePlant,BlockLilyPad,BlockCarpet,BlockTripWire,BlockTripWireHook,BlockTallGrass,BlockFlower,BlockFlowerPot,BlockSign,BlockLadder,BlockTorch,BlockRedstoneTorch,BlockStairs,BlockSlab,BlockFence,BlockPane,BlockStainedGlassPane,BlockGravel,BlockClay,BlockSand,BlockSoulSand,BlockRailBase,BlockFenceGate,BlockBasePressurePlate,BlockAnvil,BlockEnchantmentTable,BlockChest,BlockTrapDoor,BlockDoor,BlockJukebox,BlockEnderChest,BlockBrewingStand,BlockBed,BlockDropper,BlockDispenser,BlockHopper,BlockNote,BlockWorkbench,BlockFurnace,BlockMobSpawner,";

boolean closetModule = true;
boolean editPositionMode = false;
boolean editDragging = false;
boolean editLeftWasDown = false, editRightWasDown = false;
int editLastMouseX = 0, editLastMouseY = 0;
float editLastX = 0.0f, editLastY = 0.0f;

final MCAdapter mc = new MCAdapter();
void registerSetting(Object ignored) { }

    private static final String[] MODE_OPTIONS = {"God Bridge", "Telly Short", "Telly Long", "Snap", "Rush", "Ninja", "Breezily", "Snap+", "Hypixel", "Hop"};

    private static final String[] TOWER_OPTIONS = {"Vanilla", "Sync", "Hypixel", "Apex", "Sync Burst", "Sync Early Burst", "Raven 1 Tick", "Raven Prediction", "OnGround", "OnGround Early", "OnGround Pulse", "OnGround Prediction", "Yuri NCP", "Raven Pull Sync", "Raven Pull", "Raven Predict Early", "Raven Predict Turbo", "Raven Prediction Swift", "Raven Prediction Rapid", "Polar", "Intave"};
    private static final String[] KEEP_Y_OPTIONS = {"Telly A", "Telly B"};
    private static final String[] DEBUG_OPTIONS = {"None", "Basic", "Verbose", "Visual"};
    private static final String[] SOURCE_SCAFFOLD_OPTIONS = {"Normal", "Telly", "Hypixel", "Breezily", "God Bridge"};
    private static final String[] SOURCE_SPRINT_OPTIONS = {"None", "Vanilla", "Legit", "NCP", "Universal"};
    private static final String[] HOP_OPTIONS = {"Sprint", "Low", "Boost", "Burst", "Adaptive"};
    private static final double ROTATION_INCREMENT = (double) 0.0096f;
    private static final float TAKEOFF_YAW_MIN = 90.0F;
    private static final float TAKEOFF_YAW_MAX = 95.0F;
    private static final float AIRBORNE_YAW_MIN = 30.0F;
    private static final float AIRBORNE_YAW_MAX = 35.0F;
    private static final float LONG_TELLY_SMOOTH_TICKS = 9.0F;
    private static final int LOOK_DIAGONAL_BUCKETS = 1;
    private static final int GROUND_BUCKET_SEARCH_RADIUS = 1;
    private static final float GROUND_YAW_HOLD_ANGLE = 112.5F;
    private static final int GROUND_DIAGONAL_BUCKETS = 1;
    private static final int SPRINT_JUMP_HOLD_TICKS = 1;
    private static final float SPRINT_JUMP_ALIGNMENT = 12.0F;
    private static final int LOOK_LOCK_MISS_LIMIT = 4;
    private static final float YAW_BUCKET_STEP = 1.0F;
    private static final double[] FACE_SAMPLE_OFFSETS = new double[]{
            0.03125, 0.09375, 0.15625, 0.21875, 0.28125, 0.34375, 0.40625, 0.46875,
            0.53125, 0.59375, 0.65625, 0.71875, 0.78125, 0.84375, 0.90625, 0.96875
    };
    private static final double[] FACE_MIN_OFFSET = {0.0};
    private static final double[] FACE_MAX_OFFSET = {1.0};
    private static final double[] TOP_FACE_SAMPLES = {0.5, 0.25, 0.75};
    private static final double[] SIDE_FACE_SAMPLES = {0.5, 0.8, 0.2};
    private static final float ACTIVATION_SMOOTH_TICKS = 3.0F;
    private static final float ACTIVATION_YAW_LAG = 0.72F;
    private static final EnumFacing[] HORIZONTAL_FACES = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
    private static final EnumFacing[] PLACEMENT_FACES = {EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
    private static final float SPRINT_MAX_YAW_OFFSET = 50.0F;
    private static final float MOUSE_ROTATION_INCREMENT = 0.023437500F;
    private static final float MIN_YAW_JITTER = 0.11F;
    private static final float MAX_YAW_JITTER = 0.46F;

    private static final float[] HYPIXEL_YAW_OFFSETS = {
            0.0F, -1.0F, 1.0F, -2.0F, 2.0F, -3.0F, 3.0F, -4.0F, 4.0F, -5.0F, 5.0F,
            -6.0F, 6.0F, -7.0F, 7.0F, -8.0F, 8.0F, -9.0F, 9.0F, -10.0F, 10.0F,
            -11.0F, 11.0F, -12.0F, 12.0F, -13.0F, 13.0F, -14.0F, 14.0F, -15.0F, 15.0F,
            -16.0F, 16.0F, -17.0F, 17.0F, -18.0F, 18.0F, -19.0F, 19.0F, -20.0F, 20.0F,
            -22.0F, 22.0F, -24.0F, 24.0F, -26.0F, 26.0F, -28.0F, 28.0F, -30.0F, 30.0F,
            -32.0F, 32.0F, -34.0F, 34.0F, -36.0F, 36.0F, -38.0F, 38.0F, -40.0F, 40.0F,
            -42.0F, 42.0F, -44.0F, 44.0F, -46.0F, 46.0F, -48.0F, 48.0F, -50.0F, 50.0F,
            -52.0F, 52.0F, -54.0F, 54.0F, -56.0F, 56.0F, -58.0F, 58.0F, -60.0F, 60.0F,
            -62.0F, 62.0F, -64.0F, 64.0F, -66.0F, 66.0F, -68.0F, 68.0F, -70.0F, 70.0F,
            -72.0F, 72.0F, -74.0F, 74.0F, -76.0F, 76.0F, -78.0F, 78.0F, -80.0F, 80.0F,
            -82.0F, 82.0F, -84.0F, 84.0F, -86.0F, 86.0F, -88.0F, 88.0F, -90.0F, 90.0F
    };
    private static final float[] HYPIXEL_PITCH_OFFSETS = {
            0.0F, -1.0F, 1.0F, -2.0F, 2.0F, -3.0F, 3.0F, -4.0F, 4.0F, -5.0F, 5.0F,
            -6.0F, 6.0F, -7.0F, 7.0F, -8.0F, 8.0F, -9.0F, 9.0F, -10.0F, 10.0F,
            -11.0F, 11.0F, -12.0F, 12.0F, -13.0F, 13.0F, -14.0F, 14.0F, -15.0F, 15.0F,
            -16.0F, 16.0F, -17.0F, 17.0F, -18.0F, 18.0F, -19.0F, 19.0F, -20.0F, 20.0F
    };

    private SliderSetting tellyMode;
    private KeySetting[] scaffoldModeKeybinds = new KeySetting[MODE_OPTIONS.length];
    private KeySetting[] keepYModeKeybinds = new KeySetting[KEEP_Y_OPTIONS.length + 1];
    private SliderSetting keepMode;
    private SliderSetting multiplace;
    private SliderSetting iceThreshold;
    private SliderSetting clickSpeed;
    private SliderSetting debugMode;
    private SliderSetting ninjaEdgeDistance;
    private SliderSetting ninjaMinDelay;
    private SliderSetting ninjaMaxDelay;
    private SliderSetting ninjaSneakOnJump;
    private ButtonSetting ninjaRequireSneakKey;
    private ButtonSetting ninjaRequireBlocks;
    private ButtonSetting ninjaRequireLookingDown;
    private ButtonSetting ninjaDisableForward;
    private SliderSetting breezilySwitchSpeed;
    private SliderSetting breezilyGroundWaitTicks;
    private SliderSetting rushLookAhead;
    private SliderSetting rushChain;
    private SliderSetting towerMode;
    private SliderSetting sourceScaffoldMode;
    private SliderSetting sourceSprintMode;
    private SliderSetting sourceTellyStraightTicks;
    private SliderSetting sourceTellyDiagonalTicks;
    private SliderSetting sourceTellyJumpDownTicks;
    private SliderSetting sourcePlaceDelay;
    private SliderSetting sourceSneakEvery;
    private SliderSetting hopMode;
    private SliderSetting hopSpeed;
    private ButtonSetting rushAutoHop;
    private ButtonSetting sourceTellyWatchdog;
    private ButtonSetting sourceAutoJump;
    private ButtonSetting sourceSneak;
    private ButtonSetting hopAutoSprint;
    private ButtonSetting tellyOnJump;
    private ButtonSetting autoSwap;
    private ButtonSetting disableInGui;
    private ButtonSetting ninjaAvoidDoubleSneaking;
    private ButtonSetting breezilyAutoStrafe;
    private ButtonSetting breezilyOnlyStrafeOnGround;
    private ButtonSetting spoofItem;
    private ButtonSetting showBlocks;
    private SliderSetting counterPositionX;
    private SliderSetting counterPositionY;
    private ButtonSetting blockHighlight;
    private ButtonSetting editPosition;
    private ButtonSetting keepYOnRightClick;
    private ButtonSetting disableOnJumpPotion;
    private ButtonSetting silentSwing;
    private ButtonSetting alwaysSprint;

    private float posX = Float.NaN;
    private float posY = Float.NaN;
    private float relativePosX = Float.NaN;
    private float relativePosY = Float.NaN;
    private Timer fadeTimer;
    private Timer fadeInTimer;
    private float previousAlpha;
    private final java.util.ArrayList<Object[]> placedBlockVisuals = new java.util.ArrayList<Object[]>();
    private keystrokesmod.utility.font.RavenFontRenderer blockCounterFont;
    private String blockCounterFontName = "";
    private SliderSetting blockCounterFontSetting;
    // Fixed backward bridging angle; separate from Ninja's sneak predictor.
    private float ninjaBridgeYaw = Float.NaN;
    private float ninjaBridgePitch = 80.0F;
    private int ninjaBridgeInputMask;
    private float ninjaBaseYaw = Float.NaN;
    private float ninjaAppliedYaw = Float.NaN;
    // Original lane direction, not the previous recovery result.
    // Prevents missed blocks from rotating the bridge into another lane.
    private float ninjaAimAnchorYaw = Float.NaN;
    private int ninjaDiagonalSide;
    private int ninjaLastDiagonalSide;
    // Slow recovery only: NEVER change the aim for every individual block.
    private int ninjaAimMissTicks;
    private int ninjaAimRecoveryCooldown;
    private boolean ninjaPitchRecoveryUsed;
    private int ninjaSneakSafeTicks;
    private BlockPos ninjaEdgePulseSupport;

    private int originalSlot = -1;
    private int placementSlot = -1;
    private boolean hasSwapped;
    private boolean physicalSlotSwapped;
    private int heldBlockCount = -1;
    private int keepYStage;
    private int keepYStartHeight = 256;
    private boolean useCurrentHeight;

    private boolean ninjaSneaking;
    private long ninjaReleaseAt;
    private long ninjaJumpSneakUntil;
    private boolean ninjaJumpKeyWasDown;
    private BlockPos ninjaTrackedSupportBlock;

    private int breezilyTickCounter;
    private int breezilyGroundWaitRemaining;
    private boolean breezilyUseLeft = true;
    private float breezilyLockedYaw = Float.NaN;
    private float breezilyViewYaw = Float.NaN;
    private float breezilyViewPitch = Float.NaN;

    private BlockPos rushLastPlacedTarget;
    private Placement rushPlannedPlacement;
    private boolean rushTieXNext = true;
    private int rushHeadingX;
    private int rushHeadingZ;
    private int rushMissStreak;

    private BlockPos rushWarmTarget;
    private EnumFacing rushWarmFace;
    private int rushWarmTicks;

    private float rushLaneYaw = Float.NaN;
    private float rushLanePitch = Float.NaN;
    private int rushLaneAge;

    private int towerTick;
    private int towerDelay;
    private int towerLastPlacedY = Integer.MIN_VALUE;
    private double towerPrevMotionY;
    private boolean towerWasActive;
    private int towerCycle;
    private double towerLaunchY;
    private boolean towerSpoofGroundThisTick;

    private int hypixelPlaceTimer;
    private float hypixelSourceYaw = Float.NaN;
    private float hypixelSourcePitch = 90.0F;
    private float snapPlusLastMovementYaw = Float.NaN;

    private int sourceAirTicks;
    private int sourceGroundTicks;
    private int sourcePlaceDelayTicks;
    private int sourceBlocksPlaced;
    private int sourceGodBridgeJumpBlocks;
    private int sourceSneakBlocks;
    private int sourceDirectionalChange;
    private double sourceStartY;

    private int hopAirTicks;
    private boolean hopLaunchRequested;

    private boolean snapReturnPending;
    private boolean snapSkipPlacementThisTick;
    private int snapDiagonalHoldTicks;
    private float snapLastPlacementYaw = Float.NaN;

    private Object spoofRendererObject;
    private java.lang.reflect.Method spoofRendererCancelUpdate;
    private java.lang.reflect.Method spoofRendererCancelReset;
    private boolean spoofRendererResolved;
    private int spoofServerSlot = -1;
    private boolean sendingSpoofSlotPacket;

    private String lastScaffoldMode = "";
    private String lastSourceScaffoldMode = "";

    // Keep the native module name stable. Arraylist mode text belongs in the HUD renderer.
    // Reflection only: internal Raven Setting/Module classes vary between builds.
    private Object nativeScaffoldModule;
    private boolean settingVisibilityUnavailable;

    private boolean sprintSuppressed;
    private boolean sprintWasPressed;
    private boolean sprintStopRequested;
    private int sprintToggleCooldown;

    private final Vec3[] predictedPositions = new Vec3[5];
    private boolean hasPrediction;
    private EntityPlayerSP predictionInputPlayer;
    private float predictionInputYaw = Float.NaN;
    private final Placement[] candidates = new Placement[16];
    private BlockPos neededBlock;

    private int longTellyMissTicks;
    private BlockPos verificationBlock;
    private int verificationDelayTicks;
    private int revertedPlacementCount;
    private int longTellyYawBucket;
    private boolean longTellySprintJump = true;
    private boolean longTellyWasAirborne;
    private boolean longTellyNeedsSideUpdate = true;
    private boolean longTellyWasEngaged;
    private double previousVerticalMotion;
    private int lastGroundX;
    private int lastGroundZ;
    private int ticksSinceGrounded = 99;
    private int longTellyRowY;
    private int longTellySide = 1;
    private boolean hasLongTellyOrigin;
    private int longTellyOriginX;
    private int longTellyOriginZ;
    private int longTellyForwardX;
    private int longTellyForwardZ;
    private int longTellyLateralX;
    private int longTellyLateralZ;
    private final BlockPos[] queuedBlocks = new BlockPos[8];
    private final int[] queueRetryTicks = new int[8];
    private int queuedBlockCount;
    private int pendingQueueIndex = -1;
    private int queueCursor;
    private int strafeTapCooldown;
    private int queueStallTicks;
    private int skippedQueueMask;
    private float longTellyAimYaw;
    private float longTellyAimPitch;
    private Placement pendingPlacement;
    private boolean pendingPlacementReady;

    private int placementDelayTicks;
    private float yaw = -180.0F;
    private float pitch;
    private boolean canRotate;
    private boolean tellyJumping;
    private boolean takeoffLatched;
    private boolean placedThisTick;
    private int placementAttemptsThisTick;
    private float lastSentYaw;
    private float lastSentPitch;
    private float previousSentPitch;
    private float takeoffPitch;
    private int activationTicks;
    private int jumpHoldTicks;
    private int groundPlacementSide = 1;
    private boolean movingCardinally;
    private int lookSide = 1;
    private boolean lookingStraightBack;
    private float activationYawAccumulator;
    private boolean rotationSentThisTick;
    private boolean rotationSentLastTick;
    private boolean movementFixActive;
    private float rotationEaseRate = 1.0F;
    private int cardinalSide = 1;
    private float continuousCameraYaw;
    private boolean hasCameraYaw;
    private float yawGridBase;
    private int groundMissTicks;
    private boolean rotationEaseStarted;
    private float rotationEaseSlowdown;
    private int lastYawBucket;
    private boolean diagonalMovement;
    private float targetYaw;
    private float targetPitch;
    private float pendingYawJitter;
    private float lastBaseYaw;
    private float lastBasePitch;
    private float yawJitterAmplitude = 0.28F;

    private boolean sendingPlacement;
    private long nextClickAt;
    private int clickCount;
    private long clickWindowStart;
    private float measuredClickCps;
    private char clickState = 'D';
    private boolean sendingAutoClick;

    private float debugSnappedYaw;
    private float debugVerifiedYaw;
    private float debugSentYaw;
    private int debugTicks;
    private float debugYawGridOffset;
    private float debugMovementYawOffset;
    private int debugAimSelection;
    private int debugAimScore = -1;
    private int debugCandidateCount;
    private double debugOverhang;
    private Vec3 lastPlacementHit;
    private long lastPlacementTime;
    private char debugLongTellyStatus = '-';
    private char debugPlacementStatus = '-';
    private char debugMissingPlacementReason = 'n';
    private float debugPlacementPitchDelta;
    private float debugMaxPlacementPitchDelta;

    void onLoad() {
        modules.registerDescription("Credits");
        modules.registerDescription("mko0763");

        this.registerSetting(tellyMode = new SliderSetting("Modes", 0, MODE_OPTIONS));

        GroupSetting keybindGroup = new GroupSetting("Keybinds");
        this.registerSetting(keybindGroup);
        for (int i = 0; i < MODE_OPTIONS.length; i++) {
            this.registerSetting(scaffoldModeKeybinds[i] = new KeySetting(keybindGroup, MODE_OPTIONS[i], 0));
        }
        // Bind Keep Y independently: Off, Telly A, and Telly B.
        for (int i = 0; i < keepYModeKeybinds.length; i++) {
            String name = i == 0 ? "Keep Y Off" : "Keep Y " + KEEP_Y_OPTIONS[i - 1];
            this.registerSetting(keepYModeKeybinds[i] = new KeySetting(keybindGroup, name, 0));
        }

        this.registerSetting(keepMode = new SliderSetting("Keep Y", true, -1, KEEP_Y_OPTIONS));
        this.registerSetting(multiplace = new SliderSetting("Multi-place", true, -1, 2, 3, 1));
        this.registerSetting(clickSpeed = new SliderSetting("Click speed", " CPS", 0.0, 0.0, 20.0, 0.5));
        this.registerSetting(iceThreshold = new SliderSetting("Ice threshold", -0.60, -0.60, 0.60, 0.01));
        this.registerSetting(autoSwap = new ButtonSetting("Auto swap", true));
        this.registerSetting(disableInGui = new ButtonSetting("Disable in GUI", true));
        this.registerSetting(disableOnJumpPotion = new ButtonSetting("Disable on jump potion", false));
        this.registerSetting(keepYOnRightClick = new ButtonSetting("Keep Y on RMB", false));
        this.registerSetting(tellyOnJump = new ButtonSetting("Telly on jump", false));

        GroupSetting ninjaGroup = new GroupSetting("Ninja");
        this.registerSetting(ninjaGroup);
        // The 0.30 maximum matches the vanilla overhang margin used by Legit Scaffold.
        this.registerSetting(ninjaEdgeDistance = new SliderSetting(ninjaGroup, "Edge distance", " blocks", 0.08, 0.0, 0.30, 0.01));
        this.registerSetting(ninjaMinDelay = new SliderSetting(ninjaGroup, "Min sneak delay", " ms", 45.0, 0.0, 300.0, 5.0));
        this.registerSetting(ninjaMaxDelay = new SliderSetting(ninjaGroup, "Max sneak delay", " ms", 85.0, 0.0, 300.0, 5.0));
        this.registerSetting(ninjaSneakOnJump = new SliderSetting(ninjaGroup, "Sneak on jump", " ms", 0.0, 0.0, 500.0, 5.0));
        // Optional Legit Scaffold controls. Disabled by default for existing Ninja users.
        this.registerSetting(ninjaRequireSneakKey = new ButtonSetting(ninjaGroup, "Sneak key pressed", false));
        this.registerSetting(ninjaRequireBlocks = new ButtonSetting(ninjaGroup, "Holding blocks", false));
        this.registerSetting(ninjaRequireLookingDown = new ButtonSetting(ninjaGroup, "Looking down", false));
        this.registerSetting(ninjaDisableForward = new ButtonSetting(ninjaGroup, "Not moving forward", false));
        this.registerSetting(ninjaAvoidDoubleSneaking = new ButtonSetting(ninjaGroup, "Avoid double sneaking", true));

        GroupSetting breezilyGroup = new GroupSetting("Breezily");
        this.registerSetting(breezilyGroup);
        this.registerSetting(breezilyAutoStrafe = new ButtonSetting(breezilyGroup, "Auto strafe", true));
        this.registerSetting(breezilyOnlyStrafeOnGround = new ButtonSetting(breezilyGroup, "Only strafe on ground", false));
        this.registerSetting(breezilyGroundWaitTicks = new SliderSetting(breezilyGroup, "Ground wait", " ticks", 2.0, 1.0, 10.0, 1.0));
        this.registerSetting(breezilySwitchSpeed = new SliderSetting(breezilyGroup, "Switch speed", " ticks", 4.0, 2.0, 10.0, 1.0));

        GroupSetting rushGroup = new GroupSetting("Rush");
        this.registerSetting(rushGroup);
        this.registerSetting(rushLookAhead = new SliderSetting(rushGroup, "Look ahead", " blocks", 2.10, 1.20, 3.00, 0.05));
        this.registerSetting(rushChain = new SliderSetting(rushGroup, "Verified chain", " blocks/tick", 2.0, 1.0, 3.0, 1.0));
        this.registerSetting(rushAutoHop = new ButtonSetting(rushGroup, "Sprint hop", true));

        GroupSetting sourceGroup = new GroupSetting("Hypixel");
        this.registerSetting(sourceGroup);
        this.registerSetting(sourceScaffoldMode = new SliderSetting(sourceGroup, "Style", 2, SOURCE_SCAFFOLD_OPTIONS));
        this.registerSetting(sourceSprintMode = new SliderSetting(sourceGroup, "Sprint mode", 0, SOURCE_SPRINT_OPTIONS));
        this.registerSetting(sourceTellyWatchdog = new ButtonSetting(sourceGroup, "Telly watchdog", true));
        this.registerSetting(sourceTellyStraightTicks = new SliderSetting(sourceGroup, "Telly straight", " ticks", 6.0, 0.0, 8.0, 1.0));
        this.registerSetting(sourceTellyDiagonalTicks = new SliderSetting(sourceGroup, "Telly diagonal", " ticks", 4.0, 0.0, 8.0, 1.0));
        this.registerSetting(sourceTellyJumpDownTicks = new SliderSetting(sourceGroup, "Telly jump", " ticks", 1.0, 0.0, 8.0, 1.0));
        this.registerSetting(sourcePlaceDelay = new SliderSetting(sourceGroup, "Place delay", " x20ms", 0.0, 0.0, 10.0, 1.0));
        this.registerSetting(sourceAutoJump = new ButtonSetting(sourceGroup, "Auto jump", false));
        this.registerSetting(sourceSneak = new ButtonSetting(sourceGroup, "GodBridge sneak", false));
        this.registerSetting(sourceSneakEvery = new SliderSetting(sourceGroup, "Sneak every", " blocks", 1.0, 1.0, 16.0, 1.0));

        GroupSetting hopGroup = new GroupSetting("Hop");
        this.registerSetting(hopGroup);
        this.registerSetting(hopMode = new SliderSetting(hopGroup, "Movement", 0, HOP_OPTIONS));
        this.registerSetting(hopSpeed = new SliderSetting(hopGroup, "Speed", " x", 1.08, 1.00, 1.18, 0.01));
        this.registerSetting(hopAutoSprint = new ButtonSetting(hopGroup, "Auto sprint", true));

        GroupSetting towerGroup = new GroupSetting("Tower");
        this.registerSetting(towerGroup);
        this.registerSetting(towerMode = new SliderSetting(towerGroup, "Mode", 0, TOWER_OPTIONS));

        // One native, collapsible Raven group for the entire HUD and placement visuals.
        GroupSetting visualsGroup = new GroupSetting("Visuals");
        this.registerSetting(visualsGroup);
        this.registerSetting(showBlocks = new ButtonSetting(visualsGroup, "Block Counter", true));
        this.registerSetting(counterPositionX = new SliderSetting(visualsGroup, "Counter X", " %", 45.0, 0.0, 100.0, 0.1));
        this.registerSetting(counterPositionY = new SliderSetting(visualsGroup, "Counter Y", " %", 65.0, 0.0, 100.0, 0.1));
        try {
            // Exactly the same font registry used by the supplied TrueAdam script.
            String[] fontOptions = keystrokesmod.utility.font.FontManager.getHudFontOptions();
            if (fontOptions != null && fontOptions.length > 0) {
                int defaultFont = 0;
                for (int i = 0; i < fontOptions.length; i++)
                    if ("Sf-Ui".equals(fontOptions[i])) { defaultFont = i; break; }
                this.registerSetting(blockCounterFontSetting =
                        new SliderSetting(visualsGroup, "Font", defaultFont, fontOptions));
            }
        } catch (Throwable unavailableFontRegistry) {
            // Older Raven builds still use the default HUD renderer below.
        }
        this.registerSetting(blockHighlight = new ButtonSetting(visualsGroup, "Block Highlight", false));
        modules.registerColor("Visuals", "Highlight Color", 100, 255, 100, 100);
        this.registerSetting(editPosition = new ButtonSetting(visualsGroup, "Edit position", () -> openCounterPositionEditor()));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
        this.registerSetting(spoofItem = new ButtonSetting("Spoof item", false));
        this.registerSetting(alwaysSprint = new ButtonSetting("Sprint", false));
        this.registerSetting(debugMode = new SliderSetting("Debug", 0, DEBUG_OPTIONS));
        this.closetModule = true;
    }
    public void onEnable() {
        syncScaffoldModeKeybindState();
        syncKeepYModeKeybindState();
        originalSlot = (mc.thePlayer != null) ? mc.thePlayer.inventory.currentItem : -1;
        placementSlot = -1;
        hasSwapped = false;
        physicalSlotSwapped = false;
        spoofServerSlot = -1;
        sendingSpoofSlotPacket = false;
        heldBlockCount = -1;
        placedBlockVisuals.clear();
        placementDelayTicks = 3;
        yaw = -180.0F;
        pitch = 0.0F;
        canRotate = false;
        rotationSentThisTick = false;
        rotationSentLastTick = false;
        movementFixActive = false;
        resetMovementState();
        cardinalSide = 1;
        sprintSuppressed = false;
        sprintWasPressed = false;

        neededBlock = null;
        debugAimSelection = 0;
        debugOverhang = 0.0;
        lastPlacementHit = null;
        lastPlacementTime = 0L;
        longTellySide = 1;
        keepYStage = 0;
        keepYStartHeight = 256;
        useCurrentHeight = false;
        placedThisTick = false;
        placementAttemptsThisTick = 0;
        resetNinjaState();
        resetBreezilyState();
        resetRushState();
        breezilyViewYaw = Float.NaN;
        breezilyViewPitch = Float.NaN;
        snapReturnPending = false;
        snapSkipPlacementThisTick = false;
        snapDiagonalHoldTicks = 0;
        snapLastPlacementYaw = Float.NaN;
        hypixelPlaceTimer = 0;
        hypixelSourceYaw = Float.NaN;
        hypixelSourcePitch = 90.0F;
        snapPlusLastMovementYaw = Float.NaN;
        sourceAirTicks = 0;
        sourceGroundTicks = 0;
        sourcePlaceDelayTicks = 0;
        sourceBlocksPlaced = 0;
        sourceGodBridgeJumpBlocks = 0;
        sourceSneakBlocks = 0;
        sourceDirectionalChange = 0;
        sourceStartY = mc.thePlayer == null ? 0.0D : Math.floor(mc.thePlayer.posY);
        hopAirTicks = 0;
        hopLaunchRequested = false;
        lastScaffoldMode = getScaffoldMode();
        lastSourceScaffoldMode = "";
        setServerRelativeMovementInputs(false);
    }
    public void onDisable() {
        if (editPositionMode) closeCounterPositionEditor();

        restoreAutoSwapSlot();
        originalSlot = -1;
        placementSlot = -1;
        hasSwapped = false;
        physicalSlotSwapped = false;
        RotationHelper.get().forceMovementFix = false;
        client.disableMovementFix();
        if (sprintSuppressed) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(),
                    sprintWasPressed || Utils.isBindDown(mc.gameSettings.keyBindSprint));
            sprintSuppressed = false;
        }
        resetMovementState();
        resetNinjaState();
        resetBreezilyState();
        resetRushState();
        breezilyViewYaw = Float.NaN;
        breezilyViewPitch = Float.NaN;
        snapReturnPending = false;
        snapSkipPlacementThisTick = false;
        snapDiagonalHoldTicks = 0;
        snapLastPlacementYaw = Float.NaN;
        hypixelPlaceTimer = 0;
        hypixelSourceYaw = Float.NaN;
        hypixelSourcePitch = 90.0F;
        snapPlusLastMovementYaw = Float.NaN;
        sourceAirTicks = 0;
        sourceGroundTicks = 0;
        sourcePlaceDelayTicks = 0;
        sourceBlocksPlaced = 0;
        sourceGodBridgeJumpBlocks = 0;
        sourceSneakBlocks = 0;
        sourceDirectionalChange = 0;
        hopAirTicks = 0;
        hopLaunchRequested = false;
        setServerRelativeMovementInputs(false);
        setItemRendererSpoofFlags(false);
        placedBlockVisuals.clear();
    }
    void onPreUpdate() {
        restoreBreezilyCamera();
        mc.sync();

        pollActionButtons();
        if (!Utils.nullCheck()) {
            return;
        }
        if (mc.currentScreen == null) {
            pollScaffoldModeKeybinds();
            pollKeepYModeKeybinds();
        } else {
            syncScaffoldModeKeybindState();
            syncKeepYModeKeybindState();
        }
        if (shouldPauseForGui()) {
            pauseForGui();
            return;
        }
        handleModeTransition();
        handleSourceStyleTransition();
        Vec3 position = mc.thePlayer.getPositionVector();

        beginTick();
        if (isNinjaMode() && !isTowerActive()) {
            // Important: end a previous tower session when Space is released.
            // Ninja's normal bridging path must not bypass a fresh tower later.
            updateTowerSession(false);
            updateNinjaClassicPlacement();
            return;
        }
        if (isNinjaMode() && isTowerActive()) {
            // Tower uses the unchanged standard placement/rotation pipeline.
            // It must not be intercepted by the Ninja bridge-only path.
            resetNinjaPlacementAim();
        }
        if (hypixelPlaceTimer > 0) {
            hypixelPlaceTimer--;
        }
        if (sourcePlaceDelayTicks > 0) {
            sourcePlaceDelayTicks--;
        }
        if (mc.thePlayer.onGround) {
            sourceGroundTicks++;
            sourceAirTicks = 0;
        } else {
            sourceAirTicks++;
            sourceGroundTicks = 0;
        }
        logTickState(mc.thePlayer, position);
        updateKeepYState(mc.thePlayer, position);
        selectPlacementStack(mc.thePlayer);
        boolean longTellyEngaged = updateLongTellyState(mc.thePlayer, position);

        float cameraYaw = updateCameraYaw(mc.thePlayer.rotationYaw);
        boolean breezilyGodBridgeJump = isBreezilyGodBridgeJump();
        boolean snapGodBridgeJump = isSnapGodBridgeJump();
        if (snapGodBridgeJump) {
            snapReturnPending = false;
            snapSkipPlacementThisTick = false;
            snapDiagonalHoldTicks = 0;
            snapLastPlacementYaw = Float.NaN;
        }
        setServerRelativeMovementInputs(false);
        if (isBreezilyMode() || (isHypixelMode() && getSourceScaffoldMode().equals("Breezily"))) {
            breezilyViewYaw = cameraYaw;
            breezilyViewPitch = mc.thePlayer.rotationPitch;
        } else {
            breezilyViewYaw = Float.NaN;
            breezilyViewPitch = Float.NaN;
        }

        float movementYaw = getMovementYaw(cameraYaw, getForwardInput(), getStrafeInput());
        float referenceServerYaw = getServerYaw();
        float backwardYaw = unwrapYaw(movementYaw - 180.0F, referenceServerYaw);
        float diagonalYaw = isDiagonalMovement(movementYaw)
                ? backwardYaw
                : unwrapYaw(movementYaw - 135.0F * (float) updateCardinalSide(movementYaw), referenceServerYaw);

        if (!canRotate) {
            if (yaw == -180.0F && pitch == 0.0F) {
                pitch = quantizeRotation(85.0F);
            }
            yaw = quantizeRotation(diagonalYaw);
        }

        yawGridBase = cameraYaw;

        boolean flatGround = mc.thePlayer.onGround && !tellyJumping && !isTellyTakeoff();
        if (flatGround) {
            yaw = quantizeRotation(diagonalYaw);
            canRotate = true;
        }

        boolean shortDiagonal = isShortDiagonalJump();
        Placement placement = flatGround ? null : findPlacement();
        debugPlacementStatus = flatGround ? 'F' : (placement == null ? debugMissingPlacementReason : 'g');
        aimAtPlacement(placement, referenceServerYaw, mc.thePlayer, position);

        boolean tellyRotating = tellyJumping || isTellyTakeoff();

        Vec3 motion = new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
        float previousYaw = rotationSentLastTick ? lastSentYaw : cameraYaw;
        float previousPitch = rotationSentLastTick ? lastSentPitch : mc.thePlayer.rotationPitch;

        boolean urgent = !rotationSentLastTick
                || isUnsupportedAt(position.x, position.z)
                || isUnsupportedAt(position.x + motion.x * 2.0, position.z + motion.z * 2.0)
                || isUnsupportedAt(position.x + motion.x * 3.5, position.z + motion.z * 3.5)
                || isUnsupportedAt(position.x + motion.x * 5.0, position.z + motion.z * 5.0)
                || groundMissTicks >= 3;

        updateTargetRotations(position, motion, previousYaw, previousPitch, urgent, shortDiagonal);

        int lookBucket = updateLookBucket(position, cameraYaw);

        boolean cameraAligned = alignTakeoffRotations(tellyRotating, cameraYaw);

        snapGroundYaw(tellyRotating, position, motion, cameraYaw);

        aimYawWithJitter(targetYaw, nextYawJitter());
        debugSnappedYaw = targetYaw;
        aimPitch(RotationUtils.clampPitch(targetPitch));

        int groundCandidateIndex = flatGround ? aimGroundPlacement(mc.thePlayer, position, urgent, previousPitch) : -1;

        boolean placementAllowed = isPlacementAllowed(placement);

        Vec3 verifiedHit = placement != null && placementAllowed
                ? verifyPlacementAim(placement, tellyRotating, shortDiagonal, lookBucket, cameraAligned) : null;

        if (isBreezilyMode() && !breezilyGodBridgeJump) {
            float fixedBreezilyYaw = getBreezilyLockedYaw(cameraYaw);
            if (flatGround) {
                int breezilyGroundY = MathHelper.floor_double(position.y) - 1;
                int breezilyCandidateCount = buildCandidates(breezilyGroundY);
                debugCandidateCount = breezilyCandidateCount;
                groundCandidateIndex = aimBreezilyGroundCandidate(breezilyCandidateCount, fixedBreezilyYaw);
                verifiedHit = null;
            } else if (placement != null && placementAllowed) {
                verifiedHit = aimBreezilyPlacement(placement, fixedBreezilyYaw);
            } else {
                targetYaw = fixedBreezilyYaw;
                targetPitch = quantizeRotation(78.0F);
            }
        }

        if (isSnapPlusMode()) {
            groundCandidateIndex = -1;
            verifiedHit = null;
            pendingPlacementReady = false;
            pendingPlacement = null;

            updateHypixelSourceIdleRotation(movementYaw);

            boolean sourceCanPlace = isHypixelSourceCanPlace(position);
            Placement snapPlusPlacement = sourceCanPlace ? findPlacement() : null;
            if (snapPlusPlacement != null && isPlacementAllowed(snapPlusPlacement)) {
                Vec3 snapPlusHit = aimHypixelPlacement(snapPlusPlacement, movementYaw);
                if (snapPlusHit != null) {
                    placement = snapPlusPlacement;
                    verifiedHit = snapPlusHit;
                } else {
                    placement = null;
                }
            } else {
                placement = null;
                if (!Float.isNaN(hypixelSourceYaw)) {
                    targetYaw = hypixelSourceYaw;
                    targetPitch = hypixelSourcePitch;
                    pendingYawJitter = 0.0F;
                }
            }
        }

        if (isHypixelMode()) {
            groundCandidateIndex = -1;
            verifiedHit = null;
            pendingPlacementReady = false;
            pendingPlacement = null;

            String sourceStyle = getSourceScaffoldMode();
            if (sourceStyle.equals("Hypixel")) {
                updateHypixelSourceIdleRotation(movementYaw);
            } else if (sourceStyle.equals("Telly")) {
                updateSourceTellyIdleRotation(cameraYaw);
            }

            boolean sourceCanPlace = shouldSourceScaffoldPlace(position, sourceStyle);
            Placement sourcePlacement = sourceCanPlace && sourcePlaceDelayTicks <= 0 ? findPlacement() : null;

            if (sourcePlacement != null && isPlacementAllowed(sourcePlacement)) {
                Vec3 sourceHit = aimSourceScaffoldPlacement(sourcePlacement, sourceStyle, movementYaw, cameraYaw);
                if (sourceHit != null) {
                    placement = sourcePlacement;
                    verifiedHit = sourceHit;
                } else {
                    placement = null;
                }
            } else {
                placement = null;
                if (sourceStyle.equals("Hypixel") && !Float.isNaN(hypixelSourceYaw)) {
                    targetYaw = hypixelSourceYaw;
                    targetPitch = hypixelSourcePitch;
                    pendingYawJitter = 0.0F;
                }
            }

            if (sourceStyle.equals("God Bridge")) {
                updateSourceGodBridgeRotationState();
            }

            String sourceSprint = sourceSprintMode == null ? "None" : sourceSprintMode.getSelectedOption();
            if ("Universal".equals(sourceSprint) && sourceBlocksPlaced >= 3) {
                float fromYaw = rotationSentLastTick ? lastSentYaw : RotationUtils.serverRotations[0];
                targetYaw = quantizeMouseRotation(unwrapYaw(getPlayerYaw(), fromYaw), fromYaw);
                sourceBlocksPlaced = 0;
            }
        }

        debugVerifiedYaw = targetYaw;

        debugLongTellyStatus = 'N';
        if (longTellyEngaged && verifiedHit == null && groundCandidateIndex < 0 && queuedBlockCount > 0
                && !(mc.thePlayer.onGround && longTellySprintJump) && findLongTellyPlacement()) {
            aimLongTellyPlacement(previousYaw, previousPitch, lookBucket, cameraAligned);
        }

        if (verifiedHit == null && groundCandidateIndex < 0 && !pendingPlacementReady && !placedThisTick
                && !isHypixelMode() && !isSnapPlusMode()) {
            easeActivationRotation(mc.thePlayer, previousYaw, previousPitch, lookBucket, cameraAligned);
        }

        updateRotationDebug(motion, cameraYaw);

        boolean snapPlacementTick = isSnapPlacementReady(groundCandidateIndex, verifiedHit, placement);
        if (isSnapRotationActive() && snapReturnPending) {

            if (!snapPlacementTick && isSafeSnapReturn(position, motion)) {
                snapReturnPending = false;
            } else if (snapPlacementTick) {
                snapReturnPending = false;
            }
        }
        applySnapModeRotations(cameraYaw, snapPlacementTick);

        float serverYaw = RotationUtils.serverRotations[0];
        targetYaw = serverYaw + MathHelper.wrapAngleTo180_float(targetYaw - serverYaw);

        targetYaw = quantizeMouseRotation(targetYaw, lastSentYaw);
        targetPitch = RotationUtils.clampPitch(quantizeMouseRotation(targetPitch, lastSentPitch));
        if (shortDiagonal && placement != null && placementAllowed && placementDelayTicks <= 0) {
            verifiedHit = raycastFace(placement, targetYaw, targetPitch);
            if (verifiedHit == null) {
                verifiedHit = aimShortDiagonalFace(placement);
            }
        }

        if (isSnapRotationActive() && snapPlacementTick) {
            Placement snapPlacement = null;
            if (groundCandidateIndex >= 0 && groundCandidateIndex < candidates.length) {
                snapPlacement = candidates[groundCandidateIndex];
            } else if (verifiedHit != null && placement != null) {
                snapPlacement = placement;
            } else if (pendingPlacementReady) {
                snapPlacement = pendingPlacement;
            }
            if (snapPlacement != null) {
                Vec3 snapHit = aimSnapFace(snapPlacement, cameraYaw);
                if (snapHit == null) {
                    if (groundCandidateIndex >= 0) groundCandidateIndex = -1;
                    if (placement == snapPlacement) verifiedHit = null;
                } else if (placement == snapPlacement) {
                    verifiedHit = snapHit;
                }
            }
        }

        boolean towering = isTowerActive();
        updateTowerSession(towering);
        rushPlannedPlacement = null;
        if (towering) {
            groundCandidateIndex = -1;
            verifiedHit = null;
            placement = null;
            pendingPlacementReady = false;
            pendingPlacement = null;

            Placement towerPlacement = findTowerPlacement(position);
            if (towerPlacement != null) {
                Vec3 towerHit = aimTowerFace(towerPlacement, cameraYaw);
                if (towerHit != null) {
                    placement = towerPlacement;
                    verifiedHit = towerHit;
                    rushPlannedPlacement = towerPlacement;
                }
            } else {
                float currentServerYaw = RotationUtils.serverRotations[0];
                targetYaw = quantizeMouseRotation(unwrapYaw(cameraYaw, currentServerYaw), lastSentYaw);
                targetPitch = RotationUtils.clampPitch(quantizeMouseRotation(84.0F, lastSentPitch));
                pendingYawJitter = 0.0F;
            }
        } else if (isRushMode()) {

            int baseGroundCandidateIndex = groundCandidateIndex;
            Vec3 baseVerifiedHit = verifiedHit;
            Placement basePlacement = placement;
            boolean basePendingReady = pendingPlacementReady;
            Placement basePendingPlacement = pendingPlacement;
            float baseTargetYaw = targetYaw;
            float baseTargetPitch = targetPitch;
            float baseYawJitter = pendingYawJitter;

            Placement rushPlacement = planRushPlacement(position, cameraYaw);
            boolean baseReady = baseGroundCandidateIndex >= 0
                    || baseVerifiedHit != null
                    || basePendingReady;

            if (rushPlacement != null) {
                Vec3 leasedHit = rotationSentLastTick
                        ? raycastFace(rushPlacement, lastSentYaw, lastSentPitch) : null;
                boolean leased = leasedHit != null && isPlacementWithinReach(rushPlacement, 4.05D);

                if (leased) {

                    groundCandidateIndex = -1;
                    verifiedHit = leasedHit;
                    placement = rushPlacement;
                    pendingPlacementReady = false;
                    pendingPlacement = null;
                    targetYaw = lastSentYaw;
                    targetPitch = lastSentPitch;
                    pendingYawJitter = 0.0F;
                    rushPlannedPlacement = rushPlacement;
                    rushWarmTarget = null;
                    rushWarmFace = null;
                    rushWarmTicks = 0;
                    rushMissStreak = 0;
                } else if (baseReady) {

                    groundCandidateIndex = baseGroundCandidateIndex;
                    verifiedHit = baseVerifiedHit;
                    placement = basePlacement;
                    pendingPlacementReady = basePendingReady;
                    pendingPlacement = basePendingPlacement;
                    targetYaw = baseTargetYaw;
                    targetPitch = baseTargetPitch;
                    pendingYawJitter = baseYawJitter;
                    rushWarmTarget = null;
                    rushWarmFace = null;
                    rushWarmTicks = 0;
                    rushMissStreak = 0;
                } else {

                    groundCandidateIndex = -1;
                    verifiedHit = null;
                    placement = null;
                    pendingPlacementReady = false;
                    pendingPlacement = null;
                    Vec3 preparedHit = aimRushPlacement(rushPlacement, cameraYaw);
                    if (preparedHit != null) {
                        BlockPos target = rushPlacement.getTarget();
                        boolean sameWarm = rushWarmTarget != null
                                && rushWarmTarget.equals(target)
                                && rushWarmFace == rushPlacement.face;
                        rushWarmTarget = target;
                        rushWarmFace = rushPlacement.face;
                        rushWarmTicks = sameWarm ? Math.min(3, rushWarmTicks + 1) : 1;
                        rushMissStreak = 0;
                    } else {
                        groundCandidateIndex = baseGroundCandidateIndex;
                        verifiedHit = baseVerifiedHit;
                        placement = basePlacement;
                        pendingPlacementReady = basePendingReady;
                        pendingPlacement = basePendingPlacement;
                        targetYaw = baseTargetYaw;
                        targetPitch = baseTargetPitch;
                        pendingYawJitter = baseYawJitter;
                        rushMissStreak++;
                    }
                }
            } else {

                groundCandidateIndex = baseGroundCandidateIndex;
                verifiedHit = baseVerifiedHit;
                placement = basePlacement;
                pendingPlacementReady = basePendingReady;
                pendingPlacement = basePendingPlacement;
                targetYaw = baseTargetYaw;
                targetPitch = baseTargetPitch;
                pendingYawJitter = baseYawJitter;
                rushMissStreak++;
            }

            if (rushMissStreak >= 4) {
                rushLastPlacedTarget = null;
                rushWarmTarget = null;
                rushWarmFace = null;
                rushWarmTicks = 0;
                rushMissStreak = 0;
            }
        }

        sendRotations();
        placeSelectedBlock(groundCandidateIndex, verifiedHit, placement);
        if (!towering && isRushMode()) {
            runRushVerifiedChain(position, cameraYaw);
        }

        if (neededBlock != null && !placedThisTick && mc.thePlayer.onGround && !tellyJumping) {
            groundMissTicks++;
        } else {
            groundMissTicks = 0;
        }

        if (!towering) {
            updateKeepYPlacement(mc.thePlayer, position, motion);
        }

        autoClick(mc.thePlayer);
    }
    void scaffoldPrePlayerInput(PrePlayerInputEvent e) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (shouldPauseForGui()) {
            resetNinjaState();
            return;
        }
        if (isHypixelMode()) {
            applySourceSprintPolicy();
        } else {
            applySprintPolicy((isSnapPlusMode() || isHopMode()) ? false : shouldStopSprint());
        }
        boolean airTap = isLongTellyEnabled() && tellyJumping && !mc.thePlayer.onGround;
        if (airTap && e.getStrafe() == 0.0F && strafeTapCooldown <= 0 && RotationHelper.get().fixMovement()) {
            double cameraYawRadians = Math.toRadians(getPlayerYaw());
            double lateralMotion = mc.thePlayer.motionX * -Math.cos(cameraYawRadians) + mc.thePlayer.motionZ * -Math.sin(cameraYawRadians);
            if (Math.abs(lateralMotion) > 0.02) {
                e.setStrafe((lateralMotion > 0.0) ? 1.0F : -1.0F);
                strafeTapCooldown = 2 + (randomFloat(0.0F, 1.0F) < 0.4F ? 1 : 0);
            }
        }
        if (mc.thePlayer.onGround && isKeepYActive() && keepYStage > 0 && isMoving()) {
            e.setJump(true);
        }

        if (isTowerActive() && towerOwnsJump()) {
            e.setJump(false);
        }

        if (isRushMode() && rushAutoHop != null && rushAutoHop.isToggled()
                && !isTowerActive() && !isKeepYActive() && isMoving()) {
            applySprintPolicy(false);
            mc.thePlayer.setSprinting(true);
            if (mc.thePlayer.onGround && groundMissTicks == 0 && placementDelayTicks <= 0
                    && isValidBlock(getPlacementStack(mc.thePlayer))) {
                e.setJump(true);
            }
        }

        if (isHypixelMode() && !isTowerActive()) {
            String sourceStyle = getSourceScaffoldMode();
            boolean wantsAutoJump = sourceAutoJump != null && sourceAutoJump.isToggled();
            if (sourceStyle.equals("Telly")) wantsAutoJump = true;
            if (wantsAutoJump && isMoving() && mc.thePlayer.onGround
                    && isValidBlock(getPlacementStack(mc.thePlayer))) {
                e.setJump(true);
            } else if (sourceStyle.equals("God Bridge") && !wantsAutoJump
                    && sourceGodBridgeJumpBlocks >= 8 && mc.thePlayer.onGround) {

                e.setJump(true);
                sourceGodBridgeJumpBlocks = 0;
            }

            if (sourceStyle.equals("God Bridge")
                    && sourceSneak != null && sourceSneak.isToggled()) {
                // GodBridge Sneak now controls ALL automatic sneaking. With the
                // toggle off this branch leaves the user's own sneak input alone.
                boolean shouldAutoSneak = sourceDirectionalChange <= 10;
                int every = Math.max(1, (int) Math.round(sourceSneakEvery.getInput()));
                if (sourceSneakBlocks >= every) {
                    shouldAutoSneak = true;
                    sourceSneakBlocks = 0;
                }
                if (shouldAutoSneak) {
                    e.setSneak(true);
                }
            }

            if (sourceStyle.equals("Breezily")) {
                updateSourceBreezilyMovement(e);
            }
        }

        hopLaunchRequested = false;
        if (isHopMode() && !isTowerActive() && !isKeepYActive() && isMoving()
                && isValidBlock(getPlacementStack(mc.thePlayer))) {
            if (hopAutoSprint != null && hopAutoSprint.isToggled()) {
                applySprintPolicy(false);
                mc.thePlayer.setSprinting(true);
            }
            if (mc.thePlayer.onGround && groundMissTicks == 0 && placementDelayTicks <= 0) {
                e.setJump(false);
                hopLaunchRequested = true;
            }
        }

        if (!mc.thePlayer.onGround) {
            jumpHoldTicks = 0;
        }
        else if (e.isJump() && isLongTellyEnabled() && longTellySprintJump && jumpHoldTicks < SPRINT_JUMP_HOLD_TICKS
                && Math.abs(MathHelper.wrapAngleTo180_float(lastSentYaw - getPlayerYaw())) > SPRINT_JUMP_ALIGNMENT) {
            e.setJump(false);
            jumpHoldTicks++;
        }

        if (isNinjaMode()) {
            updateNinjaSneak(e);
        } else {
            resetNinjaState();
        }

        if (isBreezilyMode()) {
            if (!isBreezilyGodBridgeJump()) {
                updateBreezilyMovement(e);
            } else {

                breezilyTickCounter = 0;
            }
        } else if (!(isHypixelMode() && getSourceScaffoldMode().equals("Breezily"))) {
            resetBreezilyState();
        }
    }
    void onPostPlayerInput() {
        mc.sync();
        if (!Utils.nullCheck() || shouldPauseForGui()) {
            predictionInputPlayer = null;
            return;
        }

        updateTowerMotion();
        updateHopMotion();

        RotationHelperAdapter helper = RotationHelper.get();
        Float serverYaw = helper.getServerYaw();
        predictionInputYaw = helper.fixMovement() && serverYaw != null
                && !serverYaw.isNaN() && !serverYaw.isInfinite() ? serverYaw : mc.thePlayer.rotationYaw;
        predictionInputPlayer = mc.thePlayer;
    }
    void onPreMotion(PlayerState state) {
        mc.sync();
        if (!Utils.nullCheck() || shouldPauseForGui() || state == null) return;
        if (towerSpoofGroundThisTick && isTowerActive() && getTowerMode().startsWith("OnGround")) {
            state.onGround = true;
        }
    }

    void onPrePlayerInteract() {
        mc.sync();
        if (shouldPauseForGui()) return;
        updateSpoofedItem();
    }
    boolean onPacketSent(Object packet) {
        mc.sync();
        if (shouldPauseForGui()) return true;
        if (!(packet instanceof CPacket)) return true;
        boolean allowed = allowPacket((CPacket) packet);
        if (allowed && packet instanceof C08) {
            trackPlacedBlockVisual((C08) packet);
        }
        return allowed;
    }

    boolean onPacketReceived(Object packet) {

        return true;
    }

    boolean onMouse(int button, boolean state) {
        mc.sync();
        if (editPositionMode) {
            // The editor has a real GUI: its mouse is polled in onRenderTick.
            return false;
        }
        return button < 0 || allowMouse(button);
    }

    boolean onKeyPress(char typedChar, int keyCode) {
        if (editPositionMode && keyCode == 1) { // ESC
            closeCounterPositionEditor();
            return false;
        }
        return true;
    }

    void onRenderTick(float partialTicks) {
        restoreBreezilyCamera();
        mc.sync();
        onGuiUpdate(client.getScreen(), !client.getScreen().isEmpty());
        if (editPositionMode) {
            renderEditPosition();
            return;
        }
        if (!Utils.nullCheck()) {
            return;
        }
        if (mc.currentScreen != null) {
            fadeTimer = null;
            fadeInTimer = null;
            previousAlpha = 0.0f;
            return;
        }
        if (isEnabled() && showBlocks.isToggled()) {
            if (fadeTimer != null) {
                fadeTimer = null;
                (fadeInTimer = new Timer(150)).start();
            } else if (fadeInTimer == null && previousAlpha < 255.0f) {
                (fadeInTimer = new Timer(150)).start();
            }

            float alpha;
            if (fadeInTimer != null) {
                alpha = fadeInTimer.getValueFloat(10, 255, 1);
                if (alpha >= 255.0f) {
                    alpha = 255.0f;
                    fadeInTimer = null;
                }
            } else {
                alpha = 255.0f;
            }
            previousAlpha = alpha;

            renderBlockCount((int) alpha);
        } else {
            if (previousAlpha <= 10.0f) {
                fadeTimer = null;
                fadeInTimer = null;
                previousAlpha = 0.0f;
                return;
            }

            if (fadeTimer == null) {
                (fadeTimer = new Timer(150)).start();
                fadeInTimer = null;
            }

            float alpha = 255.0f - fadeTimer.getValueInt(0, 255, 1);
            if (alpha < 0.0f) {
                alpha = 0.0f;
            }
            previousAlpha = alpha;

            if (alpha > 10.0f) {
                renderBlockCount((int) alpha);
            }
        }
    }
    void onGuiUpdate(String screen, boolean opened) {

        tellyOnJump.setVisible(isTellyModeSelected(), this);
        editPosition.setVisible(showBlocks.isToggled(), this);
        boolean keepYEnabled = keepMode.getInput() != -1;
        keepYOnRightClick.setVisible(keepYEnabled, this);
        disableOnJumpPotion.setVisible(keepYEnabled, this);
        iceThreshold.setVisible(keepYEnabled, this);
        boolean ninja = isNinjaMode();
        ninjaEdgeDistance.setVisible(ninja, this);
        ninjaMinDelay.setVisible(ninja, this);
        ninjaMaxDelay.setVisible(ninja, this);
        ninjaSneakOnJump.setVisible(ninja, this);
        ninjaRequireSneakKey.setVisible(ninja, this);
        ninjaRequireBlocks.setVisible(ninja, this);
        ninjaRequireLookingDown.setVisible(ninja, this);
        ninjaDisableForward.setVisible(ninja, this);
        ninjaAvoidDoubleSneaking.setVisible(ninja, this);
        boolean source = isHypixelMode();
        boolean sourceBreezily = source && getSourceScaffoldMode().equals("Breezily");
        boolean breezily = isBreezilyMode() || sourceBreezily;
        breezilyAutoStrafe.setVisible(breezily, this);
        breezilyOnlyStrafeOnGround.setVisible(breezily, this);
        breezilyGroundWaitTicks.setVisible(breezily && breezilyOnlyStrafeOnGround.isToggled(), this);
        breezilySwitchSpeed.setVisible(breezily, this);

        boolean rush = isRushMode();
        rushLookAhead.setVisible(rush, this);
        rushChain.setVisible(rush, this);
        rushAutoHop.setVisible(rush, this);

        sourceScaffoldMode.setVisible(source, this);
        sourceSprintMode.setVisible(source, this);
        boolean sourceTelly = source && getSourceScaffoldMode().equals("Telly");
        sourceTellyWatchdog.setVisible(sourceTelly, this);
        sourceTellyStraightTicks.setVisible(sourceTelly, this);
        sourceTellyDiagonalTicks.setVisible(sourceTelly, this);
        sourceTellyJumpDownTicks.setVisible(sourceTelly, this);
        sourcePlaceDelay.setVisible(source, this);
        sourceAutoJump.setVisible(source && !sourceTelly, this);
        boolean sourceGodBridge = source && getSourceScaffoldMode().equals("God Bridge");
        sourceSneak.setVisible(sourceGodBridge, this);
        sourceSneakEvery.setVisible(sourceGodBridge && sourceSneak.isToggled(), this);

        boolean hop = isHopMode();
        hopMode.setVisible(hop, this);
        hopSpeed.setVisible(hop, this);
        hopAutoSprint.setVisible(hop, this);

        towerMode.setVisible(true, this);
    }
    public String getInfo() {
        return getScaffoldMode();
    }

    public boolean isAlwaysSprinting() {
        return isEnabled() && alwaysSprint.isToggled();
    }

    public int getSpoofedSlot() {
        if (!isEnabled() || !isSpoofItemEnabled() || !hasSwapped || originalSlot < 0 || originalSlot > 8) {
            return -1;
        }
        return originalSlot;
    }

    private boolean isSpoofItemEnabled() {
        return autoSwap != null && autoSwap.isToggled()
                && spoofItem != null && spoofItem.isToggled();
    }

    public float getPosX() {
        syncPosition();
        return posX;
    }

    public float getPosY() {
        syncPosition();
        return posY;
    }

    public float getRelativePosX() {
        syncPosition();
        return relativePosX;
    }

    public float getRelativePosY() {
        syncPosition();
        return relativePosY;
    }

    public void setRelativePosition(float normalizedX, float normalizedY) {
        int w = Math.max(1, client.getDisplaySize()[0]);
        int h = Math.max(1, client.getDisplaySize()[1]);
        setAbsolutePosition(normalizedX * w, normalizedY * h);
    }

    public void setAbsolutePosition(float absoluteX, float absoluteY) {
        setAbsolutePosition(absoluteX, absoluteY, new ScaledResolution(mc));
    }

    public void setAbsolutePosition(float absoluteX, float absoluteY, ScaledResolution resolution) {
        int w = Math.max(1, resolution.getScaledWidth());
        int h = Math.max(1, resolution.getScaledHeight());
        float x = MathHelper.clamp_float(absoluteX / w, 0.0F, 1.0F);
        float y = MathHelper.clamp_float(absoluteY / h, 0.0F, 1.0F);
        if (counterPositionX != null) modules.setSlider(scriptName, counterPositionX.name, Math.round(x * 1000.0F) / 10.0D);
        if (counterPositionY != null) modules.setSlider(scriptName, counterPositionY.name, Math.round(y * 1000.0F) / 10.0D);
        syncPosition(resolution);
    }

    public void resetPosition() {
        if (counterPositionX != null) modules.setSlider(scriptName, counterPositionX.name, 45.0D);
        if (counterPositionY != null) modules.setSlider(scriptName, counterPositionY.name, 65.0D);
        syncPosition();
    }

    public int getTotalBlocks() {
        int totalBlocks = 0;
        if (mc.thePlayer == null || mc.thePlayer.inventory == null) {
            return 0;
        }
        for (final ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (isValidBlock(stack)) {
                totalBlocks += stack.stackSize;
            }
        }
        return totalBlocks;
    }

    private boolean shouldPauseForGui() {
        // The counter editor must always pause scaffold, regardless of Disable in GUI.
        return editPositionMode || (disableInGui != null && disableInGui.isToggled()
                && mc.currentScreen != null);
    }

    private void pauseForGui() {
        resetNinjaState();
        resetBreezilyState();
        rotationSentThisTick = false;
        rotationSentLastTick = false;
        RotationHelper.get().forceMovementFix = false;
        client.disableMovementFix();
        movementFixActive = false;

        restoreAutoSwapSlot();
        originalSlot = -1;
        placementSlot = -1;
        hasSwapped = false;
        physicalSlotSwapped = false;

        if (sprintSuppressed) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(),
                    sprintWasPressed || Utils.isBindDown(mc.gameSettings.keyBindSprint));
            sprintSuppressed = false;
        }
        resetMovementState();
        snapReturnPending = false;
        snapSkipPlacementThisTick = false;
        snapDiagonalHoldTicks = 0;
        snapLastPlacementYaw = Float.NaN;
        breezilyViewYaw = Float.NaN;
        breezilyViewPitch = Float.NaN;
        setServerRelativeMovementInputs(false);
        setItemRendererSpoofFlags(false);
    }

    private void setSpoofServerSlot(int slot) {
        if (mc.thePlayer == null || slot < 0 || slot > 8 || spoofServerSlot == slot) {
            return;
        }

        sendingSpoofSlotPacket = true;
        try {
            client.sendPacket(new C09(slot));
            spoofServerSlot = slot;
        } finally {
            sendingSpoofSlotPacket = false;
        }
    }

    private void restoreSpoofServerSlot() {
        if (mc.thePlayer == null) {
            spoofServerSlot = -1;
            sendingSpoofSlotPacket = false;
            return;
        }

        int visibleSlot = inventory.getSlot();
        if (visibleSlot < 0 || visibleSlot > 8) {
            visibleSlot = mc.thePlayer.inventory.currentItem;
        }

        if (spoofServerSlot >= 0 && visibleSlot >= 0 && visibleSlot <= 8
                && spoofServerSlot != visibleSlot) {
            sendingSpoofSlotPacket = true;
            try {
                client.sendPacket(new C09(visibleSlot));
            } finally {
                sendingSpoofSlotPacket = false;
            }
        }
        spoofServerSlot = -1;
    }

    private void updateSpoofedItem() {
        if (mc.thePlayer == null) {
            spoofServerSlot = -1;
            sendingSpoofSlotPacket = false;
            setItemRendererSpoofFlags(false);
            return;
        }

        if (isSpoofItemEnabled()) {
            // Modern silent-hotbar style: keep one server-side block slot active
            // instead of swapping to the block and back for every placement.
            if (physicalSlotSwapped && originalSlot >= 0 && originalSlot <= 8
                    && mc.thePlayer.inventory.currentItem != originalSlot) {
                inventory.setSlot(originalSlot);
                mc.thePlayer.inventory.currentItem = originalSlot;
            }
            physicalSlotSwapped = false;

            if (hasSwapped && placementSlot >= 0 && placementSlot <= 8
                    && placementSlot != mc.thePlayer.inventory.currentItem) {
                setSpoofServerSlot(placementSlot);
            }
            setItemRendererSpoofFlags(false);
            return;
        }

        // Leaving spoof mode: put the server back on the actually visible slot once.
        if (spoofServerSlot >= 0) {
            restoreSpoofServerSlot();
        }

        // If AutoSwap is still active, fall back cleanly to the normal physical swap.
        if (hasSwapped && !physicalSlotSwapped && placementSlot >= 0 && placementSlot <= 8
                && placementSlot != mc.thePlayer.inventory.currentItem) {
            originalSlot = mc.thePlayer.inventory.currentItem;
            inventory.setSlot(placementSlot);
            mc.thePlayer.inventory.currentItem = placementSlot;
            mc.thePlayer.inventory.sync();
            physicalSlotSwapped = true;
        }

        setItemRendererSpoofFlags(false);
    }

    private void resolveSpoofRenderer() {
        if (spoofRendererResolved) return;
        spoofRendererResolved = true;
        try {
            Class<?> defaults = Class.forName("keystrokesmod.script.ScriptDefaults");
            java.lang.reflect.Field mcField = defaults.getDeclaredField("mc");
            mcField.setAccessible(true);
            Object nativeMinecraft = mcField.get(null);
            if (nativeMinecraft == null) return;
            java.lang.reflect.Method getItemRenderer = nativeMinecraft.getClass().getMethod("getItemRenderer");
            Object rendererObject = getItemRenderer.invoke(nativeMinecraft);
            if (rendererObject == null) return;
            java.lang.reflect.Method cancelUpdate = rendererObject.getClass().getMethod("setCancelUpdate", Boolean.TYPE);
            java.lang.reflect.Method cancelReset = rendererObject.getClass().getMethod("setCancelReset", Boolean.TYPE);
            spoofRendererObject = rendererObject;
            spoofRendererCancelUpdate = cancelUpdate;
            spoofRendererCancelReset = cancelReset;
        } catch (Throwable ignored) {
            spoofRendererObject = null;
            spoofRendererCancelUpdate = null;
            spoofRendererCancelReset = null;
        }
    }

    private void setItemRendererSpoofFlags(boolean active) {
        resolveSpoofRenderer();
        if (spoofRendererObject == null || spoofRendererCancelUpdate == null || spoofRendererCancelReset == null) {
            return;
        }
        try {
            spoofRendererCancelUpdate.invoke(spoofRendererObject, Boolean.valueOf(active));
            spoofRendererCancelReset.invoke(spoofRendererObject, Boolean.valueOf(active));
        } catch (Throwable ignored) {
            spoofRendererObject = null;
            spoofRendererCancelUpdate = null;
            spoofRendererCancelReset = null;
            spoofRendererResolved = false;
        }
    }

    private void restoreAutoSwapSlot() {
        if (mc.thePlayer == null) {
            placementSlot = -1;
            hasSwapped = false;
            physicalSlotSwapped = false;
            spoofServerSlot = -1;
            sendingSpoofSlotPacket = false;
            setItemRendererSpoofFlags(false);
            return;
        }

        // A spoof session keeps the server on the block slot. Restore it exactly
        // once to the slot the user is actually looking at.
        if (spoofServerSlot >= 0) {
            restoreSpoofServerSlot();
        }

        setItemRendererSpoofFlags(false);
        if (physicalSlotSwapped && originalSlot >= 0 && originalSlot <= 8
                && originalSlot != mc.thePlayer.inventory.currentItem) {
            inventory.setSlot(originalSlot);
            mc.thePlayer.inventory.currentItem = originalSlot;
            mc.thePlayer.inventory.sync();
        }

        placementSlot = -1;
        hasSwapped = false;
        physicalSlotSwapped = false;
        originalSlot = mc.thePlayer.inventory.currentItem;
    }

    private ItemStack getPlacementStack(EntityPlayerSP player) {
        if (player == null) return null;
        if (autoSwap != null && autoSwap.isToggled() && hasSwapped && placementSlot >= 0 && placementSlot <= 8) {
            ItemStack stack = player.inventory.getStackInSlot(placementSlot);
            if (isValidBlock(stack)) {
                return stack;
            }
        }
        return player.getHeldItem();
    }

    private int getPlacementSlot(EntityPlayerSP player) {
        if (player == null) return -1;
        if (autoSwap != null && autoSwap.isToggled() && hasSwapped && placementSlot >= 0 && placementSlot <= 8) {
            return placementSlot;
        }
        return player.inventory.currentItem;
    }

    private boolean isValidBlock(ItemStack stack) {
        if (stack == null || stack.stackSize <= 0 || !stack.isBlock || stack.name == null) {
            return false;
        }
        String name = stack.name.toLowerCase();
        String type = stack.type == null ? "" : stack.type;
        if (UNPLACEABLE_BLOCK_TYPES.contains("," + type + ",")) return false;
        if (UNPLACEABLE_BLOCK_NAMES.contains("," + name + ",")) return false;
        return true;
    }

    private void swapSlot(int slot) {
        if (mc.thePlayer == null || slot < 0 || slot > 8
                || !isValidBlock(mc.thePlayer.inventory.getStackInSlot(slot))) {
            return;
        }

        int visibleSlot = mc.thePlayer.inventory.currentItem;
        if (!hasSwapped || isSpoofItemEnabled()) {
            originalSlot = visibleSlot;
        }
        placementSlot = slot;

        if (slot == visibleSlot) {
            if (spoofServerSlot >= 0) {
                restoreSpoofServerSlot();
            }
            hasSwapped = false;
            physicalSlotSwapped = false;
            setItemRendererSpoofFlags(false);
            return;
        }

        hasSwapped = true;
        if (isSpoofItemEnabled()) {
            // Select the block on the server once and keep the user's visible slot
            // untouched between placements. Duplicate vanilla C09s are filtered in
            // allowPacket(), so there is no block-slot -> visible-slot pair per click.
            physicalSlotSwapped = false;
            setSpoofServerSlot(slot);
            setItemRendererSpoofFlags(false);
            return;
        }

        inventory.setSlot(slot);
        mc.thePlayer.inventory.currentItem = slot;
        mc.thePlayer.inventory.sync();
        physicalSlotSwapped = true;
    }

    private boolean selectPlacementStack(EntityPlayerSP player) {
        if (player == null) return false;

        if (!autoSwap.isToggled()) {
            if (hasSwapped) {
                restoreAutoSwapSlot();
            }
            heldBlockCount = getBlockCount(player.getHeldItem());
            return heldBlockCount > 0;
        }

        updateSpoofedItem();
        if (isSpoofItemEnabled() && !physicalSlotSwapped) {
            originalSlot = player.inventory.currentItem;
        }
        ItemStack activeStack = getPlacementStack(player);
        heldBlockCount = getBlockCount(activeStack);

        if (heldBlockCount <= 0) {
            int startSlot = getPlacementSlot(player);
            if (startSlot < 0) startSlot = player.inventory.currentItem;
            int slot = startSlot - 1;
            for (int i = slot; i > slot - 9; i--) {
                int hotbarSlot = ((i % 9) + 9) % 9;
                ItemStack candidateStack = player.inventory.getStackInSlot(hotbarSlot);
                if (isValidBlock(candidateStack)) {
                    swapSlot(hotbarSlot);
                    activeStack = getPlacementStack(player);
                    heldBlockCount = getBlockCount(activeStack);
                    break;
                }
            }
        }

        if (isIceSelectionEnabled() && isKeepYActive()
                && (tellyJumping || isTellyTakeoff() || keepYStage > 0 || isMoving())) {
            int preferredSlot = findPreferredIceSlot(player);
            if (preferredSlot >= 0 && preferredSlot != getPlacementSlot(player)) {
                swapSlot(preferredSlot);
                activeStack = getPlacementStack(player);
                heldBlockCount = getBlockCount(activeStack);
            }
        }

        return heldBlockCount > 0;
    }

    private int findPreferredIceSlot(EntityPlayerSP player) {
        int iceSlot = -1;
        int normalSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!isValidBlock(stack)) {
                continue;
            }
            String blockName = stack.name == null ? "" : stack.name.toLowerCase();
            if (blockName.equals("packed_ice") || blockName.equals("ice")) {
                if (iceSlot < 0) {
                    iceSlot = i;
                }
            } else if (normalSlot < 0) {
                normalSlot = i;
            }
        }
        if (iceSlot < 0) {
            return -1;
        }
        boolean wantIce = player.motionY < iceThreshold.getInput() || player.onGround;
        return wantIce || normalSlot < 0 ? iceSlot : normalSlot;
    }

    private int getBlockCount(ItemStack stack) {
        return isValidBlock(stack) ? stack.stackSize : 0;
    }

    private void resetMovementState() {
        predictionInputPlayer = null;
        predictionInputYaw = Float.NaN;
        activationTicks = 0;
        groundPlacementSide = 1;
        movingCardinally = false;
        lookSide = 1;
        lookingStraightBack = false;
        jumpHoldTicks = 0;
        activationYawAccumulator = 0.0F;

        rotationEaseRate = 1.0F;
        rotationEaseStarted = false;
        groundMissTicks = 0;
        hasCameraYaw = false;
        rotationEaseSlowdown = 0.0F;
        tellyJumping = false;
        takeoffLatched = false;
        sprintStopRequested = false;
        sprintToggleCooldown = 0;
        lastYawBucket = 0;
        diagonalMovement = false;
        resetNinjaState();
        resetBreezilyState();
        longTellySprintJump = true;
        longTellyWasAirborne = false;
        longTellyNeedsSideUpdate = true;
        longTellyWasEngaged = false;
        previousVerticalMotion = 0.0;
        ticksSinceGrounded = 99;
        hasLongTellyOrigin = false;
        resetLongTellyQueue();

        strafeTapCooldown = 0;
        pendingPlacementReady = false;
        verificationDelayTicks = 0;
        hypixelPlaceTimer = 0;
        hypixelSourceYaw = Float.NaN;
        hypixelSourcePitch = 90.0F;
    }

    private void resetLongTellyQueue() {
        queuedBlockCount = 0;
        pendingQueueIndex = -1;
        skippedQueueMask = 0;
        Arrays.fill(queueRetryTicks, 0);
        queueCursor = 0;
        queueStallTicks = 0;
    }

    private void autoClick(EntityPlayerSP player) {
        ItemStack placementStack = getPlacementStack(player);
        if (player == null || !isValidBlock(placementStack)) {
            return;
        }
        float configuredCps = (float) clickSpeed.getInput();
        if (configuredCps < 1.0F) {
            clickState = 'D';
            return;
        }
        if (mc.currentScreen != null) {
            clickState = 'S';
            return;
        }
        if (placementAttemptsThisTick > 0) {
            clickState = 'P';
            return;
        }
        long now = client.time();
        if (now < nextClickAt) {
            clickState = 'w';
            return;
        }
        if (nextClickAt < now - 500L) {
            nextClickAt = now;
        }
        clickState = 'C';
        sendingAutoClick = true;
        try {
            client.sendPacket(new C08(placementStack, new Vec3(-1, -1, -1), 255, new Vec3(0.0, 0.0, 0.0)));
        } catch (Throwable t) {
            clickState = 'E';
        } finally {
            sendingAutoClick = false;
        }

        float cps = configuredCps * randomFloat(0.88F, 1.12F);
        if (cps < 1.0F) {
            cps = 1.0F;
        }
        float clickInterval = 1000.0F / cps;

        if (randomFloat(0.0F, 1.0F) < 0.10F) {
            clickInterval *= randomFloat(1.5F, 2.4F);
        }
        nextClickAt += (long) clickInterval;
        if (nextClickAt < now) {
            nextClickAt = now + (long) clickInterval;
        }
        clickCount++;
        if (now - clickWindowStart >= 1000L) {
            long elapsedMillis = now - clickWindowStart;
            measuredClickCps = (float) clickCount * 1000.0F / (float) Math.max(1L, elapsedMillis);
            clickCount = 0;
            clickWindowStart = now;
        }
    }

    private float updateCameraYaw(float rawCameraYaw) {
        if (!hasCameraYaw) {
            continuousCameraYaw = rawCameraYaw;
            hasCameraYaw = true;
        } else {
            continuousCameraYaw += MathHelper.wrapAngleTo180_float(rawCameraYaw - continuousCameraYaw);
        }
        return continuousCameraYaw;
    }

    private void beginTick() {
        placedThisTick = false;
        placementAttemptsThisTick = 0;
        snapSkipPlacementThisTick = false;
        if (snapDiagonalHoldTicks > 0) {
            snapDiagonalHoldTicks--;
        }
        hasPrediction = false;
        updateYawJitter();
        rotationEaseRate = MathHelper.clamp_float(
                rotationEaseRate + randomFloat(-0.01F, 0.01F), 0.97F, 1.03F);
        if (activationTicks < 1000) {
            activationTicks++;
        }
        debugAimSelection = 0;
        debugAimScore = -1;
        pendingPlacementReady = false;
        skippedQueueMask = 0;
        for (int q = 0; q < queuedBlockCount; q++) {
            if (queueRetryTicks[q] > 0 && --queueRetryTicks[q] > 0) {
                skippedQueueMask |= 1 << q;
            }
        }
        rotationSentLastTick = rotationSentThisTick;
        rotationSentThisTick = false;
        if (placementDelayTicks > 0) {
            placementDelayTicks--;
        }
    }

    private void updateKeepYState(EntityPlayerSP player, Vec3 position) {
        if (!isKeepYActive()) {
            keepYStage = 0;
            useCurrentHeight = false;
            keepYStartHeight = MathHelper.floor_double(position.y);
        }

        if (player.onGround) {
            if (keepYStage != 0) {
                keepYStage += (keepYStage > 0) ? -1 : 1;
            }
            if (keepYStage == 0
                    && isKeepYActive()
                    && isMoving()
                    && (!disableOnJumpPotion.isToggled() || !hasJumpPotion())
                    && !isJumpPressed()) {
                keepYStage = 1;
            }
            keepYStartHeight = useCurrentHeight ? keepYStartHeight : MathHelper.floor_double(position.y);
            if (getActiveKeepYMode().equals("Telly A")) {
                useCurrentHeight = false;
            } else if (!getActiveKeepYMode().equals("Telly B")) {
                useCurrentHeight = false;
            }
            tellyJumping = false;
        }
    }

    private boolean updateLongTellyState(EntityPlayerSP player, Vec3 position) {
        if (tellyOnJump.isToggled() && isTellyModeSelected()) {
            if (!isJumpPressed() && !(isKeepYActive() && keepYStage > 0)) {
                tellyJumping = false;
                takeoffLatched = false;
            } else if (isJumpPressed() && player.onGround && isMoving()) {

                takeoffLatched = false;
            }
        }
        if (isLongTellyEnabled() && !player.onGround && (longTellyWasEngaged || tellyJumping)) {
            tellyJumping = true;
        }
        boolean longTellyEngaged = isLongTellyEnabled() && (tellyJumping || isManualTellyTakeoff());
        if (longTellyEngaged && !player.onGround) {
            longTellyMissTicks++;
        } else {
            longTellyMissTicks = 0;
        }
        if (verificationDelayTicks > 0 && --verificationDelayTicks == 0) {
            if (isReplaceableAt(verificationBlock)) {
                revertedPlacementCount++;
                skippedQueueMask = 0;
                Arrays.fill(queueRetryTicks, 0);
                queueStallTicks = 0;
                if (revertedPlacementCount == 3 && isBasicDebugEnabled()) {
                    printDebug("&cplacements are being reverted &7- the server is"
                            + " rejecting them (ghost blocks), not the script");
                }
            }
        }
        if (player.onGround) {
            lastGroundX = MathHelper.floor_double(position.x);
            lastGroundZ = MathHelper.floor_double(position.z);
            ticksSinceGrounded = 0;
        } else if (ticksSinceGrounded < 99) {
            ticksSinceGrounded++;
        }
        if (longTellyEngaged && !longTellyWasEngaged) {
            longTellySprintJump = true;
            longTellyWasAirborne = false;
            longTellyNeedsSideUpdate = true;
            hasLongTellyOrigin = false;
            queuedBlockCount = 0;
            pendingQueueIndex = -1;
        }
        longTellyWasEngaged = longTellyEngaged;
        if (strafeTapCooldown > 0) {
            strafeTapCooldown--;
        }
        Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
        if (longTellyEngaged) {
            if (player.onGround && longTellyWasAirborne) {
                longTellyWasAirborne = false;
                longTellySprintJump = !longTellySprintJump;
                longTellyNeedsSideUpdate = longTellySprintJump;
                buildLongTellyQueue(motion);
                if (!longTellySprintJump) {
                    placementDelayTicks = 0;
                }
            }
            if (!player.onGround) {
                longTellyWasAirborne = true;
            }
            if (motion.y > 0.3 && previousVerticalMotion <= 0.05) {
                buildLongTellyQueue(motion);
                if (isBasicDebugEnabled()) {
                    printDebug("&7lt jump: &f" + (longTellySprintJump ? "SPRINT" : "NORMAL")
                            + " &7q=&f" + queuedBlockCount + " &7side=&f"
                            + (longTellySide > 0 ? "R" : "L"));
                }
            }
        } else {
            queuedBlockCount = 0;
            pendingQueueIndex = -1;
        }
        previousVerticalMotion = motion.y;
        return longTellyEngaged;
    }

    private void aimAtPlacement(Placement placement, float referenceServerYaw, EntityPlayerSP player, Vec3 position) {
        if (placement == null) {
            return;
        }
        BlockPos blockPosition = placement.support;
        EnumFacing face = placement.face;
        double[] xOffsets = getFaceOffsets(face.getFrontOffsetX());
        double[] yOffsets = getFaceOffsets(face.getFrontOffsetY());
        double[] zOffsets = getFaceOffsets(face.getFrontOffsetZ());
        float[] bestRotation = null;
        float bestRotationDifference = 0.0F;
        float baseYaw = unwrapYaw(yaw, referenceServerYaw);
        float referenceYaw = rotationSentLastTick ? lastSentYaw : baseYaw;
        float referencePitch = rotationSentLastTick ? lastSentPitch : pitch;
        for (double dx : xOffsets) {
            for (double dy : yOffsets) {
                for (double dz : zOffsets) {
                    double relX = (double) blockPosition.getX() + dx - position.x;
                    double relY = (double) blockPosition.getY() + dy - position.y - (double) player.getEyeHeight();
                    double relZ = (double) blockPosition.getZ() + dz - position.z;
                    float[] rotation = getRotationsToOffset(relX, relY, relZ, baseYaw, pitch);
                    float rotationDifference = Math.abs(MathHelper.wrapAngleTo180_float(rotation[0] - referenceYaw)) + Math.abs(rotation[1] - referencePitch);
                    if (bestRotation != null && rotationDifference >= bestRotationDifference) {
                        continue;
                    }
                    Vec3 hit = raycastFace(placement, rotation[0], rotation[1]);
                    if (hit != null) {
                        bestRotation = rotation;
                        bestRotationDifference = rotationDifference;
                    }
                }
            }
        }
        if (bestRotation != null) {
            yaw = bestRotation[0];
            pitch = bestRotation[1];
            canRotate = true;
        }
    }

    private double[] getFaceOffsets(int direction) {
        return direction < 0 ? FACE_MIN_OFFSET : direction > 0 ? FACE_MAX_OFFSET : FACE_SAMPLE_OFFSETS;
    }

    private void aimYaw(float value) {
        targetYaw = value;
        pendingYawJitter = 0.0F;
    }

    private void aimYawWithJitter(float base, float yawJitter) {
        targetYaw = base + yawJitter;
        pendingYawJitter = yawJitter;
    }

    private void aimPitch(float value) {
        targetPitch = value;
    }

    private void updateTargetRotations(Vec3 position, Vec3 motion, float previousYaw, float previousPitch, boolean urgent, boolean shortDiagonal) {
        if (!urgent) {
            float[] eased = easeRotations(previousYaw, previousPitch, yaw, pitch);
            aimYaw(eased[0]);
            aimPitch(eased[1]);
        } else {
            aimYaw(yaw);
            aimPitch(pitch);

            rotationEaseStarted = false;
            rotationEaseSlowdown = 0.0F;
        }
        if (tellyJumping && (motion.y > 0.0 || position.y > (double) (keepYStartHeight + 1))) {
            float yawDiff = MathHelper.wrapAngleTo180_float(yaw - previousYaw);
            float rotationLimit = (placementDelayTicks >= 2)
                    ? randomFloat(TAKEOFF_YAW_MIN, TAKEOFF_YAW_MAX)
                    : randomFloat(AIRBORNE_YAW_MIN, AIRBORNE_YAW_MAX);
            if (Math.abs(yawDiff) > rotationLimit) {
                aimYaw(previousYaw + clampRotationDelta(yawDiff, rotationLimit));
                if (!shortDiagonal && !(isLongTellyEnabled() && queuedBlockCount > 0)) {
                    placementDelayTicks = Math.max(placementDelayTicks, 1);
                }
            }
        }
    }

    private int updateLookBucket(Vec3 position, float cameraYaw) {
        int backBucket = Math.round(MathHelper.wrapAngleTo180_float(cameraYaw + 180.0F - cameraYaw) / 45.0F);
        int currentBucket = Math.round(MathHelper.wrapAngleTo180_float(lastSentYaw - cameraYaw) / 45.0F);
        while (backBucket - currentBucket > 4) {
            backBucket -= 8;
        }
        while (backBucket - currentBucket < -4) {
            backBucket += 8;
        }
        double blockCenterX = Math.floor(position.x) + 0.5;
        double blockCenterZ = Math.floor(position.z) + 0.5;
        double lateralOffset = (position.x - blockCenterX) * (double) (-longTellyForwardZ)
                + (position.z - blockCenterZ) * (double) longTellyForwardX;
        if (lateralOffset > 0.12) {
            lookSide = 1;
        } else if (lateralOffset < -0.12) {
            lookSide = -1;
        }
        if (currentBucket == backBucket) {
            lookingStraightBack = true;
        }

        return lookingStraightBack ? backBucket : backBucket + LOOK_DIAGONAL_BUCKETS * lookSide;
    }

    private boolean alignTakeoffRotations(boolean tellyRotating, float cameraYaw) {
        boolean cameraAligned = false;
        if (tellyRotating && isTellyTakeoff() && (!isLongTellyEnabled() || longTellySprintJump)) {
            cameraAligned = true;
            lookingStraightBack = false;
            aimYawWithJitter(cameraYaw, jitter(0.12F));
            if (takeoffPitch < 30.0F || takeoffPitch > 89.5F) {
                takeoffPitch = RotationUtils.clampPitch(lastSentPitch);
            }
            takeoffPitch += randomFloat(-0.5F, 0.5F);
            takeoffPitch = MathHelper.clamp_float(takeoffPitch, 30.0F, 89.0F);
            takeoffPitch += (RotationUtils.clampPitch(lastSentPitch) - takeoffPitch) * 0.25F;
            aimPitch(takeoffPitch);

            if (isManualTellyTakeoff()) {
                placementDelayTicks = 2;
                takeoffLatched = true;
            } else if (!takeoffLatched) {
                placementDelayTicks = Math.max(placementDelayTicks, 2);
                takeoffLatched = true;
            }
            tellyJumping = true;
        }
        if (!cameraAligned) {
            takeoffLatched = false;
        }
        return cameraAligned;
    }

    private void snapGroundYaw(boolean tellyRotating, Vec3 position, Vec3 motion, float cameraYaw) {
        if (!tellyRotating) {
            float snapBase = targetYaw;
            double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            if (horizontalSpeed >= 0.02) {
                float travelYaw = (float) Math.toDegrees(
                        Math.atan2(-motion.x, motion.z));
                float cardinalOffset = Math.abs(MathHelper.wrapAngleTo180_float(travelYaw
                        - (float) Math.round(travelYaw / 90.0F) * 90.0F));
                if (movingCardinally) {
                    if (cardinalOffset > 27.0F) {
                        movingCardinally = false;
                    }
                } else if (cardinalOffset < 18.0F) {
                    movingCardinally = true;
                }
            } else {
                movingCardinally = false;
            }
            if (horizontalSpeed >= 0.02 && movingCardinally) {
                double blockCenterX = Math.floor(position.x) + 0.5;
                double blockCenterZ = Math.floor(position.z) + 0.5;
                double lateralOffset = ((position.x - blockCenterX) * -motion.z
                        + (position.z - blockCenterZ) * motion.x) / horizontalSpeed;
                if (lateralOffset > 0.12) {
                    groundPlacementSide = 1;
                } else if (lateralOffset < -0.12) {
                    groundPlacementSide = -1;
                }
                snapBase = (float) Math.toDegrees(
                        Math.atan2(motion.x, -motion.z))
                        + (float) (GROUND_DIAGONAL_BUCKETS * groundPlacementSide) * 45.0F;
            } else if (horizontalSpeed < 0.02) {
                snapBase = cameraYaw + (float) lastYawBucket * 45.0F;
            }
            float holdAngle = GROUND_YAW_HOLD_ANGLE;
            if (!movingCardinally) {
                holdAngle = 29.5F;
            } else if (horizontalSpeed >= 0.02 && groundMissTicks >= 2) {
                holdAngle = 22.5F;
            }
            float snapDelta = MathHelper.wrapAngleTo180_float(snapBase - cameraYaw);
            int bucket = Math.round(snapDelta / 45.0F);
            if (bucket != lastYawBucket
                    && Math.abs(MathHelper.wrapAngleTo180_float(snapDelta - (float) lastYawBucket * 45.0F))
                    <= holdAngle) {
                bucket = lastYawBucket;
            }
            lastYawBucket = bucket;
            aimYaw(cameraYaw + (float) bucket * 45.0F);
        } else {
            lastYawBucket = Math.round(MathHelper.wrapAngleTo180_float(targetYaw - cameraYaw) / 45.0F);
        }
    }

    private int aimGroundPlacement(EntityPlayerSP player, Vec3 position, boolean urgent, float previousPitch) {
        float cameraYaw = yawGridBase;
        int groundY = MathHelper.floor_double(position.y) - 1;
        findNeededBlock(groundY);
        int candidateCount = buildCandidates(groundY);
        debugCandidateCount = candidateCount;
        if (neededBlock != null) {
            for (int corner = 0; corner < 4 && !urgent; corner++) {
                double cornerX = position.x + (((corner & 1) == 0) ? -0.3 : 0.3);
                double cornerZ = position.z + (((corner & 2) == 0) ? -0.3 : 0.3);
                if (MathHelper.floor_double(cornerX) == neededBlock.getX() && MathHelper.floor_double(cornerZ) == neededBlock.getZ()) {
                    urgent = true;
                }
            }
        }
        if (candidateCount == 0) {
            return -1;
        }
        float bestPitch = Float.NaN;
        float bestScanPitch = 0.0F;
        int bestScore = -1;
        debugAimSelection = 0;
        debugAimScore = -1;
        boolean holdingPitch = false;

        if (rotationSentLastTick) {
            int previousCandidateIndex = matchCandidate(candidateCount, lastBaseYaw, lastBasePitch);
            if (previousCandidateIndex >= 0 && getCandidateScore(previousCandidateIndex) >= 2
                    && matchCandidate(candidateCount, lastBaseYaw + 0.5F,
                    lastBasePitch) >= 0
                    && matchCandidate(candidateCount, lastBaseYaw - 0.5F,
                    lastBasePitch) >= 0
                    && matchCandidate(candidateCount, lastBaseYaw,
                    RotationUtils.clampPitch(lastBasePitch - 0.35F)) >= 0
                    && matchCandidate(candidateCount, lastBaseYaw,
                    RotationUtils.clampPitch(lastBasePitch + 0.35F)) >= 0) {
                aimYawWithJitter(lastBaseYaw, nextYawJitter());
                holdingPitch = true;
                bestPitch = lastBasePitch;
                bestScore = 2;
                bestScanPitch = 0.0F;
                debugAimSelection = 6;
                debugAimScore = 2;
            }
        }
        int heldCandidateIndex = holdingPitch ? -1
                : matchCandidate(candidateCount, targetYaw, previousPitch);
        if (heldCandidateIndex >= 0 && getCandidateScore(heldCandidateIndex) >= 2
                && matchCandidate(candidateCount, targetYaw,
                RotationUtils.clampPitch(previousPitch - 0.35F)) >= 0
                && matchCandidate(candidateCount, targetYaw,
                RotationUtils.clampPitch(previousPitch + 0.35F)) >= 0) {
            holdingPitch = true;
            bestPitch = previousPitch;
            bestScore = 2;
            bestScanPitch = 0.0F;
            debugAimSelection = 5;
            debugAimScore = 2;
        }
        if (neededBlock != null && !holdingPitch) {
            int[] yawBuckets = getGroundYawBuckets(player, position, cameraYaw);
            analytic:
            for (int pass = 2; pass >= 1; pass--) {
                for (int bucketIndex = 0; bucketIndex < yawBuckets.length; bucketIndex++) {
                    float yawJitter = (bucketIndex == 0) ? pendingYawJitter : nextYawJitter();
                    float candidateYaw = (bucketIndex == 0) ? targetYaw
                            : cameraYaw + (float) yawBuckets[bucketIndex] * 45.0F
                            + yawJitter;
                    for (int i = 0; i < candidateCount; i++) {
                        if (getCandidateScore(i) != pass) {
                            continue;
                        }
                        float candidatePitch = findFacePitch(candidates[i], candidateYaw);
                        if (!Float.isNaN(candidatePitch)) {
                            bestPitch = candidatePitch;
                            bestScore = pass;
                            if (bucketIndex > 0) {
                                aimYawWithJitter(candidateYaw - yawJitter, yawJitter);
                                lastYawBucket = yawBuckets[bucketIndex];
                            }
                            debugAimSelection = 1;
                            debugAimScore = pass;
                            break analytic;
                        }
                    }
                }
            }
        }
        int maxScore = neededBlock != null ? 2 : 0;
        float scanPitch = 60.0F;
        while (Float.isNaN(bestPitch) || bestScore < maxScore) {
            float candidatePitch = Math.min(scanPitch, 90.0F);
            int matchedIndex = matchCandidate(candidateCount, targetYaw, quantizeRotation(candidatePitch));
            if (matchedIndex >= 0) {
                int score = getCandidateScore(matchedIndex);
                if (score > bestScore || (score == bestScore && candidatePitch < bestScanPitch)) {
                    bestPitch = candidatePitch;
                    bestScanPitch = candidatePitch;
                    bestScore = score;
                    debugAimSelection = 2;
                    debugAimScore = score;
                }
            }
            if (scanPitch >= 90.0F) {
                break;
            }

            float pitchStep = (scanPitch >= 84.0F) ? (0.3F + randomFloat(0.0F, 0.08F))
                    : (1.0F + randomFloat(-0.38F, 0.38F));
            scanPitch += pitchStep;
        }
        if (holdingPitch) {
            pitch = previousPitch;
            aimPitch(RotationUtils.clampPitch(previousPitch));
        } else if (!Float.isNaN(bestPitch)) {
            pitch = quantizeRotation(bestPitch);
            if (!urgent) {
                aimPitch(easeRotations(targetYaw, previousPitch, targetYaw,
                        pitch)[1]);
            } else {
                aimPitch(pitch);
            }
            aimPitch(RotationUtils.clampPitch(targetPitch));
        }
        int groundCandidateIndex = matchCandidate(candidateCount, targetYaw, targetPitch);
        if (groundCandidateIndex < 0 && !Float.isNaN(bestPitch)) {
            aimPitch(RotationUtils.clampPitch(bestPitch));
            groundCandidateIndex = matchCandidate(candidateCount, targetYaw, targetPitch);
        }
        return groundCandidateIndex;
    }

    private int[] getGroundYawBuckets(EntityPlayerSP player, Vec3 position, float cameraYaw) {
        int[] allBuckets = new int[]{lastYawBucket, lastYawBucket - 1,
                lastYawBucket + 1, lastYawBucket - 2, lastYawBucket + 2,
                lastYawBucket - 3, lastYawBucket + 3, lastYawBucket + 4};
        Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
        double horizontalSpeed = Utils.getHorizontalSpeed(player);
        int preferredBucket = lastYawBucket;
        if (horizontalSpeed >= 0.02) {
            double blockCenterX = Math.floor(position.x) + 0.5;
            double blockCenterZ = Math.floor(position.z) + 0.5;
            double lateralOffset = ((position.x - blockCenterX) * -motion.z
                    + (position.z - blockCenterZ) * motion.x) / horizontalSpeed;
            if (lateralOffset > 0.12) {
                groundPlacementSide = 1;
            } else if (lateralOffset < -0.12) {
                groundPlacementSide = -1;
            }
            float backwardYaw = (float) Math.toDegrees(
                    Math.atan2(motion.x, -motion.z));
            float preferredYaw = backwardYaw
                    + (float) (GROUND_DIAGONAL_BUCKETS * groundPlacementSide) * 45.0F;
            preferredBucket = Math.round(MathHelper.wrapAngleTo180_float(preferredYaw - cameraYaw) / 45.0F);
        }
        int searchRadius = GROUND_BUCKET_SEARCH_RADIUS;
        if (!movingCardinally || groundMissTicks >= 4) {
            searchRadius = 9;
        } else if (groundMissTicks >= 2) {
            searchRadius = 2;
        }
        int bucketCount = 1;
        for (int index = 1; index < allBuckets.length; index++) {
            if (Math.abs(allBuckets[index] - lastYawBucket) <= searchRadius) {
                allBuckets[bucketCount++] = allBuckets[index];
            }
        }
        int[] yawBuckets = Arrays.copyOf(allBuckets, bucketCount);
        for (int first = 0; first < yawBuckets.length; first++) {
            for (int second = first + 1; second < yawBuckets.length; second++) {
                if (Math.abs(yawBuckets[second] - preferredBucket)
                        < Math.abs(yawBuckets[first] - preferredBucket)) {
                    int previousBucket = yawBuckets[first];
                    yawBuckets[first] = yawBuckets[second];
                    yawBuckets[second] = previousBucket;
                }
            }
        }
        return yawBuckets;
    }

    private boolean isPlacementAllowed(Placement placement) {
        BlockPos target = placement == null ? null : placement.getTarget();
        int placementRow = target == null ? 0 : target.getY();
        if (queuedBlockCount > 0 && placementRow > longTellyRowY + (longTellySprintJump ? 0 : 1)) {
            return false;
        }
        if (target != null && isAbovePredictedPath(target.getX(), target.getY(), target.getZ())) {
            debugPlacementStatus = 'h';
            return false;
        }
        return true;
    }

    private Vec3 verifyPlacementAim(Placement placement, boolean tellyRotating, boolean shortDiagonal, int lookBucket, boolean cameraAligned) {
        float cameraYaw = yawGridBase;
        debugPlacementStatus = 'y';

        if (rotationSentLastTick && !tellyRotating
                && hasStableFaceAim(placement)) {
            aimYawWithJitter(lastBaseYaw, nextYawJitter());
            aimPitch(lastBasePitch);
        }
        Vec3 verifiedHit = raycastFace(placement, targetYaw, targetPitch);
        if (verifiedHit == null && tellyRotating) {
            float fallbackPitch = RotationUtils.clampPitch(Math.max(30.0F, pitch));
            Vec3 fallbackHit = raycastFace(placement, targetYaw, fallbackPitch);
            if (fallbackHit != null) {
                aimPitch(fallbackPitch);
                verifiedHit = fallbackHit;
            }
        }
        if (verifiedHit == null && (!tellyRotating || (shortDiagonal && placementDelayTicks <= 0))) {
            int baseBucket = Math.round(MathHelper.wrapAngleTo180_float(yaw - cameraYaw) / 45.0F);
            int[] yawBuckets = new int[]{baseBucket, baseBucket - 1, baseBucket + 1, baseBucket - 2, baseBucket + 2,
                    baseBucket - 3, baseBucket + 3, baseBucket + 4};
            if (isLookLocked() && isLongTellyEnabled() && queuedBlockCount > 0 && !cameraAligned
                    && baseBucket != lookBucket) {
                yawBuckets = new int[]{lookBucket, baseBucket, baseBucket - 1, baseBucket + 1, baseBucket - 2, baseBucket + 2,
                        baseBucket - 3, baseBucket + 3, baseBucket + 4};
            }
            for (int i = 0; i < yawBuckets.length && verifiedHit == null; i++) {
                float yawJitter = jitter(0.2F);
                float candidateYaw = cameraYaw + (float) yawBuckets[i] * 45.0F + yawJitter;
                float candidatePitch = findFacePitch(placement, candidateYaw);
                if (Float.isNaN(candidatePitch)) {
                    continue;
                }
                Vec3 candidateHit = raycastFace(placement, candidateYaw, candidatePitch);
                if (candidateHit != null) {
                    aimYawWithJitter(candidateYaw - yawJitter, yawJitter);
                    aimPitch(candidatePitch);
                    verifiedHit = candidateHit;
                    lastYawBucket = yawBuckets[i];

                    debugAimSelection = 3;
                    debugAimScore = 2;
                }
            }
        }
        return verifiedHit;
    }

    private void aimLongTellyPlacement(float previousYaw, float previousPitch, int lookBucket, boolean cameraAligned) {
        float cameraYaw = yawGridBase;
        float rotationLimit = longTellySprintJump
                ? randomFloat(AIRBORNE_YAW_MIN, AIRBORNE_YAW_MAX)
                : randomFloat(TAKEOFF_YAW_MIN, TAKEOFF_YAW_MAX);
        float yawDelta = MathHelper.wrapAngleTo180_float(longTellyAimYaw - previousYaw);
        float pitchDelta = longTellyAimPitch - previousPitch;
        float easeFraction = 1.0F
                - (float) Math.pow(0.05, 1.0 / (double) LONG_TELLY_SMOOTH_TICKS);
        easeFraction = Math.min(1.0F, easeFraction * rotationEaseScale(Math.max(Math.abs(yawDelta), Math.abs(pitchDelta))));
        aimYawWithJitter(previousYaw + clampRotationDelta(yawDelta * easeFraction, rotationLimit), jitter(0.1F));
        aimPitch(RotationUtils.clampPitch(previousPitch + clampRotationDelta(pitchDelta * easeFraction, rotationLimit)));
        lastYawBucket = Math.round(MathHelper.wrapAngleTo180_float(targetYaw - cameraYaw) / 45.0F);
        debugAimSelection = 4;
        debugAimScore = 2;
        Vec3 verifiedHit = null;

        if (rotationSentLastTick && !cameraAligned
                && hasStableFaceAim(pendingPlacement)) {
            aimYawWithJitter(lastBaseYaw, jitter(0.1F));
            aimPitch(RotationUtils.clampPitch(lastBasePitch));
            lastYawBucket = Math.round(MathHelper.wrapAngleTo180_float(targetYaw - cameraYaw) / 45.0F);
            verifiedHit = raycastFace(pendingPlacement, targetYaw, targetPitch);
        }
        float nearestBucket = (float) Math.round(MathHelper.wrapAngleTo180_float(targetYaw - cameraYaw) / 45.0F);
        float[] yawBuckets = (isLookLocked() && isLongTellyEnabled() && queuedBlockCount > 0 && !cameraAligned
                && nearestBucket != (float) lookBucket)
                ? new float[]{(float) lookBucket, nearestBucket}
                : new float[]{nearestBucket};
        for (int bucketIndex = 0; bucketIndex < yawBuckets.length && verifiedHit == null; bucketIndex++) {
            float bucket = yawBuckets[bucketIndex];
            float bucketTolerance = (bucket == (float) lookBucket) ? Math.max(rotationLimit, 46.0F) : rotationLimit;
            float yawJitter = jitter(0.1F);
            float candidateYaw = cameraYaw + bucket * 45.0F + yawJitter;
            if (Math.abs(MathHelper.wrapAngleTo180_float(candidateYaw - targetYaw)) <= bucketTolerance) {
                float facePitch = findNearestFacePitch(pendingPlacement, candidateYaw, previousPitch);
                if (!Float.isNaN(facePitch)) {
                    float candidatePitch = RotationUtils.clampPitch(facePitch);
                    Vec3 candidateHit = raycastFace(pendingPlacement, candidateYaw, targetPitch);
                    float selectedPitch = targetPitch;
                    if (candidateHit == null) {
                        selectedPitch = candidatePitch;
                        candidateHit = raycastFace(pendingPlacement, candidateYaw, candidatePitch);
                    }
                    if (candidateHit != null) {
                        aimYawWithJitter(candidateYaw - yawJitter, yawJitter);
                        aimPitch(selectedPitch);
                        lastYawBucket = Math.round(bucket);
                        verifiedHit = candidateHit;
                    }
                }
            }
        }
        if (verifiedHit == null) {
            verifiedHit = raycastFace(pendingPlacement, targetYaw, targetPitch);
        }
        if (verifiedHit == null && Math.abs(yawDelta) <= rotationLimit && Math.abs(pitchDelta) <= rotationLimit) {
            float adjustedPitch = findNearestFacePitch(pendingPlacement, targetYaw, targetPitch);
            if (!Float.isNaN(adjustedPitch)) {
                aimPitch(RotationUtils.clampPitch(adjustedPitch));
                verifiedHit = raycastFace(pendingPlacement, targetYaw, targetPitch);
            }
        }
        if (verifiedHit != null) {
            pendingPlacementReady = true;
            queueStallTicks = 0;
            debugLongTellyStatus = 'P';
        } else {
            debugLongTellyStatus = 'R';
            if (pendingQueueIndex >= 0 && ++queueStallTicks >= 3) {
                queueRetryTicks[pendingQueueIndex] = 2;
                skippedQueueMask |= 1 << pendingQueueIndex;
                queueStallTicks = 0;
                pendingQueueIndex = -1;
            }
        }
    }

    private boolean isHypixelSourceCanPlace(Vec3 position) {
        if (position == null) return false;
        int x = MathHelper.floor_double(position.x);
        int y = MathHelper.floor_double(position.y) - 1;
        int z = MathHelper.floor_double(position.z);
        return isReplaceableAt(x, y, z);
    }

    private boolean isHypixelSourceDiagonal() {
        float delta = getPlayerYaw() % 90.0F;
        if (delta < 0.0F) delta += 90.0F;
        return (delta > 20.0F && delta < 70.0F)
                || Utils.isBindDown(mc.gameSettings.keyBindLeft)
                || Utils.isBindDown(mc.gameSettings.keyBindRight);
    }

    private void updateHypixelSourceIdleRotation(float movementYaw) {
        if (Float.isNaN(hypixelSourceYaw)) {
            hypixelSourceYaw = getPlayerYaw() - 180.0F;
            hypixelSourcePitch = 90.0F;
        }

        float offsetYaw = movementYaw + 50.0F;
        float yawDelta = Math.abs(MathHelper.wrapAngleTo180_float(offsetYaw - hypixelSourceYaw));
        int sourceTick = yawDelta > 60.0F ? 8 : 9;
        if (hypixelPlaceTimer == sourceTick && !isJumpPressed() && mc.thePlayer.onGround) {
            float fromYaw = rotationSentLastTick ? lastSentYaw : getServerYaw();
            float desiredYaw = unwrapYaw(offsetYaw, fromYaw);
            float maxChange = getYuriRotationStep();
            float smoothedYaw = fromYaw + clampRotationDelta(
                    MathHelper.wrapAngleTo180_float(desiredYaw - fromYaw), maxChange);
            hypixelSourceYaw = quantizeMouseRotation(smoothedYaw, fromYaw);
            // Yuri only changes targetYaw here; targetPitch is intentionally kept.
        }

        targetYaw = hypixelSourceYaw;
        targetPitch = RotationUtils.clampPitch(hypixelSourcePitch);
        pendingYawJitter = 0.0F;
    }

    private float getYuriRotationStep() {
        // Yuri RotationManager multiplies Scaffold's 6.5 / 4.8 by 18.
        return (isHypixelSourceDiagonal() || isJumpPressed()) ? 117.0F : 86.4F;
    }

    private float[] findYuriWatchdogTarget(Placement placement, float movementYaw) {
        if (placement == null) return null;
        float yawBase = movementYaw + 90.0F;
        float pitchBase = 90.0F;

        for (int yawIndex = 0; yawIndex < HYPIXEL_YAW_OFFSETS.length; yawIndex++) {
            float testYaw = MathHelper.wrapAngleTo180_float(yawBase + HYPIXEL_YAW_OFFSETS[yawIndex]);
            for (int pitchIndex = 0; pitchIndex < HYPIXEL_PITCH_OFFSETS.length; pitchIndex++) {
                float testPitch = MathHelper.clamp_float(
                        pitchBase + HYPIXEL_PITCH_OFFSETS[pitchIndex], -90.0F, 90.0F);
                if (raycastFace(placement, testYaw, testPitch) != null) {
                    return new float[]{testYaw, testPitch};
                }
            }
        }
        return null;
    }

    private Vec3 getHypixelSourceHitVec(Placement placement, Vec3 rayHit) {
        if (placement == null || rayHit == null) return rayHit;
        BlockPos support = placement.support;
        EnumFacing face = placement.face;
        if (support == null || face == null) return rayHit;

        // Yuri ScaffoldUtils.computeHitVec(): keep the face axis exact and jitter
        // the other two axes by +/-0.03, clamped to 0.10..0.90 of the face.
        double jitter = 0.03D;
        double x = rayHit.x;
        double y = rayHit.y;
        double z = rayHit.z;
        if (face != EnumFacing.EAST && face != EnumFacing.WEST) {
            x = Math.max(support.getX() + 0.10D, Math.min(support.getX() + 0.90D,
                    x + (Math.random() - 0.5D) * jitter * 2.0D));
        }
        if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
            y = Math.max(support.getY() + 0.10D, Math.min(support.getY() + 0.90D,
                    y + (Math.random() - 0.5D) * jitter * 2.0D));
        }
        if (face != EnumFacing.NORTH && face != EnumFacing.SOUTH) {
            z = Math.max(support.getZ() + 0.10D, Math.min(support.getZ() + 0.90D,
                    z + (Math.random() - 0.5D) * jitter * 2.0D));
        }
        return new Vec3(x, y, z);
    }

    private Vec3 aimHypixelPlacement(Placement placement, float movementYaw) {
        if (placement == null) return null;

        float fromYaw = rotationSentLastTick ? lastSentYaw : RotationUtils.serverRotations[0];
        float fromPitch = rotationSentLastTick ? lastSentPitch : mc.thePlayer.rotationPitch;
        float[] watchdogTarget = findYuriWatchdogTarget(placement, movementYaw);

        if (watchdogTarget == null) {
            // Yuri falls back to its normal scaffold rotation solver when the strict
            // Watchdog search cannot find the requested face.
            return aimSourceNormalPlacement(placement, movementYaw, getPlayerYaw());
        }

        float desiredYaw = unwrapYaw(watchdogTarget[0], fromYaw);
        float desiredPitch = watchdogTarget[1];
        float maxChange = getYuriRotationStep();

        float smoothedYaw = fromYaw + clampRotationDelta(
                MathHelper.wrapAngleTo180_float(desiredYaw - fromYaw), maxChange);
        float smoothedPitch = fromPitch + clampRotationDelta(desiredPitch - fromPitch, maxChange);
        smoothedYaw = quantizeMouseRotation(smoothedYaw, fromYaw);
        smoothedPitch = RotationUtils.clampPitch(quantizeMouseRotation(smoothedPitch, fromPitch));

        targetYaw = smoothedYaw;
        targetPitch = smoothedPitch;
        hypixelSourceYaw = smoothedYaw;
        hypixelSourcePitch = smoothedPitch;
        pendingYawJitter = 0.0F;

        // Like Yuri with ray-cast enabled, only place once the smoothed server
        // rotation actually intersects the selected block face.
        return raycastFace(placement, smoothedYaw, smoothedPitch);
    }

    private boolean shouldSourceScaffoldPlace(Vec3 position, String style) {
        if (position == null || style == null) return false;
        if (!isHypixelSourceCanPlace(position)) return false;

        if (!style.equals("Telly")) {
            return true;
        }

        // Yuri's Hypixel Telly clears tellyNoPlace as soon as the first airborne
        // tick starts (the <=3 check only opens the gate; it does not close it later).
        if (sourceTellyWatchdog != null && sourceTellyWatchdog.isToggled()) {
            return sourceAirTicks > 0;
        }

        int threshold;
        if (isJumpPressed()) {
            threshold = sourceTellyJumpDownTicks == null ? 1
                    : Math.max(0, (int) Math.round(sourceTellyJumpDownTicks.getInput()));
        } else if (isHypixelSourceDiagonal()) {
            threshold = sourceTellyDiagonalTicks == null ? 4
                    : Math.max(0, (int) Math.round(sourceTellyDiagonalTicks.getInput()));
        } else {
            threshold = sourceTellyStraightTicks == null ? 6
                    : Math.max(0, (int) Math.round(sourceTellyStraightTicks.getInput()));
        }
        return sourceAirTicks >= threshold;
    }

    private Vec3 aimSourceScaffoldPlacement(Placement placement, String style,
                                             float movementYaw, float cameraYaw) {
        if (placement == null || style == null) return null;

        if (style.equals("Hypixel")) {
            return aimHypixelPlacement(placement, movementYaw);
        }

        if (style.equals("Telly")) {
            if (sourceTellyWatchdog != null && sourceTellyWatchdog.isToggled()) {
                return aimHypixelPlacement(placement, movementYaw);
            }
            return aimSourceNormalPlacement(placement, movementYaw, cameraYaw);
        }

        if (style.equals("Breezily")) {
            Vec3 hit = aimYuriBreezilyPlacement(placement);
            if (hit != null) return hit;
            return aimSourceNormalPlacement(placement, movementYaw, cameraYaw);
        }

        return aimSourceNormalPlacement(placement, movementYaw, cameraYaw);
    }

    private Vec3 aimYuriBreezilyPlacement(Placement placement) {
        if (placement == null) return null;

        float desiredYaw = targetYaw;
        float desiredPitch;
        if (placement.face == EnumFacing.UP) {
            desiredPitch = 90.0F;
        } else {
            desiredYaw = (float) ((Math.toDegrees(Math.atan2(
                    placement.face.getFrontOffsetZ(), placement.face.getFrontOffsetX())) % 360.0D) - 90.0D);
            desiredPitch = 80.0F;
        }

        float fromYaw = rotationSentLastTick ? lastSentYaw : RotationUtils.serverRotations[0];
        desiredYaw = quantizeMouseRotation(unwrapYaw(desiredYaw, fromYaw), fromYaw);
        desiredPitch = RotationUtils.clampPitch(quantizeMouseRotation(desiredPitch, lastSentPitch));
        Vec3 hit = raycastFace(placement, desiredYaw, desiredPitch);
        if (hit != null) {
            targetYaw = desiredYaw;
            targetPitch = desiredPitch;
            pendingYawJitter = 0.0F;
            return hit;
        }

        float facePitch = findNearestFacePitch(placement, desiredYaw, desiredPitch);
        if (!Float.isNaN(facePitch)) {
            facePitch = RotationUtils.clampPitch(quantizeMouseRotation(facePitch, lastSentPitch));
            hit = raycastFace(placement, desiredYaw, facePitch);
            if (hit != null) {
                targetYaw = desiredYaw;
                targetPitch = facePitch;
                pendingYawJitter = 0.0F;
                return hit;
            }
        }
        return null;
    }

    private Vec3 aimSourceNormalPlacement(Placement placement, float movementYaw, float cameraYaw) {
        if (placement == null || mc.thePlayer == null) return null;

        double faceX = placement.support.getX() + 0.5D + placement.face.getFrontOffsetX() * 0.5D;
        double faceZ = placement.support.getZ() + 0.5D + placement.face.getFrontOffsetZ() * 0.5D;
        double dx = faceX - mc.thePlayer.posX;
        double dz = faceZ - mc.thePlayer.posZ;
        float centerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float serverYaw = RotationUtils.serverRotations[0];

        float[] yawChoices = new float[]{
                centerYaw,
                movementYaw + 180.0F,
                movementYaw + 135.0F,
                movementYaw - 135.0F,
                targetYaw,
                lastSentYaw,
                cameraYaw + 180.0F,
                cameraYaw + 90.0F,
                cameraYaw - 90.0F
        };

        for (int i = 0; i < yawChoices.length; i++) {
            float candidateYaw = quantizeMouseRotation(
                    unwrapYaw(yawChoices[i], serverYaw), lastSentYaw);
            float candidatePitch = findNearestFacePitch(placement, candidateYaw, targetPitch);
            if (Float.isNaN(candidatePitch)) {
                candidatePitch = findFacePitch(placement, candidateYaw);
            }
            if (Float.isNaN(candidatePitch)) continue;

            candidatePitch = RotationUtils.clampPitch(
                    quantizeMouseRotation(candidatePitch, lastSentPitch));
            Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
            if (hit != null) {
                targetYaw = candidateYaw;
                targetPitch = candidatePitch;
                pendingYawJitter = 0.0F;
                return hit;
            }
        }
        return null;
    }

    private void applySourceSprintPolicy() {
        String style = getSourceScaffoldMode();
        String sprintMode = sourceSprintMode == null ? "None" : sourceSprintMode.getSelectedOption();
        if (sprintMode == null) sprintMode = "None";

        // Yuri forces LEGIT sprint whenever its Scaffold mode is HYPIXEL.
        if (style.equals("Hypixel")) {
            applySprintPolicy(false);
            return;
        }

        if (sprintMode.equals("None")) {
            applySprintPolicy(true);
            if (mc.thePlayer != null) mc.thePlayer.setSprinting(false);
            return;
        }

        if (sprintMode.equals("NCP")) {
            applySprintPolicy(true);
            if (mc.thePlayer != null) {
                mc.thePlayer.setSprinting(false);
                if (isMoving()) client.setSpeed(0.2873D / 1.84D);
            }
            return;
        }

        applySprintPolicy(false);
        if (sprintMode.equals("Vanilla") && mc.thePlayer != null) {
            mc.thePlayer.setSprinting(isMoving());
        }
    }

    private int getYuriPlaceDelayTicks() {
        if (sourcePlaceDelay == null) return 0;
        // Yuri uses delay * 20ms. At 20 TPS this maps to ceil(value * 0.4) ticks.
        double value = Math.max(0.0D, sourcePlaceDelay.getInput());
        return (int) Math.ceil(value * 0.4D);
    }

    private void updateSourceTellyIdleRotation(float cameraYaw) {
        if (mc.thePlayer == null || !mc.thePlayer.onGround || !isMoving()) return;
        boolean sprinting = mc.thePlayer.raw != null && mc.thePlayer.raw.isSprinting();
        if (!sprinting || isJumpPressed()) {
            float fromYaw = rotationSentLastTick ? lastSentYaw : RotationUtils.serverRotations[0];
            targetYaw = quantizeMouseRotation(unwrapYaw(cameraYaw, fromYaw), fromYaw);
            pendingYawJitter = 0.0F;
        }
    }

    private void updateSourceGodBridgeRotationState() {
        sourceDirectionalChange++;
        float serverYaw = rotationSentLastTick ? lastSentYaw : RotationUtils.serverRotations[0];
        if (Math.abs(MathHelper.wrapAngleTo180_float(targetYaw - serverYaw)) > 10.0F) {
            sourceDirectionalChange = (int) (Math.random() * 4.0D);
        }
    }

    private void updateSourceBreezilyMovement(PrePlayerInputEvent input) {
        if (input == null || mc.thePlayer == null) return;
        if (!canApplyBreezilyGroundStrafe(input)) return;
        if (input.getForward() <= 0.0F || input.isJump()) return;

        double yaw = getPlayerYaw() % 360.0D;
        if (yaw < 0.0D) yaw += 360.0D;
        double fracX = mc.thePlayer.posX - Math.floor(mc.thePlayer.posX);
        double fracZ = mc.thePlayer.posZ - Math.floor(mc.thePlayer.posZ);
        double off;
        double speed;

        // Minecraft 1.8 horizontal facing: 0=SOUTH, 90=WEST, 180=NORTH, 270=EAST.
        if (yaw >= 135.0D && yaw < 225.0D) {          // NORTH
            off = fracX;
            speed = mc.thePlayer.motionZ;
        } else if (yaw >= 225.0D && yaw < 315.0D) {   // EAST
            off = fracZ;
            speed = mc.thePlayer.motionX;
        } else if (yaw >= 45.0D && yaw < 135.0D) {    // WEST
            off = 1.0D - fracZ;
            speed = mc.thePlayer.motionX;
        } else {                                      // SOUTH
            off = 1.0D - fracX;
            speed = mc.thePlayer.motionZ;
        }

        speed = Math.abs(speed);
        int yuriDelay = sourcePlaceDelay == null ? 0 : (int) Math.round(sourcePlaceDelay.getInput());
        if (speed < 0.086D && Math.abs(off - 0.5D) < 0.4D && yuriDelay <= 1) return;

        double threshold = 0.5D + (Math.random() - 0.5D) / 10.0D;
        input.setStrafe(off < threshold ? -1.0F : 1.0F);
    }

    private void updateHopMotion() {
        if (!Utils.nullCheck() || !isHopMode() || isTowerActive() || !isMoving()) {
            hopAirTicks = 0;
            hopLaunchRequested = false;
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        String mode = getHopMode();
        double speedMultiplier = hopSpeed == null ? 1.08D : hopSpeed.getInput();
        speedMultiplier = MathHelper.clamp_double(speedMultiplier, 1.00D, 1.18D);

        if (hopLaunchRequested && player.onGround) {
            double launchY = mode.equals("Low") ? 0.34D : 0.42D;
            double horizontalMultiplier = 1.0D;

            if (mode.equals("Boost")) {
                horizontalMultiplier = speedMultiplier;
            } else if (mode.equals("Burst")) {
                horizontalMultiplier = Math.min(1.18D, speedMultiplier + 0.03D);
            } else if (mode.equals("Adaptive")) {
                boolean safeAhead = !isUnsupportedAt(
                        player.posX + player.motionX * 2.75D,
                        player.posZ + player.motionZ * 2.75D);
                horizontalMultiplier = safeAhead ? Math.min(1.18D, speedMultiplier + 0.02D) : 1.02D;
            } else if (mode.equals("Low")) {
                horizontalMultiplier = Math.min(1.10D, speedMultiplier);
            }

            client.setMotion(player.motionX * horizontalMultiplier, launchY,
                    player.motionZ * horizontalMultiplier);
            player.motionX *= horizontalMultiplier;
            player.motionY = launchY;
            player.motionZ *= horizontalMultiplier;
            hopAirTicks = 1;
            hopLaunchRequested = false;
            return;
        }

        if (player.onGround) {
            hopAirTicks = 0;
            return;
        }

        hopAirTicks++;

        if (mode.equals("Boost")) {
            if (hopAirTicks == 2) {
                double multiplier = Math.min(1.08D, 1.0D + (speedMultiplier - 1.0D) * 0.45D);
                client.setMotion(player.motionX * multiplier, player.motionY, player.motionZ * multiplier);
                player.motionX *= multiplier;
                player.motionZ *= multiplier;
            }
            return;
        }

        if (mode.equals("Burst")) {
            double newY = player.motionY;
            if (hopAirTicks == 3) newY -= 0.025D;
            if (hopAirTicks == 5) newY -= 0.040D;
            if (newY != player.motionY) {
                client.setMotion(player.motionX, newY, player.motionZ);
                player.motionY = newY;
            }
            return;
        }

        if (mode.equals("Adaptive")) {
            boolean safeAhead = !isUnsupportedAt(
                    player.posX + player.motionX * 2.5D,
                    player.posZ + player.motionZ * 2.5D);
            if (safeAhead && hopAirTicks == 3) {
                double newY = player.motionY - 0.030D;
                client.setMotion(player.motionX, newY, player.motionZ);
                player.motionY = newY;
            }
        }
    }

    private void easeActivationRotation(EntityPlayerSP player, float previousYaw, float previousPitch, int lookBucket, boolean cameraAligned) {
        float smoothTicks = ACTIVATION_SMOOTH_TICKS;
        boolean ramping = smoothTicks >= 1.0F && (float) activationTicks <= smoothTicks;

        float easeFraction = ramping
                ? 1.0F - (float) Math.pow(0.05, 1.0 / (double) smoothTicks)
                : 1.0F;
        if (ramping) {
            float distance = Math.max(Math.abs(MathHelper.wrapAngleTo180_float(targetYaw - previousYaw)), Math.abs(targetPitch - previousPitch));
            easeFraction = Math.min(1.0F, easeFraction * rotationEaseScale(distance));
        }
        float targetBucket = (float) Math.round(MathHelper.wrapAngleTo180_float(targetYaw - yawGridBase) / 45.0F);
        if (isLookLocked() && isLongTellyEnabled() && queuedBlockCount > 0 && !cameraAligned) {
            targetBucket = (float) lookBucket;
        }
        float currentBucket = (float) Math.round(MathHelper.wrapAngleTo180_float(previousYaw - yawGridBase) / 45.0F);
        while (targetBucket - currentBucket > 4.0F) {
            targetBucket -= 8.0F;
        }
        while (targetBucket - currentBucket < -4.0F) {
            targetBucket += 8.0F;
        }
        float nextBucket = targetBucket;
        float maxStep = YAW_BUCKET_STEP;
        boolean stepping = !player.onGround;
        if (ramping) {
            activationYawAccumulator += Math.abs(targetBucket - currentBucket) * easeFraction * ACTIVATION_YAW_LAG;
            maxStep = (float) Math.floor(activationYawAccumulator);
            activationYawAccumulator -= maxStep;
            stepping = true;
        } else {
            activationYawAccumulator = 0.0F;
        }
        if (stepping && Math.abs(targetBucket - currentBucket) > maxStep) {
            nextBucket = currentBucket + Math.signum(targetBucket - currentBucket) * maxStep;
        }
        aimYawWithJitter(yawGridBase + nextBucket * 45.0F, jitter(0.18F));
        if (ramping) {
            float pitchDelta = targetPitch - previousPitch;
            if (Math.abs(pitchDelta) > 0.5F) {
                aimPitch(RotationUtils.clampPitch(previousPitch + pitchDelta * easeFraction));
            }
        }
    }

    private boolean isBreezilyGodBridgeJump() {
        if (!isBreezilyMode() || mc.thePlayer == null) return false;
        return isJumpPressed() || !mc.thePlayer.onGround;
    }

    private boolean isSnapGodBridgeJump() {
        if (!isSnapMode() || mc.thePlayer == null) return false;

        return (isJumpPressed() || !mc.thePlayer.onGround) && isAnyMovementKeyDown();
    }

    private boolean isSnapRotationActive() {
        return isSnapMode() && !isSnapGodBridgeJump();
    }

    private boolean isSnapPlacementReady(int groundCandidateIndex, Vec3 verifiedHit, Placement placement) {
        if (!isSnapRotationActive() || placementDelayTicks > 0) return false;
        if (groundCandidateIndex >= 0 && groundCandidateIndex < candidates.length) {
            Placement groundPlacement = candidates[groundCandidateIndex];
            if (groundPlacement != null) {
                BlockPos target = groundPlacement.getTarget();
                if (isReplaceableAt(target.getX(), target.getY(), target.getZ())) return true;
            }
        }
        if (verifiedHit != null && placement != null) {
            BlockPos target = placement.getTarget();
            if (isReplaceableAt(target.getX(), target.getY(), target.getZ())) return true;
        }
        if (pendingPlacementReady && pendingPlacement != null) {
            BlockPos target = pendingPlacement.getTarget();
            return isReplaceableAt(target.getX(), target.getY(), target.getZ());
        }
        return false;
    }

    private boolean isSafeSnapReturn(Vec3 position, Vec3 motion) {
        if (position == null || motion == null) return false;
        if (isUnsupportedAt(position.x, position.z)) return false;
        return !isUnsupportedAt(position.x + motion.x * 1.35, position.z + motion.z * 1.35);
    }

    private void applySnapModeRotations(float cameraYaw, boolean placementTick) {
        if (!isSnapRotationActive()) {
            return;
        }
        if (placementTick) {
            return;
        }

        if (isRawDiagonalInput() && !Float.isNaN(snapLastPlacementYaw)) {
            targetYaw = quantizeRotation(unwrapYaw(snapLastPlacementYaw, RotationUtils.serverRotations[0]));
            pendingYawJitter = 0.0F;
            return;
        }

        float referenceYaw = RotationUtils.serverRotations[0];
        targetYaw = quantizeRotation(unwrapYaw(cameraYaw, referenceYaw));
        pendingYawJitter = 0.0F;
    }

    private Vec3 aimSnapFace(Placement placement, float cameraYaw) {
        if (placement == null) return null;

        EntityPlayerSP player = mc.thePlayer;
        double faceX = placement.support.getX() + 0.5 + placement.face.getFrontOffsetX() * 0.5;
        double faceY = placement.support.getY() + 0.5 + placement.face.getFrontOffsetY() * 0.5;
        double faceZ = placement.support.getZ() + 0.5 + placement.face.getFrontOffsetZ() * 0.5;
        double dx = faceX - player.posX;
        double dz = faceZ - player.posZ;
        float centerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float movementYaw = getMovementYaw(cameraYaw, getForwardInput(), getStrafeInput());
        float backwardYaw = movementYaw - 180.0F;

        float[] yawChoices;
        if (isRawDiagonalInput()) {
            yawChoices = new float[]{
                    backwardYaw,
                    movementYaw - 135.0F,
                    movementYaw + 135.0F,
                    centerYaw,
                    targetYaw,
                    lastSentYaw,
                    movementYaw - 90.0F,
                    movementYaw + 90.0F,
                    cameraYaw + 180.0F
            };
        } else {
            yawChoices = new float[]{
                    targetYaw,
                    centerYaw,
                    backwardYaw,
                    lastSentYaw,
                    cameraYaw + 135.0F,
                    cameraYaw - 135.0F,
                    cameraYaw + 180.0F,
                    cameraYaw + 90.0F,
                    cameraYaw - 90.0F
            };
        }

        for (int i = 0; i < yawChoices.length; i++) {
            float candidateYaw = quantizeMouseRotation(
                    unwrapYaw(yawChoices[i], lastSentYaw), lastSentYaw);
            float candidatePitch = findNearestFacePitch(placement, candidateYaw, targetPitch);
            if (Float.isNaN(candidatePitch)) {
                candidatePitch = findFacePitch(placement, candidateYaw);
            }
            if (Float.isNaN(candidatePitch)) continue;
            candidatePitch = RotationUtils.clampPitch(
                    quantizeMouseRotation(candidatePitch, lastSentPitch));
            Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
            if (hit != null) {
                targetYaw = candidateYaw;
                targetPitch = candidatePitch;
                pendingYawJitter = 0.0F;
                lastYawBucket = Math.round(MathHelper.wrapAngleTo180_float(candidateYaw - cameraYaw) / 45.0F);
                return hit;
            }
        }
        return null;
    }

    private void setServerRelativeMovementInputs(boolean enabled) {
        try {
            Class<?> helperClass = Class.forName("keystrokesmod.helper.RotationHelper");
            Object helper = helperClass.getMethod("get").invoke(null);
            helperClass.getMethod("setServerRelativeMovementInputs", Boolean.TYPE)
                    .invoke(helper, Boolean.valueOf(enabled));
        } catch (Throwable ignored) {
        }
    }

    // Ninja keeps a stable bridge heading and a single pitch for each lane.
    // S -> S+D/S+A is a lane change, not a new per-block aim calculation.
    private void resetNinjaPlacementAim() {
        ninjaBridgeYaw = Float.NaN;
        ninjaBridgePitch = 80.0F;
        ninjaBridgeInputMask = 0;
        ninjaBaseYaw = Float.NaN;
        ninjaAppliedYaw = Float.NaN;
        ninjaAimAnchorYaw = Float.NaN;
        ninjaDiagonalSide = 0;
        ninjaAimMissTicks = 0;
        ninjaAimRecoveryCooldown = 0;
        ninjaPitchRecoveryUsed = false;
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
        if (Float.isNaN(ninjaBaseYaw)) {
            // Real camera heading is a stable reference. In particular, do not
            // reuse lastSentYaw: silent scaffold rotations belong to the SERVER.
            ninjaBaseYaw = player.rotationYaw;
        } else if (Math.abs(MathHelper.wrapAngleTo180_float(
                player.rotationYaw - ninjaBaseYaw)) > 7.0F) {
            // The player deliberately turned the camera; follow that new heading.
            ninjaBaseYaw = player.rotationYaw;
            ninjaBridgeInputMask = -1;
        }

        if (mask == ninjaBridgeInputMask && !Float.isNaN(ninjaBridgeYaw)) return;

        int forward = getForwardInput();
        int strafe = getStrafeInput();
        if (forward == 0 && strafe == 0) return;

        float motionYaw = getMovementYaw(ninjaBaseYaw, forward, strafe);
        float backwardYaw = motionYaw - 180.0F;
        int diagonalSide = forward != 0 && strafe != 0
                ? (strafe < 0 ? -1 : 1) : 0;
        ninjaDiagonalSide = diagonalSide;

        // Slight side view, like manually bridging while holding S+D/S+A.
        // Choose the same side on straight segments to avoid a visual snap
        // when D or A is briefly pressed/released during the transition.
        float sideAngle;
        if (diagonalSide != 0) {
            sideAngle = diagonalSide * 9.0F;
        } else if (forward < 0) {
            sideAngle = (ninjaLastDiagonalSide != 0 ? ninjaLastDiagonalSide : -1) * 19.0F;
        } else {
            sideAngle = 0.0F;
        }
        if (diagonalSide != 0) ninjaLastDiagonalSide = diagonalSide;

        float reference = Float.isNaN(ninjaAppliedYaw) ? player.rotationYaw : ninjaAppliedYaw;
        ninjaBridgeYaw = quantizeMouseRotation(
                unwrapYaw(backwardYaw + sideAngle, reference), reference);
        ninjaAimAnchorYaw = ninjaBridgeYaw;
        if (Float.isNaN(ninjaAppliedYaw)) ninjaAppliedYaw = reference;
        if (ninjaBridgeInputMask == 0) {
            float cameraPitch = player.rotationPitch;
            ninjaBridgePitch = cameraPitch >= 75.0F && cameraPitch <= 84.0F
                    ? cameraPitch : 80.0F;
        }
        ninjaBridgeInputMask = mask;
        ninjaAimMissTicks = 0;
        ninjaAimRecoveryCooldown = 0;
        ninjaPitchRecoveryUsed = false;
    }

    // A raycast is preferable to a per-block pitch correction. If the ray
    // reaches a valid side next to the predicted lane, use that actual side.
    private boolean isNinjaLanePlacement(Placement placement, EntityPlayerSP player) {
        if (placement == null || player == null || Float.isNaN(ninjaBaseYaw)) return false;
        BlockPos target = placement.getTarget();
        int rowY = MathHelper.floor_double(player.posY) - 1;
        if (target.getY() != rowY || !isReplaceableAt(target)) return false;
        if (!isPlacementWithinReach(placement, 4.5D)) return false;
        if (isInteractableAt(placement.support.getX(), placement.support.getY(), placement.support.getZ())) return false;

        float moveYaw = getMovementYaw(ninjaBaseYaw, getForwardInput(), getStrafeInput());
        double angle = Math.toRadians(moveYaw);
        double moveX = -Math.sin(angle);
        double moveZ = Math.cos(angle);
        double dx = target.getX() + 0.5D - player.posX;
        double dz = target.getZ() + 0.5D - player.posZ;
        double ahead = dx * moveX + dz * moveZ;
        double sideways = Math.abs(-dx * moveZ + dz * moveX);
        // A diagonal lane alternates two neighboring grid cells at corners.
        // The old 1.05-block width and -0.20 rear limit rejected one of those
        // cells even when the ACTUAL raycast proved it was a legitimate face.
        return ahead > (ninjaDiagonalSide == 0 ? -0.30D : -0.65D)
                && ahead <= 2.10D
                && sideways <= (ninjaDiagonalSide == 0 ? 0.90D : 1.42D);
    }

    // Accept only a true ray hit, but do not rely on findPlacement(): its
    // straight/short-diagonal planner may choose an unreachable support face.
    private Placement getNinjaRayPlacement(EntityPlayerSP player, float yaw, float pitch) {
        MovingObjectPosition hit = RotationUtils.rayCastBlock(4.5D, yaw, pitch);
        if (hit == null || hit.getBlockPos() == null || hit.sideHit == null
                || hit.sideHit == EnumFacing.DOWN) return null;
        Placement candidate = new Placement(hit.getBlockPos(), hit.sideHit);
        if (!isNinjaLanePlacement(candidate, player)) return null;
        ItemStack stack = getPlacementStack(player);
        if (stack == null || !BlockUtils.canPlaceBlockOnSide(stack,
                candidate.support, candidate.face)) return null;
        return candidate;
    }

    private boolean hasNinjaGapAhead(EntityPlayerSP player) {
        if (player == null) return false;
        int y = MathHelper.floor_double(player.posY) - 1;
        float movement = getMovementYaw(ninjaBaseYaw,
                getForwardInput(), getStrafeInput());
        double rad = Math.toRadians(movement);
        double dx = -Math.sin(rad);
        double dz = Math.cos(rad);
        // Check ahead AND to either side at corners; no assumption about
        // whether the next diagonal step is an X or Z block.
        for (double d = 0.45D; d <= 1.45D; d += 0.5D) {
            double x = player.posX + dx * d;
            double z = player.posZ + dz * d;
            if (!hasCornerSupport(x, z, y)) return true;
        }
        return false;
    }

    // A miss is corrected only when the actual raycast cannot hit a face in
    // the intended bridge lane. Successful placements never re-aim Ninja.
    // Aim around the original S / S+A / S+D heading (never accumulate turns).
    private void recoverNinjaBridgeAim(EntityPlayerSP player) {
        if (ninjaAimRecoveryCooldown > 0 || ninjaAimMissTicks < 1
                || Float.isNaN(ninjaAimAnchorYaw) || !hasNinjaGapAhead(player)) return;
        ninjaAimRecoveryCooldown = 4;
        float anchor = ninjaAimAnchorYaw;
        float currentPitch = ninjaBridgePitch;
        float[] offsets = {0.0F, -4.0F, 4.0F, -8.0F, 8.0F,
                -12.0F, 12.0F, -18.0F, 18.0F, -24.0F, 24.0F,
                -30.0F, 30.0F, -36.0F, 36.0F};
        float[] pitches = {currentPitch, 78.0F, 82.0F, 75.0F, 85.0F, 71.0F};
        float winningYaw = Float.NaN;
        float winningPitch = currentPitch;
        double bestScore = Double.POSITIVE_INFINITY;

        // Search genuinely clickable faces, not mathematically plausible yaw
        // angles. Prefer the middle of a face to its last few edge pixels.
        for (float candidatePitch : pitches) {
            for (float offset : offsets) {
                float candidateYaw = quantizeMouseRotation(anchor + offset, anchor);
                Placement placement = getNinjaRayPlacement(player, candidateYaw, candidatePitch);
                if (placement == null) continue;
                Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
                if (hit == null) continue;
                double score = Math.abs(offset) * 0.65D
                        + Math.abs(candidatePitch - currentPitch) * 1.5D
                        + ninjaFaceCenterPenalty(placement, hit) * 8.0D;
                if (score < bestScore) {
                    bestScore = score;
                    winningYaw = candidateYaw;
                    winningPitch = candidatePitch;
                }
            }
        }

        // If the regular placement planner found an actual support face, its
        // face centre is a useful extra target, especially at a diagonal seam.
        Placement planned = findPlacement();
        if (planned != null && isNinjaLanePlacement(planned, player)) {
            double x = planned.support.getX() + 0.5D
                    + planned.face.getFrontOffsetX() * 0.5D;
            double y = planned.support.getY() + 0.5D
                    + planned.face.getFrontOffsetY() * 0.5D;
            double z = planned.support.getZ() + 0.5D
                    + planned.face.getFrontOffsetZ() * 0.5D;
            double dx = x - player.posX;
            double dz = z - player.posZ;
            float faceYaw = quantizeMouseRotation(
                    (float) Math.toDegrees(Math.atan2(-dx, dz)), anchor);
            float yawOffset = Math.abs(MathHelper.wrapAngleTo180_float(faceYaw - anchor));
            if (yawOffset <= 36.0F) {
                float facePitch = RotationUtils.clampPitch((float) Math.toDegrees(
                        Math.atan2(player.posY + player.getEyeHeight() - y,
                                Math.sqrt(dx * dx + dz * dz))));
                facePitch = quantizeRotation(facePitch);
                Vec3 hit = raycastFace(planned, faceYaw, facePitch);
                if (hit != null) {
                    double score = yawOffset * 0.65D
                            + Math.abs(facePitch - currentPitch) * 1.5D
                            + ninjaFaceCenterPenalty(planned, hit) * 8.0D;
                    if (score < bestScore) {
                        bestScore = score;
                        winningYaw = faceYaw;
                        winningPitch = facePitch;
                    }
                }
            }
        }

        if (!Float.isNaN(winningYaw)) {
            ninjaBridgeYaw = winningYaw;
            ninjaBridgePitch = winningPitch;
            ninjaAimMissTicks = 0;
        }
    }

    private double ninjaFaceCenterPenalty(Placement placement, Vec3 hit) {
        double x = hit.x - Math.floor(hit.x);
        double y = hit.y - Math.floor(hit.y);
        double z = hit.z - Math.floor(hit.z);
        if (placement.face == EnumFacing.UP) {
            return Math.abs(x - 0.5D) + Math.abs(z - 0.5D);
        }
        if (placement.face == EnumFacing.EAST || placement.face == EnumFacing.WEST) {
            return Math.abs(y - 0.5D) + Math.abs(z - 0.5D);
        }
        return Math.abs(x - 0.5D) + Math.abs(y - 0.5D);
    }

    private void updateNinjaClassicPlacement() {
        RotationHelper.get().forceMovementFix = false;
        client.disableMovementFix();
        setServerRelativeMovementInputs(false);
        movementFixActive = false;
        rotationSentThisTick = false;

        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;
        selectPlacementStack(player);
        int mask = getNinjaDirectionMask();
        if (!isMoving() || !isValidBlock(getPlacementStack(player))) {
            resetNinjaPlacementAim();
            return;
        }

        updateNinjaBridgeLane(player, mask);
        if (Float.isNaN(ninjaBridgeYaw)) return;
        if (ninjaAimRecoveryCooldown > 0) ninjaAimRecoveryCooldown--;
        if (placementDelayTicks <= 0 && !placedThisTick
                && getNinjaRayPlacement(player, ninjaBridgeYaw, ninjaBridgePitch) == null
                && hasNinjaGapAhead(player)) {
            ninjaAimMissTicks = Math.min(15, ninjaAimMissTicks + 1);
            recoverNinjaBridgeAim(player);
        }

        // Re-aim only on a change of movement lane, a deliberate camera turn,
        // or after three consecutive missing placements. No micro-pitch per block.
        float delta = MathHelper.wrapAngleTo180_float(ninjaBridgeYaw - ninjaAppliedYaw);
        float step = MathHelper.clamp_float(delta, -24.0F, 24.0F);
        ninjaAppliedYaw = quantizeMouseRotation(ninjaAppliedYaw + step, ninjaAppliedYaw);
        targetYaw = ninjaAppliedYaw;
        targetPitch = ninjaBridgePitch;
        lastSentYaw = targetYaw;
        lastSentPitch = targetPitch;
        RotationHelper.get().setRotations(targetYaw, targetPitch);
        rotationSentThisTick = true;

        if (placementDelayTicks <= 0 && !placedThisTick) {
            // First trust the real face: diagonals can alternate between
            // neighboring X and Z targets, unlike the one-target planner.
            Placement hit = getNinjaRayPlacement(player, targetYaw, targetPitch);
            boolean validAim = hit != null;
            boolean placed = hit != null && placeBlock(hit);
            if (!placed) {
                Placement planned = findPlacement();
                if (planned != null && isNinjaLanePlacement(planned, player)
                        && raycastFace(planned, targetYaw, targetPitch) != null) {
                    validAim = true;
                    placed = placeBlock(planned);
                }
            }
            if (placed) {
                ninjaAimMissTicks = 0;
            } else if (!validAim && hasNinjaGapAhead(player)) {
                ninjaAimMissTicks = Math.min(15, ninjaAimMissTicks + 1);
                recoverNinjaBridgeAim(player);
            } else if (!hasNinjaGapAhead(player)) {
                ninjaAimMissTicks = 0;
            }
        }
        autoClick(player);
    }

    private void sendRotations() {
        RotationHelper.get().setRotations(targetYaw, targetPitch);
        debugSentYaw = targetYaw;
        previousSentPitch = lastSentPitch;
        lastSentYaw = targetYaw;
        lastSentPitch = targetPitch;

        lastBaseYaw = targetYaw - pendingYawJitter;
        lastBasePitch = targetPitch;
        rotationSentThisTick = true;

        if (isBreezilyMode() && !isBreezilyGodBridgeJump()) {
            RotationHelper.get().forceMovementFix = false;
            client.disableMovementFix();
            movementFixActive = false;
        } else {
            RotationHelper.get().forceMovementFix = true;
            client.enableMovementFix();
            movementFixActive = RotationHelper.get().fixMovement();
        }
    }

    private void placeSelectedBlock(int groundCandidateIndex, Vec3 verifiedHit, Placement placement) {
        if (isSnapMode() && snapSkipPlacementThisTick) {
            return;
        }
        if (pendingPlacementReady && placementDelayTicks > 0 && !isLongTellyEnabled()) {
            debugLongTellyStatus = 'L';
        }
        if (groundCandidateIndex >= 0 && placementDelayTicks <= 0) {
            placeBlock(candidates[groundCandidateIndex]);
        } else if (verifiedHit != null && placementDelayTicks <= 0) {
            placeBlock(placement);
        } else if (pendingPlacementReady && (isLongTellyEnabled() || placementDelayTicks <= 0)) {
            if (placeBlock(pendingPlacement)) {
                verificationBlock = pendingPlacement.getTarget();
                verificationDelayTicks = 4;
            }
        }
    }

    private void updateKeepYPlacement(EntityPlayerSP player, Vec3 position, Vec3 motion) {
        if (getActiveKeepYMode().equals("Telly B") && keepYStage > 0 && !player.onGround) {
            int nextBlockY = MathHelper.floor_double(position.y + motion.y);
            if (nextBlockY <= keepYStartHeight && position.y > (double) (keepYStartHeight + 1)) {
                useCurrentHeight = true;
                Placement placement = findPlacement();
                if (placement != null && placementDelayTicks <= 0 && !placedThisTick) {
                    Vec3 hit = raycastFace(placement, lastSentYaw, lastSentPitch);
                    if (hit != null) {
                        placeBlock(placement);
                    }
                }
            }
        }
        if (isKeepYActive() && keepYStage > 0 && placementDelayTicks <= 0) {
            for (int placements = placementAttemptsThisTick; placements < getPlacementLimit(); placements++) {
                if (!placeKeepYFromRay()) {
                    break;
                }
            }
        }
    }

    private boolean allowPacket(CPacket packet) {
        if (packet instanceof C09) {
            if (!isSpoofItemEnabled() || spoofServerSlot < 0) {
                return true;
            }
            if (sendingSpoofSlotPacket) {
                return true;
            }

            // The native player controller may try to re-send either the enforced
            // block slot or the visible client slot while placing. Both are local
            // sync noise while spoofing: the server already has spoofServerSlot.
            return false;
        }
        if (packet instanceof C08) {
            if (sendingPlacement || sendingAutoClick) return true;
            return !(rotationSentThisTick || rotationSentLastTick);
        }
        if (!(packet instanceof C07)) return true;
        C07 dig = (C07) packet;
        String status = dig.status == null ? "" : dig.status;
        if (!status.equals("START_DESTROY_BLOCK") && !status.equals("ABORT_DESTROY_BLOCK") && !status.equals("STOP_DESTROY_BLOCK")) return true;
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || !isValidBlock(getPlacementStack(player))) return true;
        return !(rotationSentThisTick || rotationSentLastTick);
    }

    private boolean allowMouse(int button) {
        if (mc.currentScreen != null) {
            return true;
        }
        return button > 1;
    }

    private int buildCandidates(int groundY) {
        EntityPlayerSP player = mc.thePlayer;
        Vec3 position = player.getPositionVector();
        int minX = MathHelper.floor_double(position.x - 0.3);
        int maxX = MathHelper.floor_double(position.x + 0.3);
        int minZ = MathHelper.floor_double(position.z - 0.3);
        int maxZ = MathHelper.floor_double(position.z + 0.3);
        double motionX = player.motionX;
        double motionZ = player.motionZ;
        double horizontalSpeed = Utils.getHorizontalSpeed(player);
        boolean forwardOnly = horizontalSpeed >= 0.02;
        int count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!isSolidSupport(x, groundY, z) || isInteractableAt(x, groundY, z)) {
                    continue;
                }
                for (int f = 0; f < HORIZONTAL_FACES.length && count < candidates.length; f++) {
                    EnumFacing face = HORIZONTAL_FACES[f];
                    int targetX = x + face.getFrontOffsetX();
                    int targetZ = z + face.getFrontOffsetZ();
                    if (forwardOnly && !(neededBlock != null && targetX == neededBlock.getX() && targetZ == neededBlock.getZ())
                            && (double) face.getFrontOffsetX() * motionX + (double) face.getFrontOffsetZ() * motionZ
                            < 0.15 * horizontalSpeed) {
                        continue;
                    }
                    if (isReplaceableAt(targetX, groundY, targetZ)) {
                        candidates[count] = new Placement(new BlockPos(x, groundY, z), face);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private int matchCandidate(int candidateCount, float candidateYaw, float candidatePitch) {
        MovingObjectPosition hit = RotationUtils.rayCastBlock(4.5, candidateYaw, candidatePitch);
        if (hit == null) {
            return -1;
        }
        for (int i = 0; i < candidateCount; i++) {
            if (candidates[i].matches(hit)) {
                return i;
            }
        }
        return -1;
    }

    private void buildPrediction() {
        hasPrediction = true;
        EntityPlayerSP player = mc.thePlayer;
        Vec3 position = player.getPositionVector();
        Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
        Arrays.fill(predictedPositions, position);
        try {
            SimulatedPlayer simulatedPlayer = createSimulation();
            if (isShortDiagonalJump()) {
                simulatedPlayer.rotationYaw = player.rotationYaw;
                simulatedPlayer.movementInput.forward = getForwardInput();
                simulatedPlayer.movementInput.strafe = getStrafeInput();
                simulatedPlayer.movementInput.jump = false;
            }
            predictedPositions[0] = new Vec3(position.x, simulatedPlayer.getPos().y, position.z);
            for (int t = 1; t < predictedPositions.length; t++) {
                simulatedPlayer.tick();
                predictedPositions[t] = simulatedPlayer.getPos();
            }
        } catch (Throwable ex) {
            double verticalMotion = motion.y;
            double y = position.y;
            for (int t = 1; t < predictedPositions.length; t++) {
                y += verticalMotion;
                verticalMotion = (verticalMotion - 0.08) * 0.98;
                predictedPositions[t] = new Vec3(position.x + motion.x * t,
                        y, position.z + motion.z * t);
            }
        }
    }

    private boolean isAbovePredictedPath(int blockX, int blockY, int blockZ) {
        EntityPlayerSP player = mc.thePlayer;
        Vec3 position = player.getPositionVector();
        if ((double) blockY + 1.0 <= position.y + 0.001) {
            return false;
        }
        if (!hasPrediction) {
            buildPrediction();
        }
        double dx = ((double) blockX + 0.5) - position.x;
        double dz = ((double) blockZ + 0.5) - position.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float quadrantAngle = Math.abs(targetYaw % 90.0F);
        boolean diagonal = quadrantAngle > 22.5F && quadrantAngle < 67.5F;
        int predictionIndex = (int) (horizontalDistance * (diagonal ? 1.5 : 1.0) + 0.7);
        predictionIndex = MathHelper.clamp_int(predictionIndex, 0, predictedPositions.length - 1);

        return (double) blockY > predictedPositions[predictionIndex].y;
    }

    private Vec3 simulatePosition(int ticks, PrePlayerInputEvent input) {
        EntityPlayerSP player = mc.thePlayer;
        Vec3 position = player.getPositionVector();
        try {
            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(player.movementInput);
            simulatedPlayer.rotationYaw = player.rotationYaw;
            simulatedPlayer.movementInput.forward = input.getForward();
            simulatedPlayer.movementInput.strafe = input.getStrafe();
            simulatedPlayer.movementInput.jump = input.isJump();
            simulatedPlayer.movementInput.sneak = false;
            for (int t = 0; t < ticks; t++) {
                simulatedPlayer.tick();
            }
            return simulatedPlayer.getPos();
        } catch (Throwable t) {
            Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
            return position.offset(motion.x * ticks, 0.0, motion.z * ticks);
        }
    }

    private double getSupportOverhang(double playerX, double playerZ) {
        EntityPlayerSP player = mc.thePlayer;
        int groundY = MathHelper.floor_double(player.getPositionVector().y) - 1;
        int centerX = MathHelper.floor_double(playerX);
        int centerZ = MathHelper.floor_double(playerZ);
        double best = 999.0;
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                if (!isSolidSupport(x, groundY, z)) {
                    continue;
                }
                double nearestX = MathHelper.clamp_double(playerX, x, x + 1.0);
                double nearestZ = MathHelper.clamp_double(playerZ, z, z + 1.0);
                double distance = Math.max(Math.abs(playerX - nearestX), Math.abs(playerZ - nearestZ));
                if (distance < best) {
                    best = distance;
                }
            }
        }
        return best;
    }

    private boolean isNearEdge(int simTicks, PrePlayerInputEvent input, double edgeLead) {
        Vec3 predictedPosition = simulatePosition(simTicks, input);
        EntityPlayerSP player = mc.thePlayer;
        Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
        double horizontalSpeed = Utils.getHorizontalSpeed(player);
        double probeX = predictedPosition.x;
        double probeZ = predictedPosition.z;
        if (horizontalSpeed > 0.01) {
            double lead = Math.max(0.0, edgeLead);
            probeX += motion.x / horizontalSpeed * lead;
            probeZ += motion.z / horizontalSpeed * lead;
        }
        double overhang = getSupportOverhang(probeX, probeZ);
        if (simTicks == 1) {
            debugOverhang = overhang;
        }
        return overhang > 0.0;
    }

    private void findNeededBlock(int groundY) {
        neededBlock = null;
        EntityPlayerSP player = mc.thePlayer;
        Vec3 position = player.getPositionVector();
        try {
            SimulatedPlayer simulatedPlayer = createSimulation();
            for (int t = 0; t < 5 && neededBlock == null; t++) {
                simulatedPlayer.tick();
                Vec3 predictedPosition = simulatedPlayer.getPos();
                checkUnsupportedCorners(predictedPosition.x, predictedPosition.z, groundY);
            }
        } catch (Throwable t) {
            Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
            for (int k = 1; k <= 5 && neededBlock == null; k++) {
                checkUnsupportedCorners(position.x + motion.x * (double) k, position.z + motion.z * (double) k, groundY);
            }
        }
    }

    private SimulatedPlayer createSimulation() {
        SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput);
        if (predictionInputPlayer == mc.thePlayer && !Float.isNaN(predictionInputYaw)) {
            simulatedPlayer.rotationYaw = predictionInputYaw;
        }
        simulatedPlayer.movementInput.sneak = false;
        return simulatedPlayer;
    }

    private void checkUnsupportedCorners(double playerX, double playerZ, int groundY) {
        Vec3 motion = new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
        double bestScore = -1.0E9;
        for (int corner = 0; corner < 4; corner++) {
            double offsetX = ((corner & 1) == 0) ? -0.3 : 0.3;
            double offsetZ = ((corner & 2) == 0) ? -0.3 : 0.3;
            int blockX = MathHelper.floor_double(playerX + offsetX);
            int blockZ = MathHelper.floor_double(playerZ + offsetZ);
            if (!isReplaceableAt(blockX, groundY, blockZ)) {
                continue;
            }
            double score = offsetX * motion.x + offsetZ * motion.z;
            double centerOffsetX = ((double) blockX + 0.5) - playerX;
            double centerOffsetZ = ((double) blockZ + 0.5) - playerZ;
            float cellYaw = (float) Math.toDegrees(Math.atan2(-centerOffsetX, centerOffsetZ));
            float yawOffset = Math.abs(MathHelper.wrapAngleTo180_float(cellYaw - lastSentYaw));
            score += 0.01 * (double) (180.0F - yawOffset) / 180.0;
            if (score > bestScore) {
                bestScore = score;
                neededBlock = new BlockPos(blockX, groundY, blockZ);
            }
        }
    }

    private FaceGeometry getFaceGeometry(Placement placement, float candidateYaw) {
        int blockX = placement.support.getX();
        int blockY = placement.support.getY();
        int blockZ = placement.support.getZ();
        EnumFacing face = placement.face;
        EntityPlayerSP player = mc.thePlayer;
        Vec3 position = player.getPositionVector();
        double eyeX = position.x;
        double eyeY = position.y + (double) player.getEyeHeight();
        double eyeZ = position.z;
        double directionX = -Math.sin(Math.toRadians(candidateYaw));
        double directionZ = Math.cos(Math.toRadians(candidateYaw));
        if (face == EnumFacing.UP) {
            double topY = (double) blockY + 1.0;
            if (eyeY <= topY + 0.05) {
                return null;
            }
            double nearDistance = 0.001;
            double farDistance = 4.0;
            if (Math.abs(directionX) < 1.0E-6) {
                if (eyeX < (double) blockX + 0.05 || eyeX > (double) blockX + 0.95) {
                    return null;
                }
            } else {
                double minEdgeIntersection = ((double) blockX + 0.05 - eyeX) / directionX;
                double maxEdgeIntersection = ((double) blockX + 0.95 - eyeX) / directionX;
                nearDistance = Math.max(nearDistance, Math.min(minEdgeIntersection, maxEdgeIntersection));
                farDistance = Math.min(farDistance, Math.max(minEdgeIntersection, maxEdgeIntersection));
            }
            if (Math.abs(directionZ) < 1.0E-6) {
                if (eyeZ < (double) blockZ + 0.05 || eyeZ > (double) blockZ + 0.95) {
                    return null;
                }
            } else {
                double minEdgeIntersection = ((double) blockZ + 0.05 - eyeZ) / directionZ;
                double maxEdgeIntersection = ((double) blockZ + 0.95 - eyeZ) / directionZ;
                nearDistance = Math.max(nearDistance, Math.min(minEdgeIntersection, maxEdgeIntersection));
                farDistance = Math.min(farDistance, Math.max(minEdgeIntersection, maxEdgeIntersection));
            }
            if (nearDistance >= farDistance) {
                return null;
            }
            return new FaceGeometry(true, nearDistance, farDistance, eyeY, topY);
        }
        if (face.getFrontOffsetY() != 0) {
            return null;
        }
        double distance;
        double hitCoordinate;
        double blockEdge;
        if (face.getFrontOffsetX() != 0) {
            double facePlane = (double) blockX + ((face.getFrontOffsetX() > 0) ? 1.0 : 0.0);
            if ((eyeX - facePlane) * (double) face.getFrontOffsetX() <= 0.0 || Math.abs(directionX) < 1.0E-6) {
                return null;
            }
            distance = (facePlane - eyeX) / directionX;
            hitCoordinate = eyeZ + directionZ * distance;
            blockEdge = blockZ;
        } else {
            double facePlane = (double) blockZ + ((face.getFrontOffsetZ() > 0) ? 1.0 : 0.0);
            if ((eyeZ - facePlane) * (double) face.getFrontOffsetZ() <= 0.0 || Math.abs(directionZ) < 1.0E-6) {
                return null;
            }
            distance = (facePlane - eyeZ) / directionZ;
            hitCoordinate = eyeX + directionX * distance;
            blockEdge = (double) blockX;
        }
        if (distance <= 0.0 || distance > 4.0) {
            return null;
        }
        if (hitCoordinate < blockEdge + 0.05 || hitCoordinate > blockEdge + 0.95) {
            return null;
        }
        return new FaceGeometry(false, distance, distance, eyeY, blockY);
    }

    private float findFacePitch(Placement placement, float candidateYaw) {
        FaceGeometry geometry = getFaceGeometry(placement, candidateYaw);
        if (geometry == null) {
            return Float.NaN;
        }
        double[] samples = geometry.topFace ? TOP_FACE_SAMPLES : SIDE_FACE_SAMPLES;
        for (double sample : samples) {
            float candidatePitch;
            if (geometry.topFace) {
                double sampleDistance = geometry.nearDistance + (geometry.farDistance - geometry.nearDistance) * sample;
                candidatePitch = (float) Math.toDegrees(Math.atan2(geometry.eyeY - geometry.surfaceY, sampleDistance));
            } else {
                candidatePitch = (float) Math.toDegrees(
                        Math.atan2(geometry.eyeY - (geometry.surfaceY + sample), geometry.nearDistance));
            }
            if (candidatePitch > 90.0F) {
                candidatePitch = 90.0F;
            }
            candidatePitch = quantizeRotation(candidatePitch);
            if (raycastFace(placement, candidateYaw, candidatePitch) != null) {
                return candidatePitch;
            }
        }
        return Float.NaN;
    }

    private float findNearestFacePitch(Placement placement, float candidateYaw, float currentPitch) {
        FaceGeometry geometry = getFaceGeometry(placement, candidateYaw);
        if (geometry == null) {
            return Float.NaN;
        }
        double minimumPitch;
        double maximumPitch;
        if (geometry.topFace) {
            minimumPitch = Math.toDegrees(Math.atan2(geometry.eyeY - geometry.surfaceY, geometry.farDistance));
            maximumPitch = Math.toDegrees(Math.atan2(geometry.eyeY - geometry.surfaceY, geometry.nearDistance));
        } else {
            minimumPitch = Math.toDegrees(Math.atan2(geometry.eyeY - (geometry.surfaceY + 0.95), geometry.nearDistance));
            maximumPitch = Math.toDegrees(Math.atan2(geometry.eyeY - (geometry.surfaceY + 0.05), geometry.nearDistance));
        }

        double margin = Math.min(0.35, (maximumPitch - minimumPitch) * 0.25);
        double innerMinimum = minimumPitch + margin;
        double innerMaximum = maximumPitch - margin;
        if (innerMinimum > innerMaximum) {
            innerMinimum = (minimumPitch + maximumPitch) * 0.5;
            innerMaximum = innerMinimum;
        }
        if ((double) currentPitch >= innerMinimum && (double) currentPitch <= innerMaximum
                && raycastFace(placement, candidateYaw, currentPitch) != null) {
            return currentPitch;
        }
        double desiredPitch = (double) currentPitch;
        double span = innerMaximum - innerMinimum;
        double step = Math.min(0.45, span * 0.30);
        if (desiredPitch < innerMinimum) {
            desiredPitch = innerMinimum + randomFloat(0.0F, (float) step);
        } else if (desiredPitch > innerMaximum) {
            desiredPitch = innerMaximum - randomFloat(0.0F, (float) step);
        }
        float candidatePitch = quantizeRotation(RotationUtils.clampPitch((float) desiredPitch));
        if (raycastFace(placement, candidateYaw, candidatePitch) != null) {
            return candidatePitch;
        }
        double[] samples = {0.5 + randomFloat(-0.06F, 0.06F), 0.3 + randomFloat(-0.05F, 0.05F),
                0.7 + randomFloat(-0.05F, 0.05F)};
        for (double sample : samples) {
            float samplePitch = quantizeRotation(RotationUtils.clampPitch((float) (minimumPitch + (maximumPitch - minimumPitch) * sample)));
            if (raycastFace(placement, candidateYaw, samplePitch) != null) {
                return samplePitch;
            }
        }
        return Float.NaN;
    }

    private int getCandidateScore(int index) {
        if (neededBlock == null) {
            return 0;
        }
        Placement candidate = candidates[index];
        int targetX = candidate.support.getX() + candidate.face.getFrontOffsetX();
        int targetZ = candidate.support.getZ() + candidate.face.getFrontOffsetZ();
        if (targetX == neededBlock.getX() && targetZ == neededBlock.getZ()) {
            return 2;
        }
        int distance = Math.abs(targetX - neededBlock.getX()) + Math.abs(targetZ - neededBlock.getZ());
        return (distance == 1) ? 1 : 0;
    }

    private void resetNinjaState() {
        resetNinjaPlacementAim();
        ninjaLastDiagonalSide = 0;
        ninjaSneaking = false;
        ninjaReleaseAt = 0L;
        ninjaJumpSneakUntil = 0L;
        ninjaJumpKeyWasDown = false;
        ninjaTrackedSupportBlock = null;
        ninjaEdgePulseSupport = null;
        ninjaSneakSafeTicks = 0;
    }

    // A stable support identifier is needed only for the optional release
    // hysteresis; it must NEVER prevent a necessary new edge sneak.
    private BlockPos findNinjaSupportBlock(EntityPlayerSP player) {
        if (player == null) return null;
        int y = MathHelper.floor_double(player.posY - 0.01D);
        int centerX = MathHelper.floor_double(player.posX);
        int centerZ = MathHelper.floor_double(player.posZ);
        BlockPos best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                if (!isSolidSupport(x, y, z)) continue;
                double nearX = MathHelper.clamp_double(player.posX, x, x + 1.0D);
                double nearZ = MathHelper.clamp_double(player.posZ, z, z + 1.0D);
                double dx = player.posX - nearX;
                double dz = player.posZ - nearZ;
                double distance = dx * dx + dz * dz;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = new BlockPos(x, y, z);
                }
            }
        }
        return best;
    }

    private Vec3 simulateNinjaUnsneakedPosition(PrePlayerInputEvent input) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || input == null) return null;
        try {
            // Predict the next tick *before* Minecraft applies the movement.
            // This is the key principle of the supplied Legit Scaffold script.
            Simulation sim = Simulation.create();
            float forward = input.getForward();
            float strafe = input.getStrafe();
            // If already sneak-slowed, restore the original input for the
            // hypothetical unsneaked movement. Never amplify an input >0.3.
            if (client.isSneak() && Math.abs(forward) <= 0.301F
                    && Math.abs(strafe) <= 0.301F) {
                forward /= 0.3F;
                strafe /= 0.3F;
            }
            sim.setForward(MathHelper.clamp_float(forward, -1.0F, 1.0F));
            sim.setStrafe(MathHelper.clamp_float(strafe, -1.0F, 1.0F));
            sim.setJump(input.isJump());
            sim.setSneak(false);
            sim.tick();
            Vec3 result = sim.getPosition();
            if (result != null && !Double.isNaN(result.x) && !Double.isNaN(result.z)
                    && !Double.isInfinite(result.x) && !Double.isInfinite(result.z)) {
                return result;
            }
        } catch (Exception ignored) {
            // A failed simulator must not make Ninja lose its edge protection.
        }
        // Conservative one-tick fallback: use real horizontal motion.
        return new Vec3(player.posX + player.motionX, player.posY,
                player.posZ + player.motionZ);
    }

    // Legit Scaffold's four-corner measurement. The floor Y is the block just
    // below the feet, and coordinates must be floored (not integer-cast).
    private double computeNinjaEdgeOffset(Vec3 predicted, Vec3 current) {
        if (predicted == null || current == null) return Double.NaN;
        int floorY = MathHelper.floor_double(current.y - 0.01D);
        double best = Double.NaN;
        double[] corners = {-0.30D, 0.30D};
        for (double cornerX : corners) {
            for (double cornerZ : corners) {
                int bx = MathHelper.floor_double(current.x + cornerX);
                int bz = MathHelper.floor_double(current.z + cornerZ);
                if (!isSolidSupport(bx, floorY, bz)) continue;
                double offX = Math.abs(predicted.x - (bx +
                        (predicted.x < bx + 0.5D ? 0.0D : 1.0D)));
                double offZ = Math.abs(predicted.z - (bz +
                        (predicted.z < bz + 0.5D ? 0.0D : 1.0D)));
                boolean xDiff = MathHelper.floor_double(predicted.x) != bx;
                boolean zDiff = MathHelper.floor_double(predicted.z) != bz;
                double distance = xDiff ? (zDiff ? Math.max(offX, offZ) : offX)
                        : (zDiff ? offZ : 0.0D);
                best = Double.isNaN(best) ? distance : Math.min(best, distance);
            }
        }
        return best;
    }

    private long getNinjaRandomDelay() {
        double min = Math.max(0.0D, ninjaMinDelay.getInput());
        double max = Math.max(0.0D, ninjaMaxDelay.getInput());
        if (max < min) {
            double swap = min;
            min = max;
            max = swap;
        }
        return Math.round(min + Math.random() * (max - min));
    }

    private void updateNinjaSneak(PrePlayerInputEvent input) {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || input == null) {
            resetNinjaState();
            return;
        }

        boolean manualSneak = keybinds.isKeyDown(keybinds.getKeyCode("sneak"));
        boolean requireSneakKey = ninjaRequireSneakKey.isToggled();
        boolean moving = Math.abs(input.getForward()) > 1.0E-4F
                || Math.abs(input.getStrafe()) > 1.0E-4F;
        boolean jumping = input.isJump() || isJumpPressed();
        boolean jumpStarted = jumping && !ninjaJumpKeyWasDown;
        ninjaJumpKeyWasDown = jumping;

        // By default the player's own Sneak always wins. In optional
        // "Sneak key pressed" mode, Ninja acts as a smart edge-controlled
        // replacement while the physical Sneak key is held.
        if (isTowerActive() || (!requireSneakKey && manualSneak)
                || (requireSneakKey && !manualSneak)) {
            ninjaSneaking = false;
            ninjaReleaseAt = 0L;
            ninjaJumpSneakUntil = 0L;
            ninjaTrackedSupportBlock = null;
            ninjaEdgePulseSupport = null;
            ninjaSneakSafeTicks = 0;
            return;
        }
        if (requireSneakKey && !moving) {
            ninjaSneaking = false;
            ninjaReleaseAt = 0L;
            ninjaJumpSneakUntil = 0L;
            ninjaEdgePulseSupport = null;
            ninjaSneakSafeTicks = 0;
            // Keep the manually requested sneak when standing still.
            input.setSneak(true);
            return;
        }

        boolean allowed = (!ninjaRequireBlocks.isToggled()
                    || isValidBlock(getPlacementStack(player)))
                && (!ninjaRequireLookingDown.isToggled()
                    || player.rotationPitch >= 70.0F)
                && (!ninjaDisableForward.isToggled() || input.getForward() <= 0.0F);
        if (!allowed) {
            ninjaSneaking = false;
            ninjaReleaseAt = 0L;
            ninjaJumpSneakUntil = 0L;
            ninjaEdgePulseSupport = null;
            ninjaSneakSafeTicks = 0;
            if (requireSneakKey) input.setSneak(true);
            return;
        }

        long now = client.time();
        double jumpSneakMs = Math.max(0.0D, ninjaSneakOnJump.getInput());
        // Rising-edge detection: holding Space cannot restart this timer on
        // every grounded tick, and airborne ticks cannot extend it forever.
        if (jumpStarted && player.onGround && moving && jumpSneakMs > 0.0D) {
            ninjaJumpSneakUntil = now + Math.round(jumpSneakMs);
            ninjaReleaseAt = 0L;
        }

        // Compute a full unsneaked future tick, like the source Legit Scaffold.
        Vec3 current = player.getPositionVector();
        Vec3 predicted = simulateNinjaUnsneakedPosition(input);
        double edgeOffset = computeNinjaEdgeOffset(predicted, current);
        double threshold = MathHelper.clamp_double(ninjaEdgeDistance.getInput(), 0.0D, 0.30D);
        boolean edgeReached;
        if (Double.isNaN(edgeOffset)) {
            edgeReached = player.onGround && isNearEdge(1, input, threshold);
        } else {
            debugOverhang = edgeOffset;
            edgeReached = player.onGround && edgeOffset > threshold;
        }
        // The four-corner overhang can still appear safe while moving
        // diagonally across a corner. Confirm actual support at the predicted
        // destination before deciding to release Sneak on S+D/S+A.
        if (player.onGround && predicted != null
                && getForwardInput() != 0 && getStrafeInput() != 0) {
            int supportY = MathHelper.floor_double(player.posY - 0.01D);
            if (!hasCornerSupport(predicted.x, predicted.z, supportY)) {
                edgeReached = true;
            }
        }
        BlockPos support = findNinjaSupportBlock(player);
        boolean changedSupport = support != null && !support.equals(ninjaTrackedSupportBlock);
        ninjaTrackedSupportBlock = support;
        if (changedSupport) ninjaSneakSafeTicks = 0;

        // Slinky BridgeAssist's useful idea is ONE sneak pulse per supporting
        // block. Never use that guard to suppress an actual unsafe edge sneak.
        // Instead, keep the sneak latched until a safely supported transition.
        boolean safePredicted = predicted != null && hasCornerSupport(predicted.x,
                predicted.z, MathHelper.floor_double(player.posY - 0.01D));
        boolean nearReleaseEdge = !Double.isNaN(edgeOffset)
                && edgeOffset > Math.max(0.0D, threshold - 0.05D);
        boolean samePulseSupport = support != null && support.equals(ninjaEdgePulseSupport);
        boolean shouldHold = edgeReached || ninjaJumpSneakUntil > now
                || (ninjaSneaking && ninjaAvoidDoubleSneaking.isToggled()
                    && samePulseSupport && !changedSupport && nearReleaseEdge);
        if (shouldHold) {
            ninjaSneaking = true;
            ninjaEdgePulseSupport = support;
            ninjaSneakSafeTicks = 0;
            ninjaReleaseAt = 0L;
        } else if (ninjaSneaking) {
            ninjaSneakSafeTicks = Math.min(6, ninjaSneakSafeTicks + 1);
            boolean avoidDouble = ninjaAvoidDoubleSneaking.isToggled();
            // A single false-safe simulation tick must not release Sneak and
            // immediately cause a second edge pulse on the same block.
            boolean stable = safePredicted && (!avoidDouble
                    || changedSupport || ninjaSneakSafeTicks >= 2);
            if (stable) {
                if (ninjaReleaseAt == 0L) ninjaReleaseAt = now + getNinjaRandomDelay();
                if (now >= ninjaReleaseAt) {
                    ninjaSneaking = false;
                    ninjaReleaseAt = 0L;
                    ninjaSneakSafeTicks = 0;
                }
            } else {
                ninjaReleaseAt = 0L;
            }
        } else {
            ninjaSneakSafeTicks = 0;
            ninjaEdgePulseSupport = null;
        }
        if (ninjaJumpSneakUntil != 0L && now >= ninjaJumpSneakUntil) {
            ninjaJumpSneakUntil = 0L;
        }

        if (requireSneakKey) {
            // In this mode the physical key is the permission, not a forced
            // permanent slowdown. Override only the movement-input snapshot.
            input.setSneak(ninjaSneaking);
        } else if (ninjaSneaking) {
            input.setSneak(true);
        }
    }

    private void resetRushState() {
        rushLastPlacedTarget = null;
        rushPlannedPlacement = null;
        rushTieXNext = true;
        rushHeadingX = 0;
        rushHeadingZ = 0;
        rushMissStreak = 0;
        rushWarmTarget = null;
        rushWarmFace = null;
        rushWarmTicks = 0;
        rushLaneYaw = Float.NaN;
        rushLanePitch = Float.NaN;
        rushLaneAge = 0;
        towerTick = 0;
        towerDelay = 0;
        towerLastPlacedY = Integer.MIN_VALUE;
        towerPrevMotionY = 0.0D;
        towerWasActive = false;
        towerCycle = 0;
        towerLaunchY = 0.0D;
        towerSpoofGroundThisTick = false;
    }

    private BlockPos findFrontSupport(Vec3 position, int supportY, int headingX, int headingZ) {
        if (position == null) return null;
        int minX = MathHelper.floor_double(position.x - 0.30D);
        int maxX = MathHelper.floor_double(position.x + 0.30D);
        int minZ = MathHelper.floor_double(position.z - 0.30D);
        int maxZ = MathHelper.floor_double(position.z + 0.30D);
        double norm = Math.sqrt((double) headingX * headingX + (double) headingZ * headingZ);
        double hx = norm <= 0.0D ? 0.0D : headingX / norm;
        double hz = norm <= 0.0D ? 0.0D : headingZ / norm;
        BlockPos best = null;
        double bestScore = -1.0E12D;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!isSolidSupport(x, supportY, z) || isInteractableAt(x, supportY, z)) continue;
                double overlapX = Math.max(0.0D, Math.min(position.x + 0.30D, x + 1.0D)
                        - Math.max(position.x - 0.30D, x));
                double overlapZ = Math.max(0.0D, Math.min(position.z + 0.30D, z + 1.0D)
                        - Math.max(position.z - 0.30D, z));
                double area = overlapX * overlapZ;
                if (area <= 0.0D) continue;
                double front = ((x + 0.5D) - position.x) * hx + ((z + 0.5D) - position.z) * hz;
                double score = area * 10.0D + front;
                if (best == null || score > bestScore) {
                    bestScore = score;
                    best = new BlockPos(x, supportY, z);
                }
            }
        }
        return best;
    }

    private int[] getFrontHeading(float cameraYaw) {
        int forward = getForwardInput();
        int strafe = getStrafeInput();
        if (forward == 0 && strafe == 0) return new int[]{0, 0};
        float movementYaw = getMovementYaw(cameraYaw, forward, strafe);
        int octant = Math.round(movementYaw / 45.0F);
        double radians = Math.toRadians((double) octant * 45.0D);
        int dx = (int) Math.round(-Math.sin(radians));
        int dz = (int) Math.round(Math.cos(radians));
        return new int[]{MathHelper.clamp_int(dx, -1, 1), MathHelper.clamp_int(dz, -1, 1)};
    }

    private EnumFacing getFrontFace(int stepX, int stepZ) {
        if (stepX > 0) return EnumFacing.EAST;
        if (stepX < 0) return EnumFacing.WEST;
        if (stepZ > 0) return EnumFacing.SOUTH;
        if (stepZ < 0) return EnumFacing.NORTH;
        return null;
    }

    private Placement makeFrontStep(BlockPos support, int stepX, int stepZ) {
        if (support == null) return null;
        EnumFacing face = getFrontFace(stepX, stepZ);
        if (face == null) return null;
        Placement placement = new Placement(support, face);
        BlockPos target = placement.getTarget();
        if (!isReplaceableAt(target.getX(), target.getY(), target.getZ())) return null;
        return placement;
    }

    private Vec3 getFrontFaceCenter(Placement placement) {
        if (placement == null || placement.face == null) return null;
        return new Vec3(
                placement.support.getX() + 0.5D + placement.face.getFrontOffsetX() * 0.5D,
                placement.support.getY() + 0.5D + placement.face.getFrontOffsetY() * 0.5D,
                placement.support.getZ() + 0.5D + placement.face.getFrontOffsetZ() * 0.5D);
    }

    private double frontProjection(BlockPos block, Vec3 position, int dx, int dz) {
        double norm = Math.sqrt((double) dx * dx + (double) dz * dz);
        if (norm <= 0.0D || block == null || position == null) return 0.0D;
        double hx = dx / norm;
        double hz = dz / norm;
        return ((block.getX() + 0.5D) - position.x) * hx
                + ((block.getZ() + 0.5D) - position.z) * hz;
    }

    private BlockPos getFrontAnchor(Vec3 position, int supportY, int dx, int dz,
                                    BlockPos remembered, double maxDistance) {
        if (remembered != null
                && isSolidSupport(remembered.getX(), remembered.getY(), remembered.getZ())) {
            double rx = remembered.getX() + 0.5D - position.x;
            double rz = remembered.getZ() + 0.5D - position.z;
            if (rx * rx + rz * rz <= maxDistance * maxDistance) {
                return remembered;
            }
        }
        return findFrontSupport(position, supportY, dx, dz);
    }

    private Placement planRushPlacement(Vec3 position, float cameraYaw) {
        if (position == null || mc.thePlayer == null || placementDelayTicks > 0) return null;

        int[] heading = getFrontHeading(cameraYaw);
        int dx = heading[0], dz = heading[1];
        if (dx == 0 && dz == 0) return null;

        if (dx != rushHeadingX || dz != rushHeadingZ) {
            rushHeadingX = dx;
            rushHeadingZ = dz;
            rushLastPlacedTarget = null;
            rushTieXNext = Math.abs(mc.thePlayer.motionX) >= Math.abs(mc.thePlayer.motionZ);
            rushLaneYaw = Float.NaN;
            rushLanePitch = Float.NaN;
            rushLaneAge = 0;
        }

        int floorY = MathHelper.floor_double(position.y);
        int supportY = ((keepYStage != 0 && !useCurrentHeight)
                ? Math.min(floorY, keepYStartHeight) : floorY) - 1;

        double requestedLook = rushLookAhead == null ? 2.10D : rushLookAhead.getInput();
        double horizontalSpeed = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX
                + mc.thePlayer.motionZ * mc.thePlayer.motionZ);

        double safeLook = 1.55D + horizontalSpeed * 2.35D;
        double look = Math.max(1.55D, Math.min(requestedLook, safeLook));
        BlockPos anchor = getFrontAnchor(position, supportY, dx, dz,
                rushLastPlacedTarget, 4.05D);
        if (anchor == null) return null;

        double norm = Math.sqrt((double) dx * dx + (double) dz * dz);
        double hx = dx / norm;
        double hz = dz / norm;

        double futureX = position.x + hx * look + mc.thePlayer.motionX * 0.18D;
        double futureZ = position.z + hz * look + mc.thePlayer.motionZ * 0.18D;
        int targetX = MathHelper.floor_double(futureX);
        int targetZ = MathHelper.floor_double(futureZ);

        for (int scan = 0; scan < 5; scan++) {
            int remX = targetX - anchor.getX();
            int remZ = targetZ - anchor.getZ();
            if (remX == 0 && remZ == 0) return null;

            int stepX = 0;
            int stepZ = 0;
            int absX = Math.abs(remX);
            int absZ = Math.abs(remZ);

            if (absX > absZ) {
                stepX = remX > 0 ? 1 : -1;
            } else if (absZ > absX) {
                stepZ = remZ > 0 ? 1 : -1;
            } else if (absX > 0) {
                if (rushTieXNext) stepX = remX > 0 ? 1 : -1;
                else stepZ = remZ > 0 ? 1 : -1;
                rushTieXNext = !rushTieXNext;
            }

            EnumFacing face = getFrontFace(stepX, stepZ);
            if (face == null) return null;

            BlockPos nextCell = anchor.offset(face);
            if (isSolidSupport(nextCell.getX(), nextCell.getY(), nextCell.getZ())
                    && !isInteractableAt(nextCell.getX(), nextCell.getY(), nextCell.getZ())) {
                anchor = nextCell;
                rushLastPlacedTarget = nextCell;
                continue;
            }

            Placement planned = makeFrontStep(anchor, stepX, stepZ);
            if (planned == null) {

                if (stepX != 0 && remZ != 0) {
                    planned = makeFrontStep(anchor, 0, remZ > 0 ? 1 : -1);
                } else if (stepZ != 0 && remX != 0) {
                    planned = makeFrontStep(anchor, remX > 0 ? 1 : -1, 0);
                }
            }

            if (planned == null || !isPlacementWithinReach(planned, 4.05D)) return null;

            double lead = frontProjection(planned.getTarget(), position, dx, dz);
            if (lead > look + 0.85D) return null;
            return planned;
        }

        return null;
    }

    private boolean isPlacementWithinReach(Placement placement, double reach) {
        if (placement == null || mc.thePlayer == null) return false;
        Vec3 hit = getFrontFaceCenter(placement);
        if (hit == null) return false;
        double dx = hit.x - mc.thePlayer.posX;
        double dy = hit.y - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dz = hit.z - mc.thePlayer.posZ;
        return dx * dx + dy * dy + dz * dz <= reach * reach;
    }

    private Vec3 aimRushPlacement(Placement placement, float cameraYaw) {
        if (placement == null || !isPlacementWithinReach(placement, 4.00D)) return null;

        EntityPlayerSP player = mc.thePlayer;
        Vec3 faceCenter = getFrontFaceCenter(placement);
        double dx = faceCenter.x - player.posX;
        double dz = faceCenter.z - player.posZ;
        float centerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float moveYaw = getMovementYaw(cameraYaw, getForwardInput(), getStrafeInput());
        float laneBase = moveYaw + 180.0F;

        float[] yawChoices = new float[]{
                rushLaneYaw,
                lastSentYaw,
                laneBase,
                laneBase - 6.0F,
                laneBase + 6.0F,
                centerYaw,
                laneBase - 12.0F,
                laneBase + 12.0F
        };

        for (float rawYaw : yawChoices) {
            if (Float.isNaN(rawYaw)) continue;
            float candidateYaw = quantizeMouseRotation(
                    unwrapYaw(rawYaw, lastSentYaw), lastSentYaw);
            float pitchBase = Float.isNaN(rushLanePitch) ? 80.0F : rushLanePitch;
            float candidatePitch = findNearestFacePitch(placement, candidateYaw, pitchBase);
            if (Float.isNaN(candidatePitch)) {
                candidatePitch = findFacePitch(placement, candidateYaw);
            }
            if (Float.isNaN(candidatePitch)) continue;

            candidatePitch = RotationUtils.clampPitch(
                    quantizeMouseRotation(candidatePitch, lastSentPitch));
            Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
            if (hit != null) {
                targetYaw = candidateYaw;
                targetPitch = candidatePitch;
                pendingYawJitter = 0.0F;
                rushLaneYaw = candidateYaw;
                rushLanePitch = candidatePitch;
                rushLaneAge = Math.min(40, rushLaneAge + 1);
                return hit;
            }
        }

        rushLaneAge = 0;
        return null;
    }

    private void runRushVerifiedChain(Vec3 position, float cameraYaw) {
        if (!placedThisTick || mc.thePlayer == null || placementDelayTicks > 0) return;
        int desired = rushChain == null ? 2 : MathHelper.clamp_int((int) Math.round(rushChain.getInput()), 1, 3);
        int limit = Math.min(3, desired);

        while (placementAttemptsThisTick < limit) {
            Placement next = planRushPlacement(mc.thePlayer.getPositionVector(), cameraYaw);
            if (next == null || !isPlacementWithinReach(next, 3.95D)) return;
            Vec3 hit = raycastFace(next, lastSentYaw, lastSentPitch);
            if (hit == null) return;

            rushPlannedPlacement = next;
            if (!placeBlock(next)) {
                rushPlannedPlacement = null;
                return;
            }
        }
    }

    private void updateTowerSession(boolean active) {
        if (active && !towerWasActive) {
            towerTick = 0;
            towerDelay = 0;
            towerLastPlacedY = Integer.MIN_VALUE;
            towerPrevMotionY = mc.thePlayer == null ? 0.0D : mc.thePlayer.motionY;
            towerCycle = 0;
            towerLaunchY = mc.thePlayer == null ? 0.0D : mc.thePlayer.posY;
        } else if (!active && towerWasActive) {
            towerTick = 0;
            towerDelay = 0;
            towerLastPlacedY = Integer.MIN_VALUE;
            towerPrevMotionY = 0.0D;
            towerCycle = 0;
            towerLaunchY = 0.0D;
            rushPlannedPlacement = null;
        }
        towerWasActive = active;
    }

    private String getTowerMode() {
        if (towerMode == null) return "Vanilla";
        // Profiles from the experimental LB Tower builds can still store
        // indices 21/22. Restore them to the tested Raven Prediction slot.
        if (towerMode.getInput() >= TOWER_OPTIONS.length || towerMode.getInput() < 0) {
            modules.setSlider(scriptName, towerMode.name, 7.0D);
            return "Raven Prediction";
        }
        String mode = towerMode.getSelectedOption();
        return mode == null || mode.length() == 0 ? "Vanilla" : mode;
    }

    private boolean isTowerActive() {
        if (mc.thePlayer == null) return false;
        if (!Utils.isBindDown(mc.gameSettings.keyBindJump)) return false;

        if (isAnyMovementKeyDown()) return false;
        return isValidBlock(getPlacementStack(mc.thePlayer));
    }

    private boolean towerOwnsJump() {
        if (!isTowerActive()) return false;
        String mode = getTowerMode();
        return !mode.equals("Vanilla");
    }

    private Placement findTowerPlacement(Vec3 position) {
        if (position == null || mc.thePlayer == null || placementDelayTicks > 0) return null;

        double px = position.x + mc.thePlayer.motionX * 0.30D;
        double pz = position.z + mc.thePlayer.motionZ * 0.30D;
        int startY = MathHelper.floor_double(position.y - 0.01D);

        int minX = MathHelper.floor_double(px - 0.29D);
        int maxX = MathHelper.floor_double(px + 0.29D);
        int minZ = MathHelper.floor_double(pz - 0.29D);
        int maxZ = MathHelper.floor_double(pz + 0.29D);

        Placement best = null;
        double bestScore = -1.0E12D;

        for (int y = startY; y >= startY - 3; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!isSolidSupport(x, y, z) || isInteractableAt(x, y, z)) continue;

                    int targetY = y + 1;
                    if (targetY <= towerLastPlacedY) continue;
                    if (!isReplaceableAt(x, targetY, z)) continue;

                    if (position.y < targetY + 1.001D) continue;

                    double overlapX = Math.max(0.0D, Math.min(px + 0.30D, x + 1.0D)
                            - Math.max(px - 0.30D, x));
                    double overlapZ = Math.max(0.0D, Math.min(pz + 0.30D, z + 1.0D)
                            - Math.max(pz - 0.30D, z));
                    double area = overlapX * overlapZ;
                    if (area <= 0.0D) continue;

                    Placement candidate = new Placement(new BlockPos(x, y, z), EnumFacing.UP);
                    if (!isPlacementWithinReach(candidate, 4.35D)) continue;

                    double centerDx = x + 0.5D - px;
                    double centerDz = z + 0.5D - pz;
                    double score = area * 20.0D - centerDx * centerDx - centerDz * centerDz + y * 0.01D;
                    if (best == null || score > bestScore) {
                        best = candidate;
                        bestScore = score;
                    }
                }
            }
            if (best != null) break;
        }

        return best;
    }

    private Vec3 aimTowerFace(Placement placement, float cameraYaw) {
        if (placement == null || placement.face != EnumFacing.UP) return null;

        EntityPlayerSP player = mc.thePlayer;
        Vec3 center = getFrontFaceCenter(placement);
        double dx = center.x - player.posX;
        double dz = center.z - player.posZ;
        float centerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        float[] yawChoices = new float[]{cameraYaw, lastSentYaw, centerYaw, cameraYaw + 180.0F};
        for (float rawYaw : yawChoices) {
            float candidateYaw = quantizeMouseRotation(
                    unwrapYaw(rawYaw, lastSentYaw), lastSentYaw);
            float candidatePitch = findNearestFacePitch(placement, candidateYaw, 86.0F);
            if (Float.isNaN(candidatePitch)) {
                candidatePitch = findFacePitch(placement, candidateYaw);
            }
            if (Float.isNaN(candidatePitch)) continue;

            candidatePitch = RotationUtils.clampPitch(
                    quantizeMouseRotation(candidatePitch, lastSentPitch));
            Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
            if (hit != null) {
                targetYaw = candidateYaw;
                targetPitch = candidatePitch;
                pendingYawJitter = 0.0F;
                return hit;
            }
        }
        return null;
    }

    private void setTowerMotion(double motionY) {
        if (mc.thePlayer == null) return;

        client.setMotion(0.0D, motionY, 0.0D);
        mc.thePlayer.motionX = 0.0D;
        mc.thePlayer.motionY = motionY;
        mc.thePlayer.motionZ = 0.0D;
    }

    private void updateTowerMotion() {
        towerSpoofGroundThisTick = false;
        if (!isTowerActive() || mc.thePlayer == null) {
            towerTick = 0;
            towerDelay = 0;
            towerPrevMotionY = 0.0D;
            return;
        }

        String mode = getTowerMode();
        if (mode.equals("Vanilla")) {
            towerTick = 0;
            towerDelay = 0;
            towerPrevMotionY = mc.thePlayer.motionY;
            return;
        }

        if (mc.thePlayer.onGround && towerTick == 0) {
            towerCycle++;
            towerLaunchY = mc.thePlayer.posY;
        }

        if (mode.equals("Sync")) {
            runSyncTower(false);
        } else if (mode.equals("Hypixel")) {
            runHypixelTower();
        } else if (mode.equals("Apex")) {
            runApexTower();
        } else if (mode.equals("Sync Burst")) {
            if (towerCycle % 3 == 0) runPulseTower(false);
            else runSyncTower(false);
        } else if (mode.equals("Sync Early Burst")) {
            if (towerCycle % 3 == 0) runPulseVariant(0.64D, 1.015D, 0.13D, -0.10D);
            else runSyncTower(false);
        } else if (mode.equals("Raven 1 Tick")) {
            runRaven1Tower();
        } else if (mode.equals("Raven Prediction")) {
            runRavenPredictionTower();
        } else if (mode.equals("OnGround")) {
            runOnGroundTower();
        } else if (mode.equals("OnGround Early")) {
            runOnGroundEarlyTower();
        } else if (mode.equals("OnGround Pulse")) {
            runOnGroundPulseTower();
        } else if (mode.equals("OnGround Prediction")) {
            runOnGroundPredictionTower();
        } else if (mode.equals("Yuri NCP")) {
            runYuriNCPTower();
        } else if (mode.equals("Raven Pull Sync")) {
            runRavenPullSyncTower();
        } else if (mode.equals("Raven Pull")) {
            runRavenPullTower();
        } else if (mode.equals("Raven Predict Early")) {
            runRavenPredictionAcceleratedTower(false);
        } else if (mode.equals("Raven Predict Turbo")) {
            runRavenPredictionAcceleratedTower(true);
        } else if (mode.equals("Raven Prediction Swift")) {
            runRavenPredictionVariant(0.10D, -0.38D, 1.04D);
        } else if (mode.equals("Raven Prediction Rapid")) {
            runRavenPredictionVariant(0.17D, -0.42D, 1.005D);
        } else if (mode.equals("Polar")) {
            runPolarTower();
        } else if (mode.equals("Intave")) {
            runIntaveTower();
        }

        towerPrevMotionY = mc.thePlayer.motionY;
    }

    private double towerFraction() {
        double fraction = mc.thePlayer.posY - Math.floor(mc.thePlayer.posY);
        return fraction < 0.0D ? fraction + 1.0D : fraction;
    }

    private void runSyncTower(boolean unused) {
        double fraction = towerFraction();
        if (mc.thePlayer.onGround && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick > 0 && mc.thePlayer.motionY <= 0.0D && fraction <= 0.12D) {
            setTowerMotion(-0.08D);
            towerTick = 0;
        }
    }

    private void runPulseTower(boolean unused) {
        runPulseVariant(0.68D, 1.02D, 0.14D, -0.12D);
    }

    private void runHypixelTower() {
        if (hasJumpPotion()) {
            towerTick = 0;
            towerDelay = 0;
            return;
        }
        if (mc.thePlayer.onGround) {
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
            setTowerMotion(mc.thePlayer.motionY - 0.03D);
        } else if (towerDelay == 5) {
            setTowerMotion(mc.thePlayer.motionY - 0.50D);
        }
    }

    private void runPulseVariant(double triggerFraction, double targetHeight,
                                 double resetFraction, double catchMotion) {
        double fraction = towerFraction();
        if (mc.thePlayer.onGround && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= triggerFraction && mc.thePlayer.motionY > 0.0D) {
            setTowerMotion(Math.max(0.12D, targetHeight - fraction));
            towerTick = 2;
        } else if (towerTick == 2 && fraction <= resetFraction) {
            setTowerMotion(catchMotion);
            towerTick = 0;
        }
    }

    private void runApexTower() {
        if (mc.thePlayer.onGround && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && towerPrevMotionY > 0.0D && mc.thePlayer.motionY <= 0.0D) {
            setTowerMotion(-0.20D);
            towerTick = 2;
        } else if (towerTick == 2 && mc.thePlayer.onGround) {
            towerTick = 0;
        }
    }

    private void runRaven1Tower() {
        if (mc.thePlayer.onGround) towerTick = 0;
        int tick = towerTick++;
        double fraction = towerFraction();
        if (tick == 0) setTowerMotion(0.42D);
        else if (tick == 1) setTowerMotion(0.33D);
        else if (tick == 2) setTowerMotion(Math.max(0.005D, 1.0D - fraction));
        else if (tick == 3) setTowerMotion(0.005D);
    }

    // Yuri Polar: apply additional downward acceleration only when falling
    // and close to a solid block below the player. In this standalone Raven
    // script towerOwnsJump() suppresses vanilla jump, so we launch on ground.
    private void runPolarTower() {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.onGround) {
            setTowerMotion(0.42D);
            towerTick = 1;
            return;
        }
        if (mc.thePlayer.motionY >= 0.0D) return;
        double feetY = mc.thePlayer.posY;
        int firstY = MathHelper.floor_double(feetY - 0.01D);
        for (int y = firstY; y >= firstY - 3; y--) {
            if (!hasCornerSupport(mc.thePlayer.posX, mc.thePlayer.posZ, y)) continue;
            double distanceToGround = feetY - (y + 1.0D);
            if (distanceToGround >= 0.0D && distanceToGround < 1.26D)
                setTowerMotion(mc.thePlayer.motionY - 0.091F);
            return;
        }
    }

    // Intave: placement-gated pulse tower. The former Pulse Hyper preset
    // pulled down at a Y fraction without checking whether a block existed;
    // that sometimes left the player too low to place the next support.
    // The new cycle boosts to the next level, confirms a placed support, and
    // only then applies the short landing pull. No teleports or ground spoof.
    private void runIntaveTower() {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;
        if (player.onGround) {
            towerLaunchY = Math.floor(player.posY + 1.0E-4D);
            towerTick = 1;
            towerDelay = 0;
            setTowerMotion(0.42D);
            return;
        }
        if (towerTick <= 0) return;

        double rise = player.posY - towerLaunchY;
        int supportY = MathHelper.floor_double(towerLaunchY);
        boolean placedSupport = towerLastPlacedY >= supportY
                && hasCornerSupport(player.posX, player.posZ, supportY);

        // Reach the top of the next full block in one controlled step.
        // Compare absolute rise (not modulo fractional Y): a fraction wraps
        // around at 1.0 and caused the old pulse to reset too early.
        if (towerTick == 1 && rise >= 0.65D && rise < 0.99D && player.motionY > 0.0D) {
            // Fixed preset: no additional Intave settings in the GUI.
            double liftMotion = MathHelper.clamp_double(1.045D - rise, 0.14D, 0.38D);
            setTowerMotion(liftMotion);
            towerTick = 2;
            return;
        }

        // Only pull once the block is in the world and our feet have actually
        // cleared its top. Otherwise let vanilla physics continue, allowing
        // delayed placements to catch up instead of dropping through air.
        if (placedSupport && rise >= 1.001D && rise < 1.6D && towerTick <= 2) {
            setTowerMotion(-0.48D);
            towerTick = 3;
            return;
        }

        // Re-arm only after actual ground contact; this cannot relaunch in
        // mid-air if the server rejects or delays a placement.
        if (rise < -0.25D || player.motionY < -0.75D) {
            towerTick = 4;
        }
    }

    // A server-rejected tower block may disappear after the local right-click
    // succeeded. If we are grounded but the next slot is still empty, allow
    // findTowerPlacement() to retry that height instead of skipping it forever.
    

    // Prediction Step: a placement-first variation of Raven Prediction.
    // The former implementation teleported the feet back to integer Y and
    // relaunched in mid-air, which could desync and leave towerTick stuck.
    // Instead, a short lift opens the normal tower-placement window, then
    // we land on the block in the ordinary collision simulation.
    

    // Prediction Pull: the working Raven Prediction jump with a slightly
    // earlier return once the actual block beneath the player is solid.
    // Never teleport, spoof onGround, or pull into unplaced air.
    

    private void runRavenPredictionTower() {
        if (mc.thePlayer.onGround) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick > 0 && mc.thePlayer.motionY <= 0.0D && mc.thePlayer.motionY >= -0.09D) {
            setTowerMotion(-0.38D);
            towerTick = 0;
        }
    }

    // Experimental variants of the proven Raven Prediction tower.
    // Keep its launch and fallback untouched; pull earlier only after
    // the next landing block has actually appeared in the client world.
    // No position teleports, extra packets or synthetic onGround states.
    private void runRavenPredictionAcceleratedTower(boolean turbo) {
        if (mc.thePlayer.onGround) {
            towerLaunchY = mc.thePlayer.posY;
            setTowerMotion(0.42D);
            towerTick = 1;
            return;
        }
        if (towerTick != 1) return;

        final double gained = mc.thePlayer.posY - towerLaunchY;
        final int expectedBlockY = MathHelper.floor_double(towerLaunchY + 1.0E-4D);
        final boolean blockReady = towerLastPlacedY >= expectedBlockY
                && hasCornerSupport(mc.thePlayer.posX, mc.thePlayer.posZ, expectedBlockY);

        // Early: prefer an extra tick of clearance; Turbo: aim to land
        // immediately after the first valid successful tower placement.
        final double minimumHeight = turbo ? 1.001D : 1.105D;
        final double earliestMotion = turbo ? 0.28D : 0.17D;
        if (blockReady && gained >= minimumHeight
                && mc.thePlayer.motionY <= earliestMotion
                && mc.thePlayer.motionY >= -0.09D) {
            setTowerMotion(turbo ? -0.46D : -0.39D);
            towerTick = 2;
            return;
        }

        // When the expected block was not placed in time, do exactly what
        // Raven Prediction does rather than forcing a premature descent.
        if (mc.thePlayer.motionY <= 0.0D && mc.thePlayer.motionY >= -0.09D) {
            setTowerMotion(-0.38D);
            towerTick = 2;
        }
    }

    // Prediction speed variants from the companion Tower build.
    private void runRavenPredictionVariant(double earlyMotionLimit, double earlyPull,
                                           double minRise) {
        if (mc.thePlayer.onGround) {
            towerLaunchY = mc.thePlayer.posY;
            setTowerMotion(0.42D);
            towerTick = 1;
            return;
        }
        if (towerTick <= 0) return;

        double motion = mc.thePlayer.motionY;
        double rise = mc.thePlayer.posY - towerLaunchY;
        int neededBlockY = MathHelper.floor_double(towerLaunchY);
        boolean blockPlacedThisLevel = towerLastPlacedY >= neededBlockY;

        if (blockPlacedThisLevel && rise >= minRise
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

    private void setTowerPositionY(double y) {
        if (mc.thePlayer == null || mc.thePlayer.raw == null) return;
        mc.thePlayer.raw.setPosition(new Vec3(mc.thePlayer.posX, y, mc.thePlayer.posZ));
        mc.thePlayer.posY = y;
    }

    // Recent Yuri NCP idea adapted to Raven's script event order. The source
    // relaunches at integer Y with ~0.41998 motion. We keep that core behavior,
    // but allow a small fractional tolerance so a post-input callback cannot
    // miss the exact 0.0015 boundary by one physics step.
    private void runYuriNCPTower() {
        double fraction = towerFraction();

        if (towerTick <= 0 || mc.thePlayer.onGround) {
            double baseY = Math.floor(mc.thePlayer.posY + 1.0E-4D);
            setTowerPositionY(baseY);
            towerLaunchY = baseY;
            setTowerMotion(0.41998D);
            towerTick = 1;
            return;
        }

        double gained = mc.thePlayer.posY - towerLaunchY;
        if (gained >= 0.985D && (fraction <= 0.08D || fraction >= 0.985D)) {
            double nextY = Math.floor(mc.thePlayer.posY + 0.02D);
            if (nextY <= towerLaunchY) nextY = towerLaunchY + 1.0D;
            setTowerPositionY(nextY);
            towerLaunchY = nextY;
            setTowerMotion(0.41998D);
            towerTick = 1;
        }
    }

    // Keeps the proven Raven Pull launch/snap sequence, then starts the next
    // cycle early only when the new support block really exists. If placement
    // is not ready yet it falls back to the conservative recent pull-down
    // value used by current 1.8.9 tower implementations.
    private void runRavenPullSyncTower() {
        double fraction = towerFraction();

        if (mc.thePlayer.onGround || towerTick <= 0) {
            towerLaunchY = mc.thePlayer.posY;
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

        if (towerTick >= 3 && (fraction < 0.12D || mc.thePlayer.posY >= towerLaunchY + 0.985D)) {
            int supportY = MathHelper.floor_double(mc.thePlayer.posY) - 1;
            if (hasCornerSupport(mc.thePlayer.posX, mc.thePlayer.posZ, supportY)) {
                towerLaunchY = Math.floor(mc.thePlayer.posY);
                setTowerMotion(0.42D);
                towerTick = 1;
            } else {
                setTowerMotion(-0.0784000015258789D);
                towerTick = 4;
            }
            return;
        }

        if (towerTick >= 4 && mc.thePlayer.motionY <= 0.10D && mc.thePlayer.motionY >= -0.10D) {
            setTowerMotion(-0.0784000015258789D);
        }

        if (towerTick >= 4 && mc.thePlayer.onGround) {
            towerTick = 0;
        }
    }

    private void runRavenPullTower() {
        double fraction = towerFraction();

        if (mc.thePlayer.onGround || towerTick <= 0) {
            towerLaunchY = mc.thePlayer.posY;
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
            if (fraction < 0.10D || mc.thePlayer.posY >= towerLaunchY + 0.98D) {
                setTowerMotion(-0.08D);
                towerTick = 4;
                return;
            }
            if (mc.thePlayer.motionY <= 0.10D && mc.thePlayer.motionY >= -0.10D) {
                setTowerMotion(-0.22D);
                towerTick = 4;
                return;
            }
        }

        if (towerTick >= 4 && mc.thePlayer.onGround) {
            towerTick = 0;
        }
    }

    private void runOnGroundTower() {
        double fraction = towerFraction();
        if (mc.thePlayer.onGround && towerTick == 0) {
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
        if (mc.thePlayer.onGround && towerTick == 0) {
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
        if (mc.thePlayer.onGround && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.48D) {
            setTowerMotion(0.33D);
            towerTick = 2;
        } else if (towerTick == 2 && fraction >= 0.72D) {
            // Pulse used to stop almost exactly on the next integer Y.
            // Tower placement requires the feet to be a tiny bit above that
            // level, so give the placement tick ~4 cm of vertical headroom.
            setTowerMotion(Math.max(0.040D, 1.040D - fraction));
            towerSpoofGroundThisTick = true;
            towerTick = 3;
        } else if (towerTick >= 3 && (fraction < 0.12D || fraction > 0.97D)) {
            // Do not pull the player below the valid placement height before
            // the next block has had a chance to be placed.
            setTowerMotion(-0.005D);
            towerSpoofGroundThisTick = true;
            towerTick = 0;
        }
    }

    private void runOnGroundPredictionTower() {
        double fraction = towerFraction();
        if (mc.thePlayer.onGround && towerTick == 0) {
            setTowerMotion(0.42D);
            towerTick = 1;
        } else if (towerTick == 1 && fraction >= 0.39D && fraction <= 0.48D) {
            setTowerMotion(0.33D);
            towerTick = 2;
        } else if (towerTick >= 2 && fraction >= 0.72D) {
            setTowerMotion(Math.max(0.005D, 1.0D - fraction));
            towerSpoofGroundThisTick = true;
            towerTick = 3;
        } else if (towerTick >= 3 && mc.thePlayer.motionY <= 0.0D && mc.thePlayer.motionY >= -0.09D) {
            setTowerMotion(-0.18D);
            towerSpoofGroundThisTick = true;
            towerTick = 0;
        }
    }

    private void resetBreezilyState() {
        breezilyTickCounter = 0;
        breezilyGroundWaitRemaining = 0;
        breezilyUseLeft = true;
        breezilyLockedYaw = Float.NaN;
    }

    private int getBreezilyGroundWaitTicks() {
        if (breezilyGroundWaitTicks == null) return 1;
        return MathHelper.clamp_int((int) Math.round(breezilyGroundWaitTicks.getInput()), 1, 10);
    }

    private boolean canApplyBreezilyGroundStrafe(PrePlayerInputEvent input) {
        if (mc.thePlayer == null || breezilyOnlyStrafeOnGround == null
                || !breezilyOnlyStrafeOnGround.isToggled()) {
            breezilyGroundWaitRemaining = 0;
            return true;
        }

        if ((input != null && input.isJump()) || !mc.thePlayer.onGround) {
            // A jump immediately suspends auto-strafe. While airborne the delay
            // stays armed; it only starts counting down after landing.
            breezilyGroundWaitRemaining = getBreezilyGroundWaitTicks();
            return false;
        }

        if (breezilyGroundWaitRemaining > 0) {
            breezilyGroundWaitRemaining--;
            return false;
        }
        return true;
    }

    private void restoreBreezilyCamera() {

    }

    void onPostMotion() {
    }

    private boolean isRawDiagonalInput() {
        return getForwardInput() != 0 && getStrafeInput() != 0;
    }

    private boolean hasBreezilyDirectionInput() {
        return getForwardInput() != 0 || getStrafeInput() != 0;
    }

    private float getBreezilyTravelYaw(float cameraYaw) {
        int forward = getForwardInput();
        int strafe = getStrafeInput();
        if (forward == 0 && strafe == 0) {
            if (!Float.isNaN(breezilyLockedYaw)) {
                return breezilyLockedYaw - 180.0F;
            }
            return cameraYaw;
        }
        return getMovementYaw(cameraYaw, forward, strafe);
    }

    private void updateBreezilyMovement(PrePlayerInputEvent input) {
        if (input == null || mc.thePlayer == null) return;
        if (!canApplyBreezilyGroundStrafe(input)) {
            breezilyTickCounter = 0;
            return;
        }
        ItemStack stack = getPlacementStack(mc.thePlayer);
        if (!isValidBlock(stack)) return;

        float forward = input.getForward();
        float strafe = input.getStrafe();
        if (Math.abs(forward) < 0.001F && Math.abs(strafe) < 0.001F) {
            breezilyTickCounter = 0;
            return;
        }

        double length = Math.sqrt((double) forward * forward + (double) strafe * strafe);
        float baseForward = (float) (forward / length);
        float baseStrafe = (float) (strafe / length);

        if (!breezilyAutoStrafe.isToggled()) {
            input.setForward(baseForward);
            input.setStrafe(baseStrafe);
            return;
        }

        float weave = breezilyUseLeft ? 0.62F : -0.62F;
        float outForward = baseForward - baseStrafe * weave;
        float outStrafe = baseStrafe + baseForward * weave;
        float scale = Math.max(1.0F, Math.max(Math.abs(outForward), Math.abs(outStrafe)));
        input.setForward(outForward / scale);
        input.setStrafe(outStrafe / scale);

        int switchTicks = MathHelper.clamp_int((int) Math.round(breezilySwitchSpeed.getInput()), 2, 10);
        breezilyTickCounter++;
        if (breezilyTickCounter >= switchTicks) {
            breezilyTickCounter = 0;
            breezilyUseLeft = !breezilyUseLeft;
        }
    }

    private float getBreezilyLockedYaw(float cameraYaw) {
        if (hasBreezilyDirectionInput()) {
            float travelYaw = getBreezilyTravelYaw(cameraYaw);

            float desiredServerYaw = travelYaw + 180.0F;
            float referenceYaw = Float.isNaN(breezilyLockedYaw)
                    ? RotationUtils.serverRotations[0] : breezilyLockedYaw;
            breezilyLockedYaw = quantizeRotation(unwrapYaw(desiredServerYaw, referenceYaw));
        } else if (Float.isNaN(breezilyLockedYaw)) {
            breezilyLockedYaw = quantizeRotation(unwrapYaw(cameraYaw + 180.0F, RotationUtils.serverRotations[0]));
        }
        return breezilyLockedYaw;
    }

    private int aimBreezilyGroundCandidate(int candidateCount, float fixedYaw) {
        if (candidateCount <= 0) {
            targetPitch = quantizeRotation(78.0F);
            return -1;
        }

        int bestIndex = -1;
        int bestScore = -1;
        float bestPitch = Float.NaN;
        float bestDistance = Float.MAX_VALUE;
        for (int i = 0; i < candidateCount; i++) {
            Placement candidate = candidates[i];
            if (candidate == null) continue;
            float candidatePitch = findNearestFacePitch(candidate, fixedYaw, 78.0F);
            if (Float.isNaN(candidatePitch)) continue;
            Vec3 hit = raycastFace(candidate, fixedYaw, candidatePitch);
            if (hit == null) continue;
            int score = getCandidateScore(i);
            float distance = Math.abs(candidatePitch - 78.0F);
            if (bestIndex < 0 || score > bestScore || (score == bestScore && distance < bestDistance)) {
                bestIndex = i;
                bestScore = score;
                bestPitch = candidatePitch;
                bestDistance = distance;
            }
        }

        if (bestIndex >= 0) {
            targetYaw = fixedYaw;
            targetPitch = RotationUtils.clampPitch(bestPitch);
            pitch = targetPitch;
            debugAimSelection = 8;
            debugAimScore = bestScore;
        } else {
            targetYaw = fixedYaw;
            targetPitch = quantizeRotation(78.0F);
        }
        return bestIndex;
    }

    private Vec3 aimBreezilyPlacement(Placement placement, float fixedYaw) {
        if (placement == null) return null;
        float candidatePitch = findNearestFacePitch(placement, fixedYaw, 78.0F);
        if (Float.isNaN(candidatePitch)) {
            candidatePitch = findFacePitch(placement, fixedYaw);
        }
        if (Float.isNaN(candidatePitch)) {
            targetYaw = fixedYaw;
            targetPitch = quantizeRotation(78.0F);
            return null;
        }
        targetYaw = fixedYaw;
        targetPitch = RotationUtils.clampPitch(candidatePitch);
        pitch = targetPitch;
        return raycastFace(placement, targetYaw, targetPitch);
    }

    private float getTravelYaw() {
        EntityPlayerSP player = mc.thePlayer;
        Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
        if (motion.x * motion.x + motion.z * motion.z > 0.0016) {
            return (float) (Math.atan2(-motion.x, motion.z) * 180.0 / Math.PI);
        }
        return getInputYaw();
    }

    private boolean shouldStopSprint() {
        if (alwaysSprint.isToggled() || tellyJumping || isTellyTakeoff()) {
            sprintStopRequested = false;
            return false;
        }
        float yawOffset = Math.abs(MathHelper.wrapAngleTo180_float(lastSentYaw - getTravelYaw()));
        if (sprintStopRequested) {
            if (yawOffset < SPRINT_MAX_YAW_OFFSET - 10.0F) {
                sprintStopRequested = false;
            }
        } else if (yawOffset > SPRINT_MAX_YAW_OFFSET + 5.0F) {
            sprintStopRequested = true;
        }
        return sprintStopRequested;
    }

    private void applySprintPolicy(boolean stop) {
        if (alwaysSprint.isToggled()) {
            sprintToggleCooldown = 0;
        }
        if (sprintToggleCooldown > 0) {
            sprintToggleCooldown--;
        }
        if (stop == sprintSuppressed || sprintToggleCooldown > 0) {
            if (sprintSuppressed) {
                mc.thePlayer.setSprinting(false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            }
            return;
        }
        sprintToggleCooldown = 4;
        if (stop) {
            sprintWasPressed = mc.gameSettings.keyBindSprint.isKeyDown();
            sprintSuppressed = true;
            mc.thePlayer.setSprinting(false);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(),
                    sprintWasPressed || Utils.isBindDown(mc.gameSettings.keyBindSprint));
            sprintSuppressed = false;
        }
    }

    private void handleSourceStyleTransition() {
        if (!isHypixelMode()) {
            lastSourceScaffoldMode = "";
            return;
        }

        String current = getSourceScaffoldMode();
        if (current.equals(lastSourceScaffoldMode)) return;

        sourceAirTicks = 0;
        sourceGroundTicks = 0;
        sourcePlaceDelayTicks = 0;
        sourceBlocksPlaced = 0;
        sourceGodBridgeJumpBlocks = 0;
        sourceSneakBlocks = 0;
        sourceDirectionalChange = 0;
        sourceStartY = mc.thePlayer == null ? 0.0D : Math.floor(mc.thePlayer.posY);
        hypixelPlaceTimer = 0;
        hypixelSourceYaw = getPlayerYaw() - 180.0F;
        hypixelSourcePitch = 90.0F;
        snapPlusLastMovementYaw = Float.NaN;
        resetBreezilyState();
        lastSourceScaffoldMode = current;
    }

    private void handleModeTransition() {
        String currentMode = getScaffoldMode();
        if (currentMode.equals(lastScaffoldMode)) return;

        tellyJumping = false;
        takeoffLatched = false;
        pendingPlacementReady = false;
        pendingPlacement = null;
        placementDelayTicks = Math.max(placementDelayTicks, 1);
        resetNinjaState();
        resetBreezilyState();
        resetRushState();
        breezilyViewYaw = Float.NaN;
        breezilyViewPitch = Float.NaN;
        snapReturnPending = false;
        snapSkipPlacementThisTick = false;
        snapDiagonalHoldTicks = 0;
        snapLastPlacementYaw = Float.NaN;
        setServerRelativeMovementInputs(false);
        hypixelPlaceTimer = 0;
        sourceAirTicks = 0;
        sourceGroundTicks = 0;
        sourcePlaceDelayTicks = 0;
        sourceBlocksPlaced = 0;
        sourceGodBridgeJumpBlocks = 0;
        sourceSneakBlocks = 0;
        sourceDirectionalChange = 0;
        sourceStartY = mc.thePlayer == null ? 0.0D : Math.floor(mc.thePlayer.posY);
        hopAirTicks = 0;
        hopLaunchRequested = false;
        snapPlusLastMovementYaw = Float.NaN;
        if (currentMode.equals("Snap+") || currentMode.equals("Hypixel")) {
            hypixelSourceYaw = getPlayerYaw() - 180.0F;
            hypixelSourcePitch = 90.0F;
        } else {
            hypixelSourceYaw = Float.NaN;
            hypixelSourcePitch = 90.0F;
        }
        lastScaffoldMode = currentMode;
    }

    private void syncScaffoldModeKeybindState() {
        if (scaffoldModeKeybinds == null) return;
        for (int i = 0; i < scaffoldModeKeybinds.length; i++) {
            if (scaffoldModeKeybinds[i] != null) scaffoldModeKeybinds[i].syncState();
        }
    }

    private void pollScaffoldModeKeybinds() {
        if (scaffoldModeKeybinds == null || tellyMode == null) return;

        for (int i = 0; i < scaffoldModeKeybinds.length && i < MODE_OPTIONS.length; i++) {
            KeySetting bind = scaffoldModeKeybinds[i];
            if (bind != null && bind.consumePress()) {
                int current = (int) Math.round(tellyMode.getInput());
                if (current != i) {
                    modules.setSlider(scriptName, tellyMode.name, i);
                    // The normal mode-transition handler resets the mode-specific
                    // state safely on this same tick.
                }
                break;
            }
        }
    }

    private void syncKeepYModeKeybindState() {
        if (keepYModeKeybinds == null) return;
        for (KeySetting bind : keepYModeKeybinds) {
            if (bind != null) bind.syncState();
        }
    }

    private void pollKeepYModeKeybinds() {
        if (keepYModeKeybinds == null || keepMode == null) return;
        for (int i = 0; i < keepYModeKeybinds.length; i++) {
            KeySetting bind = keepYModeKeybinds[i];
            if (bind != null && bind.consumePress()) {
                // Keep Y has a Disabled entry in the UI: 0=Off, 1=Telly A, 2=Telly B.
                int current = (int) Math.round(modules.getSlider(scriptName, keepMode.name));
                if (current != i) modules.setSlider(scriptName, keepMode.name, i);
                break;
            }
        }
    }

    private String getScaffoldMode() {
        String mode = tellyMode == null ? "God Bridge" : tellyMode.getSelectedOption();
        return mode == null || mode.length() == 0 ? "God Bridge" : mode;
    }

    private boolean isGodBridgeMode() {
        return getScaffoldMode().equals("God Bridge");
    }

    private boolean isSnapMode() {
        return getScaffoldMode().equals("Snap");
    }

    private boolean isSnapPlusMode() {
        return getScaffoldMode().equals("Snap+");
    }

    private boolean isRushMode() {
        return getScaffoldMode().equals("Rush");
    }

    private boolean isNinjaMode() {
        return getScaffoldMode().equals("Ninja");
    }

    private boolean isBreezilyMode() {
        return getScaffoldMode().equals("Breezily");
    }

    private boolean isHypixelMode() {
        return getScaffoldMode().equals("Hypixel");
    }

    private boolean isHopMode() {
        return getScaffoldMode().equals("Hop");
    }

    private String getSourceScaffoldMode() {
        String mode = sourceScaffoldMode == null ? "Hypixel" : sourceScaffoldMode.getSelectedOption();
        return mode == null || mode.length() == 0 ? "Hypixel" : mode;
    }

    private String getHopMode() {
        String mode = hopMode == null ? "Sprint" : hopMode.getSelectedOption();
        return mode == null || mode.length() == 0 ? "Sprint" : mode;
    }

    private boolean isTellyModeSelected() {
        String mode = getScaffoldMode();
        return mode.equals("Telly Short") || mode.equals("Telly Long");
    }

    private String getTellyMode() {
        String mode = getScaffoldMode();
        if (!isTellyModeSelected()) return "Disabled";
        if (tellyOnJump.isToggled() && !isJumpPressed()) return "Disabled";
        return mode.equals("Telly Long") ? "Long" : "Short";
    }

    private String getKeepYMode() {
        return keepMode.getInput() == -1 ? "Disabled" : keepMode.getSelectedOption();
    }

    private boolean isIceSelectionEnabled() {
        return iceThreshold.getInput() > -0.595;
    }

    private boolean isKeepYActive() {
        return !getKeepYMode().equals("Disabled")
                && (!keepYOnRightClick.isToggled() || keybinds.isMouseDown(1));
    }

    private String getActiveKeepYMode() {
        return isKeepYActive() ? getKeepYMode() : "Disabled";
    }

    private float randomFloat(float minimum, float maximum) {
        return (float) (minimum + Math.random() * (maximum - minimum));
    }

    private float getPlayerYaw() {
        return mc.thePlayer.rotationYaw;
    }

    private float getServerYaw() {
        Float serverYaw = RotationHelper.get().getServerYaw();
        return (serverYaw == null) ? getPlayerYaw() : serverYaw.floatValue();
    }

    private float quantizeMouseRotation(float value, float reference) {
        float delta = value - reference;
        float snapped = Math.round(delta / MOUSE_ROTATION_INCREMENT) * MOUSE_ROTATION_INCREMENT;
        return reference + snapped;
    }

    private float unwrapYaw(float angle, float target) {
        return target + MathHelper.wrapAngleTo180_float(angle - target);
    }

    private float clampRotationDelta(float delta, float limit) {
        limit = MathHelper.clamp_float(limit, 0.0F, 180.0F);
        return MathHelper.clamp_float(delta, -limit, limit);
    }

    private float quantizeRotation(float angle) {
        return (float) ((double) angle - (double) angle % ROTATION_INCREMENT);
    }

    private float jitter(float maximumDegrees) {
        float offset = quantizeRotation(randomFloat(-maximumDegrees, maximumDegrees));
        if (offset == 0.0F) {
            offset = (randomFloat(0.0F, 1.0F) < 0.5F) ? (float) ROTATION_INCREMENT : (float) -ROTATION_INCREMENT;
        }
        return offset;
    }

    private void updateYawJitter() {
        yawJitterAmplitude += randomFloat(-0.055F, 0.055F);
        yawJitterAmplitude = MathHelper.clamp_float(yawJitterAmplitude, MIN_YAW_JITTER, MAX_YAW_JITTER);
    }

    private float nextYawJitter() {
        return jitter(yawJitterAmplitude);
    }

    private float rotationEaseScale(float distance) {
        float influence = MathHelper.clamp_float((distance - 1.0F) / 5.0F, 0.0F, 1.0F);
        return 1.0F + (rotationEaseRate - 1.0F) * influence;
    }

    private float[] easeRotations(float fromYaw, float fromPitch, float toYaw,
                              float toPitch) {
        float yawDelta = MathHelper.wrapAngleTo180_float(toYaw - fromYaw);
        float pitchDelta = RotationUtils.clampPitch(toPitch) - fromPitch;
        float distance = (float) Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
        if (distance <= 1.0F) {
            rotationEaseSlowdown = 0.0F;
            return new float[]{toYaw, RotationUtils.clampPitch(toPitch)};
        }
        float speedLimit;
        if (!rotationEaseStarted) {
            speedLimit = 73.0F;
            rotationEaseStarted = true;
            rotationEaseSlowdown = 0.0F;
        } else {
            speedLimit = 43.0F - rotationEaseSlowdown;
            if (rotationEaseSlowdown < 10.0F) {
                rotationEaseSlowdown += randomFloat(3.0F, 4.0F);
            }
        }
        speedLimit -= randomFloat(0.1F, 1.0F);
        speedLimit *= rotationEaseScale(distance);

        float step = Math.min(speedLimit, distance);
        float scale = step / distance;
        return new float[]{fromYaw + yawDelta * scale, RotationUtils.clampPitch(fromPitch + pitchDelta * scale)};
    }

    private float smooth(float angle, float smoothingFactor) {
        float randomizedFactor = MathHelper.clamp_float(smoothingFactor + randomFloat(-0.1F, 0.1F), 0.0F, 1.0F);
        return angle * (0.5F + 0.5F * (1.0F - randomizedFactor));
    }

    private float[] getRotationsToOffset(double offsetX, double offsetY, double offsetZ, float currentYaw, float currentPitch) {
        double horizontalDistance = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        float yawDelta = MathHelper.wrapAngleTo180_float((float) (Math.atan2(offsetZ, offsetX) * 180.0 / Math.PI) - 90.0F - currentYaw);
        float pitchDelta = MathHelper.wrapAngleTo180_float((float) (-Math.atan2(offsetY, horizontalDistance) * 180.0 / Math.PI) - currentPitch);
        yawDelta = (Math.abs(yawDelta) <= 1.0F) ? 0.0F : smooth(clampRotationDelta(yawDelta, 180.0F), 0.0F);
        pitchDelta = (Math.abs(pitchDelta) <= 1.0F) ? 0.0F : smooth(clampRotationDelta(pitchDelta, 180.0F), 0.0F);
        return new float[]{quantizeRotation(currentYaw + yawDelta), quantizeRotation(currentPitch + pitchDelta)};
    }

    private float getMovementYaw(float movementYaw, float forward, float strafe) {
        if (forward < 0.0F) {
            movementYaw += 180.0F;
        }
        if (strafe != 0.0F) {
            float strafeScale = (forward == 0.0F) ? 1.0F : 0.5F * Math.signum(forward);
            movementYaw += -90.0F * strafeScale * Math.signum(strafe);
        }
        return movementYaw;
    }

    private int getForwardInput() {
        int forward = 0;
        if (Utils.isBindDown(mc.gameSettings.keyBindForward)) {
            forward++;
        }
        if (Utils.isBindDown(mc.gameSettings.keyBindBack)) {
            forward--;
        }
        return forward;
    }

    private int getStrafeInput() {
        int strafe = 0;
        if (Utils.isBindDown(mc.gameSettings.keyBindLeft)) {
            strafe++;
        }
        if (Utils.isBindDown(mc.gameSettings.keyBindRight)) {
            strafe--;
        }
        return strafe;
    }

    private boolean isAnyMovementKeyDown() {
        return Utils.isBindDown(mc.gameSettings.keyBindForward)
                || Utils.isBindDown(mc.gameSettings.keyBindBack)
                || Utils.isBindDown(mc.gameSettings.keyBindLeft)
                || Utils.isBindDown(mc.gameSettings.keyBindRight);
    }

    private boolean isMoving() {
        boolean forward = Utils.isBindDown(mc.gameSettings.keyBindForward);
        boolean backward = Utils.isBindDown(mc.gameSettings.keyBindBack);
        boolean left = Utils.isBindDown(mc.gameSettings.keyBindLeft);
        boolean right = Utils.isBindDown(mc.gameSettings.keyBindRight);
        return (forward != backward) || (left != right);
    }

    private boolean isJumpPressed() {
        return Utils.isBindDown(mc.gameSettings.keyBindJump);
    }

    private int updateCardinalSide(float yaw) {
        float quadrantAngle = ((yaw + 180.0F) % 90.0F + 90.0F) % 90.0F;
        if (quadrantAngle > 8.0F && quadrantAngle < 37.0F) {
            cardinalSide = 1;
        } else if (quadrantAngle > 53.0F && quadrantAngle < 82.0F) {
            cardinalSide = -1;
        }
        return cardinalSide;
    }

    private boolean isDiagonalMovement(float yaw) {
        float quadrantAngle = Math.abs(yaw % 90.0F);
        if (diagonalMovement) {
            if (quadrantAngle < 16.0F || quadrantAngle > 74.0F) {
                diagonalMovement = false;
            }
        } else if (quadrantAngle > 24.0F && quadrantAngle < 66.0F) {
            diagonalMovement = true;
        }
        return diagonalMovement;
    }

    private float getInputYaw() {
        return MathHelper.wrapAngleTo180_float(getMovementYaw(getPlayerYaw(), getForwardInput(), getStrafeInput()));
    }

    private boolean isTellyTakeoff() {
        if (mc.thePlayer.onGround && isMoving() && !hasCeilingAbove()) {
            String keepYMode = getActiveKeepYMode();
            boolean keepYEnabled = keepYMode.equals("Telly A") || keepYMode.equals("Telly B");
            boolean tellyEnabled = getTellyMode().equals("Short") || isLongTellyEnabled();
            return (keepYEnabled && keepYStage > 0) || (tellyEnabled && isJumpPressed());
        }
        return false;
    }

    private boolean isManualTellyTakeoff() {
        if (mc.thePlayer.onGround && isMoving() && !hasCeilingAbove() && isJumpPressed()) {
            return getTellyMode().equals("Short") || isLongTellyEnabled();
        }
        return false;
    }

    private boolean isLongTellyEnabled() {
        return getTellyMode().equals("Long");
    }

    private boolean isShortDiagonalJump() {
        double speedX = Math.abs(mc.thePlayer.motionX);
        double speedZ = Math.abs(mc.thePlayer.motionZ);
        return !mc.thePlayer.onGround && getTellyMode().equals("Short") && speedX * speedX + speedZ * speedZ > 0.0016
                && Math.min(speedX, speedZ) > Math.max(speedX, speedZ) * 0.25;
    }

    private boolean isLookLocked() {
        return longTellyMissTicks < LOOK_LOCK_MISS_LIMIT;
    }

    private EnumFacing faceFromDelta(int dx, int dz) {
        if (dx > 0) {
            return EnumFacing.EAST;
        }
        if (dx < 0) {
            return EnumFacing.WEST;
        }
        if (dz > 0) {
            return EnumFacing.SOUTH;
        }
        return EnumFacing.NORTH;
    }

    private boolean intersectsPlayer(Vec3 position, int cx, int cy, int cz) {
        return (double) cx + 1.0 > position.x - 0.3 && (double) cx < position.x + 0.3
                && (double) cy + 1.0 > position.y && (double) cy < position.y + 1.8
                && (double) cz + 1.0 > position.z - 0.3 && (double) cz < position.z + 0.3;
    }

    private Placement findQueueSupport(int supportIndex, boolean upFirst, int cx, int cy, int cz) {
        int supportX;
        int supportZ;
        EnumFacing supportFace;
        int directionIndex = upFirst ? ((supportIndex == 0) ? 4 : supportIndex - 1) : supportIndex;
        int supportY = cy;
        if (directionIndex == 0) {
            supportX = cx - longTellyForwardX;
            supportZ = cz - longTellyForwardZ;
            supportFace = faceFromDelta(longTellyForwardX, longTellyForwardZ);
        } else if (directionIndex == 1) {
            supportX = cx - longTellyLateralX;
            supportZ = cz - longTellyLateralZ;
            supportFace = faceFromDelta(longTellyLateralX, longTellyLateralZ);
        } else if (directionIndex == 2) {
            supportX = cx + longTellyLateralX;
            supportZ = cz + longTellyLateralZ;
            supportFace = faceFromDelta(-longTellyLateralX, -longTellyLateralZ);
        } else if (directionIndex == 3) {
            supportX = cx + longTellyForwardX;
            supportZ = cz + longTellyForwardZ;
            supportFace = faceFromDelta(-longTellyForwardX, -longTellyForwardZ);
        } else {
            supportX = cx;
            supportY = cy - 1;
            supportZ = cz;
            supportFace = EnumFacing.UP;
        }
        return isSolidSupport(supportX, supportY, supportZ) && !isInteractableAt(supportX, supportY, supportZ)
                ? new Placement(new BlockPos(supportX, supportY, supportZ), supportFace) : null;
    }

    private void buildLongTellyQueue(Vec3 motion) {
        resetLongTellyQueue();
        if (ticksSinceGrounded > 3) {
            return;
        }
        Vec3 position = mc.thePlayer.getPositionVector();
        longTellyRowY = MathHelper.floor_double(position.y) - 1;
        double speedX = Math.abs(motion.x);
        double speedZ = Math.abs(motion.z);
        if (speedX * speedX + speedZ * speedZ < 0.01) {
            return;
        }
        int directionX = 0;
        int directionZ = 0;
        if (speedX >= speedZ * 2.4) {
            directionX = (motion.x > 0.0) ? 1 : -1;
        } else if (speedZ >= speedX * 2.4) {
            directionZ = (motion.z > 0.0) ? 1 : -1;
        } else {
            return;
        }
        if (longTellySprintJump && longTellyNeedsSideUpdate) {
            longTellyNeedsSideUpdate = false;

            if (hasLongTellyOrigin) {
                int travelledX = lastGroundX - longTellyOriginX;
                int travelledZ = lastGroundZ - longTellyOriginZ;
                if (travelledX * longTellyLateralX + travelledZ * longTellyLateralZ >= 1) {
                    longTellySide = -longTellySide;
                }
            } else {
                double lean = motion.x * (double) (-directionZ) + motion.z * (double) directionX;
                if (lean > 0.02) {
                    longTellySide = 1;
                } else if (lean < -0.02) {
                    longTellySide = -1;
                }
            }
            longTellyOriginX = lastGroundX;
            longTellyOriginZ = lastGroundZ;
            hasLongTellyOrigin = true;
        }
        longTellyForwardX = directionX;
        longTellyForwardZ = directionZ;
        longTellyLateralX = -directionZ * longTellySide;
        longTellyLateralZ = directionX * longTellySide;
        for (int i = 0; i < 3; i++) {
            int forwardOffset = longTellySprintJump ? i + 1 : i;
            queuedBlocks[i] = new BlockPos(lastGroundX + forwardOffset * directionX,
                    longTellyRowY + (longTellySprintJump ? 0 : 1),
                    lastGroundZ + forwardOffset * directionZ);
        }
        queuedBlockCount = 3;
    }

    private boolean findLongTellyPlacement() {
        EntityPlayerSP player = mc.thePlayer;
        Vec3 position = player.getPositionVector();
        float cameraYaw = yawGridBase;
        queueCursor = 0;
        while (queueCursor < queuedBlockCount && !isReplaceableAt(queuedBlocks[queueCursor])) {
            queueCursor++;
        }
        if (pendingQueueIndex >= 0 && (pendingQueueIndex < queueCursor || pendingQueueIndex >= queuedBlockCount)) {
            pendingQueueIndex = -1;
            queueStallTicks = 0;
        }
        for (int queueIndex = queueCursor; queueIndex < queuedBlockCount; queueIndex++) {
            if ((skippedQueueMask & (1 << queueIndex)) != 0) {
                continue;
            }
            BlockPos queuedBlock = queuedBlocks[queueIndex];
            int cx = queuedBlock.getX();
            int cy = queuedBlock.getY();
            int cz = queuedBlock.getZ();
            if (!isReplaceableAt(queuedBlock)) {
                if (queueIndex == pendingQueueIndex) {
                    pendingQueueIndex = -1;
                }
                continue;
            }
            double distanceX = (double) cx + 0.5 - position.x;
            double distanceZ = (double) cz + 0.5 - position.z;
            if (distanceX * distanceX + distanceZ * distanceZ > 18.0) {
                continue;
            }
            if (intersectsPlayer(position, cx, cy, cz)) {
                continue;
            }
            int firstPass = (queueIndex == pendingQueueIndex) ? 0 : 1;
            boolean upFirst = cy > longTellyRowY
                    || (!player.onGround && player.motionY > 0.0);
            for (int pass = firstPass; pass < 2; pass++) {
                for (int supportIndex = 0; supportIndex < 5; supportIndex++) {
                    Placement support = findQueueSupport(supportIndex, upFirst, cx, cy, cz);
                    if (support == null) {
                        continue;
                    }
                    float candidateYaw;
                    float candidatePitch;
                    if (pass == 0) {
                        candidateYaw = longTellyAimYaw;
                        candidatePitch = findNearestFacePitch(support, candidateYaw, lastSentPitch);
                    } else {
                        float[] candidateYaws = getLongTellyCandidateYaws(support, position, cameraYaw);
                        candidateYaw = Float.NaN;
                        candidatePitch = Float.NaN;
                        for (float baseYaw : candidateYaws) {
                            float jitteredYaw = baseYaw + nextYawJitter();
                            float facePitch = findNearestFacePitch(support, jitteredYaw,
                                    lastSentPitch);
                            if (!Float.isNaN(facePitch)) {
                                candidateYaw = jitteredYaw;
                                candidatePitch = facePitch;
                                break;
                            }
                        }
                    }
                    if (Float.isNaN(candidatePitch)) {
                        continue;
                    }
                    if (queueIndex != pendingQueueIndex) {
                        queueStallTicks = 0;
                    }
                    pendingQueueIndex = queueIndex;
                    pendingPlacement = support;
                    longTellyAimYaw = candidateYaw;
                    longTellyAimPitch = candidatePitch;
                    longTellyYawBucket = Math.round(MathHelper.wrapAngleTo180_float(candidateYaw - cameraYaw) / 45.0F);
                    return true;
                }
            }
        }
        return false;
    }

    private float[] getLongTellyCandidateYaws(Placement placement, Vec3 position, float cameraYaw) {
        double faceCenterX = (double) placement.support.getX() + 0.5 + (double) placement.face.getFrontOffsetX() * 0.5;
        double faceCenterZ = (double) placement.support.getZ() + 0.5 + (double) placement.face.getFrontOffsetZ() * 0.5;
        float faceYaw = (float) Math.toDegrees(
                Math.atan2(-(faceCenterX - position.x), faceCenterZ - position.z));

        int faceBucket = Math.round(MathHelper.wrapAngleTo180_float(faceYaw - cameraYaw) / 45.0F);
        float heldYaw = cameraYaw + (float) longTellyYawBucket * 45.0F;
        float[] candidateYaws = new float[]{
                heldYaw,
                cameraYaw + (float) faceBucket * 45.0F,
                cameraYaw + (float) (faceBucket - 1) * 45.0F,
                cameraYaw + (float) (faceBucket + 1) * 45.0F,
                faceYaw};
        float[] yawScores = new float[candidateYaws.length];
        float backYaw = (float) Math.toDegrees(
                Math.atan2((double) longTellyForwardX, (double) -longTellyForwardZ))
                + (lookingStraightBack ? 0.0F
                : (float) (LOOK_DIAGONAL_BUCKETS * lookSide) * 45.0F);
        for (int first = 0; first < candidateYaws.length; first++) {
            yawScores[first] = isLookLocked()
                    ? Math.abs(MathHelper.wrapAngleTo180_float(candidateYaws[first] - backYaw))
                    + 0.05F
                    * Math.abs(MathHelper.wrapAngleTo180_float(candidateYaws[first] - heldYaw))
                    : Math.abs(MathHelper.wrapAngleTo180_float(candidateYaws[first] - heldYaw));
        }
        for (int first = 0; first < candidateYaws.length; first++) {
            for (int second = first + 1; second < candidateYaws.length; second++) {
                if (yawScores[second] < yawScores[first]) {
                    float previousYaw = candidateYaws[first];
                    candidateYaws[first] = candidateYaws[second];
                    candidateYaws[second] = previousYaw;
                    float previousScore = yawScores[first];
                    yawScores[first] = yawScores[second];
                    yawScores[second] = previousScore;
                }
            }
        }
        return candidateYaws;
    }

    private boolean hasCeilingAbove() {
        Vec3 position = mc.thePlayer.getPositionVector();
        double minY = position.y + 1.0;
        double maxY = position.y + 1.0 + 1.8;
        int y0 = MathHelper.floor_double(minY);
        int y1 = MathHelper.floor_double(maxY - 1.0E-6);
        for (int y = y0; y <= y1; y++) {
            if (hasCornerSupport(position.x, position.z, y)) return true;
        }
        return false;
    }

    private boolean hasJumpPotion() {
        Entity player = client.getPlayer();
        if (player == null) return false;
        for (Object[] effect : player.getPotionEffects()) {
            if (effect != null && effect.length > 1 && "potion.jump".equals(String.valueOf(effect[1]))) return true;
        }
        return false;
    }

    private boolean isReplaceable(IBlockState state) {
        if (state == null || state.block == null || state.block.name == null) return true;
        String name = state.block.name.toLowerCase();
        if (name.equals("snow_layer")) return state.block.variant <= 0;
        return REPLACEABLE_BLOCK_NAMES.contains("," + name + ",");
    }

    private boolean isReplaceableAt(int x, int y, int z) {
        return isReplaceableAt(new BlockPos(x, y, z));
    }

    private boolean isReplaceableAt(BlockPos position) {
        return isReplaceable(BlockUtils.getBlockState(position));
    }

    private boolean isSolidSupport(int x, int y, int z) {
        return !isReplaceableAt(x, y, z);
    }

    private boolean isInteractableAt(int x, int y, int z) {
        return BlockUtils.isInteractable(BlockUtils.getBlock(x, y, z));
    }

    private Placement findPlacement() {
        debugMissingPlacementReason = 'n';
        EntityPlayerSP player = mc.thePlayer;
        Vec3 position = player.getPositionVector();
        boolean shortDiagonal = isShortDiagonalJump();
        BlockPos target = findPlacementTarget(player, position, shortDiagonal);
        return target == null ? null : findClosestPlacement(target, position, shortDiagonal);
    }

    private BlockPos findPlacementTarget(EntityPlayerSP player, Vec3 position, boolean shortDiagonal) {
        int playerBlockX = MathHelper.floor_double(position.x);
        int playerBlockY = MathHelper.floor_double(position.y);
        int playerBlockZ = MathHelper.floor_double(position.z);
        int targetY = ((keepYStage != 0 && !useCurrentHeight) ? Math.min(playerBlockY, keepYStartHeight) : playerBlockY) - 1;
        int targetX = playerBlockX;
        int targetZ = playerBlockZ;
        if (shortDiagonal) {
            if (!hasPrediction) {
                buildPrediction();
            }
            int ahead = 1;
            while (ahead < 3 && predictedPositions[ahead].y > targetY + 1.0) {
                ahead++;
            }
            if (hasCornerSupport(predictedPositions[ahead].x, predictedPositions[ahead].z, targetY)) {
                debugMissingPlacementReason = 'u';
                return null;
            }
            targetX = MathHelper.floor_double(predictedPositions[ahead].x);
            targetZ = MathHelper.floor_double(predictedPositions[ahead].z);
        } else if (!isReplaceableAt(targetX, targetY, targetZ)) {
            if (!player.onGround && !(isKeepYActive() && keepYStage > 0)) {
                debugMissingPlacementReason = 'u';
                return null;
            }
            Vec3 motion = new Vec3(player.motionX, player.motionY, player.motionZ);
            debugMissingPlacementReason = 'r';
            double horizontalSpeed = Utils.getHorizontalSpeed(player);
            if (horizontalSpeed < 0.01) {
                return null;
            }
            double directionX = motion.x / horizontalSpeed;
            double directionZ = motion.z / horizontalSpeed;
            int previousX = playerBlockX;
            int previousZ = playerBlockZ;
            boolean foundGap = false;
            for (double distance = 0.25; distance <= 2.0 && !foundGap; distance += 0.25) {
                int nextX = MathHelper.floor_double(position.x + directionX * distance);
                int nextZ = MathHelper.floor_double(position.z + directionZ * distance);
                if (nextX == previousX && nextZ == previousZ) {
                    continue;
                }
                nextX = previousX + MathHelper.clamp_int(nextX - previousX, -1, 1);
                nextZ = previousZ + MathHelper.clamp_int(nextZ - previousZ, -1, 1);
                if (nextX != previousX && nextZ != previousZ
                        && !isSolidSupport(previousX, targetY, nextZ) && !isSolidSupport(nextX, targetY, previousZ)) {
                    if (isReplaceableAt(previousX, targetY, nextZ)) {
                        targetX = previousX;
                        targetZ = nextZ;
                    } else if (isReplaceableAt(nextX, targetY, previousZ)) {
                        targetX = nextX;
                        targetZ = previousZ;
                    } else {
                        return null;
                    }
                    foundGap = true;
                } else if (isReplaceableAt(nextX, targetY, nextZ)) {
                    targetX = nextX;
                    targetZ = nextZ;
                    foundGap = true;
                } else if (isSolidSupport(nextX, targetY, nextZ)) {
                    previousX = nextX;
                    previousZ = nextZ;
                } else {
                    return null;
                }
            }
            if (!foundGap) {
                return null;
            }
        }
        return new BlockPos(targetX, targetY, targetZ);
    }

    private Placement findClosestPlacement(BlockPos target, Vec3 position, boolean shortDiagonal) {
        int targetX = target.getX();
        int targetY = target.getY();
        int targetZ = target.getZ();
        double reachSquared = 4.5 * 4.5;
        Placement bestPlacement = null;
        double bestDistance = 0.0;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 0; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    int supportX = targetX + dx;
                    int supportY = targetY + dy;
                    int supportZ = targetZ + dz;
                    if (!(keepYStage == 0 || useCurrentHeight || supportY < keepYStartHeight)) {
                        continue;
                    }
                    double playerOffsetX = (double) supportX + 0.5 - position.x;
                    double playerOffsetY = (double) supportY + 0.5 - position.y;
                    double playerOffsetZ = (double) supportZ + 0.5 - position.z;
                    if (playerOffsetX * playerOffsetX + playerOffsetY * playerOffsetY + playerOffsetZ * playerOffsetZ > reachSquared) {
                        continue;
                    }
                    double targetOffsetX = (double) supportX - (double) targetX;
                    double targetOffsetY = (double) supportY - (double) targetY;
                    double targetOffsetZ = (double) supportZ - (double) targetZ;
                    double targetDistanceSquared = targetOffsetX * targetOffsetX + targetOffsetY * targetOffsetY + targetOffsetZ * targetOffsetZ;
                    if (!shortDiagonal && bestPlacement != null && targetDistanceSquared >= bestDistance) {
                        continue;
                    }
                    BlockPos support = new BlockPos(supportX, supportY, supportZ);
                    IBlockState supportState = BlockUtils.getBlockState(support);
                    if (isReplaceable(supportState) || BlockUtils.isInteractable(supportState.getBlock())) {
                        continue;
                    }
                    EnumFacing side = findBestPlacementFace(supportX, supportY, supportZ, targetX, targetY, targetZ);
                    if (side == null) {
                        continue;
                    }
                    if (shortDiagonal) {
                        if (intersectsPlayer(position, supportX + side.getFrontOffsetX(), supportY + side.getFrontOffsetY(), supportZ + side.getFrontOffsetZ())) {
                            continue;
                        }
                        targetOffsetX += side.getFrontOffsetX();
                        targetOffsetY += side.getFrontOffsetY();
                        targetOffsetZ += side.getFrontOffsetZ();
                        targetDistanceSquared = targetOffsetX * targetOffsetX + targetOffsetY * targetOffsetY + targetOffsetZ * targetOffsetZ;
                    }
                    if (bestPlacement == null || targetDistanceSquared < bestDistance) {
                        bestDistance = targetDistanceSquared;
                        bestPlacement = new Placement(support, side);
                    }
                }
            }
        }
        if (bestPlacement == null) {
            debugMissingPlacementReason = 'n';
            return null;
        }
        return bestPlacement;
    }

    private EnumFacing findBestPlacementFace(int supportX, int supportY, int supportZ, int targetX, int targetY, int targetZ) {
        EnumFacing bestFace = null;
        double bestDistance = 0.0;
        for (EnumFacing face : PLACEMENT_FACES) {
            int blockX = supportX + face.getFrontOffsetX();
            int blockY = supportY + face.getFrontOffsetY();
            int blockZ = supportZ + face.getFrontOffsetZ();
            if (blockY <= targetY && isReplaceableAt(blockX, blockY, blockZ)
                    && (!(isKeepYActive() && keepYStage > 0) || blockY == targetY)) {
                double offsetX = (double) blockX - (double) targetX;
                double offsetY = (double) blockY - (double) targetY;
                double offsetZ = (double) blockZ - (double) targetZ;
                double distanceSquared = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;
                if (bestFace == null || distanceSquared < bestDistance || (distanceSquared == bestDistance && face == EnumFacing.UP)) {
                    bestDistance = distanceSquared;
                    bestFace = face;
                }
            }
        }
        return bestFace;
    }

    private Vec3 raycastFace(Placement placement, float candidateYaw, float candidatePitch) {
        MovingObjectPosition hit = RotationUtils.rayCastBlock(4.5, candidateYaw, candidatePitch);
        return placement.matches(hit) ? hit.hitVec : null;
    }

    private boolean hasStableFaceAim(Placement placement) {
        return raycastFace(placement, lastBaseYaw, lastBasePitch) != null
                && raycastFace(placement, lastBaseYaw + 0.5F, lastBasePitch) != null
                && raycastFace(placement, lastBaseYaw - 0.5F, lastBasePitch) != null
                && raycastFace(placement, lastBaseYaw,
                RotationUtils.clampPitch(lastBasePitch - 0.35F)) != null
                && raycastFace(placement, lastBaseYaw,
                RotationUtils.clampPitch(lastBasePitch + 0.35F)) != null;
    }

    private Vec3 aimShortDiagonalFace(Placement placement) {
        EntityPlayerSP player = mc.thePlayer;
        double dx = placement.support.getX() + 0.5 + placement.face.getFrontOffsetX() * 0.5 - player.posX;
        double dz = placement.support.getZ() + 0.5 + placement.face.getFrontOffsetZ() * 0.5 - player.posZ;
        float centerYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float[] yaws = {targetYaw, lastSentYaw, centerYaw};
        for (float desiredYaw : yaws) {
            float candidateYaw = quantizeMouseRotation(lastSentYaw + MathHelper.wrapAngleTo180_float(desiredYaw - lastSentYaw), lastSentYaw);
            for (int pass = 0; pass < 2; pass++) {
                float candidatePitch = pass == 0 ? findNearestFacePitch(placement, candidateYaw, targetPitch)
                        : findFacePitch(placement, candidateYaw);
                if (Float.isNaN(candidatePitch)) {
                    continue;
                }
                candidatePitch = RotationUtils.clampPitch(quantizeMouseRotation(candidatePitch, lastSentPitch));
                Vec3 hit = raycastFace(placement, candidateYaw, candidatePitch);
                if (hit != null) {
                    aimYaw(candidateYaw);
                    aimPitch(candidatePitch);
                    lastYawBucket = Math.round(MathHelper.wrapAngleTo180_float(candidateYaw - yawGridBase) / 45.0F);
                    return hit;
                }
            }
        }
        return null;
    }

    private boolean placeKeepYFromRay() {
        MovingObjectPosition hit = RotationUtils.rayCastBlock(4.5, lastSentYaw, lastSentPitch);
        if (hit == null || hit.sideHit == EnumFacing.DOWN) {
            return false;
        }
        EntityPlayerSP player = mc.thePlayer;
        BlockPos support = hit.getBlockPos();
        BlockPos target = BlockUtils.offsetPos(hit);
        int floorY = MathHelper.floor_double(player.posY);
        int placeY = (useCurrentHeight ? floorY : Math.min(floorY, keepYStartHeight)) - 1;
        if (target.getY() != placeY
                || !isSolidSupport(support.getX(), support.getY(), support.getZ())
                || isInteractableAt(support.getX(), support.getY(), support.getZ())
                || !isReplaceableAt(target.getX(), target.getY(), target.getZ())) {
            return false;
        }
        double dx = target.getX() + 0.5 - player.posX;
        double dz = target.getZ() + 0.5 - player.posZ;
        double leadX = player.motionX * 2.0;
        double leadZ = player.motionZ * 2.0;
        double leadSq = leadX * leadX + leadZ * leadZ;
        double along = leadSq > 0.0001
                ? MathHelper.clamp_double((dx * leadX + dz * leadZ) / leadSq, 0.0, 1.0) : 0.0;
        double offsetX = dx - leadX * along;
        double offsetZ = dz - leadZ * along;
        if (offsetX * offsetX + offsetZ * offsetZ > 1.0) {
            return false;
        }
        return placeBlock(new Placement(support, hit.sideHit))
                && !isReplaceableAt(target.getX(), target.getY(), target.getZ());
    }

    private int getPlacementLimit() {
        int base = MathHelper.clamp_int((int) multiplace.getInput(), 1, 3);
        if (isRushMode() && rushChain != null) {
            int chain = MathHelper.clamp_int((int) Math.round(rushChain.getInput()), 1, 3);
            return Math.max(base, chain);
        }
        return base;
    }

    private boolean placeBlock(Placement placement) {
        if (placementAttemptsThisTick >= getPlacementLimit()) {
            return false;
        }
        EnumFacing side = placement.face;
        EntityPlayerSP player = mc.thePlayer;
        if (!Utils.nullCheck() || side == null) {
            return false;
        }
        BlockPos support = placement.support;
        BlockPos target = placement.getTarget();
        IBlockState supportState = BlockUtils.getBlockState(support);
        if (isReplaceable(supportState) || BlockUtils.isInteractable(supportState.getBlock())
                || !isReplaceable(BlockUtils.getBlockState(target))) {
            return false;
        }
        Vec3 exactHit = raycastFace(placement, lastSentYaw, lastSentPitch);
        if (exactHit == null || !selectPlacementStack(player)) {
            return false;
        }
        ItemStack heldStack = getPlacementStack(player);
        if (!BlockUtils.canPlaceBlockOnSide(heldStack, support, side)) {
            return false;
        }
        Vec3 clickHit = (isSnapPlusMode() || isHypixelMode())
                ? getHypixelSourceHitVec(placement, exactHit) : exactHit;
        boolean placed;
        int visibleSlot = player.inventory.currentItem;
        boolean temporarySpoof = isSpoofItemEnabled() && hasSwapped
                && placementSlot >= 0 && placementSlot <= 8
                && placementSlot != visibleSlot;

        placementAttemptsThisTick++;
        sendingPlacement = true;
        if (temporarySpoof) {
            // Keep the server on one silent block slot across placements. We only
            // switch the local selected slot for the synchronous place call so
            // Raven supplies the correct held ItemStack. Any duplicate C09 emitted
            // by the vanilla controller is cancelled by allowPacket().
            setSpoofServerSlot(placementSlot);
            setItemRendererSpoofFlags(true);
            inventory.setSlot(placementSlot);
            player.inventory.currentItem = placementSlot;
            heldStack = player.inventory.getStackInSlot(placementSlot);
        }
        try {
            placed = mc.playerController.onPlayerRightClick(player, mc.theWorld, heldStack, support, side, clickHit);
        } finally {
            if (temporarySpoof) {
                // Client-only restore. Do not send C09 here: that old back-and-forth
                // is what produced two held-slot packets around one placement.
                inventory.setSlot(visibleSlot);
                player.inventory.currentItem = visibleSlot;
                setItemRendererSpoofFlags(false);
                physicalSlotSwapped = false;
                originalSlot = visibleSlot;
            }
            sendingPlacement = false;
        }
        placed = placed && BlockUtils.check(target, null);
        heldBlockCount = getBlockCount(getPlacementStack(player));
        if (placed) {
            placedThisTick = true;
            if (towerWasActive && side == EnumFacing.UP) {
                // The block has been placed in the local world and checked.
                // Track successful tower placements explicitly instead of
                // relying on the shared Rush-placement bookkeeping.
                towerLastPlacedY = Math.max(towerLastPlacedY, target.getY());
            }
            if (isSnapPlusMode()) {
                hypixelPlaceTimer = 10;
            }
            if (isHypixelMode()) {
                if (getSourceScaffoldMode().equals("Hypixel")) {
                    hypixelPlaceTimer = 10;
                }
                sourcePlaceDelayTicks = getYuriPlaceDelayTicks();
                sourceBlocksPlaced++;
                sourceGodBridgeJumpBlocks++;
                sourceSneakBlocks++;
            }
            if (rushPlannedPlacement != null
                    && target.equals(rushPlannedPlacement.getTarget())) {
                if (rushPlannedPlacement.face == EnumFacing.UP) {
                    towerLastPlacedY = target.getY();
                } else {
                    rushLastPlacedTarget = target;
                }
                rushPlannedPlacement = null;
                rushMissStreak = 0;
            }
            if (isSnapRotationActive()) {
                snapReturnPending = true;
                snapLastPlacementYaw = lastSentYaw;
                if (isRawDiagonalInput()) {
                    snapDiagonalHoldTicks = 2;
                }
            }
            lastPlacementHit = clickHit;
            lastPlacementTime = client.time();
            longTellyMissTicks = 0;
            debugPlacementPitchDelta = rotationSentLastTick ? (lastSentPitch - previousSentPitch) : 0.0F;
            if (Math.abs(debugPlacementPitchDelta) > Math.abs(debugMaxPlacementPitchDelta)) {
                debugMaxPlacementPitchDelta = debugPlacementPitchDelta;
            }
            if (isBasicDebugEnabled()) {
                printDebug(String.format("&7placed &f(%d,%d,%d) &7face=&f%s",
                        target.getX(), target.getY(), target.getZ(), side));
            }
            if (!silentSwing.isToggled()) {
                mc.thePlayer.swingItem();
            } else {
                client.sendPacket(new C0A());
            }
        }
        return placed;
    }

    private boolean isUnsupportedAt(double px, double pz) {
        EntityPlayerSP player = mc.thePlayer;
        int y = MathHelper.floor_double(player.getPositionVector().y) - 1;
        return !hasCornerSupport(px, pz, y);
    }

    private boolean hasCornerSupport(double x, double z, int y) {
        return isSolidSupport(MathHelper.floor_double(x - 0.3), y, MathHelper.floor_double(z - 0.3))
                || isSolidSupport(MathHelper.floor_double(x + 0.3), y, MathHelper.floor_double(z - 0.3))
                || isSolidSupport(MathHelper.floor_double(x - 0.3), y, MathHelper.floor_double(z + 0.3))
                || isSolidSupport(MathHelper.floor_double(x + 0.3), y, MathHelper.floor_double(z + 0.3));
    }

    private void syncPosition() {
        syncPosition(new ScaledResolution(mc));
    }

    private void syncPosition(ScaledResolution resolution) {
        int w = Math.max(1, resolution.getScaledWidth());
        int h = Math.max(1, resolution.getScaledHeight());
        // Settings are the single source of truth. Profiles restore them on reload.
        float x = counterPositionX != null ? (float) (counterPositionX.getInput() / 100.0D) : 0.45F;
        float y = counterPositionY != null ? (float) (counterPositionY.getInput() / 100.0D) : 0.65F;
        relativePosX = MathHelper.clamp_float(x, 0.0F, 1.0F);
        relativePosY = MathHelper.clamp_float(y, 0.0F, 1.0F);
        posX = relativePosX * w;
        posY = relativePosY * h;
    }

    private void renderBlockCount(int alpha) {
        renderBlockCount(alpha, false);
    }

    private void renderBlockCount(int alpha, boolean editing) {
        int blocks = getTotalBlocks();
        if (editing && blocks == 0) blocks = 64;
        if (blocks <= 0 && !editing) return;

        syncBlockCounterFont();
        String text = blocks + " blocks left";
        int textColor = blocks <= 15 ? 0xFFFF5555 : blocks <= 30 ? 0xFFFFFF55 : 0xFFFFFFFF;
        textColor = Utils.mergeAlpha(textColor, MathHelper.clamp_int(alpha, 0, 255));

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        syncPosition(scaledResolution);

        ItemStack icon = getBlockCounterIcon();
        float iconWidth = icon == null ? 0.0F : 18.0F;
        if (icon != null) {
            render.item(icon, posX, posY - 4.0F, 1.0F);
        }
        drawBlockCounterText(text, posX + iconWidth, posY, textColor);
    }

    private ItemStack getBlockCounterIcon() {
        try {
            // Match the exact predicate used for the counter and auto-placement.
            int heldSlot = inventory.getSlot();
            ItemStack held = inventory.getStackInSlot(heldSlot);
            if (isValidBlock(held)) return held;
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (isValidBlock(stack)) return stack;
            }
        } catch (Exception ignored) { }
        return null;
    }

    private void syncBlockCounterFont() {
        String requested = blockCounterFontSetting == null ? "Sf-Ui"
                : blockCounterFontSetting.getSelectedOption();
        if (requested == null || requested.length() == 0) requested = "Sf-Ui";
        if (blockCounterFont != null && requested.equals(blockCounterFontName)) return;
        blockCounterFont = null;
        blockCounterFontName = requested;
        try {
            blockCounterFont = keystrokesmod.utility.font.FontManager.getHudRenderer(requested, 1.0F);
        } catch (Throwable missingFont) {
            try {
                blockCounterFont = keystrokesmod.utility.font.FontManager.getHudRenderer("Sf-Ui", 1.0F);
            } catch (Throwable fallbackMissing) { }
        }
    }

    private void drawBlockCounterText(String text, float x, float y, int color) {
        if (blockCounterFont != null) blockCounterFont.drawString(text, x, y, color, true);
        else render.text2d(text, x, y, 1.0F, color, true);
    }

    private static final class Placement {
        private final BlockPos support;
        private final EnumFacing face;

        private Placement(BlockPos support, EnumFacing face) {
            this.support = support;
            this.face = face;
        }

        private BlockPos getTarget() {
            return support.offset(face);
        }

        private boolean matches(MovingObjectPosition hit) {
            return hit != null && support.equals(hit.getBlockPos()) && face == hit.sideHit;
        }
    }

    private static final class FaceGeometry {
        private final boolean topFace;
        private final double nearDistance;
        private final double farDistance;
        private final double eyeY;
        private final double surfaceY;

        private FaceGeometry(boolean topFace, double nearDistance, double farDistance, double eyeY, double surfaceY) {
            this.topFace = topFace;
            this.nearDistance = nearDistance;
            this.farDistance = farDistance;
            this.eyeY = eyeY;
            this.surfaceY = surfaceY;
        }
    }

    void onRenderWorld(float partialTicks) {
        mc.sync();
        renderPlacedBlockVisuals();
        if (!isVisualDebugEnabled() || mc.theWorld == null) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || !rotationSentThisTick) {
            return;
        }

        Vec3 position = player.getPositionVector();
        Vec3 eye = new Vec3(position.x, position.y + (double) player.getEyeHeight(), position.z);
        double yawRad = Math.toRadians((double) lastSentYaw);
        double pitchRad = Math.toRadians((double) lastSentPitch);
        double horizontal = Math.cos(pitchRad);
        Vec3 end = new Vec3(
                eye.x - Math.sin(yawRad) * horizontal * 4.5,
                eye.y - Math.sin(pitchRad) * 4.5,
                eye.z + Math.cos(yawRad) * horizontal * 4.5);

        MovingObjectPosition ray = RotationUtils.rayCastBlock(4.5, lastSentYaw, lastSentPitch);
        int rayColor = 0xA0FF5555;
        if (ray != null) {
            end = ray.hitVec;
            rayColor = 0xA055FFFF;
        }
        drawLine(eye, end, 2.0F, rayColor);

        if (lastPlacementHit != null && client.time() - lastPlacementTime <= 750L) {
            double s = 0.09;
            int placeColor = 0xF055FF55;
            drawLine(lastPlacementHit.offset(-s, 0.0, 0.0),
                    lastPlacementHit.offset(s, 0.0, 0.0), 3.0F, placeColor);
            drawLine(lastPlacementHit.offset(0.0, -s, 0.0),
                    lastPlacementHit.offset(0.0, s, 0.0), 3.0F, placeColor);
            drawLine(lastPlacementHit.offset(0.0, 0.0, -s),
                    lastPlacementHit.offset(0.0, 0.0, s), 3.0F, placeColor);
        }
    }

    private void trackPlacedBlockVisual(C08 packet) {
        if (blockHighlight == null || !blockHighlight.isToggled() || packet == null) return;
        try {
            if (packet.position == null || packet.direction == 255 || packet.itemStack == null || !packet.itemStack.isBlock) return;
            Vec3 placed = getPlacedVisualPos(packet);
            if (placed == null) return;
            long now = client.time();
            // Avoid duplicate boxes when the same placement packet is seen more than once in a tick.
            for (int i = placedBlockVisuals.size() - 1; i >= 0; i--) {
                Object[] existing = placedBlockVisuals.get(i);
                Vec3 pos = (Vec3) existing[0];
                long age = now - ((Long) existing[1]).longValue();
                if (age > 100L) break;
                if ((int) pos.x == (int) placed.x && (int) pos.y == (int) placed.y && (int) pos.z == (int) placed.z) return;
            }
            // Pending until the local world actually contains the placed block.
            // Seeing a C08 alone is not proof of a successful placement.
            if (placedBlockVisuals.size() >= 80) placedBlockVisuals.remove(0);
            placedBlockVisuals.add(new Object[]{placed, Long.valueOf(now), Boolean.FALSE});
        } catch (Exception ignored) { }
    }

    private Vec3 getPlacedVisualPos(C08 packet) {
        Vec3 p = packet.position;
        int d = packet.direction;
        return new Vec3(
                p.x + (d == 4 ? -1 : d == 5 ? 1 : 0),
                p.y + (d == 0 ? -1 : d == 1 ? 1 : 0),
                p.z + (d == 2 ? -1 : d == 3 ? 1 : 0));
    }

    private int getPlacementHighlightColor() {
        try {
            Object value = modules.getColor(scriptName, "Highlight Color");
            if (value instanceof java.awt.Color) return ((java.awt.Color) value).getRGB();
            if (value instanceof Integer) return ((Integer) value).intValue();
        } catch (Exception ignored) { }
        return 0x6464FF64;
    }

    private void renderPlacedBlockVisuals() {
        if (blockHighlight == null || !blockHighlight.isToggled() || mc.theWorld == null) {
            if (blockHighlight != null && !blockHighlight.isToggled()) placedBlockVisuals.clear();
            return;
        }
        if (placedBlockVisuals.isEmpty()) return;

        final long fadeIn = 150L, stay = 700L, fadeOut = 500L;
        final long confirmTimeout = 500L;
        long now = client.time();
        int color = getPlacementHighlightColor();
        int baseAlpha = (color >>> 24) & 0xFF;
        if (baseAlpha <= 0) baseAlpha = 255;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        Vec3 camera = render.getPosition();
        java.util.ArrayList<Object[]> dead = new java.util.ArrayList<Object[]>();

        gl.push();
        gl.depth(false);
        gl.blend(true);
        gl.texture2d(false);
        gl.lighting(false);
        gl.cull(false);
        gl.lineSmooth(true);
        for (int i = 0; i < placedBlockVisuals.size(); i++) {
            Object[] data = placedBlockVisuals.get(i);
            Vec3 pos = (Vec3) data[0];
            BlockPos target = new BlockPos(MathHelper.floor_double(pos.x),
                    MathHelper.floor_double(pos.y), MathHelper.floor_double(pos.z));
            boolean existsInWorld = !isReplaceableAt(target);
            boolean confirmed = Boolean.TRUE.equals(data[2]);
            long elapsed = now - ((Long) data[1]).longValue();
            if (!confirmed) {
                if (!existsInWorld) {
                    if (elapsed > confirmTimeout) dead.add(data);
                    continue;
                }
                data[2] = Boolean.TRUE;
                data[1] = Long.valueOf(now);
                elapsed = 0L;
            } else if (!existsInWorld) {
                // Remove the visual if the client world rolls back the placement.
                dead.add(data);
                continue;
            }
            if (elapsed > fadeIn + stay + fadeOut) {
                dead.add(data);
                continue;
            }
            float multiplier;
            if (elapsed < fadeIn) {
                multiplier = smoothPlacementVisual(elapsed / (float) fadeIn);
            } else if (elapsed > fadeIn + stay) {
                multiplier = 1.0F - smoothPlacementVisual(Math.min(1.0F, (elapsed - fadeIn - stay) / (float) fadeOut));
            } else {
                multiplier = 1.0F;
            }
            drawPlacementVisualBox((Vec3) data[0], camera, red, green, blue, (int) (baseAlpha * multiplier));
        }
        gl.depth(true);
        gl.texture2d(true);
        gl.blend(false);
        gl.cull(true);
        gl.lineSmooth(false);
        gl.resetColor();
        gl.pop();
        placedBlockVisuals.removeAll(dead);
    }

    private float smoothPlacementVisual(float t) {
        t = MathHelper.clamp_float(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private void drawPlacementVisualBox(Vec3 p, Vec3 camera, int red, int green, int blue, int alpha) {
        double x0 = p.x - camera.x, y0 = p.y - camera.y, z0 = p.z - camera.z;
        double[] vertices = {
                x0, y0, z0, x0 + 1, y0, z0, x0 + 1, y0, z0 + 1, x0, y0, z0 + 1,
                x0, y0 + 1, z0, x0 + 1, y0 + 1, z0, x0 + 1, y0 + 1, z0 + 1, x0, y0 + 1, z0 + 1
        };
        int[] quads = {0,1,2,3, 4,5,6,7, 0,1,5,4, 3,2,6,7, 0,3,7,4, 1,2,6,5};
        int[] edges = {0,1, 1,2, 2,3, 3,0, 4,5, 5,6, 6,7, 7,4, 0,4, 1,5, 2,6, 3,7};

        gl.color(red, green, blue, (int) (alpha * 0.35F));
        gl.begin(7);
        for (int i = 0; i < quads.length; i++) {
            int v = quads[i] * 3;
            gl.vertex3((float) vertices[v], (float) vertices[v + 1], (float) vertices[v + 2]);
        }
        gl.end();

        gl.lineWidth(2.0F);
        gl.color(red, green, blue, alpha);
        gl.begin(1);
        for (int i = 0; i < edges.length; i++) {
            int v = edges[i] * 3;
            gl.vertex3((float) vertices[v], (float) vertices[v + 1], (float) vertices[v + 2]);
        }
        gl.end();
    }

    private void logTickState(EntityPlayerSP player, Vec3 position) {
        if (isVerboseDebugEnabled()) {
            debugTicks++;
            boolean dryStreak = !player.onGround && longTellyMissTicks >= 2;
            if (debugTicks % 20 == 0 || dryStreak) {
                printDebug(String.format(
                        "&7g=&f%b &7twr=&f%b &7iTwr=&f%b &7stage=&f%d &7sY=&f%d &7rot=&f%.0f/%.0f &7cR=&f%b &7blk=&f%d &7mfix=&f%b &7fwd=&f%b &7jmp=&f%b &7ceil=&f%b &7keepY=&f%b &7sprOff=&f%b",
                        player.onGround, tellyJumping, isTellyTakeoff(), keepYStage, keepYStartHeight,
                        lastSentYaw, lastSentPitch, canRotate, heldBlockCount, movementFixActive,
                        isMoving(), isJumpPressed(), hasCeilingAbove(), isKeepYActive(),
                        sprintSuppressed));
                printDebug(String.format(
                        "&7fx=&f%.2f/%.2f &7cand=&f%d &7need=&f%s &7sel=&f%s &7k=&f%d &7ov=&f%.2f &7side=&f%d &7off=&f%.1f &7mv=&f%.1f &7pd=&f%.3f/%.2f &7cps=&f%.1f%c &7ys=&f%.0f/%.0f/%.0f &7lt=&f%s",
                        position.x - Math.floor(position.x), position.z - Math.floor(position.z), debugCandidateCount,
                        neededBlock != null ? (neededBlock.getX() + "," + neededBlock.getZ()) : "-", getAimSelectionDebug(), lastYawBucket,
                        debugOverhang, cardinalSide, debugYawGridOffset,
                        debugMovementYawOffset, debugPlacementPitchDelta, debugMaxPlacementPitchDelta, measuredClickCps, clickState,
                        debugSnappedYaw, debugVerifiedYaw, debugSentYaw,
                        getLongTellyDebug()));
            }
        }
    }

    private void updateRotationDebug(Vec3 motion, float cameraYaw) {
        if (motion.x * motion.x + motion.z * motion.z > 0.0016) {
            float moveDir = (float) Math.toDegrees(Math.atan2(-motion.x, motion.z));
            debugMovementYawOffset = MathHelper.wrapAngleTo180_float(moveDir - cameraYaw);
        }

        debugYawGridOffset = Math.abs(MathHelper.wrapAngleTo180_float(targetYaw - yawGridBase
                - (float) Math.round(MathHelper.wrapAngleTo180_float(targetYaw - yawGridBase) / 45.0F) * 45.0F));
    }

    private String getAimSelectionDebug() {
        switch (debugAimSelection) {
            case 1:
                return "A" + debugAimScore;
            case 2:
                return "S" + debugAimScore;
            case 3:
                return "AIR";
            case 4:
                return "LT";
            case 5:
                return "HOLD";
            default:
                return "-";
        }
    }

    private String getLongTellyDebug() {
        if (!(isLongTellyEnabled() && (tellyJumping || isManualTellyTakeoff()))) {
            return "b" + debugPlacementStatus + "/r" + debugLongTellyStatus;
        }
        return (longTellySprintJump ? "S" : "N") + (longTellySide > 0 ? "R" : "L") + ":" + queuedBlockCount
                + "/c" + queueCursor + "/k" + skippedQueueMask
                + "/d" + longTellyMissTicks + "/x" + revertedPlacementCount + "/b" + debugPlacementStatus
                + "/r" + debugLongTellyStatus + "/f" + (pendingPlacement == null ? "-" : pendingPlacement.face);
    }

    private boolean isBasicDebugEnabled() {
        String mode = debugMode.getSelectedOption();
        return mode.equals("Basic") || mode.equals("Verbose");
    }

    private boolean isVerboseDebugEnabled() {
        return debugMode.getSelectedOption().equals("Verbose");
    }

    private boolean isVisualDebugEnabled() {
        return debugMode.getSelectedOption().equals("Visual");
    }

    private void printDebug(String message) {
        Utils.sendRawMessage(Utils.formatColor("&8[&bScaffold&8]&r " + message));
    }

    private void drawLine(Vec3 start, Vec3 end, float width, int color) {
        render.line3D(start, end, width, color);
    }

void onPrePlayerInput(MovementInput input) {
    mc.sync();
    if (input == null || shouldPauseForGui()) return;
    scaffoldPrePlayerInput(new PrePlayerInputEvent(input));
}

void onDisconnect() {
    onDisable();
}

void onWorldJoin(Entity entity) {
    if (entity != null && entity.isUser) onEnable();
}

void setSettingVisibility(String settingName, boolean visible) {
    if (settingVisibilityUnavailable || settingName == null) return;
    try {
        // Do not mention Raven's internal Module/Setting classes as Java types.
        // Some B4 builds lack those classes and crash on method verification
        // before even reaching a normal catch(Exception).
        if (nativeScaffoldModule == null) {
            Class<?> defaults = Class.forName("keystrokesmod.script.ScriptDefaults");
            java.lang.reflect.Field registryField = defaults.getDeclaredField("modulesMap");
            registryField.setAccessible(true);
            Object registry = registryField.get(null);
            if (!(registry instanceof java.util.Map)) {
                settingVisibilityUnavailable = true;
                return;
            }
            nativeScaffoldModule = ((java.util.Map<?, ?>) registry).get(scriptName);
            if (nativeScaffoldModule == null) {
                settingVisibilityUnavailable = true;
                return;
            }
        }
        Object settings = nativeScaffoldModule.getClass().getMethod("getSettings")
                .invoke(nativeScaffoldModule);
        if (!(settings instanceof java.lang.Iterable)) return;
        for (Object setting : (java.lang.Iterable<?>) settings) {
            if (setting == null) continue;
            Object settingLabel = setting.getClass().getMethod("getName").invoke(setting);
            if (!settingName.equals(settingLabel)) continue;
            java.lang.reflect.Method visibilityMethod = null;
            for (java.lang.reflect.Method candidate : setting.getClass().getMethods()) {
                Class<?>[] arguments = candidate.getParameterTypes();
                if (candidate.getName().equals("setVisible") && arguments.length == 2
                        && arguments[0] == Boolean.TYPE
                        && arguments[1].isAssignableFrom(nativeScaffoldModule.getClass())) {
                    visibilityMethod = candidate;
                    break;
                }
            }
            if (visibilityMethod != null) visibilityMethod.invoke(setting,
                    Boolean.valueOf(visible), nativeScaffoldModule);
            return;
        }
    } catch (Throwable unavailable) {
        // ClassNotFound / NoClassDefFoundError / inaccessible methods:
        // visibility is optional, gameplay must never crash because of it.
        settingVisibilityUnavailable = true;
        nativeScaffoldModule = null;
    }
}

void pollActionButtons() {
    if (editPosition != null) editPosition.pollAction();
}

void openCounterPositionEditor() {
    if (client.getPlayer() == null || editPositionMode) return;
    editPositionMode = true;
    editDragging = false;
    editLeftWasDown = keybinds.isMouseDown(0);
    editRightWasDown = keybinds.isMouseDown(1);
    // This is Raven's actual empty GUI, not a fake overlay. It frees the cursor.
    render.showGui();
}

void closeCounterPositionEditor() {
    editPositionMode = false;
    editDragging = false;
    editLeftWasDown = false;
    editRightWasDown = false;
    if ("EmptyGuiScreen".equals(client.getScreen())) client.closeScreen();
}

float getCounterEditWidth() {
    syncBlockCounterFont();
    String preview = getEditPreviewText();
    float textWidth = blockCounterFont != null
            ? blockCounterFont.getStringWidth(preview) : render.getFontWidth(preview);
    return textWidth + (getBlockCounterIcon() != null ? 18.0F : 0.0F);
}

void renderEditPosition() {
    // The editor must stay open WHILE the Raven GUI is present. ESC closes it.
    if (!"EmptyGuiScreen".equals(client.getScreen())) {
        editPositionMode = false;
        editDragging = false;
        editLeftWasDown = editRightWasDown = false;
        return;
    }

    int[] display = client.getDisplaySize();
    int[] mouse = keybinds.getMousePosition();
    int scale = Math.max(1, display.length > 2 ? display[2] : 1);
    int mx = mouse[0] / scale;
    int my = display[1] - mouse[1] / scale;
    boolean left = keybinds.isMouseDown(0);
    boolean right = keybinds.isMouseDown(1);

    syncPosition();
    float width = getCounterEditWidth();
    float minX = posX - 8.0F;
    float maxX = posX + width + 8.0F;
    float minY = posY - 9.0F;
    float maxY = posY + 16.0F;
    boolean hovered = mx >= minX && mx <= maxX && my >= minY && my <= maxY;

    // Forge onMouse() isn't dispatched reliably when a GuiScreen owns the cursor.
    // Edge-detect the actual LWJGL button state from the render tick instead.
    if (right && !editRightWasDown) {
        resetPosition();
        editDragging = false;
    }
    if (left && !editLeftWasDown && hovered) {
        editDragging = true;
        editLastMouseX = mx;
        editLastMouseY = my;
        editLastX = posX;
        editLastY = posY;
    }
    if (!left) editDragging = false;
    if (editDragging && left) {
        float nx = editLastX + mx - editLastMouseX;
        float ny = editLastY + my - editLastMouseY;
        nx = (float) MathHelper.clamp_double(nx, 8.0D,
                Math.max(8.0D, display[0] - width - 8.0D));
        ny = (float) MathHelper.clamp_double(ny, 10.0D,
                Math.max(10.0D, display[1] - 23.0D));
        setAbsolutePosition(nx, ny);
    }
    editLeftWasDown = left;
    editRightWasDown = right;

    syncPosition();
    width = getCounterEditWidth();
    render.rect(0, 0, display[0], display[1], 0x70000000);
    render.rect(posX - 7.0F, posY - 8.0F,
            posX + width + 7.0F, posY + 16.0F,
            editDragging ? 0x995CA8E8 : (hovered ? 0x885CA8E8 : 0x665CA8E8));
    renderBlockCount(255, true);

    String message = "Drag block counter  |  Right click: reset  |  ESC: save & close";
    float x = Math.max(4.0F, display[0] / 2.0F - render.getFontWidth(message) / 2.0F);
    render.text2d(message, x, Math.max(5.0F, display[1] / 2.0F - 40.0F),
            1.0F, 0xFFFFFFFF, true);
}

String getEditPreviewText() {
    int blocks = getTotalBlocks();
    if (blocks == 0) blocks = 64;
    return blocks + " blocks left";
}

class GroupSetting {
    String name;
    GroupSetting(String name) { this.name = name; modules.registerGroup(name); }
}

class SliderSetting {
    String name, suffix;
    String[] options;
    boolean disabledEnum;
    boolean disabledNumeric;
    Boolean visibleState;
    double numericMin;
    double numericStep;

    SliderSetting(String name, boolean canDisable, double def, String[] options) {
        this.name=name; this.options=options; this.suffix=""; this.disabledEnum=canDisable;
        if (canDisable) {
            String[] shown=new String[options.length+1];
            shown[0]="Disabled";
            for(int i=0;i<options.length;i++) shown[i+1]=options[i];
            int uiDefault=def<0?0:(int)Math.round(def)+1;
            modules.registerSlider(name,uiDefault,shown);
        } else modules.registerSlider(name,(int)Math.round(def),options);
    }
    SliderSetting(String name, double def, String[] options) { this(name,false,def,options); }
    SliderSetting(GroupSetting group, String name, int def, String[] options) {
        this.name=name; this.options=options; this.suffix="";
        modules.registerSlider(group.name,name,"",def,options);
    }
    SliderSetting(String name, boolean canDisable, double def, double min, double max, double step) {
        this.name=name; this.suffix="";
        if (canDisable) {
            disabledNumeric=true; numericMin=min; numericStep=step;
            int count=(int)Math.round((max-min)/step)+1;
            String[] shown=new String[count+1]; shown[0]="Disabled";
            for(int i=0;i<count;i++) {
                double v=min+i*step;
                shown[i+1]=(Math.rint(v)==v)?String.valueOf((int)v):String.valueOf(v);
            }
            int uiDefault=0;
            if(def>=min) uiDefault=1+(int)Math.round((def-min)/step);
            modules.registerSlider(name,uiDefault,shown);
        } else modules.registerSlider(name,"",def,min,max,step);
    }
    SliderSetting(String name, String suffix, double def, double min, double max, double step) {
        this.name=name; this.suffix=suffix; modules.registerSlider(name,suffix,def,min,max,step);
    }
    SliderSetting(String name, double def, double min, double max, double step) { this(name,"",def,min,max,step); }
    SliderSetting(GroupSetting group, String name, String suffix, double def, double min, double max, double step) {
        this.name=name; this.suffix=suffix; modules.registerSlider(group.name,name,suffix,def,min,max,step);
    }
    double getInput() {
        double raw=modules.getSlider(scriptName,name);
        if(disabledEnum) return raw<=0?-1:raw-1;
        if(disabledNumeric) return raw<=0?-1:numericMin+(raw-1)*numericStep;
        return raw;
    }
    String getSelectedOption() {
        if(options==null||options.length==0) return "";
        int i=(int)Math.round(getInput());
        if(i<0) return "Disabled";
        if(i>=options.length)i=options.length-1;
        return options[i];
    }
    void setVisible(boolean visible,Object owner) {
        if(visibleState!=null && visibleState.booleanValue()==visible) return;
        visibleState=visible; setSettingVisibility(name,visible);
    }
}

class KeySetting {
    String name;
    boolean previousDown;

    KeySetting(GroupSetting group, String name, int defaultKey) {
        this.name = name;
        modules.registerKey(group.name, name, defaultKey);
    }

    boolean isDown() {
        return modules.getKeyPressed(scriptName, name);
    }

    boolean consumePress() {
        boolean down = isDown();
        boolean pressed = down && !previousDown;
        previousDown = down;
        return pressed;
    }

    void syncState() {
        previousDown = isDown();
    }
}

class ButtonSetting {
    String name;
    Runnable action;
    boolean actionLatch;
    Boolean visibleState;
    ButtonSetting(String name, boolean def) { this.name=name; modules.registerButton(name, def); }
    ButtonSetting(GroupSetting group, String name, boolean def) { this.name=name; modules.registerButton(group.name,name,def); }
    ButtonSetting(String name, Runnable action) { this.name=name; this.action=action; modules.registerButton(name, false); }
    ButtonSetting(GroupSetting group, String name, Runnable action) { this.name=name; this.action=action; modules.registerButton(group.name, name, false); }
    boolean isToggled() { return modules.getButton(scriptName, name); }
    void setVisible(boolean visible, Object owner) {
        if(visibleState!=null && visibleState.booleanValue()==visible) return;
        visibleState=visible; setSettingVisibility(name,visible);
    }
    void pollAction() {
        if (action == null) return;
        boolean now=modules.getButton(scriptName, name);
        if (now && !actionLatch) {
            actionLatch=true;
            modules.setButton(scriptName, name, false);
            action.run();
        } else if (!now) actionLatch=false;
    }
}

class Timer {
    long duration, start;
    Timer(long duration) { this.duration=duration; }
    void start() { start=client.time(); }
    float getValueFloat(float from, float to, int unused) {
        double t=MathHelper.clamp_double((client.time()-start)/(double)Math.max(1L,duration),0.0,1.0);
        return (float)(from+(to-from)*t);
    }
    int getValueInt(int from, int to, int unused) { return Math.round(getValueFloat(from,to,unused)); }
}

class ScaledResolution {
    int w,h;
    ScaledResolution(MCAdapter ignored) { int[] d=client.getDisplaySize(); w=d[0]; h=d[1]; }
    int getScaledWidth(){return w;}
    int getScaledHeight(){return h;}
}

class FontRendererAdapter {
    final int FONT_HEIGHT = 9;
    int getStringWidth(String text){ return render.getFontWidth(text); }
    void drawStringWithShadow(String text,float x,float y,int color){ render.text2d(text,x,y,1.0f,color,true); }
}

class GameSettingsAdapter {
    KeyBinding keyBindSprint=new KeyBinding("sprint");
    KeyBinding keyBindForward=new KeyBinding("forward");
    KeyBinding keyBindBack=new KeyBinding("back");
    KeyBinding keyBindLeft=new KeyBinding("left");
    KeyBinding keyBindRight=new KeyBinding("right");
    KeyBinding keyBindJump=new KeyBinding("jump");
}

static class KeyBinding {
    String name;
    KeyBinding(String name){this.name=name;}
    int getKeyCode(){return keybinds.getKeyCode(name);}
    boolean isKeyDown(){return keybinds.isPressed(name);}
    static void setKeyBindState(int code, boolean state) {
        String[] names={"sprint","forward","back","left","right","jump","sneak","use","attack"};
        for(String n:names) if(keybinds.getKeyCode(n)==code){ keybinds.setPressed(n,state); return; }
    }
}

class InventoryAdapter {
    int currentItem;
    ItemStack[] mainInventory=new ItemStack[36];
    void sync(){
        currentItem=inventory.getSlot();
        for(int i=0;i<mainInventory.length;i++) mainInventory[i]=inventory.getStackInSlot(i);
    }
    ItemStack getStackInSlot(int slot){ return slot>=0&&slot<mainInventory.length?mainInventory[slot]:null; }
}

class SendQueueAdapter {
    void addToSendQueue(Object packet){
        if(packet instanceof C0APacketAnimation) client.sendPacket(new C0A());
        else if(packet instanceof C08PacketPlayerBlockPlacement){
            C08PacketPlayerBlockPlacement p=(C08PacketPlayerBlockPlacement)packet;
            client.sendPacket(new C08(p.stack,new Vec3(p.pos.x,p.pos.y,p.pos.z),p.direction,new Vec3(p.ox,p.oy,p.oz)));
        }
    }
}

class EntityPlayerSP {
    Entity raw;
    double posX,posY,posZ,motionX,motionY,motionZ;
    float rotationYaw,rotationPitch;
    boolean onGround;
    InventoryAdapter inventory=new InventoryAdapter();
    MovementInput movementInput=new MovementInput(0,0,false,false);
    SendQueueAdapter sendQueue=new SendQueueAdapter();
    EntityPlayerSP(Entity e){update(e);}
    void update(Entity e){
        raw=e; if(e==null)return;
        Vec3 p=e.getPosition(),m=client.getMotion();
        posX=p.x;posY=p.y;posZ=p.z; motionX=m.x;motionY=m.y;motionZ=m.z;
        rotationYaw=e.getYaw();rotationPitch=e.getPitch();onGround=e.onGround();
        inventory.sync(); movementInput=new MovementInput(client.getForward(),client.getStrafe(),client.isJump(),client.isSneak());
    }
    Vec3 getPositionVector(){return new Vec3(posX,posY,posZ);}
    ItemStack getHeldItem(){return raw==null?null:raw.getHeldItem();}
    float getEyeHeight(){return raw==null?1.62f:raw.getEyeHeight();}
    void setSprinting(boolean v){client.setSprinting(v);}
    void swingItem(){client.swing();}
}

class WorldAdapter { }
class PlayerControllerAdapter {
    boolean onPlayerRightClick(EntityPlayerSP player, WorldAdapter worldObj, ItemStack stack, BlockPos support, EnumFacing side, Vec3 hit) {
        return client.placeBlock(new Vec3(support.x,support.y,support.z), side.name(), hit);
    }
}
class NetHandlerAdapter { void addToSendQueue(Object packet){ if(mc.thePlayer!=null) mc.thePlayer.sendQueue.addToSendQueue(packet); } }
class RenderManagerAdapter { double viewerPosX=0,viewerPosY=0,viewerPosZ=0; }

class MCAdapter {
    EntityPlayerSP thePlayer;
    Object currentScreen;
    WorldAdapter theWorld=new WorldAdapter();
    GameSettingsAdapter gameSettings=new GameSettingsAdapter();
    FontRendererAdapter fontRendererObj=new FontRendererAdapter();
    PlayerControllerAdapter playerController=new PlayerControllerAdapter();
    NetHandlerAdapter netHandler=new NetHandlerAdapter();
    RenderManagerAdapter renderManager=new RenderManagerAdapter();
    void sync(){
        Entity e=client.getPlayer();
        if(e==null) thePlayer=null; else if(thePlayer==null) thePlayer=new EntityPlayerSP(e); else thePlayer.update(e);
        currentScreen=client.getScreen().isEmpty()?null:client.getScreen();
        Float sy=client.getServerYaw(), sp=client.getServerPitch();
        if(sy!=null) RotationUtils.serverRotations[0]=sy;
        if(sp!=null) RotationUtils.serverRotations[1]=sp;
    }
    NetHandlerAdapter getNetHandler(){return netHandler;}
    RenderManagerAdapter getRenderManager(){return renderManager;}
    
}

class PrePlayerInputEvent {
    MovementInput raw;
    PrePlayerInputEvent(MovementInput raw){this.raw=raw;}
    float getForward(){return raw.forward;}
    float getStrafe(){return raw.strafe;}
    boolean isJump(){return raw.jump;}
    boolean isSneak(){return raw.sneak;}
    void setForward(float v){raw.forward=v;}
    void setStrafe(float v){raw.strafe=v;}
    void setJump(boolean v){raw.jump=v;}
    void setSneak(boolean v){raw.sneak=v;}
}

static class SimulatedPlayer {
    Simulation sim;
    MovementInput movementInput;
    float rotationYaw;
    SimulatedPlayer(Simulation sim, MovementInput input){this.sim=sim;this.movementInput=input;}
    static SimulatedPlayer fromClientPlayer(MovementInput input){
        MovementInput copy=new MovementInput(input.forward,input.strafe,input.jump,input.sneak);
        return new SimulatedPlayer(Simulation.create(),copy);
    }
    void tick(){
        sim.setForward(movementInput.forward);sim.setStrafe(movementInput.strafe);sim.setJump(movementInput.jump);sim.setSneak(movementInput.sneak);sim.setYaw(rotationYaw);sim.tick();
    }
    Vec3 getPos(){return sim.getPosition();}
}

static class BlockPos {
    final int x,y,z;
    BlockPos(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
    int getX(){return x;} int getY(){return y;} int getZ(){return z;}
    BlockPos add(int dx,int dy,int dz){return new BlockPos(x+dx,y+dy,z+dz);}
    BlockPos offset(EnumFacing f){return new BlockPos(x+f.dx,y+f.dy,z+f.dz);}
    public boolean equals(Object o){if(!(o instanceof BlockPos))return false;BlockPos p=(BlockPos)o;return p.x==x&&p.y==y&&p.z==z;}
    public int hashCode(){return (x*7340033)^(y*4099)^(z*19349663);}
    public String toString(){return x+","+y+","+z;}
}

static class EnumFacing {
    static final EnumFacing DOWN=new EnumFacing("DOWN",0,-1,0);
    static final EnumFacing UP=new EnumFacing("UP",0,1,0);
    static final EnumFacing NORTH=new EnumFacing("NORTH",0,0,-1);
    static final EnumFacing SOUTH=new EnumFacing("SOUTH",0,0,1);
    static final EnumFacing WEST=new EnumFacing("WEST",-1,0,0);
    static final EnumFacing EAST=new EnumFacing("EAST",1,0,0);
    final String id; final int dx,dy,dz;
    EnumFacing(String id,int x,int y,int z){this.id=id;dx=x;dy=y;dz=z;}
    int getFrontOffsetX(){return dx;} int getFrontOffsetY(){return dy;} int getFrontOffsetZ(){return dz;}
    String name(){return id;}
    static EnumFacing valueOf(String s){
        if(s==null) throw new IllegalArgumentException();
        String n=s.toUpperCase();
        if("DOWN".equals(n))return DOWN; if("UP".equals(n))return UP;
        if("NORTH".equals(n))return NORTH; if("SOUTH".equals(n))return SOUTH;
        if("WEST".equals(n))return WEST; if("EAST".equals(n))return EAST;
        throw new IllegalArgumentException(n);
    }
    EnumFacing getOpposite(){
        if(this==DOWN)return UP; if(this==UP)return DOWN; if(this==NORTH)return SOUTH;
        if(this==SOUTH)return NORTH; if(this==WEST)return EAST; return WEST;
    }
    public String toString(){return id;}
}

static class MovingObjectPosition {
    BlockPos pos; Vec3 hitVec; EnumFacing sideHit;
    MovingObjectPosition(BlockPos p,Vec3 hit,EnumFacing face){pos=p;hitVec=hit;sideHit=face;}
    BlockPos getBlockPos(){return pos;}
}

static class IBlockState {
    Block block;
    IBlockState(Block b){block=b;}
    Block getBlock(){return block;}
}

class C08PacketPlayerBlockPlacement {
    BlockPos pos; int direction; ItemStack stack; float ox,oy,oz;
    C08PacketPlayerBlockPlacement(BlockPos pos,int direction,ItemStack stack,float ox,float oy,float oz){this.pos=pos;this.direction=direction;this.stack=stack;this.ox=ox;this.oy=oy;this.oz=oz;}
}
class C0APacketAnimation { }

static class MathHelper {
    static int floor_double(double v){int i=(int)v;return v<i?i-1:i;}
    static float wrapAngleTo180_float(float a){a%=360f;if(a>=180f)a-=360f;if(a<-180f)a+=360f;return a;}
    static float clamp_float(float v,float lo,float hi){return v<lo?lo:(v>hi?hi:v);}
    static int clamp_int(int v,int lo,int hi){return v<lo?lo:(v>hi?hi:v);}
    static double clamp_double(double v,double lo,double hi){return v<lo?lo:(v>hi?hi:v);}
}

static class RotationUtils {
    static float[] serverRotations=new float[]{0f,0f};
    static float clampPitch(float p){return MathHelper.clamp_float(p,-90f,90f);}
    static MovingObjectPosition rayCastBlock(double reach,float yaw,float pitch){
        Object[] ray=client.raycastBlock(reach,yaw,pitch);
        if(ray==null||ray.length<3||ray[0]==null||ray[1]==null||ray[2]==null)return null;
        Vec3 p=(Vec3)ray[0], local=(Vec3)ray[1];
        BlockPos bp=new BlockPos(MathHelper.floor_double(p.x),MathHelper.floor_double(p.y),MathHelper.floor_double(p.z));
        EnumFacing face;
        try{face=EnumFacing.valueOf(String.valueOf(ray[2]));}catch(Exception e){return null;}
        return new MovingObjectPosition(bp,new Vec3(bp.x+local.x,bp.y+local.y,bp.z+local.z),face);
    }
}

static class RotationHelperAdapter {
    boolean forceMovementFix=false;
    Float getServerYaw(){Float y=client.getServerYaw();return y==null?RotationUtils.serverRotations[0]:y;}
    boolean fixMovement(){if(forceMovementFix){client.enableMovementFix();return true;}return client.isMovementFixActive();}
    void setRotations(float yaw,float pitch){RotationUtils.serverRotations[0]=yaw;RotationUtils.serverRotations[1]=pitch;client.setRotations(yaw,pitch);}
}
static class RotationHelper {
    static RotationHelperAdapter INSTANCE=new RotationHelperAdapter();
    static RotationHelperAdapter get(){return INSTANCE;}
}

static class Utils {
    static boolean nullCheck(){return client.getPlayer()!=null;}
    static boolean isBindDown(KeyBinding b){return b!=null&&keybinds.isPressed(b.name);}
    static void switchSlot(int slot,boolean instant){inventory.setSlot(slot);}
    static double getHorizontalSpeed(EntityPlayerSP p){return p==null?0.0:Math.sqrt(p.motionX*p.motionX+p.motionZ*p.motionZ);}
    static int mergeAlpha(int rgb,int alpha){return (MathHelper.clamp_int(alpha,0,255)<<24)|(rgb&0x00FFFFFF);}
    static String formatColor(String s){return util.color(s);}
    static void sendRawMessage(String s){client.print(s);}
}

static class BlockUtils {
    static IBlockState getBlockState(BlockPos p){return new IBlockState(world.getBlockAt(p.x,p.y,p.z));}
    static Block getBlock(int x,int y,int z){return world.getBlockAt(x,y,z);}
    static boolean isInteractable(Block b){return b!=null&&b.interactable;}
    static BlockPos offsetPos(MovingObjectPosition hit){return hit==null?null:hit.getBlockPos().offset(hit.sideHit);}
    static boolean canPlaceBlockOnSide(ItemStack stack,BlockPos support,EnumFacing side){return stack!=null&&client.canPlaceBlock(stack,new Vec3(support.x,support.y,support.z),side.name());}
    static boolean check(BlockPos target,Object ignored){Block b=world.getBlockAt(target.x,target.y,target.z);return b!=null&&b.name!=null&&!",air,water,flowing_water,lava,flowing_lava,fire,tallgrass,deadbush,snow_layer,double_plant,vine,sapling,yellow_flower,red_flower,brown_mushroom,red_mushroom,wheat,carrots,potatoes,nether_wart,reeds,".contains(","+b.name.toLowerCase()+",");}
}

static class RenderUtils {
    static void resetColor(){}
    static void drawColoredString(String text,char separator,int x,int y,long a,long b,boolean shadow,FontRendererAdapter f){render.text2d(text,x,y,1.0f,0xFFFFFFFF,shadow);}
}

static class GlStateManager {
    static void pushMatrix(){} static void popMatrix(){} static void enableBlend(){} static void disableBlend(){} static void blendFunc(int a,int b){} static void color(float r,float g,float b,float a){}
}
static class GL11 { static final int GL_SRC_ALPHA=770,GL_ONE_MINUS_SRC_ALPHA=771; }

boolean isEnabled(){return true;}
