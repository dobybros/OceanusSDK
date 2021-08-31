package oceanus.sdk.core.discovery.impl.server;

import oceanus.sdk.core.discovery.DiscoveryInfo;
import oceanus.sdk.core.discovery.DiscoveryManager;
import oceanus.sdk.core.discovery.data.discovery.GetAllServicesRequest;
import oceanus.sdk.core.discovery.data.discovery.GetAllServicesResponse;
import oceanus.sdk.core.net.ContentPacketListener;
import oceanus.sdk.core.net.adapters.data.ContentPacket;
import oceanus.sdk.core.net.data.ResponseTransport;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class GetAllServicesHandler extends ServerHandler implements ContentPacketListener<GetAllServicesRequest> {
    public GetAllServicesHandler(DiscoveryManager discoveryManager) {
        super(discoveryManager);
    }

    @Override
    public ResponseTransport contentPacketReceived(ContentPacket<GetAllServicesRequest> contentPacket, long serverIdCRC, InetSocketAddress address) {
        GetAllServicesRequest request = contentPacket.getContent();
        ResponseTransport responseTransport = null;
        if(request != null) {
            DiscoveryInfo discoveryInfo = discoveryManager.getDiscoveryInfo();
            GetAllServicesResponse response = new GetAllServicesResponse();
            response.setServices(new ArrayList<>(discoveryInfo.getServiceNodesMap().keySet()));
            responseTransport = response;
        }
        return responseTransport;
    }
}
