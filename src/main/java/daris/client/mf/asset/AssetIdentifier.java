package daris.client.mf.asset;

import daris.client.mf.util.CiteableIdUtils;

public class AssetIdentifier implements Comparable<AssetIdentifier> {

    private String _id;
    private String _cid;

    public AssetIdentifier(String id, boolean citeable) {
        if (citeable) {
            _cid = id;
        } else {
            _id = id;
        }
    }

    public AssetIdentifier(String assetId, String cid) {
        _id = assetId;
        _cid = cid;
    }

    public String assetId() {
        return _id;
    }

    public String citeableId() {
        return _cid;
    }

    public boolean isCiteableId() {
        return _cid != null;
    }

    public boolean isAssetId() {
        return _id != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof AssetIdentifier) {
            AssetIdentifier id = (AssetIdentifier) o;
            return (_id != null && _id.equals(id.assetId())) || (_cid != null && _cid.equals(id.citeableId()));
        }
        return false;
    }

    @Override
    public int compareTo(AssetIdentifier o) {
        if (_id != null) {
            if (o.assetId() != null) {
                if (_id.matches("^\\d+$") && o.assetId().matches("^\\d+$")) {
                    Long id1 = Long.parseLong(_id);
                    Long id2 = Long.parseLong(o.assetId());
                    return id1.compareTo(id2);
                } else {
                    return _id.compareTo(o.assetId());
                }
            } else {
                return 1;
            }
        } else {
            if (o.citeableId() != null) {
                return CiteableIdUtils.compare(_cid, o.citeableId());
            } else {
                return -1;
            }
        }
    }

}
