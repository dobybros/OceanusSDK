package oceanus.sdk.network.channels.rooms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.time.DateUtils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@CompileStatic
public abstract class RoomSessionHandler extends GroovyObjectSupport {
    public LinkedHashMap memory() {
        LinkedHashMap map = new LinkedHashMap() {
        };
//        Set<String> userIds = userIdRobotMap.keySet()
//        def robots = []
//        for(String robotUserId : userIds) {
//            robots.add(userIdRobotMap.get(robotUserId).memory())
//        }
//        map["userIdRobotMap"] = robots
        MemoryUtils.invokeMethod("fillMemoryIntoMap", new Object[]{map, getUserIdRobotMap(), "userIdRobotMap"});
        MemoryUtils.invokeMethod("fillToStringIntoMap", new Object[]{map, getUserIdUserSessionMap(), "userIdUserSessionMap"});
        MemoryUtils.invokeMethod("fillToStringIntoMap", new Object[]{map, getUserIdUserSessionViewerMap(), "userIdUserSessionViewerMap"});
        return map;
    }

    public RobotListener addRobotToRoom(final UserSession robotUserSession, Class<? extends RobotListener> robotClass) {
//        if(robotUserSession.balance == null || robotUserSession.userId == null)
        if (robotUserSession.userId == null)
            throw new CoreException(Errors.ILLEGAL_PARAMETERS, "Illegal parameters balance " + String.class.invokeMethod("valueOf", new Object[]{robotUserSession.balance}) + " userId " + String.class.invokeMethod("valueOf", new Object[]{robotUserSession.userId}) + " to add robot into room " + this.getId());

        robotUserSession.roomId = this.id;
        robotUserSession.service = context.serverConfig.service;
        robotUserSession.group = this.group;
        robotUserSession.server = context.serverConfig.server;
        roomSessionManager.addUserToRoom(robotUserSession);

        Constructor<RobotListener> constructor = (Constructor<RobotListener>) robotClass.getConstructor(UserSession.class, this.getClass());
        RobotListener robotListener = constructor.newInstance(robotUserSession, this);
        context.invokeMethod("injectBean", new Object[]{robotListener});
        userIdRobotMap.put(robotUserSession.userId, robotListener);

        roomSessionManager.connectUserOrViewerToRoom(robotUserSession);
        return robotListener;
    }

    public boolean sendResultData(String userId, ResultData resultData) {
        return sendResultData(userId, resultData, null);
    }

    public boolean sendResultData(String userId, ResultData resultData, String forId) {
        Result result = new Result();
        result.invokeMethod("setTime", new Object[]{System.currentTimeMillis()});
        result.invokeMethod("setContentEncode", new Object[]{resultData.dataEncode});
        result.invokeMethod("setContent", new Object[]{resultData.data});
        result.invokeMethod("setForId", new Object[]{forId});
        return ((boolean) (userChannelModule.invokeMethod("sendData", new Object[]{userId, result})));
    }

    public boolean sendData(String userId, String contentType, String data) {
        OutgoingData outgoingData = new OutgoingData();
        outgoingData.invokeMethod("setContentType", new Object[]{contentType});
        outgoingData.invokeMethod("setContent", new Object[]{data});
        outgoingData.invokeMethod("setContentEncode", new Object[]{ResultData.CONTENT_ENCODE_JSON});
        outgoingData.invokeMethod("setTime", new Object[]{System.currentTimeMillis()});
        return ((boolean) (userChannelModule.invokeMethod("sendData", new Object[]{userId, outgoingData})));
    }

    public boolean sendData(String userId, String contentType, SendDataToUsersFilter filter) {
        Object object = filter.call(userId, contentType);
        String data = JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
        return sendData(userId, contentType, data);
    }

    public void sendDataToUsers(String contentType, SendDataToUsersFilter filter) {
        sendDataToUsers(contentType, filter, null);
    }

