package daris.util;

public class PathUtils {

    public static String SLASH = "/";

    public static String getParent(String path) {
        if (path == null) {
            return null;
        }
        String p = trimTrailingSlash(path);
        int idx = p.lastIndexOf(SLASH);
        if (idx == 0) {
            return p.substring(0, 1);
        } else if (idx > 0) {
            return p.substring(0, idx);
        } else {
            return p;
        }
    }

    public static String getName(String path) {
        if (path == null) {
            return null;
        }
        String p = trimTrailingSlash(path);
        int idx = p.lastIndexOf(SLASH);
        if (idx >= 0) {
            return p.substring(idx + 1);
        } else {
            return p;
        }
    }

    public static String trimTrailingSlash(String path) {
        if (path == null) {
            return path;
        }
        String p = path.trim();
        if (p.isEmpty()) {
            return p;
        }
        while (p.endsWith(SLASH)) {
            if (p.length() == 1) {
                return p;
            }
            p = p.substring(0, p.lastIndexOf(SLASH));
        }
        return p;
    }

    public static String trimLeadingSlash(String path) {
        if (path == null) {
            return path;
        }
        String p = path.trim();
        if (p.isEmpty()) {
            return p;
        }
        while (p.startsWith(SLASH)) {
            if (p.length() == 1) {
                return p;
            }
            p = p.substring(1);
        }
        return p;
    }

    public static String trimSlash(String path) {
        return trimTrailingSlash(trimLeadingSlash(path));
    }

    public static String join(String path1, String... paths) {
        StringBuilder sb = new StringBuilder();
        if (path1 != null && !path1.trim().isEmpty()) {
            sb.append(trimTrailingSlash(path1.trim()));
        }
        if (paths != null) {
            for (String path : paths) {
                String p = trimSlash(path);
                if (p != null && !p.isEmpty()) {
                    sb.append(SLASH).append(p);
                }
            }
        }
        return sb.toString();
    }

    public static String getFileExtension(String path) {
        if (path != null) {
            String name = getName(path);
            int idx = name.lastIndexOf('.');
            if (idx > 0) {
                return path.substring(idx + 1);
            }
        }
        return null;
    }

    public static String removeFileExtension(String path) {
        String ext = getFileExtension(path);
        if (ext != null) {
            return path.substring(0, path.length() - ext.length() - 1);
        }
        return path;
    }

}
