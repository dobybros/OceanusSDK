package oceanus.sdk.core.discovery.errors;


import oceanus.sdk.core.common.ErrorCodes;

public interface DiscoveryErrorCodes {
    int ERROR_DUPLICATED_SERVER_CRC = ErrorCodes.DISCOVERY_START_FROM;
    int ERROR_TIMEOUT = ErrorCodes.DISCOVERY_START_FROM - 1;
    int ERROR_UNKNOWN_NODE = ErrorCodes.DISCOVERY_START_FROM - 2;
    int ERROR_NODE_TERMINATION = ErrorCodes.DISCOVERY_START_FROM - 3;
    int ERROR_IO = ErrorCodes.DISCOVERY_START_FROM - 4;
    int ERROR_FIND_SERVICE_NOT_FOUND = ErrorCodes.DISCOVERY_START_FROM - 5;
    int ERROR_SERVICE_KEY_NULL = ErrorCodes.DISCOVERY_START_FROM - 6;
    int ERROR_REQUEST_GENERATE_RESPONSE_FAILED = ErrorCodes.DISCOVERY_START_FROM - 7;
    int ERROR_FIND_NODE_NOT_FOUND = ErrorCodes.DISCOVERY_START_FROM - 8;
    int ERROR_SERVER_CRC_ID_NULL = ErrorCodes.DISCOVERY_START_FROM - 9;
    int ERROR_NO_RESULT = ErrorCodes.DISCOVERY_START_FROM - 10;
    int ERROR_PACKET_IS_SENDING = ErrorCodes.DISCOVERY_START_FROM - 11;
    int ERROR_DISCOVERY_HOST_NOT_AVAILABLE = ErrorCodes.DISCOVERY_START_FROM - 12;
    int ERROR_OWNER_PROJECT_MISSING = ErrorCodes.DISCOVERY_START_FROM - 13;
    int ERROR_REGISTER_SERVICE_FAILED = ErrorCodes.DISCOVERY_START_FROM - 14;
    int ERROR_REGISTER_NODE_FAILED = ErrorCodes.DISCOVERY_START_FROM - 15;
    int ERROR_VERSION_SMALLER = ErrorCodes.DISCOVERY_START_FROM - 16;
}
