package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.hacks.Module;
import com.instrumentalist.krs.utils.value.ListValue;
import com.instrumentalist.krs.utils.value.SettingValue;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Set;

/** Headless contract checks. Placement, collision and physics still need an in-game test. */
public final class ScaffoldModeTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        checkCatalogs();
        checkSettings();
        checkConfigRoundTrip();
        checkInputMath();
        System.out.println("Scaffold mode checks passed: " + checks);
    }

    private static void checkCatalogs() throws Exception {
        String[] bridge = {"God Bridge", "Telly Short", "Telly Long", "Snap", "Rush", "Ninja", "Breezily", "Snap+", "Hypixel", "Hop"};
        String[] towers = {"Vanilla", "Sync", "Hypixel", "Apex", "Sync Burst", "Sync Early Burst",
                "Raven 1 Tick", "Raven Prediction", "OnGround", "OnGround Early", "OnGround Pulse",
                "OnGround Prediction", "Yuri NCP", "Raven Pull Sync", "Raven Pull", "Raven Predict Early",
                "Raven Predict Turbo", "Raven Prediction Swift", "Raven Prediction Rapid", "Polar", "Intave"};
        require(Arrays.equals(bridge, ScaffoldModeController.MODES), "Every source scaffold mode, in source order");
        require(Arrays.equals(towers, ScaffoldTowerController.MODES), "Every source tower mode, in source order");
        require(!ScaffoldModeController.handles(null) && !ScaffoldModeController.handles("Normal")
                && !ScaffoldModeController.handles("Telly"), "Native scaffold modes remain native");
        require(!ScaffoldTowerController.handles(null) && !ScaffoldTowerController.handles("NCP")
                && !ScaffoldTowerController.ownsJump("unknown"), "Unknown/legacy towers cannot own jump");

        ListValue mode = setting("mode");
        require(mode.get().equals("Normal") && mode.values.length == 12, "Preserve native default and both old modes");
        require(mode.contains("Normal") && mode.contains("Telly"), "Legacy saved mode names still load");
        for (String name : bridge) {
            require(ScaffoldModeController.handles(name.toLowerCase(Locale.ROOT)), "Case-insensitive mode: " + name);
            mode.set(name.toLowerCase(Locale.ROOT));
            require(mode.get().equals(name) && Scaffold.isImportedMode(), "Selectable canonical mode: " + name);
            require(!Scaffold.rotationMode.canDisplay.canDisplay(), "Do not expose conflicting native rotations: " + name);
        }
        mode.set("not a mode");
        require(mode.get().equals("Hop"), "Invalid saved value does not corrupt selected mode");
        mode.set("Normal");
        require(Scaffold.rotationMode.canDisplay.canDisplay(), "Native rotations remain available");

        ListValue tower = setting("towerMode");
        require(tower.get().equals("Vanilla") && tower.values.length == 24, "All source towers plus three native towers");
        Method resolver = Scaffold.class.getDeclaredMethod("sourceTowerMode");
        resolver.setAccessible(true);
        for (String name : towers) {
            String label = name.equals("Vanilla") || name.equals("Hypixel") ? "Source " + name : name;
            tower.set(label);
            require(name.equals(resolver.invoke(null)), "Source tower dispatch: " + label);
            require(ScaffoldTowerController.handles(name.toUpperCase(Locale.ROOT)), "Case-insensitive tower: " + name);
            require(ScaffoldTowerController.ownsJump(name) != name.equals("Vanilla"), "Jump ownership: " + name);
        }
        for (String legacy : new String[]{"Vanilla", "NCP", "Hypixel"}) {
            tower.set(legacy);
            require(tower.get().equals(legacy) && resolver.invoke(null) == null, "Preserve old tower config: " + legacy);
        }
        tower.set("Vanilla");
    }

    private static void checkSettings() throws Exception {
        Set<Object> registered = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        Set<String> names = new HashSet<>();
        for (Field field : Scaffold.class.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Module.Setting.class)) continue;
            field.setAccessible(true);
            Object value = field.get(null);
            require(value instanceof SettingValue<?>, "Native settings discovery: " + field.getName());
            SettingValue<?> setting = (SettingValue<?>) value;
            require(names.add(setting.name.toLowerCase(Locale.ROOT)), "Unique persisted key: " + setting.name);
            registered.add(value);
        }
        int sourceSettings = 0;
        for (Field field : ScaffoldModeController.class.getFields()) {
            if (!SettingValue.class.isAssignableFrom(field.getType())) continue;
            require(registered.contains(field.get(null)), "Source setting is actually persisted/exposed: " + field.getName());
            sourceSettings++;
        }
        require(sourceSettings == 35, "All 35 imported behavior settings are registered");
        checkOptions(ScaffoldModeController.keepY, "Off", "Telly A", "Telly B");
        checkOptions(ScaffoldModeController.sourceScaffoldMode, "Normal", "Telly", "Hypixel", "Breezily", "God Bridge");
        checkOptions(ScaffoldModeController.sourceSprintMode, "None", "Vanilla", "Legit", "NCP", "Universal");
        checkOptions(ScaffoldModeController.hopMode, "Sprint", "Low", "Boost", "Burst", "Adaptive");
    }

    private static void checkOptions(ListValue value, String... expected) {
        require(Arrays.equals(value.values, expected), "Sub-mode coverage: " + value.name);
        String original = value.get();
        for (String option : expected) {
            value.set(option);
            require(value.get().equals(option), "Sub-mode config round trip: " + value.name + "/" + option);
        }
        value.set(original);
    }

    private static void checkConfigRoundTrip() throws Exception {
        Scaffold scaffold = new Scaffold(); // Construct settings only; never enable or open a client.
        var original = scaffold.configObject.save().getFirst();
        ListValue mode = setting("mode");
        ListValue tower = setting("towerMode");
        for (String name : ScaffoldModeController.MODES) {
            mode.set(name);
            tower.set("Source Hypixel");
            ScaffoldModeController.keepY.set("Telly B");
            ScaffoldModeController.sourceScaffoldMode.set("God Bridge");
            ScaffoldModeController.sourceSprintMode.set("Universal");
            ScaffoldModeController.hopMode.set("Adaptive");
            ScaffoldModeController.sourceSneak.set(true);
            ScaffoldModeController.rushLookAhead.set(2.75f);
            ScaffoldModeController.ninjaMaxDelay.set(125);
            var saved = scaffold.configObject.save().getFirst();
            scaffold.configObject.load(original);
            require(mode.get().equals("Normal"), "Reset to native config before loading " + name);
            scaffold.configObject.load(saved);
            require(mode.get().equals(name) && tower.get().equals("Source Hypixel"), "JSON restores mode/tower " + name);
            require(ScaffoldModeController.keepY.get().equals("Telly B")
                    && ScaffoldModeController.sourceScaffoldMode.get().equals("God Bridge")
                    && ScaffoldModeController.sourceSprintMode.get().equals("Universal")
                    && ScaffoldModeController.hopMode.get().equals("Adaptive"), "JSON preserves sub-mode selections " + name);
            require(ScaffoldModeController.sourceSneak.get() && ScaffoldModeController.rushLookAhead.get() == 2.75f
                    && ScaffoldModeController.ninjaMaxDelay.get() == 125, "JSON preserves boolean/float/int options " + name);
        }
        scaffold.configObject.load(original);
        require(!scaffold.tempEnabled, "Headless config load never enabled the module");
    }

    private static ListValue setting(String name) throws Exception {
        Field field = Scaffold.class.getDeclaredField(name);
        field.setAccessible(true);
        return (ListValue) field.get(null);
    }

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void checkInputMath() throws Exception {
        Method yaw = ScaffoldModeController.class.getDeclaredMethod("inputYaw", float.class, float.class, float.class);
        Method vector = ScaffoldModeController.class.getDeclaredMethod("direction", float.class);
        Method moving = ScaffoldModeController.class.getDeclaredMethod("moving", Input.class);
        Method flags = ScaffoldModeController.class.getDeclaredMethod("flags", Input.class, boolean.class, boolean.class, boolean.class);
        Method direction = ScaffoldModeController.class.getDeclaredMethod("directionalInput", Input.class, float.class, float.class);
        for (Method method : new Method[]{yaw, vector, moving, flags, direction}) method.setAccessible(true);

        for (float camera : new float[]{0, 90, 179, -179, 360}) {
            for (int forward = -1; forward <= 1; forward++) {
                for (int left = -1; left <= 1; left++) {
                    if (forward == 0 && left == 0) continue;
                    float heading = (float) yaw.invoke(null, camera, (float) forward, (float) left);
                    Vec3 actual = (Vec3) vector.invoke(null, heading);
                    double radians = Math.toRadians(camera);
                    double length = Math.hypot(forward, left);
                    double expectedX = (left * Math.cos(radians) - forward * Math.sin(radians)) / length;
                    double expectedZ = (forward * Math.cos(radians) + left * Math.sin(radians)) / length;
                    require(Math.abs(actual.x - expectedX) < 1e-6 && Math.abs(actual.z - expectedZ) < 1e-6,
                            "Correct camera-relative movement for " + camera + "/" + forward + "/" + left);
                }
            }
        }
        Input opposed = new Input(true, true, true, true, false, false, false);
        require(!(boolean) moving.invoke(null, opposed), "Opposing keys cancel rather than activating a hop");
        require(!(boolean) moving.invoke(null, Input.EMPTY), "No automatic movement on empty input");
        Input raw = new Input(true, false, true, false, true, false, true);
        Input changed = (Input) flags.invoke(null, raw, false, true, false);
        require(changed.forward() && changed.left() && !changed.backward() && !changed.right(), "Sneak/sprint overrides preserve direction");
        require(!changed.jump() && changed.shift() && !changed.sprint(), "Jump and sprint suppression are reflected in input");
        Input weave = (Input) direction.invoke(null, raw, 1f, -0.62f);
        require(weave.forward() && weave.right() && !weave.left() && !weave.backward(), "Fractional weave signs are preserved");
        require(weave.jump() && !weave.shift() && weave.sprint(), "Weave does not discard physical action keys");
    }
}
