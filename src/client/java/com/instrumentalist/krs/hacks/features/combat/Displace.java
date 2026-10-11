package com.instrumentalist.krs.hacks.features.combat;

import com.instrumentalist.krs.Client;
import com.instrumentalist.krs.events.features.MotionEvent;
import com.instrumentalist.krs.events.features.ReceivedPacketEvent;
import com.instrumentalist.krs.events.features.TickEvent;
import com.instrumentalist.krs.events.features.WorldEvent;
import com.instrumentalist.krs.hacks.Module;
import com.instrumentalist.krs.hacks.ModuleCategory;
import com.instrumentalist.krs.hacks.ModuleManager;
import com.instrumentalist.krs.utils.entity.EntityExtension;
import com.instrumentalist.krs.utils.math.BehaviorUtils;
import com.instrumentalist.krs.utils.move.InputUtil;
import com.instrumentalist.krs.utils.value.BooleanValue;
import com.instrumentalist.krs.utils.value.IntValue;
import com.instrumentalist.krs.utils.value.ListValue;
import com.instrumentalist.krs.utils.value.TextValue;
import com.mojang.blaze3d.platform.InputConstants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** Direct port of the supplied Raven Displace, with input-phase hooks for KRS. */
public final class Displace extends Module {
    private static final int VOID_SCAN_DIRECTIONS = 32;
    private static final int VOID_SCAN_RINGS = 12;
    private static final int VOID_SCAN_DEPTH = 10;
    private static final double VOID_SCAN_STEP = 0.5D;
    private static final double DYNAMIC_SCAN_STEP = 0.5D;
    private static final double DYNAMIC_SCAN_DISTANCE = 6.0D;
    private static final double DYNAMIC_SCAN_SIDE_STEP = 0.45D;
    private static final double DYNAMIC_WALL_CHECK_STEP = 0.25D;
    private static final double DYNAMIC_COLLISION_INSET = 0.03D;
    private static final double[] VOID_SCAN_X = new double[VOID_SCAN_DIRECTIONS];
    private static final double[] VOID_SCAN_Z = new double[VOID_SCAN_DIRECTIONS];

    @Setting
    private final ListValue dynamicAngle = new ListValue("Dynamic angle", new String[]{"Static", "Dynamic"}, "Static");
    @Setting
    private final IntValue yawOffset = new IntValue("Yaw offset", 90, 0, 180, () -> !isDynamicAngle());
    @Setting
    private final IntValue delay = new IntValue("Delay", 0, 0, 500, "ms") {
        @Override
        protected void changeValue(Integer value) {
            super.changeValue(Math.max(0, Math.min(500, Math.round(value / 50.0F) * 50)));
        }
    };
    @Setting
    private final ListValue direction = new ListValue("Direction", new String[]{"Left", "Right"}, "Left", () -> !isDynamicAngle());
    @Setting
    private final BooleanValue showDirection = new BooleanValue("Show direction", true);
    @Setting
    private final BooleanValue findVoid = new BooleanValue("Find void", false, () -> !isDynamicAngle());
    @Setting
    private final BooleanValue blink = new BooleanValue("Blink", false);
    @Setting
    private final BooleanValue ignoreTeammates = new BooleanValue("Ignore teammates", true);
    @Setting
    private final BooleanValue hasKnockback = new BooleanValue("Has knockback", false);
    @Setting
    private final BooleanValue itemWhitelistToggle = new BooleanValue("Item whitelist", false);
    @Setting
    private final TextValue itemWhitelist = new TextValue("Whitelisted items", "", itemWhitelistToggle::get);

