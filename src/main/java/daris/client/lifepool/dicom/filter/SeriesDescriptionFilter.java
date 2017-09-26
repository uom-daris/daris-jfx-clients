package daris.client.lifepool.dicom.filter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import daris.client.lifepool.dicom.AttributeTag;

public class SeriesDescriptionFilter extends StringAttributeFilter {

    private static Set<Operator> _candidateOps = new LinkedHashSet<Operator>(
            Arrays.asList(Operator.CONTAINS, Operator.HASNO_VALUE));

    protected SeriesDescriptionFilter() {
        super(AttributeTag.seriesDescription, null, null);
    }

    @Override
    public Set<Operator> candidateOperators() {
        return _candidateOps;
    }

}
