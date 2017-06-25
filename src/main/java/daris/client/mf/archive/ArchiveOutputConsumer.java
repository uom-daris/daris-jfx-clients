package daris.client.mf.archive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.file.Files;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.client.archive.Archive;
import arc.mime.NamedMimeType;
import arc.streams.LongInputStream;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc.Element;
import daris.streams.AbortCheck;
import daris.streams.ProgressMonitor;
import daris.streams.ProgressMonitoredInputStream;
import daris.streams.ProgressMonitoredOutputStream;
import daris.streams.StreamCopy;

public class ArchiveOutputConsumer extends arc.mf.client.ServerClient.OutputConsumer {

    private File _outputDir;
    private String _mimeType;
    private ArchiveExtractProgressMonitor _pm;
    private AbortCheck _ac;

    public ArchiveOutputConsumer(String mimeType, File outputDir, ArchiveExtractProgressMonitor pm, AbortCheck ac) {
        _mimeType = mimeType;
        _outputDir = outputDir;
        _pm = pm;
        _ac = ac;
    }

    public ArchiveOutputConsumer(String mimeType, File outputDir) {
        this(mimeType, outputDir, null, null);
    }

    @Override
    protected void consume(Element re, LongInputStream inputStream) throws Throwable {
        LongInputStream in = _pm == null ? inputStream
                : new SizedInputStream(new ProgressMonitoredInputStream(inputStream, new ProgressMonitor() {

                    @Override
                    public void updateProgress(long increment) {
                        _pm.incBytesRead(increment);
                    }
                }, _ac), inputStream.length());
        Archive.declareSupportForAllTypes();
        ArchiveInput ai = ArchiveRegistry.createInput(in, new NamedMimeType(_mimeType));
        ArchiveInput.Entry e;
        try {
            while ((e = ai.next()) != null) {
                if (_ac != null && _ac.hasBeenAborted()) {
                    throw new InterruptedIOException();
                }
                try {
                    if (e.isDirectory()) {
                        File dir = new File(_outputDir, e.name());
                        dir.mkdirs();
                        if (_pm != null) {
                            _pm.directoryCreated(dir);
                        }
                    } else {
                        InputStream is = e.stream();
                        File of = new File(_outputDir, e.name());
                        File od = of.getParentFile();
                        if (!od.exists()) {
                            Files.createDirectories(od.toPath());
                        }
                        OutputStream os = new ProgressMonitoredOutputStream(
                                new BufferedOutputStream(new FileOutputStream(of)), new ProgressMonitor() {

                                    @Override
                                    public void updateProgress(long increment) {
                                        if (_pm != null) {
                                            _pm.incBytesWritten(increment);
                                        }
                                    }
                                });
                        try {
                            StreamCopy.copy(is, os, _ac);
                        } finally {
                            os.close();
                            is.close();
                        }
                        if (_pm != null) {
                            _pm.fileCreated(of);
                        }
                    }
                } finally {
                    ai.closeEntry();
                }
            }
        } finally {
            try {
                ai.close();
            } finally {
                in.close();
            }
        }
    }

}
