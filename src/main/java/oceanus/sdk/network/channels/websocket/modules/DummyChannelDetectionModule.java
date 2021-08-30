package oceanus.sdk.network.channels.websocket.modules;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@CompileStatic
@RedeployMain
public class DummyChannelDetectionModule extends GroovyObjectSupport {
    public void main() {
        EventBusHolder.eventBus.invokeMethod("register", new Object[]{this});
    }

    @Subscribe
    @AllowConcurrentEvents
    public void channelCreated(final ChannelActiveEvent channelActiveEvent) {
        Channel channel = (Channel) (channelActiveEvent == null ? null : channelActiveEvent.ctx).invokeMethod("channel", new Object[0]);
        if (channel != null) {
            ScheduledFuture future = ScheduledExecutorHolder.scheduledExecutorService.invokeMethod("schedule", new Object[]{new Closure(this, this) {
                public Object doCall(Object it) {
                    Channel channel1 = (Channel) channelActiveEvent.ctx.invokeMethod("channel", new Object[0]);
                    if (channel1 != null && channel1.invokeMethod("isActive", new Object[0])) {
                        try {
                            return channel1.invokeMethod("close", new Object[0]);
                        } catch (Throwable ignored) {
                        }

                    }

                }

                public Object doCall() {
                    return doCall(null);
                }

            }, getExpireSeconds(), TimeUnit.SECONDS});
            Attribute<ScheduledFuture> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{getATTR_EXPIRE_TIMER()})});
            attribute.invokeMethod("set", new Object[]{future});
        }

    }

    @Subscribe
    @AllowConcurrentEvents
    public void dataReceived(IdentityReceivedEvent dataReceivedEvent) {
        Channel channel = (Channel) (dataReceivedEvent == null ? null : dataReceivedEvent.ctx).invokeMethod("channel", new Object[0]);
        if (channel != null) {
            Attribute<ScheduledFuture> attribute = channel.invokeMethod("attr", new Object[]{AttributeKey.invokeMethod("valueOf", new Object[]{getATTR_EXPIRE_TIMER()})});
            ScheduledFuture future = attribute.invokeMethod("getAndSet", new Object[]{null});
            if (future != null) {
                future.cancel(false);
            }

        }

    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public final String getATTR_EXPIRE_TIMER() {
        return ATTR_EXPIRE_TIMER;
    }

    private final String ATTR_EXPIRE_TIMER = "DUMMY_ATTR_EXPIRE_TIMER";
    @ConfigProperty(name = "dummy.channel.expire.seconds")
    private long expireSeconds = 8;
}
