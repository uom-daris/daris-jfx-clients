package daris.client.mf.session;

public interface ErrorDialog {

    void display(String message, String context, Throwable e);

}
