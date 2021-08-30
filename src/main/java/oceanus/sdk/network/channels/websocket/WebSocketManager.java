package oceanus.sdk.network.channels.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import oceanus.sdk.logger.LoggerEx;
import oceanus.sdk.network.channels.websocket.impl.GatewayHandlerInitializer;
import oceanus.sdk.network.channels.websocket.impl.WebSocketProperties;

import java.beans.JavaBean;
import java.util.LinkedHashMap;

public class WebSocketManager {
    public LinkedHashMap<String, Object> memory() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>(3);
        map.put("webSocketProperties", webSocketProperties);
        map.put("bossGroup", bossGroup);
        map.put("workerGroup", workerGroup);
        return map;
    }

    public void start() {
        if (!started) {
            started = true;
            try {
                GatewayHandlerInitializer initializer = new GatewayHandlerInitializer(this.webSocketProperties);
                context.invokeMethod("injectBean", new Object[]{initializer});
                ServerBootstrap b = new ServerBootstrap();
                b.invokeMethod("group", new Object[]{bossGroup, workerGroup}).invokeMethod("channel", new Object[]{NioServerSocketChannel.class}).invokeMethod("handler", new Object[]{new LoggingHandler(LogLevel.DEBUG)}).invokeMethod("option", new Object[]{ChannelOption.SO_BACKLOG, webSocketProperties.invokeMethod("getBacklog", new Object[0])}).invokeMethod("childOption", new Object[]{ChannelOption.TCP_NODELAY, true}).invokeMethod("childHandler", new Object[]{initializer});
                b.invokeMethod("bind", new Object[]{webSocketProperties.invokeMethod("getPort", new Object[0])}).invokeMethod("sync", new Object[0]);
                LoggerEx.invokeMethod("info", new Object[]{TAG, "Websocket server started at port " + webSocketProperties.invokeMethod("getPort", new Object[0])});
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                startFailed = throwable;
            }

        }

    }

    public void stop() {
        if (started) {
            bossGroup.invokeMethod("shutdownGracefully", new Object[0]);
            workerGroup.invokeMethod("shutdownGracefully", new Object[0]);
        }

    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean getStarted() {
        return started;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public Throwable getStartFailed() {
        return startFailed;
    }

    public void setStartFailed(Throwable startFailed) {
        this.startFailed = startFailed;
    }

    private static final String TAG = WebSocketManager.class.getSimpleName();
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    @Bean
    private WebSocketProperties webSocketProperties;
    @JavaBean
    private Context context;
    private boolean started = false;
    private Throwable startFailed;
}
