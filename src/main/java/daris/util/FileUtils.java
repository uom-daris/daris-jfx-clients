package daris.util;

import java.io.File;
import java.nio.file.Files;

public class FileUtils {

    public static String getFileExtension(File f) {
        return PathUtils.getFileExtension(f.getAbsolutePath());
    }

    public static void mkdirs(File dir, boolean renameExistingFileIfNameConflicts) throws Exception {
        if (dir.exists() && !dir.isDirectory()) {
            if (!renameExistingFileIfNameConflicts) {
                throw new Exception("Failed to make directory: " + dir.getAbsolutePath()
                        + ". There is a file with the same name in the directory.");
            } else {
                renameExistingFile(new File(dir.getAbsolutePath()));
            }
        }
        if (dir.exists()) {
            return;
        }
        File parentDir = dir.getParentFile();
        mkdirs(parentDir, renameExistingFileIfNameConflicts);
        Files.createDirectories(dir.toPath());
    }

    public static File renameExistingFile(File existingFile) {
        int i = 1;
        String inPath = existingFile.getAbsolutePath();
        String ext = PathUtils.getFileExtension(inPath);
        File outFile = null;
        boolean renamed = false;
        while (!renamed) {
            StringBuilder sb = new StringBuilder();
            if (ext != null) {
                sb.append(PathUtils.removeFileExtension(inPath));
            } else {
                sb.append(inPath);
            }
            sb.append("_").append(i);
            if (ext != null) {
                sb.append(".").append(ext);
            }
            String newPath = sb.toString();
            outFile = new File(newPath);
            if (!outFile.exists()) {
                renamed = existingFile.renameTo(outFile);
                if (!renamed) {
                    throw new RuntimeException("Failed to rename file:" + inPath);
                }
                break;
            } else {
                outFile = null;
            }
            i++;
        }
        return outFile;
    }

    public static File renameFileIfDirectoryWithSameNameExists(File inFile) {
        if (inFile.exists() && inFile.isDirectory()) {
            int i = 1;
            String inPath = inFile.getAbsolutePath();
            String ext = PathUtils.getFileExtension(inPath);
            while (true) {
                StringBuilder sb = new StringBuilder();
                if (ext != null) {
                    sb.append(PathUtils.removeFileExtension(inPath));
                } else {
                    sb.append(inPath);
                }
                sb.append("_").append(i);
                if (ext != null) {
                    sb.append(".").append(ext);
                }
                String newPath = sb.toString();
                File f = new File(newPath);
                if (!f.exists() || !f.isDirectory()) {
                    return f;
                }
                i++;
            }
        }
        return inFile;
    }

}
