package daris.client.mf.archive;

import java.io.File;

public interface ArchiveExtractProgressMonitor {

    void incBytesRead(long increment);

    void incBytesWritten(long increment);

    void fileCreated(File file);

    void directoryCreated(File dir);

}
