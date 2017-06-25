package daris.client.mf.session;

public interface ServiceErrorHandler {

    /**
     * Handles exception;
     * 
     * @param excption
     * @return true if the exception has been processed and no need to
     *         propagate; false if the exception need to be re-thrown;
     */
    boolean handleError(Throwable t);

}
