package daris.client.lifepool.dicom;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttributeTag implements Comparable<AttributeTag> {

    private int _group;
    private int _element;

    public AttributeTag(int group, int element) {
        _group = group;
        _element = element;
    }

    public int group() {
        return _group;
    }

    public int element() {
        return _element;
    }

    @Override
    public final String toString() {
        return String.format("%04X%04X", _group, _element);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && (o instanceof AttributeTag)) {
            AttributeTag tag = (AttributeTag) o;
            return tag.group() == group() && tag.element() == element();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(AttributeTag tag) {
        if (tag == null) {
            return 1;
        }
        return toString().compareTo(tag.toString());
    }

    public static AttributeTag parse(String tag) throws Exception {
        if (tag == null || tag.trim().isEmpty()) {
            return null;
        }
        if (tag.length() != 8) {
            throw new Exception("Invalid tag: " + tag);
        }
        int group = Integer.parseInt(tag.substring(0, 4));
        int element = Integer.parseInt(tag.substring(4));
        return new AttributeTag(group, element);
    }

    public static final AttributeTag accessionNumber = new AttributeTag(0x0008, 0x0050);
    public static final AttributeTag imageType = new AttributeTag(0x0008, 0x0008);
    public static final AttributeTag modality = new AttributeTag(0x0008, 0x0060);
    public static final AttributeTag presentationIntentType = new AttributeTag(0x0008, 0x0068);
    public static final AttributeTag manufacturer = new AttributeTag(0x0008, 0x0070);
    public static final AttributeTag institutionName = new AttributeTag(0x0008, 0x0080);
    public static final AttributeTag seriesDescription = new AttributeTag(0x0008, 0x103E);
    public static final AttributeTag manufacturerModelName = new AttributeTag(0x0008, 0x1090);
    public static final AttributeTag acquisitionDeviceProcessingDescription = new AttributeTag(0x0018, 0x1400);
    public static final AttributeTag viewPosition = new AttributeTag(0x0018, 0x5101);
    public static final AttributeTag imageLaterality = new AttributeTag(0x0020, 0x0062);

    private static Map<String, AttributeTag> _tagFromName = new LinkedHashMap<String, AttributeTag>(11);
    private static Map<AttributeTag, String> _nameFromTag = new LinkedHashMap<AttributeTag, String>(11);
    static {
        _tagFromName.put("Accession Number", accessionNumber);
        _nameFromTag.put(accessionNumber, "Accession Number");

        _tagFromName.put("Image Type", imageType);
        _nameFromTag.put(imageType, "Image Type");

        _tagFromName.put("Modality", modality);
        _nameFromTag.put(modality, "Modality");

        _tagFromName.put("Presentation Intent Type", presentationIntentType);
        _nameFromTag.put(presentationIntentType, "Presentation Intent Type");

        _tagFromName.put("Manufacturer", manufacturer);
        _nameFromTag.put(manufacturer, "Manufacturer");

        _tagFromName.put("Institution Name", institutionName);
        _nameFromTag.put(institutionName, "Institution Name");

        _tagFromName.put("Series Description", seriesDescription);
        _nameFromTag.put(seriesDescription, "Series Description");

        _tagFromName.put("Manufacturer Model Name", manufacturerModelName);
        _nameFromTag.put(manufacturerModelName, "Manufacturer Model Name");

        _tagFromName.put("Acquisition Device Processing Description", acquisitionDeviceProcessingDescription);
        _nameFromTag.put(acquisitionDeviceProcessingDescription, "Acquisition Device Processing Description");

        _tagFromName.put("View Position", viewPosition);
        _nameFromTag.put(viewPosition, "View Position");

        _tagFromName.put("Image Laterality", imageLaterality);
        _nameFromTag.put(imageLaterality, "Image Laterality");
    }

    public static AttributeTag get(String name) {
        return _tagFromName.get(name);
    }

    public static String getName(AttributeTag tag) {
        return _nameFromTag.get(tag);
    }
}
