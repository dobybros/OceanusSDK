package oceanus.sdk.network.channels.gateway;

import com.alibaba.fastjson.JSON;

import java.util.LinkedHashMap;
import java.util.Map;

@CompileStatic
public class UserActionHandler extends GroovyObjectSupport implements Handler<UserAction> {
    public UserActionHandler() {

    }

    @Override
    public void error(UserAction roomAction, final Throwable throwable) {
        LoggerEx.invokeMethod("error", new Object[]{getTAG(), "error occurred " + String.valueOf(throwable) + " message " + throwable.getMessage() + " roomAction " + String.valueOf(roomAction)});
    }

    @Override
    public void execute(final UserAction userAction) throws CoreException {
        switch (userAction.getAction()) {
            case 10:
                userAction.getHandler().touch();
                try {
                    userAction.getHandler().onSessionCreated();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onSessionCreated failed, " + throwable.getMessage() + ", id " + userAction.getUserId() + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case 20:
                userAction.getHandler().touch();
                try {
                    userAction.getHandler().onChannelConnected();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onChannelConnected failed, " + throwable.getMessage() + ", id " + userAction.getUserId() + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case 30:
                try {
                    userAction.getHandler().onChannelDisconnected();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onChannelDisconnected failed, " + throwable.getMessage() + ", id " + userAction.getUserId() + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case 40:
                try {
                    userAction.getHandler().onSessionDestroyed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onSessionDestroyed failed, " + throwable.getMessage() + ", id " + userAction.getUserId() + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case 1000:
                if (userAction.getClosure().asBoolean()) {
                    userAction.getHandler().touch();
                    try {
                        userAction.getClosure().invokeMethod("call", new Object[]{userAction.getClosureArgs()});
                    } catch (Throwable throwable1) {
                        throwable1.printStackTrace();
                        LoggerEx.invokeMethod("error", new Object[]{getTAG(), "userClosure call failed, closure " + String.valueOf(userAction.getClosure()) + " args " + String.valueOf(userAction.getClosureArgs()) + " error " + throwable1.getMessage()});
                    }

//                    userAction.closure.getMetaClass().invokeMethod(this, "doCall", userAction.closureArgs)
                }

                break;
            case 105:
                userAction.getHandler().touch();
                try {
                    Map jsonObject = JSON.class.invokeMethod("parseObject", new Object[]{userAction.getIncomingData().content});
                    final Reference<ResultData> resultData = new groovy.lang.Reference<ResultData>(userAction.getHandler().onDataReceived(userAction.getIncomingData().contentType, jsonObject, userAction.getIncomingData().id));
                    if (resultData.get() == null) {
                        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(2);
                        map.put("code", ResultData.CODE_SUCCESS);
                        map.put("forId", userAction.incomingData.id);
                        resultData.set(new ResultData(map));
                    }

                    if (resultData.get().code == null) resultData.get().code = ResultData.CODE_SUCCESS;
                    resultData.get().forId = userAction.getIncomingData().id;
                    boolean bool = gatewayChannelModule.invokeMethod("sendResultData", new Object[]{userAction.getUserId(), resultData.get()});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send result not successfully to userId " + userAction.getUserId() + ", code " + String.class.invokeMethod("valueOf", new Object[]{resultData.get().code}) + " dataLength " + String.class.invokeMethod("valueOf", new Object[]{resultData.get().data})});
                    }

                } catch (Throwable throwable) {
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onDataReceived contentType " + String.class.invokeMethod("valueOf", new Object[]{userAction.getIncomingData().contentType}) + " failed, " + throwable.getMessage()});
                    LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>(3);
                    map.put("forId", userAction.incomingData.id);
                    map.put("time", System.currentTimeMillis());
                    map.put("contentEncode", userAction.incomingData.contentEncode);
                    Result errorResult = new Result(map);
                    if (throwable instanceof CoreException) {
                        errorResult.code = ((CoreException) throwable).code;
                    } else {
                        errorResult.code = Errors.ERROR_UNKNOWN;
                    }

                    boolean bool = gatewayChannelModule.invokeMethod("sendData", new Object[]{userAction.getUserId(), errorResult});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send errorResult not successfully to userId " + userAction.getUserId() + ", code " + String.class.invokeMethod("valueOf", new Object[]{errorResult.code}) + " throwable " + throwable.getMessage()});
                    }

                }

                break;
            case 100:
                userAction.getHandler().touch();
                try {
                    Map jsonObject = JSON.class.invokeMethod("parseObject", new Object[]{userAction.getIncomingMessage().content});
                    final Reference<ResultData> resultData = new groovy.lang.Reference<ResultData>(userAction.getHandler().onMessageReceived(userAction.getIncomingMessage().toUserId, userAction.getIncomingMessage().toGroupId, userAction.getIncomingMessage().contentType, jsonObject, userAction.getIncomingMessage().id));
                    if (resultData.get() == null) {
                        LinkedHashMap<String, Object> map1 = new LinkedHashMap<String, Object>(2);
                        map1.put("code", ResultData.CODE_SUCCESS);
                        map1.put("forId", userAction.incomingData.id);
                        resultData.set(new ResultData(map1));
                    }

                    if (resultData.get().code == null) resultData.get().code = ResultData.CODE_SUCCESS;
                    resultData.get().forId = userAction.getIncomingData().id;
                    boolean bool = gatewayChannelModule.invokeMethod("sendResultData", new Object[]{userAction.getUserId(), resultData.get()});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send result not successfully to userId " + userAction.getUserId() + ", code " + String.class.invokeMethod("valueOf", new Object[]{resultData.get().code}) + " dataLength " + String.class.invokeMethod("valueOf", new Object[]{resultData.get().data})});
                    }

                } catch (Throwable throwable) {
//                    LoggerEx.error(TAG, "onDataReceived contentType $userAction.incomingData.contentType failed, ${throwable.getMessage()}")
                    LinkedHashMap<String, Long> map1 = new LinkedHashMap<String, Long>(3);
                    map1.put("forId", userAction.incomingMessage.id);
                    map1.put("time", System.currentTimeMillis());
                    map1.put("contentEncode", userAction.incomingMessage.contentEncode);
                    Result errorResult = new Result(map1);
                    if (throwable instanceof CoreException) {
                        errorResult.code = ((CoreException) throwable).code;
                    } else {
                        errorResult.code = Errors.ERROR_UNKNOWN;
                    }

                    boolean bool = gatewayChannelModule.invokeMethod("sendData", new Object[]{userAction.getUserId(), errorResult});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send errorResult not successfully to userId " + userAction.getUserId() + ", code " + String.class.invokeMethod("valueOf", new Object[]{errorResult.code}) + " throwable " + throwable.getMessage()});
                    }

                }

                break;
            case 120:
                try {
//                    Map jsonObject = JSON.parseObject(userAction.outgoingMessage.content)

                    userAction.getHandler().onOutgoingMessageReceived(userAction.getOutgoingMessage());
                } catch (Throwable throwable) {
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onOutgoingMessageReceived contentType " + String.class.invokeMethod("valueOf", new Object[]{userAction.getIncomingData().contentType}) + " failed, " + throwable.getMessage()});
                }

                break;
            case 110:
                userAction.getHandler().touch();
                try {
                    final Reference<ResultData> resultData = new groovy.lang.Reference<ResultData>(userAction.getHandler().onInvocation(userAction.getIncomingInvocation(), userAction.getIncomingInvocation().id));
                    if (resultData.get() == null) {
                        LinkedHashMap<String, Object> map2 = new LinkedHashMap<String, Object>(2);
                        map2.put("code", ResultData.CODE_SUCCESS);
                        map2.put("forId", userAction.incomingData.id);
                        resultData.set(new ResultData(map2));
                    }

                    boolean bool = gatewayChannelModule.invokeMethod("sendResultData", new Object[]{userAction.getUserId(), resultData.get()});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send result not successfully to userId " + userAction.getUserId() + ", code " + String.class.invokeMethod("valueOf", new Object[]{resultData.get().code}) + " dataLength " + String.class.invokeMethod("valueOf", new Object[]{resultData.get().data})});
                    }

                } catch (Throwable throwable) {
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onInvocation userAction:" + String.class.invokeMethod("valueOf", new Object[]{MessageUtils.invokeMethod("toJSONString", new Object[]{userAction.getIncomingInvocation()})}) + " " + throwable.getMessage()});
                    LinkedHashMap<String, Long> map2 = new LinkedHashMap<String, Long>(3);
                    map2.put("forId", userAction.incomingInvocation.id);
                    map2.put("time", System.currentTimeMillis());
                    map2.put("contentEncode", userAction.incomingInvocation.contentEncode);
                    Result errorResult = new Result(map2);
                    if (throwable instanceof CoreException) {
                        errorResult.code = ((CoreException) throwable).code;
                    } else {
                        errorResult.code = Errors.ERROR_UNKNOWN;
                    }

                    boolean bool = gatewayChannelModule.invokeMethod("sendData", new Object[]{userAction.getUserId(), errorResult});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send errorResult not successfully to userId " + userAction.getUserId() + ", code " + String.class.invokeMethod("valueOf", new Object[]{errorResult.code}) + " throwable " + throwable.getMessage()});
                    }

                }

