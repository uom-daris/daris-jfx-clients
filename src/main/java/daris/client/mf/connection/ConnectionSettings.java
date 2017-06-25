package daris.client.mf.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.Configuration.Transport;
import arc.xml.CanSaveToXml;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDocWriter;
import arc.xml.XmlWriter;
import daris.client.mf.authentication.IdentityProvider;

public class ConnectionSettings implements CanSaveToXml {

    public static final String LOCATION = System.getProperty("user.home") + File.separator + ".daris" + File.separator
            + "connection-settings.xml";

    private String _host;
    private int _port;
    private Transport _transport;
    private String _domain;
    private IdentityProvider _provider;
    private String _user;

    private ConnectionSettings(XmlDoc.Element se) throws Throwable {
        _host = se.value("host");
        String transport = se.value("transport");
        if (transport != null) {
            if (transport.toLowerCase().startsWith("tcp")) {
                _transport = Transport.TCPIP;
            } else if ("http".equalsIgnoreCase(transport)) {
                _transport = Transport.HTTP;
            } else if ("https".equalsIgnoreCase(transport)) {
                boolean allowUntrusted = se.booleanValue("transport/@allow-untrusted", true);
                if (!allowUntrusted) {
                    _transport = Transport.HTTPS_TRUSTED;
                } else {
                    _transport = Transport.HTTPS_UNTRUSTED;
                }
            }
        }
        _port = se.intValue("port");
        _domain = se.value("domain");
        if (se.elementExists("provider")) {
            _provider = new IdentityProvider(se.element("provider"));
        }
        _user = se.value("user");
    }

    public ConnectionSettings(String host, int port, Transport transport, String domain, IdentityProvider provider,
            String user) {
        _host = host;
        _port = port;
        _transport = transport;
        _domain = domain;
        _provider = provider;
        _user = user;
    }

    public String host() {
        return _host;
    }

    public int port() {
        return _port;
    }

    public Transport transport() {
        return _transport;
    }

    public boolean allowUntrusted() {
        return _transport == Transport.HTTPS_UNTRUSTED;
    }

    public boolean encrypt() {
        return _transport == Transport.HTTPS || _transport == Transport.HTTPS_TRUSTED
                || _transport == Transport.HTTPS_UNTRUSTED;
    }

    public String domain() {
        return _domain;
    }

    public String user() {
        return _user;
    }

    public IdentityProvider provider() {
        return _provider;
    }

    public void save(XmlWriter w) throws Throwable {
        w.push("server");
        w.add("host", _host);
        w.add("port", _port);
        String transport = _transport.name().toLowerCase().startsWith("https") ? "https"
                : (_transport.name().equalsIgnoreCase("http") ? "http" : "tcp/ip");
        String allowUntrusted = ("https".equalsIgnoreCase(transport) && allowUntrusted()) ? "true" : null;
        w.add("transport", new String[] { "allow-untrusted", allowUntrusted }, transport);
        w.add("domain", _domain);
        if (_provider != null) {
            _provider.save(w);
        }
        w.add("user", _user);
        w.pop();
    }

    private static Map<String, ConnectionSettings> _settings;

    public static void load() throws Throwable {
        if (_settings == null) {
            _settings = new LinkedHashMap<String, ConnectionSettings>();
        } else {
            _settings.clear();
        }
        File file = new File(LOCATION);
        if (file.exists()) {
            InputStreamReader reader = null;
            XmlDoc.Element xe = null;
            try {
                reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), "UTF-8");
                xe = new XmlDoc().parse(reader);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
            List<XmlDoc.Element> ses = xe.elements("server");
            if (ses != null && !ses.isEmpty()) {
                for (XmlDoc.Element se : ses) {
                    ConnectionSettings s = new ConnectionSettings(se);
                    _settings.put(s.host(), s);
                }
            }
        }
    }

    public static void save() {
        if (_settings == null || _settings.isEmpty()) {
            return;
        }
        try {
            XmlDocMaker dm = new XmlDocMaker("settings");
            XmlDocWriter xw = new XmlDocWriter(dm);
            for (ConnectionSettings s : _settings.values()) {
                s.save(xw);
            }
            XmlDoc.Element root = dm.root();
            OutputStreamWriter w = null;
            try {
                File file = new File(LOCATION);
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                w = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), "UTF-8");
                w.write(root.toString());
            } finally {
                if (w != null) {
                    w.close();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
    }

    public static void add(ConnectionSettings settings) {
        if (_settings == null) {
            try {
                load();
            } catch (Throwable e) {
                e.printStackTrace(System.err);
            }
        }
        if (_settings.containsKey(settings.host())) {
            _settings.remove(settings.host());
        }
        _settings.put(settings.host(), settings);
    }

    public static Map<String, ConnectionSettings> getAll() {
        if (_settings == null) {
            try {
                load();
            } catch (Throwable e) {
                e.printStackTrace(System.err);
            }
        }
        return Collections.unmodifiableMap(_settings);
    }

    public static ConnectionSettings getLast() {
        if (_settings == null) {
            try {
                load();
            } catch (Throwable ex) {
                ex.printStackTrace(System.err);
                return null;
            }
        }
        Iterator<ConnectionSettings> it = _settings.values().iterator();
        ConnectionSettings s = null;
        while (it.hasNext()) {
            s = it.next();
        }
        return s;
    }

    public static ConnectionSettings get(String host) {
        if (_settings != null) {
            return _settings.get(host);
        }
        return null;
    }

    public static List<String> getHosts() {
        Map<String, ConnectionSettings> settings = null;
        settings = getAll();
        List<String> hosts = new ArrayList<String>();
        if (settings != null) {
            hosts.addAll(settings.keySet());
        }
        return hosts;
    }

}
