package me.DMan16.AxItems.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.Aldreda.AxUtils.Classes.Pair;
import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxStats.AxStat;
import me.DMan16.AxStats.AxStatType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@SuppressWarnings("unchecked")
public class AxItem extends KeyedItem {
	private static HashMap<String,AxItem> items = new HashMap<String,AxItem>();
	private static List<String> disabledVanilla = new ArrayList<String>();
	private static List<String> allKeywords = new ArrayList<String>();
	
	private List<String> keywords;
	private Component name;
	private List<Component> topLore;
	private List<Component> bottomLore;
	private List<AxStat> stats;
	/**
	 * PlayerInteractEvent will always be of Right Click action
	 */
	protected Consumer<Pair<AxItem,PlayerInteractEvent>> rightClick;
	/**
	 * PlayerInteractEvent will always be of Left Click action
	 */
	protected Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick;
	
	private AxItem(ItemStack item, @Nullable String key, @Nullable List<String> keywords, AxStat ... stats) {
		this(item,key,null,null,null,keywords,stats);
	}
	
	public AxItem(ItemStack item, @Nullable String key, @Nullable Component name, @Nullable List<Component> topLore, @Nullable List<Component> bottomLore,
			@Nullable List<String> keywords, AxStat ... stats) {
		this(item,key,name,topLore,bottomLore,null,null,keywords,stats);
	}
	
	public AxItem(ItemStack item, @Nullable String key, @Nullable Component name, @Nullable List<Component> topLore, @Nullable List<Component> bottomLore,
			@Nullable Consumer<Pair<AxItem,PlayerInteractEvent>> rightClick, @Nullable Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick,
			List<String> keywords, AxStat ... stats) {
		super(clearItem(item),key);
		this.name = name;
		this.topLore = topLore == null ? new ArrayList<Component>() : new ArrayList<Component>(topLore);
		this.bottomLore = bottomLore == null ? new ArrayList<Component>() : new ArrayList<Component>(bottomLore);
		this.rightClick = rightClick;
		this.leftClick = leftClick;
		this.keywords = new ArrayList<String>();
		this.stats = new ArrayList<AxStat>();
		addKeywords(keywords);
		addStats(stats);
	}
	
	private static ItemStack clearItem(ItemStack item) {
		if (Utils.isNull(item)) return null;
		item = item.clone();
		ItemMeta meta = item.getItemMeta();
		meta.displayName(null);
		meta.lore(null);
		item.setItemMeta(meta);
		return item;
	}
	
	@Override
	public ItemStack item() {
		ItemStack item = super.item();
		ItemMeta meta = item.getItemMeta();
		meta.displayName(makeName());
		meta.lore(makeLore());
		for (AxStat stat : stats) {
			Pair<Attribute,AttributeModifier> attribute = stat.attribute();
			if (attribute != null && attribute.first() != null && attribute.second() != null) meta.addAttributeModifier(attribute.first(),attribute.second());
		}
		item.setItemMeta(meta);
		return item;
	}
	
	public Consumer<Pair<AxItem,PlayerInteractEvent>> rightClick() {
		return rightClick;
	}
	
	public Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick() {
		return leftClick;
	}
	
	protected AxItem rightClick(Consumer<Pair<AxItem,PlayerInteractEvent>> rightClick) {
		this.rightClick = rightClick;
		return this;
	}
	
	protected AxItem leftClick(Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick) {
		this.leftClick = leftClick;
		return this;
	}
	
	@Override
	public Component name() {
		return name;
	}
	
	@Override
	public AxItem name(Component name) {
		this.name = name;
		return this;
	}
	
	public List<Component> topLore() {
		return new ArrayList<Component>(topLore);
	}
	
	public AxItem topLore(List<Component> topLore) {
		this.topLore = topLore == null ? new ArrayList<Component>() : new ArrayList<Component>(topLore);
		return this;
	}
	
	public List<Component> bottomLore() {
		return new ArrayList<Component>(bottomLore);
	}
	
	public AxItem bottomLore(List<Component> bottomLore) {
		this.bottomLore = bottomLore == null ? new ArrayList<Component>() : new ArrayList<Component>(bottomLore);
		return this;
	}
	
	private Component makeName() {
		return name;
	}
	
