package io.github.alathra.vpack.pack;

import com.velocitypowered.api.proxy.ProxyServer;
import io.github.alathra.vpack.Reloadable;
import io.github.alathra.vpack.VPack;
import io.github.alathra.vpack.listeners.JoinHandler;
import io.github.alathra.vpack.pack.github.GitHubHandler;
import io.github.alathra.vpack.pack.resource.PackInfo;
import org.slf4j.Logger;

public class PackHandler implements Reloadable {
    private final VPack plugin;
    private final ProxyServer proxy;
    private final Logger logger;
    private final PackInfo packInfo;
    private final GitHubHandler gitHubHandler;

    public PackHandler(VPack plugin, ProxyServer proxy, Logger logger) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.logger = logger;

        this.packInfo = new PackInfo(proxy, logger);
        this.gitHubHandler = new GitHubHandler(plugin, proxy, logger, packInfo);

        // Send the pack to all players on startup if the pack is configured
        this.packInfo.distribute().sendToAllPlayers();
    }

    @Override
    public void onInit(VPack plugin) {
        gitHubHandler.enableUpdateTask();
        proxy.getEventManager().register(plugin, new JoinHandler(packInfo));
    }

    @Override
    public void onShutdown(VPack plugin) {
        gitHubHandler.disableUpdateTask();
    }
}
