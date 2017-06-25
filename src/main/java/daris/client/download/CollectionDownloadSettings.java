package daris.client.download;

import java.io.File;
import java.util.Map;

import daris.client.download.manifest.Manifest;
import daris.client.download.manifest.Parts;

public class CollectionDownloadSettings extends AssetDownloadSettings {

    private int _nThreads;

    public CollectionDownloadSettings(Parts parts, String outputPattern, Map<String, String> transcodes,
            boolean unarchive, File baseOutputDir, int nThreads) {
        super(parts, outputPattern, transcodes, unarchive, baseOutputDir);
        _nThreads = nThreads;
    }

    public CollectionDownloadSettings(Manifest manifest, File baseOutputDir, int nThreads) {
        super(manifest, baseOutputDir);
        _nThreads = nThreads;
    }

    public int numberOfThreads() {
        return _nThreads;
    }

    public void setNumberOfThreads(int nThreads) {
        _nThreads = nThreads;
    }

}
