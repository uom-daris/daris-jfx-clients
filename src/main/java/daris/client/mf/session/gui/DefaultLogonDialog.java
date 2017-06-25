package daris.client.mf.session.gui;

import java.util.List;

import arc.mf.client.Configuration.Transport;
import daris.client.mf.authentication.IdentityProvider;
import daris.client.mf.connection.ConnectionSettings;
import daris.client.mf.session.LogonDialog;
import daris.client.mf.session.LogonResponseHandler;
import daris.client.mf.session.LogonResultHandler;
import daris.client.mf.session.Session;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DefaultLogonDialog implements LogonDialog {

    private String _host;
    private Integer _port;
    private Transport _transport;
    private String _domain;
    private IdentityProvider _provider;
    private String _user;
    private String _password;
    private LogonResultHandler _rh;
    private List<IdentityProvider> _providers;

    private Stage _owner;

    private Stage _stage;
    private GridPane _gridPane;
    private Text _title;
    private Label _hostLabel;
    private ComboBox<String> _hostCombo;
    private Label _transportLabel;
    private ComboBox<String> _transportCombo;
    private Label _portLabel;
    private TextField _portField;
    private Label _domainLabel;
    private TextField _domainField;
    private Label _providerLabel;
    private ComboBox<IdentityProvider> _providerCombo;
    private Label _userLabel;
    private TextField _userField;
    private Label _passwordLabel;
    private PasswordField _passwordField;

    private Label _statusLabel;
    private Button _logonButton;

    public DefaultLogonDialog(Stage owner, String title) {

        _owner = owner;

        _host = null;
        _port = 80;
        _transport = Transport.HTTP;
        _domain = null;
        _provider = null;
        _user = null;
        _password = null;

        ConnectionSettings settings = ConnectionSettings.getLast();
        if (settings != null) {
            _host = settings.host();
            _transport = settings.transport();
            _port = settings.port() > 0 ? settings.port() : (settings.encrypt() ? 443 : 80);
            _domain = settings.domain();
            _provider = settings.provider();
            _user = settings.user();
        }

        /*
         * 
         */
        _gridPane = new GridPane();
        _gridPane.setAlignment(Pos.CENTER);
        _gridPane.setHgap(10);
        _gridPane.setVgap(10);
        _gridPane.setPadding(new Insets(25, 25, 25, 25));
        _gridPane.setBorder(new Border(new BorderStroke[] {
                new BorderStroke(Color.BEIGE, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT) }));

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHalignment(HPos.RIGHT);
        _gridPane.getColumnConstraints().add(cc);
        cc = new ColumnConstraints();
        cc.setHalignment(HPos.LEFT);
        _gridPane.getColumnConstraints().add(cc);

        /*
         * title
         */
        _title = new Text(title);
        _title.setFont(Font.font("Arial Black", FontWeight.EXTRA_BOLD, 16));
        GridPane.setConstraints(_title, 0, 0, 2, 1, HPos.LEFT, VPos.CENTER);

        /*
         * host
         */
        _hostLabel = new Label("Host:");
        _hostCombo = new ComboBox<String>();
        _hostCombo.setEditable(true);
        _hostCombo.setPromptText("address");
        _hostCombo.getItems().addAll(ConnectionSettings.getHosts());
        _hostCombo.setValue(_host);
        _hostCombo.setOnAction(event -> {
            String host = _hostCombo.getSelectionModel().getSelectedItem();
            if (host != null) {
                ConnectionSettings s = ConnectionSettings.get(host);
                if (s != null) {
                    _transportCombo.setValue(formatTransport(s.transport()));
                    _portField.setText(Integer.toString(s.port()));
                    _domainField.setText(s.domain());
                    _userField.setText(s.user());
                }
            }
        });
        _hostCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            _host = newValue;
            validate();
        });
        _hostCombo.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            _host = newValue;
            validate();
        });

        /*
         * transport
         */
        _transportLabel = new Label("Transport:");
        _transportCombo = new ComboBox<String>();
        _transportCombo.getItems().addAll(new String[] { "https", "http", "tcp/ip" });
        _transportCombo.setValue("https");
        _transportCombo.setOnAction((event) -> {
            String transport = _transportCombo.getSelectionModel().getSelectedItem();
            if ("https".equalsIgnoreCase(transport)) {
                _portField.setText(Integer.toString(443));
            } else if ("http".equalsIgnoreCase(transport)) {
                _portField.setText(Integer.toString(80));
            } else if (transport != null && transport.toLowerCase().startsWith("tcp")) {
                _portField.setText(Integer.toString(1967));
            }
        });
        _transportCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            _transport = parseTransport(newValue, true);
            validate();
        });

        /*
         * port
         */
        _portLabel = new Label("Port:");
        _portField = new TextField();
        _portField.textProperty().addListener((o, ov, nv) -> {
            if (!nv.matches("\\d*")) {
                _portField.setText(nv.replaceAll("[^\\d]", ""));
            }
        });
        _portField.setPrefWidth(90);
        _portField.setMaxWidth(90);
        _portField.setText(Integer.toString(_port));
        _portField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    newValue = newValue.replaceAll("[^\\d]", "").trim();
                    _portField.setText(newValue);
                    return;
                }
                if (newValue.isEmpty()) {
                    _port = null;
                } else {
                    _port = Integer.parseInt(newValue);
                }
                validate();
            }
        });
        _portField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String port = _portField.getText();
                if (port != null && !port.trim().isEmpty()) {
                    _domainField.requestFocus();
                }
            }
        });

        /*
         * domain
         */
        _domainLabel = new Label("Domain:");
        _domainField = new TextField();
        _domainField.setText(_domain);
        _domainField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                _domain = newValue;
                updateProviders(_domain);
                validate();
            }
        });
        _domainField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String domain = _domainField.getText();
                if (domain != null && !domain.trim().isEmpty()) {
                    _userField.requestFocus();
                }
            }
        });

        /*
         * provider
         */
        _providerLabel = new Label("Provider:");
        _providerCombo = new ComboBox<IdentityProvider>();
        if (_providers != null && !_providers.isEmpty()) {
            _providerCombo.getItems().setAll(_providers);
        }
        _providerCombo.setValue(_provider);
        _providerCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            _provider = newValue;
            validate();
        });

        /*
         * user
         */
        _userLabel = new Label("User:");
        _userField = new TextField();
        _userField.setText(_user);
        _userField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                _user = newValue;
                validate();
            }
        });
        _userField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String user = _userField.getText();
                if (user != null && !user.trim().isEmpty()) {
                    _passwordField.requestFocus();
                }
            }
        });

        /*
         * password
         */
        _passwordLabel = new Label("Password:");
        _passwordField = new PasswordField();
        _passwordField.setText(_password);
        _passwordField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                _password = newValue;
                validate();
            }
        });
        _passwordField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!_logonButton.disabledProperty().getValue()) {
                    _logonButton.requestFocus();
                }
            }
        });
        _passwordField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (validate()) {
                    doLogon(_rh);
                }
            }
        });

        _statusLabel = new Label();
        _statusLabel.setTextFill(Color.RED);

        _logonButton = new Button("Logon");
        _logonButton.setDisable(true);
        _logonButton.setDefaultButton(true);
        _logonButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                doLogon(_rh);
            }
        });

        /*
         * initial show
         */
        if (_domain != null) {
            updateProviders(_domain);
        } else {
            updateGUI();
        }

    }

    private static String formatTransport(Transport transport) {
        if (transport == null) {
            return null;
        }
        if (transport.name().toLowerCase().startsWith("https")) {
            return "https";
        } else if (transport.name().toLowerCase().startsWith("http")) {
            return "http";
        } else {
            return "tcp/ip";
        }
    }

    private static Transport parseTransport(String value, Boolean allowUntrusted) {
        if (value != null) {
            if ("https".equalsIgnoreCase(value)) {
                if (allowUntrusted == null) {
                    return Transport.HTTPS_UNTRUSTED;
                } else {
                    if (allowUntrusted) {
                        return Transport.HTTPS_UNTRUSTED;
                    } else {
                        return Transport.HTTPS_TRUSTED;
                    }
                }
            } else if ("http".equalsIgnoreCase(value)) {
                return Transport.HTTP;
            } else if (value.toLowerCase().startsWith("tcp")) {
                return Transport.TCPIP;
            }
        }
        return null;

    }

    private void updateGUI() {
        _gridPane.getChildren().clear();
        _gridPane.add(_title, 0, 0, 2, 1);
        _gridPane.addRow(1, _hostLabel, _hostCombo);
        _gridPane.addRow(2, _transportLabel, _transportCombo);
        _gridPane.addRow(3, _portLabel, _portField);
        _gridPane.addRow(4, _domainLabel, _domainField);
        if (_providers != null && !_providers.isEmpty()) {
            _providerCombo.getItems().setAll(_providers);
            if (_provider != null && _providers.contains(_provider)) {
                _providerCombo.setValue(_provider);
            } else {
                _provider = _providers.get(0);
                _providerCombo.setValue(_provider);
            }
            _gridPane.addRow(5, _providerLabel, _providerCombo);
            _gridPane.addRow(6, _userLabel, _userField);
            _gridPane.addRow(7, _passwordLabel, _passwordField);
            _gridPane.add(_statusLabel, 0, 8, 2, 1);
            _gridPane.add(_logonButton, 0, 9, 2, 1);
        } else {
            _gridPane.addRow(5, _userLabel, _userField);
            _gridPane.addRow(6, _passwordLabel, _passwordField);
            _gridPane.add(_statusLabel, 0, 7, 2, 1);
            _gridPane.add(_logonButton, 0, 8, 2, 1);
        }
    }

    private void updateProviders(final String domain) {
        if (domain == null || domain.isEmpty() || _host == null || _host.isEmpty() || _port == null || _port <= 0
                || _transport == null) {
            _providers = null;
            _provider = null;
            Platform.runLater(() -> {
                updateGUI();
            });
            return;
        }
        Session.getIdentityProviders(_host, _port, _transport, _domain, (providers) -> {
            _providers = providers;
            if (_providers == null || _providers.isEmpty()) {
                _provider = null;
            }
            Platform.runLater(() -> {
                updateGUI();
            });
        });

    }

    private void doLogon(LogonResultHandler rh) {
        _logonButton.setDisable(true);
        String provider = _provider == null ? null : _provider.id();
        String user = _user;
        if (provider != null) {
            int idx = _user.indexOf(":");
            if (idx < 0) {
                if (provider.contains(":")) {
                    provider = "[" + provider + "]";
                }
                user = provider.concat(":").concat(_user);
            }
        }
        Session.initialize();
        Session.logon(_host, _port, _transport, _domain, user, _password, new LogonResponseHandler() {

            @Override
            public void logonSucceeded() {
                Platform.runLater(() -> {
                    // hide the dialog
                    _logonButton.setDisable(false);
                    _stage.hide();
                });
                if (rh != null) {
                    rh.logonSucceeded();
                }
                // save settings to local profile.
                ConnectionSettings.add(new ConnectionSettings(_host, _port, _transport, _domain, _provider, _user));
                ConnectionSettings.save();
            }

            @Override
            public void logonFailed(Throwable error) {
                Platform.runLater(() -> {
                    _statusLabel.setText(error.getMessage());
                    _logonButton.setDisable(false);
                });
            }
        });
    }

    private boolean validate() {

        /*
         * validate
         */
        _logonButton.setDisable(true);
        _statusLabel.setText("");

        if (_host == null || _host.trim().isEmpty()) {
            _statusLabel.setText("Missing host address.");
            // _hostCombo.requestFocus();
            return false;
        }

        if (_transportCombo.getValue() == null) {
            _statusLabel.setText("Missing transport protocol.");
            // _transportCombo.requestFocus();
            return false;
        }

        if (_port <= 0) {
            _statusLabel.setText("Missing server port.");
            // _portField.requestFocus();
            return false;
        }

        if (_domain == null || _domain.trim().isEmpty()) {
            _statusLabel.setText("Missing authentication domain name.");
            // _domainField.requestFocus();
            return false;
        }

        if (_providers != null && _provider == null) {
            _statusLabel.setText("Missing identity provider.");
            // _providerCombo.requestFocus();
            return false;
        }

        if (_user == null || _user.trim().isEmpty()) {
            _statusLabel.setText("Missing user name.");
            // _userField.requestFocus();
            return false;
        }

        if (_password == null || _password.trim().isEmpty()) {
            _statusLabel.setText("Missing password.");
            // _passwordField.requestFocus();
            return false;
        }
        _logonButton.setDisable(false);
        return true;
    }

    @Override
    public void display(LogonResultHandler rh) {
        _rh = rh;
        Platform.runLater(() -> {
            if (_stage == null) {
                _stage = new Stage();
                _stage.initOwner(_owner);
                _stage.setTitle(_title.getText());
                // TODO:
                // _stage.initStyle(StageStyle.UTILITY);
                _stage.initModality(Modality.APPLICATION_MODAL);
                _stage.setScene(new Scene(_gridPane, 480, 520));
                _stage.setOnCloseRequest(event -> {
                    System.exit(0);
                });
            }
            _stage.show();
        });
    }

    @Override
    public void setServerHost(String host, boolean lock) {
        if (_hostCombo != null) {
            if (Platform.isFxApplicationThread()) {
                ObservableList<String> items = _hostCombo.itemsProperty().get();
                if (!items.contains(host)) {
                    items.add(host);
                }
                _hostCombo.setValue(host);
                if (lock) {
                    _hostCombo.setDisable(true);
                }
            } else {
                Platform.runLater(() -> {
                    setServerHost(host, lock);
                });
            }
        } else {
            _host = host;
        }
    }

    @Override
    public void setServerPort(int port, boolean lock) {
        if (_portField != null) {
            if (Platform.isFxApplicationThread()) {
                _portField.setText(Integer.toString(port));
                if (lock) {
                    _portField.setDisable(true);
                }
            } else {
                Platform.runLater(() -> {
                    setServerPort(port, lock);
                });
            }
        } else {
            _port = port;
        }
    }

    @Override
    public void setServerTransport(Transport transport, boolean lock) {
        _transport = transport;
        if (_transportCombo != null) {
            if (Platform.isFxApplicationThread()) {
                if (transport == null) {
                    _transportCombo.setValue(null);
                } else if (transport.name().toLowerCase().startsWith("https")) {
                    _transportCombo.setValue("https");
                } else if (transport.name().toLowerCase().startsWith("http")) {
                    _transportCombo.setValue("http");
                } else if (transport.name().toLowerCase().startsWith("tcp")) {
                    _transportCombo.setValue("tcp/ip");
                }
                if (lock) {
                    _transportCombo.setDisable(true);
                }
            } else {
                Platform.runLater(() -> {
                    setServerTransport(transport, lock);
                });
            }
        }
    }

    @Override
    public void setDomain(String domain, boolean lock) {
        if (_domainField != null) {
            if (Platform.isFxApplicationThread()) {
                _domainField.setText(domain);
                if (lock) {
                    _domainField.setDisable(true);
                }
                updateProviders(_domain);
            } else {
                Platform.runLater(() -> {
                    setDomain(domain, lock);
                });
            }
        } else {
            _domain = domain;
            updateProviders(_domain);
        }
    }

    @Override
    public void setUser(String user, boolean lock) {
        if (_userField != null) {
            if (Platform.isFxApplicationThread()) {
                _userField.setText(user);
                if (lock) {
                    _userField.setDisable(true);
                }
            } else {
                Platform.runLater(() -> {
                    setUser(user, lock);
                });
            }
        } else {
            _user = user;
        }
    }

    @Override
    public void unlockAllFields() {
        if (Platform.isFxApplicationThread()) {
            _hostCombo.setDisable(false);
            _portField.setDisable(false);
            _transportCombo.setDisable(false);
            _domainField.setDisable(false);
            if (_providerCombo != null) {
                _providerCombo.setDisable(false);
            }
            _userField.setDisable(false);
            _passwordField.setDisable(false);
        } else {
            Platform.runLater(() -> {
                unlockAllFields();
            });
        }

    }

}
