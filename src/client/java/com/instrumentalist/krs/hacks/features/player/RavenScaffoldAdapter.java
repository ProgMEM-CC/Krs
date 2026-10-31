package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.Client;
import com.instrumentalist.krs.utils.move.InputUtil;
import com.instrumentalist.krs.utils.entity.PlayerUtil;
import com.instrumentalist.krs.utils.input.Keys;
import com.instrumentalist.krs.utils.value.SettingValue;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * Explicit 26.2 API boundary for the supplied script, not a Raven API implementation.
 * Names below keep the algorithm diff readable. Block names/types are mapped from the
 * legacy registry; placement, collision/raycast shapes and inventory prediction are native.
 */
abstract class RavenScaffoldAdapter {
    final Minecraft nativeMc = Minecraft.getInstance();
    final MCAdapter mc = new MCAdapter();
    final RotationAdapter rotations = new RotationAdapter();
    final BlockAdapter BlockUtils = new BlockAdapter();
    final RotationAdapter RotationUtils = rotations;
    final ClientAdapter client = new ClientAdapter();
    final InventoryAccess inventory = new InventoryAccess();
    final KeyAccess keybinds = new KeyAccess();
    final Utilities Utils = new Utilities();

    static final class Setting {
        final SettingValue<?> value;
        Setting(SettingValue<?> value) { this.value = value; }
        double getInput() {
            Number number = (Number) value.get();
            // Raven sliders expose doubles; KRS stores floats. Recover the displayed
            // decimal instead of widening binary float noise (e.g. 1.08f -> 1.0800000429).
            return number instanceof Float ? Double.parseDouble(number.toString()) : number.doubleValue();
        }
        boolean isToggled() { return Boolean.TRUE.equals(value.get()); }
        String getSelectedOption() { return String.valueOf(value.get()); }
    }

    static final class BlockPos {
        final int x, y, z;
        BlockPos(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
        int getX() { return x; } int getY() { return y; } int getZ() { return z; }
        BlockPos add(int dx, int dy, int dz) { return new BlockPos(x + dx, y + dy, z + dz); }
        BlockPos offset(EnumFacing face) { return add(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ()); }
        net.minecraft.core.BlockPos nativePos() { return new net.minecraft.core.BlockPos(x, y, z); }
        @Override public boolean equals(Object other) { return other instanceof BlockPos p && x == p.x && y == p.y && z == p.z; }
        @Override public int hashCode() { return (x * 7340033) ^ (y * 4099) ^ (z * 19349663); }
    }

    enum EnumFacing {
        DOWN(Direction.DOWN), UP(Direction.UP), NORTH(Direction.NORTH), SOUTH(Direction.SOUTH), WEST(Direction.WEST), EAST(Direction.EAST);
        final Direction nativeFace;
        EnumFacing(Direction face) { nativeFace = face; }
        int getFrontOffsetX() { return nativeFace.getStepX(); }
        int getFrontOffsetY() { return nativeFace.getStepY(); }
        int getFrontOffsetZ() { return nativeFace.getStepZ(); }
        EnumFacing getOpposite() { return valueOf(nativeFace.getOpposite().name()); }
    }

    static final class MovingObjectPosition {
        final BlockPos pos;
        final Vec3 hitVec;
        final EnumFacing sideHit;
        MovingObjectPosition(BlockHitResult hit) {
            pos = new BlockPos(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ());
            hitVec = hit.getLocation(); sideHit = EnumFacing.valueOf(hit.getDirection().name());
        }
        BlockPos getBlockPos() { return pos; }
    }

    static final class ItemStack {
        final net.minecraft.world.item.ItemStack nativeStack;
        final int stackSize;
        final boolean isBlock;
        final String name, type;
        ItemStack(net.minecraft.world.item.ItemStack stack) {
            nativeStack = stack; stackSize = stack.getCount();
            isBlock = !stack.isEmpty() && stack.getItem() instanceof BlockItem;
            var block = isBlock ? ((BlockItem) stack.getItem()).getBlock() : null;
            name = block == null ? null : legacyName(block);
            type = block == null ? "" : legacyType(block);
        }
    }

