package daris.client.download.manifest;

public enum Parts {

    META, CONTENT, ALL;

    public static Parts fromString(String s, Parts def) {
        if (s != null) {
            Parts[] vs = values();
            for (Parts v : vs) {
                if (v.name().equalsIgnoreCase(s)) {
                    return v;
                }
            }
        }
        return def;
    }

}
