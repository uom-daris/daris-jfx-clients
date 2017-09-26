package daris.client.gui.download;

import java.io.File;

import daris.client.download.CollectionDownloadTask;
import daris.client.download.manifest.Manifest;
import daris.client.download.manifest.Parts;
import daris.client.gui.log.LogHandler;
import daris.client.gui.log.LogView;
import daris.client.mf.session.LogonResponseHandler;
import daris.client.mf.session.Session;
import daris.util.ByteUtils;
import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ManifestDownloadDialog {

    private Stage _stage;
    private BorderPane _containerPane;
    private BorderPane _borderPane1;

    private ManifestDownloadSettingsForm _settingsForm;
    private Button _downloadButton;
    private ManifestDownloadProgressPane _progressPane;
    private LogView _logPane;

    private CollectionDownloadTask _task;

    public ManifestDownloadDialog(Manifest manifest) {

        _containerPane = new BorderPane();

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem openManifestFile = new MenuItem("Open manifest file...");
        openManifestFile.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Open manifest XML file");
            fc.setInitialDirectory(new File(System.getProperty("user.home")));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Manifest XML file", "*.xml"));
            File manifestFile = fc.showOpenDialog(_stage);
            if (manifestFile != null) {
                try {
                    Manifest mf = Manifest.loadFromFile(manifestFile);
                    if (!mf.hasToken()) {
                        Session.displayLogonDialog(mf.serverHost(), mf.serverPort(), mf.serverTransport(), mf.domain(),
                                mf.user(), new LogonResponseHandler() {

                                    @Override
                                    public void logonSucceeded() {
                                        setManifest(mf);
                                    }

                                    @Override
                                    public void logonFailed(Throwable error) {

                                    }
                                });
                    } else {
                        if (mf.needToDecryptToken()) {
                            Dialog<String> dlg = new TokenDecryptDialog(mf.tokenEncrypted());
                            dlg.showAndWait();
                            String token = dlg.getResult();
                            if (token != null) {
                                manifest.setToken(token);
                            }
                        }
                        setManifest(mf);
                    }
                } catch (Throwable t) {
                    Session.displayError(t.getMessage(), "loading manifest file: " + manifestFile.getAbsolutePath(), t, false);
                }
            }
        });
        fileMenu.getItems().add(openManifestFile);
        menuBar.getMenus().add(fileMenu);
        String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            menuBar.useSystemMenuBarProperty().set(true);
        }
        _containerPane.setTop(menuBar);

        _borderPane1 = new BorderPane();
        _borderPane1.setPrefHeight(380);

        _settingsForm = new ManifestDownloadSettingsForm(manifest);
        _borderPane1.setTop(_settingsForm.gui());

        HBox buttonHBox = new HBox();
        buttonHBox.setAlignment(Pos.BASELINE_CENTER);
        buttonHBox.setPrefHeight(40);
        buttonHBox.setPadding(new Insets(5, 10, 5, 10));
        _downloadButton = new Button("Download");
        _downloadButton.setDisable(!_settingsForm.valid().valid());
        _settingsForm.validProperty().addListener((obs, ov, nv) -> {
            _downloadButton.setDisable(!nv);
        });
        _downloadButton.setOnAction(e -> {
            if (_downloadButton.getText().equals("Download")) {
                _settingsForm.setDisable(true);
                _task = new CollectionDownloadTask(_settingsForm.manifest(), _settingsForm.settings());
                _task.addLogHandler(new LogHandler(_logPane));
                _task.stateProperty().addListener((obs, ov, nv) -> {
                    if (nv == State.CANCELLED || nv == State.SUCCEEDED || nv == State.FAILED) {
                        _downloadButton.setText("Download");
                        _downloadButton.setDisable(false);
                        _settingsForm.setDisable(false);
                        _progressPane.setText(nv.name().toLowerCase());
                    }
                });
                _progressPane.progressProperty().bind(_task.progressProperty());
                if (_settingsForm.settings().parts() == Parts.META) {
                    _task.processedObjectsProperty().addListener((obs, ov, nv) -> {
                        Platform.runLater(() -> {
                            long processedObjects = _task.processedObjectsProperty().get();
                            long totalObjects = _task.totalObjectsProperty().get();
                            _progressPane.setText("(" + processedObjects + "/" + totalObjects + " assets)");
                        });
                    });
                } else {
                    _task.receivedSizeProperty().addListener((obs, ov, nv) -> {
                        Platform.runLater(() -> {
                            long startTime = _task.startTimeProperty().get();
                            if (_task.getState() == State.RUNNING && startTime > 0) {
                                long currentTime = System.currentTimeMillis();
                                long totalReceived = _task.receivedSizeProperty().get();
                                double bps = ((double) totalReceived) / ((double) (currentTime - startTime) / 1000.0);
                                String speed = ByteUtils.getHumanReadableSize((long) bps);
                                _progressPane.setText(speed + "/s");
                            }
                        });
                    });
                }
                new Thread(_task).start();
                _downloadButton.setText("Abort");
            } else if (_downloadButton.getText().equals("Abort")) {
                _task.cancel();
                _downloadButton.setDisable(false);
                _settingsForm.setDisable(false);
                _downloadButton.setText("Download");
            }
        });
        buttonHBox.getChildren().add(_downloadButton);
        _borderPane1.setBottom(buttonHBox);

        _containerPane.setCenter(_borderPane1);

        BorderPane borderPane2 = new BorderPane();

        _progressPane = new ManifestDownloadProgressPane();
        _progressPane.setPrefHeight(40);
        _progressPane.prefWidthProperty().bind(borderPane2.widthProperty());
        _progressPane.setProgress(0);

        borderPane2.setTop(_progressPane);

        _logPane = new LogView();
        borderPane2.setCenter(_logPane);

        _containerPane.setBottom(borderPane2);
    }

    private void setManifest(Manifest manifest) {
        if (Platform.isFxApplicationThread()) {
            _settingsForm = new ManifestDownloadSettingsForm(manifest);
            _borderPane1.setTop(_settingsForm.gui());
            _downloadButton.setDisable(!_settingsForm.valid().valid());
            _settingsForm.validProperty().addListener((obs, ov, nv) -> {
                _downloadButton.setDisable(!nv);
            });
        } else {
            Platform.runLater(() -> {
                setManifest(manifest);
            });
        }
    }

    public void show(Stage stage) {
        _stage = stage;
        _stage.setTitle("DaRIS Downloader");
        Scene scene = new Scene(_containerPane, 800, 768);
        _stage.setScene(scene);
        _stage.show();
    }

}
