package daris.client.gui.download;

import daris.util.CryptoUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class TokenDecryptDialog extends Dialog<String> {

    private String _encryptedToken;
    private String _token;

    public TokenDecryptDialog(String encryptedToken) {
        
        _encryptedToken = encryptedToken;

        setTitle("Decrypt token");
        setHeaderText("Token is encrypted. Enter the password to decrypt.");

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setMaxWidth(Double.MAX_VALUE);

        PasswordField password = new PasswordField();
        password.setPromptText("Token Password");

        grid.add(new Label("Password:"), 0, 0);
        grid.add(password, 1, 0);

        Text status = new Text();
        grid.add(status, 0, 1, 2, 1);

        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        password.textProperty().addListener((obs, ov, nv) -> {
            okButton.setDisable(true);
            if (nv == null || nv.trim().isEmpty()) {
                status.setText("Missing or empty password!");
            } else {
                try {
                    _token = CryptoUtils.decrypt(_encryptedToken, nv);
                    status.setText(null);
                    okButton.setDisable(false);
                } catch (Throwable e) {
                    status.setText("Invalid password!");
                }
            }
        });

        getDialogPane().setContent(grid);

        Platform.runLater(() -> password.requestFocus());
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return _token;
            }
            return null;
        });
    }

}
