package daris.client.mf.archive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import arc.streams.LongInputStream;
import arc.xml.XmlDoc.Element;
import daris.streams.AbortCheck;
import daris.streams.ProgressMonitor;
import daris.streams.ProgressMonitoredInputStream;
import daris.streams.ProgressMonitoredOutputStream;
import daris.streams.StreamCopy;

public class FileOutputConsumer extends arc.mf.client.ServerClient.OutputConsumer {

    private File _of;
    private ProgressMonitor _pmRead;
    private ProgressMonitor _pmWrite;
    private AbortCheck _ac;

    public FileOutputConsumer(File of, ProgressMonitor pmRead, ProgressMonitor pmWrite, AbortCheck ac) {
        _of = of;
        _pmRead = pmRead;
        _pmWrite = pmWrite;
        _ac = ac;
    }

    public FileOutputConsumer(File of) {
        this(of, null, null, null);
    }

    @Override
    protected void consume(Element re, LongInputStream inputStream) throws Throwable {

        InputStream in = _pmRead == null ? inputStream : new ProgressMonitoredInputStream(inputStream, _pmRead, null);
        OutputStream out = _pmWrite == null ? new BufferedOutputStream(new FileOutputStream(_of))
                : new ProgressMonitoredOutputStream(new BufferedOutputStream(new FileOutputStream(_of)), _pmWrite,
                        null);
        try {
            StreamCopy.copy(in, out, _ac);
        } finally {
            out.close();
            in.close();
        }
    }

}
