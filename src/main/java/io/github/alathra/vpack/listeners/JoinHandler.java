package io.github.alathra.vpack.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import io.github.alathra.vpack.pack.resource.PackService;

public class JoinHandler {
    private final PackService packService;

    public JoinHandler(PackService packService) {
        this.packService = packService;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onPlayerConnect(ServerPostConnectEvent event) {
        if (event.getPreviousServer() == null)
            packService.sendPackToPlayer(event.getPlayer());
    }
}
