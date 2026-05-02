package mc.obliviate.inventory.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class NMSUtil {

    public static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .hexColors().useUnusualXRepeatedCharacterHexFormat().build();

}
