package oceanus.sdk.network.channels.websocket.event;

public class PingReceivedEvent extends NettyEvent {
    public Ping getPing() {
        return ping;
    }

    public void setPing(Ping ping) {
        this.ping = ping;
    }

    private Ping ping;
}
