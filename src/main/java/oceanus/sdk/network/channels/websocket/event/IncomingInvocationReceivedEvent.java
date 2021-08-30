package oceanus.sdk.network.channels.websocket.event;

public class IncomingInvocationReceivedEvent extends NettyEvent {
    public IncomingInvocation getIncomingInvocation() {
        return incomingInvocation;
    }

    public void setIncomingInvocation(IncomingInvocation incomingInvocation) {
        this.incomingInvocation = incomingInvocation;
    }

    private IncomingInvocation incomingInvocation;
}
