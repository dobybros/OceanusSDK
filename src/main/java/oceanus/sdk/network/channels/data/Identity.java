package oceanus.sdk.network.channels.data;

@CompileStatic
@ToString
public class Identity extends Data {
    public Identity() {
        super(TYPE);
    }

    public Identity(Byte[] data, Byte encode) {
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
                            Identity request = MessagePB.Identity.invokeMethod("parseFrom", new Object[]{bytes});
                            if (MessagePB.Identity.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"id"}) != null)
                                if (request.invokeMethod("hasField", new Object[]{MessagePB.Identity.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"id"})}).asBoolean())
                                    id = ((String) (request.invokeMethod("getId", new Object[0])));
                            if (MessagePB.Identity.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"token"}) != null)
                                if (request.invokeMethod("hasField", new Object[]{MessagePB.Identity.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"token"})}).asBoolean())
                                    token = ((String) (request.invokeMethod("getToken", new Object[0])));
//                            if (MessagePB.Identity.getDescriptor().findFieldByName("name") != null)
//                                if (request.hasField(MessagePB.Identity.getDescriptor().findFieldByName("name")))
//                                    name = request.getName()
//                            if (MessagePB.Identity.getDescriptor().findFieldByName("icon") != null)
//                                if (request.hasField(MessagePB.Identity.getDescriptor().findFieldByName("icon")))
//                                    icon = request.getIcon()
                            if (MessagePB.Identity.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"reserved"}) != null)
                                if (request.invokeMethod("hasField", new Object[]{MessagePB.Identity.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"reserved"})}).asBoolean())
                                    reserved = ((String) (request.invokeMethod("getReserved", new Object[0])));
//                            if (MessagePB.Identity.getDescriptor().findFieldByName("rank") != null)
//                                if (request.hasField(MessagePB.Identity.getDescriptor().findFieldByName("rank")))
//                                    rank = request.getRank()
//                            if (MessagePB.Identity.getDescriptor().findFieldByName("balance") != null)
//                                if (request.hasField(MessagePB.Identity.getDescriptor().findFieldByName("balance")))
//                                    balance = request.getBalance()
                            if (MessagePB.Identity.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"sign"}) != null)
                                if (request.invokeMethod("hasField", new Object[]{MessagePB.Identity.invokeMethod("getDescriptor", new Object[0]).invokeMethod("findFieldByName", new Object[]{"sign"})}).asBoolean())
                                    sign = ((String) (request.invokeMethod("getSign", new Object[0])));
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new CoreException(Errors.ERROR_RPC_ENCODE_PB_PARSE_FAILED, "PB parse data failed, " + e.getMessage());
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
                Builder builder = MessagePB.Identity.invokeMethod("newBuilder", new Object[0]);
                if (id != null) builder.invokeMethod("setId", new Object[]{getId()});
                if (token != null) {
                    builder.invokeMethod("setToken", new Object[]{getToken()});
                }

                if (reserved != null) {
                    builder.invokeMethod("setReserved", new Object[]{getReserved()});
                }

                if (sign != null) {
                    builder.invokeMethod("setSign", new Object[]{getSign()});
                }

                Identity loginRequest = builder.invokeMethod("build", new Object[0]);
                Byte[] bytes = loginRequest.invokeMethod("toByteArray", new Object[0]);
                setData(bytes);
                setEncode(BinaryCodec.getENCODE_PB());
                break;
            default:
                throw new CoreException(Errors.ERROR_RPC_ENCODER_NOT_FOUND, "Encoder type doesn't be found for persistent");
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public static byte getTYPE() {
        return TYPE;
    }

    private static final byte TYPE = 1;
    private String id;
    private String token;
    private String reserved;
    private String sign;
}
