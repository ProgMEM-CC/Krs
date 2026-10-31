package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.Client;
import com.instrumentalist.krs.events.features.KeyboardEvent;
import com.instrumentalist.krs.events.features.MouseClickEvent;
import com.instrumentalist.krs.events.features.RenderHudEvent;
import com.instrumentalist.krs.events.features.UpdateEvent;
import com.instrumentalist.krs.events.features.WorldEvent;
import com.instrumentalist.krs.hacks.Module;
import com.instrumentalist.krs.hacks.ModuleCategory;
import com.instrumentalist.krs.utils.nanovg.NanoVGManager;
import com.instrumentalist.krs.utils.render.RenderUtil;
import com.instrumentalist.krs.utils.value.BooleanValue;
import com.instrumentalist.krs.utils.value.IntValue;
import com.instrumentalist.krs.utils.value.KeyBindValue;
import com.instrumentalist.krs.utils.value.TextValue;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CartographyTableBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LoomBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.nvgu.NVGU;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Native KRS port of Raven's Block In module.
 *
 * <p>The search and rotation ordering intentionally follows the supplied Raven source. KRS
 * supplies the event, rotation, ray-trace and placement primitives; the only setting that has
 * no native equivalent is Raven's item-list widget, represented here by a text setting containing
 * comma/space-separated item ids or paths.</p>
 */
public class BlockIn extends Module {
    private static final Direction[] HORIZONTALS = {
            Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH
    };

    private static final SupportOffset[] SUPPORTS = {
            new SupportOffset(0, 1, 0, Direction.DOWN),
            new SupportOffset(0, -1, 0, Direction.UP),
            new SupportOffset(0, 0, -1, Direction.NORTH),
            new SupportOffset(0, 0, 1, Direction.SOUTH),
            new SupportOffset(1, 0, 0, Direction.EAST),
            new SupportOffset(-1, 0, 0, Direction.WEST),
    };

    private static final double REACH = 4.5D;
    private static final double GRID_INSET = 0.05D;
    private static final double GRID_STEP = 0.2D;
    private static final int GRID_N = (int) Math.round(1.0D / GRID_STEP);

    @Setting
    private final IntValue speed = new IntValue("Speed", 10, 1, 30);

    @Setting
    private final IntValue randomization = new IntValue("Randomization", 10, 0, 100);

    @Setting
    private final IntValue rotationTolerance = new IntValue("Rotation Tolerance", 25, 20, 100, "°", () -> true);

    @Setting
    private final KeyBindValue selectKeybind = new KeyBindValue("Select Keybind", 0);

    @Setting
    private final BooleanValue ignoreBlocksToggle = new BooleanValue("Ignore blocks", false);

    /** Raven's ItemListSetting equivalent: ids/paths separated by commas or whitespace. */
    @Setting
    private final TextValue ignoredBlocks = new TextValue("Items", "", ignoreBlocksToggle::get);

    private boolean placing;
    private boolean slotWasSwapped;
    private int previousSlot = -1;
    private int plannedSlot = -1;
    private boolean placeQueued;
    private boolean selectKeyDown;

    private BlockPos targetHitPos;
    private Direction targetSide;
    private float aimYaw;
    private float aimPitch;

    private BlockPos hitAt;
    private Direction hitSide;
    private Vec3 placeAt;

    private float fillCount;
    private float lastFillCount = -1.0F;
    private float circleProgress;
    private float animationStartProgress;
    private float animationTargetProgress;
    private long animationStartTime;

    private boolean lastTargetAdjacent;

    public BlockIn() {
        super("Block In", ModuleCategory.Player, GLFW.GLFW_KEY_UNKNOWN, false, true);
    }

    @Override
    public void onEnable() {
        selectKeyDown = false;
        placeQueued = false;
        clearAim();
    }

    @Override
    public void onDisable() {
        disablePlacing();
        selectKeyDown = false;
        placeQueued = false;
        clearAim();
        hitAt = null;
        hitSide = null;
        placeAt = null;
        fillCount = 0.0F;
        lastFillCount = -1.0F;
        circleProgress = 0.0F;
        if (Client.rotationManager != null)
            Client.rotationManager.stopRotation();
    }

    @Override
    public void onWorld(WorldEvent event) {
        disablePlacing();
        placeQueued = false;
        clearAim();
        hitAt = null;
        hitSide = null;
        placeAt = null;
        fillCount = 0.0F;
        lastFillCount = -1.0F;
        circleProgress = 0.0F;
        if (Client.rotationManager != null)
            Client.rotationManager.stopRotation();
    }

