package oceanus.sdk.network.channels.gateway.modules;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

@RedeployMain
@CompileStatic
@ConditionalOnProperty(propertyName = "oceanus.gateway.enable")
public class GatewayChannelModule extends GroovyObjectSupport {
    public void main() {
        EventBusHolder.eventBus.invokeMethod("register", new Object[]{this});
    }

    public LinkedHashMap<String, Object> memory() {
        LinkedHashMap<String, Object> memory = new LinkedHashMap<String, Object>() {
        };

//        def map = [:]
//        Enumeration<String> userIds = userIdChannelMap.keys()
//        for(String userId : userIds) {
//            ChannelHandlerContext ctx = userIdChannelMap.get(userId)
//            map.put(userId, ctx.toString())
//        }
//        memory.put("userIdChannelMap", map)
        MemoryUtils.invokeMethod("fillToStringIntoMap", new Object[]{memory, getUserIdChannelMap(), "userIdChannelMap"});

//        map = [:]
//        userIds = userIdCachedUserSessionMap.keys()
//        for(String userId : userIds) {
//            UserSession userSession = userIdCachedUserSessionMap.get(userId)
//            map.put(userId, userSession.toString())
//        }
//        memory.put("userIdCachedUserSessionMap", map)
//        MemoryUtils.fillToStringIntoMap(memory, userIdCachedGatewayChannelMap, "userIdCachedUserSessionMap")
        return memory;
    }

    public boolean close(String userId, Integer code) {
        ChannelHandlerContext context = userIdChannelMap.remove(userId);
        if (context != null) {
            Channel channel = context.invokeMethod("channel", new Object[0]);
            if (channel != null && channel.invokeMethod("isActive", new Object[0])) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(1);
                map.put("code", code);
                Result result = new Result(map);
                NetUtils.invokeMethod("writeAndFlush", new Object[]{channel, result});
                channel.invokeMethod("close", new Object[0]);
                return true;
            }

        }

        return false;
    }

    public boolean sendResultData(String userId, ResultData resultData) {
        if (resultData == null || userId == null) {
            LoggerEx.invokeMethod("error", new Object[]{GatewayChannelModule.getTAG(), "sendResultData ignored, because illegal arguments, userId " + userId + ", resultData " + String.valueOf(resultData)});
            return false;
        }

        LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>(3);
        map.put("forId", resultData.forId);
        map.put("time", System.currentTimeMillis());
        map.put("contentEncode", resultData.dataEncode);
        Result result = new Result(map);

        result.code = resultData.code;
        result.content = resultData.data;

        return sendData(userId, result);
    }

    public boolean sendData(String userId, Data data) {
        ChannelHandlerContext context = userIdChannelMap.get(userId);
        if (context != null) {
            Channel channel = context.invokeMethod("channel", new Object[0]);
            if (channel != null) {
                return ((boolean) (NetUtils.invokeMethod("writeAndFlush", new Object[]{channel, data})));
            }

        }

        return false;
    }

    @Subscribe
    @AllowConcurrentEvents
    public void receivedIdentity(IdentityReceivedEvent identityReceivedEvent) {
        Identity identity = identityReceivedEvent.identity;
        if (identity == null || identity.token == null) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_ILLEGAL_TOKEN});
            return;

        }

        //TODO identity数据需要实现验证签名逻辑， 保护数据不被修改
//        if(identity.sign == null || sign(identity.sign)) {
//
//        }
        String userId = gatewaySessionManager.tokenUserIdMap.invokeMethod("get", new Object[]{identity.token});
        if (userId == null) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_GATEWAY_TOKEN_NOT_FOUND});
            return;

        }

        GatewaySessionHandler gatewaySessionHandler = gatewaySessionManager.userIdGatewaySessionHandlerMap.invokeMethod("get", new Object[]{userId});
        if (gatewaySessionHandler == null || gatewaySessionHandler.userChannel == null) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_GATEWAY_USER_NOT_EXIST});
            return;

        }

        ChannelHandlerContext old = userIdChannelMap.put(userId, identityReceivedEvent.ctx);
        if (old != null) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{old.invokeMethod("channel", new Object[0]), identity.id, Errors.ERROR_CHANNEL_KICKED});
