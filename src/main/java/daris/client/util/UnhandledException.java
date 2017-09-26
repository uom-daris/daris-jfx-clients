package daris.client.util;

import java.util.ArrayList;
import java.util.List;

public class UnhandledException {

    public static interface ExceptionHandler {

        void handleException(String message, String context, Throwable e);

    }

    public static final ExceptionHandler DEFAULT_EXCEPTION_HANDLER = new ExceptionHandler() {

        @Override
        public void handleException(String message, String context, Throwable e) {
            System.err.println("Error: " + (message == null ? e.getMessage() : message));
            if (context != null) {
                System.err.println("Context: " + context);
            }
            e.printStackTrace(System.err);
        }

    };

    private static List<ExceptionHandler> _handlers;

    public static void addHandler(ExceptionHandler h) {
        if (_handlers == null) {
            _handlers = new ArrayList<ExceptionHandler>();
        }
        _handlers.add(h);
    }

    public static void removeHandler(ExceptionHandler h) {
        if (_handlers != null) {
            _handlers.remove(h);
        }
    }

    public static void removeAllHandlers() {
        if (_handlers != null) {
            _handlers.clear();
            _handlers = null;
        }
    }

    public static void report(String message, String context, Throwable e) {
        if (_handlers != null) {
            for (ExceptionHandler eh : _handlers) {
                eh.handleException(message, context, e);
            }
        }
    }

//    public static void report(String context, Throwable e) {
//        report(e.getMessage(), context, e);
//    }
//
//    public static void report(Throwable e) {
//        report(e.getMessage(), null, e);
//    }

    static {
        addHandler(DEFAULT_EXCEPTION_HANDLER);
    }

}
