package oceanus.sdk.network.channels.data;

import oceanus.apis.CoreException;

public abstract class BinaryCodec extends GroovyObjectSupport {
    public short getEncodeVersion() {
        return encodeVersion;
    }

    public void setEncodeVersion(short encodeVersion) {
        this.encodeVersion = encodeVersion;
    }

    public abstract void resurrect() throws CoreException;

    public abstract void persistent() throws CoreException;

    public Byte[] getData() {
        return data;
    }

    public void setData(Byte[] data) {
        this.data = data;
    }

    public Byte getEncode() {
        return encode;
    }

    public void setEncode(Byte encode) {
        this.encode = encode;
    }

    public static byte getENCODE_PB() {
        return ENCODE_PB;
    }

    public static byte getENCODE_JSON() {
        return ENCODE_JSON;
    }

    public static byte getENCODE_JAVABINARY() {
        return ENCODE_JAVABINARY;
    }

    private static final byte ENCODE_PB = 1;
    private static final byte ENCODE_JSON = 10;
    private static final byte ENCODE_JAVABINARY = 20;
    private Byte encode;
    private Byte[] data;
    /**
     * content的版本
     */
    private short encodeVersion;
}
