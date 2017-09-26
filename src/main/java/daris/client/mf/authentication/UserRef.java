package daris.client.mf.authentication;

import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import daris.client.mf.object.ObjectRef;

public class UserRef extends ObjectRef<User> {

    public UserRef(XmlDoc.Element ue) {

    }

    @Override
    protected String resolveServiceName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void resolveServiceArgs(XmlWriter w) {
        // TODO Auto-generated method stub

    }

    @Override
    public String referentTypeName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String idToString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected User instantiate(Element re) {
        // TODO Auto-generated method stub
        return null;
    }

}
