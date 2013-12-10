package org.litesoft.utils;

public class Strings {
    public static final String[] EMPTY_ARRAY = new String[0];

    @SuppressWarnings("StringEquality")
    public static boolean areEqual(String pStr1, String pStr2) {
        return (pStr1 == pStr2) || // Same String or both null
                ((pStr1 != null) && pStr1.equals(pStr2)) ||
                ((pStr2 != null) && pStr2.equals(pStr1));
    }

    public static boolean areEqual(String[] pStrings1, String... pStrings2) {
        if (pStrings1 != pStrings2) { // Not Same or not both null
            if ((pStrings1 == null) || (pStrings2 == null) || (pStrings1.length != pStrings2.length)) {
                return false;
            }
            for (int i = 0; i < pStrings1.length; i++) {
                if (!areEqual(pStrings1[i], pStrings2[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String[] deNull(String[] pLines) {
        return (pLines != null) ? pLines : EMPTY_ARRAY;
    }
}
