package daris.client.mf.object;

public interface ObjectMessageResponse<T> {

    void responded(T r);
}
