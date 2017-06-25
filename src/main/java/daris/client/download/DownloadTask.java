package daris.client.download;

import java.util.logging.Handler;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

public abstract class DownloadTask extends Task<Void> {

    public static final String TYPE_NAME = "download";

    protected Logger logger;

    private StringProperty _type;
    private LongProperty _createTime;
    private LongProperty _startTime; // enter RUNNING
    private LongProperty _endTime; // enter
    private LongProperty _totalSize;
    private LongProperty _processedSize;
    private LongProperty _receivedSize;
    private IntegerProperty _totalObjects;
    private IntegerProperty _processedObjects;

    public DownloadTask() {
        logger = Logger.getLogger(getClass().getSimpleName());
        logger.setUseParentHandlers(false);

        _type = new SimpleStringProperty(this, "type", "download");
        _createTime = new SimpleLongProperty(this, "createTime", System.currentTimeMillis());
        _startTime = new SimpleLongProperty(this, "startTime", 0L);
        _endTime = new SimpleLongProperty(this, "endTime", 0L);
        stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == State.RUNNING) {
                setStartTime(System.currentTimeMillis());
            } else if (newValue == State.SUCCEEDED) {
                setEndTime(System.currentTimeMillis());
            }
        });

        _totalSize = new SimpleLongProperty(this, "totalSize", -1L);
        _processedSize = new SimpleLongProperty(this, "processedSize", 0L);
        _receivedSize = new SimpleLongProperty(this, "receivedSize", 0L);
        _totalObjects = new SimpleIntegerProperty(this, "totalObjects", -1);
        _processedObjects = new SimpleIntegerProperty(this, "processedObjects", 0);
    }

    public void addLogHandler(Handler handler) {
        logger.addHandler(handler);
    }

    private void checkThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Task must only be used from the FX Application Thread");
        }
    }

    public ReadOnlyStringProperty typeProperty() {
        checkThread();
        return _type;
    }

    protected void setType(String type) {
        if (Platform.isFxApplicationThread()) {
            _type.set(type);
        } else {
            Platform.runLater(() -> {
                _type.set(type);
            });
        }
    }

    private void setStartTime(long time) {
        if (Platform.isFxApplicationThread()) {
            _startTime.set(time);
        } else {
            Platform.runLater(() -> {
                _startTime.set(time);
            });
        }
    }

    private void setEndTime(long time) {
        if (Platform.isFxApplicationThread()) {
            _endTime.set(time);
        } else {
            Platform.runLater(() -> {
                _endTime.set(time);
            });
        }
    }

    public ReadOnlyLongProperty createTimeProperty() {
        checkThread();
        return _createTime;
    }

    public ReadOnlyLongProperty startTimeProperty() {
        checkThread();
        return _startTime;
    }

    public ReadOnlyLongProperty totalSizeProperty() {
        checkThread();
        return _totalSize;
    }

    protected long totalSize() {
        checkThread();
        return _totalSize.get();
    }

    protected void setTotalSize(long totalSize) {
        checkThread();
        _totalSize.set(totalSize);
    }

    protected void updateTotalSize(long totalSize) {
        if (Platform.isFxApplicationThread()) {
            _totalSize.set(totalSize);
        } else {
            Platform.runLater(() -> {
                _totalSize.set(totalSize);
            });
        }
    }

    protected void incTotalSize(long increment, boolean updateProgress) {
        if (Platform.isFxApplicationThread()) {
            long totalSize = _totalSize.get() + increment;
            _totalSize.set(totalSize);
            if (updateProgress) {
                long processedSize = _processedSize.get();
                updateProgress(processedSize, totalSize);
            }
        } else {
            Platform.runLater(() -> {
                long totalSize = _totalSize.get() + increment;
                _totalSize.set(totalSize);
                if (updateProgress) {
                    long processedSize = _processedSize.get();
                    updateProgress(processedSize, totalSize);
                }
            });
        }
    }

    public ReadOnlyLongProperty processedSizeProperty() {
        checkThread();
        return _processedSize;
    }

    protected long processedSize() {
        checkThread();
        return _processedSize.get();
    }

    protected void setProcessedSize(long processedSize) {
        checkThread();
        _processedSize.set(processedSize);
    }

    protected void updateProcessedSize(long processedSize) {
        if (Platform.isFxApplicationThread()) {
            _processedSize.set(processedSize);
        } else {
            Platform.runLater(() -> {
                _processedSize.set(processedSize);
            });
        }
    }

    protected void incProcessedSize(long increment, boolean updateProgress) {
        if (Platform.isFxApplicationThread()) {
            long processedSize = _processedSize.get() + increment;
            _processedSize.set(processedSize);
            if (updateProgress) {
                long totalSize = _totalSize.get();
                updateProgress(processedSize, totalSize);
            }
        } else {
            Platform.runLater(() -> {
                long processedSize = _processedSize.get() + increment;
                _processedSize.set(processedSize);
                if (updateProgress) {
                    long totalSize = _totalSize.get();
                    updateProgress(processedSize, totalSize);
                }
            });
        }
    }

    public ReadOnlyLongProperty receivedSizeProperty() {
        checkThread();
        return _receivedSize;
    }

    protected long receivedSize() {
        checkThread();
        return _receivedSize.get();
    }

    protected void setReceivedSize(long receivedSize) {
        checkThread();
        _receivedSize.set(receivedSize);
    }

    protected void updateReceivedSize(long receivedSize) {
        if (Platform.isFxApplicationThread()) {
            _receivedSize.set(receivedSize);
        } else {
            Platform.runLater(() -> {
                _receivedSize.set(receivedSize);
            });
        }
    }

    protected void incReceivedSize(long increment) {
        if (Platform.isFxApplicationThread()) {
            long receivedSize = _receivedSize.get() + increment;
            _receivedSize.set(receivedSize);
        } else {
            Platform.runLater(() -> {
                long receivedSize = _receivedSize.get() + increment;
                _receivedSize.set(receivedSize);
            });
        }
    }

    public ReadOnlyIntegerProperty totalObjectsProperty() {
        checkThread();
        return _totalObjects;
    }

    protected int totalObjects() {
        checkThread();
        return _totalObjects.get();
    }

    protected void setTotalObjects(int totalObjects) {
        checkThread();
        _totalObjects.set(totalObjects);
    }

    protected void updateTotalObjects(int totalObjects) {
        if (Platform.isFxApplicationThread()) {
            _totalObjects.set(totalObjects);
        } else {
            Platform.runLater(() -> {
                _totalObjects.set(totalObjects);
            });
        }
    }

    public ReadOnlyIntegerProperty processedObjectsProperty() {
        checkThread();
        return _processedObjects;
    }

    protected int processedObjects() {
        checkThread();
        return _processedObjects.get();
    }

    protected void setProcessedObjects(int processedObjects) {
        checkThread();
        _processedObjects.set(processedObjects);
    }

    protected void updateProcessedObjects(int processedObjects) {
        if (Platform.isFxApplicationThread()) {
            _processedObjects.set(processedObjects);
        } else {
            Platform.runLater(() -> {
                _processedObjects.set(processedObjects);
            });
        }
    }

    protected void incProcessedObjects(boolean updateProgress) {
        if (Platform.isFxApplicationThread()) {
            int processedObjects = _processedObjects.get() + 1;
            _processedObjects.set(processedObjects);
            if (updateProgress) {
                long totalObjects = _totalObjects.get();
                updateProgress(processedObjects, totalObjects);
            }
        } else {
            Platform.runLater(() -> {
                int processedObjects = _processedObjects.get() + 1;
                _processedObjects.set(processedObjects);
                if (updateProgress) {
                    long totalObjects = _totalObjects.get();
                    updateProgress(processedObjects, totalObjects);
                }
            });
        }
    }

    protected void incProgress(long increment) {
        if (Platform.isFxApplicationThread()) {
            double workDone = getWorkDone();
            double totalWork = getTotalWork();
            workDone += (double) increment;
            updateProgress(workDone, totalWork);
        } else {
            Platform.runLater(() -> {
                double workDone = getWorkDone();
                double totalWork = getTotalWork();
                workDone += (double) increment;
                updateProgress(workDone, totalWork);
            });
        }
    }

    @Override
    protected Void call() throws Exception {
        if (isCancelled()) {
            updateMessage("Cancelled");
            return null;
        }
        try {
            execute();
            updateMessage("Succeeded");
        } catch (InterruptedException ie) {
            if (isCancelled()) {
                updateMessage("Cancelled");
                return null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            updateMessage("Failed with error: " + t.getMessage());
            if (t instanceof Exception) {
                throw (Exception) t;
            } else {
                throw new Exception(t);
            }
        }
        return null;
    }

    protected void checkIfCancelled() throws InterruptedException {
        if (isCancelled()) {
            throw new InterruptedException();
        }
    }

    protected abstract void execute() throws Throwable;

}