    @Override
    public void onKey(KeyboardEvent event) {
        if (event == null || event.key != selectKeybind.get())
            return;

        if (event.action == GLFW.GLFW_PRESS || event.action == GLFW.GLFW_REPEAT)
            selectKeyDown = true;
        else if (event.action == GLFW.GLFW_RELEASE)
            selectKeyDown = false;
    }

    @Override
    public void onMouseClick(MouseClickEvent event) {
        if (placing && event != null && event.button >= 0)
            event.cancel();
    }

    @Override
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.level == null) {
            disablePlacing();
            placeQueued = false;
            return;
        }

        processQueuedPlacement();
        updateFillCount();
        runTargetSelection();
    }

    @Override
    public void onRenderHud(RenderHudEvent event) {
        if (mc.player == null || mc.level == null || fillCount <= 0.0F)
            return;
        if (!RenderUtil.shouldRenderWorldHudOverlays())
            return;

        long elapsed = System.currentTimeMillis() - animationStartTime;
        if (elapsed < 50L) {
            float t = Math.max(0.0F, Math.min(1.0F, elapsed / 50.0F));
            circleProgress = lerp(animationStartProgress, animationTargetProgress, quadInOutEasing(t));
        } else {
            circleProgress = animationTargetProgress;
        }

        if (Client.nanoVgManager == null)
            return;

        final float progress = circleProgress;
        Client.nanoVgManager.load(vg -> drawProgressCircle(vg, progress));
    }

    private void drawProgressCircle(NVGU vg, float progress) {
        float width = NanoVGManager.getScaledScreenWidth();
        float height = NanoVGManager.getScaledScreenHeight();
        if (width <= 1.0F || height <= 1.0F)
            return;

        float cx = width * 0.5F - 1.0F;
        float cy = height * 0.5F;
        float radius = 10.0F;
        float thickness = 3.0F;

        vg.circleBorder(cx, cy, radius, thickness, new Color(0, 0, 0, 128));
        if (progress >= 0.999F) {
            vg.circleBorder(cx, cy, radius, thickness, Color.GREEN);
            return;
        }

        float ratio = Math.max(0.0F, Math.min(1.0F, progress));
        int red = (int) ((1.0F - ratio) * 255.0F + 0.5F);
        int green = (int) (ratio * 255.0F + 0.5F);
        Color color = new Color(red, green, 0, 255);

        // NVGU exposes stroked lines and circles, rather than a legacy immediate-mode arc.
        // A short polyline preserves the Raven arc's start angle and clockwise progress.
        int segments = Math.max(1, (int) Math.ceil(ratio * 100.0F));
        double start = Math.toRadians(90.0D);
        double end = start + ratio * Math.PI * 2.0D;
        double previousX = cx + Math.cos(start) * radius;
        double previousY = cy - Math.sin(start) * radius;
        for (int i = 1; i <= segments; i++) {
            double angle = start + (end - start) * i / segments;
            double x = cx + Math.cos(angle) * radius;
            double y = cy - Math.sin(angle) * radius;
            vg.line((float) previousX, (float) previousY, (float) x, (float) y, thickness, color);
            previousX = x;
            previousY = y;
        }
    }

    private void runTargetSelection() {
        clearAim();

        if (!isSelectKeyPressed() || mc.gui.screen() != null) {
            disablePlacing();
            circleProgress = 0.0F;
            return;
        }

        int strongSlot = pickBlockSlot(true);
        int weakSlot = pickBlockSlot(false);
        if (strongSlot == -1 && weakSlot == -1) {
            disablePlacing();
            return;
        }

        plannedSlot = strongSlot != -1 ? strongSlot : weakSlot;
        if (!getTarget()) {
            disablePlacing();
            return;
        }

        plannedSlot = lastTargetAdjacent
                ? (strongSlot != -1 ? strongSlot : weakSlot)
                : (weakSlot != -1 ? weakSlot : strongSlot);

        if (!placing)
            enablePlacing();

        if (isPhysicalKeyDown(mc.options.keyAttack) || isPhysicalKeyDown(mc.options.keyUse))
            clearAim();
        suppressUseAndAttackKeys();
        equipPlannedSlot();
        updateRotationAndQueuePlacement();
    }

    public boolean isPlacing() {
        return placing;
    }

    private void processQueuedPlacement() {
        if (!placeQueued)
            return;

        placeQueued = false;
        if (hitAt == null || hitSide == null || placeAt == null || mc.gameMode == null || mc.player == null)
            return;

        BlockState support = mc.level.getBlockState(hitAt);
        BlockPos goal = hitAt.relative(hitSide);
        if (support.canBeReplaced() || !isReplaceable(goal))
            return;

        ItemStack held = mc.player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem) || !canPlaceBlockOnSide(held, hitAt, hitSide))
            return;

        BlockHitResult hit = new BlockHitResult(placeAt, hitSide, hitAt, false);
        if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit).consumesAction())
            mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private void updateFillCount() {
        if (!isSelectKeyPressed() || mc.gui.screen() != null) {
            fillCount = 0.0F;
            return;
        }

        BlockPos feet = mc.player.blockPosition();
        float count = 0.0F;
        if (!isReplaceable(feet.above(2)))
            count++;

        for (Direction direction : HORIZONTALS) {
            BlockPos side = feet.relative(direction);
            if (!isReplaceable(side))
                count++;
            if (!isReplaceable(side.above()))
                count++;
        }

        fillCount = count;
        if (fillCount != lastFillCount) {
            animationStartProgress = circleProgress;
            animationTargetProgress = Math.max(0.0F, Math.min(1.0F, fillCount / 9.0F));
            animationStartTime = System.currentTimeMillis();
            lastFillCount = fillCount;
        }
    }

    private void updateRotationAndQueuePlacement() {
        if (Client.rotationManager == null || targetHitPos == null)
            return;

        float baseYaw = Client.rotationManager.getRotationYaw();
        float basePitch = Client.rotationManager.getRotationPitch();
        float[] smoothed = smoothRotation(
                baseYaw,
                basePitch,
                aimYaw,
                aimPitch,
                speed.get(),
                randomization.get()
        );

        // The source uses a custom rotation event. KRS's source entry point performs the same
        // mouse-sensitivity bypass used by the Raven scaffold adapter and owns the next packet.
        Client.rotationManager.startSourceRotation(smoothed[0], smoothed[1]);

        BlockHitResult hit = Client.rotationManager.rayTraceBlocks(smoothed[0], smoothed[1], REACH);
        if (hit == null || hit.getType() != HitResult.Type.BLOCK)
            return;
        if (!hit.getBlockPos().equals(targetHitPos) || hit.getDirection() != targetSide)
            return;

        float yawDifference = Math.abs(Mth.wrapDegrees(smoothed[0] - baseYaw));
        float pitchDifference = Math.abs(smoothed[1] - basePitch);
        if (yawDifference <= rotationTolerance.get() && pitchDifference <= rotationTolerance.get()) {
            hitAt = hit.getBlockPos();
            hitSide = hit.getDirection();
            placeAt = hit.getLocation();
            placeQueued = true;
        }
    }

    private boolean isSelectKeyPressed() {
        if (selectKeybind.get() < 0)
            return selectKeyDown;
        if (selectKeyDown)
            return true;
        return InputConstants.isKeyDown(mc.getWindow(), selectKeybind.get());
    }

    private void suppressUseAndAttackKeys() {
        mc.options.keyAttack.setDown(false);
        mc.options.keyUse.setDown(false);
    }

    private void enablePlacing() {
        if (placing)
            return;

        placing = true;
        slotWasSwapped = false;
        previousSlot = mc.player.getInventory().getSelectedSlot();
    }

    private void disablePlacing() {
        if (!placing)
            return;

        if (slotWasSwapped && previousSlot >= 0 && previousSlot <= 8
                && mc.player != null && mc.player.getInventory().getSelectedSlot() != previousSlot) {
            mc.player.getInventory().setSelectedSlot(previousSlot);
        }

        placing = false;
        slotWasSwapped = false;
        previousSlot = -1;
        plannedSlot = -1;

        if (mc.player != null && mc.gui.screen() == null) {
            mc.options.keyAttack.setDown(isPhysicalKeyDown(mc.options.keyAttack));
            mc.options.keyUse.setDown(isPhysicalKeyDown(mc.options.keyUse));
        }

        if (Client.rotationManager != null)
            Client.rotationManager.stopRotation();
    }

    private void clearAim() {
        targetHitPos = null;
        targetSide = null;
    }

    private void equipPlannedSlot() {
        if (mc.player == null || plannedSlot < 0 || plannedSlot > 8)
            return;

        int current = mc.player.getInventory().getSelectedSlot();
        if (plannedSlot != current) {
            mc.player.getInventory().setSelectedSlot(plannedSlot);
            slotWasSwapped = true;
        }
    }

    private int pickBlockSlot(boolean preferStrong) {
        if (mc.player == null || mc.level == null)
            return -1;

        int best = -1;
        float bestScore = preferStrong ? -1.0F : Float.MAX_VALUE;
        BlockPos scorePos = mc.player.blockPosition();
        for (int slot = 8; slot >= 0; --slot) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem))
                continue;
            if (ignoreBlocksToggle.get() && isIgnoredBlock(stack))
                continue;

            float score = blockItem.getBlock().defaultBlockState().getDestroySpeed(mc.level, scorePos);
            if (preferStrong ? score > bestScore : score < bestScore) {
                bestScore = score;
                best = slot;
            }
        }
        return best;
    }

    private boolean isIgnoredBlock(ItemStack stack) {
        String configured = ignoredBlocks.get();
        if (configured == null || configured.isBlank())
            return false;

        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
        for (String token : configured.toLowerCase(Locale.ROOT).split("[,\\s]+")) {
            if (token.isBlank())
                continue;
            if (token.equals(id) || token.equals(path) || token.startsWith("minecraft:") && token.substring(10).equals(path))
                return true;
        }
        return false;
    }

    private boolean getTarget() {
        AimResult result = roofAim();
        if (result == null)
            result = sidesAim();
        if (result == null)
            return false;

        BlockPos placed = result.supportBlock.relative(result.face);
        lastTargetAdjacent = isDirectAdjacentPlacement(placed);
        targetHitPos = result.supportBlock;
        targetSide = result.face;
        aimYaw = result.yaw;
        aimPitch = result.pitch;
        return true;
    }

    private AimResult roofAim() {
        if (mc.player == null || mc.level == null || plannedSlot < 0 || plannedSlot > 8)
            return null;

        BlockPos aboveHead = mc.player.blockPosition().above(2);
        if (!isReplaceable(aboveHead))
            return null;

        ItemStack held = mc.player.getInventory().getItem(plannedSlot);
        Vec3 eye = mc.player.getEyePosition();
        double reachSquared = REACH * REACH;
        double expandedReachSquared = (REACH + 1.0D) * (REACH + 1.0D);
        int minY = Mth.floor(eye.y) + 1;
        int maxY = Mth.floor(eye.y + REACH);
        int minX = Mth.floor(eye.x - REACH);
        int maxX = Mth.floor(eye.x + REACH);
        int minZ = Mth.floor(eye.z - REACH);
        int maxZ = Mth.floor(eye.z + REACH);

        ArrayList<BlockCandidate> candidates = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double dx = x + 0.5D - eye.x;
                    double dy = y + 0.5D - eye.y;
                    double dz = z + 0.5D - eye.z;
                    if (dx * dx + dy * dy + dz * dz > expandedReachSquared)
                        continue;

                    BlockPos pos = new BlockPos(x, y, z);
                    if (isReplaceable(pos))
                        continue;

                    Block block = mc.level.getBlockState(pos).getBlock();
                    if (isInteractable(pos) || block instanceof FenceBlock || block instanceof WallBlock)
                        continue;

                    double distance = distanceSquaredToAabb(eye, pos);
                    if (distance > reachSquared)
                        continue;

                    candidates.add(new BlockCandidate(distance, pos));
                }
            }
        }

        candidates.sort((a, b) -> Double.compare(a.distance, b.distance));
        for (BlockCandidate candidate : candidates) {
            AimResult result = getBestRotationsToBlock(held, candidate.pos, eye, REACH, minY);
            if (result != null)
                return result;
        }
        return null;
    }

    private AimResult getBestRotationsToBlock(ItemStack held, BlockPos targetCell, Vec3 eye, double reach, int minY) {
        if (Client.rotationManager == null)
            return null;

        float baseYaw = Client.rotationManager.getRotationYaw();
        float basePitch = Client.rotationManager.getRotationPitch();
        boolean faceUp = Math.abs(eye.y - (targetCell.getY() + 1.0D)) < Math.abs(eye.y - targetCell.getY());
        boolean faceSouth = Math.abs(eye.z - (targetCell.getZ() + 1.0D)) < Math.abs(eye.z - targetCell.getZ());
        boolean faceEast = Math.abs(eye.x - (targetCell.getX() + 1.0D)) < Math.abs(eye.x - targetCell.getX());

        double bx = targetCell.getX();
        double by = targetCell.getY();
        double bz = targetCell.getZ();
        double jitter = GRID_STEP * 0.1D;
        ArrayList<RotationCandidate> candidates = new ArrayList<>((GRID_N + 1) * (GRID_N + 1) * 3 + 1);
        candidates.add(new RotationCandidate(0.0D, baseYaw, basePitch));

        for (int row = 0; row <= GRID_N; row++) {
            double v = clamp01(row * GRID_STEP + jitter(jitter));
            for (int col = 0; col <= GRID_N; col++) {
                double u = clamp01(col * GRID_STEP + jitter(jitter));

                addRotationCandidate(candidates, eye, bx + u,
                        faceUp ? by + 1.0D - GRID_INSET : by + GRID_INSET, bz + v, baseYaw, basePitch);
                addRotationCandidate(candidates, eye, bx + u, by + v,
                        faceSouth ? bz + 1.0D - GRID_INSET : bz + GRID_INSET, baseYaw, basePitch);
                addRotationCandidate(candidates, eye, faceEast ? bx + 1.0D - GRID_INSET : bx + GRID_INSET,
                        by + v, bz + u, baseYaw, basePitch);
            }
        }

        candidates.sort((a, b) -> Double.compare(a.cost, b.cost));
        for (RotationCandidate candidate : candidates) {
            BlockHitResult hit = Client.rotationManager.rayTraceBlocks(candidate.yaw, candidate.pitch, reach);
            if (hit == null || hit.getType() != HitResult.Type.BLOCK)
                continue;

            BlockPos hitBlock = hit.getBlockPos();
            Direction face = hit.getDirection();
            if (hitBlock.equals(targetCell) && hitBlock.getY() >= minY
                    && !(face == Direction.DOWN && targetCell.getY() == minY)
                    && canPlaceBlockOnSide(held, hitBlock, face)) {
                return new AimResult(hitBlock, face, candidate.yaw, candidate.pitch);
            }
        }
        return null;
    }

    private void addRotationCandidate(List<RotationCandidate> candidates, Vec3 eye,
                                      double x, double y, double z, float baseYaw, float basePitch) {
        float[] rotations = rotationsFromEye(eye, x, y, z);
        candidates.add(new RotationCandidate(
                Math.abs(Mth.wrapDegrees(rotations[0] - baseYaw)) + Math.abs(rotations[1] - basePitch),
                rotations[0], rotations[1]
        ));
    }

    private AimResult sidesAim() {
        if (mc.player == null || mc.level == null)
            return null;

        BlockPos feet = mc.player.blockPosition();
        BlockPos head = feet.above();
        Vec3 eye = mc.player.getEyePosition();

        ArrayList<BlockPos> baseline = new ArrayList<>(8);
        for (Direction direction : HORIZONTALS) {
            baseline.add(feet.relative(direction));
            baseline.add(head.relative(direction));
        }

        ArrayList<BlockPos> primaryGoals = new ArrayList<>(baseline.size());
        for (BlockPos pos : baseline) {
            if (isReplaceable(pos) && hasAirNeighbor(pos, feet, head))
                primaryGoals.add(pos);
        }
        if (primaryGoals.isEmpty())
            return null;

        Vec3 enemyPos = closestPlayerPosition(100.0D);
        if (enemyPos != null) {
            baseline.sort((a, b) -> Double.compare(distanceSquaredToPoint(a, enemyPos), distanceSquaredToPoint(b, enemyPos)));
            int picked = 0;
            for (BlockPos pos : baseline) {
                if (!isReplaceable(pos) || !hasAirNeighbor(pos, feet, head))
                    continue;
                AimResult result = findBestForGoals(Collections.singletonList(pos), REACH, eye);
                if (result != null)
                    return result;
                if (++picked >= 3)
                    break;
            }
        }

        AimResult result = findBestForGoals(primaryGoals, REACH, eye);
        if (result != null)
            return result;

        ArrayList<BlockPos> frontier = new ArrayList<>(primaryGoals);
        Set<Long> seen = new HashSet<>(frontier.size() * 8);
        for (BlockPos pos : frontier)
            seen.add(pos.asLong());

        for (int iteration = 0; iteration < 5; iteration++) {
            if (frontier.isEmpty())
                break;

            ArrayList<BlockPos> layer = new ArrayList<>(frontier.size() * 3);
            for (BlockPos goal : frontier) {
                for (Direction direction : Direction.values()) {
                    BlockPos support = goal.relative(direction);
                    if (!isReplaceable(support) || !seen.add(support.asLong()))
                        continue;
                    layer.add(support);
                }
            }

            if (!layer.isEmpty()) {
                result = findBestForGoals(layer, REACH, eye);
                if (result != null)
                    return result;
            }
            frontier = layer;
        }
        return null;
    }

    private AimResult findBestForGoals(List<BlockPos> goals, double reach, Vec3 eye) {
        if (goals == null || goals.isEmpty() || plannedSlot < 0 || plannedSlot > 8 || Client.rotationManager == null)
            return null;

        ItemStack held = mc.player.getInventory().getItem(plannedSlot);
        float currentYaw = Client.rotationManager.getRotationYaw();
        float currentPitch = Client.rotationManager.getRotationPitch();
        BlockHitResult currentHit = Client.rotationManager.rayTraceBlocks(currentYaw, currentPitch, reach);
        if (currentHit != null && currentHit.getType() == HitResult.Type.BLOCK) {
            BlockPos support = currentHit.getBlockPos();
            Direction face = currentHit.getDirection();
            if (!isReplaceable(support) && canPlaceBlockOnSide(held, support, face)) {
                for (BlockPos goal : goals) {
                    AimResult result = tryPlacement(reach, currentYaw, currentPitch, support, face, goal);
                    if (result != null)
                        return result;
                }
            }
        }

        double jitter = GRID_STEP * 0.1D;
        double insetTop = 1.0D - GRID_INSET - 1.0E-3D;
        double insetBottom = GRID_INSET + 1.0E-3D;
        ArrayList<PlacementCandidate> candidates = new ArrayList<>(
                Math.max(16, goals.size() * 6 * (GRID_N + 1) * (GRID_N + 1))
        );

        for (BlockPos goal : goals) {
            for (SupportOffset supportOffset : SUPPORTS) {
                BlockPos support = goal.offset(supportOffset.dx, supportOffset.dy, supportOffset.dz);
                if (isReplaceable(support) || !canPlaceBlockOnSide(held, support, supportOffset.face))
                    continue;

                double sx = support.getX();
                double sy = support.getY();
                double sz = support.getZ();
                for (int row = 0; row <= GRID_N; row++) {
                    boolean leftToRight = (row & 1) == 0;
                    double v = clamp01(row * GRID_STEP + jitter(jitter));
                    for (int col = 0; col <= GRID_N; col++) {
                        double column = clamp01(col * GRID_STEP + jitter(jitter));
                        double u = leftToRight ? column : 1.0D - column;

                        double x;
                        double y;
                        double z;
                        if (supportOffset.dy != 0) {
                            x = sx + u;
                            z = sz + v;
                            y = sy + (supportOffset.dy < 0 ? insetTop : insetBottom);
                        } else if (supportOffset.dz != 0) {
                            x = sx + u;
                            y = sy + v;
                            z = sz + (supportOffset.dz < 0 ? insetTop : insetBottom);
                        } else {
                            z = sz + u;
                            y = sy + v;
                            x = sx + (supportOffset.dx < 0 ? insetTop : insetBottom);
                        }

                        float[] rotation = rotationsFromEye(eye, x, y, z);
                        float yawDifference = Math.abs(Mth.wrapDegrees(rotation[0] - currentYaw));
                        float pitchDifference = Math.abs(rotation[1] - currentPitch);
                        if (yawDifference < 0.1F && pitchDifference < 0.1F)
                            continue;

                        candidates.add(new PlacementCandidate(
                                yawDifference + pitchDifference,
                                rotation[0], rotation[1], support, supportOffset.face, goal
                        ));
                    }
                }
            }
        }

        candidates.sort((a, b) -> Double.compare(a.cost, b.cost));
        for (PlacementCandidate candidate : candidates) {
            AimResult result = tryPlacement(reach, candidate.yaw, candidate.pitch,
                    candidate.support, candidate.face, candidate.goal);
            if (result != null)
                return result;
        }
        return null;
    }

    private AimResult tryPlacement(double reach, float yaw, float pitch,
                                   BlockPos expectedSupport, Direction expectedFace, BlockPos goal) {
        if (Client.rotationManager == null)
            return null;

        BlockHitResult hit = Client.rotationManager.rayTraceBlocks(yaw, pitch, reach);
        if (hit == null || hit.getType() != HitResult.Type.BLOCK)
            return null;
        if (!hit.getBlockPos().equals(expectedSupport) || hit.getDirection() != expectedFace)
            return null;
        if (!hit.getBlockPos().relative(hit.getDirection()).equals(goal))
            return null;
        return new AimResult(hit.getBlockPos(), hit.getDirection(), yaw, pitch);
    }

    private boolean isDirectAdjacentPlacement(BlockPos placed) {
        if (mc.player == null || placed == null)
            return false;

        BlockPos feet = mc.player.blockPosition();
        int dx = placed.getX() - feet.getX();
        int dy = placed.getY() - feet.getY();
        int dz = placed.getZ() - feet.getZ();
        if (dx == 0 && dz == 0 && dy == 2)
            return true;
        return (dy == 0 || dy == 1)
                && ((Math.abs(dx) == 1 && dz == 0) || (Math.abs(dz) == 1 && dx == 0));
    }

    private boolean hasAirNeighbor(BlockPos position, BlockPos feet, BlockPos head) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = position.relative(direction);
            if (neighbor.equals(feet) || neighbor.equals(head))
                continue;
            if (isReplaceable(neighbor))
                return true;
        }
        return false;
    }

    private boolean canPlaceBlockOnSide(ItemStack stack, BlockPos support, Direction face) {
        if (mc.player == null || mc.level == null || stack == null || stack.isEmpty()
                || !(stack.getItem() instanceof BlockItem blockItem) || support == null || face == null)
            return false;
        if (isReplaceable(support))
            return false;

        Vec3 hitPos = faceCenter(support, face);
        BlockHitResult hit = new BlockHitResult(hitPos, face, support, false);
        BlockPlaceContext context = new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND, stack, hit);
        if (!context.canPlace())
            return false;

        BlockState state = blockItem.getBlock().getStateForPlacement(context);
        if (state == null || !state.canSurvive(mc.level, context.getClickedPos()))
            return false;
        return mc.level.isUnobstructed(state, context.getClickedPos(),
                net.minecraft.world.phys.shapes.CollisionContext.of(mc.player));
    }

    private boolean isReplaceable(BlockPos pos) {
        return mc.level != null && pos != null && mc.level.getBlockState(pos).canBeReplaced();
    }

    private boolean isInteractable(BlockPos pos) {
        if (mc.level == null || pos == null)
            return false;
        Block block = mc.level.getBlockState(pos).getBlock();
        return block instanceof EntityBlock
                || block instanceof DoorBlock
                || block instanceof TrapDoorBlock
                || block instanceof FenceGateBlock
                || block instanceof ButtonBlock
                || block instanceof LeverBlock
                || block instanceof CraftingTableBlock
                || block instanceof AnvilBlock
                || block instanceof BedBlock
                || block instanceof BellBlock
                || block instanceof CartographyTableBlock
                || block instanceof ComparatorBlock
                || block instanceof FurnaceBlock
                || block instanceof HopperBlock
                || block instanceof ChestBlock
                || block instanceof GrindstoneBlock
                || block instanceof LoomBlock
                || block instanceof NoteBlock
                || block instanceof RepeaterBlock
                || block instanceof CakeBlock
                || block instanceof CampfireBlock
                || block instanceof StonecutterBlock;
    }

    private static Vec3 faceCenter(BlockPos position, Direction face) {
        return Vec3.atCenterOf(position).add(
                face.getStepX() * 0.5D,
                face.getStepY() * 0.5D,
                face.getStepZ() * 0.5D
        );
    }

    private static Vec3 closestPlayerPosition(double range) {
        if (mc.level == null || mc.player == null)
            return null;

        double maxDistanceSquared = range * range;
        double bestDistanceSquared = maxDistanceSquared;
        Vec3 best = null;
        Vec3 self = mc.player.position();
        for (var player : mc.level.players()) {
            if (player == mc.player || player.isSpectator() || player.isRemoved())
                continue;
            double distanceSquared = self.distanceToSqr(player.position());
            if (distanceSquared < bestDistanceSquared) {
                bestDistanceSquared = distanceSquared;
                best = player.position();
            }
        }
        return best;
    }

    private static double distanceSquaredToPoint(BlockPos pos, Vec3 point) {
        return square(pos.getX() + 0.5D - point.x)
                + square(pos.getY() + 0.5D - point.y)
                + square(pos.getZ() + 0.5D - point.z);
    }

    private static double distanceSquaredToAabb(Vec3 point, BlockPos pos) {
        double x = clamp(point.x, pos.getX(), pos.getX() + 1.0D);
        double y = clamp(point.y, pos.getY(), pos.getY() + 1.0D);
        double z = clamp(point.z, pos.getZ(), pos.getZ() + 1.0D);
        return point.distanceToSqr(x, y, z);
    }

    private static float[] rotationsFromEye(Vec3 eye, double x, double y, double z) {
        double dx = x - eye.x;
        double dy = y - eye.y;
        double dz = z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) -(Math.atan2(dy, horizontal) * (180.0D / Math.PI));
        return new float[]{Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90.0F, 90.0F)};
    }

    private static float[] smoothRotation(float baseYaw, float basePitch, float targetYaw, float targetPitch,
                                          int speed, int randomization) {
        float yawDifference = Mth.wrapDegrees(targetYaw - baseYaw);
        float pitchDifference = targetPitch - basePitch;
        float maximumStep = Math.max(1.0F, speed);
        float randomScale = Math.max(0.0F, Math.min(1.0F, randomization / 100.0F));
        float yawStep = limitedStep(yawDifference, maximumStep, randomScale);
        float pitchStep = limitedStep(pitchDifference, maximumStep, randomScale);
        return new float[]{baseYaw + yawStep, Mth.clamp(basePitch + pitchStep, -90.0F, 90.0F)};
    }

    private static float limitedStep(float difference, float maximum, float randomScale) {
        if (Math.abs(difference) <= maximum)
            return difference;
        float jitter = 1.0F + (float) ((Math.random() * 2.0D - 1.0D) * randomScale);
        return Math.copySign(Math.min(Math.abs(difference), maximum * jitter), difference);
    }

    private static boolean isPhysicalKeyDown(KeyMapping mapping) {
        if (mapping == null)
            return false;
        InputConstants.Key key = InputConstants.getKey(mapping.saveString());
        if (key.getType() == InputConstants.Type.MOUSE)
            return GLFW.glfwGetMouseButton(mc.getWindow().handle(), key.getValue()) == GLFW.GLFW_PRESS;
        return key.getValue() >= 0 && InputConstants.isKeyDown(mc.getWindow(), key.getValue());
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private static float quadInOutEasing(float t) {
        if (t < 0.5F)
            return 2.0F * t * t;
        return -1.0F + (4.0F - 2.0F * t) * t;
    }

    private static double square(double value) {
        return value * value;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return value < minimum ? minimum : Math.min(value, maximum);
    }

    private static double clamp01(double value) {
        return value < 0.0D ? 0.0D : value > 1.0D ? 1.0D : value;
    }

    private static double jitter(double range) {
        return range > 0.0D ? (Math.random() * 2.0D - 1.0D) * range : 0.0D;
    }

    private static final class SupportOffset {
        private final int dx;
        private final int dy;
        private final int dz;
        private final Direction face;

        private SupportOffset(int dx, int dy, int dz, Direction face) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.face = face;
        }
    }

    private static final class BlockCandidate {
        private final double distance;
        private final BlockPos pos;

        private BlockCandidate(double distance, BlockPos pos) {
            this.distance = distance;
            this.pos = pos;
        }
    }

    private static final class RotationCandidate {
        private final double cost;
        private final float yaw;
        private final float pitch;

        private RotationCandidate(double cost, float yaw, float pitch) {
            this.cost = cost;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    private static final class PlacementCandidate {
        private final double cost;
        private final float yaw;
        private final float pitch;
        private final BlockPos support;
        private final Direction face;
        private final BlockPos goal;

        private PlacementCandidate(double cost, float yaw, float pitch,
                                   BlockPos support, Direction face, BlockPos goal) {
            this.cost = cost;
            this.yaw = yaw;
            this.pitch = pitch;
            this.support = support;
            this.face = face;
            this.goal = goal;
        }
    }

    private static final class AimResult {
        private final BlockPos supportBlock;
        private final Direction face;
        private final float yaw;
        private final float pitch;

        private AimResult(BlockPos supportBlock, Direction face, float yaw, float pitch) {
            this.supportBlock = supportBlock;
            this.face = face;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}
