package oceanus.sdk.network.channels.rooms.robot;

import java.util.LinkedHashMap;
import java.util.Map;

@CompileStatic
public abstract class RobotListener<T extends RoomSessionHandler> extends GroovyObjectSupport {
    public LinkedHashMap<String, Object> memory() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(1);
        map.put("userSession", userSession.invokeMethod("toString", new Object[0]));
        return map;
    }

    public RobotListener() {
    }

    public void init() {
    }

    public RobotListener(UserSession userSession, T roomSessionHandler) {
        this.roomSessionHandler = roomSessionHandler;
        this.userSession = userSession;
    }

    public void sendData(String contentType, Map mapObject, String id) {
        this.roomSessionHandler.invokeMethod("onDataReceived", new Object[]{contentType, mapObject, getUserSession(), id});
    }

    public abstract void receivedData(String contentType, Map mapObject);

    public T getRoomSessionHandler() {
        return roomSessionHandler;
    }

    public void setRoomSessionHandler(T roomSessionHandler) {
        this.roomSessionHandler = roomSessionHandler;
    }

    public UserSession getUserSession() {
        return userSession;
    }

    public void setUserSession(UserSession userSession) {
        this.userSession = userSession;
    }

    private T roomSessionHandler;
    private UserSession userSession;
}
