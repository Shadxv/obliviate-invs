package mc.obliviate.inventory.configurable.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public interface PlaceholderUtil {

    @Nullable
    String apply(@Nullable String text);

    @Nullable
    default List<String> apply(@Nullable List<String> texts) {
        if (texts == null) return null;
        return texts.stream().map(this::apply).collect(Collectors.toList());
    }
}
