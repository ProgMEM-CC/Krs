package com.instrumentalist.krs.hacks.features.combat;

import java.util.List;
import java.util.Map;

/** Source arithmetic kept independent of the client for headless regression checks. */
public final class DisplaceMath {
    public static final int DISPLACE_WINDOW_TICKS = 10;
    public static final long ARROW_FADE_MS = 250L;
    public static final double ARROW_FORWARD_GAP = 0.24D;
    public static final double ARROW_BODY_LENGTH = 0.74D;
    public static final double ARROW_BODY_HALF_HEIGHT = 0.08D;
    public static final double ARROW_HEAD_BACKSET = 0.18D;
    public static final double ARROW_HEAD_LENGTH = 0.52D;
    public static final double ARROW_HEAD_HALF_HEIGHT = 0.30D;

    private DisplaceMath() {
    }

    public static int msToTicks(double ms) {
        return ms <= 0.0D ? 0 : (int) Math.ceil(ms / 50.0D);
    }

    /** A disabled condition passes; the OR is intentional, including either toggle alone. */
    public static boolean passesItemCondition(boolean requireKnockback, boolean whitelistEnabled,
                                              boolean hasKnockback, boolean matchesWhitelist) {
        boolean kbPass = !requireKnockback || hasKnockback;
        boolean wlPass = !whitelistEnabled || matchesWhitelist;
        return kbPass || wlPass;
    }

    /** Updates only the supplied target's window. A new/expired window returns true immediately. */
    public static boolean shouldDisplaceInCurrentWindow(Map<Integer, Integer> windows, Integer targetId,
                                                        int currentTick, double delayMs) {
        if (targetId == null)
            return true;

        Integer windowStartTick = windows.get(targetId);
        if (windowStartTick == null || currentTick - windowStartTick >= DISPLACE_WINDOW_TICKS) {
            windows.put(targetId, currentTick);
            return true;
        }

        int delayTicks = msToTicks(delayMs);
        return delayTicks <= 0 || currentTick - windowStartTick >= delayTicks;
    }

    public static float yawFromForward(double forwardX, double forwardZ) {
        return (float) (Math.toDegrees(Math.atan2(forwardZ, forwardX)) - 90.0D);
    }

    public static float fixedDisplaceYaw(float baseYaw, float offset, boolean displaceLeft) {
        return displaceLeft ? baseYaw - offset : baseYaw + offset;
    }

    public static boolean isDisplaceLeft(double playerX, double playerZ, double targetX, double targetZ,
                                         double voidX, double voidZ) {
        double targetDx = targetX - playerX;
        double targetDz = targetZ - playerZ;
        double voidDx = voidX - playerX;
        double voidDz = voidZ - playerZ;
        return targetDx * voidDz - targetDz * voidDx < 0.0D;
    }

    public static double staticVoidScore(double radius, double playerDx, double playerDz) {
        double playerDistSq = playerDx * playerDx + playerDz * playerDz;
        return radius * radius * 1000.0D + playerDistSq;
    }

    public static double dynamicLaneScore(int side, double forward) {
        return (side == 0 ? 1.4D : 1.0D) * (6.0D + 0.5D - forward);
    }

    public static float arrowFadeAlpha(long elapsedMs) {
        return elapsedMs >= ARROW_FADE_MS ? 0.0F : 1.0F - (float) elapsedMs / (float) ARROW_FADE_MS;
    }

    public static ArrowGeometry arrowGeometry(double centerX, double centerY, double centerZ,
                                               double targetWidth, float yaw) {
        double radians = Math.toRadians(yaw);
        double forwardX = -Math.sin(radians);
        double forwardZ = Math.cos(radians);
        double baseOffset = targetWidth * 0.5D + ARROW_FORWARD_GAP;
        double tailX = centerX + forwardX * baseOffset;
        double tailZ = centerZ + forwardZ * baseOffset;
        double bodyEndX = tailX + forwardX * ARROW_BODY_LENGTH;
        double bodyEndZ = tailZ + forwardZ * ARROW_BODY_LENGTH;
        double headBackX = tailX + forwardX * (ARROW_BODY_LENGTH - ARROW_HEAD_BACKSET);
        double headBackZ = tailZ + forwardZ * (ARROW_BODY_LENGTH - ARROW_HEAD_BACKSET);
        Point tail = new Point(tailX, centerY, tailZ);
        Point bodyLow = new Point(bodyEndX, centerY - ARROW_BODY_HALF_HEIGHT, bodyEndZ);
        Point bodyHigh = new Point(bodyEndX, centerY + ARROW_BODY_HALF_HEIGHT, bodyEndZ);
        Point headLow = new Point(headBackX, centerY - ARROW_HEAD_HALF_HEIGHT, headBackZ);
        Point headHigh = new Point(headBackX, centerY + ARROW_HEAD_HALF_HEIGHT, headBackZ);
        Point tip = new Point(bodyEndX + forwardX * ARROW_HEAD_LENGTH, centerY,
                bodyEndZ + forwardZ * ARROW_HEAD_LENGTH);
        return new ArrowGeometry(
                List.of(tail, bodyLow, bodyHigh, bodyLow, headLow, tip,
                        bodyLow, tip, bodyHigh, bodyHigh, tip, headHigh),
                List.of(tail, bodyLow, headLow, tip, headHigh, bodyHigh)
        );
    }

    public record Point(double x, double y, double z) {
    }

    /** Triangle vertices in groups of three; the six outline vertices form a closed loop. */
    public record ArrowGeometry(List<Point> triangles, List<Point> outline) {
    }
}
