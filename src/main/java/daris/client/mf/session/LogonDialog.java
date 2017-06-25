package daris.client.mf.session;

import arc.mf.client.Configuration.Transport;

public interface LogonDialog {

    void setServerHost(String host, boolean lock);

    void setServerPort(int port, boolean lock);

    void setServerTransport(Transport transport, boolean lock);

    void setDomain(String domain, boolean lock);

    void setUser(String user, boolean lock);

    void unlockAllFields();

    void display(LogonResultHandler rh);

}
