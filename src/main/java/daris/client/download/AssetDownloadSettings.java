package daris.client.download;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import daris.client.download.manifest.Manifest;
import daris.client.download.manifest.Parts;

public class AssetDownloadSettings {

    private Parts _parts;
    private String _outputPattern;
    private Map<String, String> _transcodes;
    private boolean _doTranscode;
    private boolean _unarchive;
    private File _baseOutputDir;

    public AssetDownloadSettings(Parts parts, String outputPattern, Map<String, String> transcodes, boolean unarchive,
            File baseOutputDir) {
        _parts = parts;
        _outputPattern = outputPattern;
        _transcodes = transcodes;
        _doTranscode = true;
        _unarchive = unarchive;
        _baseOutputDir = baseOutputDir;
    }

    public AssetDownloadSettings(Manifest manifest, File outputDir) {
        this(manifest.parts(), manifest.outputPattern(), manifest.transcodes(), manifest.unarchive(), outputDir);
    }

    public Parts parts() {
        return _parts;
    }

    public String outputPattern() {
        return _outputPattern;
    }

    public Map<String, String> transcodes() {
        return _doTranscode ? _transcodes : null;
    }

    public boolean hasTranscodes() {
        return _doTranscode && _transcodes != null && !_transcodes.isEmpty();
    }

    public boolean hasTranscodeFor(String fromType) {
        if (_doTranscode && _transcodes != null) {
            return _transcodes.containsKey(fromType);
        }
        return false;
    }

    public String transcodeToTypeFor(String fromType) {
        if (_doTranscode && _transcodes != null) {
            return _transcodes.get(fromType);
        }
        return null;
    }

    public boolean unarchive() {
        return _unarchive;
    }

    public File outputDirectory() {
        return _baseOutputDir;
    }

    public void setParts(Parts parts) {
        _parts = parts;
    }

    public void setOutputPattern(String outputPattern) {
        _outputPattern = outputPattern;
    }

    public void setTranscodes(Map<String, String> transcodes) {
        if (_transcodes != null && !_transcodes.isEmpty()) {
            _transcodes.clear();
        }
        if (transcodes == null || transcodes.isEmpty()) {
            _transcodes = null;
        } else {
            if (_transcodes == null) {
                _transcodes = new LinkedHashMap<String, String>(transcodes.size());
            }
            _transcodes.putAll(transcodes);
        }
    }

    public void setUnarchive(boolean unarchive) {
        _unarchive = unarchive;
    }

    public void setOutputDirectory(File baseOutputDir) {
        _baseOutputDir = baseOutputDir;
    }

    public boolean transcodingEnabled() {
        return _doTranscode;
    }

    public void setTranscodingEnabled(boolean enabled) {
        _doTranscode = enabled;
    }

}
