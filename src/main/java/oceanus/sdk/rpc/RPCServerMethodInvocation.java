package oceanus.sdk.rpc;

import oceanus.sdk.errors.ChatErrorCodes;
import oceanus.apis.CoreException;
import oceanus.sdk.rpc.remote.skeleton.ServiceSkeletonAnnotationHandler;
import oceanus.sdk.server.OnlineServer;

/**
 * Created by aplomb on 17-5-16.
 */
public class RPCServerMethodInvocation extends RPCServerAdapter<MethodRequest, MethodResponse> {

    private static final String TAG = RPCServerMethodInvocation.class.getSimpleName();

    @Override
    public MethodResponse onCall(MethodRequest request) throws CoreException {
        ServiceSkeletonAnnotationHandler.SkeletonMethodMapping methodMapping = getMethodMapping(request);
        MethodResponse response = methodMapping.invoke(request);
        return response;
    }

    public ServiceSkeletonAnnotationHandler.SkeletonMethodMapping getMethodMapping(MethodRequest request) throws CoreException{
        Long crc = request.getCrc();
//        if(crc == 0 || crc == -1)
//            throw new CoreException(CoreErrorCodes.ERROR_METHODREQUEST_CRC_ILLEGAL, "CRC is illegal for MethodRequest");
        String service = request.getService();
//        if(service == null)
//            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SERVICE_NULL, "Service is null for service_class_method " + ServerCacheManager.getInstance().getCrcMethodMap().get(crc));

        ServiceSkeletonAnnotationHandler serviceSkeletonAnnotationHandler = (ServiceSkeletonAnnotationHandler) OnlineServer.getInstance().getClassAnnotationHandler(ServiceSkeletonAnnotationHandler.class);
        if(serviceSkeletonAnnotationHandler == null)
            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_SKELETON_NULL, "Skeleton handler is not for service " + service + " on service_class_method ");
        ServiceSkeletonAnnotationHandler.SkeletonMethodMapping methodMapping = serviceSkeletonAnnotationHandler.getMethodMapping(crc);
        if(methodMapping == null)
            throw new CoreException(ChatErrorCodes.ERROR_METHODREQUEST_METHODNOTFOUND, "Method doesn't be found by service_class_method ");
        return methodMapping;
    }
}
