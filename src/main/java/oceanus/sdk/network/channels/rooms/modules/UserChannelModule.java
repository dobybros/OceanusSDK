package oceanus.sdk.network.channels.rooms.modules;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RedeployMain
@CompileStatic
@ConditionalOnProperty(propertyName = "oceanus.room.enable")
public class UserChannelModule extends GroovyObjectSupport {
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
        MemoryUtils.invokeMethod("fillToStringIntoMap", new Object[]{memory, getUserIdCachedUserSessionMap(), "userIdCachedUserSessionMap"});
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
            LoggerEx.invokeMethod("error", new Object[]{UserChannelModule.getTAG(), "sendResultData ignored, because illegal arguments, userId " + userId + ", resultData " + String.valueOf(resultData)});
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
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_ILLEGAL_JWT_TOKEN});
            return;

        }

//        if(identity.balance == null) {
//            identityReceivedEvent.closeChannel(identity.id, Errors.ERROR_IDENTITY_BALANCE_IS_NULL)
//            return
//        }
        //TODO identity数据需要实现验证签名逻辑， 保护数据不被修改
//        if(identity.sign == null || sign(identity.sign)) {
//
//        }
        Claims claims;
        try {
            claims = ((Claims) (JWTUtils.invokeMethod("getClaims", new Object[]{getRoomManager().jwtKey, identity.token})));
        } catch (Throwable ignored) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_ILLEGAL_JWT_TOKEN_EXPIRED});
            return;

        }

        String userStr = claims.invokeMethod("get", new Object[]{"user"});
        if (userStr == null) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_ILLEGAL_JWT_TOKEN_PARAMETER});
            return;

        }

        UserSession userSession = null;
        try {
            userSession = ((UserSession) (JSON.class.invokeMethod("parseObject", new Object[]{userStr, UserSession.class})));
        } catch (Throwable throwable) {
            LoggerEx.invokeMethod("error", new Object[]{UserChannelModule.getTAG(), "parseJSON for UserSession failed, " + throwable.getMessage() + ", jsonStr is " + userStr});
        }

        if (userSession == null) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_ILLEGAL_JWT_TOKEN_USER_SESSION});
            return;

        }

        if (!userSession.server.equals(context.serverConfig.server)) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_ILLEGAL_SERVER});
            return;

        }

        if (userSession.roomId == null) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_ILLEGAL_JWT_TOKEN_USER_SESSION});
            return;

        }


        RoomSessionHandler roomSessionHandler = roomManager.roomIdSessionHandlerMap.invokeMethod("get", new Object[]{userSession.roomId});
        if (roomSessionHandler == null) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_ROOM_NOT_EXIST});
            return;

        }

        UserSession existingUserSession = roomSessionHandler.userIdUserSessionMap.invokeMethod("get", new Object[]{userSession.userId});
        if (existingUserSession == null) {
            existingUserSession = userIdCachedUserSessionMap.get(userSession.userId);
            if (existingUserSession != null) {
                try {
                    roomManager.invokeMethod("addUserToRoom", new Object[]{existingUserSession, false});
                } catch (Throwable t) {
                    userIdCachedUserSessionMap.remove(userSession.userId);
                    throw t;
                }

            }

        }

        if (existingUserSession == null) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_ROOM_USER_NOT_EXIST});
            return;

        }

        if (!existingUserSession.service.equals(userSession.service) || !existingUserSession.group.equals(userSession.group)) {
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{identity.id, Errors.ERROR_SERVICE_GROUP_NOT_MATCH});
            return;

        }

        Channel channel = identityReceivedEvent.ctx.invokeMethod("channel", new Object[0]);
        Attribute<UserSession> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{UserChannelModule.getKEY_USER()})});
        attribute.invokeMethod("set", new Object[]{existingUserSession});

        userIdCachedUserSessionMap.remove(existingUserSession.userId);

        boolean channelReplaced = false;
        ChannelHandlerContext old = userIdChannelMap.put(userSession.userId, identityReceivedEvent.ctx);
        if (old != null) {
            Channel oldChannel = old.invokeMethod("channel", new Object[0]);
            identityReceivedEvent.invokeMethod("closeChannel", new Object[]{oldChannel, identity.id, Errors.ERROR_CHANNEL_KICKED});
            channelReplaced = true;
        }


        if (channelReplaced || existingUserSession.joinFuture != null) {
//            existingUserSession.name = identity.name
//            existingUserSession.icon = identity.icon
            existingUserSession.reserved = identity.reserved;
//            existingUserSession.rank = identity.rank
//            if(existingUserSession.balance == null) {
//                existingUserSession.balance = 12222;//identity.balance
//            }
            roomManager.invokeMethod("connectUserOrViewerToRoom", new Object[]{existingUserSession});
        }


        LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>(3);
        map.put("forId", identity.id);
        map.put("code", Data.CODE_SUCCESS);
        map.put("time", System.currentTimeMillis());
        identityReceivedEvent.invokeMethod("sendResult", new Object[]{new Result(map)});
    }

    @Subscribe
    @AllowConcurrentEvents
    public void receivedChannelInactive(ChannelInActiveEvent channelInActiveEvent) {
        Channel channel = channelInActiveEvent.ctx.invokeMethod("channel", new Object[0]);
        Attribute<UserSession> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{UserChannelModule.getKEY_USER()})});
        final UserSession userSession = attribute.invokeMethod("getAndSet", new Object[]{null});
        if (userSession != null) {
            final ChannelHandlerContext currentChannelContext = userIdChannelMap.get(userSession.userId);
            if (currentChannelContext != null && !currentChannelContext.invokeMethod("channel", new Object[0]).equals(channel)) {
                LoggerEx.invokeMethod("info", new Object[]{UserChannelModule.getTAG(), "receivedChannelInactive: Channel is changed, current is " + String.class.invokeMethod("valueOf", new Object[]{currentChannelContext.invokeMethod("channel", new Object[0])}) + " the inactive channel is " + String.valueOf(channel) + " for userId " + String.class.invokeMethod("valueOf", new Object[]{userSession.userId}) + ". ignore..."});
                return;

            }

            removeUserFromRoom(userSession, channelInActiveEvent.ctx);
        }

    }

    private void clearCacheExpireTimer(UserSession userSession) {
        if (userSession != null && userSession.cacheExpiredFuture != null) {
            userSession.cacheExpiredFuture.invokeMethod("cancel", new Object[]{true});
            userSession.cacheExpiredFuture = null;
        }

    }

    public void removeUserFromRoom(final UserSession userSession, final ChannelHandlerContext context) {
        UserSession oldSession = userIdCachedUserSessionMap.put(userSession.userId, userSession);
        clearCacheExpireTimer(oldSession);

        clearCacheExpireTimer(userSession);
        userSession.cacheExpiredFuture = ScheduledExecutorHolder.scheduledExecutorService.invokeMethod("schedule", new Object[]{new Closure(this, this) {
            public Object doCall(Object it) {
                boolean bool = getUserIdCachedUserSessionMap().remove(userSession.userId, userSession);
                if (!bool.asBoolean())
                    return LoggerEx.invokeMethod("warn", new Object[]{UserChannelModule.getTAG(), "userIdCachedUserSessionMap remove userId " + String.class.invokeMethod("valueOf", new Object[]{userSession.userId}) + " failed because value was changed. old is " + String.valueOf(userSession)});
            }

            public Object doCall() {
                return doCall(null);
            }

        }, getUserSessionCacheExpireSeconds(), TimeUnit.SECONDS});

        roomManager.invokeMethod("removeUserOrViewerFromRoom", new Object[]{userSession.userId, userSession.roomId, false});
        boolean bool = userIdChannelMap.remove(userSession.userId, context);
        if (!bool.asBoolean()) {
            LoggerEx.invokeMethod("warn", new Object[]{UserChannelModule.getTAG(), "userIdChannelMap remove userId " + String.class.invokeMethod("valueOf", new Object[]{userSession.userId}) + " failed, because channel not the same, closed channel " + String.valueOf(context) + " not removed channel " + String.valueOf(getUserIdChannelMap().get(userSession.userId))});
        }

    }

    public static String getKEY_USER() {
        return KEY_USER;
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

    public RoomSessionManager getRoomManager() {
        return roomManager;
    }

    public void setRoomManager(RoomSessionManager roomManager) {
        this.roomManager = roomManager;
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

    public ConcurrentHashMap<String, UserSession> getUserIdCachedUserSessionMap() {
        return userIdCachedUserSessionMap;
    }

    public void setUserIdCachedUserSessionMap(ConcurrentHashMap<String, UserSession> userIdCachedUserSessionMap) {
        this.userIdCachedUserSessionMap = userIdCachedUserSessionMap;
    }

    private static final String KEY_USER = "user";
    private static final String TAG = UserChannelModule.class.getSimpleName();
    @JavaBean
    private Context context;
    @Bean
    private RoomSessionManager roomManager;
    @ConfigProperty(name = "user.session.cache.expire.seconds")
    private long userSessionCacheExpireSeconds = 1800;
    private ConcurrentHashMap<String, ChannelHandlerContext> userIdChannelMap = new ConcurrentHashMap<String, ChannelHandlerContext>();
    private ConcurrentHashMap<String, UserSession> userIdCachedUserSessionMap = new ConcurrentHashMap<String, UserSession>();
}
