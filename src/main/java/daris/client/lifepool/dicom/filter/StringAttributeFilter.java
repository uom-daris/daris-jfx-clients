package daris.client.lifepool.dicom.filter;

import daris.client.lifepool.dicom.AttributeTag;

public abstract class StringAttributeFilter extends AttributeFilterBase<String> {

    protected StringAttributeFilter(AttributeTag tag, Operator operator, String value) {
        super(tag, operator, value);
    }

    @Override
    public void saveValue(StringBuilder sb) {
        sb.append("'").append(value()).append("'");
    }

}
