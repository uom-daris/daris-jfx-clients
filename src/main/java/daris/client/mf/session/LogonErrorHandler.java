package daris.client.mf.session;

public interface LogonErrorHandler {
    void logonFailed(Throwable error);
}
