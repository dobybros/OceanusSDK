package oceanus.sdk.network.channels.websocket.scheduled;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 房间服务器中负责一些不太重要的定时任务。 所以线程不会用太多。 小心使用
 */
@CompileStatic
public class ScheduledExecutorHolder extends GroovyObjectSupport {
    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        ScheduledExecutorHolder.scheduledExecutorService = scheduledExecutorService;
    }

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4, new ThreadFactoryBuilder().setNameFormat("ScheduledExecutorHolder-%d").build());
}
