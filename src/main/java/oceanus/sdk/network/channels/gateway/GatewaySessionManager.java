package oceanus.sdk.network.channels.gateway;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.unix.Errors;
import oceanus.apis.CoreException;
import oceanus.sdk.core.utils.ValidateUtils;
import oceanus.sdk.logger.LoggerEx;
import oceanus.sdk.network.channels.gateway.data.UserChannel;
import oceanus.sdk.network.channels.gateway.modules.GatewayChannelModule;
import oceanus.sdk.network.channels.websocket.WebSocketManager;
import oceanus.sdk.network.channels.websocket.scheduled.ScheduledExecutorHolder;
import oceanus.sdk.rpc.remote.annotations.ServiceBean;
import oceanus.sdk.utils.ObjectId;
import oceanus.sdk.utils.ReflectionUtil;
import oceanus.sdk.utils.SingleThreadQueueEx;
import oceanus.sdk.utils.state.StateExecutor;
import oceanus.sdk.utils.state.StateMachine;
import oceanus.services.gatewaymanager.service.GatewayManagerService;

import java.beans.JavaBean;
import java.util.*;
import java.util.concurrent.*;

/**
 * 房间会话管理器， 房间服务器需要启动该类做WebSocket服务器以及房间相关逻辑
 */

@Import(classes = {GatewayStatusService.class})
public class GatewaySessionManager {
    public Map<String, Object> memory() {
        Map<String, Object> map = new HashMap<>();
//        Set<String> handlerKeys = roomIdSessionHandlerMap.keySet()
//        def handlerMap = [:]
//        for(String key : handlerKeys) {
//            RoomSessionHandler roomSessionHandler = roomIdSessionHandlerMap.get(key)
//            handlerMap.put(key, roomSessionHandler.memory())
//        }
//        map.put("roomIdSessionHandlerMap", handlerMap)
        MemoryUtils.invokeMethod("fillMemoryIntoMap", new Object[]{map, getUserIdGatewaySessionHandlerMap(), "roomIdSessionHandlerMap"});

//        Set<String> threadKeys = roomIdSingleThreadMap.keySet()
//        def threadMap = [:]
//        for(String key : threadKeys) {
//            SingleThreadQueue queue = roomIdSingleThreadMap.get(key)
//            threadMap.put(key, queue.toString())
//        }
//        map.put("singleThreadMap", threadMap)
        MemoryUtils.invokeMethod("fillToStringIntoMap", new Object[]{map, userIdSingleThreadMap, "userIdSingleThreadMap"});

        MemoryUtils.invokeMethod("fillToStringIntoMap", new Object[]{map, getUserIdGatewayChannelMap(), "userIdGatewayChannelMap"});
        return map;
    }

    public void start() {
        threadPoolExecutor = new ThreadPoolExecutor(roomSessionManagerCoreSize, roomSessionManagerMaximumPoolSize, roomSessionManagerKeepAliveSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue(roomSessionManagerQueueCapacity), new ThreadFactoryBuilder().setNameFormat("RoomSessionManager-%d").build());

        stateMachine = ((StateMachine<Integer, GatewaySessionManager>) (new StateMachine<>(GatewaySessionManager.class.getSimpleName() + "#", STATE_NONE, this)));
        stateMachine.invokeMethod("configState", new Object[]{GatewaySessionManager.getSTATE_NONE(), getStateMachine().invokeMethod("execute", new Object[0]).invokeMethod("nextStates", new Object[]{GatewaySessionManager.getSTATE_SCAN_GATEWAY_SESSION()})}).invokeMethod("configState", new Object[]{GatewaySessionManager.getSTATE_SCAN_GATEWAY_SESSION(), getStateMachine().invokeMethod("execute", new Object[]{(StateExecutor<Integer, GatewaySessionManager>) getHandleScanRoomSessionsState()}).invokeMethod("nextStates", new Object[]{GatewaySessionManager.getSTATE_STARTED(), GatewaySessionManager.getSTATE_TERMINATED()})}).invokeMethod("configState", new Object[]{GatewaySessionManager.getSTATE_STARTED(), getStateMachine().invokeMethod("execute", new Object[]{(StateExecutor<Integer, GatewaySessionManager>) getHandlerStartedState()}).invokeMethod("nextStates", new Object[]{GatewaySessionManager.getSTATE_PAUSED(), GatewaySessionManager.getSTATE_TERMINATED()})}).invokeMethod("configState", new Object[]{GatewaySessionManager.getSTATE_PAUSED(), getStateMachine().invokeMethod("execute", new Object[0]).invokeMethod("nextStates", new Object[]{GatewaySessionManager.getSTATE_STARTED(), GatewaySessionManager.getSTATE_TERMINATED()})}).invokeMethod("configState", new Object[]{GatewaySessionManager.getSTATE_TERMINATED(), getStateMachine().invokeMethod("execute", new Object[0])});

        stateMachine.invokeMethod("gotoState", new Object[]{GatewaySessionManager.getSTATE_SCAN_GATEWAY_SESSION(), "Start scanning GatewaySessionHandler class"});
    }

