package oceanus.sdk.utils;

import oceanus.sdk.logger.LoggerEx;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class OceanusProperties {
    private static OceanusProperties instance;
    private static Properties properties;
    public static void setProperties(Properties properties) {
        OceanusProperties.properties = properties;
    }

    public static OceanusProperties getInstance() {
        if (instance == null) {
            synchronized (OceanusProperties.class) {
                if(instance == null) {
                    instance = new OceanusProperties();
                    if(properties == null) {
                        URL configResource = OceanusProperties.class.getClassLoader().getResource("oceanus.properties");
                        properties = new Properties();
                        if (configResource != null) {
                            try(InputStream is = configResource.openStream()) {
                                properties.load(is);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                                LoggerEx.error("OceanusProperties", "Load properties failed, " + throwable.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return instance;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public String getDiscoveryHost() {
        return properties.getProperty("discovery.host");
    }

    public int getRpcPort() {
        return Integer.parseInt(properties.getProperty("rpc.port", "-1"));
    }
    public int getRpcDataPort() {
        return Integer.parseInt(properties.getProperty("rpc.data.port", "-1"));
    }

    public String getScanPackage() {
        return properties.getProperty("scan.package");
    }

    public String getService() {
        return properties.getProperty("service.name");
    }

    public String getEthPrefix() {
        return properties.getProperty("server.eth.prefix");
    }

    public String getIpPrefix() {
        return properties.getProperty("server.ip.prefix");
    }

    public Integer getVersion() {
        return Integer.parseInt(properties.getProperty("version.current"));
    }

}
