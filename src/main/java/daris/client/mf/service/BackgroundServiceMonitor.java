package daris.client.mf.service;

import java.util.Timer;
import java.util.TimerTask;

import arc.xml.XmlDoc.Element;
import arc.xml.XmlStringWriter;
import daris.client.mf.session.ServiceResponseHandler;
import daris.client.mf.session.Session;
import daris.client.util.UnhandledException;

public class BackgroundServiceMonitor {
    private long _id;
    private java.util.Timer _t;
    private BackgroundServiceMonitorHandler _bsh;
    private int _nbFailuresToShow;

    public BackgroundServiceMonitor(long id, BackgroundServiceMonitorHandler bsh) {
        _id = id;
        _bsh = bsh;
        _t = null;
        _nbFailuresToShow = 1;
    }

    public void setNumberFailuresToShow(int nb) {
        _nbFailuresToShow = nb;
    }

    public void execute(int period) {
        cancel();

        _t = new Timer();
        _t.schedule(new TimerTask() {

            @Override
            public void run() {
                checkStatus();
            }
        }, 0, period);
    }

    public void cancel() {
        if (_t == null) {
            return;
        }

        _t.cancel();
        _t = null;
    }

    private void checkStatus() {
        XmlStringWriter w = new XmlStringWriter();
        try {
            w.add("id", _id);
            w.add("nb-failures-to-show", _nbFailuresToShow);

            Session.executeAsync("service.background.describe", w.document(), null, null, new ServiceResponseHandler() {

                @Override
                public void handleResult(Element xe) {

                    BackgroundService bs = null;
                    try {
                        bs = new BackgroundService(xe.element("task"));
                        _bsh.checked(bs);
                    } catch (Throwable e) {
                        UnhandledException.report(e.getMessage(), "instantiating background service", e);
                    } finally {
                        if (bs == null || bs.finished()) {
                            cancel();
                        }
                    }
                }

                @Override
                public boolean handleError(Throwable t) {
                    UnhandledException.report(t.getMessage(), "executing service: service.background.describe", t);
                    return true;
                }
            }, "executing service: service.background.describe");
        } catch (Throwable e) {
            UnhandledException.report(e.getMessage(), "executing service: service.background.describe", e);
        }
    }
}
