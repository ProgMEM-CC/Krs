package com.instrumentalist.krs.hacks.features.player;

import com.instrumentalist.krs.hacks.features.combat.DisplaceMath;
import com.instrumentalist.krs.hacks.features.combat.Displace;
import com.instrumentalist.krs.hacks.ModuleManager;
import com.instrumentalist.krs.utils.value.BooleanValue;
import com.instrumentalist.krs.utils.value.TextValue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Differential tests execute the supplied source, not a second handwritten algorithm. */
final class RavenSourceParityTest {
    static void run() throws Exception {
        towerParity();
        RavenScaffoldAlgorithmTest.run();
        chatParity();
        displaceParity();
        moduleConfigChecks();
    }

    private static void towerParity() throws Exception {
        String source = RavenReferenceCompiler.source("Scaffold.java");
        String[] methods = {"updateTowerSession", "setTowerMotion", "updateTowerMotion", "towerFraction",
                "runSyncTower", "runPulseTower", "runHypixelTower", "runPulseVariant", "runApexTower",
                "runRaven1Tower", "runPolarTower", "runIntaveTower", "runRavenPredictionTower",
                "runRavenPredictionAcceleratedTower", "runRavenPredictionVariant", "setTowerPositionY",
                "runYuriNCPTower", "runRavenPullSyncTower", "runRavenPullTower", "runOnGroundTower",
                "runOnGroundEarlyTower", "runOnGroundPulseTower", "runOnGroundPredictionTower", "hasCornerSupport"};
        StringBuilder fixture = new StringBuilder(TOWER_FIXTURE);
        for (String method : methods) fixture.append(RavenReferenceCompiler.method(source, method));
        fixture.append('}');
        Class<?> reference = RavenReferenceCompiler.compile("RavenTowerReference", fixture.toString());
        Method run = reference.getMethod("run", ScaffoldTowerState.State.class, ScaffoldTowerState.Input.class,
                boolean.class, boolean.class, String.class, ScaffoldTowerState.Support.class);
        double[] boundaries = {-0.001, 0, 0.005, 0.035, 0.08, 0.1, 0.12, 0.14, 0.39, 0.42, 0.48,
                0.5, 0.64, 0.65, 0.68, 0.71, 0.72, 0.97, 0.98, 0.985, 0.99, 1, 1.001, 1.005, 1.015, 1.025, 1.04, 1.105, 1.26, 1.6};
        double[] velocities = {-0.8, -0.75, -0.1, -0.09, -0.08, -0.001, 0, 0.005, 0.1, 0.17, 0.28, 0.33, 0.42};
        Random random = new Random(0x26_2_5CAFF01DL);
        int scenarios = 0;
        for (String mode : ScaffoldTowerState.MODES) {
            for (int i = 0; i < 2000; i++) {
                double base = i % 3 == 0 ? -63 : i % 3 == 1 ? 64 : 319;
                double y = base + boundaries[random.nextInt(boundaries.length)];
                if (i % 7 == 0) y = Math.nextDown(y);
                if (i % 11 == 0) y = Math.nextUp(y);
                var input = i % 29 == 0 ? null : new ScaffoldTowerState.Input(
                        random.nextDouble() * 2 - 1, y, random.nextDouble() * 2 - 1,
                        velocities[random.nextInt(velocities.length)], random.nextBoolean(), i % 9 == 0);
                var seed = new ScaffoldTowerState.State(random.nextInt(7) - 1, random.nextInt(8),
                        random.nextBoolean() ? Integer.MIN_VALUE : (int) base + random.nextInt(4) - 1,
                        velocities[random.nextInt(velocities.length)], random.nextBoolean(), random.nextInt(7),
                        base + (i % 4 == 0 ? 0.5 : 0), random.nextBoolean());
                boolean sessionActive = i % 13 != 0, motionActive = i % 17 != 0;
                List<String> sourceQueries = new ArrayList<>(), portQueries = new ArrayList<>();
                int mask = random.nextInt();
                ScaffoldTowerState.Support originalSupport = (x, sy, z) -> support(sourceQueries, mask, x, sy, z);
                ScaffoldTowerState.Support portSupport = (x, sy, z) -> support(portQueries, mask, x, sy, z);
                Object[] expected = (Object[]) run.invoke(null, seed, input, sessionActive, motionActive, mode, originalSupport);
                var port = new ScaffoldTowerState(seed);
                boolean clear = port.updateTowerSession(sessionActive, input);
                var output = port.updateTowerMotion(mode, motionActive, input, portSupport);
                if (!expected[0].equals(port.state()) || !expected[1].equals(output) || !expected[2].equals(clear)
                        || !sourceQueries.equals(portQueries)) {
                    throw new AssertionError(mode + " case " + i + "\nseed=" + seed + " input=" + input
                            + "\nsource=" + java.util.Arrays.toString(expected) + "\nport=" + port.state() + " / " + output
                            + "\nqueries=" + sourceQueries + " / " + portQueries);
                }
                scenarios++;
            }
        }
        System.out.println("Tower differential parity: " + scenarios + " source-vs-port state/output/support-order scenarios across 21 modes");
    }