//            return
        }


        Channel channel = identityReceivedEvent.ctx.invokeMethod("channel", new Object[0]);

        InetSocketAddress insocket = (InetSocketAddress) channel.invokeMethod("remoteAddress", new Object[0]);
        String clientIP = insocket.getAddress().getHostAddress();
        gatewaySessionHandler.userChannel.ip = clientIP;

        Attribute<UserChannel> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{GatewayChannelModule.getKEY_GATEWAY_USER()})});
        attribute.invokeMethod("set", new Object[]{gatewaySessionHandler.userChannel});

        //OnConnected//异步
        //sendCache//同步
        Object content = gatewaySessionManager.invokeMethod("channelConnected", new Object[]{gatewaySessionHandler});

        LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>(4);
        map.put("forId", identity.id);
        map.put("code", Data.CODE_SUCCESS);
        map.put("content", MessageUtils.invokeMethod("toJSONString", new Object[]{content}));
        map.put("time", System.currentTimeMillis());
        identityReceivedEvent.invokeMethod("sendResult", new Object[]{new Result(map)});
    }

    @Subscribe
    @AllowConcurrentEvents
    public void receivedChannelInactive(final ChannelInActiveEvent channelInActiveEvent) {
        Channel channel = channelInActiveEvent.ctx.invokeMethod("channel", new Object[0]);
        Attribute<UserChannel> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{GatewayChannelModule.getKEY_GATEWAY_USER()})});
        final UserChannel userSession = attribute.invokeMethod("getAndSet", new Object[]{null});
        if (userSession != null) {
            boolean bool = userIdChannelMap.remove(userSession.userId, channelInActiveEvent.ctx);
            if (!bool.asBoolean()) {
                LoggerEx.invokeMethod("warn", new Object[]{GatewayChannelModule.getTAG(), "userIdChannelMap remove userId " + String.class.invokeMethod("valueOf", new Object[]{userSession.userId}) + " failed, because channel not the same, closed channel " + String.class.invokeMethod("valueOf", new Object[]{channelInActiveEvent.ctx}) + " not removed channel " + String.valueOf(getUserIdChannelMap().get(userSession.userId))});
            }

        }

    }

    public boolean isChannelActive(String userId) {
        ChannelHandlerContext context = userIdChannelMap.get(userId);
        if (context != null) {
            Channel channel = context.invokeMethod("channel", new Object[0]);
            if (channel != null) {
                return true;
            }

        }

        return false;
    }

    public static String getKEY_GATEWAY_USER() {
        return KEY_GATEWAY_USER;
    }

    public static String getTAG() {
        return TAG;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public GatewaySessionManager getGatewaySessionManager() {
        return gatewaySessionManager;
    }

    public void setGatewaySessionManager(GatewaySessionManager gatewaySessionManager) {
        this.gatewaySessionManager = gatewaySessionManager;
    }

    public long getUserSessionCacheExpireSeconds() {
        return userSessionCacheExpireSeconds;
    }

    public void setUserSessionCacheExpireSeconds(long userSessionCacheExpireSeconds) {
        this.userSessionCacheExpireSeconds = userSessionCacheExpireSeconds;
    }

    public ConcurrentHashMap<String, ChannelHandlerContext> getUserIdChannelMap() {
        return userIdChannelMap;
    }

    public void setUserIdChannelMap(ConcurrentHashMap<String, ChannelHandlerContext> userIdChannelMap) {
        this.userIdChannelMap = userIdChannelMap;
    }

    private static final String KEY_GATEWAY_USER = "gwuser";
    private static final String TAG = GatewayChannelModule.class.getSimpleName();
    @JavaBean
    private Context context;
    @Bean
    private GatewaySessionManager gatewaySessionManager;
    @ConfigProperty(name = "user.session.cache.expire.seconds")
    private long userSessionCacheExpireSeconds = 1800;
    private ConcurrentHashMap<String, ChannelHandlerContext> userIdChannelMap = new ConcurrentHashMap<String, ChannelHandlerContext>();
}
