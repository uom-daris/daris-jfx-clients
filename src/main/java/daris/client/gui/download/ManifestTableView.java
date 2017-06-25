package daris.client.gui.download;

import java.util.AbstractMap.SimpleEntry;

import daris.client.download.manifest.Identifier;
import daris.client.download.manifest.Manifest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ManifestTableView extends TableView<SimpleEntry<String, String>> {

    private Manifest _manifest;

    @SuppressWarnings("unchecked")
    public ManifestTableView(Manifest manifest) {
        _manifest = manifest;

        TableColumn<SimpleEntry<String, String>, String> propertyCol = new TableColumn<SimpleEntry<String, String>, String>(
                "Property");
        propertyCol.setCellValueFactory(p -> {
            return new ReadOnlyObjectWrapper<String>(p.getValue().getKey());
        });
        propertyCol.setSortable(false);
        propertyCol.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
        TableColumn<SimpleEntry<String, String>, String> valueCol = new TableColumn<SimpleEntry<String, String>, String>(
                "Value");
        valueCol.setCellValueFactory(p -> {
            return new ReadOnlyObjectWrapper<String>(p.getValue().getValue());
        });
        valueCol.setSortable(false);
        valueCol.setPrefWidth(600);

        getColumns().addAll(propertyCol, valueCol);

        update(true);

    }

    public void update(boolean showTranscodes) {

        ObservableList<SimpleEntry<String, String>> entries = FXCollections.observableArrayList();
        if (_manifest.name() != null) {
            entries.add(new SimpleEntry<String, String>("Name:", _manifest.name()));
        }
        if (_manifest.description() != null) {
            entries.add(new SimpleEntry<String, String>("Description:", _manifest.description()));
        }
        entries.add(new SimpleEntry<String, String>("Mediaflux Server:", _manifest.serverAddress()));
        if (_manifest.hasToken()) {
            entries.add(new SimpleEntry<String, String>("Secure Token:", _manifest.token()));
        }
        if (_manifest.hasQuery()) {
            List<String> query = _manifest.query();
            for (String where : query) {
                entries.add(new SimpleEntry<String, String>("Query:", where));
            }
        }
        if (_manifest.hasIds()) {
            List<Identifier> ids = _manifest.ids();
            StringBuilder sbAssetIds = new StringBuilder();
            StringBuilder sbCids = new StringBuilder();
            for (Identifier id : ids) {
                if (id.assetId() != null) {
                    if (sbAssetIds.length() > 0) {
                        sbAssetIds.append(", ");
                    }
                    sbAssetIds.append(id.assetId());
                }
                if (id.citeableId() != null) {
                    if (sbCids.length() > 0) {
                        sbCids.append(", ");
                    }
                    sbCids.append(id.citeableId());
                }
            }
            if (sbAssetIds.length() > 0) {
                entries.add(new SimpleEntry<String, String>("Asset Ids:", sbAssetIds.toString()));
            }
            if (sbCids.length() > 0) {
                entries.add(new SimpleEntry<String, String>("Cids:", sbCids.toString()));
            }
        }
        if (showTranscodes && _manifest.hasTranscodes()) {
            Map<String, String> transcodes = _manifest.transcodes();
            Set<String> froms = transcodes.keySet();
            for (String from : froms) {
                entries.add(new SimpleEntry<String, String>("Transcode: ", from + " --> " + transcodes.get(from)));
            }
        }
        setItems(entries);
    }
}
