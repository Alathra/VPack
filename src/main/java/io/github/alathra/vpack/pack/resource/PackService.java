package io.github.alathra.vpack.pack.resource;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.alathra.vpack.config.Settings;
import io.github.alathra.vpack.utils.ResourcePackUtil;
import io.github.milkdrinkers.colorparser.velocity.ColorParser;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.NoSuchElementException;

public class PackService {
    private final ProxyServer proxy;
    private final PackInfo packInfo;

    public PackService(ProxyServer proxy, PackInfo packInfo) {
        this.proxy = proxy;
        this.packInfo = packInfo;
    }

    /**
     * Ensures the pack is ready for use (has URL and hash)
     */
    public void ensurePackReady() throws IOException {
        if (packInfo.getUrl().isEmpty())
            throw new IllegalStateException("Resource pack URL not configured");

        if (packInfo.getSha1().isEmpty())
            packInfo.updateSha1();
    }

    /**
     * Refreshes the pack hash from the remote source
     */
    public void refreshPack() throws IOException {
        packInfo.updateSha1();
        packInfo.saveHash();
    }

    /**
     * Updates the pack URL and refreshes the hash
     */
    public boolean updatePackUrl(URL newUrl) throws IOException {
        // Prevent overriding with identical pack
        if (packInfo.getUrl().isPresent() && packInfo.getUrl().get().equals(newUrl)) {
            final byte[] newSha1 = PackIOHandler.fetchPackHash(newUrl);
            if (packInfo.getSha1().isPresent() && packInfo.getSha1().get().equals(newSha1)) {
                return false;
            }
        }

        packInfo.setUrl(newUrl);
        if (newUrl != null) {
            packInfo.updateSha1();
            packInfo.saveHash();
        }
        return true;
    }

    /**
     * Attempts to load hash from cache, falls back to fetching if not available
     */
    public void initializeWithCache() throws IOException {
        if (packInfo.getUrl().isEmpty())
            throw new IllegalStateException("Resource pack URL not configured");

        try {
            packInfo.loadHash();
        } catch (IOException e) {
            // Cache load failed, fetch fresh hash
            packInfo.updateSha1();
            packInfo.saveHash();
        }
    }

    public void sendPackToPlayer(Player player) {
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

    public void sendPackToAllPlayers() {
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