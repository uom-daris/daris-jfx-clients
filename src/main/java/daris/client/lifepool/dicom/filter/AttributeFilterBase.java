package daris.client.lifepool.dicom.filter;

import daris.client.lifepool.dicom.AttributeTag;

public abstract class AttributeFilterBase<T> implements AttributeFilter<T> {

    private AttributeTag _tag;
    private Operator _op;
    private T _value;

    protected AttributeFilterBase(AttributeTag tag, Operator operator, T value) {
        _tag = tag;
        _op = operator;
        _value = value;
    }

    @Override
    public final AttributeTag tag() {
        return _tag;
    }

    @Override
    public Operator operator() {
        return _op;
    }

    public void setOperator(Operator op) {
        _op = op;
    }

    @Override
    public T value() {
        return _value;
    }

    public void setValue(T value) {
        _value = value;
    }

    @Override
    public void save(StringBuilder sb) {
        Operator op = operator();
        T value = value();
        sb.append("(xpath(").append(AttributeUtil.getAttributeValueXPath(_tag)).append(")");
        sb.append(" ").append(op.operator());
        if (op.numberOfValuesSupported() > 0 && value != null) {
            sb.append(" ");
            saveValue(sb);
        }
    }

    protected abstract void saveValue(StringBuilder sb);

}
