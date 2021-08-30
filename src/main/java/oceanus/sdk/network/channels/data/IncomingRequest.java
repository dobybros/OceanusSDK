package oceanus.sdk.network.channels.data;

@CompileStatic
@ToString
public class IncomingRequest extends Data {
    public IncomingRequest() {
        super(TYPE);
    }

    public IncomingRequest(Byte[] data, Byte encode) {
        this();

        setData(data);
        setEncode(encode);
        resurrect();
    }

    @Override
    public void resurrect() throws CoreException {
        Byte[] bytes = getData();
        Byte encode = getEncode();
        if (bytes != null) {
            if (encode != null) {
                switch (encode) {
                    case BinaryCodec.getENCODE_PB():
                        try {
                            IncomingRequest request = MessagePB.IncomingRequest.invokeMethod("parseFrom", new Object[]{bytes});
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingRequest.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"id"})}).asBoolean())
                                id = ((String) (request.invokeMethod("getId", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingRequest.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"service"})}).asBoolean())
                                service = ((String) (request.invokeMethod("getService", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingRequest.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"bodyEncode"})}).asBoolean())
                                bodyEncode = ((Integer) (request.invokeMethod("getBodyEncode", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingRequest.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"method"})}).asBoolean())
                                method = ((String) (request.invokeMethod("getMethod", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingRequest.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"bodyStr"})}).asBoolean())
                                bodyStr = ((String) (request.invokeMethod("getBodyStr", new Object[0])));
//                            ByteString contentString = request.getContent()
//                            if(contentString != null) {
//                                content = contentString.toByteArray()
//                            }
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingRequest.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"service"})}).asBoolean())
                                service = ((String) (request.invokeMethod("getService", new Object[0])));
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
//throw new CoreException(CoreErrorCodes.ERROR_RPC_ENCODER_NULL, "Encoder is null for persistent")
        switch (encode) {
            case BinaryCodec.getENCODE_PB():
                Builder builder = MessagePB.IncomingRequest.invokeMethod("newBuilder", new Object[0]);
                if (service != null) builder.invokeMethod("setService", new Object[]{getService()});
                if (bodyEncode != null) builder.invokeMethod("setBodyEncode", new Object[]{getBodyEncode()});
                if (id != null) builder.invokeMethod("setId", new Object[]{getId()});
                if (method != null) builder.invokeMethod("setMethod", new Object[]{getMethod()});
                if (uri != null) builder.invokeMethod("setUri", new Object[]{getUri()});
                if (bodyStr != null) builder.invokeMethod("setBodyStr", new Object[]{getBodyStr()});
                IncomingRequest incomingRequest = builder.invokeMethod("build", new Object[0]);
                Byte[] bytes = incomingRequest.invokeMethod("toByteArray", new Object[0]);
                setData(bytes);
                setEncode(BinaryCodec.getENCODE_PB());
                break;
            default:
                throw new CoreException(Errors.ERROR_RPC_ENCODER_NOT_FOUND, "Encoder type doesn't be found for persistent");
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBodyStr() {
        return bodyStr;
    }

    public void setBodyStr(String bodyStr) {
        this.bodyStr = bodyStr;
    }

    public Integer getBodyEncode() {
        return bodyEncode;
    }

    public void setBodyEncode(Integer bodyEncode) {
        this.bodyEncode = bodyEncode;
    }

    public static byte getTYPE() {
        return TYPE;
    }

    private static final byte TYPE = 60;
    private String id;
    private String service;
    private String uri;
    private String method;
    private String bodyStr;
    private Integer bodyEncode;
}
