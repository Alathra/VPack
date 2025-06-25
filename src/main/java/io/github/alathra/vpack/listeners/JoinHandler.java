package io.github.alathra.vpack.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import io.github.alathra.vpack.pack.resource.PackInfo;

public class JoinHandler {
    private final PackInfo packInfo;

    public JoinHandler(PackInfo packInfo) {
        this.packInfo = packInfo;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onPlayerConnect(ServerPostConnectEvent event) {
        if (event.getPreviousServer() == null)
            packInfo.distribute().sendToPlayer(event.getPlayer());
    }
}
