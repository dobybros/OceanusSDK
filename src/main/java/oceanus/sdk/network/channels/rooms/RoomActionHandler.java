package oceanus.sdk.network.channels.rooms;

import com.alibaba.fastjson.JSON;

import java.util.LinkedHashMap;
import java.util.Map;

@CompileStatic
public class RoomActionHandler extends GroovyObjectSupport implements Handler<RoomAction> {
    public RoomActionHandler() {

    }

    @Override
    public void error(RoomAction roomAction, final Throwable throwable) {
        LoggerEx.invokeMethod("error", new Object[]{getTAG(), "error occurred " + String.valueOf(throwable) + " message " + throwable.getMessage() + " roomAction " + String.valueOf(roomAction)});
    }

    @Override
    public void execute(final RoomAction roomAction) throws CoreException {
        switch (roomAction.getAction()) {
            case RoomAction.ACTION_ROOM_CREATED:
                try {
                    roomAction.getHandler().onRoomCreated();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onRoomCreated failed, " + throwable.getMessage() + ", id " + roomAction.getRoomId() + " group " + roomAction.getHandler().getGroup() + " maxUsers " + String.valueOf(roomAction.getHandler().getMaxUsers()) + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case RoomAction.ACTION_ROOM_DESTROYED:
                try {
                    roomAction.getHandler().onRoomDestroyed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onRoomDestroyed failed, " + throwable.getMessage() + ", id " + roomAction.getRoomId() + " group " + roomAction.getHandler().getGroup() + " maxUsers " + String.valueOf(roomAction.getHandler().getMaxUsers()) + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case RoomAction.ACTION_VIEWER_WILL_JOIN:
                try {
                    roomAction.getHandler().onViewerWillJoin(roomAction.getUserSession());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    final UserSession session1 = (roomAction == null ? null : roomAction.getUserSession());
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onUserWillJoin " + String.class.invokeMethod("valueOf", new Object[]{(session1 == null ? null : session1.userId)}) + " failed, " + throwable.getMessage() + ", id " + roomAction.getRoomId() + " group " + roomAction.getHandler().getGroup() + " maxUsers " + String.valueOf(roomAction.getHandler().getMaxUsers()) + " userSession " + String.valueOf(roomAction.getUserSession()) + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case RoomAction.ACTION_USER_WILL_JOIN:
                try {
                    roomAction.getHandler().onUserWillJoin(roomAction.getUserSession());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    final UserSession session2 = (roomAction == null ? null : roomAction.getUserSession());
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onUserWillJoin " + String.class.invokeMethod("valueOf", new Object[]{(session2 == null ? null : session2.userId)}) + " failed, " + throwable.getMessage() + ", id " + roomAction.getRoomId() + " group " + roomAction.getHandler().getGroup() + " maxUsers " + String.valueOf(roomAction.getHandler().getMaxUsers()) + " userSession " + String.valueOf(roomAction.getUserSession()) + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case RoomAction.ACTION_USER_JOINED:
                try {
                    roomAction.getHandler().onUserJoined(roomAction.getUserSession());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    final UserSession session3 = (roomAction == null ? null : roomAction.getUserSession());
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onUserJoined " + String.class.invokeMethod("valueOf", new Object[]{(session3 == null ? null : session3.userId)}) + " failed, " + throwable.getMessage() + ", id " + roomAction.getRoomId() + " group " + roomAction.getHandler().getGroup() + " maxUsers " + String.valueOf(roomAction.getHandler().getMaxUsers()) + " userSession " + String.valueOf(roomAction.getUserSession()) + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case RoomAction.ACTION_VIEWER_JOINED:
                try {
                    roomAction.getHandler().onViewerJoined(roomAction.getUserSession());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    final UserSession session4 = (roomAction == null ? null : roomAction.getUserSession());
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onViewerJoined " + String.class.invokeMethod("valueOf", new Object[]{(session4 == null ? null : session4.userId)}) + " failed, " + throwable.getMessage() + ", id " + roomAction.getRoomId() + " group " + roomAction.getHandler().getGroup() + " maxUsers " + String.valueOf(roomAction.getHandler().getMaxUsers()) + " userSession " + String.valueOf(roomAction.getUserSession()) + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case RoomAction.ACTION_USER_LEFT:
                try {
                    roomAction.getHandler().onUserLeft(roomAction.getUserSession());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    final UserSession session5 = (roomAction == null ? null : roomAction.getUserSession());
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onUserLeft " + String.class.invokeMethod("valueOf", new Object[]{(session5 == null ? null : session5.userId)}) + " failed, " + throwable.getMessage() + ", id " + roomAction.getRoomId() + " group " + roomAction.getHandler().getGroup() + " maxUsers " + String.valueOf(roomAction.getHandler().getMaxUsers()) + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case RoomAction.ACTION_VIEWER_LEFT:
                try {
                    roomAction.getHandler().onViewerLeft(roomAction.getUserSession());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    final UserSession session6 = (roomAction == null ? null : roomAction.getUserSession());
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onViewerLeft " + String.class.invokeMethod("valueOf", new Object[]{(session6 == null ? null : session6.userId)}) + " failed, " + throwable.getMessage() + ", id " + roomAction.getRoomId() + " group " + roomAction.getHandler().getGroup() + " maxUsers " + String.valueOf(roomAction.getHandler().getMaxUsers()) + " on thread " + String.valueOf(Thread.currentThread())});
                }

                break;
            case RoomAction.ACTION_ROOM_CLOSURE:
                if (roomAction.getClosure().asBoolean()) {
                    try {
                        roomAction.getClosure().invokeMethod("call", new Object[]{roomAction.getClosureArgs()});
                    } catch (Throwable throwable1) {
                        throwable1.printStackTrace();
                        LoggerEx.invokeMethod("error", new Object[]{getTAG(), "roomClosure call failed, closure " + String.valueOf(roomAction.getClosure()) + " args " + String.valueOf(roomAction.getClosureArgs()) + " error " + throwable1.getMessage()});
                    }

//                    roomAction.closure.getMetaClass().invokeMethod(this, "doCall", roomAction.closureArgs)
                }

                break;
            case RoomAction.ACTION_ROOM_MESSAGE:
                try {
                    roomAction.getHandler().updateDisbandRoomTime();
                    Map jsonObject = JSON.class.invokeMethod("parseObject", new Object[]{roomAction.getIncomingData().content});
                    final Reference<ResultData> resultData = new groovy.lang.Reference<ResultData>(roomAction.getHandler().onDataReceived(roomAction.getIncomingData().contentType, jsonObject, roomAction.getUserSession(), roomAction.getIncomingData().id));
                    if (resultData.get() == null) {
                        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(1);
                        map.put("code", ResultData.CODE_SUCCESS);
                        resultData.set(new ResultData(map));
                    }

                    if (resultData.get().code == null) resultData.get().code = ResultData.CODE_SUCCESS;
                    if (resultData.get().forId == null) resultData.get().forId = roomAction.getIncomingData().id;
                    boolean bool = userChannelModule.invokeMethod("sendResultData", new Object[]{roomAction.getUserSession().userId, resultData.get()});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send result not successfully to userId " + String.class.invokeMethod("valueOf", new Object[]{roomAction.getUserSession().userId}) + ", code " + String.class.invokeMethod("valueOf", new Object[]{resultData.get().code}) + " dataLength " + String.class.invokeMethod("valueOf", new Object[]{resultData.get().data})});
                    }

                } catch (CoreException coreException) {
                    LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>(3);
                    map.put("forId", roomAction.incomingData.id);
                    map.put("time", System.currentTimeMillis());
                    map.put("contentEncode", roomAction.incomingData.contentEncode);
                    Result errorResult = new Result(map);
                    errorResult.code = coreException.code;
                    boolean bool = userChannelModule.invokeMethod("sendData", new Object[]{roomAction.getUserSession().userId, errorResult});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send errorResult not successfully to userId " + String.class.invokeMethod("valueOf", new Object[]{roomAction.getUserSession().userId}) + ", code " + String.class.invokeMethod("valueOf", new Object[]{errorResult.code}) + " coreException " + String.class.invokeMethod("valueOf", new Object[]{coreException.message})});
                    }

                } catch (Throwable throwable) {
                    LoggerEx.invokeMethod("error", new Object[]{getTAG(), "onDataReceived contentType ERROR_UNKNOWN:" + String.valueOf(throwable)});
                    LinkedHashMap<String, Long> map1 = new LinkedHashMap<String, Long>(3);
                    map1.put("forId", roomAction.incomingData.id);
                    map1.put("time", System.currentTimeMillis());
                    map1.put("contentEncode", roomAction.incomingData.contentEncode);
                    Result errorResult = new Result(map1);
                    errorResult.code = Errors.ERROR_UNKNOWN;
                    boolean bool = userChannelModule.invokeMethod("sendData", new Object[]{roomAction.getUserSession().userId, errorResult});
                    if (!bool.asBoolean()) {
                        LoggerEx.invokeMethod("warn", new Object[]{getTAG(), "send errorResult not successfully to userId " + String.class.invokeMethod("valueOf", new Object[]{roomAction.getUserSession().userId}) + ", code " + String.class.invokeMethod("valueOf", new Object[]{errorResult.code}) + " throwable " + throwable.getMessage()});
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

    public UserChannelModule getUserChannelModule() {
        return userChannelModule;
    }

    public void setUserChannelModule(UserChannelModule userChannelModule) {
        this.userChannelModule = userChannelModule;
    }

    private final String TAG = RoomActionHandler.class.getSimpleName();
    @Bean
    private UserChannelModule userChannelModule;
}
