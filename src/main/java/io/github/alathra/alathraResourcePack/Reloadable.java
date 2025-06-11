package io.github.alathra.alathraResourcePack;

public interface Reloadable {
    /**
     * On plugin load.
     */
    void onLoad(AlathraResourcePack plugin);

    /**
     * On plugin enable.
     */
    void onEnable(AlathraResourcePack plugin);

    /**
     * On plugin disable.
     */
    void onDisable(AlathraResourcePack plugin);
}

