package daris.client.mf.util;

public class CiteableIdUtils {

    public static int compare(String cid1, String cid2) {
        if (cid1 == null && cid2 == null) {
            return 0;
        }

        if (cid1 == null && cid2 != null) {
            if (!cid2.matches("^\\d+(\\d*.)*\\d+$")) {
                throw new RuntimeException("Invalid cid: " + cid2);
            }
            return -1;
        }

        if (cid1 != null && cid2 == null) {
            if (!cid1.matches("^\\d+(\\d*.)*\\d+$")) {
                throw new RuntimeException("Invalid cid: " + cid1);
            }
            return 1;
        }

        if (!cid1.matches("^\\d+(\\d*.)*\\d+$")) {
            throw new RuntimeException("Invalid cid: " + cid1);
        }

        if (!cid2.matches("^\\d+(\\d*.)*\\d+$")) {
            throw new RuntimeException("Invalid cid: " + cid2);
        }

        if (cid1.equals(cid2)) {
            return 0;
        }

        String[] parts1 = cid1.split("\\.");
        String[] parts2 = cid2.split("\\.");
        if (parts1.length < parts2.length) {
            return -1;
        }
        if (parts1.length > parts2.length) {
            return 1;
        }
        for (int i = 0; i < parts1.length; i++) {
            if (!parts1[i].equals(parts2[i])) {
                long n1 = Long.parseLong(parts1[i]);
                long n2 = Long.parseLong(parts2[i]);
                if (n1 < n2) {
                    return -1;
                }
                if (n1 > n2) {
                    return 1;
                }
            }
        }
        return 0;
    }

}