    public void sendDataToUsers(String contentType, SendDataToUsersFilter filter, Collection<String> exceptUserIds) {
        Collection<String> userIds = new ArrayList<String>(userIdUserSessionMap.keySet());
        for (String userId : userIds) {
            if (exceptUserIds != null && exceptUserIds.contains(userId)) continue;
            Map object = (Map) filter.call(userId, contentType);
            UserSession userSession = (UserSession) userIdUserSessionMap.get(userId);
            if (userSession != null && userSession.userId != null && userIdRobotMap.containsKey(userSession.userId)) {
                RobotListener robotListener = userIdRobotMap.get(userSession.userId);
                try {
                    robotListener.invokeMethod("receivedData", new Object[]{contentType, object});
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LoggerEx.invokeMethod("error", new Object[]{TAG, "Robot receive data " + contentType + " object " + String.valueOf(object) + " failed, " + throwable.getMessage()});
                }

                continue;
            }

            String data = JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
            if (data != null) {
                sendData(userId, contentType, data);
            }

        }

        //TODO 如果viewer数量多了， 应该考虑单独开线程， 不然会影响正在玩的玩家体验
        if (!userIdUserSessionViewerMap.isEmpty().asBoolean()) {
            Collection<String> viewerIds = new ArrayList<String>(userIdUserSessionViewerMap.keySet());
            Object object = filter.call(CURRENT_USER_ID_VIEWER, contentType);
            String data = JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
            if (data != null) {
                for (String viewerId : viewerIds) {
                    if (data != null) {
                        sendData(viewerId, contentType, data);
                    }

                }

            }

        }

    }

    /**
     * 房间创建时的回调方法。
     * 此方法会在房间线程上回调， 任意onXXX的方法会同步执行
     */
    public abstract void onRoomCreated();

    /**
     * 用户加入前回调， 此时用户的通道还没有建立， 此时是业务系统在房间里占位时触发。 如果用户在一定时间内没有建立通道进入房间， 该用户会被自动删除。
     * 此方法会在房间线程上回调， 任意onXXX的方法会同步执行
     *
     * @param userSession
     */
    public abstract void onUserWillJoin(UserSession userSession);

    public abstract void onViewerWillJoin(UserSession userSession);

    /**
     * 用户通道建立时回调此方法， 表时用户已经真正的进入
     * 此方法会在房间线程上回调， 任意onXXX的方法会同步执行
     *
     * @param userSession
     */
    public abstract void onUserJoined(UserSession userSession);

    public abstract void onViewerJoined(UserSession userSession);

    /**
     * 房间收到消息的回调， 一般为房间里用户的动作消息
     * 此方法会在房间线程上回调， 任意onXXX的方法会同步执行
     *
     * @param contentType
     * @param data
     * @param userSession
     * @param clientId    从客户端发送过来的Client ID， 在ResultData里通过forId返回到客户端， 这样客户端就能知道这个Result是会那个id的
     * @return
     */
    public abstract ResultData onDataReceived(String contentType, Map mapObject, UserSession userSession, String clientId);

    /**
     * 用户通道断连后的回调
     * 此方法会在房间线程上回调， 任意onXXX的方法会同步执行
     *
     * @param userSession
     */
    public abstract void onUserLeft(UserSession userSession);

    public abstract void onViewerLeft(UserSession userSession);

    /**
     * 房间销毁后的回调， 此时该房间的所有用户通道都会被关闭
     * 此方法会在房间线程上回调， 任意onXXX的方法会同步执行
     */
    public abstract void onRoomDestroyed();

    public long needWait() {
        return -1;//表示不支持返回等待进入的时间。 可能是某些房间类型并没有这样等待时间。 例如用户进入德扑房间后距离下一次开局的大致时间
    }

    public int leftSeats() {
        return maxUsers - userIdUserSessionMap.size();
    }

    /**
     * 在用户被删除之前回调此方法， 用于添加reservedUserId达到占位的目的， 不然用户离开后这个位置随时可能会被分配给其他用户。
     * 此方法是一个主流程方法， 不要执行太重的流程， 否则容易导致阻塞。
     *
     * @param userId
     */
    public void userDisconnected(String userId) {
    }

    public void viewerDisconnected(String userId) {
    }

    /**
     * 创建房间时，同步调用初始化方法。不要执行太重流程，否则容易导致阻塞
     */
    public abstract void init();

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getForbiddenNewUserJoin() {
        return forbiddenNewUserJoin;
    }

