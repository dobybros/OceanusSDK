package oceanus.sdk.network.channels.websocket.impl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.tools.ant.util.ResourceUtils;

import javax.net.ssl.KeyManagerFactory;
import java.beans.JavaBean;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

public class GatewayHandlerInitializer extends ChannelInitializer<SocketChannel> {
    public GatewayHandlerInitializer(WebSocketProperties properties) throws Exception {
        this.nettyProperties = properties;
        if (this.nettyProperties.isSsl()) {
            this.createSSL();
        }

    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if (this.nettyProperties.isSsl() && this.sslContext != null) {
            pipeline.addLast(this.sslContext.newHandler(socketChannel.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new IdleStateHandler(this.nettyProperties.getReadIdleTime(), this.nettyProperties.getWriteIdleTime(), this.nettyProperties.getAllIdleTime(), TimeUnit.MINUTES));
        GatewayHandler gatewayHandler = new GatewayHandler(this.nettyProperties.isSsl());
        context.invokeMethod("injectBean", new Object[]{gatewayHandler});
        pipeline.invokeMethod("addLast", new Object[]{gatewayHandler});
    }

    private void createSSL() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = new FileInputStream(ResourceUtils.invokeMethod("getFile", new Object[]{"classpath:gateserver.jks"}));
        ks.load(ksInputStream, "123456".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "123456".toCharArray());
        this.sslContext = ((SslContext) (SslContextBuilder.invokeMethod("forServer", new Object[]{kmf}).invokeMethod("clientAuth", new Object[]{ClientAuth.NONE}).invokeMethod("build", new Object[0])));
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private WebSocketProperties nettyProperties;
    private SslContext sslContext;
    @JavaBean
    private Context context;
}
