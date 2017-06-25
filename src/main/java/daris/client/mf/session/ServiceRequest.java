package daris.client.mf.session;

import java.util.List;
import java.util.concurrent.RunnableFuture;

import arc.mf.client.ServerClient;

public interface ServiceRequest extends RunnableFuture<Void> {

    String context();

    String service();

    String args();

    List<ServerClient.Input> inputs();

    ServerClient.Output output();

    ServiceResponseHandler responseHandler();

    void execute() throws Throwable;

}