    public void setForbiddenNewUserJoin(boolean forbiddenNewUserJoin) {
        this.forbiddenNewUserJoin = forbiddenNewUserJoin;
    }

    public RoomSessionManager getRoomSessionManager() {
        return roomSessionManager;
    }

    public void setRoomSessionManager(RoomSessionManager roomSessionManager) {
        this.roomSessionManager = roomSessionManager;
    }

    public UserChannelModule getUserChannelModule() {
        return userChannelModule;
    }

    public void setUserChannelModule(UserChannelModule userChannelModule) {
        this.userChannelModule = userChannelModule;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public MatchingManagerService getMatchingManagerService() {
        return matchingManagerService;
    }

    public void setMatchingManagerService(MatchingManagerService matchingManagerService) {
        this.matchingManagerService = matchingManagerService;
    }

    public Map<String, UserSession> getUserIdUserSessionMap() {
        return userIdUserSessionMap;
    }

    public void setUserIdUserSessionMap(Map<String, UserSession> userIdUserSessionMap) {
        this.userIdUserSessionMap = userIdUserSessionMap;
    }

    public Map<String, UserSession> getUserIdUserSessionViewerMap() {
        return userIdUserSessionViewerMap;
    }

    public void setUserIdUserSessionViewerMap(Map<String, UserSession> userIdUserSessionViewerMap) {
        this.userIdUserSessionViewerMap = userIdUserSessionViewerMap;
    }

    public Map<String, RobotListener> getUserIdRobotMap() {
        return userIdRobotMap;
    }

    public void setUserIdRobotMap(Map<String, RobotListener> userIdRobotMap) {
        this.userIdRobotMap = userIdRobotMap;
    }

    public Set<String> getReservedUserIds() {
        return reservedUserIds;
    }

    public void setReservedUserIds(Set<String> reservedUserIds) {
        this.reservedUserIds = reservedUserIds;
    }

    /**
     * 更新房间超时解散的时间，每次玩家有动作更新
     */
    public void updateDisbandRoomTime() {
        if (roomTimeoutTime > 0) {
            this.disbandRoomTime = System.currentTimeMillis() + this.roomTimeoutTime;
        }

    }

    public JSONObject getInfo() {
        return info;
    }

    public void setInfo(JSONObject info) {
        this.info = info;
    }

    public long getRoomTimeoutTime() {
        return roomTimeoutTime;
    }

    public void setRoomTimeoutTime(long roomTimeoutTime) {
        this.roomTimeoutTime = roomTimeoutTime;
    }

    public Long getDisbandRoomTime() {
        return disbandRoomTime;
    }

    public void setDisbandRoomTime(Long disbandRoomTime) {
        this.disbandRoomTime = disbandRoomTime;
    }

    public static String getCURRENT_USER_ID_VIEWER() {
        return CURRENT_USER_ID_VIEWER;
    }

    private final String TAG = RoomSessionHandler.class.getSimpleName();
    private int maxUsers;
    private String group;
    private String id;
    private boolean forbiddenNewUserJoin;
    private JSONObject info;
    /**
     * 超时解散房间的时间
     * 玩家没有动作一定时间后解散房间
     */
    @ConfigProperty(name = "room.disbandTimeout.time")
    private long roomTimeoutTime = 20 * 60 * DateUtils.MILLIS_PER_SECOND;
    /**
     * 解散房间的时间
     */
    private volatile Long disbandRoomTime;
    @Bean
    private RoomSessionManager roomSessionManager;
    @Bean
    private UserChannelModule userChannelModule;
    @JavaBean
    private Context context;
    @ServiceBean(name = MatchingManagerService.SERVICE)
    private MatchingManagerService matchingManagerService;
    private Map<String, UserSession> userIdUserSessionMap = Collections.synchronizedMap(new LinkedHashMap<String, UserSession>());
    private Map<String, UserSession> userIdUserSessionViewerMap = Collections.synchronizedMap(new LinkedHashMap<String, UserSession>());
    private Map<String, RobotListener> userIdRobotMap = new ConcurrentHashMap<String, RobotListener>();
    private Set<String> reservedUserIds = new CopyOnWriteArraySet<String>();
    private static final String CURRENT_USER_ID_VIEWER = "viewer";
}
