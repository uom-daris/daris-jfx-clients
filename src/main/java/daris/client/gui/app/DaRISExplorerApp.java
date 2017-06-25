package daris.client.gui.app;

import arc.xml.XmlDoc.Element;
import daris.client.mf.connection.SSLSettings;
import daris.client.mf.session.ServiceResultHandler;
import daris.client.mf.session.Session;
import daris.client.mf.session.gui.DefaultErrorDialog;
import daris.client.mf.session.gui.DefaultLogonDialog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class DaRISExplorerApp extends Application {

    public static final String APPLICATION_NAME = "daris-explorer";

    public static final String APPLICATION_TITLE = "DaRIS Explorer";

    public static void main(String[] args) {

        SSLSettings.setTrustAllCertificates(true);

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
        // TODO: incomplete, need to implement...
        // Session.displayError("TTTT", "CCCCCC", new Exception("aaa"));
        Session.executeAsync("server.version", null, null, null, new ServiceResultHandler() {

            @Override
            public void handleResult(Element re) {
                System.out.println(re);
//                Session.shutdown();
                Platform.runLater(() -> {
                    System.exit(0);
                });
            }
        }, null);

    }
}
