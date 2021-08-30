package oceanus.sdk.network.channels.websocket.event;

public class IncomingDataReceivedEvent extends NettyEvent {
    public IncomingData getIncomingData() {
        return incomingData;
    }

    public void setIncomingData(IncomingData incomingData) {
        this.incomingData = incomingData;
    }

    private IncomingData incomingData;
}
