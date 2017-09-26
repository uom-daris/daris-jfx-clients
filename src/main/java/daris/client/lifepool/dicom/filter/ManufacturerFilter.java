package daris.client.lifepool.dicom.filter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import daris.client.lifepool.dicom.AttributeTag;

public class ManufacturerFilter extends StringAttributeFilter {

    private static Set<Operator> _candidateOps = new LinkedHashSet<Operator>(
            Arrays.asList(Operator.CONTAINS));

    protected ManufacturerFilter() {
        super(AttributeTag.manufacturer, null,null);
    }

    @Override
    public Set<Operator> candidateOperators() {
        return _candidateOps;
    }

}
