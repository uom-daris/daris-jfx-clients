package daris.client.mf.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import arc.exception.ThrowableUtil;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlStringWriter;
import daris.client.mf.session.ServiceRequest;
import daris.client.mf.session.ServiceResultHandler;
import daris.client.mf.session.Session;

public abstract class OrderedCollectionRef<T> {
    private Map<Integer, Region> _regions;
    private boolean _count;
    private long _total;
    private int _pageSize;

    public OrderedCollectionRef() {
        _regions = new HashMap<Integer, Region>();
        _count = false;
        _total = -1L;
        _pageSize = -1;
    }

    public OrderedCollectionRef(List<T> members) {
        (_regions = new HashMap<Integer, Region>(1)).put(0, new Region(members));
        _total = members.size();
        _count = false;
        _pageSize = -1;
    }

    public boolean canResolveEntireCollection() {
        return pagingSize() == -1;
    }

    public boolean countMembers() {
        return _count;
    }

    public void setCountMembers(boolean count) {
        _count = count;
    }

    public long totalNumberOfMembers() {
        if (_count) {
            return _total;
        }
        if (supportsPaging()) {
            return -1L;
        }
        Region r = _regions.get(0);
        if (r == null) {
            return -1L;
        }
        return r.size();
    }

    public void resolve(CollectionResolveHandler<T> rh) throws Throwable {
        if (!canResolveEntireCollection()) {
            throw new AssertionError(
                    (Object) "This collection requires paging. The entire collection cannot be retrieved in one request. Use the resolution that requests ranges of members.");
        }
        resolve(0L, Long.MAX_VALUE, rh);
    }

    public void resolve(long start, long end, CollectionResolveHandler<T> rh) throws Throwable {
        boolean count = false;
        if (_count && _total == -1L) {
            count = true;
        }
        List<Region> regions = regionsOverlapping(start, end - 1L);
        if (regions.size() == 1) {
            Region r = regions.get(0);
            int rstart = (int) (start % pagingSize());
            int rend = (end > 2147483647L) ? (rstart + (int) (2147483647L - start)) : (rstart + (int) (end - start));
            r.resolve(rstart, rend, count, rh);
            return;
        }
        MultiRegionRequest mrr = new MultiRegionRequest(regions, (int) (end - start));
        mrr.resolve(start, end, count, rh);
    }

    public boolean cached(T entry) {
        for (Region r : _regions.values()) {
            if (r.contains(entry)) {
                return true;
            }
        }
        return false;
    }

    private List<Region> regionsOverlapping(long start, long end) {
        if (!supportsPaging()) {
            Region r = _regions.get(0);
            if (r == null) {
                r = new Region(0L);
                _regions.put(0, r);
            }
            List<Region> regions = new ArrayList<Region>(1);
            regions.add(r);
            return regions;
        }
        int ps = pagingSize();
        int sr = (int) (start / ps);
        int er = (int) (end / ps);
        List<Region> rs = new ArrayList<Region>(er - sr + 1);
        for (int i = sr; i <= er; ++i) {
            Region r2 = _regions.get(i);
            if (r2 == null) {
                r2 = new Region(i * pagingSize());
                _regions.put(i, r2);
            }
            rs.add(r2);
        }
        return rs;
    }

    private void setTotal(long total) {
        _total = total;
    }

    public void reset() {
        cancel();
        _total = -1L;
        _regions.clear();
    }

    public void cancel() {
        Collection<Region> rs = _regions.values();
        if (rs != null) {
            Iterator<Region> it = rs.iterator();
            while (it.hasNext()) {
                it.next().cancel();
            }
        }
    }

    public boolean supportsPaging() {
        return false;
    }

    public int pagingSize() {
        if (_pageSize == -1) {
            return defaultPagingSize();
        }
        return _pageSize;
    }

    public int defaultPagingSize() {
        return 100;
    }

    protected abstract void resolveServiceArgs(XmlStringWriter w, long start, int size, boolean count) throws Throwable;

    protected abstract String resolveServiceName();

    protected abstract T instantiate(XmlDoc.Element xe);

    protected long total(XmlDoc.Element xe) {
        long total = 0L;
        try {
            total = xe.longValue("cursor/total", 0L);
        } catch (Throwable e) {
            ThrowableUtil.rethrowAsUnchecked(e);
        }
        return total;
    }

    protected abstract String referentTypeName();

    protected String idToString() {
        return "collection";
    }

    protected abstract String[] objectElementNames();