    public void stop() {
        LoggerEx.invokeMethod("info", new Object[]{getTAG(), "GatewaySessionManager is stopping..."});
        final long time = System.currentTimeMillis();
        Collection<String> userIds = new ArrayList<String>(userIdGatewaySessionHandlerMap.keySet());
        for (String userId : userIds) {
            closeSession(userId);
        }

        webSocketManager.invokeMethod("stop", new Object[0]);
        LoggerEx.invokeMethod("info", new Object[]{getTAG(), "RoomSessionManager stopped, takes " + String.valueOf(System.currentTimeMillis() - time)});
    }

    public SingleThreadQueueEx<UserAction> removeUserActionQueue(String userId) {
        SingleThreadQueueEx<UserAction> queue = (SingleThreadQueueEx<UserAction>) userIdSingleThreadMap.remove(userId);
//        queue.queue.clear()
        return queue;
    }

    private SingleThreadQueueEx<UserAction> getUserActionQueue(String userId) {
        SingleThreadQueueEx<UserAction> queue = (SingleThreadQueueEx<UserAction>) userIdSingleThreadMap.get(userId);
        if (queue == null) {
            UserActionHandler userActionHandler = new UserActionHandler();
            context.invokeMethod("injectBean", new Object[]{userActionHandler});
            queue = new SingleThreadQueueEx<UserAction>(threadPoolExecutor, userActionHandler);
            SingleThreadQueueEx old = userIdSingleThreadMap.putIfAbsent(userId, (SingleThreadQueueEx) queue);
            if (old != null) queue = ((SingleThreadQueueEx<UserAction>) (old));
        }

        return queue;
    }

    public void replaceChannel(GatewaySessionHandler gatewaySessionHandler, String authorisedExpression, String deviceToken, Integer terminal, int code) {
        closeChannel(gatewaySessionHandler.getId(), code);
        gatewaySessionHandler.getUserChannel().terminal = terminal;
        gatewaySessionHandler.getUserChannel().deviceToken = deviceToken;
        gatewaySessionHandler.getUserChannel().updateTime = System.currentTimeMillis();
        gatewaySessionHandler.getUserChannel().authorisedExpression = authorisedExpression;
    }

    private static void verifyAuthorisedToken(GatewaySessionHandler gatewaySessionHandler, String authorisedToken) {
        try {
            gatewaySessionHandler.verifyAuthorisedToken(authorisedToken);
        } catch (Throwable throwable) {
            if (throwable instanceof CoreException) {
                throw throwable;
            }

            throw new CoreException(Errors.ERROR_VERIFY_AUTHORISED_TOKEN_FAILED, "Verify authorised token " + authorisedToken + " failed, " + throwable.getMessage());
        }

    }

    public void reLogin(GatewaySessionHandler gatewaySessionHandler, String authorisedExpression, String deviceToken, Integer terminal, Boolean activeLogin, String authorisedToken) {
        if (gatewaySessionHandler.getUserChannel() == null)
            throw new CoreException(GatewayErrors.ERROR_USER_CHANNEL_NULL, "User channel is null while preCreateGatewaySessionHandler userId " + gatewaySessionHandler.getId() + " authorisedExpression " + authorisedExpression + " deviceToken " + deviceToken + " terminal " + String.valueOf(terminal) + " activeLogin " + String.valueOf(activeLogin));

        verifyAuthorisedToken(gatewaySessionHandler, authorisedToken);

        if (authorisedExpression == null) authorisedExpression = this.authorisedExpression;

        if (activeLogin) {//用户主动登录时， 说明用户是点击登录后进入的， 所以无论如何要替换通道
            replaceChannel(gatewaySessionHandler, authorisedExpression, deviceToken, terminal, GatewayErrors.ERROR_CHANNEL_KICKED_BY_DEVICE);
        } else if (gatewaySessionHandler.getUserChannel().deviceToken.equals(deviceToken)) {
            //当用户是被动登录时， 因为断网重新登录的时候为被动登录， 如果deviceToken是一样的， 也替换通道
            replaceChannel(gatewaySessionHandler, authorisedExpression, deviceToken, terminal, GatewayErrors.ERROR_CHANNEL_KICKED_BY_CONCURRENT);
        } else {//当用户是被动登录时， 因为断网重新登录的时候为被动登录， 如果deviceToken是不一样的， 说明这是旧设备登录， 不应该允许
            throw new CoreException(GatewayErrors.ERROR_LOGIN_FAILED_DEVICE_TOKEN_CHANGED, "Rejected because this is old device login, old deviceToken " + deviceToken + " current " + String.class.invokeMethod("valueOf", new Object[]{gatewaySessionHandler.getUserChannel().deviceToken}) + " userId " + gatewaySessionHandler.getId() + " authorisedExpression " + authorisedExpression + " terminal " + String.valueOf(terminal) + " activeLogin " + String.valueOf(activeLogin));
        }

    }

