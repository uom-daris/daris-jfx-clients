package daris.client.mf.service.messages;

import arc.xml.XmlDoc;
import arc.xml.XmlWriter;
import daris.client.mf.object.ObjectMessage;
import daris.client.mf.service.BackgroundService;
import daris.client.mf.session.Session;

public class SuspendBackgroundService extends ObjectMessage<Void> {

    private long _id;

    public SuspendBackgroundService(BackgroundService bs) {
        this(bs.id());
    }

    public SuspendBackgroundService(long id) {
        _id = id;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        try {
            w.add("id", _id);
        } catch (Throwable e) {
            Session.displayError(e.getMessage(), "adding argument for service: " + messageServiceName(), e, true);
        }
    }

    @Override
    protected String messageServiceName() {
        return "service.background.suspend";
    }

    @Override
    protected Void instantiate(XmlDoc.Element xe) {
        return null;
    }

    @Override
    public String objectTypeName() {
        return BackgroundService.TYPE_NAME;
    }

    @Override
    public String idToString() {
        return String.valueOf(_id);
    }

}
