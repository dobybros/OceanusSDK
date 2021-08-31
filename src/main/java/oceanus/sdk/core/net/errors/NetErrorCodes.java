package oceanus.sdk.core.net.errors;

import oceanus.sdk.core.common.ErrorCodes;

public interface NetErrorCodes {
    int ERROR_TIMEOUT = ErrorCodes.NET_START_FROM;
    int ERROR_PACKET_SEND_FAILED = ErrorCodes.NET_START_FROM - 1;
    int ERROR_NODE_UNREGISTERED = ErrorCodes.NET_START_FROM - 2;
}
