package daris.client.mf.session.gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class DefaultErrorDialog implements daris.client.mf.session.ErrorDialog {

    public static final String DEFAULT_TITLE = "Error";

    private Stage _owner;
    private String _title;

    public DefaultErrorDialog(Stage owner, String title) {

        _owner = owner;
        _title = title;
    }

    @Override
    public void display(String message, String context, Throwable e) {
        String msg = message == null ? (e == null ? null : e.getMessage()) : message;
        String exceptionClassName = e == null ? null : e.getClass().getName();
        String stackTrace = getStackTrace(e);
        if (Platform.isFxApplicationThread()) {
            showDialog(msg, context, exceptionClassName, stackTrace);
        } else {
            Platform.runLater(() -> {
                showDialog(msg, context, exceptionClassName, stackTrace);
            });
        }
    }

    private void showDialog(String message, String context, String exceptionClassName, String stackTrace) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(_title);
        alert.setHeaderText("Error: " + message);
        alert.setContentText(
                "Context: " + context + "\nException: " + (exceptionClassName == null ? "" : exceptionClassName));
        alert.getDialogPane().setExpanded(false);
       
        if (_owner.getScene() != null) {
            alert.initOwner(_owner);
        }

        GridPane stackTraceGridPane = new GridPane();
        stackTraceGridPane.setMaxWidth(Double.MAX_VALUE);

        Label stackTraceLabel = new Label("Stack Trace:");
        stackTraceLabel.setStyle("-fx-font-weight: BOLD;");

        stackTraceGridPane.add(stackTraceLabel, 0, 0);

        TextArea stackTraceTextArea = new TextArea(stackTrace);
        stackTraceTextArea.setEditable(false);
        stackTraceTextArea.setWrapText(true);
        stackTraceTextArea.setMaxWidth(Double.MAX_VALUE);
        stackTraceTextArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(stackTraceTextArea, Priority.ALWAYS);
        GridPane.setHgrow(stackTraceTextArea, Priority.ALWAYS);

        stackTraceGridPane.add(stackTraceTextArea, 0, 1);

        alert.getDialogPane().setExpandableContent(stackTraceGridPane);

        alert.showAndWait();
    }

    private static String getStackTrace(Throwable e) {
        if (e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            try {
                e.printStackTrace(pw);
                String stackTrace = sw.toString();
                return stackTrace;
            } finally {
                pw.close();
            }
        }
        return null;
    }

}
