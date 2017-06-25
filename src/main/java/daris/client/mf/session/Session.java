package daris.client.mf.session;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import arc.exception.ThrowableUtil;
import arc.mf.client.AuthenticationDetails;
import arc.mf.client.Configuration.Transport;
import arc.mf.client.ConnectionDetails;
import arc.mf.client.ConnectionSpec;
import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import arc.utils.ObjectUtil;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.mf.authentication.IdentityProvider;
import daris.client.mf.object.ObjectResolveHandler;
import daris.client.mf.session.gui.DefaultErrorDialog;
import daris.client.mf.session.gui.DefaultLogonDialog;

public class Session {

    public static final String DEFAULT_APP_NAME = "daris-client";

    public static final String DEFAULT_APP_TITLE = "DaRIS";

    public static final String DEFAULT_SAML_DOMAIN = "aaf";

    private static String _app;

    private static RemoteServer _rs;

    private static AuthenticationDetails _auth;

    private static String _sessionId;

    private static ErrorDialog _errorDialog;

    private static LogonDialog _logonDialog;

    private static Timer _timer;

    private static boolean _initialized = false;

    private static ThreadPoolExecutor _threadPool;

    public static ServerClient.Connection connect(String host, int port, Transport transport,
            AuthenticationDetails authDetails) throws Throwable {
        AuthenticationDetails ad = setApplication(authDetails, _app);
        if (_rs != null && !serverEquals(_rs.connectionDetails(), host, port, transport)) {
            _rs.discard();
            _rs = null;
            _auth = null;
        }
        if (_rs == null) {
            if (transport == Transport.HTTPS_TRUSTED) {
                ConnectionSpec.DEFAULT_ALLOW_UNTRUSTED_SERVER = false;
            } else {
                ConnectionSpec.DEFAULT_ALLOW_UNTRUSTED_SERVER = true;
            }
            boolean http = transport.name().toLowerCase().startsWith("http");
            boolean ssl = transport.name().toLowerCase().startsWith("https");
            _rs = new RemoteServer(host, port, http, ssl);
            _rs.setConnectionPooling(true);
        }
        ServerClient.Connection cxn = null;
        cxn = _rs.open();
        if (_sessionId == null) {
            cxn.connect(ad);
            _sessionId = cxn.sessionId();
            _auth = ad;
        } else {
            if (authEquals(_auth, ad)) {
                cxn.reconnect(_sessionId);
            } else {
                cxn.connect(ad);
                _sessionId = cxn.sessionId();
                _auth = ad;
            }
        }
        return cxn;
    }

    public static ServerClient.Connection connect(String host, int port, Transport transport, String domain,
            String user, String password) throws Throwable {
        return connect(host, port, transport, new AuthenticationDetails(_app, domain, user, password));
    }

    public static ServerClient.Connection connect(String host, int port, Transport transport, String token)
            throws Throwable {
        return connect(host, port, transport, new AuthenticationDetails(_app, token));
    }

    public static ServerClient.Connection connect() throws Throwable {
        if (_rs == null) {
            throw new ServerClient.ExNotConnected();
        }
        if (_auth == null) {
            throw new ServerClient.ExNotConnected();
        }
        ServerClient.Connection cxn = _rs.open();
        if (_sessionId != null) {
            try {
                cxn.reconnect(_sessionId);
                return cxn;
            } catch (Throwable e) {

            }
        }
        _sessionId = cxn.connect(_auth);
        return cxn;
    }

    public static void logon(String host, int port, Transport transport, AuthenticationDetails authDetails,
            LogonResponseHandler rh) {
        ServerClient.Connection cxn = null;
        try {
            cxn = connect(host, port, transport, authDetails);
            if (rh != null) {
                rh.logonSucceeded();
            }
        } catch (Throwable e) {
            if (rh != null) {
                rh.logonFailed(e);
            } else {
                displayError(null, "logging on Mediaflux", e);
            }
        } finally {
            if (cxn != null) {
                cxn.closeNe();
            }
        }
    }

    public static void logon(String host, int port, Transport transport, String domain, String user, String password,
            LogonResponseHandler rh) {
        logon(host, port, transport, new AuthenticationDetails(_app, domain, user, password), rh);
    }