    static final class Block {
        final String name;
        final int variant;
        final boolean interactable;
        final BlockState nativeState;
        Block(BlockState state) {
            nativeState = state; name = legacyName(state.getBlock());
            variant = state.hasProperty(SnowLayerBlock.LAYERS) ? state.getValue(SnowLayerBlock.LAYERS) - 1 : 0;
            // Raven's interactable metadata is external. This maps legacy interactive classes,
            // plus modern equivalents, rather than using KRS's inventory blacklist as support rules.
            var b = state.getBlock();
            interactable = b instanceof EntityBlock || b instanceof DoorBlock || b instanceof TrapDoorBlock
                    || b instanceof FenceGateBlock || b instanceof ButtonBlock || b instanceof LeverBlock
                    || b instanceof CraftingTableBlock || b instanceof AnvilBlock || b instanceof BedBlock
                    || b instanceof NoteBlock || b instanceof RepeaterBlock || b instanceof ComparatorBlock
                    || b instanceof CakeBlock || b instanceof BellBlock || b instanceof GrindstoneBlock
                    || b instanceof StonecutterBlock || b instanceof LoomBlock || b instanceof CartographyTableBlock;
        }
    }
    static final class IBlockState {
        final Block block;
        IBlockState(BlockState state) { block = new Block(state); }
        Block getBlock() { return block; }
    }

    static String legacyName(net.minecraft.world.level.block.Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if (block instanceof AirBlock) return "air";
        if (block instanceof SnowLayerBlock) return "snow_layer";
        if (block instanceof SaplingBlock) return "sapling";
        if (block instanceof FlowerBlock) return "red_flower";
        if (block instanceof TallFlowerBlock || block instanceof DoublePlantBlock) return "double_plant";
        return switch (name) {
            case "short_grass", "fern" -> "tallgrass";
            case "dead_bush" -> "deadbush";
            case "sugar_cane" -> "reeds";
            case "lily_pad" -> "waterlily";
            case "enchanting_table" -> "enchanting_table";
            case "spawner" -> "mob_spawner";
            case "note_block" -> "noteblock";
            default -> name;
        };
    }

    static String legacyType(net.minecraft.world.level.block.Block b) {
        // Inheritance matters (redstone torches, trapped chests, stained panes, etc.).
        if (b instanceof SnowLayerBlock) return "BlockSnow";
        if (b instanceof WebBlock) return "BlockWeb";
        if (b instanceof SaplingBlock) return "BlockSapling";
        if (b instanceof DaylightDetectorBlock) return "BlockDaylightDetector";
        if (b instanceof BeaconBlock) return "BlockBeacon";
        if (b instanceof AbstractBannerBlock) return "BlockBanner";
        if (b instanceof EndPortalFrameBlock) return "BlockEndPortalFrame";
        if (b instanceof EndPortalBlock) return "BlockEndPortal";
        if (b instanceof LeverBlock) return "BlockLever";
        if (b instanceof ButtonBlock) return "BlockButton";
        if (b instanceof AbstractSkullBlock) return "BlockSkull";
        if (b instanceof LiquidBlock) return "BlockLiquid";
        if (b instanceof CactusBlock) return "BlockCactus";
        if (b instanceof DoublePlantBlock) return "BlockDoublePlant";
        if (b == Blocks.LILY_PAD) return "BlockLilyPad";
        if (b instanceof CarpetBlock) return "BlockCarpet";
        if (b instanceof TripWireBlock) return "BlockTripWire";
        if (b instanceof TripWireHookBlock) return "BlockTripWireHook";
        if (b instanceof TallGrassBlock) return "BlockTallGrass";
        if (b instanceof FlowerBlock) return "BlockFlower";
        if (b instanceof FlowerPotBlock) return "BlockFlowerPot";
        if (b instanceof SignBlock) return "BlockSign";
        if (b instanceof LadderBlock) return "BlockLadder";
        if (b instanceof TorchBlock) return "BlockTorch";
        if (b instanceof StairBlock) return "BlockStairs";
        if (b instanceof SlabBlock) return "BlockSlab";
        if (b instanceof FenceBlock) return "BlockFence";
        if (b instanceof IronBarsBlock) return "BlockPane";
        if (b == Blocks.GRAVEL) return "BlockGravel";
        if (b == Blocks.CLAY) return "BlockClay";
        if (b instanceof SandBlock) return "BlockSand";
        if (b instanceof SoulSandBlock) return "BlockSoulSand";
        if (b instanceof BaseRailBlock) return "BlockRailBase";
        if (b instanceof FenceGateBlock) return "BlockFenceGate";
        if (b instanceof BasePressurePlateBlock) return "BlockBasePressurePlate";
        if (b instanceof AnvilBlock) return "BlockAnvil";
        if (b instanceof EnchantingTableBlock) return "BlockEnchantmentTable";
        if (b instanceof ChestBlock) return "BlockChest";
        if (b instanceof TrapDoorBlock) return "BlockTrapDoor";
        if (b instanceof DoorBlock) return "BlockDoor";
        if (b instanceof JukeboxBlock) return "BlockJukebox";
        if (b instanceof EnderChestBlock) return "BlockEnderChest";
        if (b instanceof BrewingStandBlock) return "BlockBrewingStand";
        if (b instanceof BedBlock) return "BlockBed";
        if (b instanceof DropperBlock) return "BlockDropper";
        if (b instanceof DispenserBlock) return "BlockDispenser";
        if (b instanceof HopperBlock) return "BlockHopper";
        if (b instanceof NoteBlock) return "BlockNote";
        if (b instanceof CraftingTableBlock) return "BlockWorkbench";
        if (b instanceof AbstractFurnaceBlock) return "BlockFurnace";
        if (b instanceof SpawnerBlock) return "BlockMobSpawner";
        return b.getClass().getSimpleName();
    }

