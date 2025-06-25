package io.github.alathra.vpack.pack.github;

import com.velocitypowered.api.proxy.ProxyServer;
import io.github.alathra.vpack.VPack;
import io.github.alathra.vpack.config.Settings;
import io.github.alathra.vpack.pack.resource.PackInfo;
import org.slf4j.Logger;

public class GitHubHandler {
    private final VPack plugin;
    private final ProxyServer proxy;
    private final Logger logger;
    private final PackInfo packInfo;

    public GitHubHandler(VPack plugin, ProxyServer proxy, Logger logger, PackInfo packInfo) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.logger = logger;
        this.packInfo = packInfo;
    }

    private GithubUpdateTask githubTask = null;

    public void enableUpdateTask() {
        final boolean shouldRun = Settings.getIsGithubEnabled();
        if (!shouldRun) return;
        if (githubTask != null && githubTask.isCancelled()) return;

        final long interval = Settings.getGithubUpdateInterval(); // in seconds, default: 600
        final long delay = 0; // Wait 2 minutes after startup

        githubTask = new GithubUpdateTask(plugin, proxy, logger, packInfo);
        githubTask.start(delay, interval);
    }

    public void disableUpdateTask() {
        if (githubTask == null)
            return;

        if (githubTask.isCancelled())
            return;

        githubTask.cancel();
    }
}
