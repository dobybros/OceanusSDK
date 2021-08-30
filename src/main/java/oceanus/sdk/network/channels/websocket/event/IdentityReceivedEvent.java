package oceanus.sdk.network.channels.websocket.event;

public class IdentityReceivedEvent extends NettyEvent {
    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    private Identity identity;
}
