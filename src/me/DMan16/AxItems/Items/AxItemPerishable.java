package me.DMan16.AxItems.Items;

import me.Aldreda.AxUtils.Classes.Pair;
import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxItems.Restrictions.Restrictions;
import me.DMan16.AxStats.AxStat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AxItemPerishable extends AxItem {
	protected final static String translateStatBase = "attribute.name.aldreda.";
	protected final static NamespacedKey durabilityKey = Utils.namespacedKey("durability");
	protected final static String attributeKey = Utils.namespacedKey("attribute").toString();
	protected final static NamespacedKey brokenKey = Utils.namespacedKey("broken");
	
	public final int maxDurability;
	public final boolean isUnbreakable;
	protected int durability;
	protected boolean broken;
	public final String repairItemKey;
	
	public AxItemPerishable(ItemStack item, @Nullable String key, @Nullable Component name, @Nullable List<Component> topLore, @Nullable List<Component> bottomLore,
				  @Nullable List<String> keywords, int maxDurability, String repairItemKey, AxStat... stats) {
		this(item,key,name,topLore,bottomLore,null,null,keywords,maxDurability,repairItemKey,stats);
	}
	
	public AxItemPerishable(ItemStack item, @Nullable String key, @Nullable Component name, @Nullable List<Component> topLore, @Nullable List<Component> bottomLore,
							@Nullable Consumer<Pair<AxItem, PlayerInteractEvent>> rightClick, @Nullable Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick,
							List<String> keywords, int maxDurability, String repairItemKey, AxStat ... stats) {
		this(item,key,name,topLore,bottomLore,null,null,keywords,maxDurability,repairItemKey,null,stats);
	}
	
	public AxItemPerishable(ItemStack item, @Nullable String key, @Nullable Component name, @Nullable List<Component> topLore, @Nullable List<Component> bottomLore,
							@Nullable List<String> keywords, int maxDurability, String repairItemKey, Material original, AxStat... stats) {
		this(item,key,name,topLore,bottomLore,null,null,keywords,maxDurability,repairItemKey,original,stats);
	}
	
	protected AxItemPerishable(ItemStack item, @Nullable String key, @Nullable Component name, @Nullable List<Component> topLore, @Nullable List<Component> bottomLore,
				  @Nullable Consumer<Pair<AxItem, PlayerInteractEvent>> rightClick, @Nullable Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick,
				  List<String> keywords, int maxDurability, String repairItemKey, Material original, AxStat ... stats) {
		super(item,key,name,topLore,bottomLore,null,null,keywords,original,stats);
		this.maxDurability = maxDurability;
		this.isUnbreakable = maxDurability <= 0;
		this.durability = maxDurability;
		this.broken = false;
		this.repairItemKey = repairItemKey;
		if (this.isUnbreakable) unbreakable(true);
		updateDurability();
	}
	
	public boolean isBroken() {
		return broken;
	}
	
	public int durability() {
		return durability;
	}
	
	@Override
	public ItemStack item(Player player) {
		if (broken) return brokenItem(player);
		return super.item(player);
	}
	
	protected AxItemPerishable updateDurability() {
		if (!isUnbreakable) PersistentDataContainerSet(durabilityKey,PersistentDataType.INTEGER,durability);
		return this;
	}
	
	public AxItemPerishable damage(int amount) {
		return changeDurability(-Math.abs(amount),false);
	}
	
	public AxItemPerishable repair(int amount) {
		return changeDurability(Math.abs(amount),false);
	}
	
	public AxItemPerishable repairWithMaterial(int times) {
		return changeDurability(Math.abs(times),true);
	}
	
	public int itemMaterialAmountToFull() {
		if (isUnbreakable) return 0;
		return (int) Math.ceil((((double)maxDurability - durability) / maxDurability) * 4);
	}
	
	protected AxItemPerishable changeDurability(int amount, boolean percent) {
		if (isUnbreakable || amount == 0) return this;
		int change;
		if (durability > 0) change = percent ? (maxDurability * amount) / 4 : amount;
		else change = amount > 0 ? 1 : -1;
		durability += change;
		updateDurability();
		return this;
	}
	
	protected ItemStack brokenItem(Player player) {
		Component name = name().append(Component.text(" (").append(Component.translatable("item.aldreda.item.broken")).append(Component.text(")")).decoration(
				TextDecoration.ITALIC,false));
		ItemStack item = Utils.makeItem(material(),name,ItemFlag.values());
		ItemMeta meta = item.getItemMeta();
		meta.setUnbreakable(true);
		meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(),attributeKey,0, AttributeModifier.Operation.ADD_NUMBER,
				EquipmentSlot.OFF_HAND));
		meta.getPersistentDataContainer().set(brokenKey,PersistentDataType.STRING,Utils.ObjectToBase64(super.item(player)));
		item.setItemMeta(meta);
		return Restrictions.Unequippable.add(item);
	}
	
	// !!!
	@Override
	protected List<Component> belowBottomLore() {
		List<Component> belowBottomLore = new ArrayList<Component>();
		Component durabilityLine;
		if (!isUnbreakable) {
			TextColor color;
			double ratio = (double)maxDurability / durability;
			if (ratio >= 100) color = NamedTextColor.RED;
			else if (ratio >= 20) color = NamedTextColor.GOLD;
			else if (ratio >= 2) color = NamedTextColor.YELLOW;
			else color = NamedTextColor.GREEN;
			durabilityLine = Component.translatable("item.durability",NamedTextColor.GRAY,Component.text(durability,color),
					Component.text(maxDurability,NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC,false);
		} else durabilityLine = Component.translatable("item.unbreakable",NamedTextColor.BLUE).decoration(TextDecoration.ITALIC,false);
		belowBottomLore.add(durabilityLine);
		return belowBottomLore;
	}
	
	public static AxItemPerishable getAxItemPerishable(ItemStack original) {
		try {
			if (original.getItemMeta().getPersistentDataContainer().get(brokenKey,PersistentDataType.STRING) != null)
				return getAxItemPerishable((ItemStack) Utils.ObjectFromBase64(original.getItemMeta().getPersistentDataContainer().get(brokenKey,PersistentDataType.STRING)));
			return (AxItemPerishable) getAxItem(original);
		} catch (Exception e) {}
		return null;
	}
	
	@Override
	protected KeyedItem meta(ItemMeta meta) {
		super.meta(meta);
		if (!isUnbreakable) try {
			durability = Math.max(Math.min(PersistentDataContainerGet(durabilityKey,PersistentDataType.INTEGER),maxDurability),0);
		} catch (Exception e) {}
		broken = !isUnbreakable && durability <= 0;
		return this;
	}
}