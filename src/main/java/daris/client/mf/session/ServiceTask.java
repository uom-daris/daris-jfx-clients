package daris.client.mf.session;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import arc.mf.client.RequestOptions;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Input;
import arc.mf.client.ServerClient.Output;
import arc.utils.AbortableOperationHandler;
import arc.utils.CanAbort;
import arc.xml.XmlDoc;

public class ServiceTask implements ServiceRequest {

    private FutureTask<Void> _task;
    private CanAbort _ca;

    private String _service;
    private String _args;
    private List<ServerClient.Input> _inputs;
    private ServerClient.Output _output;
    private String _context;
    private ServiceResponseHandler _rh;

    private ServerClient.Connection _cxn;

    public ServiceTask(String service, String args, List<ServerClient.Input> inputs, ServerClient.Output output,
            ServiceResponseHandler rh, String context) {
        _service = service;
        _args = args;
        _inputs = inputs;
        _output = output;
        _context = context;
        _rh = rh;

        _task = new FutureTask<Void>(() -> {
            execute();
        }, null);
    }

    @Override
    public String context() {
        return _context;
    }

    @Override
    public String service() {
        return _service;
    }

    @Override
    public String args() {
        return _args;
    }

    @Override
    public List<Input> inputs() {
        return _inputs;
    }

    @Override
    public Output output() {
        return _output;
    }

    @Override
    public ServiceResponseHandler responseHandler() {
        return _rh;
    }

    XmlDoc.Element execute(ServerClient.Connection cxn) throws Throwable {
        _cxn = cxn;
        RequestOptions requestOps = new RequestOptions();
        requestOps.setAbortHandler(new AbortableOperationHandler() {

            @Override
            public void finished(CanAbort ca) {
                if (isCancelled() && _cxn != null) {
                    _cxn.closeNe();
                }
            }

            @Override
            public void started(CanAbort ca) {
                _ca = ca;
            }
        });
        return _cxn.executeMultiInput(null, service(), args(), inputs(), output(), requestOps);
    }

    @Override
    public void execute() {
        Session.execute(this);
    }

    @Override
    public void run() {
        _task.run();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (_ca != null) {
            try {
                _ca.abort();
            } catch (Throwable e) {

            }
            _ca = null;
        }
        return _task.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return _task.isCancelled();
    }

    @Override
    public boolean isDone() {
        return _task.isDone();
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        return _task.get();
    }

    @Override
    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return _task.get(timeout, unit);
    }

    public Future<?> submit(ExecutorService executor) {
        return executor.submit(_task);
    }

    public void execute(ExecutorService executor) {
        executor.execute(_task);
    }

    public boolean hasResponseHandler() {
        return _rh != null;
    }

}
