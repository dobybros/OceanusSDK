package oceanus.sdk.network.channels.data;

public class OutgoingData extends Data {
    public OutgoingData() {
        super(TYPE);
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
                            OutgoingData request = MessagePB.OutgoingData.invokeMethod("parseFrom", new Object[]{bytes});
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.OutgoingData.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"id"})}).asBoolean())
                                id = ((String) (request.invokeMethod("getId", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.OutgoingData.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"time"})}).asBoolean())
                                time = ((Long) (request.invokeMethod("getTime", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.OutgoingData.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"contentType"})}).asBoolean())
                                contentType = ((String) (request.invokeMethod("getContentType", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.OutgoingData.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"contentEncode"})}).asBoolean())
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
                        throw new CoreException(Errors.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for resurrect");
                }
            }

        }

    }

    @Override
    public void persistent() throws CoreException {
        Byte encode = getEncode();
        if (encode == null)
            encode = BinaryCodec.getENCODE_PB();//throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent")
        switch (encode) {
            case BinaryCodec.getENCODE_PB():
                Builder builder = MessagePB.OutgoingData.invokeMethod("newBuilder", new Object[0]);
                if (content != null) builder.invokeMethod("setContentStr", new Object[]{getContent()});
                if (contentType != null) builder.invokeMethod("setContentType", new Object[]{getContentType()});
                if (time != null) builder.invokeMethod("setTime", new Object[]{getTime()});
                if (contentEncode != null) builder.invokeMethod("setContentEncode", new Object[]{getContentEncode()});
                if (id != null) builder.invokeMethod("setId", new Object[]{getId()});
                OutgoingData incomingMessageRequest = builder.invokeMethod("build", new Object[0]);
                Byte[] bytes = incomingMessageRequest.invokeMethod("toByteArray", new Object[0]);
                setData(bytes);
                setEncode(BinaryCodec.getENCODE_PB());
                break;
            default:
                throw new CoreException(Errors.ERROR_RPC_ENCODER_NOTFOUND, "Encoder type doesn't be found for persistent");
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getContentEncode() {
        return contentEncode;
    }

    public void setContentEncode(Integer contentEncode) {
        this.contentEncode = contentEncode;
    }

    public static byte getTYPE() {
        return TYPE;
    }

    private static final byte TYPE = 20;
    private String id;
    private Long time;
    private String contentType;
    private Integer contentEncode;
    private String content;
}