    private boolean displaceThisTick;
    private boolean active;
    private boolean hasKB;
    private boolean compensateNextTick;
    private boolean displaceLeft;
    private boolean wasDisplacingLastTick;
    private Float dynamicVoidYaw;
    private Float renderDisplaceYaw;
    private Player renderTarget;
    private Float fadingDisplaceYaw;
    private Player fadingTarget;
    private long arrowFadeStartMs;
    private Float lastRenderedDisplaceYaw;
    private Player lastRenderedTarget;
    private long lastRenderedArrowMs;
    private int tickCounter;
    private final Map<Integer, Integer> targetWindowStartTicks = new HashMap<>();
    private final DisplaceBlinkBuffer outboundBlink = new DisplaceBlinkBuffer();
    private ClientLevel stateWorld;
    private LocalPlayer statePlayer;
    private boolean rotationPreparedThisTick;
    private boolean postInputAppliedThisTick;
    private boolean gameTickHookHandled;
    private volatile Float preparedYaw;
    private volatile boolean canStartBlink;
    private String cachedWhitelistText;
    private final Set<Identifier> cachedWhitelist = new HashSet<>();

    static {
        for (int i = 0; i < VOID_SCAN_DIRECTIONS; i++) {
            double angle = Math.PI * 2.0D * (double) i / (double) VOID_SCAN_DIRECTIONS;
            VOID_SCAN_X[i] = Math.cos(angle);
            VOID_SCAN_Z[i] = Math.sin(angle);
        }
    }

