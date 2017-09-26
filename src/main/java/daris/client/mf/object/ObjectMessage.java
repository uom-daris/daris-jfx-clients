package daris.client.mf.object;

import java.util.ArrayList;
import java.util.List;

import arc.exception.ThrowableUtil;
import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlStringWriter;
import arc.xml.XmlWriter;
import daris.client.mf.session.ServiceRequest;
import daris.client.mf.session.ServiceResultHandler;
import daris.client.mf.session.Session;

public abstract class ObjectMessage<T> {

    private ServiceRequest _r;
    private List<ServerClient.Input> _inputs;
    private ServerClient.Output _output;

    public ServiceRequest send() {
        return send(null, null);
    }

    public ServiceRequest send(ObjectMessageResponse<T> rh) {
        return send(null, rh);
    }

    public ServiceRequest send(ObjectLock lock, ObjectMessageResponse<T> rh) {
        cancel();
        String args = null;
        XmlStringWriter w = new XmlStringWriter();
        messageServiceArgs(w);
        try {
            args = w.document();
        } catch (Throwable e) {
            ThrowableUtil.rethrowAsUnchecked(e);
        }
        _r = Session.executeAsync(messageServiceName(), args, inputs(), output(), new ServiceResultHandler() {

            @Override
            public void handleResult(Element re) {

                _r = null;
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                if (rh != null) {
                    if (re == null) {
                        rh.responded(null);
                    } else {
                        rh.responded(instantiate(re));
                    }
                }

            }
        }, "Executing service: " + messageServiceName());
        return _r;
    }

    public void cancel() {
        if (_r != null) {
            _r.cancel(true);
            _r = null;
        }
    }

    public List<ServerClient.Input> inputs() {
        return _inputs;
    }

    public ObjectMessage<T> setInputs(List<ServerClient.Input> inputs) {
        if (_inputs != null) {
            _inputs.clear();
        }
        if (inputs != null) {
            if (_inputs == null) {
                _inputs = new ArrayList<ServerClient.Input>();
            }
            _inputs.addAll(inputs);
        }
        return this;
    }

    public ObjectMessage<T> addInput(ServerClient.Input input) {
        if (input != null) {
            if (_inputs == null) {
                _inputs = new ArrayList<ServerClient.Input>();
            }
            _inputs.add(input);
        }
        return this;
    }

    public ServerClient.Output output() {
        return _output;
    }

    public ObjectMessage<T> setOutput(ServerClient.Output output) {
        _output = output;
        return this;
    }

    protected abstract String messageServiceName();

    protected abstract void messageServiceArgs(XmlWriter w);

    protected abstract T instantiate(XmlDoc.Element re);

    public abstract String objectTypeName();

    public abstract String idToString();
}
