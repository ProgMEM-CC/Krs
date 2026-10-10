package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.Client;
import com.instrumentalist.krs.hacks.ModuleManager;
import com.instrumentalist.krs.hacks.features.movement.MovementFix;
import com.instrumentalist.krs.utils.GuiInputBlocker;
import com.instrumentalist.krs.utils.move.InputUtil;
import com.instrumentalist.krs.utils.value.BooleanValue;
import com.instrumentalist.krs.utils.value.FloatValue;
import com.instrumentalist.krs.utils.value.IntValue;
import com.instrumentalist.krs.utils.value.ListValue;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Movement, timing and aiming for the imported scaffold modes. Placement, slot selection,
 * packet transmission and the final rotation/ray check belong to {@link Scaffold}.
 *
 * <p>Call beginTick once before planning, targetPosition for each placement iteration, then
 * aim (also on idle ticks), and canPlace AFTER aim. Call onPlaced only on a successful use.
 * Further placements in the same tick retain the first placement's rotation, as in the
 * source's verified chain. Input has a pre-MovementFix applyInput hook and a post-MovementFix
 * finishInput hook; the latter preserves Breezily's fractional weave. Neither writes keys.
 */
public final class ScaffoldModeController {
    public static final String[] MODES = {"God Bridge", "Telly Short", "Telly Long", "Snap", "Rush",
            "Ninja", "Breezily", "Snap+", "Hypixel", "Hop"};

    public static final ListValue keepY = new ListValue("Source Keep Y", new String[]{"Off", "Telly A", "Telly B"}, "Off", ScaffoldModeController::visible);
    public static final IntValue multiPlace = new IntValue("Source Multi-place", 1, 1, 3, ScaffoldModeController::visible);
    public static final BooleanValue tellyOnJump = new BooleanValue("Telly On Jump", false, () -> visible("Telly Short", "Telly Long"));
    public static final BooleanValue keepYOnRightClick = new BooleanValue("Keep Y On RMB", false, () -> visible() && !keepY.get().equals("Off"));
    public static final BooleanValue disableOnJumpPotion = new BooleanValue("Keep Y Jump Potion Check", false, () -> visible() && !keepY.get().equals("Off"));
    public static final BooleanValue alwaysSprint = new BooleanValue("Source Always Sprint", false, () -> visible() && !visible("Hypixel"));

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

    private final Minecraft mc = Minecraft.getInstance();
    private ClientLevel world;
    private LocalPlayer player;
    private String lastMode = "";
    private String lastConfiguration = "";
    private boolean active;
    private boolean towering;
    private boolean rotationOwned;
    private ClientInput ownedInput;
    private Input rawInput = Input.EMPTY;
    private Vec2 fractionalInput;
    private float fractionalCameraYaw;
    private int tick;
    private int activationDelay;
    private int placementDelay;
    private int placedThisTick;
    private int aimTick = -1;
    private boolean aimBlocked;
    private boolean wasGrounded;
    private int airTicks;
    private int groundMissTicks;
    private boolean sprintSuppressed;
    private int sprintCooldown;
    private int keepStage;
    private int keepHeight;
    private boolean keepCurrentHeight;
    private boolean keepActive;
    private boolean tellyFlight;
    private boolean takeoff;
    private boolean takeoffLatched;
    private int jumpHoldTicks;
    private float takeoffPitch = 80f;
    private int cardinalSide = 1;
    private boolean diagonalTravel;
    private float lastAimYaw = Float.NaN;
    private float easeSlowdown;

    private boolean longEngaged;
    private boolean longSprintJump = true;
    private int longSide = 1;
    private int longMissTicks;
    private int longRow;
    private BlockPos longOrigin;
    private int longForwardX;
    private int longForwardZ;
    private int lastGroundX;
    private int lastGroundZ;
    private int strafeTapCooldown;
    private final QueueCell[] queue = new QueueCell[3];
    private QueueCell pendingQueue;
    private int queueStallTicks;

    private float ninjaBaseYaw = Float.NaN;
    private float ninjaAnchorYaw = Float.NaN;
    private float ninjaYaw = Float.NaN;
    private float ninjaPitch = 80f;
    private int ninjaMask = -1;
    private int ninjaDiagonalSide;
    private int ninjaLastDiagonalSide;
    private int ninjaRecoveryCooldown;
    private boolean ninjaSneaking;
    private boolean ninjaJumpWasDown;
    private int ninjaSafeTicks;
    private long ninjaReleaseAt;
    private long ninjaJumpUntil;
    private BlockPos ninjaSupport;
    private BlockPos ninjaPulseSupport;

    private int breezilyCounter;
    private int breezilyWait;
    private boolean breezilyLeft = true;
    private float breezilyYaw = Float.NaN;
    private BlockPos rushAnchor;
    private BlockPos rushWarmTarget;
    private int rushWarmTick = -1;
    private int rushHeadingX;
    private int rushHeadingZ;
    private boolean rushTieX = true;
    private float rushLaneYaw = Float.NaN;
    private float rushLanePitch = 80f;
    private float snapPlacementYaw = Float.NaN;
    private int snapHoldTicks;
    private boolean snapReturnPending;
    private int hypixelPlaceTimer;
    private int sourceDelay;
    private int sourceBlocks;
    private int sourceJumpBlocks;
    private int sourceSneakBlocks;
    private int sourceDirectionTicks;
    private float hypixelYaw = Float.NaN;
    private float hypixelPitch = 90f;
    private boolean universalReturn;
    private int hopAirTicks;

    private static final class QueueCell {
        final BlockPos pos;
        int retryAt;
        int verifyAt;

        QueueCell(BlockPos pos) {
            this.pos = pos;
        }
    }

    private record Aim(float yaw, float pitch) { }

    public static boolean handles(String mode) {
        if (mode == null) return false;
        for (String candidate : MODES)
            if (candidate.equalsIgnoreCase(mode)) return true;
        return false;
    }

    private static boolean visible(String... modes) {
        Scaffold scaffold = ModuleManager.getModule(Scaffold.class);
        if (scaffold == null) return false;
        String mode = scaffold.tag();
        if (modes.length == 0) return handles(mode);
        for (String candidate : modes)
            if (candidate.equalsIgnoreCase(mode)) return true;
        return false;
    }

    private static boolean sourceVisible(String style) {
        return visible("Hypixel") && sourceScaffoldMode.get().equals(style);
    }

    private static boolean breezilyVisible() {
        return visible("Breezily") || sourceVisible("Breezily");
    }

    /** Call on disable/world change as well as when switching back to a native KRS mode. */
    public void reset() {
        if (ownedInput != null && mc.player == player && player != null && player.input == ownedInput)
            InputUtil.applyInput(ownedInput, usableScreen() ? physicalInput() : Input.EMPTY);
        if (rotationOwned && Client.rotationManager != null)
            Client.rotationManager.stopRotation();
        world = null;
        player = null;
        active = towering = rotationOwned = false;
        ownedInput = null;
        rawInput = Input.EMPTY;
        fractionalInput = null;
        lastMode = lastConfiguration = "";
        tick = activationDelay = placementDelay = placedThisTick = 0;
        aimTick = -1;
        aimBlocked = wasGrounded = sprintSuppressed = false;
        airTicks = groundMissTicks = sprintCooldown = 0;
        keepStage = keepHeight = 0;
        keepCurrentHeight = keepActive = tellyFlight = takeoff = takeoffLatched = false;
        jumpHoldTicks = 0;
        takeoffPitch = 80f;
        cardinalSide = 1;
        diagonalTravel = false;
        lastAimYaw = Float.NaN;
        easeSlowdown = 0f;
        longEngaged = false;
        longSprintJump = true;
        longSide = 1;
        longMissTicks = longRow = longForwardX = longForwardZ = lastGroundX = lastGroundZ = strafeTapCooldown = 0;
        longOrigin = null;
        Arrays.fill(queue, null);
        pendingQueue = null;
        queueStallTicks = 0;
        ninjaBaseYaw = ninjaAnchorYaw = ninjaYaw = Float.NaN;
        ninjaPitch = 80f;
        ninjaMask = -1;
        ninjaDiagonalSide = ninjaLastDiagonalSide = ninjaRecoveryCooldown = 0;
        clearNinjaSneak();
        ninjaJumpWasDown = false;
        breezilyCounter = breezilyWait = 0;
        breezilyLeft = true;
        breezilyYaw = Float.NaN;
        rushAnchor = rushWarmTarget = null;
        rushWarmTick = -1;
        rushHeadingX = rushHeadingZ = 0;
        rushTieX = true;
        rushLaneYaw = snapPlacementYaw = Float.NaN;
        rushLanePitch = 80f;
        snapHoldTicks = 0;
        snapReturnPending = false;
        hypixelPlaceTimer = sourceDelay = sourceBlocks = sourceJumpBlocks = sourceSneakBlocks = sourceDirectionTicks = 0;
        hypixelYaw = Float.NaN;
        hypixelPitch = 90f;
        universalReturn = false;
        hopAirTicks = 0;
    }

