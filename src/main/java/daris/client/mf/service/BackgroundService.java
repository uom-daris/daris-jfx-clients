package daris.client.mf.service;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;
import daris.client.mf.model.task.UserOwnedTask;
import daris.client.mf.object.ObjectMessageResponse;
import daris.client.mf.service.messages.AbortBackgroundService;
import daris.client.mf.service.messages.DestroyBackgroundService;
import daris.client.mf.service.messages.ResumeBackgroundService;
import daris.client.mf.service.messages.SuspendBackgroundService;
import daris.client.mf.session.Session;

public class BackgroundService extends UserOwnedTask {
    public static final String TYPE_NAME = "Background Service";

    public static class ExCannotAbort extends Exception {
        /**
         * 
         */
        private static final long serialVersionUID = 6909932923854745204L;

        public ExCannotAbort(BackgroundService bs) {
            super("Aborting is not supported for background service (id): " + bs.id());
        }
    }

    public static class ExCannotSuspend extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = 4419809482072525484L;

        public ExCannotSuspend(BackgroundService bs) {
            super("Suspension is not supported for background service (id): " + bs.id());
        }
    }

    private String _name;
    private String _key;
    private String _description;
    private int _nbOut;
    private XmlDoc.Element _args;
    private long _completed;
    private long _failures;
    private String _activity;
    private String _error;
    private boolean _canAbort;
    private boolean _canSuspend;
    private List<String> _initialFailures;

    protected BackgroundService(XmlDoc.Element xe) throws Throwable {
        super(xe);

        _args = xe.element("service");
        _name = _args.value("@name");
        _key = xe.value("key");
        _description = xe.value("description");
        _nbOut = _args.intValue("@outputs");
        _activity = xe.value("activity");
        _completed = xe.longValue("completed", 0);
        _failures = xe.longValue("failed", 0);
        _error = xe.value("error");
        _canAbort = xe.booleanValue("can-abort");
        _canSuspend = xe.booleanValue("can-suspend");
        _initialFailures = null;
        if (xe.elementExists("failure")) {
            _initialFailures = new ArrayList<String>(xe.values("failure"));
        }
    }

    public String name() {
        return _name;
    }

    public String key() {
        return _key;
    }

    public String description() {
        return _description;
    }

    public XmlDoc.Element arguments() {
        return _args;
    }

    public int numberOfOutputs() {
        return _nbOut;
    }

    public void download(ServerClient.Output output) throws Throwable {

        XmlStringWriter w = new XmlStringWriter();
        w.add("id", id());

        Session.executeAsync("service.background.results.get", w.document(), null, output, null,
                "getting background service results");

    }

    public long numberSubOperationsCompleted() {
        return _completed;
    }

    public long numberOfFailures() {
        return _failures;
    }

    public List<String> failureStackTraces() {
        return _initialFailures;
    }

    public String currentActivity() {
        return _activity;
    }

    public String error() {
        return _error;
    }

    public boolean canSuspend() {
        return _canSuspend;
    }

    public void suspend() {
        suspend(null);
    }

    public void suspend(ObjectMessageResponse<Void> sr) {
        try {
            if (!canSuspend()) {
                throw new ExCannotSuspend(this);
            }
            new SuspendBackgroundService(this).send(sr);

        } catch (Throwable t) {
            Session.displayError(t.getMessage(), "suspending background service: " + name(), t, false);
        }
    }

    public void resume() {
        resume(null);
    }

    public void resume(ObjectMessageResponse<Void> rr) {
        if (!canSuspend()) {
            return;
        }
        new ResumeBackgroundService(this).send(rr);
    }

    public boolean canAbort() {
        return _canAbort;
    }

    public void abort() {
        abort(null);
    }

    public void abort(ObjectMessageResponse<Void> ar) {
        try {
            if (!canAbort()) {
                throw new ExCannotAbort(this);
            }
            new AbortBackgroundService(this).send(ar);
        } catch (Throwable t) {
            Session.displayError(t.getMessage(), "suspending background service: " + name(), t, false);
        }
    }

    public void destroy() {
        destroy(null);
    }

    public void destroy(ObjectMessageResponse<Void> dr) {
        new DestroyBackgroundService(this).send(dr);
    }

}
