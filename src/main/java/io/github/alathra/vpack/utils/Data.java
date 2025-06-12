package io.github.alathra.vpack.utils;

import io.github.alathra.vpack.VPack;
import io.github.milkdrinkers.crate.Config;
import org.jetbrains.annotations.NotNull;

public final class Data {
    @NotNull
    public static Config get() {
        return VPack.getInstance().getConfigHandler().getDataConfig();
    }
}

