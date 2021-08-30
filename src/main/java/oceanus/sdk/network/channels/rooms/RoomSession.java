package oceanus.sdk.network.channels.rooms;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface RoomSession {
    /**
     * 房间类型
     */
    public abstract String roomType();
}
