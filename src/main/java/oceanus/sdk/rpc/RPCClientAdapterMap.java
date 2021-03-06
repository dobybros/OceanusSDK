package oceanus.sdk.rpc;


import oceanus.apis.CoreException;
import oceanus.sdk.logger.LoggerEx;
import oceanus.sdk.rpc.impl.RMIClientHandlerEx;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This task is to hold each RPCClientAdapter in map which connecting to other servers.
 * If a RPCClientAdapter is idle too long time, the RPCClientAdapter will be disconnected and removed from the map.
 * Centralize the control to RPCClientAdapters for easily been added/removed.
 *
 * @author aplomb
 */
public class RPCClientAdapterMap {
    private static final String TAG = RPCClientAdapterMap.class.getSimpleName();

    private ConcurrentHashMap<String, RPCClientAdapter> clientAdapterMap = new ConcurrentHashMap<String, RPCClientAdapter>();

    private long expireTime = TimeUnit.MINUTES.toMillis(5);

    private boolean enableSsl = false;
    /**
     * rpc ssl certificate
     */
    private String rpcSslClientTrustJksPath;
    private String rpcSslServerJksPath;
    private String rpcSslJksPwd;

    public RPCClientAdapterMap() {
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(RPCClientAdapterMap.class.getSimpleName() + ": ");
        builder.append("enableSsl: " + enableSsl + " ");
        builder.append("expireTime: " + expireTime + " ");
        builder.append("clients: " + Arrays.toString(clientAdapterMap.values().toArray()) + " ");
        return builder.toString();
    }

    public RPCClientAdapter registerServer(String ip, int rmiPort, final String serverName) {
        return registerServer(ip, rmiPort, serverName, null);
    }

    public RPCClientAdapter getClientAdapter(String serverName) {
        return clientAdapterMap.get(serverName);
    }

    public RPCClientAdapter registerServer(String ip, int rmiPort, final String serverName, RPCClientAdapter.ClientAdapterStatusListener statusListener) {
        if (serverName == null)
            return null;
        RPCClientAdapter clientAdapter = clientAdapterMap.get(serverName);
        if (clientAdapter == null) {
            if (ip == null)
                return null;
            RMIClientHandlerEx rmiClient = new RMIClientHandlerEx();
            clientAdapter = rmiClient;
            synchronized (this) {
                RPCClientAdapter existingClientAdapter = clientAdapterMap.putIfAbsent(serverName, clientAdapter);
                if (existingClientAdapter == null) {
//                LoggerEx.info(TAG, "clientAdapterMap putIfAbsent returned existing clientAdapter " + existingClientAdapter + " close the new clientAdapter " + clientAdapter);
//                clientAdapter.clientDestroy();
//                clientAdapter = existingClientAdapter;
                    rmiClient.setRmiPort(rmiPort);
                    rmiClient.setServerHost(ip);
                    rmiClient.setRmiId(serverName);
                    rmiClient.setDisconnectedAfterRetryListener((handler) -> {
                        RPCClientAdapterMap.this.unregisterServer(serverName);
//                        if (removedHandler == null) {
//                            handler.clientDestroy();
//                        }
                    });
                    rmiClient.setExpireTime(expireTime, (handler, touch, expireTime) -> {
                        RPCClientAdapterMap.this.unregisterServer(serverName);
//                        if (removedHandler == null) {
//                            handler.clientDestroy();
//                        }
                        return true;
                    });
                    rmiClient.addStatusListener(statusListener);
                    try {
                        clientAdapter.clientStart();
                    } catch (CoreException e) {
                        e.printStackTrace();
                        LoggerEx.warn(TAG, "Client adapter client start failed, " + e.getMessage() + " but will continue retry, maybe available later.");
                    }
                } else {
                    clientAdapter = existingClientAdapter;
                }
            }
        } else {
            clientAdapter.addStatusListener(statusListener);
        }
        return clientAdapter;
    }

    public RPCClientAdapter unregisterServer(String serverName) {
        RPCClientAdapter handler = clientAdapterMap.remove(serverName);
        if (handler != null) {
            handler.clientDestroy();
        }
        return handler;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public Map<String, RPCClientAdapter> getClientAdapterMap() {
        return clientAdapterMap;
    }

    public void setClientAdapterMap(ConcurrentHashMap<String, RPCClientAdapter> clientAdapterMap) {
        this.clientAdapterMap = clientAdapterMap;
    }

    public void shutdown() {
        if (clientAdapterMap != null) {
            //It will block shut down when there are connection issues.
//			Set<Entry<String, RPCClientAdapter>> entries = clientAdapterMap.entrySet();
//			for(Entry<String, RPCClientAdapter> entry : entries) {
//				entry.getValue().clientDestroy();
//			}
            clientAdapterMap.clear();
        }
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    public String getRpcSslClientTrustJksPath() {
        return rpcSslClientTrustJksPath;
    }

    public void setRpcSslClientTrustJksPath(String rpcSslClientTrustJksPath) {
        this.rpcSslClientTrustJksPath = rpcSslClientTrustJksPath;
    }

    public String getRpcSslServerJksPath() {
        return rpcSslServerJksPath;
    }

    public void setRpcSslServerJksPath(String rpcSslServerJksPath) {
        this.rpcSslServerJksPath = rpcSslServerJksPath;
    }

    public String getRpcSslJksPwd() {
        return rpcSslJksPwd;
    }

    public void setRpcSslJksPwd(String rpcSslJksPwd) {
        this.rpcSslJksPwd = rpcSslJksPwd;
    }

}
