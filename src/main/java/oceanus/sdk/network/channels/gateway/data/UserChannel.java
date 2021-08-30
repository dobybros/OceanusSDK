package oceanus.sdk.network.channels.gateway.data;

@ToString(ignoreNulls = true)
public class UserChannel extends GroovyObjectSupport {
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAuthorisedExpression() {
        return authorisedExpression;
    }

    public void setAuthorisedExpression(String authorisedExpression) {
        this.authorisedExpression = authorisedExpression;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public Integer getTerminal() {
        return terminal;
    }

    public void setTerminal(Integer terminal) {
        this.terminal = terminal;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    private String userId;
    /**
     * 可以授权访问的服务+类名+方法名的正则表达式
     */
    private String authorisedExpression;
    private String deviceToken;
    /**
     * 平台设备{@link gamesharedcore.servicestubs.gatewaymanager.service.GatewayManagerService#TERMINAL_IOS}
     */
    private Integer terminal;
    private Long createTime;
    private Long updateTime;
    private String ip;
}
