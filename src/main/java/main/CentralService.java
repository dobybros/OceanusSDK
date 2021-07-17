package main;

import java.util.List;

public interface CentralService {
    public String hello();
    List<PlayerItem> getPlayerItems(PlayerItem playerItem, List<PlayerItem> pis);
}
