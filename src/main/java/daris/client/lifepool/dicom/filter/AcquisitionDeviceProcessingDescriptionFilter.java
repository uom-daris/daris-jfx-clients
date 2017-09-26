package daris.client.lifepool.dicom.filter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import daris.client.lifepool.dicom.AttributeTag;

public class AcquisitionDeviceProcessingDescriptionFilter extends StringAttributeFilter {

    private static Set<Operator> _candidateOps = new LinkedHashSet<Operator>(Arrays.asList(Operator.CONTAINS));

    public AcquisitionDeviceProcessingDescriptionFilter() {
        super(AttributeTag.acquisitionDeviceProcessingDescription, null, null);
    }

    @Override
    public Set<Operator> candidateOperators() {
        return _candidateOps;
    }

}