    static final class MovementInput {
        float forward, strafe;
        boolean jump, sneak;
        MovementInput(float forward, float strafe, boolean jump, boolean sneak) {
            this.forward = forward; this.strafe = strafe; this.jump = jump; this.sneak = sneak;
        }
    }
    static final class PrePlayerInputEvent {
        final MovementInput raw;
        PrePlayerInputEvent(MovementInput raw) { this.raw = raw; }
        float getForward() { return raw.forward; } float getStrafe() { return raw.strafe; }
        boolean isJump() { return raw.jump; } boolean isSneak() { return raw.sneak; }
        void setForward(float v) { raw.forward = v; } void setStrafe(float v) { raw.strafe = v; }
        void setJump(boolean v) { raw.jump = v; } void setSneak(boolean v) { raw.sneak = v; }
    }
    MovementInput snapshot(ClientInput input) {
        Input flags = input == null || input.keyPresses == null ? Input.EMPTY : input.keyPresses;
        return new MovementInput(InputUtil.forwardImpulse(input), InputUtil.leftImpulse(input), flags.jump(), flags.shift());
    }
    void apply(ClientInput input, MovementInput movement, boolean sprint) {
        InputUtil.applyInput(input, new Input(movement.forward > 0, movement.forward < 0,
                movement.strafe > 0, movement.strafe < 0, movement.jump, movement.sneak, sprint));
        // The script supplies fractional impulses. Do not normalize/digitize its 0.62 weave.
        InputUtil.setMoveVector(input, new Vec2(movement.strafe, movement.forward));
    }

