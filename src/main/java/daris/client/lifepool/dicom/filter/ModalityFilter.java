package daris.client.lifepool.dicom.filter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import daris.client.lifepool.dicom.AttributeTag;
import daris.client.lifepool.dicom.Modality;

public class ModalityFilter extends AttributeFilterBase<Modality> {
    private static Set<Operator> _candidateOps = new LinkedHashSet<Operator>(Arrays.asList(Operator.EQ, Operator.NE));

    public ModalityFilter() {
        super(AttributeTag.modality, null, null);
    }

    @Override
    public Set<Operator> candidateOperators() {
        return _candidateOps;
    }

    @Override
    protected void saveValue(StringBuilder sb) {
        sb.append("'").append(value()).append("'");
    }
}
