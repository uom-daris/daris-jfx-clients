package daris.client.gui.download;

import java.io.File;

import daris.client.download.CollectionDownloadSettings;
import daris.client.download.manifest.Manifest;
import daris.client.download.manifest.Parts;
import daris.client.gui.ValidatedInterfaceComponent;
import daris.util.IsNotValid;
import daris.util.Validity;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;

public class ManifestDownloadSettingsForm extends ValidatedInterfaceComponent {

    private Manifest _manifest;
    private CollectionDownloadSettings _settings;

    private GridPane _gridPane;
    private ManifestTableView _summary;
    private ComboBox<Parts> _parts;
    private CheckBox _transcode;
    private CheckBox _unarchive;
    private ComboBox<Integer> _numThreads;
    private TextField _outputDir;
    private Button _selectOutputDirButton;
    private DirectoryChooser _outputDirChooser;
    private Text _status;

    private BooleanProperty _valid;
    private BooleanProperty _disable;

    public ManifestDownloadSettingsForm(Manifest manifest) {
        _manifest = manifest;
        _settings = new CollectionDownloadSettings(manifest, null, 1);

        _disable = new SimpleBooleanProperty(this, "disable", false);

        _gridPane = new GridPane();
        _gridPane.setHgap(10);
        _gridPane.setVgap(10);
        _gridPane.setPadding(new Insets(25, 25, 25, 25));
        _gridPane.setBorder(new Border(new BorderStroke[] {
                new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT) }));

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHalignment(HPos.RIGHT);
        cc.setPercentWidth(30);
        _gridPane.getColumnConstraints().add(cc);
        cc = new ColumnConstraints();
        cc.setHalignment(HPos.LEFT);
        cc.setFillWidth(true);
        _gridPane.getColumnConstraints().add(cc);

        int row = 0;
        Text summaryLabel = new Text("Manifest summary:");
        summaryLabel.setTextAlignment(TextAlignment.LEFT);
        summaryLabel.wrappingWidthProperty().bind(_gridPane.widthProperty().subtract(60));
        _gridPane.add(summaryLabel, 0, row, 2, 1);

        row++;
        _summary = new ManifestTableView(_manifest);
        _summary.setMaxWidth(Double.MAX_VALUE);
        _summary.prefWidthProperty().bind(_gridPane.widthProperty());
        _summary.setPrefHeight(150);
        _gridPane.add(_summary, 0, row, 2, 1);

        row++;
        Label partsLabel = new Label("Parts:");
        _gridPane.add(partsLabel, 0, row);

        _parts = new ComboBox<Parts>(FXCollections.observableArrayList(Parts.values()));
        _parts.setValue(_settings.parts());
        _parts.valueProperty().addListener((observable, oldValue, newValue) -> {
            _settings.setParts(newValue);
        });
        _parts.disableProperty().bind(disableProperty());
        _gridPane.add(_parts, 1, row);

        if (_settings.hasTranscodes()) {
            row++;
            Label transcodeLabel = new Label("Transcode:");
            _gridPane.add(transcodeLabel, 0, row);

            _transcode = new CheckBox();
            _transcode.setSelected(_settings.transcodingEnabled());
            _transcode.selectedProperty().addListener((obs, ov, nv) -> {
                _settings.setTranscodingEnabled(nv);
                _summary.update(nv);
            });
            _transcode.disableProperty().bind(disableProperty());
            _gridPane.add(_transcode, 1, row);
        }

        row++;
        Label unarchiveLabel = new Label("Unarchive:");
        _gridPane.add(unarchiveLabel, 0, row);

        _unarchive = new CheckBox();
        _unarchive.setSelected(_settings.unarchive());
        _unarchive.selectedProperty().addListener((obs, ov, nv) -> {
            _settings.setUnarchive(nv);
        });
        _unarchive.disableProperty().bind(disableProperty());
        _gridPane.add(_unarchive, 1, row);

        row++;
        Label numThreadsLabel = new Label("Number of Threads:");
        _gridPane.add(numThreadsLabel, 0, row);

        _numThreads = new ComboBox<Integer>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        _numThreads.setValue(_settings.numberOfThreads());
        _numThreads.valueProperty().addListener((obs, ov, nv) -> {
            _settings.setNumberOfThreads(nv);
        });
        _numThreads.disableProperty().bind(disableProperty());
        _gridPane.add(_numThreads, 1, row);
        

        row++;
        Label outputDirLabel = new Label("Output Folder:");
        _gridPane.add(outputDirLabel, 0, row);

        HBox outputDirHBox = new HBox();
        _outputDir = new TextField();
        _outputDir.textProperty().addListener((obs, ov, nv) -> {
            _settings.setOutputDirectory(nv == null ? null : new File(nv));
            notifyOfChangeInState();
        });
        _outputDir.setPrefWidth(320);
        _outputDir.disableProperty().bind(disableProperty());
        outputDirHBox.getChildren().add(_outputDir);
        _outputDirChooser = new DirectoryChooser();
        _outputDirChooser.setTitle("Select output folder");
        _outputDirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        _selectOutputDirButton = new Button("Select...");
        _selectOutputDirButton.setOnAction(e -> {
            File dir = _outputDirChooser.showDialog(gui().getScene().getWindow());
            if (dir != null) {
                _outputDir.setText(dir.getAbsolutePath());
            }
        });
        _selectOutputDirButton.disableProperty().bind(disableProperty());
        outputDirHBox.getChildren().add(_selectOutputDirButton);
        _gridPane.add(outputDirHBox, 1, row);

        row++;
        _status = new Text();
        GridPane.setHalignment(_status, HPos.CENTER);
        _status.setFont(Font.font(12));
        _status.setFill(Color.RED);
        _status.setTextAlignment(TextAlignment.CENTER);
        _gridPane.add(_status, 0, row, 2, 1);

        _valid = new SimpleBooleanProperty(this, "valid", valid().valid());
        addChangeListener(() -> {
            Validity v = valid();
            if (v.valid()) {
                setStatusMessage(null);
            } else {
                setStatusMessage(v.reasonForIssue());
            }
            if (Platform.isFxApplicationThread()) {
                _valid.set(v.valid());
            } else {
                Platform.runLater(() -> {
                    _valid.set(v.valid());
                });
            }
        });

        notifyOfChangeInState();
    }

    public ReadOnlyBooleanProperty validProperty() {
        return _valid;
    }

    public BooleanProperty disableProperty() {
        return _disable;
    }

    public void setDisable(boolean disable) {
        if (Platform.isFxApplicationThread()) {
            _disable.set(disable);
        } else {
            Platform.runLater(() -> {
                _disable.set(disable);
            });
        }
    }

    public Validity valid() {
        Validity v = super.valid();
        if (v.valid()) {
            File outDir = _settings.outputDirectory();
            if (outDir == null) {
                v = new IsNotValid("Output folder is not selected.");
            } else if (!outDir.exists()) {
                v = new IsNotValid("Output folder " + outDir.getPath() + " does not exist.");
            }
        }
        return v;
    }

    public Manifest manifest() {
        return _manifest;
    }

    public CollectionDownloadSettings settings() {
        return _settings;
    }

    private void setStatusMessage(String message) {
        if (Platform.isFxApplicationThread()) {
            _status.setText(message);
        } else {
            Platform.runLater(() -> {
                _status.setText(message);
            });
        }
    }

    @Override
    public Parent gui() {
        return _gridPane;
    }

}
