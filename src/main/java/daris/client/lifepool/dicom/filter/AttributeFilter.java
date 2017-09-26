package daris.client.lifepool.dicom.filter;

import java.util.Set;

import daris.client.lifepool.dicom.AttributeTag;

public interface AttributeFilter<T> extends Filter {

    AttributeTag tag();

    Operator operator();

    void setOperator(Operator op);

    Set<Operator> candidateOperators();

    T value();

    void setValue(T value);

}
