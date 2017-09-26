package daris.client.mf.asset.lock.messages;

import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import daris.client.mf.object.ObjectMessage;
import daris.client.mf.session.Session;

public class AssetSessionUnlock extends ObjectMessage<Void> {

    private String _id;
    private String _cid;

    public AssetSessionUnlock(String id, String cid) {
        _id = id;
        _cid = cid;
    }

    @Override
    protected String messageServiceName() {
        return "asset.session.unlock";
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        try {
            if (_id != null) {
                w.add("id", _id);
            } else {
                w.add("cid", _cid);
            }
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
