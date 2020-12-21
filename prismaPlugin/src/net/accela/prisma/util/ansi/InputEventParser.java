package net.accela.prisma.util.ansi;

import net.accela.ansi.sequence.ESCSequence;
import net.accela.prisma.event.*;
import net.accela.prisma.geometry.Point;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * An input parser intended to make it easier to decode the otherwise raw input.
 * <p>
 * At most times it will return null, but if it does catch a sequence
 * then it will create an {@link InputEvent} from it and return that one.
 */
public class InputEventParser {
    final Plugin plugin;

    enum ParserState {
        GROUND,
        ESC,
        ANSI_IDENT,
        ANSI_MOUSE
    }

    @NotNull ParserState parserState = ParserState.GROUND;
    @NotNull String currentSequence = "";

    public InputEventParser(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * <pre>
     * Possible inputs:
     *  <char>                           -> char
     *  <esc> <nochar>                   -> esc
     *  <esc> <esc>                      -> esc
     *  <esc> <char>                     -> Alt-keypress or keycode sequence
     *  <esc> '[' <nochar>               -> Alt-[
     *  <esc> '[' (<num>) (';'<num>) '~' -> keycode sequence, <num> defaults to 1
     * </pre>
     */
    @Nullable
    public InputEvent parse(@NotNull final String singleEntry) {
        switch (parserState) {
            case GROUND:
                // ESC
                if (singleEntry.equals(ESCSequence.ESC_STRING)) {
                    parserState = ParserState.ESC;
                }
                // Normal input
                else {
                    return parseChars(singleEntry);
                }
                break;
            case ESC:
                // Double ESC means normal input with a single ESC character
                if (singleEntry.equals(ESCSequence.ESC_STRING)) {
                    parserState = ParserState.GROUND;
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.ESC);
                }
                // It's ANSI.
                // It could also be ALT[, but for the sake of code simplicity I've opted not to include that.
                else if (singleEntry.equals("[")) {
                    parserState = ParserState.ANSI_IDENT;
                }
                // It's ALT<singleEntry>
                else {
                    parserState = ParserState.GROUND;
                    return new StringInputEvent(plugin, singleEntry, false, false, true, false);
                }
                break;
            case ANSI_IDENT:
                // Add to history if it's not "[".
                // Some terminals like to add an extra one for some reason.
                if (singleEntry.equals("[")) break;
                currentSequence += singleEntry;

                // Check if the entry is a terminating character
                if (Character.isLetter(singleEntry.charAt(0)) || singleEntry.charAt(0) == '~') {
                    if (singleEntry.equals("M")) parserState = ParserState.ANSI_MOUSE;
                    else {
                        // Parse the sequence, reset and return
                        String sequence = currentSequence;
                        reset();
                        return parseANSI(sequence);
                    }
                }
                break;
            case ANSI_MOUSE:
                // Add to history
                currentSequence += singleEntry;

                // Check if the sequence is long enough to be complete
                if (currentSequence.length() == 4) {
                    String sequence = currentSequence;
                    reset();
                    return parseMouse(sequence);
                }
                break;
        }
        return null;
    }

    void reset() {
        parserState = ParserState.GROUND;
        currentSequence = "";
    }

    InputEvent parseChars(@NotNull final String sequence) {
        if (sequence.length() != 1) {
            return new StringInputEvent(plugin, sequence);
        } else {
            int charAsInt = sequence.charAt(0);
            switch (charAsInt) {
                case 0:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.NUL);
                case 1:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.SOH);
                case 2:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.STX);
                case 3:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.ETX);
                case 4:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.EOT);
                case 5:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.ENQ);
                case 6:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.ACK);
                case 7:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.BEL);
                case 8:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.BS);
                case 9:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.HT);
                case 10:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.LF);
                case 11:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.VT);
                case 12:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.FF);
                case 13:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.CR);
                case 14:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.SO);
                case 15:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.SI);
                case 16:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.DLE);
                case 17:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.DC1);
                case 18:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.DC2);
                case 19:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.DC3);
                case 20:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.DC4);
                case 21:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.NAK);
                case 22:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.SYN);
                case 23:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.ETB);
                case 24:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.CAN);
                case 25:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.EM);
                case 26:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.SUB);
                case 27:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.ESC);
                case 28:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.FS);
                case 29:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.GS);
                case 30:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.RS);
                case 31:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.US);
                case 127:
                    return new SpecialInputEvent(plugin, SpecialInputEvent.SpecialKey.DEL);
                default:
                    return new StringInputEvent(plugin, sequence);
            }
        }
    }

    MouseInputEvent parseMouse(@NotNull final String sequence) {
        // Netrunner spec
        if (sequence.charAt(1) == '0') {
            int x = netrunnerCharToInt(sequence.charAt(2)) - 1; // (-1) - Convert to a 0-based coordinate system
            int y = netrunnerCharToInt(sequence.charAt(3)) - 1; // (-1) - Convert to a 0-based coordinate system

            return new MouseInputEvent(plugin, x, y, MouseInputEvent.MouseInputType.LEFT);
        }
        // xterm spec, or at least assume so
        else {
            int x = xtermCharToInt(sequence.charAt(2)) - 1; // (-1) - Convert to a 0-based coordinate system
            int y = xtermCharToInt(sequence.charAt(3)) - 1; // (-1) - Convert to a 0-based coordinate system
            int mod = xtermCharToInt(sequence.charAt(1));

            // Mouse mod bits
            // --------------
            //              1
            //             2|
            //            4||
            //           8|||
            //         16||||
            //        32|||||
            //       64||||||
            //     128|||||||
            //       ||||||||
            //       vvvvvvvv
            //       00000000
            //     <---^--^-^
            //         |  | |
            // more mods  | |
            //    modifiers |
            //         button

            // Button options
            // 0 = MB1 pressed
            // 1 = MB2 pressed
            // 2 = MB3 pressed
            // 3 = released
            int button = mod & 0x3; // bit mask of 00000011

            // Whether SHIFT was held down or not.
            boolean shift = false;
            if (((mod >> 2) & 1) == 1) shift = true;

            // Whether META was held down or not.
            boolean meta = false;
            if (((mod >> 3) & 1) == 1) meta = true;

            // Whether CTRL was held down or not.
            boolean control = false;
            if (((mod >> 4) & 1) == 1) control = true;

            // Whether to use the first set of alternative buttons or not.
            boolean buttonsAlt1 = false;
            if (((mod >> 5) & 1) == 1) buttonsAlt1 = true;

            // Whether to use second set of alternative buttons or not.
            boolean buttonsAlt2 = false;
            if (((mod >> 6) & 1) == 1) buttonsAlt2 = true;

            // Whether this was a motion event or not.
            boolean motion = false;
            if (((mod >> 7) & 1) == 1) motion = true;

            // Calculate which alternative button set to use, if any.
            // If both one and two are enabled, then the third alternative button set will be used.
            if (buttonsAlt1) button += 1;
            if (buttonsAlt2) button += 2;

            // Return an InputEvent or null.
            if (motion) {
                return new MouseInputEvent(plugin, x, y, MouseInputEvent.MouseInputType.MOTION, shift, meta, control);
            } else {
                MouseInputEvent.MouseInputType mouseInputType = intToMouseInputEvent(button);
                if (mouseInputType == null) return null;
                else return new MouseInputEvent(plugin, x, y, mouseInputType, shift, meta, control);
            }
        }
    }


    @Nullable
    MouseInputEvent.MouseInputType intToMouseInputEvent(int mouseButton) {
        switch (mouseButton) {
            case 0:
                return MouseInputEvent.MouseInputType.LEFT;
            case 1:
                return MouseInputEvent.MouseInputType.MIDDLE;
            case 2:
                return MouseInputEvent.MouseInputType.RIGHT;
            case 3:
                return MouseInputEvent.MouseInputType.RELEASE;
            case 4:
                return MouseInputEvent.MouseInputType.MOTION;
            case 5:
                return MouseInputEvent.MouseInputType.SCROLL_UP;
            case 6:
                return MouseInputEvent.MouseInputType.SCROLL_DOWN;
            case 7:
                return MouseInputEvent.MouseInputType.SCROLL_LEFT;
            case 8:
                return MouseInputEvent.MouseInputType.SCROLL_RIGHT;
            case 9:
                return MouseInputEvent.MouseInputType.EXTRA_0;
            case 10:
                return MouseInputEvent.MouseInputType.EXTRA_1;
            case 11:
                return MouseInputEvent.MouseInputType.EXTRA_2;
            case 12:
                return MouseInputEvent.MouseInputType.EXTRA_3;
            default:
                return null;
        }
    }

    int xtermCharToInt(char chr) {
        return chr - 32;
    }

    int netrunnerCharToInt(char chr) {
        return chr - 40;
    }

    @Nullable
    InputEvent parseANSI(@NotNull final String sequence) {
        System.out.print("Sequence: [");
        char[] chars = sequence.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            System.out.print(chars[i] + (i == 0 ? "" : ","));
        }
        System.out.println("]");

        final @NotNull String keycode;
        final @NotNull String modifier;

        // Grab the last split item as a char
        char lastChar = sequence.charAt(sequence.length() - 1);

        // If the modifier comes last ( for example <esc>[14~ or <esc>[4;2~ )
        if (lastChar == '~') {
            String[] split = Pattern.compile("[;~]").split(sequence);

            keycode = split[0];

            // Don't try to add a modifier if there isn't one.
            if (split.length > 1) modifier = split[1];
            else modifier = "1";

            // We now have a keycode, and possibly a modifier as well.
            return parseSpecialKey(keycode, Integer.parseInt(modifier));
        }
        // If the modifier comes first ( for example <esc>[5C or <esc>[1;5C )
        else if (Character.isLetter(lastChar)) {
            String[] split;
            String importantPartOfSequence;

            // Cut the part that we don't need (wtf does the `1` in `<esc>[1;5C` even do?)
            if (sequence.contains(";")) {
                importantPartOfSequence = sequence.substring(sequence.indexOf(";") + 1);
            } else {
                importantPartOfSequence = sequence;
            }

            // Split it
            split = Pattern.compile("([0-9]|[A-Z]|[a-z])")
                    .matcher(importantPartOfSequence)
                    .results()
                    .map(MatchResult::group)
                    .toArray(String[]::new);

            // Don't try to add a modifier if there isn't one.
            if (split.length == 1) {
                keycode = split[0];
                modifier = "1";
            } else {
                keycode = split[1];
                modifier = split[0];
            }

            // We now have a keycode, and possibly a modifier as well.
            // If it's a Point
            if (lastChar == 'R') {
                return new PointInputEvent(plugin, new Point(Integer.parseInt(keycode), Integer.parseInt(modifier)));
            }
            // Else parse as normal
            else {
                return parseSpecialKey(keycode, Integer.parseInt(modifier));

            }
        } else {
            Logger.getLogger("InputEventParser").log(
                    Level.WARNING, "man, wtf is wrong with your input (\"" + sequence + "\")"
            );
            return null;
        }
    }

    SpecialInputEvent parseSpecialKey(@NotNull String keycode, int modifier) {
        SpecialInputEvent.SpecialKey specialKey = sequenceMap.get(keycode);
        // If there's no special key we don't need to continue
        if (specialKey == null) return null;

        // Order: { META, CTRL, ALT, SHIFT }.
        boolean[] modifierBits = new boolean[]{false, false, false, false};

        // Subtract 1 to create a valid bitmap
        modifier--;

        // Calculate which modifier keys were active
        for (int i = 3; i >= 0; i--) {
            modifierBits[3 - i] = ((modifier >> i) & 1) == 1;
        }

        // Return the resulting values
        return new SpecialInputEvent(
                plugin,
                specialKey,
                modifierBits[0],
                modifierBits[1],
                modifierBits[2],
                modifierBits[3]
        );
    }

    // A translation layer
    static final HashMap<String, SpecialInputEvent.SpecialKey> sequenceMap = new HashMap<>() {{
        put("A", SpecialInputEvent.SpecialKey.UP);
        put("B", SpecialInputEvent.SpecialKey.DOWN);
        put("C", SpecialInputEvent.SpecialKey.RIGHT);
        put("D", SpecialInputEvent.SpecialKey.LEFT);

        put("P", SpecialInputEvent.SpecialKey.F1);
        put("Q", SpecialInputEvent.SpecialKey.F2);
        put("R", SpecialInputEvent.SpecialKey.F3);
        put("S", SpecialInputEvent.SpecialKey.F4);

        put("F", SpecialInputEvent.SpecialKey.END);
        put("G", SpecialInputEvent.SpecialKey.KEYPAD_5);
        put("H", SpecialInputEvent.SpecialKey.HOME);

        put("1", SpecialInputEvent.SpecialKey.HOME);
        put("2", SpecialInputEvent.SpecialKey.INSERT);
        put("3", SpecialInputEvent.SpecialKey.DELETE);
        put("4", SpecialInputEvent.SpecialKey.END);
        put("5", SpecialInputEvent.SpecialKey.PGUP);
        put("6", SpecialInputEvent.SpecialKey.PGDN);
        put("7", SpecialInputEvent.SpecialKey.HOME);

        put("10", SpecialInputEvent.SpecialKey.F0);
        put("11", SpecialInputEvent.SpecialKey.F1);
        put("12", SpecialInputEvent.SpecialKey.F2);
        put("13", SpecialInputEvent.SpecialKey.F3);
        put("14", SpecialInputEvent.SpecialKey.F4);
        put("15", SpecialInputEvent.SpecialKey.F5);
        put("17", SpecialInputEvent.SpecialKey.F6);
        put("18", SpecialInputEvent.SpecialKey.F7);
        put("19", SpecialInputEvent.SpecialKey.F8);
        put("20", SpecialInputEvent.SpecialKey.F9);
        put("21", SpecialInputEvent.SpecialKey.F10);
        put("23", SpecialInputEvent.SpecialKey.F11);
        put("24", SpecialInputEvent.SpecialKey.F12);
        put("25", SpecialInputEvent.SpecialKey.F13);
        put("26", SpecialInputEvent.SpecialKey.F14);
        put("28", SpecialInputEvent.SpecialKey.F15);
        put("29", SpecialInputEvent.SpecialKey.F16);
        put("31", SpecialInputEvent.SpecialKey.F17);
        put("32", SpecialInputEvent.SpecialKey.F18);
        put("33", SpecialInputEvent.SpecialKey.F19);
        put("34", SpecialInputEvent.SpecialKey.F20);
    }};
}