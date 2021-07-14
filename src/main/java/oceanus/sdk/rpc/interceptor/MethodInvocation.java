package oceanus.sdk.rpc.interceptor;

import oceanus.sdk.core.discovery.errors.CoreErrorCodes;
import oceanus.apis.CoreException;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

public class MethodInvocation {
    public final String methodKey;
    public final Object target;
    public final Class<?> clazz;
    public final Method method;
    public final Object[] arguments;
    public final List<MethodInterceptor> methodInterceptors;
    protected int currentInterceptorIndex = -1;

    public MethodInvocation(@Nullable Object target, @Nullable Class<?> clazz, Method method, @Nullable Object[] arguments, List<MethodInterceptor> methodInterceptors, String methodKey) {
        this.methodKey = methodKey;
        this.target = target;
        this.method = method;
        this.arguments = arguments;
        this.methodInterceptors = methodInterceptors;
        this.clazz = clazz;

    }

    @Nullable
    public Object proceed() throws CoreException {
        if (this.methodInterceptors == null || this.currentInterceptorIndex == this.methodInterceptors.size() - 1) {
            return invoke();
        } else {
            MethodInterceptor interceptor = this.methodInterceptors.get(++this.currentInterceptorIndex);
            if (interceptor != null) {
                return interceptor.invoke(this);
            }
        }
        return null;
    }

    public Object invoke() throws CoreException {
        return this.invokeMethod();
    }

    public Object invokeMethod() throws CoreException {
        if (target != null && method != null) {
            try {
                return method.invoke(target, arguments);
            } catch (Throwable e) {
                if(e instanceof CoreException) {
                    throw (CoreException) e;
                } else {
                    throw new CoreException(CoreErrorCodes.ERROR_RPC_SERVER_CALL_FAILED, "invoke method " + method + " class " + clazz + " failed, " + e.getMessage());
                }
            }
//            GroovyObject gObj = (GroovyObject) this.target;
//            //TODO Bind GroovyClassLoader base on current thread.
//            return gObj.invokeMethod(this.method, this.arguments);
        }
        return null;
    }

}
