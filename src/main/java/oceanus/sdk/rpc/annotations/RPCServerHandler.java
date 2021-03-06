package oceanus.sdk.rpc.annotations;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface RPCServerHandler {
	public String rpcType();
}
