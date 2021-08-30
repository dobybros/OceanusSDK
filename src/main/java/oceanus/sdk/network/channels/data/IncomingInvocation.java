package oceanus.sdk.network.channels.data;

@CompileStatic
@ToString
public class IncomingInvocation extends Data {
    public IncomingInvocation() {
        super(TYPE);
    }

    public IncomingInvocation(Byte[] data, Byte encode) {
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
                            IncomingInvocation request = MessagePB.IncomingInvocation.invokeMethod("parseFrom", new Object[]{bytes});
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingInvocation.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"id"})}).asBoolean())
                                id = ((String) (request.invokeMethod("getId", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingInvocation.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"service"})}).asBoolean())
                                service = ((String) (request.invokeMethod("getService", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingInvocation.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"contentEncode"})}).asBoolean())
                                contentEncode = ((Integer) (request.invokeMethod("getContentEncode", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingInvocation.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"class"})}).asBoolean())
                                className = ((String) (request.invokeMethod("getClass_", new Object[0])));
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingInvocation.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"method"})}).asBoolean())
                                methodName = ((String) (request.invokeMethod("getMethod", new Object[0])));
//                            ByteString contentString = request.getContent()
//                            if(contentString != null) {
//                                content = contentString.toByteArray()
//                            }
                            if (request.invokeMethod("hasField", new Object[]{MessagePB.IncomingInvocation.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"argsStr"})}).asBoolean())
                                args = ((String) (request.invokeMethod("getArgsStr", new Object[0])));
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
                Builder builder = MessagePB.IncomingInvocation.invokeMethod("newBuilder", new Object[0]);
                if (service != null) builder.invokeMethod("setService", new Object[]{getService()});
                if (contentEncode != null) builder.invokeMethod("setContentEncode", new Object[]{getContentEncode()});
                if (id != null) builder.invokeMethod("setId", new Object[]{getId()});
                if (className != null) builder.invokeMethod("setClass_", new Object[]{getClassName()});
                if (methodName != null) builder.invokeMethod("setMethod", new Object[]{getMethodName()});
                if (args != null) builder.invokeMethod("setArgsStr", new Object[]{getArgs()});
                IncomingInvocation incomingInvocationRequest = builder.invokeMethod("build", new Object[0]);
                Byte[] bytes = incomingInvocationRequest.invokeMethod("toByteArray", new Object[0]);
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

    public Integer getContentEncode() {
        return contentEncode;
    }

    public void setContentEncode(Integer contentEncode) {
        this.contentEncode = contentEncode;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public static byte getTYPE() {
        return TYPE;
    }

    private static final byte TYPE = 50;
    private String id;
    private String service;
    private String className;
    private String methodName;
    private String args;
    private Integer contentEncode;
}
