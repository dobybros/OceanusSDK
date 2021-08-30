package oceanus.sdk.network.channels.rooms.modules;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@RedeployMain
@CompileStatic
@ConditionalOnProperty(propertyName = "oceanus.room.enable")
public class RoomSessionModule extends GroovyObjectSupport {
    public void main() {
        EventBusHolder.eventBus.invokeMethod("register", new Object[]{this});
    }

    @Subscribe
    @AllowConcurrentEvents
    public void receivedIncomingData(IncomingDataReceivedEvent incomingDataReceivedEvent) {
        IncomingData incomingData = incomingDataReceivedEvent.incomingData;
        if (incomingData == null) {
            incomingDataReceivedEvent.invokeMethod("closeChannel", new Object[]{incomingData.id, Errors.ERROR_ILLEGAL_PARAMETERS});
            return;

        }

        Channel channel = incomingDataReceivedEvent.ctx.invokeMethod("channel", new Object[0]);
        Attribute<UserSession> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{UserChannelModule.getKEY_USER()})});
        UserSession userSession = attribute.invokeMethod("get", new Object[0]);
        if (userSession == null) {
            incomingDataReceivedEvent.invokeMethod("closeChannel", new Object[]{incomingData.id, Errors.ERROR_USER_SESSION_NOT_EXIST});
            return;

        }


        //TODO 防重复调用， 调用频率限制
        roomManager.invokeMethod("receiveIncomingData", new Object[]{userSession, incomingData});
    }

    @Subscribe
    @AllowConcurrentEvents
    public void receivedPing(PingReceivedEvent pingReceivedEvent) {
        Channel channel = pingReceivedEvent.ctx.invokeMethod("channel", new Object[0]);
        Attribute<UserSession> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{UserChannelModule.getKEY_USER()})});
        UserSession userSession = attribute.invokeMethod("get", new Object[0]);
        if (userSession == null) {
            pingReceivedEvent.invokeMethod("closeChannel", new Object[]{null, Errors.ERROR_USER_SESSION_NOT_EXIST});
            return;

        }


        //TODO 调用频率限制
        pingReceivedEvent.invokeMethod("ping", new Object[]{channel});
    }

    public static String getTAG() {
        return TAG;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public RoomSessionManager getRoomManager() {
        return roomManager;
    }

    public void setRoomManager(RoomSessionManager roomManager) {
        this.roomManager = roomManager;
    }

    private static final String TAG = RoomSessionModule.class.getSimpleName();
    @JavaBean
    private Context context;
    @Bean
    private RoomSessionManager roomManager;
}
