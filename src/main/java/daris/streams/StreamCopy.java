package daris.streams;

import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

public class StreamCopy {

    public static final int BUFFER_SIZE = 8192;

    public static void copy(InputStream in, OutputStream out, boolean closeInput, boolean closeOutput,
            AbortCheck abortCheck) throws Throwable {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        try {
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                if (abortCheck != null) {
                    if (abortCheck.hasBeenAborted()) {
                        throw new InterruptedIOException("Aborted.");
                    }
                }
            }
        } finally {
            Throwable t = null;
            if (closeInput) {
                try {
                    in.close();
                } catch (Throwable ex) {
                    t = ex;
                }
            }
            if (closeOutput) {
                try {
                    out.close();
                } catch (Throwable ex) {
                    t = ex;
                }
            }
            if (t != null) {
                throw t;
            }
        }
    }

    public static void copy(InputStream in, OutputStream out, AbortCheck abortCheck) throws Throwable {
        copy(in, out, false, false, abortCheck);
    }

}
