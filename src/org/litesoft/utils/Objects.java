package org.litesoft.utils;

public class Objects<ArrayType> {
    public static final Object[] EMPTY_ARRAY = new Object[0];

    public static final String OBJECT_CLASS_NAME = Object.class.getName();

    private final int mArrayLength;
    private final ArrayType[] mArray;

    private Objects(int pArrayLength, ArrayType[] pArray) {
        mArrayLength = pArrayLength;
        mArray = pArray;
    }

    public boolean hasArray() {
        return (mArrayLength == -1);
    }

    public int getArrayLength() {
        return mArrayLength;
    }

    public ArrayType[] getArray() {
        return mArray;
    }

    public static <T> Objects<T> prepAppend(T[] pCurrent, T[] pAdditional) {
        int zAddLength = length(pAdditional);
        if ((pCurrent != null) && (0 == zAddLength)) {
            return new Objects<T>(-1, pCurrent);
        }
        int zCurLength = length(pCurrent);
        if ((pAdditional != null) && (0 == zCurLength)) {
            return new Objects<T>(-1, pAdditional);
        }
        return new Objects<T>(zCurLength + zAddLength, null);
    }

    public static int length(Object[] pArray) {
        return (pArray == null) ? 0 : pArray.length;
    }

    public static <T> T[] appendTo(T[] pTo, T[] pCurrent, T[] pAdditional) {
        assertNotNull("To", pTo);
        int zCurLength = length(pCurrent);
        int zAddLength = length(pAdditional);
        if (pTo.length < (zCurLength + zAddLength)) {
            throw new IllegalArgumentException("'To' array too short");
        }
        if (zCurLength != 0) {
            System.arraycopy(pCurrent, 0, pTo, 0, zCurLength);
        }
        if (zAddLength != 0) {
            System.arraycopy(pAdditional, 0, pTo, zCurLength, zAddLength);
        }
        return pTo;
    }

    public static RuntimeException nullValueException(String pWhat) {
        return new IllegalArgumentException(pWhat + " Not allowed to be null!");
    }

    public static void assertNull(String pWhat, Object pToCheck) {
        if (pToCheck != null) {
            throw new IllegalArgumentException(pWhat + " Should have been null, but was: " + pToCheck);
        }
    }

    public static <T> T assertNotNull(String pWhat, T pToCheck) {
        if (pToCheck == null) {
            throw nullValueException(pWhat);
        }
        return pToCheck;
    }

    public static boolean areNonArraysEqual(Object pThis, Object pThat) {
        return (pThis == pThat) || ((pThis != null) && pThis.equals(pThat));
    }

    /**
     * This method strips the package name off a fully qualified class name returning just the substring
     * beginning one character beyond the last ".".
     *
     * @return the substring beginning one character beyond the last "."; null or no "." just returns pFullyQualifiedClassName
     */
    public static String justClassName(String pFullyQualifiedClassName) {
        int zAt = (pFullyQualifiedClassName != null) ? pFullyQualifiedClassName.lastIndexOf('.') : -1;
        return (zAt != -1) ? pFullyQualifiedClassName.substring(zAt + 1) : pFullyQualifiedClassName;
    }

    /**
     * This method strips the package name off a fully qualified class name returning just the substring
     * beginning one character beyond the last ".".
     *
     * @return the substring beginning one character beyond the last "."; null or no "." just returns
     */
    public static String justClassName(Class<?> pClass) {
        return (pClass != null) ? justClassName(pClass.getName()) : null;
    }

    /**
     * This method strips the package name off the class name of the pObject returning just the substring
     * beginning one character beyond the last ".".
     *
     * @see Objects#justClassName(Class
     */
    public static String justClassNameOf(Object pObject) {
        return (pObject != null) ? justClassName(pObject.getClass()) : null;
    }

    public static <T> T deNull(T toTest, T defaultValue) {
        return (toTest != null) ? toTest : defaultValue;
    }

    public static boolean isNullOrEmpty(Object[] pArrayToCheck) {
        return ((pArrayToCheck == null) || (pArrayToCheck.length == 0));
    }

    public static <T> boolean oneOf(T toTest, T... options) {
        if ((toTest != null) && (options != null)) {
            for (T option : options) {
                if (toTest.equals(option)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> T oneOfToString(String toTest, T... options) {
        if ((toTest != null) && (options != null)) {
            for (T option : options) {
                if (option != null && toTest.equals(option.toString())) {
                    return option;
                }
            }
        }
        return null;
    }

    public static <T> T oneOfToStringIgnoreCase(String toTest, T... options) {
        if ((toTest != null) && (options != null)) {
            for (T option : options) {
                if ((option != null) && toTest.equalsIgnoreCase(option.toString())) {
                    return option;
                }
            }
        }
        return null;
    }

    public static String toString(Object pObject) {
        return (pObject != null) ? pObject.toString() : null;
    }

    public static int hashCodeFor(Object pObject) {
        return (pObject != null) ? pObject.hashCode() : 0;
    }
}
