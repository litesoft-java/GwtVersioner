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
    public static final String SCRIPT_START = "<script src=\"";
    public static final String MIDDLE2VER = "/";
    public static final String VERSION = "ver.";
    public static final String SCRIPT_MIDDLE = MIDDLE2VER + VERSION + "js/";
    public static final String SCRIPT_END = ".js\"></script>";

    public static void main(String[] args) {
        System.out.println("GwtVersioner vs 1.0");
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
            String zError = new GwtVersioner(zDir = zDir.getCanonicalFile(), "ver" + createVersionFromNow()).process();
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

    private final File mDir;
    private final String mVersionedDirName;
    private final Map<File, String[]> mHtmlFileContentsByFileToMutate = new HashMap<File, String[]>();
    private final List<File> mDirectoriesToMove = new ArrayList<File>();

    public GwtVersioner(File pDir, String pVersionedDirName) {
        mDir = pDir;
        mVersionedDirName = pVersionedDirName;
    }

    private String process() throws IOException {
        populate();
        if (mDirectoriesToMove.isEmpty()) {
            return "No Directories found (of form 'ver.')";
        }
        if (mHtmlFileContentsByFileToMutate.isEmpty()) {
            return "No html files with '<script src=\"' + (. / ..) '/ver.js/' + aa + '.js\"></script>' found in them";
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
        int zStartAt = zLine.indexOf(SCRIPT_START);
        if (zStartAt != -1) {
            int zDotsAt = zStartAt + SCRIPT_START.length();
            int zMiddleAt = zLine.indexOf(SCRIPT_MIDDLE, zDotsAt);
            if (zMiddleAt != -1) {
                int zLanguageAt = zMiddleAt + SCRIPT_MIDDLE.length();
                int zEndAt = zLine.indexOf(SCRIPT_END, zLanguageAt);
                if (zEndAt != -1) {
                    if (validateDots(zLine, zDotsAt, zMiddleAt) && validateLanguage(zLine, zLanguageAt, zEndAt)) {
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
        if (zLength != 2) {
            return false;
        }
        while (pFrom < pUptoExclusive) {
            if (!Characters.isLowerCaseAsciiAlpha(pLine.charAt(pFrom++))) {
                return false;
            }
        }
        return true;
    }

    private void updateHtmlFiles() {
        for (File zFile : mHtmlFileContentsByFileToMutate.keySet()) {
            updateHtmlFile(zFile, mHtmlFileContentsByFileToMutate.get(zFile));
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