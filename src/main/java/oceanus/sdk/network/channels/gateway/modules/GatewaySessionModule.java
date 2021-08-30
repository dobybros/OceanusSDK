package oceanus.sdk.network.channels.gateway.modules;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

@RedeployMain
@CompileStatic
@ConditionalOnProperty(propertyName = "oceanus.gateway.enable", havingValue = "true")
public class GatewaySessionModule extends GroovyObjectSupport {
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
        Attribute<UserChannel> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{GatewayChannelModule.getKEY_GATEWAY_USER()})});
        UserChannel userSession = attribute.invokeMethod("get", new Object[0]);
        if (userSession == null) {
            incomingDataReceivedEvent.invokeMethod("closeChannel", new Object[]{incomingData.id, Errors.ERROR_USER_SESSION_NOT_EXIST});
            return;

        }


        //TODO 防重复调用， 调用频率限制
        gatewaySessionManager.invokeMethod("receiveIncomingData", new Object[]{userSession.userId, incomingData});
    }

    @Subscribe
    @AllowConcurrentEvents
    public void receivedIncomingMessage(IncomingMessageReceivedEvent incomingMessageReceivedEvent) {
        IncomingMessage incomingMessage = incomingMessageReceivedEvent.incomingMessage;
        if (incomingMessage == null) {
            incomingMessageReceivedEvent.invokeMethod("closeChannel", new Object[]{incomingMessage.id, Errors.ERROR_ILLEGAL_PARAMETERS});
            return;

        }

        Channel channel = incomingMessageReceivedEvent.ctx.invokeMethod("channel", new Object[0]);
        Attribute<UserChannel> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{GatewayChannelModule.getKEY_GATEWAY_USER()})});
        UserChannel userSession = attribute.invokeMethod("get", new Object[0]);
        if (userSession == null) {
            incomingMessageReceivedEvent.invokeMethod("closeChannel", new Object[]{incomingMessage.id, Errors.ERROR_USER_SESSION_NOT_EXIST});
            return;

        }


        //TODO 防重复调用， 调用频率限制
        gatewaySessionManager.invokeMethod("receiveIncomingMessage", new Object[]{userSession.userId, incomingMessage});
    }

    @Subscribe
    @AllowConcurrentEvents
    public void receivedIncomingInvocation(IncomingInvocationReceivedEvent incomingInvocationReceivedEvent) {
        IncomingInvocation incomingInvocation = incomingInvocationReceivedEvent.incomingInvocation;
        if (incomingInvocation == null) {
            incomingInvocationReceivedEvent.invokeMethod("closeChannel", new Object[]{incomingInvocation.id, Errors.ERROR_ILLEGAL_PARAMETERS});
            return;

        }

        Channel channel = incomingInvocationReceivedEvent.ctx.invokeMethod("channel", new Object[0]);
        Attribute<UserChannel> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{GatewayChannelModule.getKEY_GATEWAY_USER()})});
        UserChannel userSession = attribute.invokeMethod("get", new Object[0]);
        if (userSession == null) {
            incomingInvocationReceivedEvent.invokeMethod("closeChannel", new Object[]{incomingInvocation.id, Errors.ERROR_USER_SESSION_NOT_EXIST});
            return;

        }


        //TODO 防重复调用， 调用频率限制
        gatewaySessionManager.invokeMethod("receiveIncomingInvocation", new Object[]{userSession.userId, incomingInvocation});
    }

    @Subscribe
    @AllowConcurrentEvents
    public void receivedPing(PingReceivedEvent pingReceivedEvent) {
        Channel channel = pingReceivedEvent.ctx.invokeMethod("channel", new Object[0]);
        Attribute<UserChannel> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{GatewayChannelModule.getKEY_GATEWAY_USER()})});
        UserChannel userChannel = attribute.invokeMethod("get", new Object[0]);
        if (userChannel == null) {
            pingReceivedEvent.invokeMethod("closeChannel", new Object[]{null, Errors.ERROR_USER_SESSION_NOT_EXIST});
            return;

        }


        GatewaySessionHandler gatewaySessionHandler = gatewaySessionManager.userIdGatewaySessionHandlerMap.invokeMethod("get", new Object[]{userChannel.userId});
        if (gatewaySessionHandler != null) gatewaySessionHandler.invokeMethod("touch", new Object[0]);
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

    public GatewaySessionManager getGatewaySessionManager() {
        return gatewaySessionManager;
    }

    public void setGatewaySessionManager(GatewaySessionManager gatewaySessionManager) {
        this.gatewaySessionManager = gatewaySessionManager;
    }

    private static final String TAG = GatewaySessionModule.class.getSimpleName();
    @JavaBean
    private Context context;
    @Bean
    private GatewaySessionManager gatewaySessionManager;
}