    public static void logon(String host, int port, Transport transport, String token, LogonResponseHandler rh) {
        logon(host, port, transport, new AuthenticationDetails(_app, token), rh);
    }

    public static void execute(ServiceTask task) {
        ServerClient.Connection cxn = null;
        try {
            cxn = connect();
            XmlDoc.Element re = task.execute(cxn);
            task.responseHandler().handleResult(re);
        } catch (ServerClient.ExAborted | InterruptedException ex) {
            // System.out.println("Aborted service: " + sr.service());
        } catch (ServerClient.ExNotConnected | ServerClient.ExSessionInvalid e) {
            if ((_auth == null || _auth.token() == null) && _logonDialog != null) {
                _logonDialog.display(() -> {
                    execute(task);
                });
            } else {
                if (!task.responseHandler().handleError(e)) {
                    displayError(e.getMessage(), "executing service: " + task.service(), e);
                    ThrowableUtil.rethrowAsUnchecked(e);
                }
            }
        } catch (Throwable e) {
            if ((e instanceof ServerClient.ExIO) || (e instanceof IOException) || (e instanceof RemoteServer.ExConnect)
                    || (e instanceof SocketException) || (e instanceof XmlDoc.ExParseError)) {
                ping();
            }
            if (!task.responseHandler().handleError(e)) {
                displayError(e.getMessage(), "executing service: " + task.service(), e);
                ThrowableUtil.rethrowAsUnchecked(e);
            }
        } finally {
            if (cxn != null) {
                cxn.closeNe();
            }
        }
    }

    public static void execute(String service, String args, List<ServerClient.Input> inputs, ServerClient.Output output,
            ServiceResponseHandler rh, String context) {
        execute(new ServiceTask(service, args, inputs, output, rh, context));
    }

    public static ServiceRequest executeAsync(ServiceTask task) {
        _threadPool.submit(task);
        return task;
    }

    public static ServiceRequest executeAsync(String service, String args, List<ServerClient.Input> inputs,
            ServerClient.Output output, ServiceResponseHandler rh, String context) {
        return executeAsync(new ServiceTask(service, args, inputs, output, rh, context));
    }

    public static ServiceRequest executeAsync(String service, String args, List<ServerClient.Input> inputs,
            ServerClient.Output output, ServiceResultHandler rh, String context) {
        return executeAsync(new ServiceTask(service, args, inputs, output, new ServiceResponseHandler() {

            @Override
            public void handleResult(Element re) {
                rh.handleResult(re);
            }

            @Override
            public boolean handleError(Throwable t) {
                return false;
            }
        }, context));
    }

    public static void displayError(String message, String context, Throwable t) {
        if (_errorDialog != null) {
            _errorDialog.display(message == null ? t.getMessage() : message, context, t);
        } else {
            if (message != null) {
                System.err.println(message);
            }
            if (context != null) {
                System.err.println(context);
            }
            t.printStackTrace(System.err);
        }
    }

    public static void getIdentityProviders(String host, int port, Transport transport, String domain,
            ObjectResolveHandler<List<IdentityProvider>> rh) {
        if (host == null || port < 0 || domain == null || domain.isEmpty()) {
            if (rh != null) {
                rh.resolved(null);
            }
            return;
        }

        boolean useHttp = transport.name().toUpperCase().startsWith("HTTP");
        boolean encrypt = transport.name().toUpperCase().startsWith("HTTPS");
        RemoteServer server = new RemoteServer(host, port, useHttp, encrypt);
        ServerClient.Connection cxn = null;
        try {
            cxn = server.open();
            List<XmlDoc.Element> pes = cxn
                    .execute("authentication.domain.provider.list", "<domain>" + domain + "</domain>")
                    .elements("provider");
            if (pes != null && !pes.isEmpty()) {
                List<IdentityProvider> providers = new ArrayList<IdentityProvider>(pes.size());
                for (XmlDoc.Element pe : pes) {
                    providers.add(new IdentityProvider(pe));
                }
                if (providers != null) {
                    if (rh != null) {
                        rh.resolved(providers);
                        return;
                    }
                }
            }
            if (rh != null) {
                rh.resolved(null);
            }
        } catch (Throwable e) {
            if (rh != null) {
                rh.resolved(null);
            }
        }

    }

