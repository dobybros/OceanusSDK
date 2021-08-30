package oceanus.sdk.network.channels.gateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Maps;
import oceanus.apis.CoreException;
import oceanus.sdk.core.utils.ValidateUtils;
import oceanus.sdk.logger.LoggerEx;
import oceanus.sdk.network.channels.data.*;
import oceanus.sdk.network.channels.gateway.data.UserChannel;
import oceanus.sdk.network.channels.gateway.modules.GatewayChannelModule;
import oceanus.sdk.utils.ObjectId;

import java.beans.JavaBean;
import java.lang.ref.Reference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CompileStatic
public abstract class GatewaySessionHandler extends GroovyObjectSupport {
    public GatewaySessionHandler() {
        token = ((String) (ObjectId.invokeMethod("get", new Object[0]).invokeMethod("toString", new Object[0])));
    }

    public void touch() {
        touch = System.currentTimeMillis();
    }

    public LinkedHashMap<String, Object> memory() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(5);
        map.put("id", id);
        map.put("token", token);
        map.put("userChannel", userChannel);
        map.put("cacheKeyToTimeMap", cacheKeyToTimeMap);
        map.put("touch", touch);
        return map;
    }

    public abstract void onSessionCreated();

    /**
     * 通过验证，通道正式建连;同步
     *
     * @return
     */
    public final ConcurrentMap<String, Long> channelConnected() {
        return cacheKeyToTimeMap;
    }

    /**
     * 通过验证，通道正式建连;异步单线程处理
     */
    public abstract void onChannelConnected();

    public abstract void onChannelDisconnected();

    public abstract void onSessionDestroyed();

    public abstract ResultData onDataReceived(String contentType, Map jsonObject, String id);

    public abstract ResultData onMessageReceived(String toUserId, String toGroupId, String contentType, Map jsonObject, String id);

    public ResultData onRequest(IncomingRequest incomingRequest, String id) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(2);
        map.put("code", ResultData.CODE_SUCCESS);
        map.put("forId", id);
        return new ResultData(map);
    }

    public ResultData onInvocation(IncomingInvocation incomingInvocation, String id) {
        ValidateUtils.invokeMethod("checkAllNotNull", new Object[]{incomingInvocation, id});
        Pattern r = Pattern.compile(userChannel.authorisedExpression);
        String matchStr = incomingInvocation.service + "_" + incomingInvocation.className + "_" + incomingInvocation.methodName;
        Matcher m = r.matcher(matchStr);
        if (!m.matches().asBoolean()) {
            throw new CoreException(GatewayErrors.ERROR_UNAUTHORISED_SERVICE_CALL, "Unauthorised calling to service " + String.valueOf(incomingInvocation) + " authorisedExpression " + String.class.invokeMethod("valueOf", new Object[]{getUserChannel().authorisedExpression}));
        }

        final Reference<Object[]> args = new groovy.lang.Reference<Object[]>(null);
        if (incomingInvocation.args != null) {
            try {
                JSONArray jsonArray = JSON.class.invokeMethod("parseArray", new Object[]{incomingInvocation.args});
                args.set(jsonArray.toArray());
            } catch (Throwable t) {
                t.printStackTrace();
                LoggerEx.invokeMethod("error", new Object[]{TAG, "Parse args string to json array failed, " + t.getMessage() + " incomingInvocation " + incomingInvocation + " id " + id});
                throw t;
            }

        }

        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(3);
        map.put("userId", this.id);
        map.put("ip", userChannel.ip);
        map.put("terminal", userChannel.terminal);
        GatewayUserSession gatewayUserSession = new GatewayUserSession(map);
        if (args.get() == null) {
            args.set(new Object[]);
        } else {
            Object[] theArgs = new Object[args.get().length + 1];
            theArgs[0] = gatewayUserSession;
            if (theArgs.length > 1) {
                System.arraycopy(args.get(), 0, theArgs, 1, args.get().length);
            }

            args.set(theArgs);
        }

//        LoggerEx.info(TAG, "context " + context)
        RPCCaller rpcCaller = context.invokeMethod("getRPCCaller", new Object[0]);

        final long startTime = System.currentTimeMillis();
        Object returnObj = rpcCaller.invokeMethod("call", new Object[]{incomingInvocation.service, incomingInvocation.className, incomingInvocation.methodName, Object.class, args.get()});
        final long endTime = System.currentTimeMillis();
        LoggerEx.invokeMethod("info", new Object[]{TAG, "invoke " + String.class.invokeMethod("valueOf", new Object[]{incomingInvocation.className}) + ",method:" + String.class.invokeMethod("valueOf", new Object[]{incomingInvocation.methodName}) + " args:" + String.class.invokeMethod("valueOf", new Object[]{MessageUtils.invokeMethod("toJSONString", new Object[]{args.get()})}) + " use time :" + String.valueOf(endTime - startTime)});


        LinkedHashMap<String, Object> map1 = new LinkedHashMap<String, Object>(2);
        map1.put("code", ResultData.CODE_SUCCESS);
        map1.put("forId", id);
        ResultData resultData = new ResultData(map1);
        resultData.data = JSON.toJSONString(returnObj);
        return resultData;
    }

    /**
     * Verify successfully by default
     * Override this method for business logic and throw CoreException when token is illegal
     *
     * @param authorisedToken
     */
    public void verifyAuthorisedToken(String authorisedToken) {

    }

    public void onOutgoingMessageReceived(OutgoingMessage outgoingMessage) {
        gatewayChannelModule.invokeMethod("sendData", new Object[]{getUserChannel().userId, outgoingMessage});
    }

    public boolean sendData(String userId, String contentType, String data) {
        OutgoingData outgoingData = new OutgoingData();
        outgoingData.invokeMethod("setContentType", new Object[]{contentType});
        outgoingData.invokeMethod("setContent", new Object[]{data});
        outgoingData.invokeMethod("setContentEncode", new Object[]{ResultData.CONTENT_ENCODE_JSON});
        outgoingData.invokeMethod("setTime", new Object[]{System.currentTimeMillis()});
        return ((boolean) (gatewayChannelModule.invokeMethod("sendData", new Object[]{userId, outgoingData})));
    }

    public Map<String, Long> getCacheKeyToTime() {
        return cacheKeyToTimeMap;
    }

    public long getTouch() {
        return touch;
    }

    public void setTouch(long touch) {
        this.touch = touch;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserChannel getUserChannel() {
        return userChannel;
    }

    public void setUserChannel(UserChannel userChannel) {
        this.userChannel = userChannel;
    }

    public GatewayChannelModule getGatewayChannelModule() {
        return gatewayChannelModule;
    }

    public void setGatewayChannelModule(GatewayChannelModule gatewayChannelModule) {
        this.gatewayChannelModule = gatewayChannelModule;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private final String TAG = GatewaySessionHandler.class.getSimpleName();
    private long touch;
    private String token;
    /**
     * userId
     */
    private String id;
    private UserChannel userChannel;
    /**
     * 缓存该玩家事件更新的时间
     * 玩家登录后通知该缓存
     */
    private final Map<String, Long> cacheKeyToTimeMap = Maps.newConcurrentMap();
    @Bean
    private GatewayChannelModule gatewayChannelModule;
    @JavaBean
    private Context context;
}
