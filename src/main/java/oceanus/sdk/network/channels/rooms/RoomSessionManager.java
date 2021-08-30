package oceanus.sdk.network.channels.rooms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 房间会话管理器， 房间服务器需要启动该类做WebSocket服务器以及房间相关逻辑
 */
@CompileStatic
@Import(classes = {RoomStatusService.class})
@Bean
public class RoomSessionManager extends GroovyObjectSupport {
    public LinkedHashMap<String, Object> memory() {
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<String, Object>(10);
        map1.put("webSocketManager", webSocketManager.invokeMethod("memory", new Object[0]));
        map1.put("threadPoolExecutor", threadPoolExecutor.toString());
        map1.put("stateMachine", stateMachine.invokeMethod("toString", new Object[0]));
        map1.put("handlerClass", MessageUtils.invokeMethod("toJSONString", new Object[]{roomTypeToRoomSessionHandlerClassMap}));
        map1.put("roomCounter", roomCounter);
        map1.put("maxRooms", maxRooms);
        map1.put("groupRoomIdsMap", groupRoomIdsMap);
        map1.put("supportJoinAtRuntime", supportJoinAtRuntime);
        map1.put("canJoinRoomIdSessionHandlers", canJoinRoomIdSessionHandlers);
        map1.put("jwtExpiredSeconds", jwtExpiredSeconds);
        LinkedHashMap<String, Object> map = map1;
//        Set<String> handlerKeys = roomIdSessionHandlerMap.keySet()
//        def handlerMap = [:]
//        for(String key : handlerKeys) {
//            RoomSessionHandler roomSessionHandler = roomIdSessionHandlerMap.get(key)
//            handlerMap.put(key, roomSessionHandler.memory())
//        }
//        map.put("roomIdSessionHandlerMap", handlerMap)
        MemoryUtils.invokeMethod("fillMemoryIntoMap", new Object[]{map, getRoomIdSessionHandlerMap(), "roomIdSessionHandlerMap"});

//        Set<String> threadKeys = roomIdSingleThreadMap.keySet()
//        def threadMap = [:]
//        for(String key : threadKeys) {
//            SingleThreadQueue queue = roomIdSingleThreadMap.get(key)
//            threadMap.put(key, queue.toString())
//        }
//        map.put("singleThreadMap", threadMap)
        MemoryUtils.invokeMethod("fillToStringIntoMap", new Object[]{map, roomIdSingleThreadMap, "roomIdSingleThreadMap"});

        MemoryUtils.invokeMethod("fillToStringIntoMap", new Object[]{map, getUserIdUserSessionMap(), "userIdUserSessionMap"});
        return map;
    }

    public void start() {
        jwtKey = ((String) (MD5Util.invokeMethod("md5", new Object[]{(getContext().serverConfig.service + getJwtKeySuffix()).invokeMethod("getBytes", new Object[]{"utf-8"})})));
        threadPoolExecutor = new ThreadPoolExecutor(roomSessionManagerCoreSize, roomSessionManagerMaximumPoolSize, roomSessionManagerKeepAliveSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue(roomSessionManagerQueueCapacity), new ThreadFactoryBuilder().setNameFormat("RoomSessionManager-%d").build());

        if (supportJoinAtRuntime) {
            canJoinRoomIdSessionHandlers = Sets.newConcurrentHashSet();
        }

        stateMachine = ((StateMachine<Integer, RoomSessionManager>) (new StateMachine<>(RoomSessionManager.class.getSimpleName() + "#", STATE_NONE, this)));
        stateMachine.invokeMethod("configState", new Object[]{RoomSessionManager.getSTATE_NONE(), getStateMachine().invokeMethod("execute", new Object[0]).invokeMethod("nextStates", new Object[]{RoomSessionManager.getSTATE_SCAN_ROOM_SESSION()})}).invokeMethod("configState", new Object[]{RoomSessionManager.getSTATE_SCAN_ROOM_SESSION(), getStateMachine().invokeMethod("execute", new Object[]{(StateExecutor<Integer, RoomSessionManager>) getHandleScanRoomSessionsState()}).invokeMethod("nextStates", new Object[]{RoomSessionManager.getSTATE_STARTED(), RoomSessionManager.getSTATE_TERMINATED()})}).invokeMethod("configState", new Object[]{RoomSessionManager.getSTATE_STARTED(), getStateMachine().invokeMethod("execute", new Object[]{(StateExecutor<Integer, RoomSessionManager>) getHandlerStartedState()}).invokeMethod("nextStates", new Object[]{RoomSessionManager.getSTATE_PAUSED(), RoomSessionManager.getSTATE_TERMINATED()})}).invokeMethod("configState", new Object[]{RoomSessionManager.getSTATE_PAUSED(), getStateMachine().invokeMethod("execute", new Object[0]).invokeMethod("nextStates", new Object[]{RoomSessionManager.getSTATE_STARTED(), RoomSessionManager.getSTATE_TERMINATED()})}).invokeMethod("configState", new Object[]{RoomSessionManager.getSTATE_TERMINATED(), getStateMachine().invokeMethod("execute", new Object[0])});

        stateMachine.invokeMethod("gotoState", new Object[]{RoomSessionManager.getSTATE_SCAN_ROOM_SESSION(), "Start scanning RoomSessionHandler class"});
    }

