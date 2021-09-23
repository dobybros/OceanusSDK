package oceanus.apis;

import java.util.Collection;

public interface RPCManager {
    <R> R callOneServer(String service, String clazz, String method, String onlyCallOneServer, Class<R> returnClass, Object... args) throws CoreException;

    <R> R call(String service, String clazz, String method, Class<R> returnClass, Object... args) throws CoreException;

    void callAllServers(Collection<String> services, String clazz, String method, Object... args);

    void callAllServersAsync(Collection<String> services, String clazz, String method, Object... args);

    <S> S getService(String service, Class<S> sClass);

    <S> S getService(String service, Class<S> sClass, String onlyCallOneServer);
}