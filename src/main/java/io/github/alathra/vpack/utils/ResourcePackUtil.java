package io.github.alathra.vpack.utils;

import io.github.alathra.vpack.config.Settings;
import io.github.alathra.vpack.pack.resource.PackInfo;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.NoSuchElementException;

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
}
