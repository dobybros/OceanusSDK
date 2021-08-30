package oceanus.sdk.network.channels.rooms;

import java.util.Map;

/**
 * 发送给房间内所有用户的拦截器
 * <p>
 * 返回true消息才能下发到用户
 */
public interface SendDataToUsersFilter extends GroovyObjectSupport {
    public abstract Map filter(String userId, String contentType);
}
