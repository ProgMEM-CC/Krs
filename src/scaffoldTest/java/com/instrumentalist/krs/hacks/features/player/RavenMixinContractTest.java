package com.instrumentalist.krs.hacks.features.player;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/** Checks hook descriptors against the actual 26.2 bytecode, without launching Minecraft. */
final class RavenMixinContractTest {
    static void run() throws Exception {
        int injections = 0;
        String manifest = RavenReferenceCompiler.source("src/client/resources/krs.mixins.json");
        for (String name : List.of("ChatBypassNetworkMixin", "DisplaceConnectionMixin", "DisplaceLevelRendererMixin")) {
            if (!manifest.contains('"' + name + '"')) throw new AssertionError("Unregistered mixin " + name);
            ClassNode mixin = read("com/instrumentalist/mixin/injector/" + name);
            AnnotationNode declaration = annotations(mixin.visibleAnnotations, mixin.invisibleAnnotations).stream()
                    .filter(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")).findFirst().orElseThrow();
            @SuppressWarnings("unchecked") List<Type> targets = (List<Type>) values(declaration).get("value");
            ClassNode targetClass = read(targets.getFirst().getInternalName());
            for (MethodNode handler : mixin.methods) {
                for (AnnotationNode annotation : annotations(handler.visibleAnnotations, handler.invisibleAnnotations)) {
                    Map<String, Object> params = values(annotation);
                    if (!params.containsKey("method")) continue;
                    @SuppressWarnings("unchecked") List<String> selectors = (List<String>) params.get("method");
                    for (String selector : selectors) {
                        List<MethodNode> candidates = targetClass.methods.stream().filter(m -> selector.contains("(")
                                ? (m.name + m.desc).equals(selector) : m.name.equals(selector)).toList();
                        if (candidates.size() != 1) throw new AssertionError("Ambiguous/missing 26.2 target " + selector);
                        MethodNode target = candidates.getFirst();
                        if (annotation.desc.endsWith("/Inject;")) {
                            Type[] actual = Type.getArgumentTypes(handler.desc), expected = Type.getArgumentTypes(target.desc);
                            if (actual.length != 1 && actual.length != expected.length + 1)
                                throw new AssertionError("Callback signature length mismatch: " + handler.name);
                            for (int i = 0; actual.length != 1 && i < expected.length; i++)
                                if (!actual[i].equals(expected[i])) throw new AssertionError("Callback signature mismatch: " + handler.name);
                        }
                        Object atValue = params.get("at");
                        List<?> ats = atValue instanceof List<?> list ? list : atValue == null ? List.of() : List.of(atValue);
                        for (Object at : ats) {
                            var atParams = values((AnnotationNode) at);
                            if (!"INVOKE".equals(atParams.get("value"))) continue;
                            String invocation = (String) atParams.get("target");
                            if (calls(target).stream().noneMatch(c -> ("L" + c.owner + ";" + c.name + c.desc).equals(invocation)))
                                throw new AssertionError("Missing 26.2 invocation target: " + invocation);
                        }
                        injections++;
                    }
                }
            }
        }
        ClassNode localPlayer = read("net/minecraft/client/player/LocalPlayer");
        ClassNode moduleManager = read("com/instrumentalist/krs/hacks/ModuleManager");
        MethodNode initialization = moduleManager.methods.stream().filter(m -> m.name.equals("onInitialize")).findFirst().orElseThrow();
        for (String module : List.of("combat/Displace", "player/ChatBypass"))
            if (calls(initialization).stream().noneMatch(c -> c.owner.equals("com/instrumentalist/krs/hacks/features/" + module) && c.name.equals("<init>")))
                throw new AssertionError("Module not registered: " + module);
        MethodNode aiStep = localPlayer.methods.stream().filter(m -> m.name.equals("aiStep") && m.desc.equals("()V")).findFirst().orElseThrow();
        if (calls(aiStep).stream().noneMatch(c -> c.owner.equals("net/minecraft/client/player/ClientInput") && c.name.equals("tick")))
            throw new AssertionError("Scaffold pre-update no longer precedes input generation in LocalPlayer.aiStep");
        String keyboard = RavenReferenceCompiler.source("src/client/java/com/instrumentalist/mixin/injector/KeyboardInputMixin.java");
        String normal = keyboard.substring(keyboard.indexOf("if (!GuiInputBlocker.shouldBlockGameMovement())"), keyboard.indexOf("Scaffold.hookImportedInput(input, true);", keyboard.indexOf("if (!GuiInputBlocker.shouldBlockGameMovement())")));
        ordered(normal, "Scaffold.hookImportedInput", "Displace.beforeMovementCorrection", "MovementFix.updateInput",
                "Scaffold.finishImportedInput", "Displace.afterMovementCorrection");
        System.out.println("26.2 mixin contracts: " + injections + " descriptors/injection targets plus input-phase ordering passed (not a live mixin launch)");
    }

    private static void ordered(String text, String... calls) {
        int cursor = 0;
        for (String call : calls) {
            int found = text.indexOf(call, cursor);
            if (found < 0) throw new AssertionError("Missing/out-of-order input hook " + call);
            cursor = found + call.length();
        }
    }

    private static ClassNode read(String name) throws Exception {
        try (var stream = RavenMixinContractTest.class.getClassLoader().getResourceAsStream(name + ".class")) {
            if (stream == null) throw new AssertionError("Missing bytecode " + name);
            ClassNode result = new ClassNode();
            new ClassReader(stream).accept(result, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return result;
        }
    }
    private static List<AnnotationNode> annotations(List<AnnotationNode> a, List<AnnotationNode> b) {
        List<AnnotationNode> result = new ArrayList<>();
        if (a != null) result.addAll(a);
        if (b != null) result.addAll(b);
        return result;
    }
    private static Map<String, Object> values(AnnotationNode annotation) {
        Map<String, Object> result = new HashMap<>();
        if (annotation.values != null) for (int i = 0; i < annotation.values.size(); i += 2)
            result.put((String) annotation.values.get(i), annotation.values.get(i + 1));
        return result;
    }
    private static List<MethodInsnNode> calls(MethodNode method) {
        List<MethodInsnNode> result = new ArrayList<>();
        for (var instruction : method.instructions) if (instruction instanceof MethodInsnNode call) result.add(call);
        return result;
    }
}
