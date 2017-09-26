package daris.client.mf.object;

import java.util.Timer;
import java.util.TimerTask;

import daris.client.mf.asset.lock.LockToken;

public class ObjectLock {

    private boolean _granted;
    private LockToken _lt;
    private Timer _t;

    public ObjectLock() {
        _granted = false;
        _lt = null;
        _t = null;
    }

    public boolean acquired() {
        return _granted;
    }

    protected void grant(final LockToken lt) {
        if (!_granted) {
            _granted = true;
            if (lt != null && _lt == null) {
                _lt = lt;
                _t = new Timer();
                _t.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        _lt.renew();
                    }
                }, 0L, _lt.renewPeriodInSeconds() * 1000);
            }
        }
    }

    public void release() {
        if (_t != null) {
            _t.cancel();
            _t = null;
        }
        if (_lt != null) {
            _lt.release();
            _lt = null;
        }
        _granted = false;
    }
}
