package oceanus.sdk.network.channels.rooms.data;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.concurrent.ScheduledFuture;

@ToString(ignoreNulls = true, excludes = {"joinFuture", "cacheExpiredFuture"})
public class UserSession extends GroovyObjectSupport {
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ScheduledFuture getJoinFuture() {
        return joinFuture;
    }

    public void setJoinFuture(ScheduledFuture joinFuture) {
        this.joinFuture = joinFuture;
    }

    public ScheduledFuture getCacheExpiredFuture() {
        return cacheExpiredFuture;
    }

    public void setCacheExpiredFuture(ScheduledFuture cacheExpiredFuture) {
        this.cacheExpiredFuture = cacheExpiredFuture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    @JSONField(name = "s")
    private String server;
    @JSONField(name = "id")
    private String userId;
    @JSONField(name = "sv")
    private String service;
    @JSONField(name = "g")
    private String group;
    @JSONField(name = "rid")
    private String roomId;
    @JSONField(serialize = false, deserialize = false)
    private long time;
    @JSONField(serialize = false, deserialize = false)
    private Object info;
    @JSONField(serialize = false, deserialize = false)
    private Long balance;
    @JSONField(serialize = false, deserialize = false)
    private String rank;
    @JSONField(serialize = false, deserialize = false)
    private String name;
    @JSONField(serialize = false, deserialize = false)
    private String icon;
    @JSONField(serialize = false, deserialize = false)
    private String reserved;
    @JSONField(serialize = false, deserialize = false)
    private ScheduledFuture joinFuture;
    @JSONField(serialize = false, deserialize = false)
    private ScheduledFuture cacheExpiredFuture;
}
