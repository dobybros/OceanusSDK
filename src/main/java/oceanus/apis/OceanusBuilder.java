package oceanus.apis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OceanusBuilder {
    private NewObjectInterception newObjectInterception;

    public OceanusBuilder withNewObjectInterception(NewObjectInterception newObjectInterception) {
        this.newObjectInterception = newObjectInterception;
        return this;
    }
    public Oceanus build() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String oceanusClass = "oceanus.sdk.apis.impl.OceanusImpl";
        String rpcManagerClass = "oceanus.sdk.apis.impl.RPCManagerImpl";
        Class<?> theOceanusClass, theRPCManagerClass;
        theOceanusClass = Class.forName(oceanusClass);
        theRPCManagerClass = Class.forName(rpcManagerClass);
        RPCManager rpcManager = (RPCManager) theRPCManagerClass.getConstructor().newInstance();
        Oceanus oceanus = (Oceanus) theOceanusClass.getConstructor().newInstance();
        Method setRPCManagerMethod = theOceanusClass.getMethod("setRPCManager", RPCManager.class);
        setRPCManagerMethod.invoke(oceanus, rpcManager);
        Method setNewObjectInterceptionMethod = theOceanusClass.getMethod("setNewObjectInterception", NewObjectInterception.class);
        setNewObjectInterceptionMethod.invoke(oceanus, this.newObjectInterception);
        return oceanus;
    }
}
