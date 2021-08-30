package oceanus.sdk.network.channels.websocket.event;

import oceanus.sdk.network.channels.data.Ping;
import oceanus.sdk.network.channels.websocket.utils.NetUtils;

import java.util.LinkedHashMap;

public class NettyEvent extends GroovyObjectSupport {
    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public boolean ping(Channel channel) {
        return ((boolean) (NetUtils.invokeMethod("writeAndFlush", new Object[]{channel, Ping.TYPE})));
    }

    public boolean sendResult(Result result) {
        Channel channel = ctx.invokeMethod("channel", new Object[0]);
        return this.sendResult(channel, result);
    }

    public boolean sendResult(Channel channel, Result result) {
        return ((boolean) (NetUtils.invokeMethod("writeAndFlush", new Object[]{channel, result})));
    }

    public void closeChannel(Channel channel, String forId, int code) {
        if (channel != null && channel.invokeMethod("isActive", new Object[0])) {
            try {
                LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>(3);
                map.put("forId", forId);
                map.put("code", code);
                map.put("time", System.currentTimeMillis());
                sendResult(channel, new Result(map));
            } catch (Throwable ignored) {
            } finally {
                channel.invokeMethod("close", new Object[0]);
            }

        }

    }

    public void closeChannel(String forId, int code) {
        Channel channel = ctx.invokeMethod("channel", new Object[0]);
        closeChannel(channel, forId, code);
    }

    private ChannelHandlerContext ctx;
}
