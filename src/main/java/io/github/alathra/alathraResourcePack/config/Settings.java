package io.github.alathra.alathraResourcePack.config;

import io.github.alathra.alathraResourcePack.utils.Cfg;

public class Settings {
    public static String getPackUrl() {
        return Cfg.get().getOrDefault("pack-uri", "");
    }

    public static void setPackUrl(String url) {
        Cfg.get().set("pack-uri", url);
    }

    public static boolean getForceHashUpdateOnStart() {
        return Cfg.get().getOrDefault("force-hash-update-on-start", true);
    }

    public static int getPackBufferSize() {
        return Cfg.get().getOrDefault("pack-buffer-size", 8192);
    }

    public static boolean getIsGithubEnabled() {
        return Cfg.get().getOrDefault("github.enabled", false);
    }

    public static int getGithubUpdateInterval() {
        return Cfg.get().getOrDefault("github.interval", 600);
    }

    public static String getGithubUsername() {
        return Cfg.get().getOrDefault("github.username", "");
    }

    public static String getGithubRepository() {
        return Cfg.get().getOrDefault("github.repository", "");
    }

    public static boolean getPackIsRequired() {
        return Cfg.get().getOrDefault("pack-info.required", true);
    }

    public static String getPackPromptMessage() {
        return Cfg.get().getOrDefault("pack-info.prompt", "Please download the resource pack!");
    }
}
