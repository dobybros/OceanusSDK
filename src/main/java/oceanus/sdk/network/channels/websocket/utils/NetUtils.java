package oceanus.sdk.network.channels.websocket.utils;

public class NetUtils extends GroovyObjectSupport {
    public static boolean writeAndFlush(Channel channel, Data data) {
        if (channel != null && channel.invokeMethod("isActive", new Object[0])) {
            if (data.data == null) {
                data.invokeMethod("persistent", new Object[0]);
            }

            ByteBuf byteBuf = Unpooled.invokeMethod("directBuffer", new Object[]{1 + data.data.length});
            try {
                // byteBuf.writeBytes(msgId.getBytes())
                // byteBuf.writeShort(Integer.parseInt(msgId))
                byteBuf.invokeMethod("writeByte", new Object[]{data.type});
                if (data.data.length > 0) byteBuf.invokeMethod("writeBytes", new Object[]{data.data});
                channel.invokeMethod("writeAndFlush", new Object[]{new BinaryWebSocketFrame(byteBuf)});
                return true;
            } catch (Throwable t) {
                t.printStackTrace();
                byteBuf.invokeMethod("release", new Object[0]);
            }

        }

        return false;
    }

    public static boolean writeAndFlush(Channel channel, byte type) {
        if (channel != null && channel.invokeMethod("isActive", new Object[0])) {
            ByteBuf byteBuf = Unpooled.invokeMethod("directBuffer", new Object[]{1});
            try {
                // byteBuf.writeBytes(msgId.getBytes())
                // byteBuf.writeShort(Integer.parseInt(msgId))
                byteBuf.invokeMethod("writeByte", new Object[]{type});
                channel.invokeMethod("writeAndFlush", new Object[]{new BinaryWebSocketFrame(byteBuf)});
                return true;
            } catch (Throwable t) {
                t.printStackTrace();
                byteBuf.invokeMethod("release", new Object[0]);
            }

        }

        return false;
    }

}
