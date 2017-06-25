package daris.client.mf.asset;

import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class AssetUtils {

    public static XmlDoc.Element getAssetMeta(ServerClient.Connection cxn, AssetIdentifier id) throws Throwable {
        return getAssetMeta(cxn, id.assetId(), id.citeableId());
    }

    public static XmlDoc.Element getAssetMeta(ServerClient.Connection cxn, String assetId, String cid)
            throws Throwable {
        XmlStringWriter w = new XmlStringWriter();
        if (assetId != null) {
            w.add("id", assetId);
        } else {
            w.add("cid", cid);
        }
        return cxn.execute("asset.get", w.document()).element("asset");
    }

    public static String generatePath(Connection cxn, String assetId, String expr) throws Throwable {
        XmlStringWriter w = new XmlStringWriter();
        w.add("id", assetId);
        w.add("expr", expr);
        return cxn.execute("asset.path.generate", w.document()).value("path");
    }

}
