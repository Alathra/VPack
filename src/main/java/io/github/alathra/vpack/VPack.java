package io.github.alathra.vpack;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.alathra.vpack.config.ConfigHandler;
import io.github.alathra.vpack.pack.PackHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;

@Plugin(
    id = "vpack",
    name = BuildConstants.NAME,
    description = BuildConstants.DESCRIPTION,
    version = BuildConstants.VERSION,
    authors = {"Schmeb_", "darksaid98"},
    dependencies = {
        @Dependency(id = "geyser", optional = true),
        @Dependency(id = "floodgate", optional = true)
    }
)
public class VPack {
    private static VPack instance;
    private final Path dataDirectory;
    private final ProxyServer proxy;
    private final Logger logger;

    // handlers
    private ConfigHandler configHandler;
    private PackHandler packHandler;

    // Handlers list (defines order of load/enable/disable)
    private List<? extends Reloadable> handlers;

    @Inject
    public VPack(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onProxyInitialization(ProxyInitializeEvent event) {
        configHandler = new ConfigHandler(instance);
        packHandler = new PackHandler(this, proxy, logger, dataDirectory);

        handlers = List.of(
            configHandler,
            packHandler
        );

        for (Reloadable handler : handlers)
            handler.onInit(instance);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onProxyShutdown(ProxyShutdownEvent event) {
        for (Reloadable handler : handlers)
            handler.onShutdown(instance);
    }

    @NotNull
    public static VPack getInstance() {
        return instance;
    }

    @NotNull
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    @NotNull
    public Path getDataDirectory() {
        return dataDirectory;
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }
}
