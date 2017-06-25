package daris.client.download;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.Writer;

import arc.archive.ArchiveRegistry;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.download.manifest.Identifier;
import daris.client.download.manifest.Parts;
import daris.client.mf.archive.ArchiveExtractProgressMonitor;
import daris.client.mf.archive.ArchiveOutputConsumer;
import daris.client.mf.archive.FileOutputConsumer;
import daris.client.mf.asset.AssetUtils;
import daris.streams.AbortCheck;
import daris.streams.ProgressMonitor;
import daris.util.FileUtils;
import daris.util.MessageUpdater;
import daris.util.PathUtils;
import arc.xml.XmlStringWriter;

public class AssetDownloadTask extends DownloadTask {

    public static int NUM_RETRIES_ON_DIR_NAME_CONFLICTS = 5;

    public static void download(ServerClient.Connection cxn, XmlDoc.Element ae, String outputPath,
            AssetDownloadSettings settings, ProgressMonitor readPM, ProgressMonitor writePM, MessageUpdater mh,
            AbortCheck abortCheck) throws Throwable {
        if (outputPath == null) {
            if (settings.outputPattern() == null) {
                outputPath = ae.value("path");
            } else {
                // generating asset output path
                String assetId = ae.value("@id");
                outputPath = AssetUtils.generatePath(cxn, assetId, settings.outputPattern());
                //@formatter:off
//                if (mh != null) {
//                    mh.updateMessage("generated asset " + assetId + " output path: " + outputPath);
//                }
                //@formatter:on
            }
        }
        if (settings.parts() == Parts.META || settings.parts() == Parts.ALL) {
            // download metadata
            AssetDownloadTask.downloadMeta(ae, settings.outputDirectory(), outputPath, mh, abortCheck);
        }
        String type = ae.value("type");
        String ctype = ae.value("content/type");
        if (type != null && settings.hasTranscodeFor(type)) {
            // transcode
            String toType = settings.transcodeToTypeFor(type);
            AssetDownloadTask.transcode(cxn, ae, toType, settings.outputDirectory(), outputPath, readPM, writePM, mh,
                    abortCheck);
        } else if (ctype != null && ArchiveRegistry.isAnArchive(ctype) && settings.unarchive()) {
            // unarchive
            AssetDownloadTask.unarchive(cxn, ae, settings.outputDirectory(), outputPath, readPM, writePM, mh,
                    abortCheck);
        } else if (settings.parts() == Parts.CONTENT || settings.parts() == Parts.ALL) {
            // download content
            AssetDownloadTask.downloadContent(cxn, ae, settings.outputDirectory(), outputPath, readPM, writePM, mh,
                    abortCheck);
        }
    }

    public static void downloadMeta(XmlDoc.Element ae, File outputDir, String outputPath, MessageUpdater mh,
            AbortCheck abortCheck) throws Throwable {
        downloadMeta(ae, outputDir, outputPath, mh, abortCheck, NUM_RETRIES_ON_DIR_NAME_CONFLICTS);
    }

    private static void downloadMeta(XmlDoc.Element ae, File outputDir, String outputPath, MessageUpdater mh,
            AbortCheck abortCheck, int nRetries) throws Throwable {
        if (abortCheck != null && abortCheck.hasBeenAborted()) {
            throw new InterruptedException();
        }
        StringBuilder sb = new StringBuilder(PathUtils.join(outputDir.getCanonicalPath(), outputPath));
        if (!outputPath.endsWith(".meta.xml")) {
            sb.append(".meta.xml");
        }
        String filePath = sb.toString();
        String dirPath = PathUtils.getParent(filePath);
        try {
            if (dirPath != null) {
                File dir = new File(dirPath);
                if (!(dir.exists() && dir.isDirectory())) {
                    FileUtils.mkdirs(dir, true);
                }
            }
            File f = new File(filePath);
            if (mh != null) {
                String assetId = ae.value("@id");
                mh.updateMessage("saving asset " + assetId + " metadata to file:" + f.getCanonicalPath());
            }
            Writer w = new BufferedWriter(new FileWriter(f));
            try {
                w.write(ae.toString());
            } finally {
                w.close();
            }
        } catch (FileNotFoundException e) {
            if (nRetries > 0) {
                System.out.println("Directory name conflicts. Rename and retry...");
                downloadMeta(ae, outputDir, outputPath, mh, abortCheck, NUM_RETRIES_ON_DIR_NAME_CONFLICTS);
            } else {
                throw e;
            }
        }
    }

