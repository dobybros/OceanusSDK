package oceanus.sdk.network.channels.gateway;

@ToString
public class UserAction extends GroovyObjectSupport {
    public GatewaySessionHandler getHandler() {
        return handler;
    }

    public void setHandler(GatewaySessionHandler handler) {
        this.handler = handler;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public UserChannel getUserChannel() {
        return userChannel;
    }

    public void setUserChannel(UserChannel userChannel) {
        this.userChannel = userChannel;
    }

    public IncomingData getIncomingData() {
        return incomingData;
    }

    public void setIncomingData(IncomingData incomingData) {
        this.incomingData = incomingData;
    }

    public IncomingMessage getIncomingMessage() {
        return incomingMessage;
    }

    public void setIncomingMessage(IncomingMessage incomingMessage) {
        this.incomingMessage = incomingMessage;
    }

    public IncomingInvocation getIncomingInvocation() {
        return incomingInvocation;
    }

    public void setIncomingInvocation(IncomingInvocation incomingInvocation) {
        this.incomingInvocation = incomingInvocation;
    }

    public IncomingRequest getIncomingRequest() {
        return incomingRequest;
    }

    public void setIncomingRequest(IncomingRequest incomingRequest) {
        this.incomingRequest = incomingRequest;
    }

    public OutgoingMessage getOutgoingMessage() {
        return outgoingMessage;
    }

    public void setOutgoingMessage(OutgoingMessage outgoingMessage) {
        this.outgoingMessage = outgoingMessage;
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

    private GatewaySessionHandler handler;
    private String userId;
    public static final int ACTION_SESSION_CREATED = 10;
    public static final int ACTION_USER_CONNECTED = 20;
    public static final int ACTION_USER_DISCONNECTED = 30;
    public static final int ACTION_SESSION_DESTROYED = 40;
    public static final int ACTION_USER_DATA = 105;
    public static final int ACTION_USER_MESSAGE = 100;
    public static final int ACTION_USER_INVOCATION = 110;
    public static final int ACTION_USER_REQUEST = 115;
    public static final int ACTION_USER_OUTGOING_MESSAGE = 120;
    public static final int ACTION_USER_CLOSURE = 1000;
    private int action;
    private UserChannel userChannel;
    private IncomingData incomingData;
    private IncomingMessage incomingMessage;
    private IncomingInvocation incomingInvocation;
    private IncomingRequest incomingRequest;
    private OutgoingMessage outgoingMessage;
    private Closure<Void> closure;
    private Object[] closureArgs;
}
