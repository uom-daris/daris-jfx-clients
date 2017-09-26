package daris.client.lifepool.dicom;

public enum PresentationIntentState {

    FOR_PRESENTATION, FOR_PROCESSING;

    public String toString() {
        return name().replace('_', ' ');
    }

}
