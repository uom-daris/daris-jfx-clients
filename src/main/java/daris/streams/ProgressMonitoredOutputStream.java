package daris.streams;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;

public class ProgressMonitoredOutputStream extends java.io.FilterOutputStream {

    private ProgressMonitor _pm;
    private AbortCheck _ac;
    private long _bytesWritten = 0L;

    public ProgressMonitoredOutputStream(OutputStream out, ProgressMonitor pm, AbortCheck ac) {
        super(out);
        _pm = pm;
        _ac = ac;
        _bytesWritten = 0L;
    }

    public ProgressMonitoredOutputStream(OutputStream out, ProgressMonitor pm) {
        this(out, pm, null);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        _bytesWritten++;
        if (_pm != null) {
            _pm.updateProgress(1);
        }
        if (_ac != null && _ac.hasBeenAborted()) {
            InterruptedIOException exc = new InterruptedIOException("progress");
            exc.bytesTransferred = (int) _bytesWritten;
            throw exc;
        }
    }

    @Override
    public void write(byte[] data, int off, int length) throws IOException {
        out.write(data, off, length);
        _bytesWritten += length;
        if (_pm != null) {
            _pm.updateProgress(length);
        }
        if (_ac != null && _ac.hasBeenAborted()) {
            InterruptedIOException exc = new InterruptedIOException("progress");
            exc.bytesTransferred = (int) _bytesWritten;
            throw exc;
        }

    }

    public long byteWritten() {
        return _bytesWritten;
    }

}
