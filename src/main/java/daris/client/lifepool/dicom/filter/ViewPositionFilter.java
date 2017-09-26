package daris.client.lifepool.dicom.filter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import daris.client.lifepool.dicom.AttributeTag;

public class ViewPositionFilter extends StringAttributeFilter {

    private static Set<Operator> _candidateOps = new LinkedHashSet<Operator>(Arrays.asList(Operator.CONTAINS));

    public ViewPositionFilter() {
        super(AttributeTag.viewPosition, null, null);
    }

    @Override
    public Set<Operator> candidateOperators() {
        return _candidateOps;
    }

}
