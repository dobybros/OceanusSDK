package oceanus.sdk.network.channels.data;

@CompileStatic
@ToString
public class IncomingData extends Data {
    public IncomingData() {
        super(TYPE);
    }

    public IncomingData(Byte[] data, Byte encode) {
        this();

        setData(data);
        setEncode(encode);
        resurrect();
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
                            IncomingData request = MessagePB.IncomingData.invokeMethod("parseFrom", new Object[]{bytes});
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingData.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"id"})}).asBoolean())
                                id = ((String) (request.invokeMethod("getId", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingData.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"contentType"})}).asBoolean())
                                contentType = ((String) (request.invokeMethod("getContentType", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingData.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"contentEncode"})}).asBoolean())
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
        if (encode == null)
            encode = BinaryCodec.getENCODE_PB();//throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent")
        switch (encode) {
            case BinaryCodec.getENCODE_PB():
                Builder builder = MessagePB.IncomingData.invokeMethod("newBuilder", new Object[0]);
                if (content != null) builder.invokeMethod("setContentStr", new Object[]{getContent()});
                if (contentType != null) builder.invokeMethod("setContentType", new Object[]{getContentType()});
                if (contentEncode != null) builder.invokeMethod("setContentEncode", new Object[]{getContentEncode()});
                if (id != null) builder.invokeMethod("setId", new Object[]{getId()});
                IncomingData incomingDataRequest = builder.invokeMethod("build", new Object[0]);
                Byte[] bytes = incomingDataRequest.invokeMethod("toByteArray", new Object[0]);
                setData(bytes);
                setEncode(BinaryCodec.getENCODE_PB());
                break;
            default:
                throw new CoreException(Errors.ERROR_RPC_ENCODER_NOT_FOUND, "Encoder type doesn't be found for persistent");
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

    private static final byte TYPE = 10;
    private String id;
    private String contentType;
    private Integer contentEncode;
    private String content;
}
