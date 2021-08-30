package oceanus.sdk.network.channels.data;

public class ResultData extends GroovyObjectSupport {
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getDataEncode() {
        return dataEncode;
    }

    public void setDataEncode(Integer dataEncode) {
        this.dataEncode = dataEncode;
    }

    public String getForId() {
        return forId;
    }

    public void setForId(String forId) {
        this.forId = forId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static int getCONTENT_ENCODE_JSON() {
        return CONTENT_ENCODE_JSON;
    }

    public static int getCONTENT_ENCODE_JSON_GZIP() {
        return CONTENT_ENCODE_JSON_GZIP;
    }

    private static final int CONTENT_ENCODE_JSON = 1;
    private static final int CONTENT_ENCODE_JSON_GZIP = 2;
    public static final int CODE_SUCCESS = 1;
    private String forId;
    private Integer code;
    private String message;
    private String data;
    private Integer dataEncode = ResultData.getCONTENT_ENCODE_JSON();
}
