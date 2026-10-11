package com.instrumentalist.krs.hacks.features.player;

import javax.tools.ToolProvider;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/** Compiles selected UNMODIFIED reference methods against small API-only test doubles. */
final class RavenReferenceCompiler {
    static final Path ROOT = Path.of(System.getProperty("krs.referenceRoot"));

    static String source(String relative) throws Exception {
        return Files.readString(ROOT.resolve(relative), StandardCharsets.UTF_8);
    }

    static String method(String text, String name) {
        var matcher = Pattern.compile("(?m)^\\s*(?:(?:public|protected|private|static|final|synchronized)\\s+)*[\\w<>\\[\\].?]+\\s+"
                + Pattern.quote(name) + "\\s*\\([^;{}]*\\)\\s*\\{").matcher(text);
        if (!matcher.find()) throw new AssertionError("Reference method missing: " + name);
        int open = text.indexOf('{', matcher.start());
        int end = closingBrace(text, open);
        return text.substring(matcher.start(), end + 1);
    }

    static String body(String text, String name) {
        String method = method(text, name);
        return method.substring(method.indexOf('{') + 1, method.lastIndexOf('}'));
    }

    private static int closingBrace(String text, int open) {
        int depth = 0;
        boolean lineComment = false, blockComment = false, quoted = false, character = false, escaped = false;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i), next = i + 1 < text.length() ? text.charAt(i + 1) : 0;
            if (lineComment) { if (c == '\n') lineComment = false; continue; }
            if (blockComment) { if (c == '*' && next == '/') { blockComment = false; i++; } continue; }
            if (quoted || character) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (quoted && c == '"') quoted = false;
                else if (character && c == '\'') character = false;
                continue;
            }
            if (c == '/' && next == '/') { lineComment = true; i++; continue; }
            if (c == '/' && next == '*') { blockComment = true; i++; continue; }
            if (c == '"') { quoted = true; continue; }
            if (c == '\'') { character = true; continue; }
            if (c == '{') depth++;
            else if (c == '}' && --depth == 0) return i;
        }
        throw new AssertionError("Unterminated reference method");
    }

    static Class<?> compile(String name, String source) throws Exception {
        Path directory = Path.of("reference-fixtures").toAbsolutePath();
        Files.createDirectories(directory);
        Path file = directory.resolve(name + ".java");
        Files.writeString(file, source, StandardCharsets.UTF_8);
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new AssertionError("Reference tests require JDK 25, not a JRE");
        int result = compiler.run(null, null, null, "-encoding", "UTF-8", "-classpath",
                System.getProperty("java.class.path"), "-d", directory.toString(), file.toString());
        if (result != 0) throw new AssertionError("Could not compile reference fixture " + name);
        // Keep the directory loader alive until the test process exits: fixtures lazily
        // resolve their API-double nested classes after their outer class has loaded.
        var loader = new URLClassLoader(new java.net.URL[]{directory.toUri().toURL()}, RavenReferenceCompiler.class.getClassLoader());
        return Class.forName(name, true, loader);
    }
}
