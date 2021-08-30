package oceanus.sdk.network.channels.rooms;

import java.util.List;

public interface CreateRoomAssignmentHandler extends GroovyObjectSupport {
    public abstract MatchingResultContainer assignCreateRoom(String group, List<MatchingItemRoom> matchingItemRoomList);

    /**
     * 获取房间类型
     *
     * @param group
     * @return
     */
    public abstract String getRoomType(String group);
}
