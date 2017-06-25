package daris.client.download.manifest;

import java.util.ArrayList;
import java.util.List;

import arc.xml.XmlDoc;
import daris.client.mf.asset.AssetIdentifier;

public class Identifier extends AssetIdentifier {

    private String _outputPath;

    public Identifier(String assetId, String citeableId, String outputPath) {
        super(assetId, citeableId);
        _outputPath = outputPath;
    }

    public Identifier(XmlDoc.Element e) throws Throwable {
        this("id".equals(e.name()) ? e.value() : null, "cid".equals(e.name()) ? e.value() : null, e.value("@path"));
    }

    public void setOutputPath(String outputPath) {
        _outputPath = outputPath;
    }

    public String outputPath() {
        return _outputPath;
    }

    public boolean valid() {
        return assetId() != null || citeableId() != null;
    }

    public static List<Identifier> instantiate(XmlDoc.Element me) throws Throwable {
        if (!me.elementExists("id") && !me.elementExists("cid")) {
            return null;
        }
        List<Identifier> ids = new ArrayList<Identifier>();
        if (me.elementExists("id")) {
            List<XmlDoc.Element> ides = me.elements("id");
            for (XmlDoc.Element ide : ides) {
                ids.add(new Identifier(ide));
            }
        }
        if (me.elementExists("cid")) {
            List<XmlDoc.Element> cides = me.elements("cid");
            for (XmlDoc.Element cide : cides) {
                ids.add(new Identifier(cide));
            }
        }
        return ids;
    }

}