	protected List<Component> makeLore() {
		List<Component> lore = new ArrayList<Component>();
		List<Component> aboveTopLore = aboveTopLore();
		List<Component> statsLore = statsLore();
		List<Component> setsLore = setsLore();
		List<Component> enchantmentsLore = enchantmentsLore();
		List<Component> belowBottomLore = belowBottomLore();
		//lore.add(Component.empty());		// ?
		if (!aboveTopLore.isEmpty()) {
			lore.addAll(aboveTopLore);
			lore.add(Component.empty());
		}
		if (!topLore.isEmpty()) {
			lore.addAll(topLore);
			lore.add(Component.empty());
		}
		if (!statsLore.isEmpty()) {
			lore.addAll(statsLore);
			lore.add(Component.empty());
		}
		if (!enchantmentsLore.isEmpty()) {	// Maybe after setsLore?
			lore.addAll(enchantmentsLore);
			lore.add(Component.empty());
		}
		if (!setsLore.isEmpty()) {
			lore.addAll(setsLore);
			lore.add(Component.empty());
		}
		if (!bottomLore.isEmpty()) {
			lore.addAll(bottomLore);
			lore.add(Component.empty());
		}
		if (!belowBottomLore.isEmpty()) lore.addAll(belowBottomLore);
		return lore;
	}
	
	protected List<Component> aboveTopLore() {
		List<Component> aboveTopLore = new ArrayList<Component>();
		
		return aboveTopLore;
	}
	
	private List<Component> statsLore() {
		List<Component> statsLore = new ArrayList<Component>();
		for (AxStat stat : stats) statsLore.add(stat.line());
		return statsLore;
	}
	
	protected List<Component> setsLore() {
		List<Component> setsLore = new ArrayList<Component>();
		
		return setsLore;
	}
	
