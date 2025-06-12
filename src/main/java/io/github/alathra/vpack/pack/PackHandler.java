package io.github.alathra.vpack.pack;

import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.alathra.vpack.Reloadable;
import io.github.alathra.vpack.VPack;
import io.github.alathra.vpack.listeners.JoinHandler;
import io.github.alathra.vpack.pack.github.GitHubHandler;
import io.github.alathra.vpack.pack.resource.PackInfo;
import io.github.alathra.vpack.pack.resource.PackService;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class PackHandler implements Reloadable {
    private final VPack plugin;
    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private final PackService packService;
    private final GitHubHandler gitHubHandler;

    public PackHandler(VPack plugin, ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        PackInfo packInfo = new PackInfo(logger, dataDirectory.resolve("data").resolve("pack.sha1"), true);
        this.packService = new PackService(proxy, packInfo);

        try {
            this.packService.initializeWithCache();
            this.packService.sendPackToAllPlayers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.gitHubHandler = new GitHubHandler(plugin, proxy, logger, packService);
    }

    @Override
    public void onInit(VPack plugin) {
        gitHubHandler.enableUpdateTask();
        proxy.getEventManager().register(plugin, new JoinHandler(packService));
    }

    @Override
    public void onShutdown(VPack plugin) {
        gitHubHandler.disableUpdateTask();
    }
}
