package mc.obliviate.inventory.configurable.util;

import com.google.common.base.Preconditions;
import mc.obliviate.inventory.configurable.GuiConfigurationTable;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemStackSerializer {

    @Nonnull
    public static ItemStack deserializeMaterial(@Nonnull ConfigurationSection section) {
        return ItemStackSerializer.deserializeMaterial(section, GuiConfigurationTable.getDefaultConfigurationTable());
    }

    @Nonnull
    public static ItemStack deserializeMaterial(@Nonnull ConfigurationSection section, GuiConfigurationTable table) {
        final String materialName = section.getString(table.getMaterialSectionName());
        if (materialName == null) throw new IllegalArgumentException("material section could not find");

        final Optional<XMaterial> xmaterial = XMaterial.matchXMaterial(materialName);
        if (!xmaterial.isPresent()) {
            throw new IllegalArgumentException("Material could not found: " + materialName);
        }

        ItemStack item = xmaterial.get().parseItem();
        if (item == null) {
            throw new IllegalArgumentException("Material could not parsed as item stack: " + materialName);
        }
        return item;
    }

    @Nonnull
    public static ItemStack deserializeItemStack(@Nonnull ConfigurationSection section) {
        return ItemStackSerializer.deserializeItemStack(section, GuiConfigurationTable.getDefaultConfigurationTable());
    }

    @Nonnull
    public static ItemStack deserializeItemStack(@Nonnull ConfigurationSection section, @Nullable GuiConfigurationTable table) {
        if (table == null) table = GuiConfigurationTable.getDefaultConfigurationTable();
        Preconditions.checkNotNull(table, "param table and default table cannot be null at same time.");

        if (section.getBoolean("bukkit-serializing", false)) {
            ItemStack item = section.getItemStack("item");
            Preconditions.checkNotNull(item, "bukkit serializing could not applied to item: " + section.getName());
            return item;
        }

        final ItemStack item = deserializeMaterial(section, table);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(section.getString(table.getDisplayNameSectionName()));
        meta.setLore(section.getStringList(table.getLoreSectionName()));
        item.setItemMeta(meta);

        parseColorOfItemStack(item);
        applyEnchantmentsToItemStack(item, deserializeEnchantments(section, table));

        meta = item.getItemMeta();
        if (section.isSet(table.getCustomModelDataSectionName()))
            meta.setCustomModelData(section.getInt(table.getCustomModelDataSectionName()));
        if (section.getBoolean(table.getUnbreakableSectionName()))
            meta.setUnbreakable(true);
        if (section.isSet(table.getDurabilitySectionName()) && meta instanceof Damageable damageable)
            damageable.setDamage(section.getInt(table.getDurabilitySectionName()));
        if (section.getBoolean(table.getGlowSectionName())) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            if (meta.getEnchants().isEmpty()) {
                Enchantment protection = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("protection"));
                if (protection != null) {
                    meta.addEnchant(protection, 1, true);
                }
            }
        }
        item.setItemMeta(meta);

        applyItemFlagsToItemStacks(item, deserializeItemFlags(section, table));
        item.setAmount(section.getInt(table.getAmountSectionName(), 1));

        return item;
    }

    public static void applyItemFlagsToItemStacks(@Nonnull ItemStack item, @Nonnull ItemFlag[] itemFlags) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (itemFlags.length == 0) return;

        for (ItemFlag itemFlag : itemFlags) {
            if (itemFlag == null) continue;
            meta.addItemFlags(itemFlag);
        }
        item.setItemMeta(meta);
    }

    public static ItemFlag[] deserializeItemFlags(@Nonnull ConfigurationSection section) {
        return ItemStackSerializer.deserializeItemFlags(section, GuiConfigurationTable.getDefaultConfigurationTable());
    }

    public static ItemFlag[] deserializeItemFlags(@Nonnull ConfigurationSection section, @Nullable GuiConfigurationTable table) {
        if (table == null) table = GuiConfigurationTable.getDefaultConfigurationTable();
        Preconditions.checkNotNull(table, "param table and default table cannot be null at same time.");

        ItemFlag[] itemFlags = new ItemFlag[ItemFlag.values().length];

        List<String> serializedItemFlags = section.getStringList(table.getItemFlagsSectionName());
        if (serializedItemFlags.isEmpty()) return itemFlags;
        if (serializedItemFlags.contains("*")) return ItemFlag.values();

        int index = 0;
        for (String serializedItemFlag : serializedItemFlags) {
            try {
                ItemFlag itemFlag = ItemFlag.valueOf(serializedItemFlag);
                Preconditions.checkNotNull(itemFlag);
                itemFlags[index++] = itemFlag;
            } catch (Exception e) {
                throw new IllegalArgumentException("item flag could not find: " + serializedItemFlag);
            }
        }

        return itemFlags;
    }

    public static void applyEnchantmentsToItemStack(ItemStack item, @Nonnull Map<Enchantment, Integer> enchantments) {
        if (item == null) return;
        if (enchantments.isEmpty()) return;
        if (item.getType().equals(XMaterial.ENCHANTED_BOOK.parseMaterial())) {
            final EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            if (meta == null) return;

            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                meta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
            }

            item.setItemMeta(meta);
        } else {
            item.addUnsafeEnchantments(enchantments);
        }
    }

    public static Map<Enchantment, Integer> deserializeEnchantments(@Nonnull ConfigurationSection section) {
        return ItemStackSerializer.deserializeEnchantments(section, GuiConfigurationTable.getDefaultConfigurationTable());
    }

    public static Map<Enchantment, Integer> deserializeEnchantments(@Nonnull ConfigurationSection section, @Nonnull GuiConfigurationTable table) {
        if (!section.isSet(table.getEnchantmentsSectionName())) return new HashMap<>();
        Map<Enchantment, Integer> map = new HashMap<>();
        for (final String serializedEnchantment : section.getStringList(table.getEnchantmentsSectionName())) {
            final Map.Entry<Enchantment, Integer> enchantmentValue = deserializeEnchantment(serializedEnchantment);
            map.put(enchantmentValue.getKey(), enchantmentValue.getValue());
        }
        return map;
    }

    public static Map.Entry<Enchantment, Integer> deserializeEnchantment(@Nonnull String serializedEnchantment) {
        Preconditions.checkNotNull(serializedEnchantment, "serialized enchantment cannot be null");
        String[] datas = serializedEnchantment.split(":");
        Preconditions.checkArgument(datas.length == 2, "Enchantment could not deserialized: " + serializedEnchantment);
        Enchantment enchantment;
        int value;
        try {
            enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(datas[0].toLowerCase()));
            value = Integer.parseInt(datas[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Enchantment or its Value could not resolved: " + datas[0]);
        }
        Preconditions.checkArgument(enchantment != null, "Enchantment could not find: " + datas[0]);
        final Enchantment finalEnchantment = enchantment;
        final int finalValue = value;
        return new Map.Entry<>() {
            @Override
            public Enchantment getKey() {
                return finalEnchantment;
            }

            @Override
            public Integer getValue() {
                return finalValue;
            }

            @Override
            public Integer setValue(Integer value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static void serializeItemStack(@Nullable ItemStack item, @Nonnull ConfigurationSection section) {
        serializeItemStack(item, section, GuiConfigurationTable.getDefaultConfigurationTable());
    }

    public static void serializeItemStack(@Nullable ItemStack item, @Nonnull ConfigurationSection section, @Nonnull GuiConfigurationTable table) {
        if (item == null || item.getType().equals(XMaterial.AIR.parseMaterial())) {
            section.set(table.getMaterialSectionName(), XMaterial.AIR.name());
            return;
        }

        if (item.getItemMeta() instanceof PotionMeta ||
                item.getItemMeta() instanceof EnchantmentStorageMeta ||
                item.getItemMeta() instanceof FireworkMeta ||
                item.getItemMeta() instanceof BookMeta ||
                item.getItemMeta() instanceof BannerMeta ||
                item.getItemMeta() instanceof MapMeta ||
                item.getItemMeta() instanceof LeatherArmorMeta ||
                item.getItemMeta() instanceof SkullMeta ||
                item.getItemMeta() instanceof FireworkEffectMeta) {
            section.set("bukkit-serializing", true);
            section.set("item", item);
            return;
        }

        section.set(table.getMaterialSectionName(), XMaterial.matchXMaterial(item).name());
        if (item.getItemMeta() instanceof Damageable damageable && damageable.getDamage() != 0) {
            section.set(table.getDurabilitySectionName(), damageable.getDamage());
        }
        if (item.getAmount() != 1) {
            section.set(table.getAmountSectionName(), item.getAmount());
        }
        if (!item.getEnchantments().isEmpty()) {
            section.set(table.getEnchantmentsSectionName(), serializeEnchantments(item.getEnchantments()));
        }

        if (item.getItemMeta() != null) {
            section.set(table.getDisplayNameSectionName(), item.getItemMeta().getDisplayName());
            if (item.getItemMeta().getLore() != null && !item.getItemMeta().getLore().isEmpty()) {
                section.set(table.getLoreSectionName(), item.getItemMeta().getLore());
            }
            if (!item.getItemMeta().getItemFlags().isEmpty()) {
                section.set(table.getItemFlagsSectionName(), serializeItemFlags(item.getItemMeta().getItemFlags()));
            }
            section.set(table.getUnbreakableSectionName(), item.getItemMeta().isUnbreakable());
            if (item.getItemMeta().hasCustomModelData()) {
                section.set(table.getCustomModelDataSectionName(), item.getItemMeta().getCustomModelData());
            }
        }
    }

    public static List<String> serializeEnchantments(@Nonnull Map<Enchantment, Integer> enchantments) {
        final List<String> results = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            results.add(serializeEnchantment(entry.getKey(), entry.getValue()));
        }
        return results;
    }

    public static String serializeEnchantment(@Nonnull Enchantment enchantment, @Nonnegative int level) {
        return enchantment.getKey().getKey() + ":" + level;
    }

    private static List<String> serializeItemFlags(@Nonnull Set<ItemFlag> flags) {
        return flags.stream().map(ItemFlag::name).collect(Collectors.toList());
    }

    public static void applyPlaceholdersToItemStack(ItemStack item, PlaceholderUtil placeholderUtil) {
        if (item == null) return;
        if (placeholderUtil == null) return;
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName(placeholderUtil.apply(meta.getDisplayName()));
        meta.setLore(placeholderUtil.apply(meta.getLore()));
        item.setItemMeta(meta);
    }

    public static void parseColorOfItemStack(ItemStack item) {
        if (item == null) return;
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (meta.getDisplayName() != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName()));
        }
        if (meta.getLore() != null) {
            meta.setLore(meta.getLore().stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList()));
        }
        item.setItemMeta(meta);
    }
}
