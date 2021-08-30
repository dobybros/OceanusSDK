package oceanus.sdk.network.channels.data;

public abstract class Data extends BinaryCodec {
    public Data(byte type) {
        this.type = type;
    }

    public String getId() {
        return null;
    }

    /**
     * @param type the type to set
     */
    public void setType(byte type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    public byte getType() {
        return type;
    }

    public static int getCODE_SUCCESS() {
        return CODE_SUCCESS;
    }

    public static int getCODE_FAILED() {
        return CODE_FAILED;
    }

    private static final int CODE_SUCCESS = 1;
    private static final int CODE_FAILED = 0;
    private byte type;
}
