package me.DMan16.AxItems.Items;

import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxItems.AxItems;
import me.DMan16.AxItems.Restrictions.Restrictions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class AxItemPerishable extends AxItem {
	protected static final String translateStatBase = "attribute.name.aldreda.";
	protected static final NamespacedKey durabilityKey = new NamespacedKey(AxItems.getInstance(),"axitem_durability");
	protected static final String attributeKey = new NamespacedKey(AxItems.getInstance(),"axitem_attribute").toString();
	protected static final NamespacedKey brokenKey = new NamespacedKey(AxItems.getInstance(),"axitem_broken");
	
	public final int maxDurability;
	public final boolean isUnbreakable;
	protected int durability;
	protected boolean broken;
	public final String repairItemKey;
	
	protected AxItemPerishable(ItemStack item, @Nullable String key, @Nullable Component name, int maxDurability, String repairItemKey) {
		this(item,key,name,null,maxDurability,repairItemKey);
	}
	
	protected AxItemPerishable(ItemStack item, @Nullable String key, @Nullable Component name, Material original, int maxDurability, String repairItemKey) {
		super(item,key,name,original);
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
	public ItemStack item(@Nullable Player player) {
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
		if (key() != null) meta.getPersistentDataContainer().set(ItemKey,PersistentDataType.STRING,key());
		item.setItemMeta(meta);
		return Restrictions.Unequippable.add(item);
	}
	
	// !!!
	@Override
	protected List<Component> bottomLoreFinal(Player player) {
		List<Component> bottomLore = super.bottomLoreFinal(player);
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
		bottomLore.add(durabilityLine);
		return bottomLore;
	}
	
	public static AxItemPerishable getAxItem(ItemStack original) {
		try {
			if (original.getItemMeta().getPersistentDataContainer().get(brokenKey,PersistentDataType.STRING) != null)
				return getAxItem((ItemStack) Utils.ObjectFromBase64(original.getItemMeta().getPersistentDataContainer().get(brokenKey,PersistentDataType.STRING)));
			return (AxItemPerishable) AxItem.getAxItem(original,AxItem.class);
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