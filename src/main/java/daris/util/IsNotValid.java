package daris.util;

public class IsNotValid implements Validity {

    public static final IsNotValid INSTANCE = new IsNotValid();
    private String _reason;

    public IsNotValid() {
        this(null);
    }

    public IsNotValid(final String reason) {
        _reason = reason;
    }

    @Override
    public boolean valid() {
        return false;
    }

    @Override
    public String reasonForIssue() {
        return _reason;
    }

}
