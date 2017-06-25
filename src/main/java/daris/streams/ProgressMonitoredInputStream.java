package daris.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

public class ProgressMonitoredInputStream extends java.io.FilterInputStream {

    private ProgressMonitor _pm;
    private AbortCheck _ac;

    private long _bytesRead = 0;
    private long _size = 0;

    public ProgressMonitoredInputStream(InputStream in, ProgressMonitor pm, AbortCheck ac) {
        super(in);
        _pm = pm;
        _ac = ac;
        _bytesRead = 0;
        _size = 0;
        try {
            _size = in.available();
        } catch (IOException ioe) {
            _size = 0;
        }
    }

    public ProgressMonitoredInputStream(InputStream in, ProgressMonitor pm) {
        this(in, pm, null);
    }

    /**
     * Overrides <code>FilterInputStream.read</code> to update the progress
     * monitor after the read.
     */
    public int read() throws IOException {
        int c = in.read();
        if (c >= 0) {
            _bytesRead++;
            if (_pm != null) {
                _pm.updateProgress(1);
            }
        }
        if (_ac != null && _ac.hasBeenAborted()) {
            InterruptedIOException exc = new InterruptedIOException("progress");
            exc.bytesTransferred = (int) _bytesRead;
            throw exc;
        }
        return c;
    }

    /**
     * Overrides <code>FilterInputStream.read</code> to update the progress
     * monitor after the read.
     */
    public int read(byte b[], int off, int len) throws IOException {
        int nr = in.read(b, off, len);
        if (nr > 0) {
            _bytesRead += nr;
            if (_pm != null) {
                _pm.updateProgress(nr);
            }
        }
        if (_ac != null && _ac.hasBeenAborted()) {
            InterruptedIOException exc = new InterruptedIOException("progress");
            exc.bytesTransferred = (int) _bytesRead;
            throw exc;
        }
        return nr;
    }

    /**
     * Overrides <code>FilterInputStream.skip</code> to update the progress
     * monitor after the skip.
     */
    public long skip(long n) throws IOException {
        long nr = in.skip(n);
        if (nr > 0) {
            _bytesRead += nr;
            if (_pm != null) {
                _pm.updateProgress(nr);
            }
        }
        return nr;
    }

    /**
     * Overrides <code>FilterInputStream.reset</code> to reset the progress
     * monitor as well as the stream.
     */
    public synchronized void reset() throws IOException {
        in.reset();
        long nread = _bytesRead;
        _bytesRead = _size - in.available();
        if (_pm != null) {
            _pm.updateProgress(_bytesRead - nread);
        }

    }

    public synchronized long bytesRead() {
        return _bytesRead;
    }
}
