package oceanus.apis;

import oceanus.sdk.core.discovery.node.Node;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Oceanus {
    void setNewObjectInterception(NewObjectInterception newObjectInterception);
    CompletableFuture<Void> init(ClassLoader classLoader);
    CompletableFuture<Void> init(String service, ClassLoader classLoader);
    void injectBean(Object bean);
    RPCManager getRPCManager();
    List<Node> getNodesByService(String service) throws CoreException;

    CompletableFuture<List<String>> getRegisteredServices();
}
