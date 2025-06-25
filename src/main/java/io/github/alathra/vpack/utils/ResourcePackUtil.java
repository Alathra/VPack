package io.github.alathra.vpack.utils;

import io.github.alathra.vpack.config.Settings;
import io.github.alathra.vpack.pack.resource.PackInfo;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ResourcePackUtil {
    public static ResourcePackInfo createPackInfo(PackInfo packInfo) throws URISyntaxException, NoSuchElementException {
        if (!packInfo.isConfigured())
            return ResourcePackInfo.resourcePackInfo()
                .build();

        final URL url = packInfo.getUrl().orElseThrow();
        final byte[] sha1 = packInfo.getSha1().orElseThrow();

        return ResourcePackInfo.resourcePackInfo()
            .uri(url.toURI())
            .hash(hex(sha1))
            .build();
    }

    public static ResourcePackRequest.Builder createPackRequest(ResourcePackInfo resourcePackInfo) {
        return ResourcePackRequest.resourcePackRequest()
            .packs(resourcePackInfo)
            .replace(true)
            .required(Settings.getPackIsRequired());
    }

    public static String hex(byte[] bytes) {
        final StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b)); // lowercase hex
        }
        return hexString.toString();
    }

    /**
     * Get the SHA1 hash from a downloadable URL
     *
     * @param url the url to download from
     * @return the sha1 as an optional wrapped in a completable future
     */
    public static CompletableFuture<Optional<byte[]>> fetchSha1FromUrl(URL url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final MessageDigest digest = MessageDigest.getInstance("SHA-1");
                final URLConnection con = url.openConnection();

                try (
                    final InputStream packStream = con.getInputStream();
                    final DigestInputStream hashStream = new DigestInputStream(packStream, digest)
                ) {
                    byte[] buffer = new byte[8192];

                    while (hashStream.read(buffer) != -1) {
                    }

                    return Optional.of(digest.digest());
                }
            } catch (NoSuchAlgorithmException | IOException e) {
                return Optional.empty();
            }
        });
    }
}
