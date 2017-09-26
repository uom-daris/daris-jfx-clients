package daris.client.lifepool.dicom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImageType {

    public static enum PixelDataCharacteristics {
        ORIGINAL, DERIVED
    }

    public static enum PatientExaminationCharacteristics {
        PRIMARY, SECONDARY
    }

    private PixelDataCharacteristics _pixelDataCharacteristics;
    private PatientExaminationCharacteristics _patientExaminationCharacteristics;
    private List<String> _optionalValues;

    public ImageType(PixelDataCharacteristics pdc, PatientExaminationCharacteristics pec, String... optionalValues) {
        _pixelDataCharacteristics = pdc;
        _patientExaminationCharacteristics = pec;
        if (optionalValues != null && optionalValues.length > 0) {
            _optionalValues = new ArrayList<String>(Arrays.asList(optionalValues));
        }
    }

    public PixelDataCharacteristics pixelDataCharacteristics() {
        return _pixelDataCharacteristics;
    }

    public PatientExaminationCharacteristics patientExaminationCharacteristics() {
        return _patientExaminationCharacteristics;
    }

    public List<String> optionalValues() {
        if (_optionalValues != null && _optionalValues.isEmpty()) {
            return null;
        } else {
            return Collections.unmodifiableList(_optionalValues);
        }
    }

    public int totalNumberOfValues() {
        int total = 0;
        if (pixelDataCharacteristics() != null) {
            total++;
        }
        if (patientExaminationCharacteristics() != null) {
            total++;
        }
        if (_optionalValues != null) {
            total += _optionalValues.size();
        }
        return total;
    }

}
