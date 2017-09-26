package daris.client.mf.object;

import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlStringWriter;
import arc.xml.XmlWriter;
import daris.client.mf.asset.lock.LockToken;
import daris.client.mf.session.ServiceRequest;
import daris.client.mf.session.ServiceResultHandler;
import daris.client.mf.session.Session;

public abstract class ObjectRef<T> {

    private ServiceRequest _task;

    private T _obj;

    private ObjectLock _lock;

    protected abstract String resolveServiceName();

    protected abstract void resolveServiceArgs(XmlWriter w);

    public ServiceRequest resolve(ObjectResolveHandler<T> rh, boolean lock) {
        if (lock) {
            if (!supportLocking()) {
                throw new AssertionError("Locking is not supported. supportLocking() method need to be overridden.");
            }
            if (lock) {
                if (_lock == null) {
                    _lock = new ObjectLock();
                }
                if (!_lock.acquired()) {
                    _obj = null;
                }
            }
        }
        if (!needToResolve(lock)) {
            if (rh != null) {
                rh.resolved(_obj);
            }
            return _task;
        }
        cancel();
        XmlStringWriter w = new XmlStringWriter();
        resolveServiceArgs(w);
        String args = null;
        try {
            args = w.document();
        } catch (Throwable e) {
            Session.displayError(e.getMessage(), "setting arguments for service: " + resolveServiceName(), e, true);
        }
        _task = Session.executeAsync(resolveServiceName(), args, null, null, new ServiceResultHandler() {

            @Override
            public void handleResult(Element re) {

                if (re == null) {
                    _obj = null;
                } else {
                    _obj = instantiate(re);
                }
                if (_lock != null) {
                    if (supportLocking()) {
                        LockToken lockToken = re == null ? null : instantiateLockToken(re);
                        _lock.grant(lockToken);
                    } else {
                        _lock.grant(null);
                    }
                }
                if (rh != null) {
                    rh.resolved(_obj);
                }

            }
        }, "executing service: " + resolveServiceName());
        return _task;
    }

    public void cancel() {
        if (_task != null) {
            if (!_task.isDone()) {
                _task.cancel(true);
            }
            _task = null;
        }
        if (_lock != null) {
            _lock.release();
            _lock = null;
        }
    }

    public boolean resolved() {
        return _obj != null;
    }

    public boolean needToResolve() {
        return needToResolve(false);
    }

    public boolean needToResolve(boolean lock) {
        return !resolved() || (lock && (_lock == null || !_lock.acquired()));
    }

    public void reset() throws Throwable {
        cancel();
        if (_obj != null) {
            _obj = null;
        }
    }

    public abstract String referentTypeName();

    public T referent() {
        return _obj;
    }

    public abstract String idToString();

    protected abstract T instantiate(XmlDoc.Element re);

    protected LockToken instantiateLockToken(XmlDoc.Element re) {
        throw new AssertionError("Object reference " + referentTypeName()
                + " must implement instantiateLockToken(xe) for objects that were locked on retrieval.");
    }

    protected boolean supportLocking() {
        return false;
    }

}
