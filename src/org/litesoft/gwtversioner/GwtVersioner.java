package org.litesoft.gwtversioner;

import org.litesoft.utils.Characters;
import org.litesoft.utils.FileSystemException;
import org.litesoft.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GwtVersioner {
    public static final String SCRIPT_STARTSWITH = "<script src=\"";
    public static final String MIDDLE2VER = "/";
    public static final String VERSION = "ver.";
    public static final String SCRIPT_MIDDLE = MIDDLE2VER + VERSION + "js/";
    public static final String SCRIPT_FILE_TYPE = ".js#";
    public static final String SCRIPT_ENDSWITH = "\"></script>";

    public static void main(String[] args) {
        String zVersion = createVersionFromNow();
        System.out.println("GwtVersioner vs 1.2 -> " + zVersion);
        if (args.length > 1) {
            System.err.println("Too many arguments, expected only a directory reference");
            System.exit(1);
        }
        String zDirPath = (args.length == 0) ? "." : args[0];
        File zDir = new File(zDirPath);
        if (!zDir.isDirectory()) {
            System.err.println("Not a directory: " + zDir.getAbsolutePath());
            System.exit(1);
        }
        try {
            String zError = new GwtVersioner(zDir = zDir.getCanonicalFile(), zVersion).process();
            if (zError != null) {
                System.err.println(zError + " in directory: " + zDir.getAbsolutePath());
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error processing directory: " + zDir.getAbsolutePath());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // vvvvvvvvvvvvvvvvvvvvvvvvvv Ripped From Timestamps vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv

    private static String createVersionFromNow() { //  . . . . . . . . . . . .01234567890123456
        String zTS = new Timestamp(System.currentTimeMillis()).toString(); // yyyy-mm-dd hh:mm:ss.fffffffff
        return zTS.substring(0, 4) // Year
                + zTS.substring(5, 7) // Month
                + zTS.substring(8, 10) // Day
                + Characters.toLowercaseAlphaBase26(Integer.parseInt(zTS.substring(11, 13))) // Hours
                + Characters.toBase36(Integer.parseInt(zTS.substring(14, 16)) / 2) // Minutes
                ;
    }

    // ^^^^^^^^^^^^^^^^^^^^^^^^^^ Ripped From Timestamps ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    // vvvvvvvvvvvvvvvvvvvvvvvvvv Ripped From Old CopyVersioned vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv

    private static File adjustForVersionCopy(File pFile, String pVersion) {
        String zPath = pFile.getPath();
        int zAt = zPath.indexOf(".html");
        String zVersionedPath = zPath.substring(0, zAt) + "_" + pVersion + zPath.substring(zAt);
        return new File(zVersionedPath);
    }

    // ^^^^^^^^^^^^^^^^^^^^^^^^^^ Ripped From Old CopyVersioned ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    private final File mDir;
    private final String mVersion;
    private final String mVersionedDirName;
    private final Map<File, String[]> mHtmlFileContentsByFileToMutate = new HashMap<File, String[]>();
    private final List<File> mDirectoriesToMove = new ArrayList<File>();

    public GwtVersioner(File pDir, String pVersion) {
        mDir = pDir;
        mVersionedDirName = "ver" + (mVersion = pVersion);
    }

    private String process() throws IOException {
        populate();
        if (mDirectoriesToMove.isEmpty()) {
            return "No Directories found (of form 'ver.')";
        }
        if (mHtmlFileContentsByFileToMutate.isEmpty()) {
            return "No html files with '" + SCRIPT_STARTSWITH + "' + (. / ..) + '" + SCRIPT_MIDDLE +
                    "' + a...a + '" + SCRIPT_FILE_TYPE + "' + aa_AA + '" + SCRIPT_ENDSWITH + "' found in them";
        }
        updateHtmlFiles();
        moveDirectories();
        return null;
    }

    private void populate() {
        File[] zList = mDir.listFiles();
        if (zList != null) {
            for (File zEntry : zList) {
                if (zEntry.isFile()) {
                    checkForHtmlFile(zEntry);
                } else if (zEntry.isDirectory()) {
                    if (zEntry.getName().startsWith("ver.")) {
                        mDirectoriesToMove.add(zEntry);
                    } else {
                        checkSubDirForHtml(zEntry);
                    }
                }
            }
        }
    }

    private void checkSubDirForHtml(File pDir) {
        File[] zList = pDir.listFiles();
        if (zList != null) {
            for (File zEntry : zList) {
                if (zEntry.isFile()) {
                    checkForHtmlFile(zEntry);
                } else if (zEntry.isDirectory()) {
                    checkSubDirForHtml(zEntry);
                }
            }
        }
    }

    private void checkForHtmlFile(File pFile) {
        if (pFile.getName().toLowerCase().endsWith(".html")) {
            String[] zLines = FileUtils.loadTextFile(pFile);
            if (adjustedVersion(zLines)) {
                mHtmlFileContentsByFileToMutate.put(pFile, zLines);
            }
        }
    }

    private boolean adjustedVersion(String[] pLines) {
        for (int i = 0; i < pLines.length; i++) {
            if (adjustedVersion(pLines, i)) {
                return true;
            }
        }
        return false;
    }

    private boolean adjustedVersion(String[] pLines, int pLineOffset) {
        String zLine = pLines[pLineOffset];
        int zStartAt = zLine.indexOf(SCRIPT_STARTSWITH);
        if (zStartAt != -1) {
            int zDotsAt = zStartAt + SCRIPT_STARTSWITH.length();
            int zMiddleAt = zLine.indexOf(SCRIPT_MIDDLE, zDotsAt);
            if ((zMiddleAt != -1) && validateDots(zLine, zDotsAt, zMiddleAt)) {
                int zPostMiddleAt = zMiddleAt + SCRIPT_MIDDLE.length();
                int zFileTypeAt = zLine.indexOf(SCRIPT_FILE_TYPE, zPostMiddleAt);
                int zEndAt = zLine.indexOf(SCRIPT_ENDSWITH, zPostMiddleAt);
                if ((zFileTypeAt != -1) && (zEndAt != -1) && (zFileTypeAt < zEndAt)) {
                    int zLanguageAt = zFileTypeAt + SCRIPT_FILE_TYPE.length();
                    if (validateLanguage(zLine, zLanguageAt, zEndAt)) {
                        int zVersionAt = zMiddleAt + MIDDLE2VER.length();
                        int zPostVersion = zVersionAt + VERSION.length();
                        zLine = zLine.substring(0, zVersionAt) + mVersionedDirName + "/" + zLine.substring(zPostVersion);
                        pLines[pLineOffset] = zLine;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean validateDots(String pLine, int pFrom, int pUptoExclusive) {
        int zLength = pUptoExclusive - pFrom;
        if ((zLength < 1) || (zLength > 2)) {
            return false;
        }
        while (pFrom < pUptoExclusive) {
            if ('.' != pLine.charAt(pFrom++)) {
                return false;
            }
        }
        return true;
    }

    private boolean validateLanguage(String pLine, int pFrom, int pUptoExclusive) {
        int zLength = pUptoExclusive - pFrom;
        boolean rv = (zLength == 5);
        rv &= Characters.isLowerCaseAsciiAlpha(pLine.charAt(pFrom++));
        rv &= Characters.isLowerCaseAsciiAlpha(pLine.charAt(pFrom++));
        rv &= ('_' == pLine.charAt(pFrom++));
        rv &= Characters.isUpperCaseAsciiAlpha(pLine.charAt(pFrom++));
        rv &= Characters.isUpperCaseAsciiAlpha(pLine.charAt(pFrom));
        return rv;
    }

    private void updateHtmlFiles() {
        for (File zFile : mHtmlFileContentsByFileToMutate.keySet()) {
            String[] zLines = mHtmlFileContentsByFileToMutate.get(zFile);
            updateHtmlFile(zFile, zLines);
            updateHtmlFile(adjustForVersionCopy(zFile, mVersion), zLines);
        }
    }

    private void updateHtmlFile(File pFile, String[] pLines) {
        File zBakFile = FileUtils.storeTextFile(pFile, pLines);
        if (zBakFile == null) {
            throw new FileSystemException("Update to '" + pFile + "' did NOT produce a '.bak' file");
        }
        FileUtils.deleteIfExists(zBakFile);
    }

    private void moveDirectories() {
        File zVersionedDir = new File(mDir, mVersionedDirName);
        if (!zVersionedDir.mkdir()) {
            throw new FileSystemException("Unable to create dir '" + zVersionedDir + "'");
        }
        for (File zFile : mDirectoriesToMove) {
            moveDirectoryTo(zFile, zVersionedDir);
        }
    }

    private void moveDirectoryTo(File pFromVersionedDir, File pToVersionedDir) {
        String zName = pFromVersionedDir.getName().substring(VERSION.length());
        File zToDir = new File(pToVersionedDir, zName);
        if (!pFromVersionedDir.renameTo(zToDir)) {
            if (!pFromVersionedDir.renameTo(zToDir)) {
                if (!pFromVersionedDir.renameTo(zToDir)) {
                    throw new FileSystemException("Unable to move dir '" + pFromVersionedDir + "' to '" + zToDir + "'");
                }
            }
        }
    }
}