    private static boolean support(List<String> queries, int mask, int x, int y, int z) {
        queries.add(x + "," + y + "," + z);
        return ((x * 7340033 ^ y * 4099 ^ z * 19349663 ^ mask) & 3) == 0;
    }

    private static final String TOWER_FIXTURE = """
            import com.instrumentalist.krs.hacks.features.player.ScaffoldTowerState;
            public class RavenTowerReference {
                int towerTick, towerDelay, towerLastPlacedY, towerCycle;
                double towerPrevMotionY, towerLaunchY;
                boolean towerWasActive, towerSpoofGroundThisTick, motionChanged, positionChanged;
                Object rushPlannedPlacement = new Object();
                String selectedMode; boolean requestedActive, jumpPotion;
                ScaffoldTowerState.Support support;
                MC mc = new MC(); Client client = new Client();
                static class MC { EntityPlayerSP thePlayer; }
                class EntityPlayerSP {
                    double posX, posY, posZ, motionX, motionY, motionZ; boolean onGround;
                    Raw raw = new Raw();
                    class Raw { void setPosition(Vec3 v) { positionChanged = true; } }
                }
                static class Vec3 { double x,y,z; Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;} }
                class Client { void setMotion(double x,double y,double z){motionChanged=true;} }
                static class MathHelper {
                    static int floor_double(double v){int i=(int)v;return v<i?i-1:i;}
                    static double clamp_double(double v,double lo,double hi){return v<lo?lo:(v>hi?hi:v);}
                }
                boolean hasJumpPotion(){return jumpPotion;}
                boolean isTowerActive(){return requestedActive;}
                String getTowerMode(){return selectedMode;}
                boolean isSolidSupport(int x,int y,int z){return support.isSolidSupport(x,y,z);}
                public static Object[] run(ScaffoldTowerState.State s, ScaffoldTowerState.Input p,
                        boolean sessionActive, boolean motionActive, String mode, ScaffoldTowerState.Support support) {
                    var r=new RavenTowerReference();
                    r.towerTick=s.towerTick(); r.towerDelay=s.towerDelay(); r.towerLastPlacedY=s.towerLastPlacedY();
                    r.towerCycle=s.towerCycle(); r.towerPrevMotionY=s.towerPrevMotionY(); r.towerLaunchY=s.towerLaunchY();
                    r.towerWasActive=s.towerWasActive(); r.towerSpoofGroundThisTick=s.towerSpoofGroundThisTick();
                    r.selectedMode=mode; r.requestedActive=motionActive; r.support=support;
                    if(p!=null){r.mc.thePlayer=r.new EntityPlayerSP(); var q=r.mc.thePlayer;
                        q.posX=p.x();q.posY=p.y();q.posZ=p.z();q.motionY=p.motionY();q.onGround=p.onGround();r.jumpPotion=p.jumpPotion();}
                    r.updateTowerSession(sessionActive);
                    boolean cleared=r.rushPlannedPlacement==null;
                    r.updateTowerMotion();
                    var state=new ScaffoldTowerState.State(r.towerTick,r.towerDelay,r.towerLastPlacedY,r.towerPrevMotionY,
                            r.towerWasActive,r.towerCycle,r.towerLaunchY,r.towerSpoofGroundThisTick);
                    var q=r.mc.thePlayer;
                    var output=new ScaffoldTowerState.Output(r.motionChanged,q==null?0:q.motionY,r.positionChanged,
                            q==null?0:q.posY,r.towerSpoofGroundThisTick);
                    return new Object[]{state,output,cleared};
                }
            """;