    public GatewaySessionHandler preCreateGatewaySessionHandler(String userId, String authorisedExpression, String deviceToken, Integer terminal, Boolean activeLogin, String authorisedToken) {
        checkState();
        ValidateUtils.invokeMethod("checkAllNotNull", new Object[]{userId, deviceToken, terminal});
        if (authorisedExpression == null) authorisedExpression = this.authorisedExpression;
        if (activeLogin == null) activeLogin = false;


        GatewaySessionHandler gatewaySessionHandler = userIdGatewaySessionHandlerMap.get(userId);
        if (gatewaySessionHandler != null) {
            reLogin(gatewaySessionHandler, authorisedExpression, deviceToken, terminal, activeLogin, authorisedToken);
        } else {
            gatewaySessionHandler = gatewaySessionHandlerClass.getConstructor().newInstance();
//        handler.userChannel = userChannel
            LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>(5);
            map.put("userId", userId);
            map.put("authorisedExpression", authorisedExpression);
            map.put("deviceToken", deviceToken);
            map.put("terminal", terminal);
            map.put("createTime", System.currentTimeMillis());
            gatewaySessionHandler.setUserChannel(new UserChannel(map));
            gatewaySessionHandler.setId(gatewaySessionHandler.getUserChannel().userId);

            context.invokeMethod("injectBean", new Object[]{gatewaySessionHandler});
            verifyAuthorisedToken(gatewaySessionHandler, authorisedToken);

            boolean exceedMaxChannels = true;
            if (roomCounter <= this.maxChannels) {
                synchronized (userLock) {
                    if (roomCounter <= this.maxChannels) {
                        GatewaySessionHandler existing = userIdGatewaySessionHandlerMap.putIfAbsent(gatewaySessionHandler.getId(), gatewaySessionHandler);
                        if (existing == null) {
                            tokenUserIdMap.putIfAbsent(gatewaySessionHandler.getToken(), gatewaySessionHandler.getId());
                            roomCounter++;

                            SingleThreadQueueEx<UserAction> queue = getUserActionQueue(gatewaySessionHandler.getId());
                            UserAction action = new UserAction();


                            queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(gatewaySessionHandler)action.setUserId(gatewaySessionHandler.getId())action.setAction(UserAction.ACTION_SESSION_CREATED)});
                        } else {
                            gatewaySessionHandler = existing;
                        }

                        exceedMaxChannels = false;
                    }

                }

            }