                break;
            case 115:
                userAction.getHandler().touch();
                try {
                    final Reference<ResultData> resultData = new groovy.lang.Reference<ResultData>(userAction.getHandler().onRequest(userAction.getIncomingRequest(), userAction.getIncomingRequest().id));
                    if (resultData.get() == null) {
                        LinkedHashMap<String, Object> map3 = new LinkedHashMap<String, Object>(2);
                        map3.put("code", ResultData.CODE_SUCCESS);
                        map3.put("forId", userAction.incomingRequest.id);
                        resultData.set(new ResultData(map3));
                    }

                    boolean bool = gatewayChannelModule.invokeMethod("sendResultData", new Object[]{userAction.getUserId(), resultData.get()});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send result not successfully to userId " + userAction.getUserId() + ", code " + String.class.invokeMethod("valueOf", new Object[]{resultData.get().code}) + " dataLength " + String.class.invokeMethod("valueOf", new Object[]{resultData.get().data})});
                    }

                } catch (Throwable throwable) {
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onRequest " + throwable.getMessage()});
                    LinkedHashMap<String, Long> map3 = new LinkedHashMap<String, Long>(3);
                    map3.put("forId", userAction.incomingRequest.id);
                    map3.put("time", System.currentTimeMillis());
                    map3.put("contentEncode", userAction.incomingRequest.bodyEncode);
                    Result errorResult = new Result(map3);
                    if (throwable instanceof CoreException) {
                        errorResult.code = ((CoreException) throwable).code;
                    } else {
                        errorResult.code = Errors.ERROR_UNKNOWN;
                    }

                    boolean bool = gatewayChannelModule.invokeMethod("sendData", new Object[]{userAction.getUserId(), errorResult});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send errorResult not successfully to userId " + userAction.getUserId() + ", code " + String.class.invokeMethod("valueOf", new Object[]{errorResult.code}) + " throwable " + throwable.getMessage()});
                    }

                }

                break;
            default:
                break;
        }
    }

    public final String getTAG() {
        return TAG;
    }

    public GatewayChannelModule getGatewayChannelModule() {
        return gatewayChannelModule;
    }

    public void setGatewayChannelModule(GatewayChannelModule gatewayChannelModule) {
        this.gatewayChannelModule = gatewayChannelModule;
    }

    private final String TAG = UserActionHandler.class.getSimpleName();
    @Bean
    private GatewayChannelModule gatewayChannelModule;
}