    private boolean prepare(String mode, boolean tower, boolean hasBlocks) {
        if (!handles(mode) || !hasBlocks || mc.player == null || mc.level == null || !usableScreen()
                || mc.player.isSpectator() || !mc.player.isAlive()) {
            if (active) reset();
            return false;
        }
        String configuration = keepY.get() + ":" + (mode.equalsIgnoreCase("Hypixel")
                ? sourceScaffoldMode.get() : mode.equalsIgnoreCase("Hop") ? hopMode.get() : "");
        if (!active || player != mc.player || world != mc.level || !lastMode.equalsIgnoreCase(mode)
                || !lastConfiguration.equals(configuration) || towering != tower) {
            reset();
            player = mc.player;
            world = mc.level;
            lastMode = mode;
            lastConfiguration = configuration;
            active = true;
            towering = tower;
            activationDelay = 3;
            wasGrounded = player.onGround();
            keepHeight = Mth.floor(player.getY());
            lastGroundX = Mth.floor(player.getX());
            lastGroundZ = Mth.floor(player.getZ());
            hypixelYaw = player.getYRot() - 180f;
        }
        return true;
    }

    private boolean ready(String mode) {
        return active && mode != null && lastMode.equalsIgnoreCase(mode) && mc.player == player
                && player != null && mc.level == world && world != null && usableScreen();
    }

    private boolean usableScreen() {
        return mc.gui.screen() == null && !GuiInputBlocker.shouldBlockGameMovement();
    }

    public void beginTick(String mode, boolean tower, boolean hasBlocks) {
        if (!prepare(mode, tower, hasBlocks)) return;
        rawInput = physicalInput();
        tick++;
        if (placedThisTick == 0 && moving(rawInput) && player.onGround() && unsupportedAhead(1.5))
            groundMissTicks++;
        else
            groundMissTicks = 0;
        if (pendingQueue != null && placedThisTick == 0 && replaceable(pendingQueue.pos)) {
            if (++queueStallTicks >= 3) {
                pendingQueue.retryAt = tick + 2;
                pendingQueue = null;
                queueStallTicks = 0;
            }
        }
        placedThisTick = 0;
        aimBlocked = false;
        fractionalInput = null;
        activationDelay = Math.max(0, activationDelay - 1);
        placementDelay = Math.max(0, placementDelay - 1);
        hypixelPlaceTimer = Math.max(0, hypixelPlaceTimer - 1);
        sourceDelay = Math.max(0, sourceDelay - 1);
        snapHoldTicks = Math.max(0, snapHoldTicks - 1);
        strafeTapCooldown = Math.max(0, strafeTapCooldown - 1);
        ninjaRecoveryCooldown = Math.max(0, ninjaRecoveryCooldown - 1);
        sprintCooldown = Math.max(0, sprintCooldown - 1);
        sourceDirectionTicks = Math.min(100, sourceDirectionTicks + 1);
        boolean grounded = player.onGround();
        airTicks = grounded ? 0 : airTicks + 1;
        if (tower) {
            wasGrounded = grounded;
            return;
        }

        updateKeepY();
        boolean tellyEnabled = tellyEnabled(mode);
        takeoff = grounded && moving(rawInput) && !headBlocked()
                && (keepStage > 0 || tellyEnabled && rawInput.jump());
        boolean longEnabled = mode.equalsIgnoreCase("Telly Long") && tellyEnabled;
        if (longEnabled && (takeoff || tellyFlight)) longEngaged = true;
        if (!longEnabled || grounded && !takeoff) {
            longEngaged = false;
            longSprintJump = true;
            longOrigin = null;
            Arrays.fill(queue, null);
            pendingQueue = null;
        }
        if (grounded) {
            lastGroundX = Mth.floor(player.getX());
            lastGroundZ = Mth.floor(player.getZ());
            if (longEngaged && !wasGrounded) {
                longSprintJump = !longSprintJump;
                buildLongQueue();
                if (!longSprintJump) placementDelay = 0;
            }
            tellyFlight = false;
        } else if (takeoffLatched || longEngaged) {
            tellyFlight = true;
        }
        if (takeoff && !takeoffLatched) {
            takeoffLatched = true;
            tellyFlight = true;
            takeoffPitch = Mth.clamp(currentPitch(), 30f, 89f);
            if (!longEngaged || longSprintJump) placementDelay = Math.max(placementDelay, 2);
            if (longEngaged) buildLongQueue();
        }
        if (!grounded) {
            takeoffLatched = false;
            jumpHoldTicks = 0;
        }
        if (!takeoff && grounded) takeoffLatched = false;
        if (!tellyEnabled && !keepActive) tellyFlight = false;
        if (longEngaged && !grounded) longMissTicks++;
        else longMissTicks = 0;

        if (mode.equalsIgnoreCase("Ninja")) updateNinjaLane(rawInput);
        if (mode.equalsIgnoreCase("Breezily") && moving(rawInput)) breezilyYaw = movementYaw(rawInput) + 180f;
        universalReturn = mode.equalsIgnoreCase("Hypixel") && sourceSprintMode.get().equals("Universal") && sourceBlocks >= 3;
        if (universalReturn) sourceBlocks = 0;
        updateSprintPolicy(mode);
        if (mode.equalsIgnoreCase("Hypixel") && !sourceScaffoldMode.get().equals("Hypixel")
                && sourceSprintMode.get().equals("NCP") && moving(rawInput) && motionAllowed()) {
            Vec3 direction = direction(movementYaw(rawInput));
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(direction.x * (0.2873 / 1.84), motion.y, direction.z * (0.2873 / 1.84));
        }
        if (mode.equalsIgnoreCase("Hop")) updateHopMotion();
        wasGrounded = grounded;
    }

    private void updateKeepY() {
        keepActive = !keepY.get().equals("Off") && (!keepYOnRightClick.get() || keyDown(mc.options.keyUse));
        if (!keepActive) {
            keepStage = 0;
            keepCurrentHeight = false;
            keepHeight = Mth.floor(player.getY());
            return;
        }
        if (player.onGround()) {
            keepStage = Math.max(0, keepStage - 1);
            if (keepStage == 0 && moving(rawInput) && !rawInput.jump()
                    && (!disableOnJumpPotion.get() || !player.hasEffect(MobEffects.JUMP_BOOST))) keepStage = 1;
            if (!keepCurrentHeight) keepHeight = Mth.floor(player.getY());
            if (keepY.get().equals("Telly A")) keepCurrentHeight = false;
        } else if (keepY.get().equals("Telly B") && keepStage > 0
                && Mth.floor(player.getY() + player.getDeltaMovement().y) <= keepHeight
                && player.getY() > keepHeight + 1) {
            // The B profile releases the launch-height cap on the source's late descent.
            keepCurrentHeight = true;
        }
        if (Math.abs(player.getY() - keepHeight) > 4) {
            keepHeight = Mth.floor(player.getY());
            keepCurrentHeight = false;
        }
    }

    private boolean tellyEnabled(String mode) {
        return (mode.equalsIgnoreCase("Telly Short") || mode.equalsIgnoreCase("Telly Long"))
                && (!tellyOnJump.get() || rawInput.jump());
    }