    public static void downloadContent(ServerClient.Connection cxn, XmlDoc.Element ae, File outputDir,
            String outputPath, ProgressMonitor pmRead, ProgressMonitor pmWrite, MessageUpdater mh,
            AbortCheck abortCheck) throws Throwable {
        downloadContent(cxn, ae, outputDir, outputPath, pmRead, pmWrite, mh, abortCheck,
                NUM_RETRIES_ON_DIR_NAME_CONFLICTS);
    }

    private static void downloadContent(ServerClient.Connection cxn, XmlDoc.Element ae, File outputDir,
            String outputPath, ProgressMonitor pmRead, ProgressMonitor pmWrite, MessageUpdater mh,
            AbortCheck abortCheck, int nRetries) throws Throwable {
        if (!ae.elementExists("content")) {
            return;
        }
        File f = new File(PathUtils.join(outputDir.getCanonicalPath(), outputPath));
        try {
            // rename the file if a directory with same name already exist.
            f = FileUtils.renameFileIfDirectoryWithSameNameExists(f);

            // create parent directories (and rename existing file if conflict
            // with
            // any parent directory name)
            File dir = f.getParentFile();
            if (!(dir.exists() && dir.isDirectory())) {
                FileUtils.mkdirs(dir, true);
            }

            String assetId = ae.value("@id");
            if (mh != null) {
                mh.updateMessage("downloading asset " + assetId + " content to file:" + f.getCanonicalPath());
            }
            cxn.execute("asset.get", "<id>" + assetId + "</id>", null,
                    new FileOutputConsumer(f, pmRead, pmWrite, abortCheck));
        } catch (FileNotFoundException e) {
            if (nRetries > 0) {
                System.out.println("Directory name conflicts. Rename and retry...");
                downloadContent(cxn, ae, outputDir, outputPath, pmRead, pmWrite, mh, abortCheck, nRetries - 1);
            } else {
                throw e;
            }
        }
    }

    public static void unarchive(Connection cxn, Element ae, File outputDir, String outputPath, ProgressMonitor pmRead,
            ProgressMonitor pmWrite, MessageUpdater mh, AbortCheck abortCheck) throws Throwable {
        unarchive(cxn, ae, outputDir, outputPath, pmRead, pmWrite, mh, abortCheck, NUM_RETRIES_ON_DIR_NAME_CONFLICTS);
    }

    private static void unarchive(Connection cxn, Element ae, File outputDir, String outputPath, ProgressMonitor pmRead,
            ProgressMonitor pmWrite, MessageUpdater mh, AbortCheck abortCheck, int nRetries) throws Throwable {
        if (!ae.elementExists("content")) {
            // no content
            return;
        }
        String ctype = ae.value("content/type");
        if (!ArchiveRegistry.isAnArchive(ctype)) {
            // not an archive
            return;
        }
        File dir = new File(PathUtils.join(outputDir.getCanonicalPath(), outputPath));
        try {
            if (!(dir.exists() && dir.isDirectory())) {
                FileUtils.mkdirs(dir, true);
            }
            String assetId = ae.value("@id");
            if (mh != null) {
                mh.updateMessage(
                        "extracting asset " + assetId + " content archive to directory: " + dir.getCanonicalPath());
            }
            cxn.execute("asset.get", "<id>" + assetId + "</id>", null,
                    new ArchiveOutputConsumer(ctype, dir, new ArchiveExtractProgressMonitor() {

                        @Override
                        public void incBytesRead(long increment) {
                            if (pmRead != null) {
                                pmRead.updateProgress(increment);
                            }
                        }

                        @Override
                        public void incBytesWritten(long increment) {
                            if (pmWrite != null) {
                                pmWrite.updateProgress(increment);
                            }
                        }

                        @Override
                        public void fileCreated(File f) {
                            if (mh != null) {
                                mh.updateMessage("extracted file:" + f.getAbsolutePath());
                            }
                        }

                        @Override
                        public void directoryCreated(File dir) {
                            if (mh != null) {
                                mh.updateMessage("created directory:" + dir.getAbsolutePath());
                            }
                        }
                    }, abortCheck));
        } catch (FileNotFoundException e) {
            if (nRetries > 0) {
                System.out.println("Directory name conflicts. Rename and retry...");
                unarchive(cxn, ae, outputDir, outputPath, pmRead, pmWrite, mh, abortCheck, nRetries - 1);
            } else {
                throw e;
            }
        }
    }

    public static void transcode(Connection cxn, Element ae, String toType, File outputDir, String outputPath,
            ProgressMonitor pmRead, ProgressMonitor pmWrite, MessageUpdater mh, AbortCheck abortCheck)
            throws Throwable {
        transcode(cxn, ae, toType, outputDir, outputPath, pmRead, pmWrite, mh, abortCheck,
                NUM_RETRIES_ON_DIR_NAME_CONFLICTS);
    }

