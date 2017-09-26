package daris.client.mf.access;

public class AccessToken implements AccessControlledResource {

    public static class Key {
        private String _type;
        private String _name;

        public Key(String type, String name) {
            _type = type;
            _name = name;
        }

        public String type() {
            return _type;
        }

        public String name() {
            return _name;
        }

        public int hashCode() {
            return _name.hashCode();
        }

        public boolean equals(Object o) {
            Key ok = (Key) o;
            if (!type().equals(ok.type())) {
                return false;
            }

            if (!name().equals(ok.name())) {
                return false;
            }

            return true;
        }
    }

    private Key _key;
    private Permission _p;
    private boolean _loaded;
    private boolean _access;

    public AccessToken(String type, String name, Permission p) {
        _key = new Key(type, name);

        _p = p;
        _loaded = false;
        _access = false;
    }

    public Key key() {
        return _key;
    }

    public String type() {
        return _key.type();
    }

    public String name() {
        return _key.name();
    }

    public String resourceName() {
        return type() + " " + name();
    }

    public Permission permission() {
        return _p;
    }

    public boolean loaded() {
        return _loaded;
    }

    public boolean haveAccess() {
        return _access;
    }

    public void setHaveAccess(boolean access) {
        _access = access;
        _loaded = true;
    }

    public static boolean haveAccess(AccessControlledResource... resources) {
        for (int i = 0; i < resources.length; i++) {
            if (!resources[i].accessToken(null).haveAccess()) {
                return false;
            }
        }

        return true;
    }

    public AccessToken accessToken(Permission p) {
        if (p == null || p.equals(_p)) {
            return this;
        }

        return null;
    }

    public boolean have(Permission p) {
        AccessToken t = accessToken(p);

        assert t != null : "Permission not supported: " + p;

        return t.haveAccess();
    }
}