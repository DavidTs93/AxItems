package me.DMan16.AxItems.Items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import me.Aldreda.AxUtils.Classes.Pair;

public class AxItem extends KeyedItem {
	private static HashMap<String,AxItem> items = new HashMap<String,AxItem>();
	private static Set<String> disabledVanilla = new HashSet<String>();
	private static Set<String> allKeywords = new HashSet<String>();
	
	private Set<String> keywords;
	/**
	 * PlayerInteractEvent will always be of Right Click action
	 */
	public final Consumer<Pair<AxItem,PlayerInteractEvent>> rightClick;
	/**
	 * PlayerInteractEvent will always be of Left Click action
	 */
	public final Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick;
	
	public AxItem(ItemStack item, @Nullable String key, @Nullable String ... keywords) {
		this(item,key,null,null,keywords);
	}
	
	public AxItem(ItemStack item, @Nullable String key, @Nullable Consumer<Pair<AxItem,PlayerInteractEvent>> rightClick,
			@Nullable Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick, String ... keywords) {
		super(item,key);
		this.keywords = new HashSet<String>();
		addKeywords(keywords);
		this.rightClick = rightClick;
		this.leftClick = leftClick;
	}
	
	/**
	 * IMPORTANT!!!
	 * Once an Item has been registered its registered form can no longer be changed!!!
	 */
	public AxItem register() {
		items.put(Objects.requireNonNull(items.containsKey(Objects.requireNonNull(key())) ? null :
			Objects.requireNonNull(key(),"Item key cannot be NULL!"),"The key: \"" + key() + "\" is already being used!"),this.getClass().cast(clone()));
		return this;
	}
	
	public boolean hasKeyword(String keyword) {
		if (keyword == null) return false;
		return keywords.contains(keyword.toLowerCase());
	}
	
	public Set<String> getKeywords() {
		return new HashSet<String>(keywords);
	}
	
	AxItem addKeywords(String ... keywords) {
		for (String keyword : keywords) if (keyword != null) {
			this.keywords.add(keyword.toLowerCase());
			allKeywords.add(keyword.toLowerCase());
		}
		return this;
	}
	
	public static AxItem getAxItem(ItemStack original) {
		try {
			AxItem item = getAxItem(original.getItemMeta().getPersistentDataContainer().get(ItemKey,PersistentDataType.STRING));
			if (original.getEnchantments().size() > 0) item.addEnchantments(Pair.fromMap(original.getEnchantments()));
			return item;
		} catch (Exception e) {}
		return null;
	}
	
	public static AxItem getAxItem(String key) {
		if (key == null) return null;
		key = key.toLowerCase();
		AxItem item = items.get(key);
		if (item != null) return item.clone();
		if (disabledVanilla.contains(key)) return null;
		Material material = Material.getMaterial(key.toUpperCase());
		if (material != null) return new AxItem(new ItemStack(material),key,"minecraft","vanilla");
		return null;
	}
	
	static AxItem getAxItemOriginal(String key) {
		if (key == null) return null;
		AxItem item = items.get(key);
		return item;
	}
	
	public static void addDisabledVanillas(String ... keys) {
		for (String key : keys) try {
			disabledVanilla.add(Material.getMaterial(key.toUpperCase()).name().toLowerCase());
		} catch (Exception e) {}
	}
	
	public static Set<AxItem> getByKeywords(String ... keywords) {
		Set<AxItem> set = new HashSet<AxItem>();
		if (keywords.length > 0) for (AxItem item : items.values()) {
			boolean add = true;
			for (String keyword : keywords) if (!item.hasKeyword(keyword)) {
				add = false;
				break;
			}
			if (add) set.add(item);
		}
		return set;
	}

	
	public static Set<String> getAllItemKeys() {
		return items.keySet();
	}
	
	public static Set<String> getAllKeywords() {
		return new HashSet<String>(allKeywords);
	}
	
	@Override
	public AxItem clone() {
		try {
			AxItem item = this.getClass().cast(super.clone());
			item.keywords = new HashSet<String>(this.keywords);
			return item;
		} catch (Exception e) {}
		return null;
	}
}