package net.accela.ansi.sequence;

import net.accela.ansi.Patterns;
import net.accela.ansi.exception.ESCSequenceException;
import net.accela.util.RegexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * A class representing a single ANSI escape sequence.
 * This abstraction was implemented both as a security measure, and as a convenience tool.
 */
public class ESCSequence implements CharSequence {
    @NotNull
    protected String sequenceString = "";

    public ESCSequence() {
    }

    public ESCSequence(@NotNull String sequenceString) throws ESCSequenceException {
        validate(sequenceString, Patterns.ANSI8Bit);
        this.sequenceString = sequenceString;
    }

    static void validate(@NotNull String sequence, @NotNull Pattern pattern) throws ESCSequenceException {
        // Ensure there's no extra characters besides sequences in the string
        String filtered = RegexUtil.filterExcludeByPattern(sequence, pattern);

        if (!filtered.equals("")) {
            throw new ESCSequenceException(sequence,
                    "No extra characters besides the sequence itself are allowed. Proposed sequence: '" +
                            sequence + "', filtered sequence: '" + filtered + "'."
            );
        }

        // Ensure there's only one sequence in the string
        long matchCount = pattern.matcher(sequence).results().count();
        if (matchCount != 1) {
            throw new ESCSequenceException(sequence,
                    "The string needs to consist of a single sequence - no more, no less. Proposed sequence: '" +
                            sequence + "', matches: '" + matchCount + "'."
            );
        }
    }

    public enum ESCSequenceType {
        SS2, SS3, DCS, CSI, ST, OSC, SOS, PM, APC, RIS, UNKNOWN
    }

    public final @NotNull ESCSequenceType getESCSequenceType() {
        if (RegexUtil.testForMatch(sequenceString, Patterns.SS2)) {
            return ESCSequenceType.SS2;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.SS3)) {
            return ESCSequenceType.SS3;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.DCS)) {
            return ESCSequenceType.DCS;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.CSI)) {
            return ESCSequenceType.CSI;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.ST)) {
            return ESCSequenceType.ST;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.OSC)) {
            return ESCSequenceType.OSC;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.SOS)) {
            return ESCSequenceType.SOS;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.PM)) {
            return ESCSequenceType.PM;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.APC)) {
            return ESCSequenceType.APC;
        } else if (RegexUtil.testForMatch(sequenceString, Patterns.RIS)) {
            return ESCSequenceType.RIS;
        } else return ESCSequenceType.UNKNOWN;
    }

    @Override
    public int length() {
        return sequenceString.length();
    }

    @Override
    public char charAt(int i) {
        return sequenceString.charAt(i);
    }

    @Override
    public CharSequence subSequence(int i, int i1) {
        return sequenceString.subSequence(i, i1);
    }

    @NotNull
    @Override
    public String toString() {
        return sequenceString;
    }

    @Override
    public IntStream chars() {
        return sequenceString.chars();
    }

    @Override
    public IntStream codePoints() {
        return sequenceString.codePoints();
    }
}