    public void copyMembersInto(OrderedCollectionRef<T> oc) {
        oc._regions.clear();
        for (Map.Entry<Integer, Region> me : _regions.entrySet()) {
            oc._regions.put(me.getKey(), me.getValue().duplicate());
        }
    }

    private class Region {
        private ServiceRequest _request;
        private long _start;
        private List<T> _members;
        private long _atime;

        public Region(long start) {
            _start = start;
            _members = null;
            _atime = System.currentTimeMillis();
            _request = null;
        }

        public Region(List<T> members) {
            _start = 0L;
            _members = members;
            _atime = System.currentTimeMillis();
            _request = null;
        }

        public Region duplicate() {
            Region rr = new Region(_start);
            if (_members != null) {
                rr._members = new ArrayList<T>((Collection<? extends T>) _members);
            }
            rr._atime = _atime;
            return rr;
        }

        public void resolve(int start, int end, boolean count, CollectionResolveHandler<T> rh) throws Throwable {
            if (_members != null) {
                if (start >= _members.size()) {
                    rh.resolved(null);
                    return;
                }
                int lend = end;
                if (lend > _members.size()) {
                    lend = _members.size();
                }
                rh.resolved(_members.subList(start, lend));
                return;
            }
            cancel();
            XmlStringWriter w = new XmlStringWriter();
            OrderedCollectionRef.this.resolveServiceArgs(w, _start, pagingSize(), count);
            _request = Session.executeAsync(OrderedCollectionRef.this.resolveServiceName(), w.document(), null, null,
                    new ServiceResultHandler() {

                        @Override
                        public void handleResult(Element xe) {
                            _request = null;
                            if (xe == null) {
                                rh.resolved(null);
                                return;
                            }
                            if (count) {
                                OrderedCollectionRef.this.setTotal(OrderedCollectionRef.this.total(xe));
                            }
                            String[] names = OrderedCollectionRef.this.objectElementNames();
                            for (int i = 0; i < names.length; ++i) {
                                try {
                                    List<XmlDoc.Element> oes = xe.elements(names[i]);
                                    if (oes != null) {
                                        int nes = oes.size();
                                        if (_members == null) {
                                            _members = new ArrayList<T>(nes);
                                        }
                                        for (int j = 0; j < oes.size(); ++j) {
                                            XmlDoc.Element oe = oes.get(j);
                                            T member = OrderedCollectionRef.this.instantiate(oe);
                                            _members.add(member);
                                        }
                                    }
                                } catch (Throwable e) {
                                    ThrowableUtil.rethrowAsUnchecked(e);
                                }
                            }
                            if (_members == null) {
                                _members = new ArrayList<T>(0);
                            }
                            if (_members.size() == 0) {
                                rh.resolved(_members);
                                return;
                            }
                            int lstart = start;
                            if (lstart > _members.size()) {
                                rh.resolved(null);
                                return;
                            }
                            int lend = end;
                            if (lend > _members.size()) {
                                lend = _members.size();
                            }
                            rh.resolved(_members.subList(start, lend));
                        }
                    }, "Retrieving collection members for collection " + idToString());
        }

        public boolean contains(T entry) {
            return _members.contains(entry);
        }

        public void cancel() {
            if (_request != null) {
                _request.cancel(true);
                _request = null;
            }
        }

        public long size() {
            if (_members == null) {
                return -1L;
            }
            return _members.size();
        }
    }

    private class MultiRegionRequest {
        private List<Region> _regions;
        private int _size;
        private int _remaining;

        public MultiRegionRequest(List<Region> regions, int size) {
            _regions = regions;
            _size = size;
            _remaining = regions.size();
        }

        public void resolve(long start, long end, boolean count, CollectionResolveHandler<T> rh) throws Throwable {
            List<T> resolved = new ArrayList<T>(_size);
            int pagingSize = OrderedCollectionRef.this.pagingSize();
            for (int i = 0; i < _regions.size(); ++i) {
                Region r = _regions.get(i);
                int rstart;
                if (i == 0) {
                    rstart = (int) (start % pagingSize);
                } else {
                    rstart = 0;
                }
                int rend;
                if (i == _regions.size() - 1) {
                    rend = (int) (end % pagingSize);
                    if (rend == 0 && end > 0L) {
                        rend = pagingSize;
                    }
                } else {
                    rend = pagingSize;
                }
                r.resolve(rstart, rend, count, new CollectionResolveHandler<T>() {
                    @Override
                    public void resolved(List<T> o) {
                        if (o != null) {
                            resolved.addAll(o);
                        }
                        if (--_remaining == 0) {
                            rh.resolved(resolved);
                        }
                    }
                });
                count = false;
            }
        }
    }

}