	protected List<Component> enchantmentsLore() {
		List<Component> enchantmentsLore = new ArrayList<Component>();
		for (Entry<Enchantment,Integer> ench : getEnchantments().entrySet()) {
			NamespacedKey key = ench.getKey().getKey();
			Component comp = Component.translatable("enchantment." + key.getNamespace() + "." + key.getKey());
			if (ench.getValue() !=  ench.getKey().getStartLevel() || ench.getKey().getStartLevel() !=  ench.getKey().getMaxLevel())
				comp = comp.append(Component.space()).append(Component.text(Utils.toRoman(ench.getValue())));
			enchantmentsLore.add(comp.color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,false));
		}
		return enchantmentsLore;
	}
	
	protected List<Component> belowBottomLore() {
		List<Component> belowBottomLore = new ArrayList<Component>();
		
		return belowBottomLore;
	}
	
	public List<AxSet> getSets() {
		List<AxSet> sets = new ArrayList<AxSet>();
		if (!hasKeyword("set")) return sets;
		for (String keyword : keywords) if (keyword.startsWith("set_")) {
			AxSet set = AxSet.getAxSet(keyword.replaceFirst("set_",""));
			if (set != null) sets.add(set);
		}
		return sets;
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
	
	public AxItem original() {
		return getAxItem(key());
	}
	
	public boolean hasKeyword(String keyword) {
		if (keyword == null) return false;
		return keywords.contains(keyword.toLowerCase());
	}
	
	public List<String> getKeywords() {
		return new ArrayList<String>(keywords);
	}
	
	protected AxItem addKeywords(List<String> keywords) {
		if (keywords == null) return this;
		for (String keyword : keywords) if (keyword != null) {
			keyword = keyword.toLowerCase();
			if (!this.keywords.contains(keyword)) this.keywords.add(keyword);
			if (!allKeywords.contains(keyword)) allKeywords.add(keyword);
		}
		return this;
	}
	
	protected AxItem removeKeywords(String ... keywords) {
		return removeKeywords(Arrays.asList(keywords));
	}
	
	protected AxItem removeKeywords(List<String> keywords) {
		if (keywords == null) return this;
		for (String keyword : keywords) if (keyword != null) this.keywords.remove(keyword.toLowerCase());
		return this;
	}
	
	protected List<AxStat> getStats() {
		return new ArrayList<AxStat>(stats);
	}
	
	protected AxItem addStats(AxStat ... stats) {
		return addStats(Arrays.asList(stats));
	}
	
	protected AxItem addStats(List<AxStat> stats) {
		if (stats == null) return this;
		List<AxStat> add = new ArrayList<AxStat>();
		for (AxStat stat : stats) if (stat != null && stat.val1() != 0) {
			ListIterator<AxStat> iter = this.stats.listIterator();
			boolean found = false;
			while (iter.hasNext()) {
				AxStat joined = iter.next().join(stat);
				if (joined != null) {
					found = true;
					if (joined.val1() != 0) iter.set(joined);
					else iter.remove();
					break;
				}
			}
			if (!found) add.add(stat);
		}
		this.stats.addAll(add);
		return this;
	}
	
	protected AxItem removeStats(AxStat ... stats) {
		return removeStats(Arrays.asList(stats));
	}
	
	protected AxItem removeStats(List<AxStat> stats) {
		if (stats == null) return this;
		for (AxStat stat : stats) if (stat != null) this.stats.remove(stat);
		return this;
	}
	
	protected AxItem removeStatTypes(AxStatType ... stats) {
		return removeStatTypes(Arrays.asList(stats));
	}
	
	protected AxItem removeStatTypes(List<AxStatType> stats) {
		if (stats == null) return this;
		Iterator<AxStat> iter = this.stats.iterator();
		while (iter.hasNext()) if (stats.contains(iter.next().type())) iter.remove();
		return this;
	}
	
	public static ItemStack updateItem(ItemStack item) {
		try {
			return getAxItem(item).update(item).item();
		} catch (Exception e) {}
		return item;
	}
	
	protected AxItem update(ItemStack item) {
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
		if (material != null) return new AxItem(new ItemStack(material),key,Arrays.asList("minecraft","vanilla"));
		return null;
	}
	
	static AxItem getAxItemOriginal(String key) {
		if (key == null) return null;
		AxItem item = items.get(key);
		return item;
	}
	
	public static void addDisabledVanillas(String ... keys) {
		for (String key : keys) try {
			key = Material.getMaterial(key.toUpperCase()).name().toLowerCase();
			if (!disabledVanilla.contains(key)) disabledVanilla.add(key);
		} catch (Exception e) {}
	}
	
	public static void addDisabledVanillas(Material ... materials) {
		addDisabledVanillas(Arrays.asList(materials));
	}
	
	public static void addDisabledVanillas(List<Material> materials) {
		for (Material material : materials) if (material != null && !disabledVanilla.contains(material.name().toLowerCase())) disabledVanilla.add(material.name().toLowerCase());
	}
	
	public static boolean isDisabledVanilla(String key) {
		return key != null && disabledVanilla.contains(key);
	}
	
	public static boolean isDisabledVanilla(Material material) {
		return material != null && disabledVanilla.contains(material.name().toLowerCase());
	}
	
	public static List<AxItem> getByKeywords(String ... keywords) {
		List<AxItem> set = new ArrayList<AxItem>();
		List<String> keys = new ArrayList<String>();
		for (String keyword : keywords) if (keyword != null && !disabledVanilla.contains(keyword)) keys.add(keyword);
		if (!keys.isEmpty()) for (AxItem item : items.values()) {
			boolean add = true;
			for (String keyword : keys) if (!item.hasKeyword(keyword)) {
				add = false;
				break;
			}
			if (add && !set.contains(item)) set.add(item);
		}
		return set;
	}

	
	public static Set<String> getAllItemKeys() {
		return items.keySet();
	}
	
	public static List<String> getAllKeywords() {
		return new ArrayList<String>(allKeywords);
	}
	
	@Override
	public AxItem setAmount(int amount) {
		return (AxItem) super.setAmount(amount);
	}
	
	@Override
	public AxItem addAmount(int amount) {
		return (AxItem) super.addAmount(amount);
	}
	
	@Override
	public AxItem removeAmount(int amount) {
		return (AxItem) super.removeAmount(amount);
	}
	
	@Override
	public AxItem setEnchantments(Pair<Enchantment,Integer> ... enchantments) {
		return (AxItem) super.setEnchantments(enchantments);
	}
	
	@Override
	public AxItem clearEnchantments() {
		return (AxItem) super.clearEnchantments();
	}
	
	@Override
	public AxItem addEnchantments(Pair<Enchantment,Integer> ... enchantments) {
		return (AxItem) super.addEnchantments(enchantments);
	}
	
	@Override
	public AxItem removeEnchantments(Enchantment ... enchantments) {
		return (AxItem) super.removeEnchantments(enchantments);
	}
	
	@Override
	public AxItem model(Integer model) {
		return (AxItem) super.model(model);
	}
	
	@Override
	public <T> AxItem PersistentDataContainerSet(NamespacedKey key, PersistentDataType<T,T> type, T val) {
		return (AxItem) super.PersistentDataContainerSet(key,type,val);
	}
	
	@Override
	public AxItem unbreakable(boolean flag) {
		return (AxItem) super.unbreakable(flag);
	}
	
	@Override
	public AxItem PersistentDataContainerRemove(NamespacedKey key) {
		return (AxItem) super.PersistentDataContainerRemove(key);
	}
	
	@Override
	public AxItem clone() {
		try {
			AxItem item = this.getClass().cast(super.clone());
			item.keywords = new ArrayList<String>(this.keywords);
			item.topLore = new ArrayList<Component>(this.topLore);
			item.bottomLore = new ArrayList<Component>(this.bottomLore);
			item.stats = new ArrayList<AxStat>(this.stats);
			return item;
		} catch (Exception e) {}
		return null;
	}
}