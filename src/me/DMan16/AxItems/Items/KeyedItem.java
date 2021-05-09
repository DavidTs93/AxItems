package me.DMan16.AxItems.Items;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.Aldreda.AxUtils.Classes.Pair;
import me.Aldreda.AxUtils.Utils.Utils;
import net.kyori.adventure.text.Component;

@SuppressWarnings("unchecked")
class KeyedItem implements Cloneable {
	protected final static NamespacedKey ItemKey = Utils.namespacedKey("aldreda_axitem");
	
	private ItemStack item;
	private String key;
	
	KeyedItem(Material material, @Nullable String key) {
		this(new ItemStack(Objects.requireNonNull(material)),key);
	}
	
	KeyedItem(ItemStack item, @Nullable String key) {
		this.item = Objects.requireNonNull(Utils.isNull(item) ? null : item.clone());
		if (key != null) {
			ItemMeta meta = this.item.getItemMeta();
			meta.getPersistentDataContainer().set(ItemKey,PersistentDataType.STRING,key);
			this.item.setItemMeta(meta);
		}
		this.key = key;
	}
	
	/**
	 * @return clone of the item
	 */
	public ItemStack item() {
		return item.clone();
	}
	
	public String key() {
		return key;
	}
	
	private ItemMeta meta() {
		return item.getItemMeta();
	}
	
	protected KeyedItem meta(ItemMeta meta) {
		item.setItemMeta(meta);
		return this;
	}
	
	public int getAmount() {
		return item.getAmount();
	}
	
	public KeyedItem setAmount(int amount) {
		item.setAmount(amount);
		return this;
	}
	
	public KeyedItem addAmount(int amount) {
		setAmount(getAmount() + amount);
		return this;
	}
	
	public KeyedItem removeAmount(int amount) {
		setAmount(getAmount() - amount);
		return this;
	}
	
	public Component name() {
		return meta().hasDisplayName() ? meta().displayName() : null;
	}
	
	public KeyedItem name(Component name) {
		ItemMeta meta = meta();
		meta.displayName(name);
		return meta(meta);
	}
	
	public Map<Enchantment,Integer> getEnchantments() {
		return item.getEnchantments();
	}
	
	public KeyedItem setEnchantments(Pair<Enchantment,Integer> ... enchantments) {
		return clearEnchantments().setEnchantments(enchantments);
	}
	
	public KeyedItem clearEnchantments() {
		return removeEnchantments(getEnchantments().keySet().toArray(new Enchantment[0]));
	}
	
	public KeyedItem addEnchantments(Pair<Enchantment,Integer> ... enchantments) {
		if (enchantments.length == 0) return this;
		item.addUnsafeEnchantments(Pair.toMap(enchantments));
		return this;
	}
	
	public KeyedItem removeEnchantments(Enchantment ... enchantments) {
		for (Enchantment ench : enchantments) item.removeEnchantment(ench);
		return this;
	}
	
	public Integer model() {
		return meta().hasCustomModelData() ? meta().getCustomModelData() : null;
	}
	
	public KeyedItem model(Integer model) {
		ItemMeta meta = meta();
		meta.setCustomModelData(model);
		return meta(meta);
	}
	
	public <T> T PersistentDataContainerGet(NamespacedKey key, PersistentDataType<T,T> type) {
		return meta().getPersistentDataContainer().get(key,type);
	}
	
	public <T> KeyedItem PersistentDataContainerSet(NamespacedKey key, PersistentDataType<T,T> type, T val) {
		ItemMeta meta = meta();
		meta.getPersistentDataContainer().set(key,type,val);
		return meta(meta);
	}
	
	public KeyedItem PersistentDataContainerRemove(NamespacedKey key) {
		ItemMeta meta = meta();
		meta.getPersistentDataContainer().remove(key);
		return meta(meta);
	}
	
	public boolean unbreakable() {
		return meta().isUnbreakable();
	}
	
	public KeyedItem unbreakable(boolean flag) {
		ItemMeta meta = meta();
		meta.setUnbreakable(flag);
		return meta(meta);
	}
	
	/*public Multimap<Attribute,AttributeModifier> getAttributes() {
		return meta().getAttributeModifiers();
	}
	
	public KeyedItem setAttributes(Pair<Attribute,AttributeModifier> ... attributes) {
		return clearAttributes().addAttributes(attributes);
	}
	
	public KeyedItem clearAttributes() {
		return removeAttributes(getAttributes().keySet().toArray(new Attribute[0]));
	}
	
	public KeyedItem addAttributes(Pair<Attribute,AttributeModifier> ... attributes) {
		ItemMeta meta = meta();
		for (Pair<Attribute,AttributeModifier> attribute : attributes) meta.addAttributeModifier(attribute.first(),attribute.second());
		return meta(meta);
	}
	
	public KeyedItem removeAttributes(Attribute ... attributes) {
		ItemMeta meta = meta();
		for (Attribute attribute : attributes) meta.removeAttributeModifier(attribute);
		return meta(meta);
	}
	
	public KeyedItem removeAttributes(Pair<Attribute,AttributeModifier> ... attributes) {
		ItemMeta meta = meta();
		for (Pair<Attribute,AttributeModifier> attribute : attributes) meta.removeAttributeModifier(attribute.first(),attribute.second());
		return meta(meta);
	}*/
	
	@Override
	public KeyedItem clone() {
		try {
			KeyedItem item = this.getClass().cast(super.clone());
			item.item = item.item.clone();
			return item;
		} catch (Exception e) {}
		return null;
	}
}