    public static void getIdentityProviders(String host, int port, Transport transport,
            ObjectResolveHandler<List<IdentityProvider>> rh) {
        getIdentityProviders(host, port, transport, DEFAULT_SAML_DOMAIN, rh);
    }

    public static void initialize() {
        initialize(DEFAULT_APP_NAME, new DefaultLogonDialog(null, DEFAULT_APP_TITLE),
                new DefaultErrorDialog(null, "Log on " + DEFAULT_APP_TITLE));
    }

    public static void initialize(String app, LogonDialog logonDialog, ErrorDialog errorDialog) {
        if (!_initialized) {
            _app = app;
            _logonDialog = logonDialog;
            _errorDialog = errorDialog;
            _threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            _initialized = true;
        }
    }

    public static void logoff() {
        if (_sessionId == null) {
            return;
        }
        ServerClient.Connection cxn = null;
        try {
            try {
                cxn = _rs.open();
                cxn.logoff();
            } finally {
                _sessionId = null;
                if (cxn != null) {
                    cxn.close();
                }
            }
        } catch (Throwable e) {
            displayError(e.getMessage(), "Logging off Mediaflux", e);
            ThrowableUtil.rethrowAsUnchecked(e);
        }
    }

    static void ping() {
        if (_timer == null) {
            _timer = new Timer();
        } else {
            _timer.cancel();
            _timer.purge();
        }
        _timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (_sessionId != null) {
                    execute("server.ping", null, null, null, new ServiceResponseHandler() {

                        public boolean handleError(Throwable e) {
                            ping();
                            return true;
                        }

                        public void handleResult(XmlDoc.Element re) {
                            _timer.cancel();
                            _timer.purge();
                            _timer = null;
                        }
                    }, "ping server");
                }
            }
        }, 0, 5000);
    }

    private static AuthenticationDetails setApplication(AuthenticationDetails authDetails, String app) {
        if (authDetails.token() != null) {
            return new AuthenticationDetails(app, authDetails.token());
        } else {
            return new AuthenticationDetails(app, authDetails.domain(), authDetails.userName(),
                    authDetails.userPassword());
        }
    }

    private static boolean serverEquals(ConnectionDetails cd, String host, int port, Transport transport)
            throws Throwable {
        if (cd != null && host != null && transport != null) {
            if (host.equalsIgnoreCase(cd.hostName()) || host.equalsIgnoreCase(cd.hostAddress())) {
                if (port == cd.port()) {
                    if (transport.name().toLowerCase().startsWith("https")) {
                        if (transport == Transport.HTTPS_TRUSTED) {
                            return cd.useHttp() && cd.encrypt() && !cd.allowUntrustedServer();
                        } else {
                            return cd.useHttp() && cd.encrypt() && cd.allowUntrustedServer();
                        }
                    } else if (transport.name().equalsIgnoreCase("http")) {
                        return cd.useHttp() && !cd.encrypt();
                    } else {
                        return !cd.useHttp();
                    }
                }
            }
        }
        return false;
    }

    private static boolean authEquals(AuthenticationDetails ad1, AuthenticationDetails ad2) {
        if (ad1 != null && ad2 != null && ObjectUtil.equals(ad1.application(), ad2.application())) {
            if (ad1.token() != null && ad2.token() != null) {
                return ad1.token().equals(ad2.token());
            } else {
                return ObjectUtil.equals(ad1.domain(), ad2.domain())
                        && ObjectUtil.equals(ad1.userName(), ad2.userName())
                        && ObjectUtil.equals(ad1.userPassword(), ad2.userPassword());
            }

        }
        return false;
    }

    public static LogonDialog logonDialog() {
        return _logonDialog;
    }

    public static void showLogonDialog(String host, int port, Transport transport, String domain, String user,
            LogonResponseHandler rh) {
        if (_logonDialog != null) {
            _logonDialog.setServerHost(host, host != null);
            _logonDialog.setServerPort(port, port > 0);
            _logonDialog.setServerTransport(transport, transport != null);
            _logonDialog.setDomain(domain, domain != null);
            _logonDialog.setUser(user, user != null);
            _logonDialog.display(rh);
        } else {
            throw new RuntimeException("No logon dialog is set for the session.");
        }
    }

    public static void shutdown() {
        _threadPool.shutdown();
    }

}
