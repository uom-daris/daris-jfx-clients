package daris.client.gui.log;

import java.util.logging.LogRecord;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class LogHandler extends java.util.logging.Handler {

    public static final int MAX_ENTRIES = 10000;

    private ListProperty<LogRecord> _records = new SimpleListProperty<LogRecord>();
    private LogView _view;

    public LogHandler(LogView logView) {
        _records.set(FXCollections.observableArrayList());
        _view = logView;
        _view.itemsProperty().bind(_records);
    }

    public ReadOnlyListProperty<LogRecord> recordsProperty() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Task must only be used from the FX Application Thread");
        }
        return _records;
    }

    @Override
    public void publish(LogRecord record) {
        if (Platform.isFxApplicationThread()) {
            int size = _records.size();
            if (size == MAX_ENTRIES) {
                _records.remove(size - 1);
            }
            _records.add(0, record);
        } else {
            Platform.runLater(() -> {
                int size = _records.size();
                if (size == MAX_ENTRIES) {
                    _records.remove(size - 1);
                }
                _records.add(0, record);
            });
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {

    }

    public LogView view() {
        return _view;
    }

}