    final class EntityPlayerSP {
        final LocalPlayer raw;
        double posX, posY, posZ, motionX, motionY, motionZ;
        float rotationYaw, rotationPitch;
        boolean onGround;
        final InventoryAdapter inventory = new InventoryAdapter();
        MovementInput movementInput;
        EntityPlayerSP(LocalPlayer raw) { this.raw = raw; update(); }
        void update() {
            posX = raw.getX(); posY = raw.getY(); posZ = raw.getZ();
            Vec3 m = raw.getDeltaMovement(); motionX = m.x; motionY = m.y; motionZ = m.z;
            rotationYaw = raw.getYRot(); rotationPitch = raw.getXRot(); onGround = raw.onGround();
            inventory.sync(); movementInput = snapshot(raw.input);
        }
        Vec3 getPositionVector() { return new Vec3(posX, posY, posZ); }
        ItemStack getHeldItem() { return new ItemStack(raw.getMainHandItem()); }
        float getEyeHeight() { return raw.getEyeHeight(); }
        void setSprinting(boolean value) { raw.setSprinting(value); }
        void swingItem() { PlayerUtil.INSTANCE.swingHandWithoutPacket(InteractionHand.MAIN_HAND); }
    }
    final class InventoryAdapter {
        int currentItem;
        final ItemStack[] mainInventory = new ItemStack[36];
        void sync() {
            if (nativeMc.player == null) return;
            currentItem = nativeMc.player.getInventory().getSelectedSlot();
            for (int i = 0; i < 36; i++) mainInventory[i] = new ItemStack(nativeMc.player.getInventory().getItem(i));
        }
        ItemStack getStackInSlot(int slot) {
            return slot < 0 || slot >= 36 || nativeMc.player == null ? null : new ItemStack(nativeMc.player.getInventory().getItem(slot));
        }
    }
    final class InventoryAccess {
        int getSlot() { return nativeMc.player == null ? -1 : nativeMc.player.getInventory().getSelectedSlot(); }
        void setSlot(int slot) { if (nativeMc.player != null) nativeMc.player.getInventory().setSelectedSlot(slot); }
    }
    final class MCAdapter {
        EntityPlayerSP thePlayer;
        Object currentScreen;
        final GameSettingsAdapter gameSettings = new GameSettingsAdapter();
        void sync() {
            if (nativeMc.player == null || nativeMc.level == null) thePlayer = null;
            else if (thePlayer == null || thePlayer.raw != nativeMc.player) thePlayer = new EntityPlayerSP(nativeMc.player);
            else thePlayer.update();
            currentScreen = nativeMc.gui.screen();
            if (Client.rotationManager != null) {
                // Map the script's server-rotation query to KRS's effective silent rotation.
                // The upstream Raven getter implementation is not supplied for comparison.
                rotations.serverRotations[0] = Client.rotationManager.getRotationYaw();
                rotations.serverRotations[1] = Client.rotationManager.getRotationPitch();
            }
        }
    }
    final class GameSettingsAdapter {
        final KeyMapping keyBindSprint = nativeMc.options.keySprint, keyBindForward = nativeMc.options.keyUp,
                keyBindBack = nativeMc.options.keyDown, keyBindLeft = nativeMc.options.keyLeft,
                keyBindRight = nativeMc.options.keyRight, keyBindJump = nativeMc.options.keyJump;
    }
    static final class MathHelper {
        static int floor_double(double v) { int i = (int) v; return v < i ? i - 1 : i; }
        static float wrapAngleTo180_float(float a) { a %= 360f; if (a >= 180f) a -= 360f; if (a < -180f) a += 360f; return a; }
        static float clamp_float(float v, float lo, float hi) { return v < lo ? lo : Math.min(v, hi); }
        static int clamp_int(int v, int lo, int hi) { return v < lo ? lo : Math.min(v, hi); }
        static double clamp_double(double v, double lo, double hi) { return v < lo ? lo : Math.min(v, hi); }
    }
    final class Utilities {
        boolean nullCheck() { return nativeMc.player != null && nativeMc.level != null; }
        boolean isBindDown(KeyMapping key) { return key != null && key.isDown(); }
        double getHorizontalSpeed(EntityPlayerSP player) { return Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ); }
    }
    final class KeyAccess {
        boolean isMouseDown(int button) { return InputConstants.isKeyDown(button); }
        boolean sneakDown() { return physicalDown(nativeMc.options.keyShift); }
    }
    boolean physicalDown(KeyMapping mapping) {
        InputConstants.Key key = InputConstants.getKey(mapping.saveString());
        if (key.getType() == InputConstants.Type.MOUSE) return keybinds.isMouseDown(key.getValue());
        return key.getValue() >= 0 && InputConstants.isKeyDown(key.getValue());
    }
    final class ClientAdapter {
        long time() { return System.nanoTime() / 1_000_000L; }
        boolean isSneak() { return nativeMc.player != null && nativeMc.player.input.keyPresses.shift(); }
        void setMotion(double x, double y, double z) { nativeMc.player.setDeltaMovement(x, y, z); }
        void setSpeed(double speed) {
            float forward = InputUtil.forwardImpulse(nativeMc.player.input), strafe = InputUtil.leftImpulse(nativeMc.player.input);
            float yaw = nativeMc.player.getYRot();
            if (forward < 0) yaw += 180;
            if (strafe != 0) yaw += -90 * (forward == 0 ? 1 : 0.5f * Math.signum(forward)) * Math.signum(strafe);
            double radians = Math.toRadians(yaw);
            nativeMc.player.setDeltaMovement(-Math.sin(radians) * speed, nativeMc.player.getDeltaMovement().y, Math.cos(radians) * speed);
        }
    }
    final class RotationAdapter {
        final float[] serverRotations = new float[2];
        boolean forceMovementFix;
        Float getServerYaw() { return serverRotations[0]; }
        boolean fixMovement() { return forceMovementFix; }
        float clampPitch(float pitch) { return MathHelper.clamp_float(pitch, -90, 90); }
        void setRotations(float yaw, float pitch) {
            serverRotations[0] = yaw; serverRotations[1] = pitch;
            // Shared integration supplies a source-quantized entry point: no second sensitivity snap.
            Client.rotationManager.startSourceRotation(yaw, pitch);
        }
        MovingObjectPosition rayCastBlock(double reach, float yaw, float pitch) {
            if (!Utils.nullCheck()) return null;
            Vec3 start = nativeMc.player.getEyePosition();
            double y = Math.toRadians(yaw), p = Math.toRadians(pitch), horizontal = Math.cos(p);
            Vec3 end = start.add(-Math.sin(y) * horizontal * reach, -Math.sin(p) * reach, Math.cos(y) * horizontal * reach);
            BlockHitResult result = nativeMc.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, nativeMc.player));
            return result.getType() == HitResult.Type.BLOCK ? new MovingObjectPosition(result) : null;
        }
    }
    final class BlockAdapter {
        IBlockState getBlockState(BlockPos pos) { return new IBlockState(nativeMc.level.getBlockState(pos.nativePos())); }
        Block getBlock(int x, int y, int z) { return getBlockState(new BlockPos(x, y, z)).block; }
        boolean isInteractable(Block block) { return block != null && block.interactable; }
        BlockPos offsetPos(MovingObjectPosition hit) { return hit == null ? null : hit.pos.offset(hit.sideHit); }
        boolean canPlaceBlockOnSide(ItemStack stack, BlockPos support, EnumFacing face) {
            if (stack == null || !stack.isBlock || !Utils.nullCheck()) return false;
            Vec3 center = new Vec3(support.x + 0.5 + face.getFrontOffsetX() * 0.5,
                    support.y + 0.5 + face.getFrontOffsetY() * 0.5, support.z + 0.5 + face.getFrontOffsetZ() * 0.5);
            BlockPlaceContext context = new BlockPlaceContext(nativeMc.player, InteractionHand.MAIN_HAND, stack.nativeStack,
                    new BlockHitResult(center, face.nativeFace, support.nativePos(), false));
            BlockState state = ((BlockItem) stack.nativeStack.getItem()).getBlock().getStateForPlacement(context);
            return state != null && context.canPlace() && state.canSurvive(nativeMc.level, context.getClickedPos())
                    && nativeMc.level.isUnobstructed(state, context.getClickedPos(), net.minecraft.world.phys.shapes.CollisionContext.of(nativeMc.player));
        }
        boolean check(BlockPos target, Object ignored) {
            Block block = getBlock(target.x, target.y, target.z);
            return !RavenScaffoldCore.REPLACEABLE_BLOCK_NAMES.contains("," + block.name + ",");
        }
    }

    /** Source SimulatedPlayer wrapper: rotationYaw intentionally starts at zero, as supplied. */
    final class SimulatedPlayer {
        final RavenScaffoldSimulation sim;
        final MovementInput movementInput;
        float rotationYaw;
        SimulatedPlayer(MovementInput input) {
            sim = RavenScaffoldSimulation.create();
            movementInput = new MovementInput(input.forward, input.strafe, input.jump, input.sneak);
        }
        void tick() {
            sim.setForward(movementInput.forward); sim.setStrafe(movementInput.strafe);
            sim.setJump(movementInput.jump); sim.setSneak(movementInput.sneak); sim.setYaw(rotationYaw); sim.tick();
        }
        Vec3 getPos() { return sim.getPosition(); }
    }
}
