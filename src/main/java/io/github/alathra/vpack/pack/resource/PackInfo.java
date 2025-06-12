package io.github.alathra.vpack.pack.resource;

import io.github.alathra.vpack.config.Settings;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A class that manages the resource pack information, including its URL and SHA-1 hash.
 * It provides methods to get and set the pack URL and hash, as well as to update and save the hash to disk.
 */
public class PackInfo {
    private final Logger logger;
    private final PackIOHandler ioHandler;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private @Nullable URL url;
    private byte[] sha1;

    public PackInfo(Logger logger, Path cachePath, boolean loadHashOnInit) {
        this.logger = logger;
        this.ioHandler = new PackIOHandler(this.logger, cachePath);

        initializeUrl();

        if (loadHashOnInit && url != null) {
            initializeHash();
        }
    }

    private void initializeUrl() {
        String urlString = Settings.getPackUrl();
        if (urlString == null || urlString.isEmpty()) {
            if (urlString == null) {
                logger.warn("[BSP] Missing config key \"pack-uri\"");
            }
            this.url = null;
            return;
        }

        try {
            this.url = new URL(urlString);
        } catch (MalformedURLException e) {
            logger.error("[BSP] Invalid pack URL: {}", urlString, e);
            this.url = null;
        }
    }

    private void initializeHash() {
        try {
            updateSha1();
        } catch (IOException e) {
            logger.warn("[BSP] Failed to fetch resource pack hash on initialization. " +
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

    public void setUrl(URL newUrl) {
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

    public void updateSha1() throws IOException {
        Optional<URL> currentUrl = getUrl();
        if (currentUrl.isEmpty())
            throw new IllegalStateException("Cannot update hash: no URL configured");

        final byte[] newHash = PackIOHandler.fetchPackHash(currentUrl.get());
        setSha1(newHash);
    }

    public void saveHash() throws IOException {
        Optional<byte[]> currentHash = getSha1();
        if (currentHash.isEmpty())
            throw new IllegalStateException("Cannot save hash: no hash available");

        ioHandler.saveHashToFile(currentHash.get());
    }

    public void loadHash() throws IOException {
        byte[] loadedHash = ioHandler.loadHashFromFile();
        setSha1(loadedHash);
    }

    public boolean isConfigured() {
        lock.readLock().lock();
        try {
            return url != null && sha1 != null;
        } finally {
            lock.readLock().unlock();
        }
    }
}