    public void stop() {
        LoggerEx.invokeMethod("info", new Object[]{getTAG(), "RoomSessionManager is stopping..."});
        final long time = System.currentTimeMillis();
        Collection<String> roomIds = new ArrayList<String>(roomIdSessionHandlerMap.keySet());
        for (String roomId : roomIds) {
            closeRoom(roomId);
        }

        webSocketManager.invokeMethod("stop", new Object[0]);
        LoggerEx.invokeMethod("info", new Object[]{getTAG(), "RoomSessionManager stopped, takes " + String.valueOf(System.currentTimeMillis() - time)});
    }

    public String generateJwtToken(UserSession userSession, int expireSeconds) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put(KEY_USER, JSON.toJSONString(userSession));
        String token = JWTUtils.invokeMethod("createToken", new Object[]{getJwtKey(), claims, TimeUnit.SECONDS.toMillis(expireSeconds)});
        return token;
    }

    public UserSession parseJwtToken(String token) {
        Claims claims = null;
        try {
            claims = ((Claims) (JWTUtils.invokeMethod("getClaims", new Object[]{getJwtKey(), token})));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            LoggerEx.invokeMethod("info", new Object[]{getTAG(), "Parse jwt token failed, " + throwable.getMessage() + ", token " + token});
        }

        UserSession userSession = null;
        if (claims != null) {
            String value = claims.invokeMethod("get", new Object[]{getKEY_USER()});
            if (value != null) {
                try {
                    userSession = ((UserSession) (JSON.class.invokeMethod("parseObject", new Object[]{value, UserSession.class})));
                } catch (Throwable throwable1) {
                    throwable1.printStackTrace();
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "Parse UserSession json string failed, " + throwable1.getMessage() + ", value " + value});
                }

            }

        }

        return userSession;
    }

    public JoinRoomAssignmentHandler getJoinRoomAssignmentHandler() {
        return joinRoomAssignmentHandler;
    }

    public void setJoinRoomAssignmentHandler(JoinRoomAssignmentHandler joinRoomAssignmentHandler) {
        this.joinRoomAssignmentHandler = joinRoomAssignmentHandler;
    }

    public CreateRoomAssignmentHandler getCreateRoomAssignmentHandler() {
        return createRoomAssignmentHandler;
    }

    public void setCreateRoomAssignmentHandler(CreateRoomAssignmentHandler createRoomAssignmentHandler) {
        this.createRoomAssignmentHandler = createRoomAssignmentHandler;
    }

    private SingleThreadQueueEx<RoomAction> getRoomActionQueue(String roomId) {
        SingleThreadQueueEx<RoomAction> queue = (SingleThreadQueueEx<RoomAction>) roomIdSingleThreadMap.get(roomId);
        if (queue == null) {
            RoomActionHandler roomActionHandler = new RoomActionHandler();
            context.invokeMethod("injectBean", new Object[]{roomActionHandler});
            queue = new SingleThreadQueueEx<RoomAction>(threadPoolExecutor, roomActionHandler);
            SingleThreadQueueEx old = roomIdSingleThreadMap.putIfAbsent(roomId, (SingleThreadQueueEx) queue);
            if (old != null) queue = ((SingleThreadQueueEx<RoomAction>) (old));
        }

        return queue;
    }

    public RoomSessionHandler addOrGetRoom(int maxUsers, String roomType, String group) {
        return addOrGetRoom(maxUsers, group, roomType, false, null);
    }

    public RoomSessionHandler addOrGetRoom(int maxUsers, String roomType, String group, String roomId) {
        return addOrGetRoom(maxUsers, group, roomType, false, roomId);
    }

    /**
     * 清理超时房间
     */
    public void clearTimeOutRoom() {
        roomIdSessionHandlerMap.values().forEach(new Closure(this, this) {
            public void doCall(Object roomSessionHandler) {

                Long disbandRoomTime = ((RoomSessionHandler) roomSessionHandler).getDisbandRoomTime();
                if (disbandRoomTime != null && disbandRoomTime != 0 && disbandRoomTime < System.currentTimeMillis()) {
                    LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "clear time out Room :" + ((RoomSessionHandler) roomSessionHandler).getId()});
                    closeRoom(((RoomSessionHandler) roomSessionHandler).getId());
                }

            }

        });
    }

    public boolean canCreateRoom() {
        return roomCounter <= maxRooms;
    }

    public RoomSessionHandler addOrGetRoom(int maxUsers, String group, String roomType, boolean forbiddenNewUserJoin, String roomId) {
        return addOrGetRoom(maxUsers, group, roomType, false, roomId, null);
    }

    public RoomSessionHandler addOrGetRoom(int maxUsers, String group, String roomType, boolean forbiddenNewUserJoin, String roomId, JSONObject info) {
        checkState();
        Class roomSessionHandlerClazz = roomTypeToRoomSessionHandlerClassMap.get(roomType);
        if (roomSessionHandlerClazz == null) {
            throw new CoreException(Errors.ERROR_ROOM_TYPE_UNKNOWN);
        }

        RoomSessionHandler handler = roomSessionHandlerClazz.getConstructor().newInstance();
        handler.setMaxUsers(maxUsers);
        handler.setGroup(group);
        handler.setId((roomId == null ? ObjectId.invokeMethod("get", new Object[0]).invokeMethod("toString", new Object[0]) : roomId));
        handler.setForbiddenNewUserJoin(forbiddenNewUserJoin);
        handler.setInfo(info);
        context.invokeMethod("injectBean", new Object[]{handler});
        handler.updateDisbandRoomTime();
//        if (configureRoomSessionHandlerBeforeAdd != null) {
//            try {
//                configureRoomSessionHandlerBeforeAdd.configure(handler)
//            } catch (Throwable throwable) {
//                LoggerEx.error(TAG, "configureRoomSessionHandlerBeforeAdd failed, ${throwable.message} for handler $handler")
//            }
//        }
        boolean needInit = false;
        if (roomCounter <= maxRooms) {
            synchronized (roomLock) {
                if (roomCounter <= maxRooms) {
                    RoomSessionHandler oldRoomSessionHandler = roomIdSessionHandlerMap.putIfAbsent(handler.getId(), handler);
                    if (oldRoomSessionHandler == null) {
                        Set<String> roomIdSet = groupRoomIdsMap.get(group);
                        if (roomIdSet == null) {
                            roomIdSet = Sets.newConcurrentHashSet();
                            Set<String> old = groupRoomIdsMap.putIfAbsent(group, roomIdSet);
                            if (old != null) {
                                roomIdSet = old;
                            }

                        }

                        roomIdSet.add(handler.getId());
                        roomCounter++;
                        needInit = true;
                    } else {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "roomSessionHandler id " + handler.getId() + " exists already, the " + String.valueOf(handler) + " will be ignored, return old " + String.valueOf(oldRoomSessionHandler)});
                        return oldRoomSessionHandler;
                    }

                }

            }

            if (needInit) {
                handler.init();
            }

        } else {
            throw new CoreException(Errors.ERROR_EXCEED_ROOM_CAPACITY, "Exceed room capacity " + String.valueOf(maxRooms) + ", current " + String.valueOf(roomCounter));
        }


        SingleThreadQueueEx<RoomAction> queue = getRoomActionQueue(handler.getId());
        RoomAction action = new RoomAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(handler)action.setRoomId(handler.getId())action.setAction(RoomAction.ACTION_ROOM_CREATED)});
        return handler;
    }

    public boolean invokeOnRoomThread(String roomId, Closure<Void> closure, Object... args) {
        if (roomId == null || closure == null) {
            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "invokeOnRoomThread ignored, because of illegal arguments, roomId " + roomId + " closure " + String.valueOf(closure) + ", args " + String.valueOf(args)});
            return false;
        }


        RoomSessionHandler roomSessionHandler = roomIdSessionHandlerMap.get(roomId);
        if (roomSessionHandler == null) {
            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "invokeOnRoomThread ignored, because room doesn\'t exist for roomId " + roomId + " closure " + String.valueOf(closure) + ", args " + String.valueOf(args)});
            return false;
        }

        roomSessionHandler.updateDisbandRoomTime();
        SingleThreadQueueEx<RoomAction> queue = getRoomActionQueue(roomSessionHandler.getId());
        RoomAction action = new RoomAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler)action.setRoomId(roomSessionHandler.getId())action.setAction(RoomAction.ACTION_ROOM_CLOSURE)action.setClosure(closure)action.setClosureArgs(args)});
        return true;
    }

    public void closeRoom(String roomId) {
        checkState();

        RoomSessionHandler roomSessionHandler;
        synchronized (roomLock) {
            roomSessionHandler = roomIdSessionHandlerMap.remove(roomId);
            if (roomSessionHandler == null)
                throw new CoreException(Errors.ERROR_ROOM_NOT_EXIST, "Room " + roomId + " not exist while closing room");
            Set<String> roomIdSet = groupRoomIdsMap.get(roomSessionHandler.getGroup());
            if (roomIdSet != null) {
                roomIdSet.remove(roomId);
            }

            roomCounter--;
        }

        synchronized (roomSessionHandler) {
            Set<String> keys = roomSessionHandler.getUserIdUserSessionMap().keySet();
            for (String key : keys) {
                userIdUserSessionMap.remove(key);
                userChannelModule.invokeMethod("close", new Object[]{key, Errors.ERROR_CHANNEL_ROOM_CLOSED});
            }

            roomSessionHandler.getUserIdUserSessionMap().clear();
            if (supportJoinAtRuntime) {
                canJoinRoomIdSessionHandlers.remove(roomId);
            }

        }


        SingleThreadQueueEx<RoomAction> queue = getRoomActionQueue(roomSessionHandler.getId());
        RoomAction action = new RoomAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler)action.setRoomId(roomSessionHandler.getId())action.setAction(RoomAction.ACTION_ROOM_DESTROYED)});
    }

    public RoomSessionHandler getRoom(String roomId) {
        return roomIdSessionHandlerMap.get(roomId);
    }

    /**
     * 为房间预留位置， 但是预留的时间不会超过 UserChannelModule#userSessionCacheExpireSeconds
     *
     * @param userId
     * @param roomId
     * @return
     */
    public boolean addReservedUserId(String userId, String roomId) {
        RoomSessionHandler roomSessionHandler = roomIdSessionHandlerMap.get(roomId);
        if (roomSessionHandler != null) {
            synchronized (roomSessionHandler) {
                roomSessionHandler = roomIdSessionHandlerMap.get(roomId);
                if (roomSessionHandler != null && !roomSessionHandler.getUserIdUserSessionMap().containsKey(userId) && roomSessionHandler.getUserIdUserSessionMap().size() + roomSessionHandler.getReservedUserIds().size() < roomSessionHandler.getMaxUsers()) {
                    roomSessionHandler.getReservedUserIds().add(userId);
                    if (supportJoinAtRuntime) {
                        if (roomSessionHandler.getUserIdUserSessionMap().size() + roomSessionHandler.getReservedUserIds().size() < roomSessionHandler.getMaxUsers()) {
                            canJoinRoomIdSessionHandlers.add(roomId);
                        } else {
                            canJoinRoomIdSessionHandlers.remove(roomId);
                        }

                    }

                    return true;
                }

            }

        }

        return false;
    }

    public boolean removeReservedUserId(String userId, String roomId) {
        RoomSessionHandler roomSessionHandler = roomIdSessionHandlerMap.get(roomId);
        if (roomSessionHandler != null) {
            synchronized (roomSessionHandler) {
                roomSessionHandler = roomIdSessionHandlerMap.get(roomId);
                if (roomSessionHandler != null && !roomSessionHandler.getUserIdUserSessionMap().containsKey(userId) && roomSessionHandler.getReservedUserIds().contains(userId)) {
                    roomSessionHandler.getReservedUserIds().remove(userId);
                    if (supportJoinAtRuntime) {
                        if (roomSessionHandler.getUserIdUserSessionMap().size() + roomSessionHandler.getReservedUserIds().size() < roomSessionHandler.getMaxUsers()) {
                            canJoinRoomIdSessionHandlers.add(roomId);
                        } else {
                            canJoinRoomIdSessionHandlers.remove(roomId);
                        }

                    }

                    return true;
                }

            }

        }

        return false;

    }

    private void doUserLeft(RoomSessionHandler roomSessionHandler, UserSession userSession) {
        try {
            roomSessionHandler.userDisconnected(userSession.userId);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "userDisconnected userId " + String.class.invokeMethod("valueOf", new Object[]{userSession.userId}) + " failed, " + throwable.getMessage()});
        }


        SingleThreadQueueEx<RoomAction> queue = getRoomActionQueue(userSession.roomId);
        RoomAction action = new RoomAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler)action.setRoomId(userSession.roomId)action.setUserSession(userSession)action.setAction(RoomAction.ACTION_USER_LEFT)});
    }

    private void doViewerLeft(RoomSessionHandler roomSessionHandler, UserSession userSession) {
        try {
            roomSessionHandler.viewerDisconnected(userSession.userId);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "viewerDisconnected userId " + String.class.invokeMethod("valueOf", new Object[]{userSession.userId}) + " failed, " + throwable.getMessage()});
        }


        SingleThreadQueueEx<RoomAction> queue = getRoomActionQueue(userSession.roomId);
        RoomAction action = new RoomAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler)action.setRoomId(userSession.roomId)action.setUserSession(userSession)action.setAction(RoomAction.ACTION_VIEWER_LEFT)});
    }

    public String addViewerToRoom(UserSession userSession) {
        return addViewerToRoom(userSession, false);
    }

    public String addViewerToRoom(UserSession userSession, boolean needGenerateToken) {
        final groovy.lang.Reference<UserSession> session = new groovy.lang.Reference<UserSession>(userSession);

        checkState();
        RoomSessionHandler roomSessionHandler = roomIdSessionHandlerMap.get(session.get().roomId);
        if (roomSessionHandler == null)
            throw new CoreException(Errors.ERROR_ROOM_NOT_EXIST, "Room " + String.class.invokeMethod("valueOf", new Object[]{session.get().roomId}) + " not exist while add user to room");

        if (!roomSessionHandler.getUserIdUserSessionViewerMap().containsKey(session.get().userId).asBoolean()) {
            synchronized (roomSessionHandler) {
                RoomSessionHandler theRoomSessionHandler = roomIdSessionHandlerMap.get(session.get().roomId);
                if (theRoomSessionHandler == null)
                    throw new CoreException(Errors.ERROR_ROOM_NOT_EXIST, "Room " + String.class.invokeMethod("valueOf", new Object[]{session.get().roomId}) + " not exist while add user to room");
                roomSessionHandler = theRoomSessionHandler;
                if (!roomSessionHandler.getUserIdUserSessionViewerMap().containsKey(session.get().userId).asBoolean()) {
                    UserSession old = roomSessionHandler.getUserIdUserSessionViewerMap().putIfAbsent(session.get().userId, session.get());
                    if (old != null) {
                        //如果用户已经在房间里， 按正常逻辑处理， 返回token
                        session.set(old);
//                            throw new CoreException(Errors.ERROR_ROOM_USER_ALREADY_EXIST, "Room $userSession.roomId add user failed because already exit, userSession $userSession old $old")
                    }

                    if (roomSessionHandler.getReservedUserIds().contains(session.get().userId)) {
                        roomSessionHandler.getReservedUserIds().remove(session.get().userId);
                    }

                    if (roomSessionHandler.getUserIdUserSessionMap().containsKey(session.get().userId)) {
                        roomSessionHandler.getUserIdUserSessionMap().remove(session.get().userId);
                        doUserLeft(roomSessionHandler, session.get());
                    }


                    UserSession oldUserSession = userIdUserSessionMap.put(session.get().userId, session.get());
                    if (oldUserSession != null) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "userIdUserSessionMap put new userSession as viewer " + String.valueOf(session.get()) + " replaced old " + String.valueOf(oldUserSession) + ", it is not usual, please check"});
                    }

                }

            }

        }


        String token = null;
        if (needGenerateToken) {
            token = generateJwtToken(session.get(), jwtExpiredSeconds);
        }


        if (!userChannelModule.userIdChannelMap.invokeMethod("containsKey", new Object[]{session.get().userId}).asBoolean()) {
            session.get().joinFuture = ScheduledExecutorHolder.scheduledExecutorService.invokeMethod("schedule", new Object[]{new Closure(this, this) {
                public UserSession doCall(Object it) {
                    try {
                        return removeUserOrViewerFromRoom(session.get().userId, session.get().roomId);
                    } catch (Throwable throwable) {
                        return LoggerEx.invokeMethod("error", new Object[]{getTAG(), "User doesn\'t join on time (" + String.valueOf(jwtExpiredSeconds + 3) + " seconds), but remove user " + String.class.invokeMethod("valueOf", new Object[]{session.get().userId}) + " from room " + String.class.invokeMethod("valueOf", new Object[]{session.get().roomId}) + " failed, " + throwable.getMessage()});
                    }

                }

                public UserSession doCall() {
                    return doCall(null);
                }

            }, jwtExpiredSeconds + 3, TimeUnit.SECONDS});
        }


        SingleThreadQueueEx<RoomAction> queue = getRoomActionQueue(session.get().roomId);
        RoomAction action = new RoomAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler)action.setRoomId(session.get().roomId)action.setUserSession(session.get())action.setAction(RoomAction.ACTION_VIEWER_WILL_JOIN)});
        return token;
    }

    public String addUserToRoom(UserSession userSession) {
        return addUserToRoom(userSession, true);
    }

    public String addUserToRoom(final UserSession userSession, boolean needGenerateToken) {
        checkState();
        final Reference<RoomSessionHandler> roomSessionHandler = new groovy.lang.Reference<RoomSessionHandler>(roomIdSessionHandlerMap.get(userSession.roomId));
        if (roomSessionHandler.get() == null)
            throw new CoreException(Errors.ERROR_ROOM_NOT_EXIST, "Room " + String.class.invokeMethod("valueOf", new Object[]{userSession.roomId}) + " not exist while add user to room");

        boolean exceeded = false;

        if (roomSessionHandler.get().getReservedUserIds().contains(userSession.userId) || roomSessionHandler.get().getUserIdUserSessionMap().size() + roomSessionHandler.get().getReservedUserIds().size() < roomSessionHandler.get().getMaxUsers()) {
            synchronized (roomSessionHandler.get()) {
                RoomSessionHandler theRoomSessionHandler = roomIdSessionHandlerMap.get(userSession.roomId);
                if (theRoomSessionHandler == null)
                    throw new CoreException(Errors.ERROR_ROOM_NOT_EXIST, "Room " + String.class.invokeMethod("valueOf", new Object[]{userSession.roomId}) + " not exist while add user to room");
                roomSessionHandler.set(theRoomSessionHandler);
                if (roomSessionHandler.get().getReservedUserIds().contains(userSession.userId) || roomSessionHandler.get().getUserIdUserSessionMap().size() + roomSessionHandler.get().getReservedUserIds().size() < roomSessionHandler.get().getMaxUsers()) {
                    UserSession old = roomSessionHandler.get().getUserIdUserSessionMap().put(userSession.userId, userSession);
//                    if(old != null) {
//                        //如果用户已经在房间里， 按正常逻辑处理， 返回token
//                        userSession = old
////                            throw new CoreException(Errors.ERROR_ROOM_USER_ALREADY_EXIST, "Room $userSession.roomId add user failed because already exit, userSession $userSession old $old")
//                    } else {
//                    }
                    if (roomSessionHandler.get().getReservedUserIds().contains(userSession.userId)) {
                        roomSessionHandler.get().getReservedUserIds().remove(userSession.userId);
                    }

                    if (roomSessionHandler.get().getUserIdUserSessionViewerMap().containsKey(userSession.userId)) {
                        roomSessionHandler.get().getUserIdUserSessionViewerMap().remove(userSession.userId);
                        doViewerLeft(roomSessionHandler.get(), userSession);
                    }

                    if (supportJoinAtRuntime) {
                        if (roomSessionHandler.get().getUserIdUserSessionMap().size() + roomSessionHandler.get().getReservedUserIds().size() < roomSessionHandler.get().getMaxUsers()) {
                            canJoinRoomIdSessionHandlers.add(userSession.roomId);
                        } else {
                            canJoinRoomIdSessionHandlers.remove(userSession.roomId);
                        }

                    }

                    UserSession oldUserSession = userIdUserSessionMap.put(userSession.userId, userSession);
                    if (oldUserSession != null) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "userIdUserSessionMap put new userSession " + String.valueOf(userSession) + " replaced old " + String.valueOf(oldUserSession) + ", it is not usual, please check"});
                    }

                } else {
                    if (supportJoinAtRuntime) {
                        canJoinRoomIdSessionHandlers.remove(userSession.roomId);
                    }

                    exceeded = true;
                }

            }

        } else {
            exceeded = true;
        }

        if (exceeded)
            throw new CoreException(Errors.ERROR_EXCEED_USER_CAPACITY, "Room " + String.class.invokeMethod("valueOf", new Object[]{userSession.roomId}) + " add user failed because exceed capacity " + String.valueOf(roomSessionHandler.get().getMaxUsers()) + " current " + String.valueOf(roomSessionHandler.get().getUserIdUserSessionMap().size()));

        String token = null;
        if (needGenerateToken) {
            token = generateJwtToken(userSession, jwtExpiredSeconds);
        }


        if (!userChannelModule.userIdChannelMap.invokeMethod("containsKey", new Object[]{userSession.userId}).asBoolean()) {
            userSession.joinFuture = ScheduledExecutorHolder.scheduledExecutorService.invokeMethod("schedule", new Object[]{new Closure(this, this) {
                public UserSession doCall(Object it) {
                    try {
                        return removeUserOrViewerFromRoom(userSession.userId, userSession.roomId);
                    } catch (Throwable throwable) {
                        return LoggerEx.invokeMethod("error", new Object[]{getTAG(), "User doesn\'t join on time (" + String.valueOf(jwtExpiredSeconds + 3) + " seconds), but remove user " + String.class.invokeMethod("valueOf", new Object[]{userSession.userId}) + " from room " + String.class.invokeMethod("valueOf", new Object[]{userSession.roomId}) + " failed, " + throwable.getMessage()});
                    }

                }

                public UserSession doCall() {
                    return doCall(null);
                }

            }, jwtExpiredSeconds + 3, TimeUnit.SECONDS});
        }


        SingleThreadQueueEx<RoomAction> queue = getRoomActionQueue(userSession.roomId);
        RoomAction action = new RoomAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler.get())action.setRoomId(userSession.roomId)action.setUserSession(userSession)action.setAction(RoomAction.ACTION_USER_WILL_JOIN)});
        return token;
    }

    public void connectUserOrViewerToRoom(UserSession userSession) {
        boolean force = false;
        if (userSession.joinFuture == null) {//新通道替换旧通道的时候， 需要强制UserJoined一次， UserJoined会比UserLeft调用次数更多
            force = true;
        }

        boolean result = userSession.joinFuture.invokeMethod("cancel", new Object[]{true});
        userSession.joinFuture = null;
        if (force || result) {
            checkState();
            RoomSessionHandler roomSessionHandler = roomIdSessionHandlerMap.get(userSession.roomId);
            if (roomSessionHandler == null)
                throw new CoreException(Errors.ERROR_ROOM_NOT_EXIST, "Room " + String.class.invokeMethod("valueOf", new Object[]{userSession.roomId}) + " not exist while connect user to room");

            SingleThreadQueueEx<RoomAction> queue = getRoomActionQueue(userSession.roomId);

            if (roomSessionHandler.getUserIdUserSessionMap().containsKey(userSession.userId)) {
                RoomAction action = new RoomAction();


                queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler)action.setRoomId(userSession.roomId)action.setUserSession(userSession)action.setAction(RoomAction.ACTION_USER_JOINED)});
            } else if (roomSessionHandler.getUserIdUserSessionViewerMap().containsKey(userSession.userId)) {
                RoomAction action = new RoomAction();


                queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler)action.setRoomId(userSession.roomId)action.setUserSession(userSession)action.setAction(RoomAction.ACTION_VIEWER_JOINED)});
            } else {
                throw new CoreException(Errors.ERROR_ROOM_USER_NOT_EXIST, "User " + String.class.invokeMethod("valueOf", new Object[]{userSession.userId}) + " not in room or be a viewer. please check. userSession " + String.valueOf(userSession));
            }

        } else {
            LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "connectUserToRoom cancel joinFuture too late. " + String.valueOf(userSession)});
        }

    }

    public UserSession removeUserOrViewerFromRoom(String userId, String roomId) {
        return removeUserOrViewerFromRoom(userId, roomId, true);
    }

    public UserSession removeUserOrViewerFromRoom(String userId, String roomId, boolean needRemoveChannel) {
        checkState();
        RoomSessionHandler roomSessionHandler = roomIdSessionHandlerMap.get(roomId);
        if (roomSessionHandler == null) {
            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "Room " + roomId + " not exist while remove user " + userId + " from room"});
            return;

