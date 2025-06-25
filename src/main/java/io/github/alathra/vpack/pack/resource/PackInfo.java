package io.github.alathra.vpack.pack.resource;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.alathra.vpack.config.Settings;
import io.github.alathra.vpack.utils.ResourcePackUtil;
import io.github.milkdrinkers.colorparser.velocity.ColorParser;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A class that manages the resource pack information, including its URL and SHA-1 hash.
 * It provides methods to get and set the pack URL and hash, as well as to update and save the hash to disk.
 */
public final class PackInfo {
    private final ProxyServer proxy;
    private final Logger logger;
    private final Distribute distribute;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private @Nullable URL url;
    private byte[] sha1;

    public PackInfo(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
        this.distribute = new Distribute(proxy, this);

        initializeUrl();
        if (url != null) {
            initializeHash();
        }
    }

    private void initializeUrl() {
        final String urlString = Settings.getPackUrl();
        if (urlString == null || urlString.isEmpty()) {
            if (urlString == null) {
                logger.warn("Missing config key \"pack-uri\"");
            }
            this.url = null;
            return;
        }

        try {
            this.url = new URL(urlString);
        } catch (MalformedURLException e) {
            logger.error("Invalid pack URL: {}", urlString, e);
            this.url = null;
        }
    }

    private void initializeHash() {
        try {
            updateSha1();
        } catch (IOException e) {
            logger.warn("Failed to fetch resource pack hash on initialization. " +
                "Hash will be calculated on first request or restart", e);
        }
    }

    public Optional<URL> getUrl() {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(url);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void setUrl(URL newUrl) {
        lock.writeLock().lock();
        try {
            this.url = newUrl;
            Settings.setPackUrl(newUrl == null ? "" : newUrl.toString());
            this.sha1 = null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<byte[]> getSha1() {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(sha1.clone());
        } finally {
            lock.readLock().unlock();
        }
    }

    private void setSha1(byte[] newSha1) {
        lock.writeLock().lock();
        try {
            if (newSha1 != null) {
                this.sha1 = newSha1.clone();
            } else {
                this.sha1 = null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Updates the URL of the pack if it is different from the current one.
     * If the new URL is identical to the current one, it checks if the SHA-1 hash matches.
     * If both match, it returns false indicating no update was made.
     * If the URL is updated, it also updates the SHA-1 hash.
     *
     * @param newUrl The new URL to set for the pack.
     * @return true if the URL was updated, false if it was identical and no update was needed.
     * @throws IOException if there is an error fetching the SHA-1 hash from the new URL.
     * @apiNote This method is designed to prevent unnecessary updates when the pack at the new URL is identical to the current one. It internally calls {@link #updateSha1()} when required.
     */
    public boolean updateUrl(URL newUrl) throws IOException {
        // Prevent overriding with identical pack
        if (getUrl().isPresent() && getUrl().get().equals(newUrl)) {
            Optional<byte[]> remoteSha1 = ResourcePackUtil.fetchSha1FromUrl(newUrl).join();

            if (remoteSha1.isPresent() && getSha1().isPresent() && Arrays.equals(getSha1().get(), remoteSha1.get())) {
                return false; // The new pack at the URL is identical to the current one
            }
        }

        setUrl(newUrl);
        if (newUrl != null) {
            updateSha1();
        }
        return true;
    }

    /**
     * Updates the SHA-1 hash of the pack by fetching it from the configured URL.
     * If the URL is not set, an exception is thrown.
     * If the fetch fails, an IOException is thrown.
     */
    public void updateSha1() throws IOException {
        Optional<URL> currentUrl = getUrl();
        if (currentUrl.isEmpty())
            throw new IllegalStateException("Cannot update hash: no URL configured");

        Optional<byte[]> newHashOpt = ResourcePackUtil.fetchSha1FromUrl(currentUrl.get()).join();
        if (newHashOpt.isEmpty())
            throw new IOException("Failed to fetch SHA1 hash from URL: " + currentUrl.get());

        setSha1(newHashOpt.get());
    }

    /**
     * Checks if the pack is configured with a URL and SHA-1 hash and is thus safe to be distributed.
     *
     * @return true if the pack is configured, false otherwise.
     */
    public boolean isConfigured() {
        lock.readLock().lock();
        try {
            return url != null && sha1 != null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Distribute distribute() {
        return distribute;
    }

    public static final class Distribute {
        private final ProxyServer proxy;
        private final PackInfo packInfo;

        public Distribute(ProxyServer proxy, PackInfo packInfo) {
            this.proxy = proxy;
            this.packInfo = packInfo;
        }

        public void sendToPlayer(Player player) {
            if (!packInfo.isConfigured())
                return;

            try {
                final ResourcePackInfo info = ResourcePackUtil.createPackInfo(packInfo);
                final ResourcePackRequest.Builder request = ResourcePackUtil.createPackRequest(info);

                player.sendResourcePacks(
                    request
                        .prompt(
                            ColorParser.of(Settings.getPackPromptMessage())
                                .legacy()
                                .mini(player)
                                .build()
                        )
                        .build()
                );
            } catch (URISyntaxException | NoSuchElementException e) {
                throw new RuntimeException(e);
            }
        }

        public void sendToAllPlayers() {
            if (!packInfo.isConfigured())
                return;

            try {
                final ResourcePackInfo info = ResourcePackUtil.createPackInfo(packInfo);
                final ResourcePackRequest.Builder request = ResourcePackUtil.createPackRequest(info);

                for (Player player : proxy.getAllPlayers()) {
                    final ResourcePackRequest builtRequest = request
                        .prompt(
                            ColorParser.of(Settings.getPackPromptMessage())
                                .legacy()
                                .mini(player)
                                .build()
                        )
                        .build();

                    player.sendResourcePacks(builtRequest);
                }
            } catch (URISyntaxException | NoSuchElementException e) {
                throw new RuntimeException(e);
            }
        }
    }
}