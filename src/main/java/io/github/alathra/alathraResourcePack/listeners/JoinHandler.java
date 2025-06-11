package io.github.alathra.alathraResourcePack.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import io.github.alathra.alathraResourcePack.config.Settings;
import io.github.alathra.alathraResourcePack.core.PackInfo;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;

import java.net.URI;

public class JoinHandler {
    private final PackInfo packInfo;
    private final ResourcePackRequest request;

    public JoinHandler(PackInfo packInfo) {
        this.packInfo = packInfo;

        ResourcePackInfo PACK_INFO = ResourcePackInfo.resourcePackInfo()
                .uri(URI.create(packInfo.getUrl().toString()))
                .hash(toHex(packInfo.getSha1()))
                .build();

        request = ResourcePackRequest.resourcePackRequest()
                .packs(PACK_INFO)
                .prompt(Component.text(Settings.getPackPromptMessage()))
                .required(Settings.getPackIsRequired())
                .build();
    }

    @Subscribe
    public void onPlayerConnect(ServerPostConnectEvent event) {
        if (!this.packInfo.isConfigured()) return;

        event.getPlayer().sendResourcePacks(request);
    }

    private String toHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b)); // lowercase hex
        }
        return hexString.toString();
    }
}
