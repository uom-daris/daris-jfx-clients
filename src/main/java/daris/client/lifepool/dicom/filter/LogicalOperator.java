package daris.client.lifepool.dicom.filter;

public enum LogicalOperator {

    AND, OR;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
