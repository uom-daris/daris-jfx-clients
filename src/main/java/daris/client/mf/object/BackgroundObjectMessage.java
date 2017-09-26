package daris.client.mf.object;

import java.util.List;

import arc.exception.ThrowableUtil;
import arc.mf.client.ServerClient;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlStringWriter;
import arc.xml.XmlWriter;
import daris.client.mf.session.ServiceRequest;
import daris.client.mf.session.ServiceResultHandler;
import daris.client.mf.session.Session;

public abstract class BackgroundObjectMessage {

    public static int DEFAULT_RETAIN_HOURS = 24;
    private boolean _sent;
    private ServiceRequest _request;
    private int _retainHours;
    private String _key;
    private String _description;
    private boolean _notify;
    protected Long _id;

    public BackgroundObjectMessage() {
        _sent = false;
        _request = null;
        _retainHours = 24;
        _key = null;
        _description = null;
        _notify = false;
    }

    public void setKey(String key) {
        _key = key;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public void setNotify(boolean notify) {
        _notify = notify;
    }

    public int retainHours() {
        return _retainHours;
    }

    public void setRetainHours(int hours) {
        _retainHours = hours;
    }

    public void send() throws Throwable {
        send(null);
    }

    public void send(BackgroundObjectMessageResponse rh) throws Throwable {
        if (_sent) {
            throw new AssertionError((Object) "Message has already been sent. Cannot resend");
        }
        _sent = true;
        cancel();
        XmlStringWriter w = new XmlStringWriter();
        w.add("background", new String[] { "key", _key, "retain", String.valueOf(_retainHours) }, true);
        if (_description != null) {
            w.add("description", _description);
        }
        if (_notify) {
            w.add("notify", true);
        }
        int nbServices = nbServices();
        if (nbServices < 1) {
            throw new Exception("At least one serivce must be executed.");
        }
        if (nbServices == 1) {
            w.push("service",
                    new String[] { "name", messageServiceName(), "outputs", String.valueOf(numberOfOutputs()) });
            messageServiceArgs(w);
            w.pop();
        } else {
            for (int i = 0; i < nbServices; ++i) {
                w.push("service", new String[] { "name", messageServiceName(i) });
                messageServiceArgs(w, i);
                w.pop();
            }
        }
        _request = Session.executeAsync("service.execute", w.document(), inputs(), null, new ServiceResultHandler() {

            @Override
            public void handleResult(Element re) {
                _request = null;
                if (rh != null) {
                    if (re == null) {
                        rh.responded(null);
                    } else {
                        Long id = null;
                        try {
                            id = re.longValue("id", null);
                        } catch (Throwable e) {
                            ThrowableUtil.rethrowAsUnchecked(e);
                        }
                        rh.responded(id);
                    }
                }
            }
        }, "submitting background task");
    }

    public void cancel() {
        if (_request != null) {
            _request.cancel(true);
            _request = null;
        }
    }

    protected abstract void messageServiceArgs(XmlWriter w);

    protected List<ServerClient.Input> inputs() {
        return null;
    }

    protected abstract String messageServiceName();

    protected int numberOfOutputs() {
        return 0;
    }

    protected abstract String objectTypeName();

    protected abstract String idToString();

    protected int nbServices() {
        return 1;
    }

    protected String messageServiceName(int i) {
        return null;
    }

    protected void messageServiceArgs(XmlWriter w, int t) throws Throwable {

    }

    public void destroy() throws Throwable {
        if (!_sent) {
            return;
        }
        destroy(_id);
    }

    public static void destroy(Long id) {
        if (id == null) {
            return;
        }
        destroy(id, null);
    }

    public static void destroy(long id, ObjectMessageResponse<Void> rh) {
        Session.executeAsync("service.background.destroy", "<id>" + id + "</id>", null, null,
                new ServiceResultHandler() {

                    @Override
                    public void handleResult(Element re) {
                        if (rh != null) {
                            rh.responded(null);
                        }
                    }
                }, "destroying background task " + id);
    }

}
