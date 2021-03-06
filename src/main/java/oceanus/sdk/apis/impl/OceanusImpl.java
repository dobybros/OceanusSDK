package oceanus.sdk.apis.impl;

import oceanus.apis.CoreException;
import oceanus.apis.NewObjectInterception;
import oceanus.apis.Oceanus;
import oceanus.apis.RPCManager;
import oceanus.sdk.core.discovery.NodeRegistrationHandler;
import oceanus.sdk.core.discovery.node.Node;
import oceanus.sdk.core.discovery.node.Service;
import oceanus.sdk.core.discovery.node.ServiceNodeResult;
import oceanus.sdk.core.common.ErrorCodes;
import oceanus.sdk.core.net.errors.NetErrorCodes;
import oceanus.sdk.logger.LoggerEx;
import oceanus.sdk.rpc.impl.RMIServerHandler;
import oceanus.sdk.rpc.remote.skeleton.LookupServiceBeanAnnotationHandler;
import oceanus.sdk.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;
import oceanus.sdk.server.OnlineServer;
import oceanus.sdk.utils.OceanusProperties;
import oceanus.sdk.utils.annotation.ClassAnnotationHandler;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ConfigurationBuilder;

import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class OceanusImpl implements Oceanus {
    private static final String TAG = OceanusImpl.class.getSimpleName();
    private RPCManager rpcManager;
    private NewObjectInterception newObjectInterception;
    private ConcurrentHashMap<Object, ClassAnnotationHandler> classAnnotationHandlerMap = new ConcurrentHashMap<>();
    private OnlineServer onlineServer;
    private Reflections reflections;
    private AtomicBoolean isStarted = new AtomicBoolean(false);
    public OceanusImpl() {
        this(null);
    }
    public OceanusImpl(Properties properties) {
        OnlineServer onlineServer = OnlineServer.getInstance();
        if(onlineServer != null) {
            throw new IllegalStateException("Oceanus can NOT be initiated twice. ");
        }
        if(properties != null) {
            OceanusProperties.setProperties(properties);
        }
        this.onlineServer = new OnlineServer();
        ClassAnnotationHandler[] handlers = new ClassAnnotationHandler[]{
                new ServiceSkeletonAnnotationHandler(),
                new LookupServiceBeanAnnotationHandler(),
        };
        for(ClassAnnotationHandler handler : handlers) {
            classAnnotationHandlerMap.putIfAbsent(handler.getKey(), handler);
        }
        this.onlineServer.setClassAnnotationHandlerMap(classAnnotationHandlerMap);
    }
    @Override
    public void setNewObjectInterception(NewObjectInterception newObjectInterception) {
        this.newObjectInterception = newObjectInterception;
        OnlineServer.getInstance().setNewObjectInterception(newObjectInterception);
    }

    @Override
    public CompletableFuture<Void> init(ClassLoader classLoader) {
        return init(null, classLoader);
    }

    @Override
    public CompletableFuture<Void> init(String customService, ClassLoader classLoader) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            if(isStarted.compareAndSet(false, true)) {
                System.setProperty("sun.rmi.transport.tcp.handshakeTimeout", String.valueOf(30000));
                System.setProperty("sun.rmi.transport.tcp.responseTimeout", String.valueOf(TimeUnit.MINUTES.toMillis(1)));
                String serviceStr = null;
                if(customService == null) {
                    serviceStr = OceanusProperties.getInstance().getService();
                } else {
                    serviceStr = customService;
                }
                int rmiPort = OceanusProperties.getInstance().getRpcPort();
                if(rmiPort != -1) {
                    RMIServerHandler rmiServerHandler = new RMIServerHandler();
                    rmiServerHandler.setRmiPort(rmiPort);
                    rmiServerHandler.setServerName(OnlineServer.getInstance().getServer());
                    rmiServerHandler.setIpHolder(OnlineServer.getInstance().getIpHolder());
                    rmiServerHandler.serverStart();
                    LoggerEx.info(TAG, "Oceanus is in Provider/Consumer mode. Can invoke other providers but also able to be a provider. ");
                } else {
                    LoggerEx.info(TAG, "Oceanus is in Consumer mode. Can only invoke other providers but not able to be a provider. ");
                }
                Service service = new Service();
                service.setService(serviceStr);
                service.setStatus(Service.STATUS_DEPLOYED);
                service.setType(Service.TYPE_JAVA);
                service.setVersion(OceanusProperties.getInstance().getVersion());
                service.setMinVersion(0);
                service.setUploadTime(System.currentTimeMillis());
                OnlineServer.getInstance().setService(serviceStr);
                OnlineServer.getInstance().registerService(service).whenComplete((serviceRuntime, throwable) -> {
                    if(throwable != null) {
                        future.completeExceptionally(throwable);
                        LoggerEx.error(TAG, "Register service " + service);
                    } else {
                        prepareAnnotations(classLoader);

                        future.complete(null);
                    }
                });
            }
        } catch(Throwable t) {
            t.printStackTrace();
            future.completeExceptionally(t);
        }
        return future;
    }

    private void prepareAnnotations(ClassLoader classLoader) {
        reflections = new Reflections(new ConfigurationBuilder()
                .addScanners(new TypeAnnotationsScanner())
                .forPackages(OceanusProperties.getInstance().getScanPackage())
                .addClassLoader(classLoader));

        for(ClassAnnotationHandler classAnnotationHandler : classAnnotationHandlerMap.values()) {
            classAnnotationHandler.setReflections(reflections);
            try {
                classAnnotationHandler.handle();
            } catch (Throwable e) {
                e.printStackTrace();
                LoggerEx.error(TAG, "ClassAnnotationHandler " + classAnnotationHandler + " handle failed, " + e.getMessage());
            }
        }
    }

    @Override
    public void injectBean(Object bean) {
        throw new NotImplementedException();
    }

    @Override
    public RPCManager getRPCManager() {
        return rpcManager;
    }

    @Override
    public CompletableFuture<List<String>> getRegisteredServices() {
        NodeRegistrationHandler nodeRegistrationHandler = OnlineServer.getInstance().getNodeRegistrationHandler();
        if(nodeRegistrationHandler == null) {
            LoggerEx.error(TAG, "nodeRegistrationHandler is null while getRegisteredServices");
            CompletableFuture<List<String>> future = new CompletableFuture<>();
            future.completeExceptionally(new CoreException(NetErrorCodes.ERROR_NODE_UNREGISTERED, "nodeRegistrationHandler is null while getRegisteredServices"));
            return future;
        }
        return nodeRegistrationHandler.getRegisteredServices();
    }

    public void setRPCManager(RPCManager rpcManager) {
        this.rpcManager = rpcManager;
    }

   @Override
    public List<Node> getNodesByService(String service) throws CoreException {
        if (StringUtils.isEmpty(service)) {
            LoggerEx.error(TAG, "getNodesByService service is null");
            throw new CoreException("getNodesByService service is null");
        }
        NodeRegistrationHandler nodeRegistrationHandler = OnlineServer.getInstance().getNodeRegistrationHandler();
        if(null == nodeRegistrationHandler) {
            LoggerEx.error(TAG, "nodeRegistrationHandler is null while callAllServers on services " + service);
            throw new CoreException("nodeRegistrationHandler is null while callAllServers on services " + service);
        }
       CompletableFuture<ServiceNodeResult> future = nodeRegistrationHandler.getNodesWithServices(Collections.singletonList(service), null, false);
       try {
           ServiceNodeResult result = future.get();
           Map<String, List<Node>> serviceNodeCRCMap = result.getServiceNodes();
           List<Node> nodeList = serviceNodeCRCMap.get(service);
           return null == nodeList ? new ArrayList<>() : nodeList;
       } catch (Throwable e) {
           e.printStackTrace();
           LoggerEx.error(TAG, "getNodesByService when getNodesWithServices " + service + " failed, " + e.getMessage());
       }
       return new ArrayList<>();
    }
}
