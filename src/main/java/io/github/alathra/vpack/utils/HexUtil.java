package io.github.alathra.vpack.utils;

import java.util.HexFormat;

public final class HexUtil {
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    public static String toString(final byte[] bytes) {
        return HEX_FORMAT.formatHex(bytes);
    }

    public static byte[] toByteArray(final String hex) {
        return HEX_FORMAT.parseHex(hex.replaceAll("\\s+", ""));
    }
}
