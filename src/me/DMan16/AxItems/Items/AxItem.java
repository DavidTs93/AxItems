package me.DMan16.AxItems.Items;

import me.Aldreda.AxUtils.Classes.Pair;
import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxStats.AxStat;
import me.DMan16.AxStats.AxStatType;
import me.DMan16.AxStats.AxStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class AxItem extends KeyedItem {
	private static List<String> AxItemKeys = new ArrayList<String>();
	private static List<AxItem> AxItems = new ArrayList<AxItem>();
	private static List<String> disabledVanilla = new ArrayList<String>();
	private static List<String> allKeywords = new ArrayList<String>();
	private final static HashMap<Material,String> originals = new HashMap<Material,String>();
	
	private List<String> keywords;
	private Component name;
	private List<Component> topLore;
	private List<Component> bottomLore;
	private List<AxStat> stats;
	private List<AxSet> sets;
	/**
	 * PlayerInteractEvent will always be of Right Click action
	 */
	protected Consumer<Pair<AxItem,PlayerInteractEvent>> rightClick;
	/**
	 * PlayerInteractEvent will always be of Left Click action
	 */
	protected Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick;
	private final Material original;
	
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
		this(item,key,name,topLore,bottomLore,rightClick,leftClick,keywords,null,stats);
	}
	
	protected AxItem(ItemStack item, @Nullable String key, @Nullable Component name, @Nullable List<Component> topLore, @Nullable List<Component> bottomLore,
				  @Nullable List<String> keywords, Material original, AxStat ... stats) {
		this(item,key,name,topLore,bottomLore,null,null,keywords,original,stats);
	}
	
	protected AxItem(ItemStack item, @Nullable String key, @Nullable Component name, @Nullable List<Component> topLore, @Nullable List<Component> bottomLore,
			@Nullable Consumer<Pair<AxItem,PlayerInteractEvent>> rightClick, @Nullable Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick,
			List<String> keywords, Material original, AxStat ... stats) {
		super(clearItem(item),key);
		this.name = name;
		this.topLore = topLore == null ? new ArrayList<Component>() : new ArrayList<Component>(topLore);
		this.bottomLore = bottomLore == null ? new ArrayList<Component>() : new ArrayList<Component>(bottomLore);
		this.rightClick = rightClick;
		this.leftClick = leftClick;
		this.keywords = new ArrayList<String>();
		this.original = original;
		this.stats = new ArrayList<AxStat>();
		this.sets = new ArrayList<AxSet>();
		addKeywords(keywords);
		addStats(stats);
	}
	
	private static ItemStack clearItem(ItemStack item) {
		if (Utils.isNull(item)) return null;
		item = clearAttributes(item);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(null);
		meta.lore(null);
		item.setItemMeta(meta);
		return item;
	}
	
	private static ItemStack clearAttributes(ItemStack item) {
		if (Utils.isNull(item)) return null;
		item = item.clone();
		ItemMeta meta = item.getItemMeta();
		meta.setAttributeModifiers(null);
		item.setItemMeta(meta);
		return item;
	}
	
	@Override
	public ItemStack item(Player player) {
		ItemStack item = super.item(player);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(makeName());
		meta.lore(makeLore(player));
		for (AxStat stat : stats) {
			Pair<Attribute,AttributeModifier> attribute = stat.attribute();
			if (attribute != null && attribute.first() != null && attribute.second() != null) meta.addAttributeModifier(attribute.first(),attribute.second());
		}
		item.setItemMeta(meta);
		return item;
	}
	
	public AxItem rightClick(Pair<AxItem,PlayerInteractEvent> info) {
		if (rightClick != null) rightClick.accept(info);
		return this;
	}
	
	public AxItem leftClick(Pair<AxItem,PlayerInteractEvent> info) {
		if (leftClick != null) leftClick.accept(info);
		return this;
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
	
	protected List<Component> makeLore(Player player) {
		List<List<Component>> loreTemp = new ArrayList<List<Component>>();
		List<Component> aboveTopLore = aboveTopLore();
		List<Component> statsLore = statsLore();
		List<Component> setLore = setLore(player);
		List<Component> enchantmentsLore = enchantmentsLore();
		List<Component> belowBottomLore = belowBottomLore();
		//lore.add(Component.empty());		// ?
		if (!aboveTopLore.isEmpty()) loreTemp.add(aboveTopLore);
		if (!topLore.isEmpty()) loreTemp.add(topLore);
		if (!statsLore.isEmpty()) loreTemp.add(statsLore);
		if (!enchantmentsLore.isEmpty()) loreTemp.add(enchantmentsLore);	// Maybe after setLore?
		if (!setLore.isEmpty()) loreTemp.add(setLore);
		if (!bottomLore.isEmpty()) loreTemp.add(bottomLore);
		if (!belowBottomLore.isEmpty()) loreTemp.add(belowBottomLore);
		List<Component> lore = new ArrayList<Component>();
		for (int i = 0; i < loreTemp.size(); i++) {
			if (i > 0) lore.add(Component.empty());
			lore.addAll(loreTemp.get(i));
		}
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

	private List<Component> setLore(Player player) {
		List<Component> setLore = new ArrayList<Component>();
		List<AxSet> sets = getSets();
		if (sets.isEmpty() || player == null) return setLore;
		boolean first = true;
		for (AxSet set : getSets()) {
			if (first) first = false;
			else setLore.add(Component.empty());
			setLore.addAll(set.lore(player));
		}
		return setLore;
	}
	
	protected List<Component> enchantmentsLore() {
		List<Component> enchantmentsLore = new ArrayList<Component>();
		for (Entry<Enchantment,Integer> ench : getEnchantments().entrySet()) {
			NamespacedKey key = ench.getKey().getKey();
			Component comp = Component.translatable("enchantment." + key.getNamespace() + "." + key.getKey());
			if (ench.getValue() != ench.getKey().getStartLevel() || ench.getKey().getStartLevel() != ench.getKey().getMaxLevel())
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
			if (set != null && set.contains(this)) sets.add(set);
		}
		return sets;
	}
	
	public static void addToSets(@NotNull String key, AxSet ... sets) {
		if (key == null) return;
		int idx = AxItemKeys.indexOf(key.toLowerCase());
		if (idx < 0) return;
		AxItem item = AxItems.get(idx);
		for (AxSet set : sets) if (set != null && set.isRegistered()) item.addKeywords(Arrays.asList("set","set_" + set.key()));
	}
	
	/**
	 * IMPORTANT!!!
	 * Once an Item has been registered its registered form can no longer be changed!!!
	 */
	public AxItem register() {
		Objects.requireNonNull(key(),"Item key cannot be NULL!");
		if (AxItemKeys.contains(key())) throw new IllegalArgumentException("The key: \"" + key() + "\" is already being used!");
		if (original != null) if (originals.putIfAbsent(original,key()) != null) throw new IllegalArgumentException("An original AxItemPerishable for this Material already exists!");
		AxItemKeys.add(key());
		AxItems.add(clone());
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
		this.stats = AxStats.joinStats(Utils.joinLists(this.stats,stats));
		/*if (stats == null) return this;
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
		this.stats.addAll(add);*/
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
	
	public static ItemStack update(ItemStack item, @NotNull Player player) {
		try {
			return getAxItem(item).item(player);
		} catch (Exception e) {}
		return item;
	}
	
	public static AxItem getAxItem(ItemStack original) {
		try {
			ItemMeta meta = original.getItemMeta();
			AxItem item;
			if (meta.getPersistentDataContainer().has(ItemKey,PersistentDataType.STRING)) {
				item = getAxItem(meta.getPersistentDataContainer().get(ItemKey,PersistentDataType.STRING));
				original = clearAttributes(original);
			} else {
				item = getAxItem(original.getType().name());
				item.name(meta.displayName());
				item.topLore(meta.lore());
			}
			item.meta(meta);
			item.setAmount(original.getAmount());
			
			/*List<Component> aboveTopLore = item.aboveTopLore();
			List<Component> statsLore = item.statsLore();
			List<Component> enchantmentsLore = item.enchantmentsLore();
			List<Component> belowBottomLore = item.belowBottomLore();
			ListIterator<Component> iter = original.lore().listIterator();
			List<Component> topLore = new ArrayList<Component>();
			List<Component> bottomLore = new ArrayList<Component>();
			for (int i = 0; iter.hasNext() && i < aboveTopLore.size(); i++) if (!aboveTopLore.get(i).equals(iter.next())) break;*/
			
			return item;
		} catch (Exception e) {}
		return null;
	}
	
	public static AxItem getAxItem(String key) {
		if (key == null) return null;
		key = key.toLowerCase();
		int idx = AxItemKeys.indexOf(key);
		if (idx >= 0) return AxItems.get(idx).clone();
		if (disabledVanilla.contains(key)) return null;
		Material material = Material.getMaterial(key.toUpperCase());
		if (material != null) return new AxItem(new ItemStack(material),key,Arrays.asList("minecraft","vanilla"));
		return null;
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
		if (!keys.isEmpty()) for (AxItem item : AxItems) {
			boolean add = true;
			for (String keyword : keys) if (!item.hasKeyword(keyword)) {
				add = false;
				break;
			}
			if (add && !set.contains(item)) set.add(item);
		}
		return set;
	}

	
	public static List<String> getAllItemKeys() {
		return new ArrayList<String>(AxItemKeys);
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
			AxItem item = (AxItem) super.clone();
			item.keywords = new ArrayList<String>(this.keywords);
			item.topLore = new ArrayList<Component>(this.topLore);
			item.bottomLore = new ArrayList<Component>(this.bottomLore);
			item.stats = new ArrayList<AxStat>(this.stats);
			return item;
		} catch (Exception e) {}
		return null;
	}
	
	public static AxItem getLegal(ItemStack original) {
		if (Utils.isNull(original)) return null;
		AxItem item = getAxItem(original);
		if (item != null) return item;
		return AxItem.getAxItem(originals.get(original.getType()));
	}
}