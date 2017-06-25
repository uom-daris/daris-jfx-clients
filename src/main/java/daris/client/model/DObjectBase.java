package daris.client.model;

import arc.xml.XmlDoc;

public abstract class DObjectBase implements DObject {

    private String _cid;
    private String _name;
    private String _description;
    private Type _type;

    protected DObjectBase(XmlDoc.Element oe) throws Throwable {
        if (oe != null) {
            _cid = oe.value("id");
            _name = oe.value("name");
            _description = oe.value("description");
            _type = Type.fromString(oe.value("@type"));
        }
    }

    protected DObjectBase(String cid, String name, String description, Type type) {
        _cid = cid;
        _name = name;
        _description = description;
        _type = type;
    }

    @Override
    public String cid() {
        return _cid;
    }

    @Override
    public String name() {
        return _name;
    }

    @Override
    public String description() {
        return _description;
    }

    @Override
    public Type type() {
        return _type;
    }

    public static DObject instantiate(XmlDoc.Element xe) {
        // TODO
        return null;
    }

}