//            throw new CoreException(Errors.ERROR_ROOM_NOT_EXIST, "Room $roomId not exist while add user to room")
        }


        boolean isViewer = false;

        UserSession userSession;
        synchronized (roomSessionHandler) {
            RoomSessionHandler theRoomSessionHandler = roomIdSessionHandlerMap.get(roomId);
            roomSessionHandler = theRoomSessionHandler;
            if (roomSessionHandler != null) {
//                if(!roomSessionHandler.userIdUserSessionMap.containsKey(userId))
//                    throw new CoreException(Errors.ERROR_ROOM_USER_NOT_EXIST, "Room $roomId userId $userId not exit while remove user from room")

                userSession = roomSessionHandler.getUserIdUserSessionMap().remove(userId);
                if (userSession == null) {
                    userSession = roomSessionHandler.getUserIdUserSessionViewerMap().remove(userId);
                    if (userSession != null) isViewer = true;
                    else {
                        LoggerEx.invokeMethod("error", new Object[]{getTAG(), "Room " + roomId + " userId " + userId + " not exit while remove user from room"});
                        return;

                    }

                }


                if (needRemoveChannel) {
                    boolean channelRemoved = userChannelModule.invokeMethod("close", new Object[]{userId, Errors.ERROR_CHANNEL_USER_REMOVED});
                }

                if (supportJoinAtRuntime) {
                    if (roomSessionHandler.getUserIdUserSessionMap().size() + roomSessionHandler.getReservedUserIds().size() < roomSessionHandler.getMaxUsers()) {
                        canJoinRoomIdSessionHandlers.add(roomId);
                    }

                }

                boolean removed = userIdUserSessionMap.remove(userId, userSession);
                if (!removed.asBoolean()) {
                    LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "userIdUserSessionMap remove userId " + userId + " failed, userSession " + String.valueOf(userSession)});
                }


                if (isViewer) {
                    try {
                        roomSessionHandler.viewerDisconnected(userId);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        LoggerEx.invokeMethod("error", new Object[]{getTAG(), "viewerDisconnected userId " + userId + " failed, " + throwable.getMessage()});
                    }

                } else {
                    try {
                        roomSessionHandler.userDisconnected(userId);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        LoggerEx.invokeMethod("error", new Object[]{getTAG(), "userDisconnected userId " + userId + " failed, " + throwable.getMessage()});
                    }

                }

            }

        }


        SingleThreadQueueEx<RoomAction> queue = getRoomActionQueue(roomId);
        if (isViewer) {
            RoomAction action = new RoomAction();


            queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler)action.setRoomId(roomId)action.setUserSession(userSession)action.setAction(RoomAction.ACTION_VIEWER_LEFT)});
        } else {
            RoomAction action = new RoomAction();


            queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler)action.setRoomId(roomId)action.setUserSession(userSession)action.setAction(RoomAction.ACTION_USER_LEFT)});
        }

        return userSession;
        return null;
    }

    public void receiveIncomingData(UserSession userSession, IncomingData incomingData) {
        RoomSessionHandler roomSessionHandler = roomIdSessionHandlerMap.get(userSession.roomId);
        if (roomSessionHandler == null)
            throw new CoreException(Errors.ERROR_ROOM_NOT_EXIST, "Room " + String.class.invokeMethod("valueOf", new Object[]{userSession.roomId}) + " not exist while receiving incomingData");

        //接收消息采用房间的消息单线程来处理， 和房间状态单线程分开
        SingleThreadQueueEx<RoomAction> queue = getRoomActionQueue(userSession.roomId);
        RoomAction action = new RoomAction();


        queue.invokeMethod("offerAndStart", new Object[]{action.setHandler(roomSessionHandler)action.setIncomingData(incomingData)action.setRoomId(userSession.roomId)action.setUserSession(userSession)action.setAction(RoomAction.ACTION_ROOM_MESSAGE)});
    }

    public void checkState() {
        if (!stateMachine.currentState.equals(STATE_STARTED))
            throw new CoreException(Errors.ERROR_ROOM_MANAGER_NOT_STARTED, "RoomManager not started");
        if (MapUtils.invokeMethod("isEmpty", new Object[]{roomTypeToRoomSessionHandlerClassMap}).asBoolean())
            throw new CoreException(Errors.ERROR_ROOM_HANDLER_CLASS_IS_NULL, "roomHandlerClass is null");
    }

    public int roomCounter() {
        return roomCounter;
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

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getJwtKeySuffix() {
        return jwtKeySuffix;
    }

    public void setJwtKeySuffix(String jwtKeySuffix) {
        this.jwtKeySuffix = jwtKeySuffix;
    }

    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }

    public void setWebSocketManager(WebSocketManager webSocketManager) {
        this.webSocketManager = webSocketManager;
    }

    public final Object getRoomLock() {
        return roomLock;
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

    public static int getSTATE_SCAN_ROOM_SESSION() {
        return STATE_SCAN_ROOM_SESSION;
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

    public StateMachine<Integer, RoomSessionManager> getStateMachine() {
        return stateMachine;
    }

    public void setStateMachine(StateMachine<Integer, RoomSessionManager> stateMachine) {
        this.stateMachine = stateMachine;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public ConcurrentHashMap<String, Set<String>> getGroupRoomIdsMap() {
        return groupRoomIdsMap;
    }

    public void setGroupRoomIdsMap(ConcurrentHashMap<String, Set<String>> groupRoomIdsMap) {
        this.groupRoomIdsMap = groupRoomIdsMap;
    }

    public ConcurrentHashMap<String, RoomSessionHandler> getRoomIdSessionHandlerMap() {
        return roomIdSessionHandlerMap;
    }

    public void setRoomIdSessionHandlerMap(ConcurrentHashMap<String, RoomSessionHandler> roomIdSessionHandlerMap) {
        this.roomIdSessionHandlerMap = roomIdSessionHandlerMap;
    }

    public ConcurrentHashMap<String, UserSession> getUserIdUserSessionMap() {
        return userIdUserSessionMap;
    }

    public void setUserIdUserSessionMap(ConcurrentHashMap<String, UserSession> userIdUserSessionMap) {
        this.userIdUserSessionMap = userIdUserSessionMap;
    }

    public Set<String> getCanJoinRoomIdSessionHandlers() {
        return canJoinRoomIdSessionHandlers;
    }

    public void setCanJoinRoomIdSessionHandlers(Set<String> canJoinRoomIdSessionHandlers) {
        this.canJoinRoomIdSessionHandlers = canJoinRoomIdSessionHandlers;
    }

    public UserChannelModule getUserChannelModule() {
        return userChannelModule;
    }

    public void setUserChannelModule(UserChannelModule userChannelModule) {
        this.userChannelModule = userChannelModule;
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

    private final String TAG = RoomSessionManager.class.getSimpleName();
    private String jwtKey;
    @JavaBean
    private Context context;
    @ConfigProperty(name = "jwt.key.suffix")
    private String jwtKeySuffix = "_RoomManagerI_Am_Very_Complex_!#WQ%@#\$!RKJ)94931";
    @Bean
    private WebSocketManager webSocketManager;
    private final Object roomLock = new Object();
    @ConfigProperty(name = "room.session.manager.queue.capacity")
    private int roomSessionManagerQueueCapacity = 10000;
    @ConfigProperty(name = "room.session.manager.core.size")
    private int roomSessionManagerCoreSize = 5;
    @ConfigProperty(name = "room.session.manager.maximum.pool.size")
    private int roomSessionManagerMaximumPoolSize = 100;
    @ConfigProperty(name = "room.session.manager.keep.alive.seconds")
    private int roomSessionManagerKeepAliveSeconds = 120;
    @ConfigProperty(name = "joined.room.jwt.expired.seconds")
    private int jwtExpiredSeconds = 10;
    private final String KEY_USER = "user";
    private JoinRoomAssignmentHandler joinRoomAssignmentHandler;
    private CreateRoomAssignmentHandler createRoomAssignmentHandler;
    private static final int STATE_NONE = 1;
    private static final int STATE_SCAN_ROOM_SESSION = 10;
    private static final int STATE_STARTED = 20;
    private static final int STATE_PAUSED = 30;
    private static final int STATE_TERMINATED = 120;
    private Class<? extends RoomSessionHandler> roomSessionHandlerClass;
    /**
     * roomType->房间session处理器
     */
    private Map<String, Class<? extends RoomSessionHandler>> roomTypeToRoomSessionHandlerClassMap = Maps.newConcurrentMap();
    private StateMachine<Integer, RoomSessionManager> stateMachine;
    private ThreadPoolExecutor threadPoolExecutor;
    @ConfigProperty(name = "max.rooms")
    private int maxRooms = 5000;
    private ConcurrentHashMap<String, Set<String>> groupRoomIdsMap = new ConcurrentHashMap<String, Set<String>>();
    private ConcurrentHashMap<String, RoomSessionHandler> roomIdSessionHandlerMap = new ConcurrentHashMap<String, RoomSessionHandler>();
    private ConcurrentHashMap<String, UserSession> userIdUserSessionMap = new ConcurrentHashMap<String, UserSession>();
    private volatile int roomCounter = 0;
    @ConfigProperty(name = "support.join.at.runtime")
    private boolean supportJoinAtRuntime = false;
    private Set<String> canJoinRoomIdSessionHandlers;
    private ConcurrentHashMap<String, SingleThreadQueueEx> roomIdSingleThreadMap = new ConcurrentHashMap<String, SingleThreadQueueEx>();
    @Bean
    private UserChannelModule userChannelModule;
    private Closure handlerStartedState = new Closure(this, this) {
        public Object doCall(Object roomSessionManager, Object stateMachine) {
            return getWebSocketManager().invokeMethod("start", new Object[0]);
        }

    };
    private Closure handleScanRoomSessionsState = new Closure(this, this) {
        public Object doCall(Object roomSessionManager, Object stateMachine) {
            Collection<Class<?>> classes = getContext().invokeMethod("getClasses", new Object[0]);
            if (classes != null) {
                for (Class clazz : classes) {
                    RoomSession annotation = (RoomSession) clazz.getAnnotation(RoomSession.class);
                    if (annotation != null) {
                        if (!RoomSessionHandler.class.isAssignableFrom(clazz).asBoolean()) {
                            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "RoomSession annotation is found on class " + String.valueOf(clazz) + ", but not implemented RoomSessionHandler which is a must. Ignore this class..."});
                            continue;
                        }

                        if (!ReflectionUtil.invokeMethod("canBeInitiated", new Object[]{clazz}).asBoolean()) {
                            LoggerEx.invokeMethod("error", new Object[]{getTAG(), "RoomSession annotation is found on class " + String.valueOf(clazz) + ", but not be initialized with empty parameter which is a must. Ignore this class..."});
                            continue;
                        }

                        Class<? extends RoomSessionHandler> roomSessionHandlerClass = (Class<? extends RoomSessionHandler>) clazz;
                        roomTypeToRoomSessionHandlerClassMap.put(annotation.roomType(), roomSessionHandlerClass);
                        LoggerEx.invokeMethod("info", new Object[]{getTAG(), "Found RoomSessionHandler Class " + String.valueOf(roomSessionHandlerClass) + ", can only support one handler class, scanning will be stopped"});
                    }

                }

            }

            if (MapUtils.invokeMethod("isEmpty", new Object[]{roomTypeToRoomSessionHandlerClassMap}).asBoolean()) {
                return RoomSessionManager.this.getStateMachine().invokeMethod("gotoState", new Object[]{RoomSessionManager.getSTATE_TERMINATED(), "No RoomSessionHandler found"});
            } else {
                return RoomSessionManager.this.getStateMachine().invokeMethod("gotoState", new Object[]{RoomSessionManager.getSTATE_STARTED(), "Scanned RoomSessionHandler " + String.class.invokeMethod("valueOf", new Object[]{MessageUtils.invokeMethod("toJSONString", new Object[]{roomTypeToRoomSessionHandlerClassMap})})});
            }

        }

    };

    public interface ConfigureRoomSessionHandlerBeforeAdd extends GroovyObjectSupport {
        public abstract void configure(RoomSessionHandler handler);
    }
}
