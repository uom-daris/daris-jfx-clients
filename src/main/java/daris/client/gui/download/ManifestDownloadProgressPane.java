package daris.client.gui.download;

import daris.client.gui.InterfaceComponent;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class ManifestDownloadProgressPane extends StackPane implements InterfaceComponent {

    private Text _progressText;
    private ProgressBar _progressBar;

    public ManifestDownloadProgressPane() {

        _progressText = new Text();
        _progressText.setTextAlignment(TextAlignment.CENTER);

        _progressBar = new ProgressBar();
        _progressBar.setMaxWidth(Double.MAX_VALUE);
        _progressBar.prefHeightProperty().bind(heightProperty());
        _progressBar.prefWidthProperty().bind(widthProperty().subtract(40));

        getChildren().addAll(_progressBar, _progressText);

    }

    @Override
    public Parent gui() {
        return this;
    }

    public DoubleProperty progressProperty() {
        return _progressBar.progressProperty();
    }

    public StringProperty textProperty() {
        return _progressText.textProperty();
    }

    public void setText(String text) {
        if (Platform.isFxApplicationThread()) {
            _progressText.setText(text);
        } else {
            Platform.runLater(() -> {
                _progressText.setText(text);
            });
        }
    }

    public void setProgress(double progress) {
        if (Platform.isFxApplicationThread()) {
            _progressBar.setProgress(progress);
        } else {
            Platform.runLater(() -> {
                _progressBar.setProgress(progress);
            });
        }
    }

}
