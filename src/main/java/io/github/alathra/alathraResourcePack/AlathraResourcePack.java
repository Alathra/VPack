package io.github.alathra.alathraResourcePack;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.alathra.alathraResourcePack.config.ConfigHandler;
import io.github.alathra.alathraResourcePack.config.Settings;
import io.github.alathra.alathraResourcePack.core.GithubUpdateTask;
import io.github.alathra.alathraResourcePack.core.PackInfo;
import io.github.alathra.alathraResourcePack.listeners.JoinHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.List;

@Plugin(id = "alathraresourcepack", name = "AlathraResourcePack", version = BuildConstants.VERSION, authors = {"Schmeb_"})
public class AlathraResourcePack {
    private static AlathraResourcePack instance;
    private final Path pathDirectory;
    private final ProxyServer proxy;
    private final Logger logger;

    // handlers
    private ConfigHandler configHandler;

    // Handlers list (defines order of load/enable/disable)
    private List<? extends Reloadable> handlers;

    private final static String hashCacheName = "pack.sha1";
    private PackInfo packInfo;
    private GithubUpdateTask githubTask = null;

    @Inject
    public AlathraResourcePack(ProxyServer proxy, Logger logger, @DataDirectory Path pathDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.pathDirectory = pathDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        configHandler = new ConfigHandler(instance);

        handlers = List.of(
                configHandler
        );

        for (Reloadable handler : handlers)
            handler.onLoad(instance);

        boolean forceHashUpdate = Settings.getForceHashUpdateOnStart();
        File hashCache = pathDirectory.resolve(hashCacheName).toFile();
        boolean shouldUpdateHash = forceHashUpdate || !hashCache.exists();

        boolean loadSuccessful = this.loadPackInfo(shouldUpdateHash, hashCache);
        // Cache Save/Loading
        if (loadSuccessful && this.packInfo.isConfigured()) {
            if (shouldUpdateHash) {
                try {
                    this.packInfo.saveHash();
                } catch (IOException e) {
                    logger.warn("[BSP] Could not update cached hash!");
                }
            } else {
                readFromCache(hashCache);
            }
        }

        proxy.getEventManager().register(this, new JoinHandler(packInfo));

        enableUpdateTask();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        instance = null;
        disableUpdateTask();
    }

    private void readFromCache(File cache){
        try (BufferedReader hashReader = new BufferedReader(new FileReader(cache))){
            String hexHash = hashReader.readLine();
            this.packInfo.setSha1(HexFormat.of().parseHex(hexHash));
        } catch (java.io.FileNotFoundException e){
            // I have done stuff like this in other parts of this plugin as well.
            // Basically: We don't ever want this error to occur.
            // => If it occurs, it's real bad and should appear as a full-blown error.
            throw new RuntimeException(e);
        } catch (IOException e){
            logger.error("Could not load cached hash! The plugin will not work unless you reload it.");
        }
    }

    private boolean loadPackInfo(boolean forceUpdate, File cache){
        try{
            this.packInfo = new PackInfo(this, forceUpdate, cache);
            return true;
        } catch (MalformedURLException e){
            logger.warn("[BSP] Resourcepack URL has an invalid format");
        }
        return false;
    }

    private void enableUpdateTask() {
        boolean shouldRun = Settings.getIsGithubEnabled();
        if(!shouldRun) return;
        if(githubTask != null && githubTask.isCancelled()) return;

        final long interval = Settings.getGithubUpdateInterval(); // in seconds, default: 600
        final long delay = 120; // Wait 2 minutes after startup

        githubTask = new GithubUpdateTask(this, packInfo);
        githubTask.start(delay, interval);
    }

    private void disableUpdateTask() {
        if (githubTask == null)
            return;

        if (githubTask.isCancelled())
            return;

        githubTask.cancel();
    }

    @NotNull
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    @NotNull
    public ProxyServer getServer() {
        return proxy;
    }

    @NotNull
    public ConsoleCommandSource getConsoleCommandSource() {
        return proxy.getConsoleCommandSource();
    }

    @NotNull
    public Path getPathDirectory() {
        return pathDirectory;
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    public static AlathraResourcePack getInstance() {
        return instance;
    }
}
