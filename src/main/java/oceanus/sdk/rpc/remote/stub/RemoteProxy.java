package oceanus.sdk.rpc.remote.stub;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import oceanus.sdk.utils.ReflectionUtil;

import java.lang.reflect.Method;

public class RemoteProxy extends Proxy implements MethodInterceptor {
    private static final String TAG = RemoteProxy.class.getSimpleName();

    Enhancer enhancer = new Enhancer();

    public RemoteProxy(ServiceStubManager serviceStubManager, RemoteServerHandler remoteServerHandler) {
        super(serviceStubManager, remoteServerHandler);
    }


    public Object getProxy(Class clazz) {
        //设置需要创建的子类
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        //通过字节码技术动态创建子类实例
        return enhancer.create();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args,
                            MethodProxy proxy) throws Throwable {
        // TODO Auto-generated method stub
        if(method.getDeclaringClass().equals(Object.class)) {
            return proxy.invokeSuper(obj, args);
        }
        Long crc = ReflectionUtil.getCrc(method, remoteServerHandler.getToService());
        RpcCacheManager.getInstance().putCrcMethodMap(crc, remoteServerHandler.getToService() + '_' + method.getDeclaringClass().getSimpleName() + '_' + method.getName());
        return invoke(crc, args);
    }
}