package daris.client.mf.asset.lock;

import daris.client.mf.asset.lock.messages.AssetSessionRelock;
import daris.client.mf.asset.lock.messages.AssetSessionUnlock;
import daris.client.mf.object.ObjectMessageResponse;

public class AssetLockToken implements LockToken {
    private String _id;
    private String _cid;
    private boolean _locked;

    public AssetLockToken(String id, String cid) {

        _id = id;
        _cid = cid;
        _locked = true;
    }

    @Override
    public void release() {
        if (locked()) {
            new AssetSessionUnlock(_id, _cid).send(new ObjectMessageResponse<Void>() {

                @Override
                public void responded(Void r) {
                    setLocked(false);
                }
            });
        }
    }

    private synchronized boolean locked() {
        return _locked;
    }

    private synchronized void setLocked(boolean locked) {
        _locked = locked;
    }

    @Override
    public boolean renew() {
        if (!locked()) {
            return false;
        }
        new AssetSessionRelock(_id, _cid, renewPeriodInSeconds()).send(new ObjectMessageResponse<Void>() {

            @Override
            public void responded(Void r) {
                setLocked(true);
            }
        });
        return true;
    }

    @Override
    public int renewPeriodInSeconds() {
        return 30;
    }
}