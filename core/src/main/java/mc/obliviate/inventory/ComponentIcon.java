package mc.obliviate.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ComponentIcon implements GuiIcon {

	private final Icon icon;

	private ComponentIcon(Icon icon) {
		this.icon = icon;
	}

	public static ComponentIcon fromIcon(Icon icon) {
		return new ComponentIcon(icon);
	}

	public Icon toIcon() {
		return icon;
	}

	@Nonnull
	public ComponentIcon setLore(final Component... lore) {
		return setLore(new ArrayList<>(Arrays.asList(lore)));
	}

	@Nonnull
	public ComponentIcon setLore(final List<Component> lore) {
		final ItemMeta meta = icon.getItem().getItemMeta();
		if (meta == null) return this;
		meta.lore(lore);
		icon.getItem().setItemMeta(meta);
		return this;
	}

	@Nonnull
	public ComponentIcon setName(final Component name) {
		final ItemMeta meta = icon.getItem().getItemMeta();
		if (meta == null) return this;
		meta.displayName(name);
		icon.getItem().setItemMeta(meta);
		return this;
	}

	@Nonnull
	public ComponentIcon appendLore(final Component... newLines) {
		return appendLore(new ArrayList<>(Arrays.asList(newLines)));
	}

	@Nonnull
	public ComponentIcon appendLore(final List<Component> lore) {
		final ItemMeta meta = icon.getItem().getItemMeta();
		if (meta == null) return this;
		List<Component> existing = meta.lore();
		List<Component> combined = (existing != null) ? new ArrayList<>(existing) : new ArrayList<>();
		combined.addAll(lore);
		meta.lore(combined);
		icon.getItem().setItemMeta(meta);
		return this;
	}

	@Nonnull
	public ComponentIcon insertLore(final int index, final Component... newLines) {
		return insertLore(index, new ArrayList<>(Arrays.asList(newLines)));
	}

	@Nonnull
	public ComponentIcon insertLore(final int index, final List<Component> newLines) {
		final ItemMeta meta = icon.getItem().getItemMeta();
		if (meta == null) return this;
		List<Component> existing = meta.lore();
		List<Component> combined = (existing != null) ? new ArrayList<>(existing) : new ArrayList<>();
		combined.addAll(index, newLines);
		meta.lore(combined);
		icon.getItem().setItemMeta(meta);
		return this;
	}

	@Nonnull
	public ComponentIcon setDurability(final short newDamage) {
		final ItemMeta meta = icon.getItem().getItemMeta();
		if (meta instanceof Damageable damageable) {
			damageable.setDamage(newDamage);
			icon.getItem().setItemMeta(meta);
		}
		return this;
	}

	@Nonnull
	public ComponentIcon setDurability(final int newDamage) {
		return setDurability((short) newDamage);
	}

	@Nonnull
	public ComponentIcon setAmount(final int amount) {
		this.icon.setAmount(amount);
		return this;
	}

	@Nonnull
	public ComponentIcon hideFlags(final ItemFlag... itemFlag) {
		this.icon.hideFlags(itemFlag);
		return this;
	}

	@Nonnull
	public ComponentIcon hideFlags() {
		this.icon.hideFlags();
		return this;
	}

	@Nonnull
	public ComponentIcon enchant(final Enchantment enchantment) {
		enchant(enchantment, enchantment.getStartLevel());
		return this;
	}

	@Nonnull
	public ComponentIcon enchant(final Map<Enchantment, Integer> enchantments) {
		this.icon.enchant(enchantments);
		return this;
	}

	@Nonnull
	public ComponentIcon enchant(final Enchantment enchantment, final int level) {
		this.icon.enchant(enchantment, level);
		return this;
	}

	@Nonnull
	public Consumer<InventoryClickEvent> getClickAction() {
		return this.icon.getClickAction();
	}

	@Nonnull
	public ComponentIcon onClick(Consumer<InventoryClickEvent> clickAction) {
		this.icon.onClick(clickAction);
		return this;
	}

	@Nonnull
	public Consumer<InventoryDragEvent> getDragAction() {
		return this.icon.getDragAction();
	}

	@Nonnull
	public ComponentIcon onDrag(Consumer<InventoryDragEvent> dragAction) {
		this.icon.onDrag(dragAction);
		return this;
	}

	public ItemStack getItem() {
		return icon.getItem();
	}

}
