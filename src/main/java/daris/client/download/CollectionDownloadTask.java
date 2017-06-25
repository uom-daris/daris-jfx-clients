package daris.client.download;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import arc.archive.ArchiveRegistry;
import arc.exception.ThrowableUtil;
import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;
import daris.client.download.manifest.Identifier;
import daris.client.download.manifest.Manifest;
import daris.client.download.manifest.Parts;
import daris.client.mf.asset.AssetUtils;
import daris.client.mf.session.Session;
import daris.streams.AbortCheck;
import daris.streams.ProgressMonitor;
import daris.util.MessageUpdater;

public class CollectionDownloadTask extends DownloadTask {

    private Manifest _manifest;
    private CollectionDownloadSettings _settings;

    public CollectionDownloadTask(Manifest manifest, CollectionDownloadSettings settings) {
        _manifest = manifest;
        _settings = settings;
        stateProperty().addListener((obs, ov, nv) -> {
            if (nv == State.CANCELLED || nv == State.FAILED || nv == State.SUCCEEDED) {
                logger.info(nv.name().toLowerCase());
            }
        });
        exceptionProperty().addListener((obs, ov, nv) -> {
            logger.warning(ThrowableUtil.stackTrace(nv));
        });
    }

    public void setManifest(Manifest manifest) {
        _manifest = manifest;
    }

    public void updateSettings(CollectionDownloadSettings settings) {
        _settings = settings;
    }

    @SuppressWarnings("rawtypes")
    public void execute() throws Throwable {

        ServerClient.Connection cxn = _manifest.hasToken() ? Session.connect(_manifest.serverHost(),
                _manifest.serverPort(), _manifest.serverTransport(), _manifest.token()) : Session.connect();
        try {
            Set<Identifier> ids = new TreeSet<Identifier>();
            if (_manifest.hasIds()) {
                List<Identifier> mids = _manifest.ids();
                for (Identifier id : mids) {
                    if (id.isCiteableId()) {
                        // add recursively
                        ids.addAll(executeQuery(cxn,
                                new ArrayList<String>(Arrays.asList(
                                        "cid='" + id.citeableId() + "' or cid starts with '" + id.citeableId() + "'")),
                                _manifest.outputPattern()));
                    } else {
                        ids.add(id);
                    }
                }
            }
            if (_manifest.hasQuery()) {
                ids.addAll(executeQuery(cxn, _manifest.query(), _manifest.outputPattern()));
            }

            int total = ids.size();
            updateTotalObjects(total);
            if (total <= 0) {
                return;
            }
            if (_settings.numberOfThreads() > 1 && total > 1) {
                // multithread
                int nThreads = total < _settings.numberOfThreads() ? total : _settings.numberOfThreads();
                BlockingQueue<Identifier> queue = new ArrayBlockingQueue<Identifier>(ids.size() + nThreads, false, ids);
                for (int i = 0; i < nThreads; i++) {
                    // one poison object per thread
                    queue.put(new Identifier(null, null, null));
                }
                ExecutorService executor = Executors.newFixedThreadPool(nThreads);
                try {
                    Future[] futures = new Future[nThreads];
                    for (int i = 0; i < nThreads; i++) {
                        futures[i] = executor.submit(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                Identifier id;
                                while ((id = queue.take()).valid()) {
                                    try {
                                        checkIfCancelled();
                                        downloadAsset(cxn, id);
                                    } catch (Throwable e) {
                                        if (e instanceof Exception) {
                                            throw (Exception) e;
                                        } else {
                                            throw new Exception(e);
                                        }
                                    }
                                }
                                return null;
                            }
                        });
                    }
                    for (Future future : futures) {
                        try {
                            future.get();
                        } catch (ExecutionException e) {
                            executor.shutdownNow();
                            throw e.getCause();
                        }
                    }
                } finally {
                    executor.shutdown();
                }
            } else {
                for (Identifier id : ids) {
                    checkIfCancelled();
                    downloadAsset(cxn, id);
                }
            }
        } finally {
            cxn.close();
        }
    }

    private void downloadAsset(ServerClient.Connection cxn, Identifier id) throws Throwable {
        XmlDoc.Element ae = AssetUtils.getAssetMeta(cxn, id);
        String type = ae.value("type");
        String ctype = ae.value("content/type");
        boolean hasContent = ae.elementExists("content");
        boolean transcode = _settings.hasTranscodeFor(type);
        boolean unarchive = _settings.unarchive() && ctype != null && ArchiveRegistry.isAnArchive(ctype);
        long size = ae.longValue("content/size", 0);
        if (hasContent) {
            incTotalSize(size, true);
        }
        AssetDownloadTask.download(cxn, ae, id.outputPath(), _settings, new ProgressMonitor() {

            @Override
            public void updateProgress(long increment) {
                if (unarchive) {
                    incProcessedSize(increment, true);
                }
            }
        }, new ProgressMonitor() {
            @Override
            public void updateProgress(long increment) {
                incReceivedSize(increment);
                if (!transcode && !unarchive) {
                    incProcessedSize(increment, true);
                }
            }
        }, new MessageUpdater() {

            @Override
            public void updateMessage(String message) {
                CollectionDownloadTask.this.updateMessage(message);
                logger.info(message);
            }
        }, new AbortCheck() {

            @Override
            public boolean hasBeenAborted() {
                return isCancelled();
            }
        });
        if (hasContent && transcode) {
            incProcessedSize(size, true);
        }
        incProcessedObjects(_settings.parts() == Parts.META);
    }

    private static List<Identifier> executeQuery(ServerClient.Connection cxn, List<String> query, String outputPattern)
            throws Throwable {
        XmlStringWriter w = new XmlStringWriter();
        for (String where : query) {
            w.add("where", where);
        }
        w.add("size", "infinity");
        if (outputPattern != null) {
            w.add("action", "pipe");
            w.push("service", new String[] { "name", "asset.path.generate" });
            w.add("expr", outputPattern);
            w.pop();
            w.add("pipe-generate-result-xml", true);
        } else {
            w.add("action", "get-path");
        }
        XmlDoc.Element re = cxn.execute("asset.query", w.document());
        List<XmlDoc.Element> pes = re.elements("path");
        if (pes != null && !pes.isEmpty()) {
            List<Identifier> ids = new ArrayList<Identifier>(pes.size());
            for (XmlDoc.Element pe : pes) {
                String assetId = pe.value("@id");
                String path = pe.value();
                ids.add(new Identifier(assetId, null, path));
            }
            return ids;
        }
        return null;
    }

}
