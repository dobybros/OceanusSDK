package oceanus.sdk.network.channels.rooms;

@ToString
public class RoomAction extends GroovyObjectSupport {
    public RoomSessionHandler getHandler() {
        return handler;
    }

    public void setHandler(RoomSessionHandler handler) {
        this.handler = handler;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public UserSession getUserSession() {
        return userSession;
    }

    public void setUserSession(UserSession userSession) {
        this.userSession = userSession;
    }

    public IncomingData getIncomingData() {
        return incomingData;
    }

    public void setIncomingData(IncomingData incomingData) {
        this.incomingData = incomingData;
    }

    public Closure<Void> getClosure() {
        return closure;
    }

    public void setClosure(Closure<Void> closure) {
        this.closure = closure;
    }

    public Object[] getClosureArgs() {
        return closureArgs;
    }

    public void setClosureArgs(Object[] closureArgs) {
        this.closureArgs = closureArgs;
    }

    private RoomSessionHandler handler;
    private String roomId;
    public static final int ACTION_ROOM_CREATED = 10;
    public static final int ACTION_VIEWER_WILL_JOIN = 15;
    public static final int ACTION_USER_WILL_JOIN = 20;
    public static final int ACTION_USER_JOINED = 25;
    public static final int ACTION_VIEWER_JOINED = 28;
    public static final int ACTION_USER_LEFT = 30;
    public static final int ACTION_VIEWER_LEFT = 32;
    public static final int ACTION_ROOM_DESTROYED = 40;
    public static final int ACTION_ROOM_MESSAGE = 100;
    public static final int ACTION_ROOM_CLOSURE = 1000;
    private int action;
    private UserSession userSession;
    private IncomingData incomingData;
    private Closure<Void> closure;
    private Object[] closureArgs;
}