    private static void chatParity() throws Exception {
        String source = RavenReferenceCompiler.source("ChatBypass.java")
                .replaceAll("(?m)^(?:package|import) [^;]+;", "")
                .replace("public class ChatBypass extends Module", "public class RavenChatReference")
                .replace("public ChatBypass()", "public RavenChatReference()")
                .replace("super(\"Chat Bypass\", category.other);", "")
                .replace("@SubscribeEvent", "");
        int end = source.lastIndexOf('}');
        String doubles = """
                void registerSetting(Object value){}
                static class ButtonSetting {boolean value;ButtonSetting(String name,boolean value){this.value=value;}boolean isToggled(){return value;}}
                static class Utils {static boolean nullCheck(){return true;}}
                static class C01PacketChatMessage {String value;C01PacketChatMessage(String value){this.value=value;}String getMessage(){return value;}}
                static class PacketUtils {static String last;static void sendPacketNoEvent(C01PacketChatMessage packet){last=packet.value;}}
                static class SendPacketEvent {Object packet;boolean cancelled;Object getPacket(){return packet;}void setCanceled(boolean value){cancelled=value;}}
                public static String run(String text,boolean known){var r=new RavenChatReference();r.filterKnownWords.value=known;
                    var event=new SendPacketEvent();event.packet=new C01PacketChatMessage(text);r.onSendPacket(event);
                    return event.cancelled?PacketUtils.last:text;}
                public static java.util.List<String> words(){return new RavenChatReference().filteredWords;}
                }
                """;
        Class<?> reference = RavenReferenceCompiler.compile("RavenChatReference", "import java.util.*;\n" + source.substring(0, end) + doubles);
        Method run = reference.getMethod("run", String.class, boolean.class);
        @SuppressWarnings("unchecked") List<String> words = (List<String>) reference.getMethod("words").invoke(null);
        List<String> messages = new ArrayList<>(List.of("", " ", "   ", "hello", "AEIOUY aeiouy", "friendly weekend",
                "a  b   c ", "\tline\t", "/msg", "/msg ", "/MSG Alex hello", "/msg\tAlex hello", "/tp Alex 0 64 0",
                "\u0130END", "emoji \uD83D\uDE00", "/r  ", "\nend\n"));
        for (String word : words) {
            messages.add(word);
            messages.add(word + word);
            messages.add(word.toUpperCase(Locale.ROOT));
            messages.add("prefix" + word + "suffix");
            messages.add("hello  " + word + "   goodbye ");
            messages.add("/msg Alex " + word);
            messages.add("/tp " + word);
            messages.add("/shout " + word.toUpperCase(Locale.ROOT));
        }
        Locale previous = Locale.getDefault();
        int comparisons = 0;
        try {
            for (Locale locale : List.of(Locale.US, Locale.forLanguageTag("tr-TR"), Locale.forLanguageTag("lt-LT"))) {
                Locale.setDefault(locale);
                for (boolean known : new boolean[]{true, false}) {
                    for (String message : messages) {
                        Object expected = outcome(() -> (String) run.invoke(null, message, known));
                        Object actual = outcome(() -> ChatBypassText.transform(message, known));
                        if (!expected.equals(actual)) throw new AssertionError("Chat source mismatch: locale=" + locale + " known=" + known);
                        if (message.startsWith("/")) {
                            Object command = outcome(() -> "/" + ChatBypassText.transformCommandInput(message.substring(1), known));
                            if (!expected.equals(command)) throw new AssertionError("Command adapter source mismatch");
                        }
                        comparisons++;
                    }
                }
            }
        } finally {
            Locale.setDefault(previous);
        }
        if (ChatBypassText.transform(null, true) != null) throw new AssertionError("Null adaptation");
        System.out.println("ChatBypass differential parity: " + comparisons + " source-vs-port cases (three locales, both settings, command adapter)");
    }

    @FunctionalInterface private interface TextCall { String run() throws Exception; }
    private static Object outcome(TextCall call) throws Exception {
        try { return "value:" + call.run(); }
        catch (InvocationTargetException e) { return e.getCause().getClass(); }
        catch (RuntimeException e) { return e.getClass(); }
    }

