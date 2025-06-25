package io.github.alathra.vpack.config;

import io.github.alathra.vpack.utils.Cfg;
import io.github.alathra.vpack.utils.Data;

public class Settings {
    public static String getPackUrl() {
        return Data.get().getOrDefault("pack-uri", "");
    }

    public static void setPackUrl(String url) {
        Data.get().set("pack-uri", url);
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