    private static void transcode(Connection cxn, Element ae, String toType, File outputDir, String outputPath,
            ProgressMonitor pmRead, ProgressMonitor pmWrite, MessageUpdater mh, AbortCheck abortCheck, int nRetries)
            throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append(outputDir.getCanonicalPath());
        String path = PathUtils.trimLeadingSlash(outputPath);
        String ext = ae.value("content/type/@ext");
        if (ext != null && path.endsWith("." + ext)) {
            path = path.substring(0, path.length() - 1 - ext.length());
        }
        sb.append("/").append(path);
        String mtype = toType.replace('/', '_');
        if (!path.endsWith(mtype)) {
            sb.append("/").append(mtype);
        }
        File dir = new File(sb.toString());
        try {
            if (!(dir.exists() && dir.isDirectory())) {
                FileUtils.mkdirs(dir, true);
            }
            String assetId = ae.value("@id");
            String type = ae.value("type");
            XmlStringWriter w = new XmlStringWriter();
            w.add("id", assetId);
            w.push("transcode");
            w.add("from", ae.value("type"));
            w.add("to", toType);
            w.pop();
            w.add("atype", "aar");
            w.add("clevel", 0);
            if (mh != null) {
                mh.updateMessage("transcoding asset " + assetId + "(" + type + "->" + toType + ")...");
            }
            cxn.execute("asset.transcode", w.document(), null,
                    new ArchiveOutputConsumer("application/arc-archive", dir, new ArchiveExtractProgressMonitor() {
                        @Override
                        public void incBytesRead(long increment) {
                            if (pmRead != null) {
                                pmRead.updateProgress(increment);
                            }
                        }

                        @Override
                        public void incBytesWritten(long increment) {
                            if (pmWrite != null) {
                                pmWrite.updateProgress(increment);
                            }
                        }

                        @Override
                        public void fileCreated(File f) {
                            if (mh != null) {
                                mh.updateMessage("downloaded (transcoded) file:" + f.getAbsolutePath());
                            }
                        }

                        @Override
                        public void directoryCreated(File dir) {
                            if (mh != null) {
                                mh.updateMessage("created directory:" + dir.getAbsolutePath());
                            }
                        }
                    }, abortCheck));
        } catch (FileNotFoundException e) {
            if (nRetries > 0) {
                System.out.println("Directory name conflicts. Rename and retry...");
                transcode(cxn, ae, toType, outputDir, outputPath, pmRead, pmWrite, mh, abortCheck, nRetries - 1);
            } else {
                throw e;
            }
        }
    }

    private ServerClient.Connection _cxn;
    private Identifier _id;
    private AssetDownloadSettings _settings;

    public AssetDownloadTask(ServerClient.Connection cxn, Identifier id, AssetDownloadSettings settings) {
        _cxn = cxn;
        _id = id;
        _settings = settings;
    }

    @Override
    protected void execute() throws Throwable {
        XmlDoc.Element ae = AssetUtils.getAssetMeta(_cxn, _id);
        String type = ae.value("type");
        String ctype = ae.value("content/type");
        boolean hasContent = ae.elementExists("content");
        boolean transcode = _settings.hasTranscodeFor(type);
        boolean unarchive = _settings.unarchive() && ctype != null && ArchiveRegistry.isAnArchive(ctype);
        long size = ae.longValue("content/size", 0);
        if (hasContent) {
            updateTotalSize(size);
        }
        updateProcessedSize(0);
        updateTotalObjects(1);
        updateProcessedObjects(0);
        download(_cxn, ae, _id.outputPath(), _settings, new ProgressMonitor() {

            @Override
            public void updateProgress(long increment) {
                if (unarchive) {
                    AssetDownloadTask.this.incProcessedSize(increment, true);
                }
            }
        }, new ProgressMonitor() {
            @Override
            public void updateProgress(long increment) {
                AssetDownloadTask.this.incReceivedSize(increment);
                if (!unarchive && !transcode) {
                    AssetDownloadTask.this.incProcessedSize(increment, true);
                }
            }
        }, new MessageUpdater() {

            @Override
            public void updateMessage(String message) {
                AssetDownloadTask.this.updateMessage(message);
                logger.info(message);
            }
        }, new AbortCheck() {

            @Override
            public boolean hasBeenAborted() {
                return isCancelled();
            }
        });
        updateProcessedObjects(1);
        if (hasContent) {
            updateProcessedSize(size);
            updateProgress(size, size);
        } else {
            updateProgress(1, 1);
        }
    }

    public String idAsString() {
        return "asset " + (_id.assetId() != null ? _id.assetId() : _id.citeableId());
    }

}
