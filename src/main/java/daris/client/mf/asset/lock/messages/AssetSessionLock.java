package daris.client.mf.asset.lock.messages;

import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import daris.client.mf.object.ObjectMessage;
import daris.client.mf.session.Session;

public class AssetSessionLock extends ObjectMessage<Void> {

    private String _id;
    private String _cid;
    private int _timeout;

    public AssetSessionLock(String id, String cid, int timeout) {
        _id = id;
        _cid = cid;
        _timeout = timeout;
    }

    @Override
    protected String messageServiceName() {
        return "asset.session.lock";
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        try {
            if (_id != null) {
                w.add("id", _id);
            } else {
                w.add("cid", _cid);
            }
            w.add("timeout", _timeout);
        } catch (Throwable e) {
            Session.displayError(e.getMessage(), "settings arguments for service: " + messageServiceName(), e, true);
        }
    }

    @Override
    protected Void instantiate(Element re) {
        return null;
    }

    @Override
    public String objectTypeName() {
        return "Asset Session Lock";
    }

    @Override
    public String idToString() {
        return _id;
    }

}
