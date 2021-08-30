package oceanus.sdk.network.channels.data;

@CompileStatic
public class Result extends Data {
    public Result() {
        super(TYPE);
        setEncode(BinaryCodec.getENCODE_PB());
    }

    /**
     * @param code the code to set
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * @return the code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public String getForId() {
        return forId;
    }

    public void setForId(String forId) {
        this.forId = forId;
    }

    @Override
    public void resurrect() throws CoreException {
        Byte[] bytes = (Byte[]) getData();
        Byte encode = getEncode();
        if (bytes != null) {
            if (encode != null) {
                switch (encode) {
                    case BinaryCodec.getENCODE_PB():
                        try {
                            Result request = MessagePB.Result.invokeMethod("parseFrom", new Object[]{bytes});
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.Result.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"description"})}).asBoolean())
                                description = ((String) (request.invokeMethod("getDescription", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.Result.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"forId"})}).asBoolean())
                                forId = ((String) (request.invokeMethod("getForId", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.Result.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"code"})}).asBoolean())
                                code = ((Integer) (request.invokeMethod("getCode", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.Result.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"time"})}).asBoolean())
                                time = ((Long) (request.invokeMethod("getTime", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.Result.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"serverId"})}).asBoolean())
                                serverId = ((String) (request.invokeMethod("getServerId", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.Result.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"contentEncode"})}).asBoolean())
                                contentEncode = ((Integer) (request.invokeMethod("getContentEncode", new Object[0])));
//                            ByteString contentString = request.getContent()
//                            if(contentString != null) {
//                                content = contentString.toByteArray()
//                            }
                            content = ((String) (request.invokeMethod("getContentStr", new Object[0])));
                        } catch (InvalidProtocolBufferException e) {
                            e.invokeMethod("printStackTrace", new Object[0]);
                            throw new CoreException(Errors.ERROR_RPC_ENCODE_PB_PARSE_FAILED, "PB parse data failed, " + e.invokeMethod("getMessage", new Object[0]));
                        }

                        break;
                    default:
                        throw new CoreException(Errors.ERROR_RPC_ENCODER_NOT_FOUND, "Encoder type doesn't be found for resurrect");
                }
            }

        }

    }

    @Override
    public void persistent() throws CoreException {
        Byte encode = getEncode();
        if (encode == null) encode = BinaryCodec.getENCODE_PB();
//			throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent")
        switch (encode) {
            case BinaryCodec.getENCODE_PB():
                Builder builder = MessagePB.Result.invokeMethod("newBuilder", new Object[0]);
                if (code != null) builder.invokeMethod("setCode", new Object[]{getCode()});
                if (description != null) builder.invokeMethod("setDescription", new Object[]{getDescription()});
                if (forId != null) builder.invokeMethod("setForId", new Object[]{getForId()});
                if (time != null) builder.invokeMethod("setTime", new Object[]{getTime()});
                if (serverId != null) builder.invokeMethod("setServerId", new Object[]{getServerId()});
                if (contentEncode != null) builder.invokeMethod("setContentEncode", new Object[]{getContentEncode()});
                if (content != null) builder.invokeMethod("setContentStr", new Object[]{getContent()});
                Result resultRequest = builder.invokeMethod("build", new Object[0]);
                Byte[] bytes = resultRequest.invokeMethod("toByteArray", new Object[0]);
                setData(bytes);
                setEncode(BinaryCodec.getENCODE_PB());
                break;
            default:
                throw new CoreException(Errors.ERROR_RPC_ENCODER_NOT_FOUND, "Encoder type doesn't be found for persistent");
        }
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getContentEncode() {
        return contentEncode;
    }

    public void setContentEncode(Integer contentEncode) {
        this.contentEncode = contentEncode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static byte getTYPE() {
        return TYPE;
    }

    private static final byte TYPE = 100;
    private Integer code;
    private String description;
    private String forId;
    private String serverId;
    private Long time;
    private Integer contentEncode;
    private String content;
}
