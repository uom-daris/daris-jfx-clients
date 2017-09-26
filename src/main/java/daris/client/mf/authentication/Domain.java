package daris.client.mf.authentication;

import arc.xml.XmlDoc;
import daris.client.mf.access.AccessToken;
import daris.client.mf.access.LocalAccessToken;
import daris.client.mf.access.Permission;

public class Domain extends Actor {
    public static final String RESOURCE_NAME = "domain";

    public enum Type {
        LOCAL("Local"), EXTERNAL("External"), PLUGIN("Plugin");

        private String _userValue;

        private Type(String userValue) {
            _userValue = userValue;
        }

        public String userValue() {
            return _userValue;
        }
    }

    public static final Authority AUTHORITY_MEDIAFLUX = new Authority("mediaflux", null);

    private Authority _authority;
    private String _description;
    private Type _type;
    private AccessToken _administer;

    public Domain(Authority authority, String name, Type type, String description) {
        this(authority, name, type, description, null);
    }

    public Domain(Authority authority, String name, Type type, String description, AccessToken administer) {
        super((authority == null) ? name : authority.toString() + ":" + name, RESOURCE_NAME);

        _authority = (authority == null) ? AUTHORITY_MEDIAFLUX : authority;

        _description = description;
        _type = type;
        _administer = administer;
    }

    public Domain(XmlDoc.Element xe) throws Throwable {
        super(xe.value("@name"), RESOURCE_NAME);

        _authority = authority(xe);

        _type = xe.value("@type").equals("external") ? Type.EXTERNAL
                : (xe.value("@type").equals("plugin") ? Type.PLUGIN : Type.LOCAL);
        _description = xe.value("description");
        _administer = new LocalAccessToken(RESOURCE_NAME, xe.value("@name"), Permission.ADMINISTER,
                xe.booleanValue("access/administer"));
    }

    public String description() {
        return _description;
    }

    public Type type() {
        return _type;
    }

    public String name() {
        return actorName();
    }

    public Authority authority() {
        return _authority;
    }

    public AccessToken administer() {
        return _administer;
    }

    public static Authority authority(XmlDoc.Element xe) throws Throwable {
        String authProto = xe.value("@protocol");
        String authName = xe.value("@authority");

        if (authName == null) {
            return AUTHORITY_MEDIAFLUX;
        } else if (authProto == null) {
            authProto = AUTHORITY_MEDIAFLUX.protocol();
        }

        return new Authority(authProto, authName);
    }
}
