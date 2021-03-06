package oceanus.sdk.rpc.remote.stub;

import oceanus.sdk.rpc.interceptor.MethodInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RPCInterceptorFactory {
    private Map<String, Map<String, List<MethodInterceptor>>> allMethodInterceptorMap = new ConcurrentHashMap<>();
    private static RPCInterceptorFactory instance;
    public void addMethodInterceptor(String serviceName, String key, MethodInterceptor methodInterceptor) {
        if (serviceName != null && key != null && methodInterceptor != null) {
            if (allMethodInterceptorMap.containsKey(serviceName)) {
                Map<String, List<MethodInterceptor>> interceptorMap = allMethodInterceptorMap.get(serviceName);
                if (interceptorMap.get(key) != null) {
                    List<MethodInterceptor> methodInterceptors = interceptorMap.get(key);
                    if (methodInterceptors != null) {
                        methodInterceptors.add(methodInterceptor);
                    }
                } else {
                    List methodInterceptors = new ArrayList<MethodInterceptor>();
                    methodInterceptors.add(methodInterceptor);
                    interceptorMap.put(key, methodInterceptors);
                }
            } else {
                Map<String, List<MethodInterceptor>> methodInterceptorMap = new ConcurrentHashMap<>();
                List methodInterceptors = new ArrayList<MethodInterceptor>();
                methodInterceptors.add(methodInterceptor);
                methodInterceptorMap.put(key, methodInterceptors);
                allMethodInterceptorMap.putIfAbsent(serviceName, methodInterceptorMap);
            }
        }
    }

    public Map<String, Map<String, List<MethodInterceptor>>> getAllMethodInterceptorMap() {
        return allMethodInterceptorMap;
    }

    public void setAllMethodInterceptorMap(Map<String, Map<String, List<MethodInterceptor>>> allMethodInterceptorMap) {
        this.allMethodInterceptorMap = allMethodInterceptorMap;
    }
    public synchronized static RPCInterceptorFactory getInstance() {
        if(instance == null){
            instance = new RPCInterceptorFactory();
        }
        return instance;
    }
}
