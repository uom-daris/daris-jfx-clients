package daris.client.download.manifest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.Configuration.Transport;
import arc.xml.XmlDoc;

public class Manifest {

    public static final String DEFAULT_MANIFEST_NAME = "/download.manifest.xml";

    private String _name;
    private String _description;

    private String _host;
    private int _port;
    private Transport _transport;
    private String _token;
    private String _tokenEncrypted;
    private String _domain;
    private String _user;

    private List<String> _wheres;
    private List<Identifier> _ids;
    private Parts _parts;
    private String _outputPattern;
    private Map<String, String> _transcodes;
    private boolean _unarchive;

    public Manifest(XmlDoc.Element me) throws Throwable {

        _name = me.value("name");

        _description = me.value("description");

        _host = me.value("server/host");
        if (_host == null) {
            throw new Exception("Invalid manifest: missing server/host");
        }

        _port = me.intValue("server/port");
        if (_port <= 0) {
            throw new Exception("Invalid manifest: invalid server/port: " + _port);
        }

        String transport = me.value("server/transport");
        if (transport == null) {
            throw new Exception("Invalid manifest: missing server/transport");
        }
        if ("https".equalsIgnoreCase(transport)) {
            _transport = Transport.HTTPS_UNTRUSTED;
        } else if ("http".equalsIgnoreCase(transport)) {
            _transport = Transport.HTTP;
        } else {
            _transport = Transport.TCPIP;
        }

        _token = me.value("authentication/token");
        _tokenEncrypted = me.value("authentication/token-encrypted");
        _domain = me.value("authentication/domain");
        _user = me.value("authentication/user");
        if (_token == null && _tokenEncrypted == null && (_domain == null || _user == null)) {
            throw new Exception(
                    "Invalid manifest: missing authentication/token, or authentication/token-encrypted, or authentication/domain and authentication/user.");
        }
        if (me.elementExists("where")) {
            _wheres = new ArrayList<String>(me.values("where"));
        }
        _ids = Identifier.instantiate(me);
        _parts = Parts.fromString(me.value("parts"), Parts.CONTENT);
        _outputPattern = me.value("output-pattern");
        if (me.elementExists("transcode")) {
            List<XmlDoc.Element> tes = me.elements("transcode");
            _transcodes = new LinkedHashMap<String, String>(tes.size());
            for (XmlDoc.Element te : tes) {
                _transcodes.put(te.value("from"), te.value("to"));
            }
        }
        _unarchive = me.booleanValue("unarchive", false);
        if (!hasQuery() && !hasIds()) {
            throw new Exception("Invalid manifest: missing query (where) or asset id or cid.");
        }
    }

    public String name() {
        return _name;
    }

    public String description() {
        return _description;
    }

    public String serverHost() {
        return _host;
    }

    public int serverPort() {
        return _port;
    }

    public Transport serverTransport() {
        return _transport;
    }

    public String token() {
        return _token;
    }

    public void setToken(String token) {
        _token = token;
    }

    public String tokenEncrypted() {
        return _tokenEncrypted;
    }

    public boolean needToDecryptToken() {
        return _tokenEncrypted != null && _token == null;
    }

    public String domain() {
        return _domain;
    }

    public String user() {
        return _user;
    }

    public List<String> query() {
        return _wheres;
    }

    public List<Identifier> ids() {
        return _ids;
    }

    public boolean hasIds() {
        return _ids != null && !_ids.isEmpty();
    }

    public boolean hasQuery() {
        return _wheres != null && !_wheres.isEmpty();
    }

    public String outputPattern() {
        return _outputPattern;
    }

    public Map<String, String> transcodes() {
        return _transcodes;
    }

    public boolean hasTranscodes() {
        return _transcodes != null && !_transcodes.isEmpty();
    }

    public boolean unarchive() {
        return _unarchive;
    }

    public Parts parts() {
        return _parts;
    }

    public String serverAddress() {
        if (_host != null && _transport != null) {
            StringBuilder sb = new StringBuilder();
            if (_transport.name().toLowerCase().startsWith("https")) {
                sb.append("https://");
            } else if (_transport.name().toLowerCase().startsWith("tcp")) {
                sb.append("tcp://");
            } else {
                sb.append("http://");
            }
            sb.append(_host);
            sb.append(":").append(_port);
            return sb.toString();
        }
        return null;
    }

    public boolean hasToken() {
        return _token != null;
    }

    public static Manifest loadFromResource() throws Throwable {
        InputStream in = Manifest.class.getResourceAsStream(DEFAULT_MANIFEST_NAME);
        if (in == null) {
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            XmlDoc.Element e = new XmlDoc().parse(reader);
            return new Manifest(e);
        } finally {
            reader.close();
            in.close();
        }
    }

    public static Manifest loadFromFile(File f) throws Throwable {
        InputStream in = new FileInputStream(f);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            XmlDoc.Element e = new XmlDoc().parse(reader);
            return new Manifest(e);
        } finally {
            reader.close();
            in.close();
        }
    }

}
