package daris.client.mf.asset.lock;

public interface LockToken {

    int renewPeriodInSeconds();

    boolean renew();

    void release();

}
