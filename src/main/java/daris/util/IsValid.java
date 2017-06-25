package daris.util;

public class IsValid implements Validity {
    
    public static final IsValid INSTANCE = new IsValid();
    private String _reason;

    public IsValid() {
        this(null);
    }

    public IsValid(final String reason) {
        _reason = reason;
    }

    @Override
    public final boolean valid() {
        return true;
    }

    @Override
    public String reasonForIssue() {
        return _reason;
    }

}