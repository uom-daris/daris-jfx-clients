package daris.client.lifepool.dicom.filter;

public enum Operator {
    HAS_VALUE("has value", 0), HASNO_VALUE("hasno value", 0), CONTAINS("contains", 1), STARTS_WITH("starts with",
            1), ENDS_WITH("ends with", 1), EQ("=", 1), LT("<", 1), GT(">", 1), LE("<=", 1), GE(">=", 1), NE("!=", 1);
    private String _op;
    private int _nbValues;

    Operator(String op, int nbValues) {
        _op = op;
        _nbValues = nbValues;
    }

    public String operator() {
        return _op;
    }

    @Override
    public String toString() {
        return _op;
    }

    public int numberOfValuesSupported() {
        return _nbValues;
    }
}
