package daris.client.mf.service.messages;

import arc.xml.XmlDoc;
import arc.xml.XmlWriter;
import daris.client.mf.object.ObjectMessage;
import daris.client.mf.service.BackgroundService;
import daris.client.mf.session.Session;

public class AbortBackgroundService extends ObjectMessage<Void> {

    private long _id;

    public AbortBackgroundService(BackgroundService bs) {
        this(bs.id());
    }

    public AbortBackgroundService(long id) {
        _id = id;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        try {
            w.add("id", _id);
        } catch (Throwable e) {
            Session.displayError(e.getMessage(), "adding arugment for service: " + messageServiceName(), e, true);
        }
    }

    @Override
    protected String messageServiceName() {
        return "service.background.abort";
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