    private static void displaceParity() throws Exception {
        String source = RavenReferenceCompiler.source("Displace.java");
        StringBuilder fixture = new StringBuilder("""
                import java.util.*;
                public class RavenDisplaceReference {
                    static final int DISPLACE_WINDOW_TICKS=10;
                    Map<Integer,Integer> targetWindowStartTicks=new HashMap<>();
                    Slider delay=new Slider();
                    static class Slider {double value;double getInput(){return value;}}
                    static class EntityPlayer {int id;int getEntityId(){return id;}}
                    public boolean window(Integer id,int tick,double ms){delay.value=ms; EntityPlayer target=null;
                        if(id!=null){target=new EntityPlayer();target.id=id;}return shouldDisplaceInCurrentWindow(target,tick);}
                    public Map<Integer,Integer> windows(){return targetWindowStartTicks;}
                    public float yaw(double x,double z){return yawFromForward(x,z);}
                """);
        for (String name : List.of("msToTicks", "shouldDisplaceInCurrentWindow", "yawFromForward"))
            fixture.append(RavenReferenceCompiler.method(source, name));
        fixture.append('}');
        Class<?> reference = RavenReferenceCompiler.compile("RavenDisplaceReference", fixture.toString());
        Method window = reference.getMethod("window", Integer.class, int.class, double.class);
        Method windows = reference.getMethod("windows");
        Method yaw = reference.getMethod("yaw", double.class, double.class);
        int comparisons = 0;
        for (double delay : new double[]{-1, 0, 0.01, 49, 50, 51, 100, 450, 500}) {
            Object original = reference.getConstructor().newInstance();
            var port = new HashMap<Integer, Integer>();
            for (int tick = 0; tick < 65; tick++) {
                Integer id = tick % 13 == 0 ? null : tick % 3;
                boolean expected = (boolean) window.invoke(original, id, tick, delay);
                boolean actual = DisplaceMath.shouldDisplaceInCurrentWindow(port, id, tick, delay);
                if (expected != actual || !port.equals(windows.invoke(original))) throw new AssertionError("Displace window: " + tick + "/" + delay);
                comparisons++;
            }
        }
        Object original = reference.getConstructor().newInstance();
        for (double x : new double[]{-5, -1, -0.0, 0, 1, 5}) for (double z : new double[]{-5, -1, -0.0, 0, 1, 5}) {
            float expected = (float) yaw.invoke(original, x, z);
            if (Float.floatToIntBits(expected) != Float.floatToIntBits(DisplaceMath.yawFromForward(x, z)))
                throw new AssertionError("Displace yaw source mismatch");
            comparisons++;
        }
        for (int bits = 0; bits < 16; bits++) {
            boolean requireKB = (bits & 1) != 0, whitelist = (bits & 2) != 0, kb = (bits & 4) != 0, matches = (bits & 8) != 0;
            boolean expected = (!requireKB || kb) || (!whitelist || matches);
            if (DisplaceMath.passesItemCondition(requireKB, whitelist, kb, matches) != expected)
                throw new AssertionError("Source OR item-condition quirk was changed");
        }
        var geometry = DisplaceMath.arrowGeometry(0, 1, 0, 0.6, 0);
        if (geometry.triangles().size() != 12 || geometry.outline().size() != 6
                || Math.abs(geometry.outline().get(3).z() - 1.8) > 1e-12
                || DisplaceMath.arrowFadeAlpha(125) != 0.5f || DisplaceMath.arrowFadeAlpha(250) != 0f)
            throw new AssertionError("Displace arrow dimensions/fade");
        System.out.println("Displace differential parity: " + comparisons + " source-vs-port window/yaw cases; item truth table and arrow checks passed");
    }

    private static void moduleConfigChecks() throws Exception {
        ChatBypass chat = new ChatBypass();
        if (chat.tempEnabled || !chat.filterKnownWords.get()) throw new AssertionError("ChatBypass defaults");
        chat.filterKnownWords.set(false);
        var savedChat = chat.configObject.save().getFirst();
        chat.filterKnownWords.set(true);
        chat.configObject.load(savedChat);
        if (chat.filterKnownWords.get() || chat.tempEnabled) throw new AssertionError("ChatBypass config round-trip");

        Displace displace = new Displace();
        TextValue whitelist = null;
        BooleanValue enabledWhitelist = null;
        for (var field : ModuleManager.getSettings(displace)) {
            Object value = field.get(displace);
            if (value instanceof TextValue text && text.name.equals("Whitelisted items")) whitelist = text;
            if (value instanceof BooleanValue bool && bool.name.equals("Item whitelist")) enabledWhitelist = bool;
        }
        if (whitelist == null || enabledWhitelist == null || displace.tempEnabled || !whitelist.get().isEmpty())
            throw new AssertionError("Displace native setting registration/defaults");
        whitelist.set("minecraft:stone, minecraft:diamond_sword");
        enabledWhitelist.set(true);
        var savedDisplace = displace.configObject.save().getFirst();
        whitelist.set(""); enabledWhitelist.set(false);
        displace.configObject.load(savedDisplace);
        if (!whitelist.get().equals("minecraft:stone, minecraft:diamond_sword") || !enabledWhitelist.get() || displace.tempEnabled)
            throw new AssertionError("Displace config round-trip");
        System.out.println("Displace and ChatBypass native settings/defaults/JSON round-trips passed without enabling either module");
    }
}
