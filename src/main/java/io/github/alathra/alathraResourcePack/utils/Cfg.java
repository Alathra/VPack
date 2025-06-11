package io.github.alathra.alathraResourcePack.utils;

import com.github.milkdrinkers.Crate.Config;
import io.github.alathra.alathraResourcePack.AlathraResourcePack;
import org.jetbrains.annotations.NotNull;

public abstract class Cfg {
    @NotNull
    public static Config get() {
        return AlathraResourcePack.getInstance().getConfigHandler().getConfig();
    }
}

