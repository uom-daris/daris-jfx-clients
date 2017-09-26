package daris.client.mf.access;

public class Permission {
    public static final Permission ACCESS = new Permission("ACCESS");
    public static final Permission MODIFY = new Permission("MODIFY");
    public static final Permission ADMINISTER = new Permission("ADMINISTER");
    public static final Permission PUBLISH = new Permission("PUBLISH");
    public static final Permission CREATE = new Permission("CREATE");
    public static final Permission DESTROY = new Permission("DESTROY");
    public static final Permission EXECUTE = new Permission("EXECUTE");
    public static final Permission NONE = new Permission("NONE");

    private String _name;

    public Permission(String name) {
        _name = name;
    }

    public String name() {
        return _name;
    }

    public boolean equals(Permission o) {
        return name().equals(o.name());
    }

    public int hashCode() {
        return _name.hashCode();
    }

    public String toString() {
        return _name;
    }

    public static Permission byName(String value) {
        if (value.equalsIgnoreCase("access")) {
            return Permission.ACCESS;
        } else if (value.equalsIgnoreCase("modify")) {
            return Permission.MODIFY;
        } else if (value.equalsIgnoreCase("administer")) {
            return Permission.ADMINISTER;
        } else if (value.equalsIgnoreCase("publish")) {
            return Permission.PUBLISH;
        } else if (value.equalsIgnoreCase("create")) {
            return Permission.CREATE;
        } else if (value.equalsIgnoreCase("destroy")) {
            return Permission.DESTROY;
        } else if (value.equalsIgnoreCase("execute")) {
            return Permission.EXECUTE;
        } else if (value.equalsIgnoreCase("none")) {
            return Permission.NONE;
        }

        return new Permission(value);
    }
}
