package oceanus.sdk.network.channels.websocket.impl;

@CompileStatic
@Bean
public class WebSocketProperties extends GroovyObjectSupport {
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getReadIdleTime() {
        return readIdleTime;
    }

    public void setReadIdleTime(int readIdleTime) {
        this.readIdleTime = readIdleTime;
    }

    public int getWriteIdleTime() {
        return writeIdleTime;
    }

    public void setWriteIdleTime(int writeIdleTime) {
        this.writeIdleTime = writeIdleTime;
    }

    public int getAllIdleTime() {
        return allIdleTime;
    }

    public void setAllIdleTime(int allIdleTime) {
        this.allIdleTime = allIdleTime;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public int getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(int publicPort) {
        this.publicPort = publicPort;
    }

    /**
     * 对外端口
     */
    @ConfigProperty(name = "ws.public.port")
    private int publicPort = 443;
    /**
     * 监听端口
     */
    @ConfigProperty(name = "ws.port")
    private int port = 8000;
    /**
     * 读取空闲时间，单位：分钟
     */
    @ConfigProperty(name = "ws.read.idle.minutes")
    private int readIdleTime = 5;
    /**
     * 写入空闲时间，单位：分钟
     */
    @ConfigProperty(name = "ws.write.idle.minutes")
    private int writeIdleTime = 5;
    /**
     * 读和写空闲时间，单位：分钟
     */
    @ConfigProperty(name = "ws.all.idle.minutes")
    private int allIdleTime = 5;
    /**
     * 连接队列长度
     */
    @ConfigProperty(name = "ws.backlog")
    private int backlog = 1024;
    /**
     * 是否启用SSL
     */
    private boolean ssl = false;
}
