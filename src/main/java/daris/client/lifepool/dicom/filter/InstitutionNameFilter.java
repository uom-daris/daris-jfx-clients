package daris.client.lifepool.dicom.filter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import daris.client.lifepool.dicom.AttributeTag;

public class InstitutionNameFilter extends StringAttributeFilter {

    private static Set<Operator> _candidateOps = new LinkedHashSet<Operator>(
            Arrays.asList(Operator.EQ, Operator.NE, Operator.CONTAINS));

    protected InstitutionNameFilter(AttributeTag tag, Operator operator, String value) {
        super(AttributeTag.institutionName, null, null);
    }

    @Override
    public Set<Operator> candidateOperators() {
        return _candidateOps;
    }

}
