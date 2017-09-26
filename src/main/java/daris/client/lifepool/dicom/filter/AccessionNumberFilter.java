package daris.client.lifepool.dicom.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import daris.client.lifepool.dicom.AttributeTag;

public class AccessionNumberFilter extends StringAttributeFilter {

    private static Set<Operator> _candidateOps = new LinkedHashSet<Operator>(
            Arrays.asList(Operator.EQ, Operator.NE, Operator.STARTS_WITH, Operator.ENDS_WITH, Operator.CONTAINS));

    public AccessionNumberFilter() {
        super(AttributeTag.accessionNumber, null, null);

    }

    @Override
    public Set<Operator> candidateOperators() {
        return Collections.unmodifiableSet(_candidateOps);
    }

}
