package io.github.alathra.vpack;

public interface Reloadable {
    /**
     * On plugin load.
     */
    void onInit(VPack plugin);

    /**
     * On plugin disable.
     */
    void onShutdown(VPack plugin);
}