            if (exceedMaxChannels)
                throw new CoreException(Errors.ERROR_EXCEED_USER_CHANNEL_CAPACITY, "Exceed user channel capacity " + this + ".maxChannels, current " + String.valueOf(roomCounter));
        }

        gatewaySessionHandler.touch();
        return gatewaySessionHandler;
    }

    public void invokeOnUserThread(String userId, Closure<Void> closure, Object... args) {
        if (userId == null || closure == null) {
            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "invokeOnRoomThread ignored, because of illegal arguments, roomId " + userId + " closure " + String.valueOf(closure) + ", args " + String.valueOf(args)});
            return;

        }


        GatewaySessionHandler gatewaySessionHandler = userIdGatewaySessionHandlerMap.get(userId);
        if (gatewaySessionHandler == null) {
            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "invokeOnRoomThread ignored, because room doesn\'t exist for roomId " + userId + " closure " + String.valueOf(closure) + ", args " + String.valueOf(args)});
            return;

        }

        SingleThreadQueueEx<UserAction> queue = getUserActionQueue(gatewaySessionHandler.getId());
        UserAction action = new UserAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(gatewaySessionHandler)action.setUserId(gatewaySessionHandler.getId())action.setAction(UserAction.ACTION_USER_CLOSURE)action.setClosure(closure)action.setClosureArgs(args)});

    }

    public void closeSession(String userId) {
        closeSession(userId, true);
    }

    public void closeSession(String userId, boolean closeQuietly) {
        checkState();

        GatewaySessionHandler gatewaySessionHandler;
        gatewayManagerService.invokeMethod("logout", new Object[]{userId, getContext().serverConfig.service});
        SingleThreadQueueEx<UserAction> queue;
        synchronized (userLock) {
            gatewaySessionHandler = userIdGatewaySessionHandlerMap.remove(userId);
            if (gatewaySessionHandler == null) {
                if (closeQuietly) {
                    return;

                }

                throw new CoreException(Errors.ERROR_GATEWAY_CHANNEL_NOT_EXIST, "Gateway channel " + userId + " not exist while closing gateway channel");
            }

            queue = removeUserActionQueue(userId);
            tokenUserIdMap.remove(gatewaySessionHandler.getToken());
            roomCounter--;
        }

        gatewayChannelModule.invokeMethod("close", new Object[]{gatewaySessionHandler.getId(), GatewayErrors.ERROR_CHANNEL_USER_CLOSED});

//        SingleThreadQueueEx<UserAction> queue = getUserActionQueue(gatewaySessionHandler.id)
        if (queue != null) {
            UserAction action = new UserAction();


            queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(gatewaySessionHandler)action.setUserId(gatewaySessionHandler.getId())action.setAction(UserAction.ACTION_SESSION_DESTROYED)});
        }

    }

    public void closeChannel(String userId, int code) {
        closeChannel(userId, code, true);
    }

    public void closeChannel(String userId, int code, boolean closeQuietly) {
        GatewaySessionHandler gatewaySessionHandler = userIdGatewaySessionHandlerMap.get(userId);
        if (gatewaySessionHandler == null) {
            if (closeQuietly) {
                return;

            }

            throw new CoreException(GatewayErrors.ERROR_USER_NOT_EXIST, "Gateway user " + userId + " not exist while closing channel");
        }


        if (gatewayChannelModule.invokeMethod("close", new Object[]{userId, code}).asBoolean()) {
            SingleThreadQueueEx<UserAction> queue = getUserActionQueue(userId);
            UserAction action = new UserAction();


            queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(gatewaySessionHandler)action.setUserId(userId)action.setAction(UserAction.ACTION_USER_DISCONNECTED)});
        }

    }

    public void refreshHandlerToken(String userId) {
        GatewaySessionHandler gatewaySessionHandler = userIdGatewaySessionHandlerMap.get(userId);
        if (gatewaySessionHandler == null)
            throw new CoreException(GatewayErrors.ERROR_USER_NOT_EXIST, "User " + userId + " not exist while refreshHandlerToken");

        synchronized (userLock) {
            String old = gatewaySessionHandler.getToken();
            gatewaySessionHandler.setToken(ObjectId.invokeMethod("get", new Object[0]).invokeMethod("toString", new Object[0]));
            tokenUserIdMap.remove(old);
            tokenUserIdMap.put(gatewaySessionHandler.getToken(), gatewaySessionHandler.getId());
        }


    }

    public GatewaySessionHandler getGatewaySessionHandler(String userId) {
        return userIdGatewaySessionHandlerMap.get(userId);
    }

    public void receiveIncomingData(String userId, IncomingData incomingData) {
        GatewaySessionHandler gatewaySessionHandler = userIdGatewaySessionHandlerMap.get(userId);
        if (gatewaySessionHandler == null)
            throw new CoreException(GatewayErrors.ERROR_USER_NOT_EXIST, "User " + userId + " not exist while receiving incomingData");

        //接收消息采用房间的消息单线程来处理， 和房间状态单线程分开
        SingleThreadQueueEx<UserAction> queue = getUserActionQueue(userId);
        UserAction action = new UserAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(gatewaySessionHandler)action.setIncomingData(incomingData)action.setUserId(userId)action.setAction(UserAction.ACTION_USER_DATA)});
    }

    public void receiveIncomingMessage(String userId, IncomingMessage incomingMessage) {
        GatewaySessionHandler gatewaySessionHandler = userIdGatewaySessionHandlerMap.get(userId);
        if (gatewaySessionHandler == null)
            throw new CoreException(GatewayErrors.ERROR_USER_NOT_EXIST, "User " + userId + " not exist while receiving incomingMessage");

        //接收消息采用房间的消息单线程来处理， 和房间状态单线程分开
        SingleThreadQueueEx<UserAction> queue = getUserActionQueue(userId);
        UserAction action = new UserAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(gatewaySessionHandler)action.setIncomingMessage(incomingMessage)action.setUserId(userId)action.setAction(UserAction.ACTION_USER_MESSAGE)});
    }

    public void receiveIncomingInvocation(String userId, IncomingInvocation incomingInvocation) {
        GatewaySessionHandler gatewaySessionHandler = userIdGatewaySessionHandlerMap.get(userId);
        if (gatewaySessionHandler == null)
            throw new CoreException(GatewayErrors.ERROR_USER_NOT_EXIST, "User " + userId + " not exist while receiving incomingInvocation");

        //接收消息采用房间的消息单线程来处理， 和房间状态单线程分开
        SingleThreadQueueEx<UserAction> queue = getUserActionQueue(userId);
        UserAction action = new UserAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(gatewaySessionHandler)action.setIncomingInvocation(incomingInvocation)action.setUserId(userId)action.setAction(UserAction.ACTION_USER_INVOCATION)});
    }

    public void receiveIncomingRequest(String userId, IncomingRequest incomingRequest) {
        GatewaySessionHandler gatewaySessionHandler = userIdGatewaySessionHandlerMap.get(userId);
        if (gatewaySessionHandler == null)
            throw new CoreException(GatewayErrors.ERROR_USER_NOT_EXIST, "User " + userId + " not exist while receiving incomingInvocation");

        //接收消息采用房间的消息单线程来处理， 和房间状态单线程分开
        SingleThreadQueueEx<UserAction> queue = getUserActionQueue(userId);
        UserAction action = new UserAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(gatewaySessionHandler)action.setIncomingRequest(incomingRequest)action.setUserId(userId)action.setAction(UserAction.ACTION_USER_REQUEST)});
    }

    public void checkState() {
        if (!stateMachine.currentState.equals(STATE_STARTED))
            throw new CoreException(Errors.ERROR_GATEWAY_SESSION_MANAGER_NOT_STARTED, "GatewaySessionManager not started");
        if (gatewaySessionHandlerClass == null)
            throw new CoreException(Errors.ERROR_GATEWAY_SESSION_HANDLER_CLASS_IS_NULL, "gatewaySessionHandlerClass is null");
    }

    public int roomCounter() {
        return roomCounter;
    }

    /**
     * 找到玩家，单线程发消息
     *
     * @param userId
     * @param outgoingMessage
     */
    public void receiveOutgoingMessage(String userId, OutgoingMessage outgoingMessage) {
        GatewaySessionHandler gatewaySessionHandler = userIdGatewaySessionHandlerMap.get(userId);
        if (gatewaySessionHandler == null)
            throw new CoreException(GatewayErrors.ERROR_USER_NOT_EXIST, "User " + userId + " not exist while receiving outgoingMessage");

        //接收消息采用房间的消息单线程来处理， 和房间状态单线程分开
        SingleThreadQueueEx<UserAction> queue = getUserActionQueue(userId);
        UserAction action = new UserAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(gatewaySessionHandler)action.setOutgoingMessage(outgoingMessage)action.setUserId(userId)action.setAction(UserAction.ACTION_USER_OUTGOING_MESSAGE)});
    }

    /**
     * 缓存当{@link gamesharedcore.servicestubs.gatewaymanager.entity.InstantMessage#cacheTimeKey}不为空，
     * 更新时间{@link gamesharedcore.network.channels.gateway.GatewaySessionHandler#cacheKeyToTimeMap}
     *
     * @param cacheTimeKey
     * @param time
     * @param userId
     */
    public void putCacheKeyToTime(String cacheTimeKey, Long time, String userId) {
        if (StringUtils.invokeMethod("isNotEmpty", new Object[]{cacheTimeKey}) && time != null) {
            GatewaySessionHandler gatewaySessionHandler = userIdGatewaySessionHandlerMap.get(userId);
            if (gatewaySessionHandler != null) {
                gatewaySessionHandler.getCacheKeyToTime().put(cacheTimeKey, time);
            }

        }

    }

    /**
     * 通过验证，正式连接
     *
     * @param gatewaySessionHandler
     * @return
     */
    public LinkedHashMap<String, ConcurrentMap<String, Long>> channelConnected(final GatewaySessionHandler gatewaySessionHandler) {
        try {
            //异步消息
            SingleThreadQueueEx<UserAction> queue = getUserActionQueue(gatewaySessionHandler.getId());
            UserAction action = new UserAction();


            queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(gatewaySessionHandler)action.setUserId(gatewaySessionHandler.getId())action.setAction(UserAction.ACTION_USER_CONNECTED)});
            //同步消息
            LinkedHashMap<String, ConcurrentMap<String, Long>> map = new LinkedHashMap<String, ConcurrentMap<String, Long>>(1);
            map.put("cacheTimeMap", gatewaySessionHandler.channelConnected());
            return map;
        } catch (Throwable t) {
            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "channelConnected userId:" + gatewaySessionHandler.getId() + " error:" + String.valueOf(t)});
        }

        return null;
    }

    public final String getTAG() {
        return TAG;
    }

    public String getJwtKey() {
        return jwtKey;
    }

    public void setJwtKey(String jwtKey) {
        this.jwtKey = jwtKey;
    }

    public Integer getJwtExpireSeconds() {
        return jwtExpireSeconds;
    }

    public void setJwtExpireSeconds(Integer jwtExpireSeconds) {
        this.jwtExpireSeconds = jwtExpireSeconds;
    }

    public Integer getSessionExpireCheckPeriodSeconds() {
        return sessionExpireCheckPeriodSeconds;
    }

    public void setSessionExpireCheckPeriodSeconds(Long sessionExpireCheckPeriodSeconds) {
        this.sessionExpireCheckPeriodSeconds = sessionExpireCheckPeriodSeconds;
    }

    public Long getSessionInactiveExpireTime() {
        return sessionInactiveExpireTime;
    }

    public void setSessionInactiveExpireTime(Long sessionInactiveExpireTime) {
        this.sessionInactiveExpireTime = sessionInactiveExpireTime;
    }

    public String getAuthorisedExpression() {
        return authorisedExpression;
    }

    public void setAuthorisedExpression(String authorisedExpression) {
        this.authorisedExpression = authorisedExpression;
    }

    public GatewayManagerService getGatewayManagerService() {
        return gatewayManagerService;
    }

    public void setGatewayManagerService(GatewayManagerService gatewayManagerService) {
        this.gatewayManagerService = gatewayManagerService;
    }

    public final String getJWT_FIELD_USER_ID() {
        return JWT_FIELD_USER_ID;
    }

    public static String getJWT_FIELD_AUTHORISED_SERVICES() {
        return JWT_FIELD_AUTHORISED_SERVICES;
    }

    public static String getJWT_FIELD_DEVICE_TOKEN_CRC() {
        return JWT_FIELD_DEVICE_TOKEN_CRC;
    }

    public static String getJWT_FIELD_TERMINAL() {
        return JWT_FIELD_TERMINAL;
    }

    public static String getJWT_FIELD_ACTIVE_LOGIN() {
        return JWT_FIELD_ACTIVE_LOGIN;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }

    public void setWebSocketManager(WebSocketManager webSocketManager) {
        this.webSocketManager = webSocketManager;
    }

    public final Object getUserLock() {
        return userLock;
    }

    public int getRoomSessionManagerQueueCapacity() {
        return roomSessionManagerQueueCapacity;
    }

    public void setRoomSessionManagerQueueCapacity(int roomSessionManagerQueueCapacity) {
        this.roomSessionManagerQueueCapacity = roomSessionManagerQueueCapacity;
    }

    public int getRoomSessionManagerCoreSize() {
        return roomSessionManagerCoreSize;
    }

    public void setRoomSessionManagerCoreSize(int roomSessionManagerCoreSize) {
        this.roomSessionManagerCoreSize = roomSessionManagerCoreSize;
    }

    public int getRoomSessionManagerMaximumPoolSize() {
        return roomSessionManagerMaximumPoolSize;
    }

    public void setRoomSessionManagerMaximumPoolSize(int roomSessionManagerMaximumPoolSize) {
        this.roomSessionManagerMaximumPoolSize = roomSessionManagerMaximumPoolSize;
    }

    public int getRoomSessionManagerKeepAliveSeconds() {
        return roomSessionManagerKeepAliveSeconds;
    }

    public void setRoomSessionManagerKeepAliveSeconds(int roomSessionManagerKeepAliveSeconds) {
        this.roomSessionManagerKeepAliveSeconds = roomSessionManagerKeepAliveSeconds;
    }

    public final String getKEY_USER() {
        return KEY_USER;
    }

    public static int getSTATE_NONE() {
        return STATE_NONE;
    }

    public static int getSTATE_SCAN_GATEWAY_SESSION() {
        return STATE_SCAN_GATEWAY_SESSION;
    }

    public static int getSTATE_STARTED() {
        return STATE_STARTED;
    }

    public static int getSTATE_PAUSED() {
        return STATE_PAUSED;
    }

    public static int getSTATE_TERMINATED() {
        return STATE_TERMINATED;
    }

    public StateMachine<Integer, GatewaySessionManager> getStateMachine() {
        return stateMachine;
    }

    public void setStateMachine(StateMachine<Integer, GatewaySessionManager> stateMachine) {
        this.stateMachine = stateMachine;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public ConcurrentHashMap<String, GatewaySessionHandler> getUserIdGatewaySessionHandlerMap() {
        return userIdGatewaySessionHandlerMap;
    }

    public void setUserIdGatewaySessionHandlerMap(ConcurrentHashMap<String, GatewaySessionHandler> userIdGatewaySessionHandlerMap) {
        this.userIdGatewaySessionHandlerMap = userIdGatewaySessionHandlerMap;
    }

    public ConcurrentHashMap<String, UserChannel> getUserIdGatewayChannelMap() {
        return userIdGatewayChannelMap;
    }

    public void setUserIdGatewayChannelMap(ConcurrentHashMap<String, UserChannel> userIdGatewayChannelMap) {
        this.userIdGatewayChannelMap = userIdGatewayChannelMap;
    }

    public ConcurrentHashMap<String, String> getTokenUserIdMap() {
        return tokenUserIdMap;
    }

    public void setTokenUserIdMap(Map<String, String> tokenUserIdMap) {
        this.tokenUserIdMap = tokenUserIdMap;
    }

    public GatewayChannelModule getGatewayChannelModule() {
        return gatewayChannelModule;
    }

    public void setGatewayChannelModule(GatewayChannelModule gatewayChannelModule) {
        this.gatewayChannelModule = gatewayChannelModule;
    }

    public Closure getHandlerStartedState() {
        return handlerStartedState;
    }

    public void setHandlerStartedState(Closure handlerStartedState) {
        this.handlerStartedState = handlerStartedState;
    }

    public Closure getHandleScanRoomSessionsState() {
        return handleScanRoomSessionsState;
    }

    public void setHandleScanRoomSessionsState(Closure handleScanRoomSessionsState) {
        this.handleScanRoomSessionsState = handleScanRoomSessionsState;
    }

    private final String TAG = GatewaySessionManager.class.getSimpleName();
    @ConfigProperty(name = "gateway.jwt.key")
    private String jwtKey = "a#\$*(@#&\$KFRkj3245ju8f78#\$_)&";
    @ConfigProperty(name = "gateway.jwt.expire.seconds")
    private Integer jwtExpireSeconds = 60 * 60 * 12;
    @ConfigProperty(name = "gateway.session.expire.check.period.seconds")
    private Long sessionExpireCheckPeriodSeconds = 60;
    @ConfigProperty(name = "gateway.session.inactive.expire.seconds")
    private Long sessionInactiveExpireTime = TimeUnit.MINUTES.toMillis(10);
    @ConfigProperty(name = "gateway.authorised.expression")
    private String authorisedExpression = ".*_.*Apis_.*";
    @ServiceBean(name = GatewayManagerService.SERVICE)
    private GatewayManagerService gatewayManagerService;
    private final String JWT_FIELD_USER_ID = "u";
    private static final String JWT_FIELD_AUTHORISED_SERVICES = "as";
    private static final String JWT_FIELD_DEVICE_TOKEN_CRC = "d";
    private static final String JWT_FIELD_TERMINAL = "t";
    private static final String JWT_FIELD_ACTIVE_LOGIN = "a";
    @JavaBean
    private Context context;
    @Bean
    private WebSocketManager webSocketManager;
    private final Object userLock = new Object();
    @ConfigProperty(name = "gateway.session.manager.queue.capacity")
    private int roomSessionManagerQueueCapacity = 10000;
    @ConfigProperty(name = "gateway.session.manager.core.size")
    private int roomSessionManagerCoreSize = 5;
    @ConfigProperty(name = "gateway.session.manager.maximum.pool.size")
    private int roomSessionManagerMaximumPoolSize = 500;
    @ConfigProperty(name = "gateway.session.manager.keep.alive.seconds")
    private int roomSessionManagerKeepAliveSeconds = 120;
    private final String KEY_USER = "user";
    private static final int STATE_NONE = 1;
    private static final int STATE_SCAN_GATEWAY_SESSION = 10;
    private static final int STATE_STARTED = 20;
    private static final int STATE_PAUSED = 30;
    private static final int STATE_TERMINATED = 120;
    private Class<? extends GatewaySessionHandler> gatewaySessionHandlerClass;
    private StateMachine<Integer, GatewaySessionManager> stateMachine;
    private ThreadPoolExecutor threadPoolExecutor;
    @ConfigProperty(name = "gateway.max.users")
    private int maxChannels = 8000;
    private ConcurrentHashMap<String, GatewaySessionHandler> userIdGatewaySessionHandlerMap = new ConcurrentHashMap<String, GatewaySessionHandler>();
    private ConcurrentHashMap<String, UserChannel> userIdGatewayChannelMap = new ConcurrentHashMap<String, UserChannel>();
    private Map<String, String> tokenUserIdMap = new ConcurrentHashMap<String, String>();
    private volatile int roomCounter = 0;
    private ConcurrentHashMap<String, SingleThreadQueueEx> userIdSingleThreadMap = new ConcurrentHashMap<String, SingleThreadQueueEx>();
    @Bean
    private GatewayChannelModule gatewayChannelModule;
    private Closure handlerStartedState = new Closure(this, this) {
        public Object doCall(Object roomSessionManager, Object stateMachine) {
            getWebSocketManager().invokeMethod("start", new Object[0]);

            return ScheduledExecutorHolder.scheduledExecutorService.invokeMethod("scheduleAtFixedRate", new Object[]{new Closure(DUMMY__1234567890_DUMMYYYYYY___.this, DUMMY__1234567890_DUMMYYYYYY___.this) {
                public void doCall(Object it) {
                    List<GatewaySessionHandler> deletedHandlers = new ArrayList<GatewaySessionHandler>();
                    Collection<GatewaySessionHandler> gatewaySessionHandlers = getUserIdGatewaySessionHandlerMap().values();
                    for (GatewaySessionHandler gatewaySessionHandler : gatewaySessionHandlers) {
                        if (System.currentTimeMillis() - gatewaySessionHandler.getTouch() > getSessionInactiveExpireTime()) {
                            deletedHandlers.add(gatewaySessionHandler);
                        }

                    }

                    for (GatewaySessionHandler gatewaySessionHandler : deletedHandlers) {
                        if (System.currentTimeMillis() - gatewaySessionHandler.getTouch() > getSessionInactiveExpireTime()) {
                            LoggerEx.invokeMethod("info", new Object[]{getTAG(), "GatewaySessionHandler expired, will be removed, id " + gatewaySessionHandler.getId() + ". last touch time " + String.valueOf(new Date(gatewaySessionHandler.getTouch())) + " sessionInactiveExpireTime " + String.valueOf(getSessionInactiveExpireTime())});
                            closeSession(gatewaySessionHandler.getId());
                        }

                    }

                }

                public void doCall() {
                    doCall(null);
                }

            }, getSessionExpireCheckPeriodSeconds(), getSessionExpireCheckPeriodSeconds(), TimeUnit.SECONDS});
        }

    };
    private Closure handleScanRoomSessionsState = new Closure(this, this) {
        public Object doCall(Object roomSessionManager, Object stateMachine) {
            Collection<Class<?>> classes = getContext().invokeMethod("getClasses", new Object[0]);
            if (classes != null) {
                for (Class clazz : classes) {
                    GatewaySession annotation = (GatewaySession) clazz.getAnnotation(GatewaySession.class);
                    if (annotation != null) {
                        if (!GatewaySessionHandler.class.isAssignableFrom(clazz).asBoolean()) {
                            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "GatewaySession annotation is found on class " + String.valueOf(clazz) + ", but not implemented GatewaySessionHandler which is a must. Ignore this class..."});
                            continue;
                        }

                        if (!ReflectionUtil.invokeMethod("canBeInitiated", new Object[]{clazz}).asBoolean()) {
                            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "GatewaySession annotation is found on class " + String.valueOf(clazz) + ", but not be initialized with empty parameter which is a must. Ignore this class..."});
                            continue;
                        }

                        gatewaySessionHandlerClass = (Class<? extends GatewaySessionHandler>) clazz;
                        LoggerEx.invokeMethod("info", new Object[]{getTAG(), "Found gatewaySessionHandlerClass Class " + String.valueOf(gatewaySessionHandlerClass) + ", can only support one handler class, scanning will be stopped"});
                        break;
                    }

                }

            }

            if (gatewaySessionHandlerClass == null) {
                return GatewaySessionManager.this.getStateMachine().invokeMethod("gotoState", new Object[]{GatewaySessionManager.getSTATE_TERMINATED(), "No gatewaySessionHandler found"});
            } else {
                return GatewaySessionManager.this.getStateMachine().invokeMethod("gotoState", new Object[]{GatewaySessionManager.getSTATE_STARTED(), "Scanned gatewaySessionHandler " + String.valueOf(GatewaySessionManager.this.gatewaySessionHandlerClass)});
            }

        }

    };
}
