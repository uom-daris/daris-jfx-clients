package daris.client.gui.app;

import java.io.File;

import daris.client.download.manifest.Manifest;
import daris.client.gui.download.ManifestDownloadDialog;
import daris.client.gui.download.TokenDecryptDialog;
import daris.client.mf.connection.SSLSettings;
import daris.client.mf.session.LogonResponseHandler;
import daris.client.mf.session.Session;
import daris.client.mf.session.gui.DefaultErrorDialog;
import daris.client.mf.session.gui.DefaultLogonDialog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ManifestDownloadApp extends Application {

    public static final String APPLICATION_NAME = "daris-downloader";

    public static final String APPLICATION_TITLE = "DaRIS Downloader";

    public static void main(String[] args) {

        SSLSettings.setTrustAllCertificates(true);
        arc.mf.client.archive.Archive.declareSupportForAllTypes();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Session.initialize(APPLICATION_NAME, new DefaultLogonDialog(primaryStage, APPLICATION_TITLE),
                new DefaultErrorDialog(primaryStage, "Error"));

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        Manifest manifest = null;
        try {
            manifest = Manifest.loadFromResource();
            if (manifest == null) {
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    Alert alert = new Alert(AlertType.INFORMATION,
                            "Please select and open a manifest XML file. Click Ok to continue.");
                    alert.setTitle("Info");
                    alert.setHeaderText("Open manifest XML file");
                    alert.showAndWait();
                }
                FileChooser fc = new FileChooser();
                fc.setTitle("Open manifest XML file");
                fc.setInitialDirectory(new File(System.getProperty("user.home")));
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Manifest XML file", "*.xml"));
                File manifestFile = fc.showOpenDialog(primaryStage);
                if (manifestFile == null) {
                    primaryStage.close();
                    Platform.exit();
                    System.exit(0);
                    return;
                }
                manifest = Manifest.loadFromFile(manifestFile);
            }
        } catch (Throwable e) {
            Session.displayError(e.getMessage(), "loading manifest", e);
        }
        if (manifest.needToDecryptToken()) {
            Dialog<String> dlg = new TokenDecryptDialog(manifest.tokenEncrypted());
            dlg.showAndWait();
            String token = dlg.getResult();
            if (token == null) {
                primaryStage.close();
                Platform.exit();
                System.exit(0);
                return;
            } else {
                manifest.setToken(token);
            }
        }
        final Manifest mf = manifest;
        if (!mf.hasToken()) {
            Session.showLogonDialog(mf.serverHost(), mf.serverPort(), mf.serverTransport(), mf.domain(), mf.user(),
                    new LogonResponseHandler() {

                        @Override
                        public void logonSucceeded() {
                            Platform.runLater(() -> {
                                ManifestDownloadDialog dlg = new ManifestDownloadDialog(mf);
                                dlg.show(primaryStage);
                            });
                        }

                        @Override
                        public void logonFailed(Throwable error) {

                        }
                    });
        } else {
            ManifestDownloadDialog dlg = new ManifestDownloadDialog(mf);
            dlg.show(primaryStage);
        }
    }
}