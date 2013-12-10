package org.litesoft.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static String currentWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    public static BufferedWriter createWriter(File pFile, boolean pAppend)
            throws IOException {
        Objects.assertNotNull("File", pFile);
        return IOUtils.createWriter(new FileOutputStream(insureParent(pFile), pAppend));
    }

    public static BufferedReader createReader(File pFile)
            throws IOException {
        Objects.assertNotNull("File", pFile);
        return IOUtils.createReader(new FileInputStream(pFile));
    }

    public static File insureParent(File pExpectedFile)
            throws FileSystemException {
        Objects.assertNotNull("ExpectedFile", pExpectedFile);
        File zParentFile = pExpectedFile.getParentFile();
        if (zParentFile != null) {
            insure(zParentFile);
        }
        return pExpectedFile;
    }

    public static File insure(File pExpectedDir)
            throws FileSystemException {
        Objects.assertNotNull("ExpectedDir", pExpectedDir);
        if (!pExpectedDir.isDirectory()) {
            if (pExpectedDir.exists()) {
                throw new FileSystemException("Exists, but is Not a Directory: " + pExpectedDir.getAbsolutePath());
            }
            if (!pExpectedDir.mkdirs() || !pExpectedDir.isDirectory()) {
                throw new FileSystemException("Unable to create Directory: " + pExpectedDir.getAbsolutePath());
            }
        }
        return pExpectedDir;
    }

    /**
     * @return !null if file changed! - the ".bak" file
     */
    public static File storeTextFile(File pFile, String... pLines)
            throws FileSystemException {
        if (pFile.exists() && Strings.areEqual(loadTextFile(pFile), pLines)) {
            return null;
        }
        File file = new File(pFile.getAbsolutePath() + ".new");
        addLines(file, false, pLines);
        return rollIn(file, pFile, new File(pFile.getAbsolutePath() + ".bak"));
    }

    public static String[] loadTextFile(File pFile)
            throws FileSystemException {
        Objects.assertNotNull("File", pFile);
        try {
            if (!pFile.exists()) {
                throw new FileNotFoundException(pFile.getAbsolutePath());
            }
            return IOUtils.loadTextFile(createReader(pFile));
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    public static File rollIn(File pNewFile, File pTargetFile, File pBackupFile)
            throws FileSystemException {
        Objects.assertNotNull("NewFile", pNewFile);
        Objects.assertNotNull("TargetFile", pTargetFile);
        Objects.assertNotNull("BackupFile", pBackupFile);
        if (!pNewFile.exists()) {
            throw new FileSystemException("Does not Exist: " + pNewFile.getPath());
        }
        boolean targetExisted = false;
        if (pTargetFile.exists()) {
            targetExisted = true;
            deleteIfExists(pBackupFile);
            renameFromTo(pTargetFile, pBackupFile);
        }
        try {
            renameFromTo(pNewFile, pTargetFile);
        } catch (FileSystemException e) {
            if (targetExisted) {
                attemptToRollBack(pNewFile, pTargetFile, pBackupFile);
            }
            throw e;
        }
        return pBackupFile;
    }

    public static void deleteIfExists(File pFile)
            throws FileSystemException {
        Objects.assertNotNull("File", pFile);
        if (pFile.isFile()) {
            if (!pFile.delete() || pFile.exists()) {
                throw new FileSystemException("Unable to delete File: " + pFile.getAbsolutePath());
            }
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private static void attemptToRollBack(File pNewFile, File pTargetFile, File pBackupFile) {
        switch ((pNewFile.exists() ? 0 : 4) + (pTargetFile.exists() ? 0 : 2) + (pBackupFile.exists() ? 0 : 1)) {
            // What Happen'd
            case 0: // There: ------------- Nobody --------------
            case 2: // There:            pTargetFile
            case 4: // There: pNewFile
            case 6: // There: pNewFile & pTargetFile
            case 7: // There: pNewFile & pTargetFile & pBackupFile
                return;
            case 3: // There:            pTargetFile & pBackupFile
                pTargetFile.renameTo(pNewFile);
                // Fall Thru
            case 1: // There:                          pBackupFile
            case 5: // There: pNewFile               & pBackupFile
                pBackupFile.renameTo(pTargetFile);
                break;
        }
    }

    /**
     * This method will rename the pSourceFile to the pDestinationFile name.
     * <p/>
     * It is Inherently fragile when dealing with multiple processes playing
     * in the same file system name spaces.  It should therefore NOT be used
     * in a multi-process creation-consumption shared file system name space.
     * <p/>
     * Specifically there are two windows of opportunities for multiple processes
     * to mess with the "as linear" assumptions
     */
    private static void renameFromTo(File pSourceFile, File pDestinationFile)
            throws FileSystemException {
        Objects.assertNotNull("SourceFile", pSourceFile);
        Objects.assertNotNull("DestinationFile", pDestinationFile);
        // Win 1 Start
        if (!pSourceFile.exists()) {
            throw new FileSystemException("SourceFile does not exist: " + pSourceFile.getAbsolutePath());
        }
        if (pDestinationFile.exists()) {
            throw new FileSystemException("DestinationFile already exists: " + pDestinationFile.getAbsolutePath());
        }
        // Win 2 Start
        if (!pSourceFile.renameTo(pDestinationFile))    // Win 1 End
        {
            throw renameFailed(pSourceFile, pDestinationFile, "Failed");
        }
        boolean sThere = pSourceFile.exists();
        boolean dThere = pDestinationFile.exists();
        // Win 2 End
        if (sThere) {
            throw renameFailed(pSourceFile, pDestinationFile, "claims Succeess, but Source still there" + (dThere ? " and so is the Destination!" : "!"));
        }
        if (!dThere) {
            throw renameFailed(pSourceFile, pDestinationFile, "claims Succeess, but the Destination is NOT there!");
        }
    }

    private static FileSystemException renameFailed(File pSourceFile, File pDestinationFile, String pAdditionalExplanation) {
        throw new FileSystemException("Rename (" + pSourceFile.getAbsolutePath() + ") to (" + pDestinationFile.getAbsolutePath() + ") " + pAdditionalExplanation);
    }

    public static void writeLines(File pFile, boolean pAppend, String... pLines)
            throws FileSystemException {
        addLines(pFile, pAppend, pLines);
    }

    private static void addLines(File pFile, boolean pAppend, String... pLines)
            throws FileSystemException {
        try {
            addLines(createWriter(pFile, pAppend), pLines);
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
    }

    private static void addLines(BufferedWriter pWriter, String... pLines)
            throws IOException {
        boolean closed = false;
        try {
            for (String line : Strings.deNull(pLines)) {
                if (line != null) {
                    pWriter.write(line);
                    pWriter.write('\n');
                }
            }
            closed = true;
            pWriter.close();
        } finally {
            if (!closed) {
                Closeables.dispose(pWriter);
            }
        }
    }
}
