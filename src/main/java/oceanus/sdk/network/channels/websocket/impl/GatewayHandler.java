package oceanus.sdk.network.channels.websocket.impl;

import com.google.common.eventbus.EventBus;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.unix.Errors;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import oceanus.apis.CoreException;
import oceanus.sdk.logger.LoggerEx;
import oceanus.sdk.network.channels.websocket.event.*;
import oceanus.sdk.network.channels.websocket.utils.NetUtils;

import java.util.LinkedHashMap;

public class GatewayHandler extends AbstractWebSocketServerHandler {
    public GatewayHandler(boolean ssl) {
        super(ssl);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        LoggerEx.info(TAG, "channelActive $ctx")
//        ctx.channel().attr(AttributeKey.valueOf(""))
//        sessionManager.create(ctx.channel())
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(1);
        map.put("ctx", ctx);
        eventBus.post((Object) new ChannelActiveEvent(map));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        LoggerEx.info(TAG, "channelInactive $ctx")
//        sessionManager.remove(ctx.channel())
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(1);
        map.put("ctx", ctx);
        eventBus.post((Object) new ChannelInActiveEvent(map));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        LoggerEx.info(TAG, "userEventTriggered $ctx $evt")
        if (evt instanceof IdleStateEvent) {
//            logger.debug("channel {} idle {}", ctx.channel(), ((IdleStateEvent) evt).state().name())
//            ctx.close()
        }

    }

    public boolean sendResult(ChannelHandlerContext ctx, Result result) {
        return ((boolean) (NetUtils.invokeMethod("writeAndFlush", new Object[]{ctx.invokeMethod("channel", new Object[0]), result})));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LoggerEx.invokeMethod("info", new Object[]{TAG, "exceptionCaught " + String.valueOf(ctx) + " " + String.valueOf(cause)});

        Channel channel = ctx.invokeMethod("channel", new Object[0]);
        if (channel != null && channel.invokeMethod("isActive", new Object[0])) {
            String forId = null;
            Integer code = null;
            if (cause instanceof WSCoreException) {
                WSCoreException wsCoreException = (WSCoreException) cause;
                forId = ((String) (wsCoreException.forId));
                code = ((Integer) (wsCoreException.code));
            } else if (cause instanceof CoreException) {
                CoreException coreException = (CoreException) cause;
                code = ((Integer) (coreException.code));
            } else {
                code = ((Integer) (Errors.ERROR_UNKNOWN));
            }


            try {
                LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>(3);
                map.put("forId", forId);
                map.put("code", code);
                map.put("time", System.currentTimeMillis());
                sendResult(ctx, new Result(map));
            } catch (Throwable ignored) {
            } finally {
                try {
                    channel.invokeMethod("close", new Object[0]);
                } catch (Throwable ignored) {
                }

            }

        }

    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, BinaryWebSocketFrame webSocketFrame) {
//        LoggerEx.info(TAG, "messageReceived $ctx $webSocketFrame")
        Byte[] body;
        byte type = (byte) 0;
        ByteBuf byteBuf = webSocketFrame.invokeMethod("content", new Object[0]);
        try {
            type = ((byte) (byteBuf.invokeMethod("readByte", new Object[0])));
            checkType(type, ctx);
            int readableBytes = byteBuf.invokeMethod("readableBytes", new Object[0]);
            if (readableBytes > 32768)
                throw new IllegalArgumentException("Received bytes is bigger than 32768, ignore...");
            body = new Byte[readableBytes];
            byteBuf.invokeMethod("readBytes", new Object[]{body});
        } finally {
            byteBuf.invokeMethod("release", new Object[0]);
        }


        if (body != null) {
            switch (type) {
                case Identity.TYPE:
                    LinkedHashMap<String, Identity> map = new LinkedHashMap<String, Identity>(2);
                    map.put("identity", new Identity(body, Data.ENCODE_PB));
                    map.put("ctx", ctx);
                    eventBus.post((Object) new IdentityReceivedEvent(map));
                    break;
                case IncomingData.TYPE:
                    LinkedHashMap<String, IncomingData> map1 = new LinkedHashMap<String, IncomingData>(2);
                    map1.put("incomingData", new IncomingData(body, Data.ENCODE_PB));
                    map1.put("ctx", ctx);
                    eventBus.post((Object) new IncomingDataReceivedEvent(map1));
                    break;
                case Ping.TYPE:
                    LinkedHashMap<String, Ping> map2 = new LinkedHashMap<String, Ping>(2);
                    map2.put("ping", new Ping());
                    map2.put("ctx", ctx);
                    eventBus.post((Object) new PingReceivedEvent(map2));
                    break;
                case IncomingMessage.TYPE:
                    LinkedHashMap<String, IncomingMessage> map3 = new LinkedHashMap<String, IncomingMessage>(2);
                    map3.put("incomingMessage", new IncomingMessage(body, Data.ENCODE_PB));
                    map3.put("ctx", ctx);
                    eventBus.post((Object) new IncomingMessageReceivedEvent(map3));
                    break;
                case IncomingInvocation.TYPE:
                    LinkedHashMap<String, IncomingInvocation> map4 = new LinkedHashMap<String, IncomingInvocation>(2);
                    map4.put("incomingInvocation", new IncomingInvocation(body, Data.ENCODE_PB));
                    map4.put("ctx", ctx);
                    eventBus.post((Object) new IncomingInvocationReceivedEvent(map4));
                    break;
                default:
                    LoggerEx.invokeMethod("error", new Object[]{TAG, "Unexpected type received " + String.valueOf(type) + ", length " + String.valueOf(body.length) + ". Ignored..."});
                    break;
            }
        }

    }

    private static void checkType(byte type, final ChannelHandlerContext ctx) {
        switch (type) {
            case Identity.TYPE:
            case IncomingData.TYPE:
            case Ping.TYPE:
            case IncomingInvocation.TYPE:
            case IncomingMessage.TYPE:
                break;
            default:
                throw new IllegalArgumentException("Illegal type " + String.valueOf(type) + " received from ctx " + String.valueOf(ctx));
        }
    }

    private static final String TAG = GatewayHandler.class.getSimpleName();
    private EventBus eventBus = EventBusHolder.eventBus;
}
