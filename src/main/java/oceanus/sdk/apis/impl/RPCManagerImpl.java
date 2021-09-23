package oceanus.sdk.apis.impl;

import oceanus.apis.CoreException;
import oceanus.apis.RPCManager;
import oceanus.sdk.core.discovery.NodeRegistrationHandler;
import oceanus.sdk.errors.ChatErrorCodes;
import oceanus.sdk.logger.LoggerEx;
import oceanus.sdk.rpc.remote.stub.ServiceStubManager;
import oceanus.sdk.server.OnlineServer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RPCManagerImpl implements RPCManager {
    private static final String TAG = RPCManagerImpl.class.getSimpleName();

    @Override
    public <R> R callOneServer(String service, String clazz, String method, String onlyCallOneServer, Class<R> returnClass, Object... args) throws CoreException {
        ServiceStubManager serviceStubManager = OnlineServer.getInstance().getServiceStubManagerFactory().get();
        return serviceStubManager.call(service, clazz, method, onlyCallOneServer, returnClass, args);
    }

    @Override
    public <R> R call(String service, String clazz, String method, Class<R> returnClass, Object... args) throws CoreException {
        ServiceStubManager serviceStubManager = OnlineServer.getInstance().getServiceStubManagerFactory().get();
        return serviceStubManager.call(service, clazz, method, null, returnClass, args);
    }

    @Override
    public void callAllServers(Collection<String> services, String clazz, String method, Object... args) {
        NodeRegistrationHandler nodeRegistrationHandler = OnlineServer.getInstance().getNodeRegistrationHandler();
        if (nodeRegistrationHandler == null) {
            LoggerEx.error(TAG, "nodeRegistrationHandler is null while callAllServers on services " + services + " class " + clazz + " method " + method + " args count " + (args != null ? args.length : 0));
            return;
        }
        try {
            nodeRegistrationHandler.getNodesWithServices(services, null, true).thenAccept(serviceNodeResult -> {
                Map<String, List<Long>> serviceNodeCRCMap = serviceNodeResult.getServiceNodeCRCIds();

                if (serviceNodeCRCMap != null) {
                    for (Map.Entry<String, List<Long>> entry : serviceNodeCRCMap.entrySet()) {
                        List<Long> serverCrcs = entry.getValue();
                        if (serverCrcs != null) {
                            for (Long serverCrc : serverCrcs) {
                                try {
                                    callOneServer(entry.getKey(), clazz, method, String.valueOf(serverCrc), Object.class, args);
                                } catch (Throwable throwable) {
                                    LoggerEx.error(TAG, "callAllServers on service " + entry.getKey() + " serverCrc " + serverCrc + " class " + clazz + " method " + method + " args count " + (args != null ? args.length : 0) + " error " + throwable.getMessage());
                                }
                            }
                        }
                    }
                }
            }).get();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new CoreException(ChatErrorCodes.ERROR_CALL_ALL_SERVER_FAILED, "callAllServers failed " + " services " + services + " class " + clazz + " method " + method + " args " + Arrays.toString(args) + " errorMessage " + e.getMessage());
        }
    }

    @Override
    public void callAllServersAsync(Collection<String> services, String clazz, String method, Object... args) {
        NodeRegistrationHandler nodeRegistrationHandler = OnlineServer.getInstance().getNodeRegistrationHandler();
        if (nodeRegistrationHandler == null) {
            LoggerEx.error(TAG, "nodeRegistrationHandler is null while callAllServers on services " + services + " class " + clazz + " method " + method + " args count " + (args != null ? args.length : 0));
            return;
        }
        nodeRegistrationHandler.getNodesWithServices(services, null, true).thenAccept(serviceNodeResult -> {
            Map<String, List<Long>> serviceNodeCRCMap = serviceNodeResult.getServiceNodeCRCIds();

            if (serviceNodeCRCMap != null) {
                for (Map.Entry<String, List<Long>> entry : serviceNodeCRCMap.entrySet()) {
                    List<Long> serverCrcs = entry.getValue();
                    if (serverCrcs != null) {
                        for (Long serverCrc : serverCrcs) {
                            try {
                                callOneServer(entry.getKey(), clazz, method, String.valueOf(serverCrc), Object.class, args);
                            } catch (Throwable throwable) {
                                LoggerEx.error(TAG, "callAllServers on service " + entry.getKey() + " serverCrc " + serverCrc + " class " + clazz + " method " + method + " args count " + (args != null ? args.length : 0) + " error " + throwable.getMessage());
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public <S> S getService(String service, Class<S> sClass) {
        ServiceStubManager serviceStubManager = OnlineServer.getInstance().getServiceStubManagerFactory().get();
        return serviceStubManager.getService(service, sClass);
    }

    @Override
    public <S> S getService(String service, Class<S> sClass, String onlyCallOneServer) {
        ServiceStubManager serviceStubManager = OnlineServer.getInstance().getServiceStubManagerFactory().get();
        return serviceStubManager.getService(service, sClass, onlyCallOneServer);
    }
}
