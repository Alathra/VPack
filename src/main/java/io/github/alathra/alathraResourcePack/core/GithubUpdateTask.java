package io.github.alathra.alathraResourcePack.core;

import com.velocitypowered.api.scheduler.ScheduledTask;
import io.github.alathra.alathraResourcePack.AlathraResourcePack;
import io.github.alathra.alathraResourcePack.config.Settings;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class GithubUpdateTask implements Runnable {
    private final AlathraResourcePack plugin;
    private final PackInfo packInfo;
    private ScheduledTask scheduledTask;
    private boolean cancelled = false;

    public GithubUpdateTask(AlathraResourcePack plugin, PackInfo packInfo) {
        super();
        this.plugin = plugin;
        this.packInfo = packInfo;
    }

    public void start(long initialDelaySeconds, long intervalSeconds) {
        if (cancelled) return;

        this.scheduledTask = plugin.getServer().getScheduler()
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
            plugin.getLogger().info("Trying to get latest version...");

            GitHub gitHub = GitHub.connectAnonymously();
            String username = Settings.getGithubUsername();
            String repoName = Settings.getGithubRepository();

            GHRepository repository = gitHub.getRepository(username + "/" + repoName);
            GHRelease release = repository.getLatestRelease();

            GHAsset latestAsset = release.listAssets().toList().get(0);
            if (latestAsset != null)
                return latestAsset.getBrowserDownloadUrl();
        } catch (IOException e) {
            plugin.getLogger().warn("Failed to fetch latest GitHub release: {}", e.getMessage());
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
            this.packInfo.setUrl(url);
        } catch (java.net.MalformedURLException e){
            plugin.getLogger().error("Malformed GitHub asset URL: {}", e.getMessage());
            return;
        }

        if (this.packInfo.getUrl() == null) return;

        new ReloadPackTask(
                this.plugin,
                plugin.getServer(),
                plugin.getConsoleCommandSource(),
                this.packInfo,
                false,
                true
        ).start();
    }
}
