package io.github.alathra.vpack.pack.resource;

import org.slf4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Handles I/O operations for resource packs, including fetching hashes, saving/loading hashes from cache, and managing the pack stream.
 */
public class PackIOHandler {
    private final Logger logger;
    private final Path cacheFile;

    public PackIOHandler(Logger logger, Path cacheFile) {
        this.logger = logger;
        this.cacheFile = cacheFile;
    }

    public static byte[] fetchPackHash(URL packUrl) throws IOException {
//        logger.info("[BSP] Fetching resource pack hash from: {}", packUrl);

        final MessageDigest hashObject = createSha1Digest();

        try (InputStream packStream = openPackStream(packUrl);
             DigestInputStream hashStream = new DigestInputStream(packStream, hashObject)) {

//            logger.info("[BSP] Downloading and hashing pack...");
            hashStream.readAllBytes();

            byte[] hash = hashObject.digest();
//            logger.info("[BSP] Pack hash calculated successfully");
            return hash;
        }
    }

    private static InputStream openPackStream(URL packUrl) throws IOException {
        try {
            return packUrl.openStream();
        } catch (IOException e) {
//            logger.error("[BSP] Failed to download pack from: {}", packUrl, e);
            throw new IOException("Failed to download resource pack from " + packUrl, e);
        }
    }

    public void saveHashToFile(byte[] hash) throws IOException {
        ensureCacheDirectoryExists();

        final String hexHash = HexFormat.of().formatHex(hash);

        try (FileWriter writer = new FileWriter(cacheFile.toFile())) {
            writer.write(hexHash);
            logger.debug("[BSP] Hash saved to cache file: {}", cacheFile);
        } catch (IOException e) {
            logger.error("[BSP] Failed to save hash to file: {}", cacheFile, e);
            throw e;
        }
    }

    public byte[] loadHashFromFile() throws IOException {
        if (!cacheFileExists())
            throw new FileNotFoundException("Hash cache file not found: " + cacheFile);

        try {
            final String hexHash = Files.readString(cacheFile).trim();

            if (hexHash.isEmpty())
                throw new IOException("Hash cache file is empty");

            final byte[] hash = HexFormat.of().parseHex(hexHash);
            logger.debug("[BSP] Hash loaded from cache file: {}", cacheFile);
            return hash;
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid hash format in cache file: " + cacheFile, e);
        } catch (IOException e) {
            logger.error("[BSP] Failed to load hash from file: {}", cacheFile, e);
            throw e;
        }
    }

    public boolean cacheFileExists() {
        return cacheFile.toFile().exists() && cacheFile.toFile().isFile();
    }

    private static MessageDigest createSha1Digest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            // This should never happen according to Java specification
            throw new RuntimeException("SHA-1 algorithm not available in this JVM implementation", e);
        }
    }

    private void ensureCacheDirectoryExists() throws IOException {
        final File parentDir = cacheFile.getParent().toFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create cache directory: " + parentDir.getPath());
            }
        }
    }
}