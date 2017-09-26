package daris.client.lifepool.dicom.filter;

import daris.client.lifepool.dicom.AttributeTag;

public class AttributeUtil {
    public static String getAttributeValueXPath(AttributeTag tag) {
        return "daris:dicom-dateset/object/de[@tag='" + tag + "']/value";
    }
}
