package oceanus.sdk.network.channels.rooms;

import java.util.List;

public interface JoinRoomAssignmentHandler extends GroovyObjectSupport {
    public abstract MatchingResultContainer assignJoinRoom(String group, List<MatchingItem> matchingItemList);
}
