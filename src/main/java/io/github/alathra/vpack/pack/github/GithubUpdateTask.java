package io.github.alathra.vpack.pack.github;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import io.github.alathra.vpack.VPack;
import io.github.alathra.vpack.config.Settings;
import io.github.alathra.vpack.pack.resource.PackService;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class GithubUpdateTask implements Runnable {
    private final VPack plugin;
    private final ProxyServer proxy;
    private final Logger logger;
    private final PackService packService;

    private ScheduledTask scheduledTask;
    private boolean cancelled = false;

    public GithubUpdateTask(VPack plugin, ProxyServer proxy, Logger logger, PackService packService) {
        super();
        this.plugin = plugin;
        this.proxy = proxy;
        this.logger = logger;
        this.packService = packService;
    }

    public void start(long initialDelaySeconds, long intervalSeconds) {
        if (cancelled) return;

        this.scheduledTask = proxy.getScheduler()
            .buildTask(plugin, this)
            .delay(initialDelaySeconds, TimeUnit.SECONDS)
            .repeat(intervalSeconds, TimeUnit.SECONDS)
            .schedule();
    }

    public void cancel() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    private String getLatestRelease() {
        try {
            logger.info("Trying to get latest version...");

            GitHub gitHub = GitHub.connectAnonymously();
            String username = Settings.getGithubUsername();
            String repoName = Settings.getGithubRepository();

            GHRepository repository = gitHub.getRepository(username + "/" + repoName);
            GHRelease release = repository.getLatestRelease();

            GHAsset latestAsset = release.listAssets().toList().get(0);
            if (latestAsset != null)
                return latestAsset.getBrowserDownloadUrl();
        } catch (IOException e) {
            logger.warn("Failed to fetch latest GitHub release: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        final String downloadLink = getLatestRelease();
        if (downloadLink == null) return;

        try {
            final URL url = new URL(downloadLink);
            final boolean isNewPack = packService.updatePackUrl(url);

            if (isNewPack)
                packService.sendPackToAllPlayers();
        } catch (java.net.MalformedURLException e) {
            logger.error("Malformed GitHub asset URL: {}", e.getMessage());
            return;
        } catch (IOException e) {
            logger.error("Failed to update pack url from GitHub: {}", e.getMessage());
            return;
        }
    }
}