    public Displace() {
        super("Displace", ModuleCategory.Combat, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Override
    public String tag() {
        return delay.get() + "ms";
    }

    @Override
    public String description() {
        return "Alternates displacement and compensation. Whitelisted items: comma/space-separated registry IDs.";
    }

    @Override
    public void onEnable() {
        outboundBlink.release();
        resetState();
        stateWorld = mc.level;
        statePlayer = mc.player;
    }

    @Override
    public void onDisable() {
        canStartBlink = false;
        outboundBlink.release();
        resetState();
    }

    @Override
    public void onWorld(WorldEvent event) {
        outboundBlink.discard();
        resetState();
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (event.packet instanceof ClientboundDisconnectPacket
                || event.packet instanceof ClientboundStartConfigurationPacket
                || event.packet instanceof ClientboundRespawnPacket) {
            outboundBlink.discard();
            resetState();
        }
    }

    private void resetState() {
        displaceThisTick = false;
        active = false;
        hasKB = false;
        compensateNextTick = false;
        wasDisplacingLastTick = false;
        dynamicVoidYaw = null;
        renderDisplaceYaw = null;
        renderTarget = null;
        preparedYaw = null;
        canStartBlink = false;
        rotationPreparedThisTick = false;
        postInputAppliedThisTick = false;
        gameTickHookHandled = false;
        clearArrowState();
        tickCounter = 0;
        targetWindowStartTicks.clear();
        stateWorld = null;
        statePlayer = null;
    }

    /** Call immediately before dispatching KRS TickEvent to emulate source HIGHEST priority. */
    public static void beforeGameTick() {
        Displace module = enabledModule();
        if (module != null) {
            module.gameTick();
            module.gameTickHookHandled = true;
        }
    }

    @Override
    public void onTick(TickEvent event) {
        // Fallback for callers dispatching TickEvent directly; never release twice in one tick.
        if (gameTickHookHandled) {
            gameTickHookHandled = false;
        } else {
            gameTick();
        }
    }

    private void gameTick() {
        // Exactly the next game tick, independent of UpdateEvent, FPS, target and movement input.
        outboundBlink.release();
        preparedYaw = null;
        canStartBlink = false;
        rotationPreparedThisTick = false;
        postInputAppliedThisTick = false;
        checkWorldIdentity();
    }

    private boolean checkWorldIdentity() {
        if (stateWorld != mc.level || statePlayer != mc.player) {
            outboundBlink.discard();
            resetState();
            stateWorld = mc.level;
            statePlayer = mc.player;
        }
        return mc.player != null && mc.level != null;
    }

    /**
     * Call once after other rotation producers, before MovementFix.updateInput in KeyboardInput.tick.
     * This is the source ClientRotationEvent phase; onMotion must not advance the state machine.
     */
    public static void beforeMovementCorrection(boolean inputBlocked) {
        Displace module = enabledModule();
        if (module != null)
            module.prepareClientRotation(inputBlocked);
    }

    public static boolean shouldForceMovementFix() {
        Displace module = enabledModule();
        return module != null && module.preparedYaw != null
                && mc.player != null && mc.player == module.statePlayer && mc.level == module.stateWorld;
    }

    /** MovementFix uses this for both its correction target and movement/jump yaw. */
    public static float movementYaw(float fallback) {
        Displace module = enabledModule();
        return module != null && shouldForceMovementFix() ? module.preparedYaw : fallback;
    }

    /** Call after MotionEvent dispatch, before packet rotation normalization (source LOWEST). */
    public static void applyMotionRotation(MotionEvent event) {
        if (event != null && !event.post && shouldForceMovementFix())
            event.yaw = movementYaw(event.yaw);
    }

    private void prepareClientRotation(boolean inputBlocked) {
        if (!checkWorldIdentity()) {
            clearActiveState();
            return;
        }
        if (rotationPreparedThisTick)
            return;
        rotationPreparedThisTick = true;
        preparedYaw = null;
        canStartBlink = false;

        tickCounter++;
        int currentTick = tickCounter;
        pruneTargetDelayStates();
        boolean hasKBEnchant = hasKnockbackEnchantment();
        boolean passesItemCondition = DisplaceMath.passesItemCondition(
                hasKnockback.get(), itemWhitelistToggle.get(), hasKBEnchant,
                itemWhitelistToggle.get() && matchesWhitelist(mc.player.getMainHandItem())
        );
        if (inputBlocked || !passesItemCondition) {
            clearActiveState();
            return;
        }

        Player target = null;
        boolean attacking = mc.options.keyAttack.isDown()
                || ModuleManager.getModuleState(KillAura.class) && KillAura.closestEntity != null;
        if (attacking)
            target = findClosestTarget(9.0D);

        active = target != null && (hasKBEnchant || anyMovementKey());
        if (!active) {
            clearActiveState();
            return;
        }

        dynamicVoidYaw = isDynamicAngle()
                ? findDynamicVoidYaw(target)
                : findVoid.get() ? findStaticVoidYaw(target) : null;
        if (dynamicVoidYaw == null && !isDynamicAngle())
            displaceLeft = direction.get().equalsIgnoreCase("Left");
        renderDisplaceYaw = dynamicVoidYaw != null ? dynamicVoidYaw : isDynamicAngle() ? null : getFixedDisplaceYaw();
        renderTarget = renderDisplaceYaw != null ? target : null;
        if (renderDisplaceYaw == null) {
            clearActiveState();
            return;
        }

        hasKB = hasKBEnchant;
        displaceThisTick = !displaceThisTick;
        if (displaceThisTick && !DisplaceMath.shouldDisplaceInCurrentWindow(
                targetWindowStartTicks, target.getId(), currentTick, delay.get())) {
            startArrowFade();
            displaceThisTick = false;
            compensateNextTick = false;
            wasDisplacingLastTick = false;
            dynamicVoidYaw = null;
            renderDisplaceYaw = null;
            renderTarget = null;
            return;
        }

        if (!displaceThisTick && wasDisplacingLastTick && !mc.options.keyAttack.isUnbound()) {
            // Queue one click, not a held attack state or a direct attack packet.
            KeyMapping.click(InputConstants.getKey(mc.options.keyAttack.saveString()));
        }
        wasDisplacingLastTick = displaceThisTick;
        if (!displaceThisTick || renderDisplaceYaw == null)
            return;

        // Tick-local override leaves the other rotation producers and the user's Movement Fix
        // setting intact, and naturally restores their yaw on the compensation tick.
        preparedYaw = renderDisplaceYaw;
        canStartBlink = blink.get();
    }

    /** Call last, after all KRS input correction/Scaffold/WTap hooks. */
    public static void afterMovementCorrection(ClientInput input) {
        Displace module = enabledModule();
        if (module != null)
            module.postInput(input);
    }

    private void postInput(ClientInput input) {
        if (input == null || !rotationPreparedThisTick || postInputAppliedThisTick)
            return;
        postInputAppliedThisTick = true;
        if (!active) {
            compensateNextTick = false;
            return;
        }
        if (compensateNextTick && !displaceThisTick) {
            compensateNextTick = false;
            setInputAxes(input, displaceLeft ? -1.0F : 1.0F, InputUtil.forwardImpulse(input));
            return;
        }
        if (!displaceThisTick || hasKB || !anyMovementKey())
            return;

        setInputAxes(input, InputUtil.leftImpulse(input), 1.0F);
        compensateNextTick = true;
    }

    private static void setInputAxes(ClientInput input, float left, float forward) {
        Input previous = input.keyPresses;
        input.keyPresses = new Input(forward > 0.0F, forward < 0.0F, left > 0.0F, left < 0.0F,
                previous.jump(), previous.shift(), previous.sprint());
        // The source writes individual scalar impulses. Do not normalize them again here.
        InputUtil.setMoveVector(input, new Vec2(left, forward));
    }

    private boolean anyMovementKey() {
        return mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
    }

    private boolean isDynamicAngle() {
        return dynamicAngle.get().equalsIgnoreCase("Dynamic");
    }

    private boolean hasKnockbackEnchantment() {
        return mc.level.registryAccess().lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(Enchantments.KNOCKBACK))
                .map(enchantment -> EnchantmentHelper.getItemEnchantmentLevel(enchantment, mc.player.getMainHandItem()) > 0)
                .orElse(false);
    }

    private boolean matchesWhitelist(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        String configured = itemWhitelist.get();
        if (!java.util.Objects.equals(configured, cachedWhitelistText)) {
            cachedWhitelistText = configured;
            cachedWhitelist.clear();
            if (configured != null) {
                for (String token : configured.split("[,;\\s]+")) {
                    Identifier id = Identifier.tryParse(token);
                    if (id != null && BuiltInRegistries.ITEM.containsKey(id))
                        cachedWhitelist.add(id);
                }
            }
        }
        return cachedWhitelist.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /**
     * Raven CombatTargeting was not supplied. Mapping follows native KRS KillAura eligibility:
     * loaded, non-local, alive players; KRS eye-to-box distance <= range; AntiBot when enabled;
     * Teams when enabled and Ignore teammates is true. No new visibility/FOV/invisibility gate
     * and no KillAura preferred-name priority. Raven distance/tie/filter details are unverifiable.
     */
    private Player findClosestTarget(double range) {
        boolean antiBotEnabled = ModuleManager.getModuleState(AntiBot.class);
        boolean teamsEnabled = ignoreTeammates.get() && ModuleManager.getModuleState(Teams.class);
        Player closest = null;
        double closestDistance = range * range;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player) || player instanceof LocalPlayer
                    || player.isRemoved() || player.isDeadOrDying() || player.deathTime != 0
                    || BehaviorUtils.isBot(player, antiBotEnabled)
                    || BehaviorUtils.isTeammate(player, teamsEnabled))
                continue;
            double distance = EntityExtension.squaredBoxedDistanceTo(player, mc.player);
            if (distance > closestDistance || closest != null && distance == closestDistance)
                continue;
            closest = player;
            closestDistance = distance;
        }
        return closest;
    }

    private void pruneTargetDelayStates() {
        Iterator<Map.Entry<Integer, Integer>> iterator = targetWindowStartTicks.entrySet().iterator();
        while (iterator.hasNext()) {
            Entity entity = mc.level.getEntity(iterator.next().getKey());
            if (!(entity instanceof Player player) || player.isRemoved() || player.isDeadOrDying() || player.deathTime != 0)
                iterator.remove();
        }
    }

    private Float findStaticVoidYaw(Player target) {
        if (target == null || mc.player == null || mc.level == null)
            return null;
        double bestX = 0.0D;
        double bestZ = 0.0D;
        double bestScore = Double.MAX_VALUE;
        for (int ring = 1; ring <= VOID_SCAN_RINGS; ring++) {
            double radius = (double) ring * VOID_SCAN_STEP;
            boolean foundInRing = false;
            for (int i = 0; i < VOID_SCAN_DIRECTIONS; i++) {
                double x = target.getX() + VOID_SCAN_X[i] * radius;
                double z = target.getZ() + VOID_SCAN_Z[i] * radius;
                if (!isVoidColumn(x, target.getY(), z))
                    continue;
                double score = DisplaceMath.staticVoidScore(radius, x - mc.player.getX(), z - mc.player.getZ());
                if (score < bestScore) {
                    bestScore = score;
                    bestX = x;
                    bestZ = z;
                    foundInRing = true;
                }
            }
            if (foundInRing)
                break;
        }
        if (bestScore == Double.MAX_VALUE)
            return null;

        updateDisplaceSide(target, bestX, bestZ);
        double dx = bestX - target.getX();
        double dz = bestZ - target.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.001D)
            return null;
        double aimRadius = Math.min(dist, Math.max(0.35D, (double) target.getBbWidth() * 0.5D + 0.15D));
        double aimX = target.getX() + dx / dist * aimRadius;
        double aimZ = target.getZ() + dz / dist * aimRadius;
        Vec3 eyes = mc.player.getEyePosition();
        return DisplaceMath.yawFromForward(aimX - eyes.x, aimZ - eyes.z);
    }

    private Float findDynamicVoidYaw(Player target) {
        if (target == null || mc.player == null || mc.level == null)
            return null;
        double bestForwardX = 0.0D;
        double bestForwardZ = 0.0D;
        double bestScore = 0.0D;
        for (int i = 0; i < VOID_SCAN_DIRECTIONS; i++) {
            double forwardX = VOID_SCAN_X[i];
            double forwardZ = VOID_SCAN_Z[i];
            double score = scoreVoidPath(target, forwardX, forwardZ);
            if (score > bestScore) {
                bestScore = score;
                bestForwardX = forwardX;
                bestForwardZ = forwardZ;
            }
        }
        if (bestScore <= 0.0D)
            return null;

        updateDisplaceSide(target, target.getX() + bestForwardX, target.getZ() + bestForwardZ);
        return DisplaceMath.yawFromForward(bestForwardX, bestForwardZ);
    }

    private double scoreVoidPath(Player target, double forwardX, double forwardZ) {
        double sideX = -forwardZ;
        double sideZ = forwardX;
        double score = 0.0D;
        double checkedForward = 0.0D;
        int consecutiveCenterVoid = 0;
        AABB baseCollisionBox = target.getBoundingBox().deflate(DYNAMIC_COLLISION_INSET, 0.0D, DYNAMIC_COLLISION_INSET);
        for (int step = 1; step <= (int) (DYNAMIC_SCAN_DISTANCE / DYNAMIC_SCAN_STEP); step++) {
            double forward = (double) step * DYNAMIC_SCAN_STEP;
            if (!isDynamicPathClear(target, baseCollisionBox, forwardX, forwardZ, checkedForward, forward))
                break;
            checkedForward = forward;
            boolean centerVoid = false;
            for (int side = -1; side <= 1; side++) {
                double sideOffset = (double) side * DYNAMIC_SCAN_SIDE_STEP;
                double x = target.getX() + forwardX * forward + sideX * sideOffset;
                double z = target.getZ() + forwardZ * forward + sideZ * sideOffset;
                if (isVoidColumn(x, target.getY(), z)) {
                    score += DisplaceMath.dynamicLaneScore(side, forward);
                    centerVoid |= side == 0;
                }
            }
            if (centerVoid) {
                consecutiveCenterVoid++;
                score += consecutiveCenterVoid * 2.0D;
            } else {
                consecutiveCenterVoid = 0;
            }
        }
        return score;
    }

    private boolean isDynamicPathClear(Player target, AABB baseCollisionBox, double forwardX, double forwardZ,
                                       double fromForward, double toForward) {
        for (double forward = fromForward + DYNAMIC_WALL_CHECK_STEP;
             forward <= toForward + 1.0E-4D; forward += DYNAMIC_WALL_CHECK_STEP) {
            AABB box = baseCollisionBox.move(forwardX * forward, 0.0D, forwardZ * forward);
            if (hasBlockCollision(target, box))
                return false;
        }
        return true;
    }

    private boolean hasBlockCollision(Player target, AABB box) {
        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX + 1.0D);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.floor(box.maxY + 1.0D);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ + 1.0D);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        CollisionContext context = CollisionContext.of(target);
        VoxelShape testShape = Shapes.create(box);
        for (int blockX = minX; blockX < maxX; blockX++) {
            for (int blockZ = minZ; blockZ < maxZ; blockZ++) {
                if (!mc.level.hasChunkAt(pos.set(blockX, mc.level.getMinY(), blockZ)))
                    return true;
                for (int blockY = minY; blockY < maxY; blockY++) {
                    // In 26.2 getMaxY() is INCLUSIVE. isOutsideBuildHeight handles both ends.
                    if (mc.level.isOutsideBuildHeight(blockY))
                        return true;
                    pos.set(blockX, blockY, blockZ);
                    VoxelShape shape = mc.level.getBlockState(pos).getCollisionShape(mc.level, pos, context);
                    if (!shape.isEmpty() && Shapes.joinIsNotEmpty(testShape,
                            shape.move(blockX, blockY, blockZ), BooleanOp.AND))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean isVoidColumn(double x, double y, double z) {
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        int startY = Mth.floor(y) - 1;
        int endY = Math.max(mc.level.getMinY(), startY - VOID_SCAN_DEPTH);
        // Preserve the inclusive source scan (up to eleven air blocks), including its empty
        // scan below the minimum height. Above-world blocks remain air as in the source.
        for (int blockY = Math.min(startY, mc.level.getMaxY()); blockY >= endY; blockY--) {
            if (!mc.level.getBlockState(new BlockPos(blockX, blockY, blockZ)).isAir())
                return false;
        }
        return true;
    }

    private void updateDisplaceSide(Player target, double voidX, double voidZ) {
        displaceLeft = DisplaceMath.isDisplaceLeft(mc.player.getX(), mc.player.getZ(),
                target.getX(), target.getZ(), voidX, voidZ);
    }

    private float getFixedDisplaceYaw() {
        // Raven's optional serverRotations maps to KRS's current silent-rotation request.
        float baseYaw = Client.rotationManager != null && Client.rotationManager.isRotating()
                ? Client.rotationManager.getRotationYaw() : mc.player.getYRot();
        return DisplaceMath.fixedDisplaceYaw(baseYaw, yawOffset.get(), displaceLeft);
    }

    private void clearActiveState() {
        startArrowFade();
        active = false;
        displaceThisTick = false;
        compensateNextTick = false;
        wasDisplacingLastTick = false;
        dynamicVoidYaw = null;
        renderDisplaceYaw = null;
        renderTarget = null;
        preparedYaw = null;
        canStartBlink = false;
    }

    private void clearFadingArrow() {
        fadingDisplaceYaw = null;
        fadingTarget = null;
        arrowFadeStartMs = 0L;
    }

    private void clearArrowState() {
        clearFadingArrow();
        lastRenderedDisplaceYaw = null;
        lastRenderedTarget = null;
        lastRenderedArrowMs = 0L;
    }

    private void startArrowFade() {
        long nowMs = System.currentTimeMillis();
        if (lastRenderedDisplaceYaw != null && lastRenderedTarget != null && !lastRenderedTarget.isRemoved()
                && nowMs - lastRenderedArrowMs <= DisplaceMath.ARROW_FADE_MS) {
            fadingDisplaceYaw = lastRenderedDisplaceYaw;
            fadingTarget = lastRenderedTarget;
            arrowFadeStartMs = nowMs;
        }
        lastRenderedDisplaceYaw = null;
        lastRenderedTarget = null;
        lastRenderedArrowMs = 0L;
    }

    /** Called by DisplaceLevelRendererMixin while features can still be submitted this frame. */
    public static void submitDirectionArrow(SubmitNodeCollector submitter, CameraRenderState camera, float partialTicks) {
        Displace module = enabledModule();
        if (module != null)
            module.renderDirectionArrow(submitter, camera, partialTicks);
    }

    private void renderDirectionArrow(SubmitNodeCollector submitter, CameraRenderState camera, float partialTicks) {
        if (mc.player == null || mc.level == null || !showDirection.get()
                || stateWorld != mc.level || statePlayer != mc.player) {
            clearArrowState();
            return;
        }
        long nowMs = System.currentTimeMillis();
        boolean activeArrow = active && renderDisplaceYaw != null && renderTarget != null && !renderTarget.isRemoved();
        Float arrowYaw = renderDisplaceYaw;
        Player arrowTarget = renderTarget;
        float alpha = 1.0F;
        if (activeArrow) {
            clearFadingArrow();
        } else {
            if (fadingDisplaceYaw == null || fadingTarget == null || fadingTarget.isRemoved()) {
                clearFadingArrow();
                return;
            }
            long elapsedMs = nowMs - arrowFadeStartMs;
            if (elapsedMs >= DisplaceMath.ARROW_FADE_MS) {
                clearFadingArrow();
                return;
            }
            arrowYaw = fadingDisplaceYaw;
            arrowTarget = fadingTarget;
            alpha = DisplaceMath.arrowFadeAlpha(elapsedMs);
        }

        Vec3 position = arrowTarget.getPosition(partialTicks);
        DisplaceMath.ArrowGeometry geometry = DisplaceMath.arrowGeometry(position.x,
                position.y + (double) arrowTarget.getBbHeight() * 0.5D, position.z,
                arrowTarget.getBbWidth(), arrowYaw);
        DisplaceArrowRenderer.submit(submitter, camera, geometry, alpha);
        if (activeArrow) {
            lastRenderedDisplaceYaw = arrowYaw;
            lastRenderedTarget = arrowTarget;
            lastRenderedArrowMs = nowMs;
        }
    }

    /**
     * Call before SendPacketEvent dispatch, after PacketUtil.consumeSilentPacket's early return.
     * Source HIGH priority starts the hold on the movement packet before other buffers consume it.
     * Final capture remains after cancellation/modification, so no packet is queued by two owners.
     */
    public static void beforeSendPacket(Connection connection, Packet<?> packet) {
        Displace module = enabledModule();
        if (module != null)
            module.outboundBlink.arm(connection, packet, module.mayStartBlink());
    }

    private boolean mayStartBlink() {
        return canStartBlink && blink.get() && stateWorld == mc.level && statePlayer == mc.player;
    }

    /** Final outbound hook: after KRS cancellation/modification, before Connection sends anything. */
    public static boolean captureOutboundPacket(Connection connection, Channel channel, Packet<?> packet,
                                                ChannelFutureListener listener, boolean flush) {
        Displace module = enabledModule();
        return module != null && module.outboundBlink.capture(connection, channel, packet, listener, flush,
                module.mayStartBlink());
    }

    public static void connectionClosed(Connection connection) {
        Displace module = ModuleManager.getModule(Displace.class);
        if (module != null)
            module.outboundBlink.discardConnection(connection);
    }

    /** Call at the start of the existing setLevel hook, before dispatching WorldEvent. */
    public static void beforeWorldChange() {
        Displace module = ModuleManager.getModule(Displace.class);
        if (module != null) {
            module.outboundBlink.discard();
            module.resetState();
        }
    }

    private static Displace enabledModule() {
        Displace module = ModuleManager.getModule(Displace.class);
        return module != null && module.tempEnabled ? module : null;
    }
}
