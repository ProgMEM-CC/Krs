package com.instrumentalist.krs.hacks.features.player;

import java.util.Arrays;
import java.util.List;

/** The reference Chat Bypass transformation, independent of Minecraft and module state. */
public final class ChatBypassText {
    private static final List<String> FILTERED_WORDS = Arrays.asList(
            // blocked words; ordering and the duplicate entry are intentional
            "kill", "retard", "anal", "beaner", "bestiality", "blowjob", "cameltoe", "chink", "clit", "cock", "coon", "cunnilingus", "cunt", "dick", "dildo", "dilf", "dyke", "ejaculate", "ejaculati" /*ing & ion*/,
            "fag", "foreskin", "gilf", "hentai", "jerkoff", "jizz", "kike", "kill yourself", "kill urself", "kys", "loli", "masturbate", "masturbati" /*ing & ion*/, "milf", "nazi", "nigga", "nigger",
            "orgy", "pedo", "penis", "porn", "pussy", "rape", "raping", "redtube", "retard", "schlong", "shemale", "sex", "swastika", "tits", "titties", "trannie", "tranny", "vagina", "whore", "xhamster",
            "xvideos", "end",
            // censored words
            "arse", "ass", "bastard", "bitch", "boob", "douche", "fuck", "hitler", "shit", "twat", "wank"
    );

    private static final List<String> ALLOWED_COMMANDS = Arrays.asList(
            "ac", "achat", "pc", "pchat", "gc", "gchat", "shout", "msg", "message", "r", "reply", "t", "tell", "w", "whisper"
    );

    private ChatBypassText() {
    }

    /**
     * Transforms a plain message or a slash-prefixed command exactly as the supplied source.
     * Null is passed through as an explicit adaptation for callers outside the packet API.
     */
    public static String transform(String message, boolean onlyKnownWords) {
        if (message == null)
            return null;

        String[] split = splitCommand(message);
        if (split == null || split[1].isEmpty())
            return message;

        String msg = split[1];
        if (onlyKnownWords) {
            StringBuilder newMsg = new StringBuilder();
            String[] words = msg.split(" ");
            for (String word : words) {
                // Keep matching the original lowercase word even after replacements mutate word.
                // The default locale, first match per entry, split and trim mirror the reference.
                String lowerCaseWord = word.toLowerCase();
                for (String filteredWord : FILTERED_WORDS) {
                    int index = lowerCaseWord.indexOf(filteredWord.toLowerCase());
                    if (index != -1) {
                        String matched = word.substring(index, index + filteredWord.length()),
                                replaced = doReplace(matched);
                        word = word.substring(0, index) + replaced + word.substring(index + filteredWord.length());
                    }
                }
                newMsg.append(word).append(" ");
            }
            msg = newMsg.toString().trim();
        } else {
            msg = doReplace(msg);
        }

        if (split[0] != null)
            msg = split[0] + " " + msg;
        return msg;
    }

    /** Adapts the modern listener's slash-free command argument, removing exactly one slash. */
    public static String transformCommandInput(String command, boolean onlyKnownWords) {
        if (command == null)
            return null;
        return transform("/" + command, onlyKnownWords).substring(1);
    }

    private static String[] splitCommand(String msg) {
        if (msg.startsWith("/")) {
            if (!isValidCommand(msg))
                return null;
            int spaceIndex = msg.indexOf(" ");
            if (spaceIndex != -1) {
                return new String[]{
                        msg.substring(0, spaceIndex),
                        // All arguments, including a recipient, are intentionally transformed.
                        msg.substring(spaceIndex + 1)
                };
            }
        }
        return new String[]{null, msg};
    }

    private static String doReplace(String text) {
        // Uppercase I and Y deliberately use the same replacements as lowercase i and y.
        return text
                .replace("a", "\u00E1").replace("e", "\u00E9").replace("i", "\u00A1")
                .replace("o", "\u00F3").replace("u", "\u00FA").replace("y", "\u00FF")
                .replace("A", "\u00C1").replace("E", "\u00C9").replace("I", "\u00A1")
                .replace("O", "\u00D3").replace("U", "\u00DA").replace("Y", "\u00FF");
    }

    private static boolean isValidCommand(String msg) {
        for (String cmd : ALLOWED_COMMANDS) {
            String commandPrefix = "/" + cmd + " ";
            if (msg.startsWith(commandPrefix))
                return true;
        }
        return false;
    }
}
