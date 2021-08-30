package oceanus.sdk.network.channels.websocket.impl;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;

public abstract class AbstractWebSocketServerHandler extends ChannelInboundHandlerAdapter {
    public AbstractWebSocketServerHandler() {
        this.init(DEFAULT_SOCKET_PATH, false);
    }

    public AbstractWebSocketServerHandler(boolean ssl) {
        this.init(DEFAULT_SOCKET_PATH, ssl);
    }

    public AbstractWebSocketServerHandler(String webSocketPath, boolean ssl) {
        this.init(webSocketPath, ssl);
    }

    private void init(String webSocketPath, boolean ssl) {
        this.webSocketPath = webSocketPath;
        this.ssl = ssl;
    }

    /**
     * 将BinaryWebSocketFrame传递给子类处理
     *
     * @param ctx            ChannelHandlerContext
     * @param webSocketFrame BinaryWebSocketFrame
     */
    protected abstract void messageReceived(ChannelHandlerContext ctx, BinaryWebSocketFrame webSocketFrame);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            this.handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            this.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        } else {
//            LoggerEx.debug(TAG, "channel ${ctx.channel().id().toString()} unknown msg type: ${msg.getClass()}")
            ctx.close();
        }

    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame) {
        if (webSocketFrame instanceof BinaryWebSocketFrame) {
            this.messageReceived(ctx, (BinaryWebSocketFrame) webSocketFrame);
        } else if (webSocketFrame instanceof CloseWebSocketFrame) {
            this.handShaker.close(ctx.channel(), ((CloseWebSocketFrame) webSocketFrame).retain());
        } else {
//            LoggerEx.debug(TAG, "server not support WebSocketFrame type: ${webSocketFrame.getClass()}")
        }

    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        try {
            if (request.decoderResult().isSuccess()) {
//                LoggerEx.debug(TAG, "channel ${ctx.channel().id().toString()} decoder result failed")
                sendHttpResponse(ctx, request, new DefaultFullHttpResponse(io.netty.handler.codec.http.HttpVersion.HTTP_1_1, io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR));
                return;

            }


            if (request.method() != HttpMethod.GET) {
//                LoggerEx.debug(TAG, "channel ${ctx.channel().id().toString()} not support http method ${request.method().name()}")
                sendHttpResponse(ctx, request, new DefaultFullHttpResponse(io.netty.handler.codec.http.HttpVersion.HTTP_1_1, io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN));
                return;

            }


            if (!request.uri().startsWith(this.webSocketPath)) {
//                LoggerEx.debug(TAG, "channel ${ctx.channel().id().toString()} unknown uri ${request.uri()}")
                sendHttpResponse(ctx, request, new DefaultFullHttpResponse(io.netty.handler.codec.http.HttpVersion.HTTP_1_1, io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND));
                return;

            }


            // WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(
            // getWebSocketLocation(request), null, false)
            WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, false, 1024 * 1024);
            this.handShaker = factory.newHandshaker(request);
            if (this.handShaker == null) {
//                LoggerEx.debug(TAG, "channel ${ctx.channel().id().toString()} unsupported webSocket version")
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                this.handShaker.handshake(ctx.channel(), request);
            }


            this.afterHandShaker(ctx, request);
        } catch (Exception e) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(io.netty.handler.codec.http.HttpVersion.HTTP_1_1, io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR));
        } finally {
            request.release();
        }

    }

    protected void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response) {
        ChannelFuture f = ctx.channel().writeAndFlush(response);
//        if (!io.netty.handler.codec.http.HttpUtil.invokeMethod("isKeepAlive", new Object[]{request}) || !response.invokeMethod("status", new Object[0]).invokeMethod("code", new Object[0]).equals(HTTP_OK)) {
        if (!HttpUtil.isKeepAlive(request) || response.status().code() != HTTP_OK) {
//            f.invokeMethod("addListener", new Object[]{ChannelFutureListener.CLOSE});
            f.addListener(ChannelFutureListener.CLOSE);
        }

    }

    private String getWebSocketLocation(HttpRequest request) {
        if (this.ssl) {
            return DEFAULT_WEB_SOCKET_PROTOCOL_WSS + request.headers().get(HttpHeaderNames.HOST) + this.webSocketPath;
        } else {
            return DEFAULT_WEB_SOCKET_PROTOCOL + request.headers().get(HttpHeaderNames.HOST) + this.webSocketPath;
        }

    }

    /**
     * 握手之后的处理逻辑
     *
     * @param ctx     ChannelHandlerContext
     * @param request FullHttpRequest
     */
    protected void afterHandShaker(ChannelHandlerContext ctx, FullHttpRequest request) {
    }

    private static final String TAG = AbstractWebSocketServerHandler.class.getSimpleName();
    /**
     * HTTP OK CODE
     */
    private static final int HTTP_OK = 200;
    /**
     * 默认的webSocket path
     */
    private static final String DEFAULT_SOCKET_PATH = "/";
    /**
     * 默认协议：ws://
     */
    private static final String DEFAULT_WEB_SOCKET_PROTOCOL = "ws://";
    /**
     * 默认协议：wss://
     */
    private static final String DEFAULT_WEB_SOCKET_PROTOCOL_WSS = "wss://";
    /**
     * handShaker
     */
    private WebSocketServerHandshaker handShaker;
    /**
     * WebSocket Path
     */
    private String webSocketPath;
    /**
     * ssl
     */
    private boolean ssl;
}
