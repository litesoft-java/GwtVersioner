package org.litesoft.utils;

public class Characters {
    public static final int NEWLINE = 10;
    public static final int DEL = 127;
    public static final int HIBIT_SPACE = 160;

    private static final String UNACCEPTABLE_NON_CONTROL_FILENAME_CHARACTERS = "|\\?*<\":>+[]/";

    public static boolean isUnacceptableNonControlFilenameChar(char c) {
        return (UNACCEPTABLE_NON_CONTROL_FILENAME_CHARACTERS.indexOf(c) != -1);
    }

    public static boolean isControlChar(char c) {
        return (c < ' ') || ((DEL <= c) && (c < HIBIT_SPACE));
    }

    public static boolean isDisplayable7BitAsciiAllowingSpaceAndNewline(char c) {
        return (c == NEWLINE) || ((' ' <= c) && (c < DEL));
    }

    public static boolean isAlphaNumericUnderScore7BitAscii(char c) {
        return isNumeric(c) || is7BitAlphaUnderScore(c);
    }

    public static boolean is7BitAlphaUnderScore(char c) {
        return (c == '_') || is7BitAlpha(c);
    }

    public static boolean is7BitAlphaNumeric(char c) {
        return isNumeric(c) || is7BitAlpha(c);
    }

    public static boolean is7BitAlpha(char c) {
        return ((('A' <= c) && (c <= 'Z')) || (('a' <= c) && (c <= 'z')));
    }

    public static boolean isUpperCaseAsciiAlpha(char pChar) {
        return ('A' <= pChar) && (pChar <= 'Z');
    }

    public static boolean isLowerCaseAsciiAlpha(char pChar) {
        return ('a' <= pChar) && (pChar <= 'z');
    }

    public static boolean isAsciiAlpha(char pChar) {
        return isUpperCaseAsciiAlpha(pChar) || isLowerCaseAsciiAlpha(pChar);
    }

    public static boolean isNumeric(char pChar) {
        return ('0' <= pChar) && (pChar <= '9');
    }

    private static final String ALPHA_BASE_26 = "abcdefghijklmnopqrstuvwxyz";

    public static int fromLowercaseAlphaBase26(char c) {
        return ALPHA_BASE_26.indexOf(c);
    }

    public static char toLowercaseAlphaBase26(int p0to25) throws IndexOutOfBoundsException {
        return ALPHA_BASE_26.charAt(p0to25);
    }

    private static final String BASE_36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static int fromBase36(char c) {
        return BASE_36.indexOf(c);
    }

    public static char toBase36(int p0to35) throws IndexOutOfBoundsException {
        return BASE_36.charAt(p0to35);
    }
}
