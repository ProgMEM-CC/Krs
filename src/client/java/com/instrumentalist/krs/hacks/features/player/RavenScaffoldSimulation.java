package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.utils.move.InputUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.ArrayList;
import java.util.List;

/**
 * Isolated replacement for the UNSUPPLIED Raven Simulation.create implementation.
 * KRS has no player movement simulator. This predicts a detached vanilla-style walking
 * body using native 26.2 collision shapes. It cannot establish binary/physics identity
 * with Raven (especially fluids, climbing, step-up, pose changes and server physics).
 * The core preserves every supplied create/set/tick/get call and its original fallback.
 */
final class RavenScaffoldSimulation {
    private final Minecraft mc = Minecraft.getInstance();
    private final LocalPlayer player;
    private Vec3 position, motion;
    private AABB box;
    private float forward, strafe, yaw;
    private boolean jump, sneak, grounded;
    private final boolean sprinting;

    private RavenScaffoldSimulation(LocalPlayer player) {
        this.player = player; position = player.position(); motion = player.getDeltaMovement();
        box = player.getBoundingBox(); yaw = player.getYRot(); grounded = player.onGround();
        sprinting = player.isSprinting(); forward = InputUtil.forwardImpulse(player.input);
        strafe = InputUtil.leftImpulse(player.input); jump = player.input.keyPresses.jump(); sneak = player.input.keyPresses.shift();
    }
    static RavenScaffoldSimulation create() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) throw new IllegalStateException("No prediction world");
        return new RavenScaffoldSimulation(player);
    }
    void setForward(float value) { forward = value; }
    void setStrafe(float value) { strafe = value; }
    void setJump(boolean value) { jump = value; }
    void setSneak(boolean value) { sneak = value; }
    void setYaw(float value) { yaw = value; }
    Vec3 getPosition() { return position; }

    void tick() {
        if (mc.level == null || mc.player != player) throw new IllegalStateException("Prediction world changed");
        double mx = Math.abs(motion.x) < 0.003 ? 0 : motion.x;
        double my = Math.abs(motion.y) < 0.003 ? 0 : motion.y;
        double mz = Math.abs(motion.z) < 0.003 ? 0 : motion.z;
        if (jump && grounded) {
            my = 0.42;
            var boost = player.getEffect(MobEffects.JUMP_BOOST);
            if (boost != null) my += 0.1 * (boost.getAmplifier() + 1);
            if (sprinting) { mx -= Math.sin(Math.toRadians(yaw)) * 0.2; mz += Math.cos(Math.toRadians(yaw)) * 0.2; }
        }
        float friction = grounded ? mc.level.getBlockState(BlockPos.containing(position.x, box.minY - 1, position.z)).getBlock().getFriction() * 0.91f : 0.91f;
        float acceleration = grounded ? player.getSpeed() * (0.16277136f / (friction * friction * friction)) : (sprinting ? 0.026f : 0.02f);
        float f = forward * 0.98f * (sneak ? 0.3f : 1), s = strafe * 0.98f * (sneak ? 0.3f : 1);
        float length = (float) Math.sqrt(f * f + s * s);
        if (length >= 1.0E-4) {
            float scale = acceleration / Math.max(1, length); f *= scale; s *= scale;
            double radians = Math.toRadians(yaw), sin = Math.sin(radians), cos = Math.cos(radians);
            mx += s * cos - f * sin; mz += f * cos + s * sin;
        }
        if (sneak && grounded) {
            while (mx != 0 && mc.level.noCollision(player, box.move(mx, -1, 0))) mx = trim(mx);
            while (mz != 0 && mc.level.noCollision(player, box.move(0, -1, mz))) mz = trim(mz);
            while (mx != 0 && mz != 0 && mc.level.noCollision(player, box.move(mx, -1, mz))) { mx = trim(mx); mz = trim(mz); }
        }
        List<VoxelShape> obstacles = new ArrayList<>();
        for (VoxelShape shape : mc.level.getBlockCollisions(player, box.expandTowards(mx, my, mz))) obstacles.add(shape);
        double cy = Shapes.collide(Direction.Axis.Y, box, obstacles, my); box = box.move(0, cy, 0);
        double cx = Shapes.collide(Direction.Axis.X, box, obstacles, mx); box = box.move(cx, 0, 0);
        double cz = Shapes.collide(Direction.Axis.Z, box, obstacles, mz); box = box.move(0, 0, cz);
        grounded = my < 0 && cy != my;
        position = new Vec3((box.minX + box.maxX) * 0.5, box.minY, (box.minZ + box.maxZ) * 0.5);
        motion = new Vec3(cx == mx ? mx * friction : 0, ((cy == my ? my : 0) - 0.08) * 0.98, cz == mz ? mz * friction : 0);
    }
    private static double trim(double value) { return Math.abs(value) < 0.05 ? 0 : value - Math.copySign(0.05, value); }
}
