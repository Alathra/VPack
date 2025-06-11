package io.github.alathra.alathraResourcePack.config;

import com.github.milkdrinkers.Crate.Config;
import io.github.alathra.alathraResourcePack.AlathraResourcePack;
import io.github.alathra.alathraResourcePack.Reloadable;

import javax.inject.Singleton;

/**
 * A class that generates/loads {@literal &} provides access to a configuration file.
 */
@Singleton
public class ConfigHandler implements Reloadable {
    private final AlathraResourcePack plugin;
    private Config cfg;

    /**
     * Instantiates a new Config handler.
     *
     * @param plugin the plugin instance
     */
    public ConfigHandler(AlathraResourcePack plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AlathraResourcePack plugin) {
        cfg = new Config("config", plugin.getPathDirectory().toString(), plugin.getClass().getResourceAsStream("/config.yml")); // Create a config file from the template in our resources folder
    }

    @Override
    public void onEnable(AlathraResourcePack plugin) {
    }

    @Override
    public void onDisable(AlathraResourcePack plugin) {
    }

    /**
     * Gets main config object.
     *
     * @return the config object
     */
    public Config getConfig() {
        return cfg;
    }
}
