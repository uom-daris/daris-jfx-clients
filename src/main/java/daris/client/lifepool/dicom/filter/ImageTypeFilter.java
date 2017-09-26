package daris.client.lifepool.dicom.filter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import daris.client.lifepool.dicom.AttributeTag;
import daris.client.lifepool.dicom.ImageType;

public class ImageTypeFilter implements AttributeFilter<ImageType> {

    private static Set<Operator> _candidateOps = new LinkedHashSet<Operator>(Arrays.asList(Operator.EQ, Operator.NE));

    private Operator _op;
    private ImageType _value;

    public ImageTypeFilter(Operator op, ImageType value) {
        _op = op;
        _value = value;
    }

    @Override
    public final Set<Operator> candidateOperators() {
        return _candidateOps;
    }

    @Override
    public void save(StringBuilder sb) {
        Operator op = operator();
        ImageType value = value();
        int totalNumberOfValues = value == null ? 0 : value.totalNumberOfValues();
        if (op != null && op.numberOfValuesSupported() > 0 && totalNumberOfValues > 0) {
            if (totalNumberOfValues > 1) {
                sb.append("(");
            }
            ImageType.PixelDataCharacteristics pdc = value.pixelDataCharacteristics();
            boolean first = true;
            if (pdc != null) {
                append(sb, tag(), op, pdc.name(), first ? null : LogicalOperator.AND);
                first = false;
            }
            ImageType.PatientExaminationCharacteristics pec = value.patientExaminationCharacteristics();
            if (pec != null) {
                append(sb, tag(), op, pec.name(), first ? null : LogicalOperator.AND);
                first = false;
            }
            List<String> optionalValues = value.optionalValues();
            if (optionalValues != null && !optionalValues.isEmpty()) {
                for (String optionalValue : optionalValues) {
                    append(sb, tag(), op, optionalValue, first ? null : LogicalOperator.AND);
                    first = false;
                }
            }
            if (totalNumberOfValues > 1) {
                sb.append(")");
            }
        }
    }

    private static void append(StringBuilder sb, AttributeTag tag, Operator op, String value,
            LogicalOperator logicalOp) {
        if (logicalOp != null) {
            sb.append(" ").append(logicalOp).append(" ");
        }
        sb.append("xpath(");
        sb.append(AttributeUtil.getAttributeValueXPath(tag));
        sb.append(")");
        sb.append(" ").append(op.operator());
        if (op.numberOfValuesSupported() > 0 && value != null) {
            sb.append(" ").append("'").append(value).append("'");
        }
    }

    @Override
    public AttributeTag tag() {
        return AttributeTag.imageType;
    }

    @Override
    public Operator operator() {
        return _op;
    }

    @Override
    public ImageType value() {
        return _value;
    }

    @Override
    public void setOperator(Operator op) {
        _op = op;
    }

    @Override
    public void setValue(ImageType value) {
        _value = value;
    }

}
