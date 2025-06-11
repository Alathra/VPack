package io.github.alathra.alathraResourcePack.core;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.alathra.alathraResourcePack.AlathraResourcePack;
import io.github.alathra.alathraResourcePack.config.Settings;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.HexFormat;

public class ReloadPackTask implements Runnable {
    private final AlathraResourcePack plugin;
    private final ProxyServer server;
    private final PackInfo packInfo;
    private final boolean sync;
    private final boolean push;
    private final CommandSource taskAuthor;
    private final String oldHash;

    private FetchTask executingTask;

    public ReloadPackTask(
            AlathraResourcePack plugin,
            ProxyServer server,
            CommandSource taskAuthor,
            PackInfo packInfo,
            boolean sync,
            boolean push
    ) {
        this.plugin = plugin;
        this.server = server;
        this.taskAuthor = taskAuthor;
        this.packInfo = packInfo;
        this.sync = sync;
        this.push = push;
        this.oldHash = this.packInfo.isConfigured()
                ? HexFormat.of().formatHex(this.packInfo.getSha1())
                : null;
    }

    public void start() {
        // Schedule this task to run repeatedly every 2 ticks
        server.getScheduler().buildTask(plugin, this)
                .repeat(Duration.ofMillis(100)) // ≈ 2 ticks
                .schedule();
    }

    @Override
    public void run() {
        if (this.executingTask == null) {
            this.executingTask = new FetchTask(this.packInfo);
            if (this.sync) {
                server.getScheduler().buildTask(plugin, this.executingTask).schedule();
            } else {
                server.getScheduler().buildTask(plugin, this.executingTask).schedule(); // Velocity is always async
            }
            return;
        }

        Boolean success = this.executingTask.getSuccessState();
        if (success == null) return;

        if (!success) {
            sendToAuthor("§cCould not fetch resource pack!");
            server.getConsoleCommandSource().sendMessage(Component.text("[BSP] Could not fetch resource pack in reload task! Sync: " + this.sync));
        } else if (saveHash()) {
            if (this.push && this.oldHash != null && !this.oldHash.equals(HexFormat.of().formatHex(this.packInfo.getSha1()))) {
                sendToAuthor("Updated pack hash!");
                server.getConsoleCommandSource().sendMessage(Component.text("[BSP] Updated pack hash!"));
                pushPackToPlayers();
            }
        }
        // No way to cancel tasks explicitly in Velocity, so the task will end unless repeated.
    }

    private boolean saveHash() {
        try {
            this.packInfo.saveHash();
            return true;
        } catch (IOException e) {
            sendToAuthor("§cCould not save hash! The hash is still updated, but will reset on the next restart.");
            server.getConsoleCommandSource().sendMessage(Component.text("Could not save hash to cache file in reload task. Sync: " + this.sync));
            return false;
        }
    }

    private void pushPackToPlayers() {
        sendToAuthor("Pushing update to all players");

        ResourcePackInfo PACK_INFO = ResourcePackInfo.resourcePackInfo()
                .uri(URI.create(packInfo.getUrl().toString()))
                .hash(toHex(packInfo.getSha1()))
                .build();

        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(PACK_INFO)
                .prompt(Component.text(Settings.getPackPromptMessage()))
                .required(Settings.getPackIsRequired())
                .build();

        for (Player player : server.getAllPlayers()) {
            player.clearResourcePacks();
            player.sendResourcePacks(request);
        }
    }

    private void sendToAuthor(String message) {
        if (this.taskAuthor != null) {
            this.taskAuthor.sendMessage(Component.text(message));
        }
    }

    private class FetchTask implements Runnable {
        private final PackInfo packInfo;
        private Boolean isSuccessful;

        public FetchTask(PackInfo packInfo) {
            this.packInfo = packInfo;
        }

        @Override
        public void run() {
            try {
                this.packInfo.updateSha1();
                this.isSuccessful = true;
            } catch (IOException e) {
                this.isSuccessful = false;
            }
            // Trigger reload logic in main task again
            server.getScheduler().buildTask(plugin, ReloadPackTask.this).schedule();
        }

        public Boolean getSuccessState() {
            return isSuccessful;
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b)); // lowercase hex
        }
        return hexString.toString();
    }
}