    /** Receives the fresh camera-relative keyboard snapshot, before MovementFix. */
    public void applyInput(String mode, ClientInput input, boolean tower, boolean hasBlocks) {
        if (input == null || !prepare(mode, tower, hasBlocks) || player.input != input) return;
        rawInput = input.keyPresses == null ? Input.EMPTY : input.keyPresses;
        fractionalInput = null;
        if (tower) return;
        ownedInput = input;
        Input output = rawInput;
        boolean jump = output.jump();
        boolean sneak = output.shift();
        boolean sprint = output.sprint();
        boolean moving = moving(output);
        boolean canJump = player.onGround() && moving && motionAllowed() && !headBlocked();
        if (canJump && keepActive && keepStage > 0) jump = true;

        if (mode.equalsIgnoreCase("Rush") && rushAutoHop.get() && !keepActive && moving) {
            sprint = true;
            if (canJump && groundMissTicks == 0 && activationDelay == 0) jump = true;
        }
        if (mode.equalsIgnoreCase("Hypixel")) {
            String style = sourceScaffoldMode.get();
            boolean autoJump = sourceAutoJump.get() || style.equals("Telly");
            if (canJump && autoJump) jump = true;
            else if (canJump && style.equals("God Bridge") && sourceJumpBlocks >= 8) {
                jump = true;
                sourceJumpBlocks = 0;
            }
            if (style.equals("God Bridge") && sourceSneak.get()) {
                boolean pulse = sourceDirectionTicks <= 10;
                if (sourceSneakBlocks >= sourceSneakEvery.get()) {
                    pulse = true;
                    sourceSneakBlocks = 0;
                }
                sneak |= pulse;
            }
            if (!style.equals("Hypixel") && sourceSprintMode.get().equals("Vanilla")) sprint = moving;
        }
        if (mode.equalsIgnoreCase("Hop") && !keepActive && moving && motionAllowed()) {
            // Hop's launch is applied exactly once by beginTick; vanilla must not replace Low's 0.34.
            jump = false;
            if (hopAutoSprint.get()) sprint = true;
        }
        if (mode.equalsIgnoreCase("Telly Long") && longEngaged) {
            if (!longSprintJump) sprint = false;
            if (jump && player.onGround() && longSprintJump && jumpHoldTicks < 1
                    && Math.abs(Mth.wrapDegrees(currentYaw() - player.getYRot())) > 12f) {
                jump = false;
                jumpHoldTicks++;
            }
            if (!player.onGround() && left(output) == 0 && strafeTapCooldown == 0) {
                double radians = Math.toRadians(player.getYRot());
                Vec3 motion = player.getDeltaMovement();
                double lateral = motion.x * -Math.cos(radians) + motion.z * -Math.sin(radians);
                if (Math.abs(lateral) > 0.02) {
                    output = directionalInput(output, forward(output), lateral > 0 ? 1 : -1);
                    strafeTapCooldown = ThreadLocalRandom.current().nextInt(2, 4);
                }
            }
        }
        output = flags(output, jump, sneak, sprint);
        if (mode.equalsIgnoreCase("Ninja")) output = ninjaInput(output);
        if (mode.equalsIgnoreCase("Breezily")) output = breezilyInput(output, false);
        else if (mode.equalsIgnoreCase("Hypixel") && sourceScaffoldMode.get().equals("Breezily"))
            output = breezilyInput(output, true);

        updateSprintPolicy(mode);
        if (sprintSuppressed || output.shift()) {
            output = flags(output, output.jump(), output.shift(), false);
            player.setSprinting(false);
        } else if (output.sprint() && moving(output) && motionAllowed()
                && (player.getFoodData().getFoodLevel() > 6 || player.getAbilities().flying)) {
            player.setSprinting(true);
        }
        InputUtil.applyInput(input, output);
        if (fractionalInput != null) InputUtil.setMoveVector(input, fractionalInput);
    }

    /**
     * Call immediately after MovementFix.updateInput. Its digital reconstruction otherwise
     * turns the source's 0.62 weave into a full diagonal. No-op for all other input.
     */
    public void finishInput(String mode, ClientInput input) {
        if (!ready(mode) || towering || fractionalInput == null || input != ownedInput) return;
        float yaw = MovementFix.shouldFixMovement() ? Client.rotationManager.getRotationYaw() : player.getYRot();
        double delta = Math.toRadians(fractionalCameraYaw - yaw);
        float left = (float) (fractionalInput.x * Math.cos(delta) - fractionalInput.y * Math.sin(delta));
        float forward = (float) (fractionalInput.x * Math.sin(delta) + fractionalInput.y * Math.cos(delta));
        InputUtil.setMoveVector(input, new Vec2(left, forward));
        fractionalInput = null;
    }

    private Input breezilyInput(Input input, boolean sourceStyle) {
        if (breezilyOnlyStrafeOnGround.get()) {
            if (input.jump() || !player.onGround()) {
                breezilyWait = breezilyGroundWaitTicks.get();
                breezilyCounter = 0;
                return input;
            }
            if (breezilyWait > 0) {
                breezilyWait--;
                return input;
            }
        } else breezilyWait = 0;
        if (!moving(input)) {
            breezilyCounter = 0;
            return input;
        }
        if (sourceStyle) {
            if (forward(input) <= 0 || input.jump()) return input;
            double yaw = Mth.positiveModulo(player.getYRot(), 360f);
            double fx = player.getX() - Math.floor(player.getX());
            double fz = player.getZ() - Math.floor(player.getZ());
            Vec3 motion = player.getDeltaMovement();
            double offset;
            double speed;
            if (yaw >= 135 && yaw < 225) { offset = fx; speed = Math.abs(motion.z); }
            else if (yaw >= 225 && yaw < 315) { offset = fz; speed = Math.abs(motion.x); }
            else if (yaw >= 45 && yaw < 135) { offset = 1 - fz; speed = Math.abs(motion.x); }
            else { offset = 1 - fx; speed = Math.abs(motion.z); }
            if (speed < 0.086 && Math.abs(offset - 0.5) < 0.4 && sourcePlaceDelay.get() <= 1) return input;
            return directionalInput(input, forward(input), offset < random(0.45f, 0.55f) ? -1 : 1);
        }
        if (input.jump() || !player.onGround()) {
            // Jumping Breezily deliberately uses the ordinary God Bridge path.
            breezilyCounter = 0;
            return input;
        }
        if (!breezilyAutoStrafe.get()) return input;
        Vec2 base = InputUtil.movementVector(input);
        float weave = breezilyLeft ? 0.62f : -0.62f;
        fractionalInput = new Vec2(base.x + base.y * weave, base.y - base.x * weave).normalized();
        fractionalCameraYaw = player.getYRot();
        Input result = directionalInput(input, fractionalInput.y, fractionalInput.x);
        if (++breezilyCounter >= breezilySwitchSpeed.get()) {
            breezilyCounter = 0;
            breezilyLeft = !breezilyLeft;
        }
        return result;
    }

    private void updateSprintPolicy(String mode) {
        boolean stop;
        if (Scaffold.noSprint.get()) stop = true;
        else if (towering) stop = false;
        else if (mode.equalsIgnoreCase("Hypixel")) {
            // The original Hypixel style forces LEGIT regardless of the selected policy.
            stop = !sourceScaffoldMode.get().equals("Hypixel")
                    && (sourceSprintMode.get().equals("None") || sourceSprintMode.get().equals("NCP"));
        } else if (mode.equalsIgnoreCase("Telly Long") && longEngaged && !longSprintJump) stop = true;
        else if (alwaysSprint.get() || mode.equalsIgnoreCase("Snap+") || mode.equalsIgnoreCase("Hop")
                || mode.equalsIgnoreCase("Rush") && rushAutoHop.get() || tellyFlight || takeoff) stop = false;
        else {
            Vec3 motion = player.getDeltaMovement();
            float travel = horizontalSpeed(motion) >= 0.04 ? (float) Math.toDegrees(Math.atan2(-motion.x, motion.z)) : movementYaw(rawInput);
            float difference = Math.abs(Mth.wrapDegrees(currentYaw() - travel));
            stop = sprintSuppressed ? difference >= 40f : difference > 55f;
            if (sprintCooldown > 0) stop = sprintSuppressed;
        }
        if (stop != sprintSuppressed) sprintCooldown = 4;
        sprintSuppressed = stop;
        if (stop) player.setSprinting(false);
    }

    public boolean suppressSprint(String mode) {
        return ready(mode) && !towering && sprintSuppressed;
    }

