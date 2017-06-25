package daris.client.mf.object;

import java.util.List;

public interface CollectionResolveHandler<T> {
    void resolved(List<T> collection);
}
