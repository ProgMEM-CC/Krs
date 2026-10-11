package com.instrumentalist.krs.hacks.features.player;

import java.util.Arrays;
import java.util.Random;

/** Isolated reference-vs-translated arithmetic/queue checks; not an in-game physics test. */
final class RavenScaffoldAlgorithmTest {
    static void run() throws Exception {
        String original = RavenReferenceCompiler.source("Scaffold.java");
        String translated = RavenReferenceCompiler.source("src/client/java/com/instrumentalist/krs/hacks/features/player/RavenScaffoldCore.java");
        String[] methods = {"quantizeMouseRotation", "unwrapYaw", "clampRotationDelta", "quantizeRotation",
                "jitter", "updateYawJitter", "nextYawJitter", "rotationEaseScale", "easeRotations", "smooth",
                "getRotationsToOffset", "getMovementYaw", "updateCardinalSide", "isDiagonalMovement",
                "resetLongTellyQueue", "buildLongTellyQueue"};
        Class<?>[] classes = new Class<?>[2];
        for (int i = 0; i < 2; i++) {
            String name = i == 0 ? "RavenAlgorithmReference" : "RavenAlgorithmPort";
            StringBuilder fixture = new StringBuilder(FIXTURE.replace("FIXTURE_NAME", name));
            for (String method : methods) fixture.append(RavenReferenceCompiler.method(i == 0 ? original : translated, method));
            classes[i] = RavenReferenceCompiler.compile(name, fixture.append('}').toString());
        }
        Object first = classes[0].getConstructor().newInstance(), second = classes[1].getConstructor().newInstance();
        var reference = classes[0].getMethod("step", float.class, float.class, float.class, float.class, double.class, double.class, int.class);
        var port = classes[1].getMethod("step", float.class, float.class, float.class, float.class, double.class, double.class, int.class);
        Random random = new Random(262);
        for (int i = 0; i < 5000; i++) {
            float yaw = random.nextFloat() * 1440 - 720, pitch = random.nextFloat() * 240 - 120;
            float toYaw = i % 10 == 0 ? yaw + 0.1f : random.nextFloat() * 720 - 360;
            float toPitch = i % 10 == 0 ? pitch : random.nextFloat() * 240 - 120;
            double x = random.nextDouble() * 0.8 - 0.4, z = random.nextDouble() * 0.8 - 0.4;
            Object[] args = {yaw, pitch, toYaw, toPitch, x, z, i};
            Object[] expected = (Object[]) reference.invoke(first, args), actual = (Object[]) port.invoke(second, args);
            if (!Arrays.deepEquals(expected, actual)) throw new AssertionError("Scaffold algorithm difference at " + i
                    + "\nsource=" + Arrays.deepToString(expected) + "\nport=" + Arrays.deepToString(actual));
        }
        System.out.println("Scaffold isolated differential parity: 5000 rotation/easing/jitter/queue steps across 16 extracted methods");
    }

    private static final String FIXTURE = """
            import java.util.*;
            public class FIXTURE_NAME {
                static final double ROTATION_INCREMENT=(double)0.0096f;
                static final float MOUSE_ROTATION_INCREMENT=0.023437500F,MIN_YAW_JITTER=0.11F,MAX_YAW_JITTER=0.46F;
                float rotationEaseRate=1.02f,rotationEaseSlowdown,yawJitterAmplitude=0.28f;
                boolean rotationEaseStarted,diagonalMovement;
                int cardinalSide=1;
                Random random=new Random(730);
                float randomFloat(float minimum,float maximum){return (float)(minimum+random.nextDouble()*(maximum-minimum));}
                int queuedBlockCount,pendingQueueIndex=-1,skippedQueueMask,queueCursor,queueStallTicks,ticksSinceGrounded;
                int lastGroundX,lastGroundZ,longTellyRowY,longTellySide=1,longTellyOriginX,longTellyOriginZ;
                int longTellyForwardX,longTellyForwardZ,longTellyLateralX,longTellyLateralZ;
                boolean longTellySprintJump=true,longTellyNeedsSideUpdate=true,hasLongTellyOrigin;
                BlockPos[] queuedBlocks=new BlockPos[8];int[] queueRetryTicks=new int[8];
                MC mc=new MC();
                static class MC { Player thePlayer=new Player(); }
                static class Player {Vec3 position=new Vec3(0,64,0);Vec3 getPositionVector(){return position;}}
                static class Vec3 {double x,y,z;Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;}}
                static class BlockPos {int x,y,z;BlockPos(int x,int y,int z){this.x=x;this.y=y;this.z=z;}public String toString(){return x+","+y+","+z;}}
                static class MathHelper {
                    static int floor_double(double v){int i=(int)v;return v<i?i-1:i;}
                    static float wrapAngleTo180_float(float a){a%=360f;if(a>=180f)a-=360f;if(a<-180f)a+=360f;return a;}
                    static float clamp_float(float v,float lo,float hi){return v<lo?lo:(v>hi?hi:v);}
                }
                static class RotationUtils {static float clampPitch(float p){return MathHelper.clamp_float(p,-90,90);}}
                public Object[] step(float yaw,float pitch,float toYaw,float toPitch,double x,double z,int tick){
                    updateYawJitter();float jitter=nextYawJitter();
                    float[] eased=easeRotations(yaw,pitch,toYaw,toPitch);
                    float[] offsets=getRotationsToOffset(x,-2,z,yaw,pitch);
                    lastGroundX=tick%11-5;lastGroundZ=tick%7-3;ticksSinceGrounded=tick%5;
                    mc.thePlayer.position=new Vec3(lastGroundX+0.5,tick%2==0?-62.1:64.5,lastGroundZ+0.5);
                    longTellySprintJump=tick%3!=0;longTellyNeedsSideUpdate=tick%4==0;
                    buildLongTellyQueue(new Vec3(x,0.2,z));
                    return new Object[]{quantizeMouseRotation(toYaw,yaw),unwrapYaw(toYaw,yaw),quantizeRotation(toYaw),
                        jitter,eased,offsets,getMovementYaw(yaw,(tick%3)-1,(tick%5)-2),updateCardinalSide(toYaw),
                        isDiagonalMovement(toYaw),rotationEaseSlowdown,yawJitterAmplitude,
                        Arrays.toString(queuedBlocks),Arrays.toString(queueRetryTicks),queuedBlockCount,pendingQueueIndex,
                        skippedQueueMask,queueCursor,queueStallTicks,longTellySide,longTellyOriginX,longTellyOriginZ,
                        longTellyForwardX,longTellyForwardZ,longTellyLateralX,longTellyLateralZ};
                }
            """;
}