    private void updateHopMotion() {
        if (!motionAllowed() || !moving(rawInput) || keepActive) {
            hopAirTicks = 0;
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        String variant = hopMode.get();
        double speed = Mth.clamp(hopSpeed.get(), 1f, 1.18f);
        if (player.onGround()) {
            hopAirTicks = 0;
            if (activationDelay > 0 || groundMissTicks > 0 || headBlocked()) return;
            double multiplier = switch (variant) {
                case "Low" -> Math.min(1.10, speed);
                case "Boost" -> speed;
                case "Burst" -> Math.min(1.18, speed + 0.03);
                case "Adaptive" -> unsupportedAhead(2.75) ? 1.02 : Math.min(1.18, speed + 0.02);
                default -> 1.0;
            };
            player.setDeltaMovement(motion.x * multiplier, variant.equals("Low") ? 0.34 : 0.42, motion.z * multiplier);
            hopAirTicks = 1;
        } else {
            hopAirTicks++;
            if (variant.equals("Boost") && hopAirTicks == 2) {
                double multiplier = Math.min(1.08, 1 + (speed - 1) * 0.45);
                player.setDeltaMovement(motion.x * multiplier, motion.y, motion.z * multiplier);
            } else if (variant.equals("Burst") && (hopAirTicks == 3 || hopAirTicks == 5)) {
                player.setDeltaMovement(motion.x, motion.y - (hopAirTicks == 3 ? 0.025 : 0.040), motion.z);
            } else if (variant.equals("Adaptive") && hopAirTicks == 3 && !unsupportedAhead(2.5)) {
                player.setDeltaMovement(motion.x, motion.y - 0.030, motion.z);
            }
        }
    }

    private void updateNinjaLane(Input input) {
        if (!moving(input)) return;
        int mask = (input.forward() ? 1 : 0) | (input.backward() ? 2 : 0)
                | (input.left() ? 4 : 0) | (input.right() ? 8 : 0);
        if (!Float.isFinite(ninjaBaseYaw) || Math.abs(Mth.wrapDegrees(player.getYRot() - ninjaBaseYaw)) > 7f) {
            ninjaBaseYaw = player.getYRot();
            ninjaMask = -1;
        }
        if (mask == ninjaMask && Float.isFinite(ninjaYaw)) return;
        int forward = forward(input);
        int left = left(input);
        ninjaDiagonalSide = forward != 0 && left != 0 ? Integer.signum(left) : 0;
        float sideAngle = ninjaDiagonalSide != 0 ? ninjaDiagonalSide * 9f
                : forward < 0 ? (ninjaLastDiagonalSide != 0 ? ninjaLastDiagonalSide : -1) * 19f : 0f;
        if (ninjaDiagonalSide != 0) ninjaLastDiagonalSide = ninjaDiagonalSide;
        ninjaYaw = ninjaAnchorYaw = inputYaw(ninjaBaseYaw, forward, left) - 180f + sideAngle;
        if (ninjaMask == -1 && !Float.isFinite(lastAimYaw))
            ninjaPitch = player.getXRot() >= 75f && player.getXRot() <= 84f ? player.getXRot() : 80f;
        ninjaMask = mask;
        ninjaRecoveryCooldown = 0;
    }

    private Input ninjaInput(Input input) {
        boolean manualSneak = keyDown(mc.options.keyShift);
        boolean requireKey = ninjaRequireSneakKey.get();
        boolean jumpStarted = input.jump() && !ninjaJumpWasDown;
        ninjaJumpWasDown = input.jump();
        if ((!requireKey && manualSneak) || (requireKey && !manualSneak) || !motionAllowed()) {
            clearNinjaSneak();
            return input;
        }
        if (requireKey && !moving(input)) {
            clearNinjaSneak();
            return flags(input, input.jump(), true, input.sprint());
        }
        boolean holdingBlocks = player.getMainHandItem().getItem() instanceof BlockItem
                || player.getOffhandItem().getItem() instanceof BlockItem;
        boolean allowed = (!ninjaRequireBlocks.get() || holdingBlocks)
                && (!ninjaRequireLookingDown.get() || player.getXRot() >= 70f)
                && (!ninjaDisableForward.get() || forward(input) <= 0);
        if (!allowed) {
            clearNinjaSneak();
            return requireKey ? flags(input, input.jump(), true, input.sprint()) : input;
        }
        long now = System.nanoTime() / 1_000_000L;
        if (jumpStarted && player.onGround() && moving(input) && ninjaSneakOnJump.get() > 0) {
            ninjaJumpUntil = now + ninjaSneakOnJump.get();
            ninjaReleaseAt = 0;
        }
        Vec3 current = player.position();
        // One unsneaked step: native velocity plus ground acceleration in the raw camera lane.
        // Collision clipping keeps the predictor from sneaking into a wall rather than an edge.
        Vec3 motion = player.getDeltaMovement();
        Vec3 acceleration = moving(input) ? direction(movementYaw(input)).scale(player.onGround() ? 0.1 : 0.02) : Vec3.ZERO;
        Vec3 delta = new Vec3(motion.x + acceleration.x, 0, motion.z + acceleration.z);
        AABB body = player.getBoundingBox();
        if (!world.noCollision(player, body.move(delta.x, 0, 0))) delta = new Vec3(0, 0, delta.z);
        if (!world.noCollision(player, body.move(delta.x, 0, delta.z))) delta = new Vec3(delta.x, 0, 0);
        Vec3 predicted = current.add(delta);
        int floor = Mth.floor(player.getY() - 0.01);
        double overhang = ninjaEdgeOffset(predicted, current, floor);
        double threshold = Mth.clamp(ninjaEdgeDistance.get(), 0f, 0.30f);
        boolean supported = cornerSupport(predicted.x, predicted.z, floor);
        boolean edge = player.onGround() && (Double.isNaN(overhang) ? !supported : overhang > threshold);
        if (player.onGround() && forward(input) != 0 && left(input) != 0 && !supported) edge = true;
        BlockPos support = frontSupport(current, floor, 0, 0);
        boolean changed = support != null && !support.equals(ninjaSupport);
        ninjaSupport = support;
        if (changed) ninjaSafeTicks = 0;
        boolean nearEdge = !Double.isNaN(overhang) && overhang > Math.max(0, threshold - 0.05);
        boolean hold = edge || ninjaJumpUntil > now || ninjaSneaking && ninjaAvoidDoubleSneaking.get()
                && support != null && support.equals(ninjaPulseSupport) && !changed && nearEdge;
        if (hold) {
            ninjaSneaking = true;
            ninjaPulseSupport = support;
            ninjaSafeTicks = 0;
            ninjaReleaseAt = 0;
        } else if (ninjaSneaking) {
            ninjaSafeTicks = Math.min(6, ninjaSafeTicks + 1);
            if (supported && (!ninjaAvoidDoubleSneaking.get() || changed || ninjaSafeTicks >= 2)) {
                if (ninjaReleaseAt == 0) {
                    int min = Math.min(ninjaMinDelay.get(), ninjaMaxDelay.get());
                    int max = Math.max(ninjaMinDelay.get(), ninjaMaxDelay.get());
                    ninjaReleaseAt = now + ThreadLocalRandom.current().nextInt(min, max + 1);
                }
                if (now >= ninjaReleaseAt) {
                    ninjaSneaking = false;
                    ninjaReleaseAt = 0;
                    ninjaSafeTicks = 0;
                }
            } else ninjaReleaseAt = 0;
        } else {
            ninjaSafeTicks = 0;
            ninjaPulseSupport = null;
        }
        if (now >= ninjaJumpUntil) ninjaJumpUntil = 0;
        return flags(input, input.jump(), requireKey ? ninjaSneaking : input.shift() || ninjaSneaking, input.sprint());
    }

    private double ninjaEdgeOffset(Vec3 predicted, Vec3 current, int floor) {
        double best = Double.NaN;
        for (double dx : new double[]{-0.30, 0.30}) {
            for (double dz : new double[]{-0.30, 0.30}) {
                int x = Mth.floor(current.x + dx);
                int z = Mth.floor(current.z + dz);
                if (!solid(new BlockPos(x, floor, z))) continue;
                double offX = Math.abs(predicted.x - (x + (predicted.x < x + 0.5 ? 0 : 1)));
                double offZ = Math.abs(predicted.z - (z + (predicted.z < z + 0.5 ? 0 : 1)));
                boolean xDiff = Mth.floor(predicted.x) != x;
                boolean zDiff = Mth.floor(predicted.z) != z;
                double offset = xDiff ? zDiff ? Math.max(offX, offZ) : offX : zDiff ? offZ : 0;
                best = Double.isNaN(best) ? offset : Math.min(best, offset);
            }
        }
        return best;
    }

    private void clearNinjaSneak() {
        ninjaSneaking = false;
        ninjaSafeTicks = 0;
        ninjaReleaseAt = ninjaJumpUntil = 0;
        ninjaSupport = ninjaPulseSupport = null;
    }

    public BlockPos targetPosition(String mode, BlockPos defaultTarget, boolean tower) {
        if (!ready(mode) || defaultTarget == null || tower) return defaultTarget;
        int row = targetRow();
        if (longEngaged && queue[0] != null) row = Math.min(row, longRow + (longSprintJump ? 0 : 1));
        BlockPos target = new BlockPos(defaultTarget.getX(), row, defaultTarget.getZ());
        if (mode.equalsIgnoreCase("Hypixel") || mode.equalsIgnoreCase("Snap+")) return target;
        if (mode.equalsIgnoreCase("Telly Long") && longEngaged && !(player.onGround() && longSprintJump)) {
            BlockPos queued = nextQueuedTarget();
            if (queued != null) return queued;
        }
        if (mode.equalsIgnoreCase("Rush")) {
            BlockPos rushTarget = rushTarget(row);
            if (rushTarget != null) return rushTarget;
        }
        if (mode.equalsIgnoreCase("Ninja") && Float.isFinite(ninjaYaw)) {
            BlockHitResult hit = Client.rotationManager.rayTraceBlocks(currentYaw(), ninjaPitch, player.blockInteractionRange());
            if (hit.getType() == HitResult.Type.BLOCK && hit.getDirection() != Direction.DOWN) {
                BlockPos rayTarget = hit.getBlockPos().relative(hit.getDirection());
                if (inNinjaLane(rayTarget, row)) return rayTarget;
            }
        }
        Vec3 motion = player.getDeltaMovement();
        if (mode.equalsIgnoreCase("Telly Short") && tellyFlight && !player.onGround()
                && Math.min(Math.abs(motion.x), Math.abs(motion.z)) > Math.max(Math.abs(motion.x), Math.abs(motion.z)) * 0.25) {
            double y = player.getY();
            double dy = motion.y;
            int ahead = 1;
            while (ahead < 3 && y + dy > row + 1) { y += dy; dy = (dy - 0.08) * 0.98; ahead++; }
            return BlockPos.containing(player.getX() + motion.x * ahead, row, player.getZ() + motion.z * ahead);
        }
        if (!replaceable(target) && moving(rawInput) && (player.onGround() || keepStage > 0)) {
            Vec3 travel = horizontalSpeed(motion) >= 0.01 ? new Vec3(motion.x, 0, motion.z).normalize() : direction(movementYaw(rawInput));
            for (double distance = 0.25; distance <= 2; distance += 0.25) {
                BlockPos ahead = BlockPos.containing(player.getX() + travel.x * distance, row, player.getZ() + travel.z * distance);
                if (replaceable(ahead)) return ahead;
            }
        }
        return target;
    }

    private int targetRow() {
        int floor = Mth.floor(player.getY());
        return (keepStage > 0 && !keepCurrentHeight ? Math.min(floor, keepHeight) : floor) - 1;
    }

    public boolean canPlace(String mode, boolean tower) {
        if (!ready(mode)) return false;
        if (tower) return true;
        if (aimBlocked || activationDelay > 0 || placedThisTick >= placementLimit(mode, false)) return false;
        if (mode.equalsIgnoreCase("Ninja")) return moving(rawInput);
        if (mode.equalsIgnoreCase("Hypixel") || mode.equalsIgnoreCase("Snap+")) {
            if (!replaceable(BlockPos.containing(player.getX(), Math.floor(player.getY()) - 1, player.getZ()))) return false;
            if (mode.equalsIgnoreCase("Hypixel")) {
                if (sourceDelay > 0 || universalReturn) return false;
                if (sourceScaffoldMode.get().equals("Telly")) {
                    int threshold = rawInput.jump() ? sourceTellyJumpDownTicks.get()
                            : sourceDiagonal() ? sourceTellyDiagonalTicks.get() : sourceTellyStraightTicks.get();
                    if (sourceTellyWatchdog.get() ? airTicks == 0 : airTicks < threshold) return false;
                }
            }
        }
        if (longEngaged && pendingQueue != null) return !(player.onGround() && longSprintJump);
        return placementDelay <= 0 && !takeoff;
    }

    public int placementLimit(String mode, boolean tower) {
        if (!ready(mode)) return 0;
        if (tower) return 1;
        int base = Mth.clamp(multiPlace.get(), 1, 3);
        return mode.equalsIgnoreCase("Rush") ? Math.max(base, Mth.clamp(rushChain.get(), 1, 3)) : base;
    }

    public void onPlaced(String mode, BlockPos pos) {
        if (!ready(mode) || pos == null) return;
        placedThisTick++;
        groundMissTicks = longMissTicks = queueStallTicks = 0;
        if (towering) return;
        for (QueueCell cell : queue) {
            if (cell != null && cell.pos.equals(pos)) {
                cell.verifyAt = tick + 4;
                cell.retryAt = 0;
                if (cell == pendingQueue) pendingQueue = null;
            }
        }
        if (mode.equalsIgnoreCase("Rush")) {
            rushAnchor = pos.immutable();
            rushWarmTarget = null;
        }
        if (mode.equalsIgnoreCase("Snap") && !snapGodBridge()) {
            snapPlacementYaw = currentYaw();
            snapReturnPending = true;
            snapHoldTicks = rawDiagonal() ? 2 : 0;
        }
        if (mode.equalsIgnoreCase("Snap+")) hypixelPlaceTimer = 10;
        if (mode.equalsIgnoreCase("Hypixel")) {
            if (sourceScaffoldMode.get().equals("Hypixel")) hypixelPlaceTimer = 10;
            sourceDelay = (int) Math.ceil(sourcePlaceDelay.get() * 0.4);
            sourceBlocks++;
            sourceJumpBlocks++;
            sourceSneakBlocks++;
        }
    }

    private void buildLongQueue() {
        Arrays.fill(queue, null);
        pendingQueue = null;
        queueStallTicks = 0;
        Vec3 motion = player.getDeltaMovement();
        double x = Math.abs(motion.x);
        double z = Math.abs(motion.z);
        if (x * x + z * z < 0.01 || airTicks > 3) return;
        int dx = 0;
        int dz = 0;
        if (x >= z * 2.4) dx = motion.x > 0 ? 1 : -1;
        else if (z >= x * 2.4) dz = motion.z > 0 ? 1 : -1;
        else return; // The source's long queue is cardinal; diagonal jumps use its ordinary planner.
        if (longSprintJump) {
            if (longOrigin != null) {
                int travelled = (lastGroundX - longOrigin.getX()) * -longForwardZ * longSide
                        + (lastGroundZ - longOrigin.getZ()) * longForwardX * longSide;
                if (travelled >= 1) longSide = -longSide;
            } else {
                double lean = motion.x * -dz + motion.z * dx;
                if (Math.abs(lean) > 0.02) longSide = lean > 0 ? 1 : -1;
            }
            longOrigin = new BlockPos(lastGroundX, 0, lastGroundZ);
        }
        longForwardX = dx;
        longForwardZ = dz;
        longRow = targetRow();
        for (int i = 0; i < queue.length; i++) {
            int offset = longSprintJump ? i + 1 : i;
            queue[i] = new QueueCell(new BlockPos(lastGroundX + dx * offset,
                    longRow + (longSprintJump ? 0 : 1), lastGroundZ + dz * offset));
        }
    }

    private BlockPos nextQueuedTarget() {
        for (QueueCell cell : queue) {
            if (cell == null || cell.retryAt > tick || cell.verifyAt > tick || !replaceable(cell.pos)) continue;
            // Four ticks after a predicted use, a reverted cell becomes eligible again.
            cell.verifyAt = 0;
            double dx = cell.pos.getX() + 0.5 - player.getX();
            double dz = cell.pos.getZ() + 0.5 - player.getZ();
            if (dx * dx + dz * dz > 18 || player.getBoundingBox().intersects(new AABB(cell.pos))) continue;
            boolean support = solid(cell.pos.below());
            for (Direction face : Direction.Plane.HORIZONTAL) support |= solid(cell.pos.relative(face));
            if (!support) continue;
            if (pendingQueue != cell) queueStallTicks = 0;
            pendingQueue = cell;
            return cell.pos;
        }
        pendingQueue = null;
        return null;
    }

    private BlockPos rushTarget(int row) {
        if (!moving(rawInput) || activationDelay > 0) return null;
        double radians = Math.toRadians(Math.round(movementYaw(rawInput) / 45f) * 45f);
        int dx = (int) Math.round(-Math.sin(radians));
        int dz = (int) Math.round(Math.cos(radians));
        Vec3 position = player.position();
        Vec3 motion = player.getDeltaMovement();
        if (dx != rushHeadingX || dz != rushHeadingZ || groundMissTicks >= 4) {
            rushHeadingX = dx;
            rushHeadingZ = dz;
            rushAnchor = rushWarmTarget = null;
            rushLaneYaw = Float.NaN;
            rushTieX = Math.abs(motion.x) >= Math.abs(motion.z);
        }
        BlockPos anchor = rushAnchor;
        if (anchor == null || anchor.getY() != row || !solid(anchor)
                || horizontalDistanceSquared(position, Vec3.atCenterOf(anchor)) > 4.05 * 4.05)
            anchor = frontSupport(position, row, dx, dz);
        if (anchor == null) return null;
        double length = Math.hypot(dx, dz);
        if (length < 0.01) return null;
        double look = Math.max(1.55, Math.min(rushLookAhead.get(), 1.55 + horizontalSpeed(motion) * 2.35));
        int endX = Mth.floor(position.x + dx / length * look + motion.x * 0.18);
        int endZ = Mth.floor(position.z + dz / length * look + motion.z * 0.18);
        for (int scan = 0; scan < 5; scan++) {
            int remX = endX - anchor.getX();
            int remZ = endZ - anchor.getZ();
            if (remX == 0 && remZ == 0) return null;
            boolean stepX = Math.abs(remX) > Math.abs(remZ) || Math.abs(remX) == Math.abs(remZ) && rushTieX;
            if (Math.abs(remX) == Math.abs(remZ)) rushTieX = !rushTieX;
            BlockPos next = anchor.offset(stepX ? Integer.signum(remX) : 0, 0, stepX ? 0 : Integer.signum(remZ));
            if (solid(next)) {
                anchor = next;
                rushAnchor = next;
                continue;
            }
            if (!replaceable(next)) {
                if (stepX && remZ != 0) next = anchor.offset(0, 0, Integer.signum(remZ));
                else if (!stepX && remX != 0) next = anchor.offset(Integer.signum(remX), 0, 0);
                else return null;
            }
            double lead = ((next.getX() + 0.5 - position.x) * dx + (next.getZ() + 0.5 - position.z) * dz) / length;
            Vec3 face = Vec3.atCenterOf(anchor).add((next.getX() - anchor.getX()) * 0.5, 0, (next.getZ() - anchor.getZ()) * 0.5);
            double reach = Math.min(player.blockInteractionRange(), placedThisTick > 0 ? 3.95 : 4.05);
            return replaceable(next) && lead <= look + 0.85 && player.getEyePosition().distanceToSqr(face) <= reach * reach ? next : null;
        }
        return null;
    }

    private BlockPos frontSupport(Vec3 position, int row, int dx, int dz) {
        BlockPos best = null;
        double score = -Double.MAX_VALUE;
        double length = Math.max(1, Math.hypot(dx, dz));
        for (int x = Mth.floor(position.x - 0.30); x <= Mth.floor(position.x + 0.30); x++) {
            for (int z = Mth.floor(position.z - 0.30); z <= Mth.floor(position.z + 0.30); z++) {
                BlockPos pos = new BlockPos(x, row, z);
                if (!solid(pos)) continue;
                double overlapX = Math.max(0, Math.min(position.x + 0.30, x + 1) - Math.max(position.x - 0.30, x));
                double overlapZ = Math.max(0, Math.min(position.z + 0.30, z + 1) - Math.max(position.z - 0.30, z));
                double candidate = overlapX * overlapZ * 10
                        + ((x + 0.5 - position.x) * dx + (z + 0.5 - position.z) * dz) / length;
                if (overlapX * overlapZ > 0 && candidate > score) { best = pos; score = candidate; }
            }
        }
        return best;
    }

    private boolean inNinjaLane(BlockPos pos, int row) {
        if (pos.getY() != row || !replaceable(pos)) return false;
        Vec3 travel = direction(movementYaw(rawInput));
        double dx = pos.getX() + 0.5 - player.getX();
        double dz = pos.getZ() + 0.5 - player.getZ();
        double ahead = dx * travel.x + dz * travel.z;
        double sideways = Math.abs(-dx * travel.z + dz * travel.x);
        return ahead > (ninjaDiagonalSide == 0 ? -0.30 : -0.65) && ahead <= 2.10
                && sideways <= (ninjaDiagonalSide == 0 ? 0.90 : 1.42);
    }

    public void aim(String mode, BlockPos neighbour, Direction face, Vec3 hitPos, boolean tower) {
        if (!ready(mode) || tower || Client.rotationManager == null || placedThisTick > 0) return;
        // A multi-place loop may query the same target repeatedly. Smoothing advances once/tick.
        if (aimTick == tick) return;
        aimTick = tick;
        boolean hasTarget = neighbour != null && face != null && hitPos != null;
        float movement = movementYaw(rawInput);
        float camera = player.getYRot();
        if (universalReturn) {
            aimBlocked = true;
            request(new Aim(camera, currentPitch()), 180f, 180f);
            return;
        }
        if (mode.equalsIgnoreCase("Ninja")) {
            aimNinja(neighbour, face, hitPos, hasTarget);
            return;
        }
        if (mode.equalsIgnoreCase("Snap+") || mode.equalsIgnoreCase("Hypixel")) {
            aimSource(mode, neighbour, face, hitPos, hasTarget, movement, camera);
            return;
        }
        if (takeoff && (!longEngaged || longSprintJump)) {
            takeoffPitch = Mth.clamp(takeoffPitch + random(-0.5f, 0.5f), 30f, 89f);
            request(new Aim(camera, takeoffPitch), 180f, 180f);
            aimBlocked = true;
            return;
        }
        if (mode.equalsIgnoreCase("Snap") && !snapGodBridge()) {
            if (!hasTarget || activationDelay > 0 || placementDelay > 0) {
                boolean safeReturn = !unsupportedAhead(0) && !unsupportedAhead(1.35);
                if (snapReturnPending && safeReturn) snapReturnPending = false;
                float yaw = Float.isFinite(snapPlacementYaw) && (rawDiagonal() || snapHoldTicks > 0 || snapReturnPending)
                        ? snapPlacementYaw : camera;
                request(new Aim(yaw, currentPitch()), 180f, 180f);
            } else {
                Aim aim = chooseAim(neighbour, face, hitPos, currentPitch(), rawDiagonal()
                        ? new float[]{movement - 180, movement - 135, movement + 135, currentYaw()}
                        : new float[]{currentYaw(), preferredBridgeYaw(movement), movement - 180});
                request(aim, 180f, 180f);
            }
            return;
        }
        if (mode.equalsIgnoreCase("Breezily") && player.onGround() && !rawInput.jump()) {
            float yaw = Float.isFinite(breezilyYaw) ? breezilyYaw : camera + 180;
            Aim aim = hasTarget ? fixedYawAim(neighbour, face, yaw, 78f) : null;
            request(aim == null ? new Aim(yaw, 78f) : aim, 180f, 180f);
            return;
        }
        if (mode.equalsIgnoreCase("Rush") && hasTarget) {
            aimRush(neighbour, face, hitPos, movement);
            return;
        }
        float preferred = preferredBridgeYaw(movement);
        Aim desired;
        if (longEngaged && !player.onGround()) {
            float back = (float) Math.toDegrees(Math.atan2(longForwardX, -longForwardZ));
            float held = Float.isFinite(lastAimYaw) ? camera + Math.round(Mth.wrapDegrees(lastAimYaw - camera) / 45f) * 45f : preferred;
            desired = hasTarget ? chooseAim(neighbour, face, hitPos, currentPitch(), longMissTicks < 4
                    ? new float[]{back + longSide * 45, back, held, preferred}
                    : new float[]{held, preferred, back}) : new Aim(preferred, 80f);
            float limit = longSprintJump ? random(30f, 35f) : random(90f, 95f);
            float ease = (float) (1 - Math.pow(0.05, 1.0 / 9.0));
            Aim eased = new Aim(currentYaw() + Mth.wrapDegrees(desired.yaw - currentYaw()) * ease,
                    currentPitch() + (desired.pitch - currentPitch()) * ease);
            if (hasTarget && hits(neighbour, face, currentYaw(), currentPitch())) eased = new Aim(currentYaw(), currentPitch());
            request(eased, limit, limit);
            return;
        }
        desired = hasTarget ? chooseAim(neighbour, face, hitPos, currentPitch(),
                new float[]{currentYaw(), preferred, preferred - 45, preferred + 45, movement - 180}) : new Aim(preferred, 85f);
        boolean urgent = unsupportedAhead(0) || unsupportedAhead(2) || unsupportedAhead(5) || groundMissTicks >= 3;
        float speed;
        if (tellyFlight && !player.onGround()) {
            speed = airTicks <= 1 ? random(90f, 95f) : random(30f, 35f);
        } else if (urgent) {
            speed = 180f;
            easeSlowdown = 0;
        } else {
            speed = easeSlowdown == 0 ? 73f : 43f - Math.min(10f, easeSlowdown);
            easeSlowdown += random(3f, 4f);
        }
        if (hasTarget) {
            float jittered = desired.yaw + random(-0.28f, 0.28f);
            if (hits(neighbour, face, jittered, desired.pitch)) desired = new Aim(jittered, desired.pitch);
        }
        request(desired, speed, speed);
    }

    private void aimSource(String mode, BlockPos neighbour, Direction face, Vec3 hitPos,
                           boolean hasTarget, float movement, float camera) {
        String style = mode.equalsIgnoreCase("Snap+") ? "Hypixel" : sourceScaffoldMode.get();
        boolean watchdog = style.equals("Hypixel") || style.equals("Telly") && sourceTellyWatchdog.get();
        if (style.equals("Hypixel")) {
            float offset = movement + 50f;
            int returnTick = Math.abs(Mth.wrapDegrees(offset - hypixelYaw)) > 60f ? 8 : 9;
            if (hypixelPlaceTimer == returnTick && !rawInput.jump() && player.onGround())
                hypixelYaw = currentYaw() + Mth.clamp(Mth.wrapDegrees(offset - currentYaw()), -yuriStep(), yuriStep());
        }
        boolean placing = hasTarget && canPlace(mode, false);
        if (!placing) {
            if (style.equals("Hypixel")) request(new Aim(hypixelYaw, hypixelPitch), yuriStep(), yuriStep());
            else if (style.equals("Telly") && player.onGround() && moving(rawInput)
                    && (!player.isSprinting() || rawInput.jump())) request(new Aim(camera, currentPitch()), 180f, 180f);
            else request(new Aim(preferredBridgeYaw(movement), 85f), 73f, 73f);
            return;
        }
        Aim desired = null;
        if (watchdog) desired = watchdogAim(neighbour, face, movement);
        if (style.equals("Breezily")) {
            float yaw = face == Direction.UP ? currentYaw()
                    : (float) Math.toDegrees(Math.atan2(face.getStepZ(), face.getStepX())) - 90f;
            desired = fixedYawAim(neighbour, face, yaw, face == Direction.UP ? 90f : 80f);
        }
        boolean strictWatchdogTarget = watchdog && desired != null;
        if (desired == null) {
            float center = Client.rotationManager.getRotationsTo(hitPos.x, hitPos.y, hitPos.z)[0];
            desired = chooseAim(neighbour, face, hitPos, currentPitch(), new float[]{center, movement + 180,
                    movement + 135, movement - 135, currentYaw(), camera + 180, camera + 90, camera - 90});
        }
        if (style.equals("God Bridge") && Math.abs(Mth.wrapDegrees(desired.yaw - currentYaw())) > 10f)
            sourceDirectionTicks = ThreadLocalRandom.current().nextInt(4);
        float step = strictWatchdogTarget ? yuriStep() : 180f;
        request(desired, step, step);
        if (watchdog) {
            hypixelYaw = currentYaw();
            hypixelPitch = currentPitch();
        }
    }

    private float yuriStep() {
        return sourceDiagonal() || rawInput.jump() ? 117f : 86.4f;
    }

    private boolean sourceDiagonal() {
        float offset = Mth.positiveModulo(player.getYRot(), 90f);
        return offset > 20f && offset < 70f || rawInput.left() || rawInput.right();
    }

    private Aim watchdogAim(BlockPos neighbour, Direction face, float movement) {
        // Preserve Yuri's ordered search: +90 yaw, 0/-1/+1 ... +/-20, then +/-2 to 90.
        // Analytic face bounds cheaply reject impossible pitches before native KRS ray tests.
        for (int offset = 0; offset <= 90; offset += offset < 20 ? 1 : 2) {
            int count = offset == 0 ? 1 : 2;
            for (int sign = 0; sign < count; sign++) {
                float yaw = movement + 90f + (sign == 0 ? -offset : offset);
                double[] range = pitchRange(neighbour, face, yaw);
                if (range == null) continue;
                for (int pitch = 90; pitch >= 70; pitch--) {
                    if (pitch < range[0] - 0.4 || pitch > range[1] + 0.4) continue;
                    Aim candidate = normalized(new Aim(yaw, pitch));
                    if (hits(neighbour, face, candidate.yaw, candidate.pitch)) return candidate;
                }
            }
        }
        return null;
    }

    private void aimRush(BlockPos neighbour, Direction face, Vec3 hitPos, float movement) {
        if (hits(neighbour, face, currentYaw(), currentPitch())) {
            request(new Aim(currentYaw(), currentPitch()), 180f, 180f);
            rushWarmTarget = null;
            return;
        }
        Aim desired = chooseAim(neighbour, face, hitPos, rushLanePitch,
                new float[]{rushLaneYaw, currentYaw(), movement + 180, movement + 174, movement + 186});
        BlockPos target = neighbour.relative(face);
        if (!target.equals(rushWarmTarget)) {
            rushWarmTarget = target;
            rushWarmTick = tick;
        }
        // Lease a rotation for one tick before opening the front chain, as in the source.
        aimBlocked = tick <= rushWarmTick;
        rushLaneYaw = desired.yaw;
        rushLanePitch = desired.pitch;
        request(desired, 180f, 180f);
    }

    private void aimNinja(BlockPos neighbour, Direction face, Vec3 hitPos, boolean hasTarget) {
        if (!moving(rawInput)) {
            ninjaMask = -1;
            ninjaBaseYaw = ninjaAnchorYaw = ninjaYaw = Float.NaN;
            if (rotationOwned) Client.rotationManager.stopRotation();
            rotationOwned = false;
            return;
        }
        updateNinjaLane(rawInput);
        if (hasTarget && ninjaRecoveryCooldown == 0 && !hits(neighbour, face, ninjaYaw, ninjaPitch)) {
            ninjaRecoveryCooldown = 4;
            Aim best = null;
            double bestScore = Double.MAX_VALUE;
            float[] offsets = {0, -4, 4, -8, 8, -12, 12, -18, 18, -24, 24, -30, 30, -36, 36};
            for (float pitch : new float[]{ninjaPitch, 78, 82, 75, 85, 71}) {
                for (float offset : offsets) {
                    Aim candidate = normalized(new Aim(ninjaAnchorYaw + offset, pitch));
                    if (!hits(neighbour, face, candidate.yaw, candidate.pitch)) continue;
                    double score = Math.abs(offset) * 0.65 + Math.abs(pitch - ninjaPitch) * 1.5;
                    if (score < bestScore) { best = candidate; bestScore = score; }
                }
            }
            float[] direct = Client.rotationManager.getRotationsTo(hitPos.x, hitPos.y, hitPos.z);
            float offset = Math.abs(Mth.wrapDegrees(direct[0] - ninjaAnchorYaw));
            double score = offset * 0.65 + Math.abs(direct[1] - ninjaPitch) * 1.5;
            if (offset <= 36f && score < bestScore && hits(neighbour, face, direct[0], direct[1]))
                best = new Aim(direct[0], direct[1]);
            if (best != null) { ninjaYaw = best.yaw; ninjaPitch = best.pitch; }
        }
        request(new Aim(ninjaYaw, ninjaPitch), 24f, 180f);
    }

    private float preferredBridgeYaw(float movement) {
        float quadrant = Mth.positiveModulo(movement + 180f, 90f);
        if (quadrant > 8 && quadrant < 37) cardinalSide = 1;
        else if (quadrant > 53 && quadrant < 82) cardinalSide = -1;
        if (diagonalTravel) {
            if (quadrant < 16 || quadrant > 74) diagonalTravel = false;
        } else if (quadrant > 24 && quadrant < 66) diagonalTravel = true;
        float yaw = diagonalTravel ? movement - 180f : movement - 135f * cardinalSide;
        float camera = player.getYRot();
        return camera + Math.round(Mth.wrapDegrees(yaw - camera) / 45f) * 45f;
    }

    private Aim chooseAim(BlockPos neighbour, Direction face, Vec3 hitPos, float preferredPitch, float[] yaws) {
        for (float yaw : yaws) {
            if (!Float.isFinite(yaw)) continue;
            Aim candidate = fixedYawAim(neighbour, face, yaw, preferredPitch);
            if (candidate != null) return candidate;
        }
        // KRS's native search supplied this actual reachable hit point. It is the final fallback,
        // never a fake hit: the caller still checks the resulting server rotation before use.
        float[] direct = Client.rotationManager.getRotationsTo(hitPos.x, hitPos.y, hitPos.z);
        return new Aim(direct[0], direct[1]);
    }

    private Aim fixedYawAim(BlockPos neighbour, Direction face, float yaw, float preferredPitch) {
        Aim held = normalized(new Aim(yaw, preferredPitch));
        if (hits(neighbour, face, held.yaw, held.pitch)) return held;
        double[] range = pitchRange(neighbour, face, held.yaw);
        if (range == null) return null;
        double min = range[0];
        double max = range[1];
        double margin = Math.min(0.35, (max - min) * 0.25);
        double[] pitches = {Mth.clamp(preferredPitch, min + margin, max - margin),
                (min + max) * 0.5, min + (max - min) * 0.3, min + (max - min) * 0.7};
        for (double pitch : pitches) {
            Aim candidate = normalized(new Aim(held.yaw, (float) pitch));
            if (hits(neighbour, face, candidate.yaw, candidate.pitch)) return candidate;
        }
        return null;
    }

    /** Angular span of the requested full-block face at a fixed yaw; native rays validate it. */
    private double[] pitchRange(BlockPos block, Direction face, float yaw) {
        Vec3 eye = player.getEyePosition();
        double dx = -Math.sin(Math.toRadians(yaw));
        double dz = Math.cos(Math.toRadians(yaw));
        double reach = player.blockInteractionRange();
        if (face == Direction.UP) {
            double height = eye.y - block.getY() - 1;
            if (height <= 0.05) return null;
            double near = 0.001;
            double far = reach;
            double[] origins = {eye.x, eye.z};
            double[] directions = {dx, dz};
            int[] edges = {block.getX(), block.getZ()};
            for (int axis = 0; axis < 2; axis++) {
                if (Math.abs(directions[axis]) < 1e-6) {
                    if (origins[axis] < edges[axis] + 0.05 || origins[axis] > edges[axis] + 0.95) return null;
                } else {
                    double a = (edges[axis] + 0.05 - origins[axis]) / directions[axis];
                    double b = (edges[axis] + 0.95 - origins[axis]) / directions[axis];
                    near = Math.max(near, Math.min(a, b));
                    far = Math.min(far, Math.max(a, b));
                }
            }
            if (near >= far) return null;
            return new double[]{Math.toDegrees(Math.atan2(height, far)), Math.toDegrees(Math.atan2(height, near))};
        }
        if (face == Direction.DOWN) return null;
        boolean xFace = face.getStepX() != 0;
        int step = xFace ? face.getStepX() : face.getStepZ();
        double plane = (xFace ? block.getX() : block.getZ()) + (step > 0 ? 1 : 0);
        double origin = xFace ? eye.x : eye.z;
        double direction = xFace ? dx : dz;
        if ((origin - plane) * step <= 0 || Math.abs(direction) < 1e-6) return null;
        double distance = (plane - origin) / direction;
        double coordinate = (xFace ? eye.z : eye.x) + (xFace ? dz : dx) * distance;
        int edge = xFace ? block.getZ() : block.getX();
        if (distance <= 0 || distance > reach || coordinate < edge + 0.05 || coordinate > edge + 0.95) return null;
        double min = Math.toDegrees(Math.atan2(eye.y - block.getY() - 0.95, distance));
        double max = Math.toDegrees(Math.atan2(eye.y - block.getY() - 0.05, distance));
        return new double[]{min, max};
    }

    private boolean hits(BlockPos neighbour, Direction face, float yaw, float pitch) {
        if (neighbour == null || face == null || !Float.isFinite(yaw) || !Float.isFinite(pitch)) return false;
        Aim fixed = normalized(new Aim(yaw, pitch));
        return Client.rotationManager.canHitBlockAtRotation(neighbour, face, fixed.yaw, fixed.pitch);
    }

    private Aim normalized(Aim aim) {
        float[] fixed = Client.rotationManager.normalizeRotation(aim.yaw, aim.pitch, currentYaw(), currentPitch());
        return new Aim(fixed[0], fixed[1]);
    }

    private void request(Aim aim, float yawSpeed, float pitchSpeed) {
        if (aim == null || !Float.isFinite(aim.yaw) || !Float.isFinite(aim.pitch)) return;
        Client.rotationManager.startRotation(aim.yaw, Mth.clamp(aim.pitch, -90f, 90f), yawSpeed, pitchSpeed);
        rotationOwned = true;
        lastAimYaw = currentYaw();
    }

    private float currentYaw() {
        return Client.rotationManager != null ? Client.rotationManager.getRotationYaw() : player.getYRot();
    }

    private float currentPitch() {
        return Client.rotationManager != null ? Client.rotationManager.getRotationPitch() : player.getXRot();
    }

    private boolean snapGodBridge() {
        return moving(rawInput) && (rawInput.jump() || !player.onGround());
    }

    private boolean rawDiagonal() {
        return forward(rawInput) != 0 && left(rawInput) != 0;
    }

    private boolean motionAllowed() {
        return !player.getAbilities().flying && !player.isPassenger() && !player.isFallFlying()
                && !player.isInWater() && !player.isInLava();
    }

    private boolean headBlocked() {
        return !world.noCollision(player, player.getBoundingBox().move(0, 0.42, 0));
    }

    private boolean replaceable(BlockPos pos) {
        return pos != null && world != null && world.getBlockState(pos).canBeReplaced();
    }

    private boolean solid(BlockPos pos) {
        if (pos == null || world == null) return false;
        var state = world.getBlockState(pos);
        return !state.canBeReplaced() && !state.getCollisionShape(world, pos).isEmpty();
    }

    private boolean cornerSupport(double x, double z, int row) {
        return solid(BlockPos.containing(x - 0.30, row, z - 0.30))
                || solid(BlockPos.containing(x + 0.30, row, z - 0.30))
                || solid(BlockPos.containing(x - 0.30, row, z + 0.30))
                || solid(BlockPos.containing(x + 0.30, row, z + 0.30));
    }

    private boolean unsupportedAhead(double ticks) {
        Vec3 motion = player.getDeltaMovement();
        return !cornerSupport(player.getX() + motion.x * ticks, player.getZ() + motion.z * ticks, Mth.floor(player.getY()) - 1);
    }

    private Input physicalInput() {
        return new Input(keyDown(mc.options.keyUp), keyDown(mc.options.keyDown), keyDown(mc.options.keyLeft),
                keyDown(mc.options.keyRight), keyDown(mc.options.keyJump), keyDown(mc.options.keyShift), keyDown(mc.options.keySprint));
    }

    private boolean keyDown(KeyMapping mapping) {
        if (mapping == null || mapping.isUnbound()) return false;
        // Mouse-bound controls are maintained by vanilla; this helper never mutates KeyMapping.
        return mapping.key.getType() == InputConstants.Type.MOUSE ? mapping.isDown()
                : InputConstants.isKeyDown(mc.getWindow(), mapping.key.getValue());
    }

    private float movementYaw(Input input) {
        return inputYaw(player.getYRot(), forward(input), left(input));
    }

    private static float inputYaw(float camera, float forward, float left) {
        float yaw = camera + (forward < 0 ? 180f : 0f);
        if (left != 0) yaw -= 90f * Math.signum(left) * (forward == 0 ? 1f : 0.5f * Math.signum(forward));
        return yaw;
    }

    private static Vec3 direction(float yaw) {
        double radians = Math.toRadians(yaw);
        return new Vec3(-Math.sin(radians), 0, Math.cos(radians));
    }

    private static boolean moving(Input input) {
        return forward(input) != 0 || left(input) != 0;
    }

    private static int forward(Input input) {
        return (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
    }

    private static int left(Input input) {
        return (input.left() ? 1 : 0) - (input.right() ? 1 : 0);
    }

    private static Input directionalInput(Input input, float forward, float left) {
        return new Input(forward > 1e-4f, forward < -1e-4f, left > 1e-4f, left < -1e-4f, input.jump(), input.shift(), input.sprint());
    }

    private static Input flags(Input input, boolean jump, boolean shift, boolean sprint) {
        return new Input(input.forward(), input.backward(), input.left(), input.right(), jump, shift, sprint);
    }

    private static double horizontalSpeed(Vec3 motion) {
        return Math.hypot(motion.x, motion.z);
    }

    private static double horizontalDistanceSquared(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return dx * dx + dz * dz;
    }

    private static float random(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }
}
