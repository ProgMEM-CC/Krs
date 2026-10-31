package com.instrumentalist.krs.hacks.features.player;

import java.util.List;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.instrumentalist.krs.hacks.Module;
import com.instrumentalist.krs.utils.value.SettingValue;

/** Headless checks for behavior extracted directly from the supplied BlockIn source. */
final class BlockInSourceTest {
    static void run() throws Exception {
        String source = RavenReferenceCompiler.source("BlockIn.java");
        checkSourceShape(source);
        checkRegistrationContract();
        checkNativeContract();

        Class<?> reference = compileHelpers(source);
        checkScalarHelpers(reference);
        checkDirectAdjacency(reference);

        System.out.println("BlockIn source checks passed: animation math and direct-placement boundaries");
    }

    private static void checkSourceShape(String source) {
        require(source.contains("public class BlockIn extends Module"),
                "BlockIn source declares the expected module type");
        require(source.contains("super(\"Block In\", category.player);"),
                "BlockIn source keeps the expected display name and player category");
        for (String setting : List.of("Speed", "Randomization", "Rotation Tolerance", "Select Keybind"))
            require(source.contains("new " + (setting.equals("Select Keybind") ? "KeySetting" : "SliderSetting")
                            + "(\"" + setting + "\""),
                    "BlockIn source keeps setting: " + setting);
    }

    private static void checkRegistrationContract() throws Exception {
        String manager = RavenReferenceCompiler.source(
                "src/client/java/com/instrumentalist/krs/hacks/ModuleManager.java");
        String registration = "new BlockIn()";
        int registrationIndex = manager.indexOf(registration);
        int devListIndex = manager.indexOf("devModules.addAll(List.of(");
        require(registrationIndex >= 0, "BlockIn is registered in the normal module list");
        require(devListIndex > registrationIndex,
                "BlockIn registration precedes the development-only module list");
    }

    private static void checkNativeContract() throws Exception {
        BlockIn module = new BlockIn();
        require(module.moduleName.equals("Block In"), "Native BlockIn keeps the source display name");

        Set<String> names = new HashSet<>();
        int settings = 0;
        for (Field field : BlockIn.class.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Module.Setting.class)) continue;
            field.setAccessible(true);
            Object value = field.get(module);
            require(value instanceof SettingValue<?>, "Native BlockIn setting value: " + field.getName());
            SettingValue<?> setting = (SettingValue<?>) value;
            require(names.add(setting.name.toLowerCase(Locale.ROOT)),
                    "Native BlockIn setting names are unique: " + setting.name);
            settings++;
        }
        require(settings == 6, "Native BlockIn exposes all source settings");
        require(names.containsAll(List.of("speed", "randomization", "rotation tolerance", "select keybind",
                        "ignore blocks", "items")),
                "Native BlockIn setting names match the source");
    }

    private static Class<?> compileHelpers(String source) throws Exception {
        StringBuilder fixture = new StringBuilder("""
                public class BlockInReference {
                    static class MC { final Player thePlayer = new Player(); }
                    static class Player { double posX, posY, posZ; }
                    static class BlockPos {
                        final int x, y, z;
                        BlockPos(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
                        int getX() { return x; }
                        int getY() { return y; }
                        int getZ() { return z; }
                    }
                    static class MathHelper {
                        static int floor_double(double value) { return (int) Math.floor(value); }
                    }
                    final MC mc = new MC();

                    boolean adjacent(double x, double y, double z, int px, int py, int pz) {
                        mc.thePlayer.posX = x;
                        mc.thePlayer.posY = y;
                        mc.thePlayer.posZ = z;
                        return isDirectAdjacentPlacement(new BlockPos(px, py, pz));
                    }
                """);
        for (String method : List.of("lerp", "quadInOutEasing", "sq", "clamp01", "isDirectAdjacentPlacement"))
            fixture.append(RavenReferenceCompiler.method(source, method));
        return RavenReferenceCompiler.compile("BlockInReference", fixture.append('}').toString());
    }

    private static void checkScalarHelpers(Class<?> reference) throws Exception {
        var lerp = reference.getDeclaredMethod("lerp", float.class, float.class, float.class);
        var easing = reference.getDeclaredMethod("quadInOutEasing", float.class);
        var square = reference.getDeclaredMethod("sq", double.class);
        var clamp = reference.getDeclaredMethod("clamp01", double.class);
        lerp.setAccessible(true);
        easing.setAccessible(true);
        square.setAccessible(true);
        clamp.setAccessible(true);

        for (float[] sample : new float[][]{{0f, 10f, 0f}, {0f, 10f, .25f}, {-4f, 8f, .5f}, {2f, -6f, 1f}}) {
            float expected = sample[0] + (sample[1] - sample[0]) * sample[2];
            float actual = (float) lerp.invoke(null, sample[0], sample[1], sample[2]);
            require(Float.floatToIntBits(expected) == Float.floatToIntBits(actual),
                    "Source lerp behavior");
        }

        float previous = Float.NEGATIVE_INFINITY;
        for (int i = 0; i <= 100; i++) {
            float t = i / 100f;
            float value = (float) easing.invoke(null, t);
            require(value >= previous && value >= 0f && value <= 1f,
                    "Source easing is monotonic and bounded at " + t);
            float mirror = (float) easing.invoke(null, 1f - t);
            require(Math.abs(value + mirror - 1f) < 1e-6f,
                    "Source easing symmetry at " + t);
            previous = value;
        }
        require((float) easing.invoke(null, 0f) == 0f && (float) easing.invoke(null, 1f) == 1f,
                "Source easing endpoints");

        require((double) square.invoke(null, -3.5) == 12.25, "Source square helper");
        for (double value : new double[]{-1, -Double.MIN_VALUE, 0, .25, 1, 2}) {
            double expected = value < 0 ? 0 : value > 1 ? 1 : value;
            require((double) clamp.invoke(null, value) == expected, "Source clamp helper: " + value);
        }
    }

    private static void checkDirectAdjacency(Class<?> reference) throws Exception {
        Object instance = reference.getConstructor().newInstance();
        var adjacent = reference.getDeclaredMethod(
                "adjacent", double.class, double.class, double.class, int.class, int.class, int.class);
        adjacent.setAccessible(true);

        double x = 1.2, y = 64.9, z = -2.1;
        require((boolean) adjacent.invoke(instance, x, y, z, 1, 66, -3),
                "Roof placement two blocks above the feet is direct");
        require((boolean) adjacent.invoke(instance, x, y, z, 2, 64, -3),
                "Horizontal placement at feet level is direct");
        require((boolean) adjacent.invoke(instance, x, y, z, 1, 65, -4),
                "Horizontal placement at head level is direct");

        for (int[] position : new int[][]{{1, 64, -3}, {2, 66, -3}, {2, 64, -2}, {1, 67, -3}})
            require(!(boolean) adjacent.invoke(instance, x, y, z, position[0], position[1], position[2]),
                    "Diagonal, same-cell and distant placements are not direct");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
