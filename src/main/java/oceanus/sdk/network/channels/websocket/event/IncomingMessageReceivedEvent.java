package oceanus.sdk.network.channels.websocket.event;

public class IncomingMessageReceivedEvent extends NettyEvent {
    public IncomingMessage getIncomingMessage() {
        return incomingMessage;
    }

    public void setIncomingMessage(IncomingMessage incomingMessage) {
        this.incomingMessage = incomingMessage;
    }

    private IncomingMessage incomingMessage;
}
