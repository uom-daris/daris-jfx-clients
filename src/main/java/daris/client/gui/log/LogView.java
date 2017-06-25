package daris.client.gui.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class LogView extends ListView<LogRecord> {

    java.util.logging.Formatter _formatter;

    public LogView() {
        _formatter = new java.util.logging.Formatter() {

            @Override
            public String format(LogRecord record) {
                StringBuilder sb = new StringBuilder();
                // sb.append(record.getThreadID());
                sb.append("[").append(new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(record.getMillis())))
                        .append("] ");
                sb.append(record.getMessage());
                return sb.toString();
            }
        };
        setCellFactory(param -> new ListCell<LogRecord>() {
            @Override
            protected void updateItem(LogRecord item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getMessage() == null) {
                    setText(null);
                } else {
                    setText(_formatter.format(item));
                }
            }
        });
        this.setStyle("-fx-font-family: Monaco, Courier New, Monospaced; -fx-font-size: 9pt;");
    }

}