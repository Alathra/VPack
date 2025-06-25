package io.github.alathra.vpack.config;

import io.github.alathra.vpack.Reloadable;
import io.github.alathra.vpack.VPack;
import io.github.milkdrinkers.crate.Config;

import javax.inject.Singleton;

/**
 * A class that generates/loads {@literal &} provides access to a configuration file.
 */
@Singleton
public class ConfigHandler implements Reloadable {
    private final VPack plugin;
    private final Config cfg;
    private final Config data;

    /**
     * Instantiates a new Config handler.
     *
     * @param plugin the plugin instance
     */
    public ConfigHandler(VPack plugin) {
        this.plugin = plugin;
        cfg = new Config("config", plugin.getDataDirectory().toString(), plugin.getClass().getResourceAsStream("/config.yml")); // Create a config file from the template in our resources folder
        data = new Config("data", plugin.getDataDirectory().toString(), plugin.getClass().getResourceAsStream("/data.yml")); // Create a config file from the template in our resources folder
    }

    @Override
    public void onInit(VPack plugin) {
    }

    @Override
    public void onShutdown(VPack plugin) {
    }

    /**
     * Gets main config object.
     *
     * @return the config object
     */
    public Config getConfig() {
        return cfg;
    }

    /**
     * Gets data config object.
     *
     * @return the config object
     */
    public Config getDataConfig() {
        return data;
    }
}
