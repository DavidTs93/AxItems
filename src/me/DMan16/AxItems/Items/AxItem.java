package me.DMan16.AxItems.Items;

import me.Aldreda.AxUtils.Classes.Pair;
import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxItems.Restrictions.Restrictions;
import me.DMan16.AxItems.Restrictions.Restrictions.Restriction;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class AxItem extends KeyedItem {
	private static String minecraft = "minecraft";
	private static String vanilla = "vanilla";
	private static NamespacedKey LoreLengthKey = new NamespacedKey(me.DMan16.AxItems.AxItems.getInstance(),"axitem_lore_length");
	
	private static List<String> AxItemKeys = new ArrayList<String>();
	private static List<AxItem> AxItems = new ArrayList<AxItem>();
	private static List<Material> disabledVanilla = new ArrayList<Material>();
	private static List<String> allKeywords = new ArrayList<String>(Arrays.asList(minecraft));
	private final static HashMap<Material,String> originals = new HashMap<Material,String>();
	
	private List<String> keywords;
	private Component name;
	private List<Component> topLore;
	private List<Component> bottomLore;
	private List<Component> belowBottomLore;
	private List<Restriction> restrictions;
	private List<AxStat> stats;
	/**
	 * PlayerInteractEvent will always be of Right Click action
	 */
	protected Consumer<Pair<AxItem,PlayerInteractEvent>> rightClick;
	/**
	 * PlayerInteractEvent will always be of Left Click action
	 */
	protected Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick;
	private final Material original;
	
	public AxItem(ItemStack item, @Nullable String key, @Nullable Component name) {
		this(item,key,name,null);
	}
	
	public AxItem(ItemStack item, @Nullable String key, @Nullable Component name, Material original) {
		super(clearItem(item),key);
		this.name = name;
		this.topLore = new ArrayList<Component>();
		this.bottomLore = new ArrayList<Component>();
		this.belowBottomLore = new ArrayList<Component>();
		this.rightClick = rightClick;
		this.leftClick = leftClick;
		this.keywords = new ArrayList<String>();
		this.original = original;
		this.restrictions = new ArrayList<Restriction>();
		this.stats = new ArrayList<AxStat>();
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
		item = item.clone();
		ItemMeta meta = item.getItemMeta();
		meta.setAttributeModifiers(null);
		if (meta.getPersistentDataContainer().has(LoreLengthKey,PersistentDataType.INTEGER)) meta.getPersistentDataContainer().remove(LoreLengthKey);
		for (Restriction restriction : Restrictions.getRestrictions()) restriction.remove(meta);
		item.setItemMeta(meta);
		return item;
	}
	
	@Override
	public ItemStack item(@Nullable Player player) {
		ItemStack item = super.item(player);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(makeName());
		List<Component> lore = makeLore(player);
		if (!lore.isEmpty()) meta.lore(lore);
		meta.getPersistentDataContainer().set(LoreLengthKey,PersistentDataType.INTEGER,Math.max(0,lore.size() - this.belowBottomLore.size()));
		for (AxStat stat : stats) {
			Pair<Attribute,AttributeModifier> attribute = stat.attribute();
			if (attribute != null && attribute.first() != null && attribute.second() != null) meta.addAttributeModifier(attribute.first(),attribute.second());
		}
		for (Restriction restriction : restrictions) restriction.add(meta);
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
	
	public AxItem rightClick(Consumer<Pair<AxItem,PlayerInteractEvent>> rightClick) {
		this.rightClick = rightClick;
		return this;
	}
	
	public AxItem leftClick(Consumer<Pair<AxItem,PlayerInteractEvent>> leftClick) {
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
	
	protected List<Component> makeLore(@Nullable Player player) {
		List<List<Component>> loreTemp = new ArrayList<List<Component>>();
		List<Component> statsLore = statsLore(player);
		List<Component> setLore = setLore(player);
		List<Component> bottomLore = bottomLoreFinal(player);
		List<Component> enchantmentsLore = enchantmentsLore();
		if (!topLore.isEmpty()) loreTemp.add(topLore);
		if (!statsLore.isEmpty()) loreTemp.add(statsLore);
		if (!enchantmentsLore.isEmpty()) loreTemp.add(enchantmentsLore);	// Maybe after setLore?
		if (!setLore.isEmpty()) loreTemp.add(setLore);
		if (!bottomLore.isEmpty()) loreTemp.add(bottomLore);
		List<Component> lore = new ArrayList<Component>();
		for (int i = 0; i < loreTemp.size(); i++) {
			if (i > 0) lore.add(Component.empty());
			lore.addAll(loreTemp.get(i));
		}
		if (!belowBottomLore.isEmpty()) lore.addAll(belowBottomLore);
		return lore;
	}
	
	protected List<Component> bottomLoreFinal(Player player) {
		return bottomLore();
	}
	
	protected List<Component> statsLore(Player player) {
		return stats.stream().map(stat -> stat.line()).collect(Collectors.toList());
		/*List<Component> statsLore = new ArrayList<Component>();
		for (AxStat stat : stats) statsLore.add(stat.line());
		return statsLore;*/
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
			if (ench.getValue() <= 0) continue;
			NamespacedKey key = ench.getKey().getKey();
			Component comp = Component.translatable("enchantment." + key.getNamespace() + "." + key.getKey());
			if (ench.getValue() != ench.getKey().getStartLevel() || ench.getKey().getStartLevel() != ench.getKey().getMaxLevel())
				comp = comp.append(Component.space()).append(Component.text(Utils.toRoman(ench.getValue())));
			enchantmentsLore.add(comp.color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,false));
		}
		return enchantmentsLore;
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
		if (minecraft.equalsIgnoreCase(key())) throw new IllegalArgumentException("The key: \"" + key() + "\" is illegal!!!");
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
	
	public AxItem addKeywords(String ... keywords) {
		return addKeywords(Arrays.asList(keywords));
	}
	
	public AxItem addKeywords(List<String> keywords) {
		if (keywords != null) for (String keyword : keywords) if (keyword != null) {
			keyword = keyword.toLowerCase();
			if (!this.keywords.contains(keyword)) this.keywords.add(keyword);
			if (!allKeywords.contains(keyword)) allKeywords.add(keyword);
		}
		if (!this.keywords.contains(minecraft)) this.keywords.add(minecraft);
		return this;
	}
	
	public AxItem removeKeywords(String ... keywords) {
		return removeKeywords(Arrays.asList(keywords));
	}
	
	public AxItem removeKeywords(List<String> keywords) {
		if (keywords == null) return this;
		for (String keyword : keywords) if (keyword != null && !keyword.equalsIgnoreCase(minecraft)) this.keywords.remove(keyword.toLowerCase());
		return this;
	}
	
	public List<Restriction> getRestrictions() {
		return new ArrayList<Restriction>(restrictions);
	}
	
	public AxItem addRestrictions(Restriction ... restrictions) {
		return addRestrictions(Arrays.asList(restrictions));
	}
	
	public AxItem addRestrictions(List<Restriction> restrictions) {
		this.restrictions = Utils.joinLists(this.restrictions,restrictions);
		return this;
	}
	
	public AxItem removeRestrictions(Restriction ... restrictions) {
		return removeRestrictions(Arrays.asList(restrictions));
	}
	
	public AxItem removeRestrictions(List<Restriction> restrictions) {
		if (restrictions == null) return this;
		for (Restriction restriction : restrictions) if (restriction != null) this.restrictions.remove(restriction);
		return this;
	}
	
	public List<AxStat> getStats() {
		return new ArrayList<AxStat>(stats);
	}
	
	public AxItem addStats(AxStat ... stats) {
		return addStats(Arrays.asList(stats));
	}
	
	public AxItem addStats(List<AxStat> stats) {
		this.stats = AxStats.joinStats(Utils.joinLists(this.stats,stats));
		return this;
	}
	
	public AxItem removeStats(AxStat ... stats) {
		return removeStats(Arrays.asList(stats));
	}
	
	public AxItem removeStats(List<AxStat> stats) {
		if (stats == null) return this;
		for (AxStat stat : stats) if (stat != null) this.stats.remove(stat);
		return this;
	}
	
	public AxItem removeStatTypes(AxStatType ... stats) {
		return removeStatTypes(Arrays.asList(stats));
	}
	
	public AxItem removeStatTypes(List<AxStatType> stats) {
		if (stats == null) return this;
		Iterator<AxStat> iter = this.stats.iterator();
		while (iter.hasNext()) if (stats.contains(iter.next().type())) iter.remove();
		return this;
	}
	
	public static ItemStack update(ItemStack item, @Nullable Player player) {
		try {
			return getAxItem(item).item(player);
		} catch (Exception e) {}
		return item;
	}
	
	public static AxItem getAxItem(ItemStack original) {
		return getAxItem(original,null);
	}
	
	protected static AxItem getAxItem(ItemStack original, Class<? extends AxItem> AxItemClass) {
		try {
			ItemMeta meta = original.getItemMeta();
			AxItem item;
			if (meta.getPersistentDataContainer().has(ItemKey,PersistentDataType.STRING)) {
				item = getAxItem(meta.getPersistentDataContainer().get(ItemKey,PersistentDataType.STRING));
				if (AxItemClass == null) AxItemClass = item.getClass();
				if (AxItemClass == AxItem.class) {
					if (meta.getPersistentDataContainer().has(LoreLengthKey,PersistentDataType.INTEGER) && meta.hasLore()) {
						int len = meta.getPersistentDataContainer().get(LoreLengthKey,PersistentDataType.INTEGER);
						List<Component> lore = meta.lore();
						if (len >= 0 && lore.size() > len) {
							item.belowBottomLore = new ArrayList<Component>();
							for (int i = len; i < lore.size(); i++) item.belowBottomLore.add(lore.get(i));
						}
					}
					item.addRestrictions(Restrictions.getRestrictions(meta));
					original = clearAttributes(original);
					meta = original.getItemMeta();
				} else try {
					return (AxItem) item.getClass().getDeclaredMethod("getAxItem",ItemStack.class).invoke(null,original);
				} catch (Exception e) {
					return getAxItem(original,(Class<? extends AxItem>) item.getClass().getSuperclass());
				}
			} else {
				item = getAxItem(original.getType().name());
				if (AxItemClass == null) AxItemClass = item.getClass();
				if (AxItemClass == AxItem.class) {
					item.name(meta.displayName());
					item.topLore(meta.lore());
				} else try {
					return (AxItem) item.getClass().getDeclaredMethod("getAxItem",ItemStack.class).invoke(null,original);
				} catch (Exception e) {
					return getAxItem(original,(Class<? extends AxItem>) item.getClass().getSuperclass());
				}
			}
			item.meta(meta);
			item.setAmount(original.getAmount());
			return item;
		} catch (Exception e) {}
		return null;
	}
	
	public static AxItem getAxItem(String key) {
		if (key == null) return null;
		key = key.toLowerCase();
		int idx = AxItemKeys.indexOf(key);
		if (idx >= 0) return AxItems.get(idx).clone();
		return getMaterialItem(key);
	}
	
	private static AxItem getMaterialItem(String key) {
		return getMaterialItem(Material.getMaterial(key.toUpperCase()));
	}
	
	public static AxItem getMaterialItem(Material material) {
		if (isDisabledVanilla(material)) return null;
		return new AxItem(new ItemStack(material),material.name().toLowerCase(),null).addKeywords(vanilla);
	}
	
	public static void addDisabledVanillas(String ... keys) {
		for (String key : keys) if (key != null) {
			Material material = Material.getMaterial(key.toUpperCase());
			if (material != null && !disabledVanilla.contains(material)) disabledVanilla.add(material);
		}
	}
	
	public static void addDisabledVanillas(Material ... materials) {
		addDisabledVanillas(Arrays.asList(materials));
	}
	
	public static void addDisabledVanillas(List<Material> materials) {
		for (Material material : materials) if (material != null && !disabledVanilla.contains(material)) disabledVanilla.add(material);
	}
	
	public static boolean isDisabledVanilla(String key) {
		return key != null && isDisabledVanilla(Material.getMaterial(key.toUpperCase()));
	}
	
	public static boolean isDisabledVanilla(Material material) {
		return Utils.isNull(material) || !material.isItem() || disabledVanilla.contains(material);
	}
	
	public static List<AxItem> getByKeywords(String ... keywords) {
		List<AxItem> items = new ArrayList<AxItem>();
		List<String> keys = new ArrayList<String>();
		for (String keyword : keywords) if (keyword != null) keys.add(keyword.toLowerCase());
		if (keys.isEmpty()) return items;
		for (AxItem item : getAllAxItemsIncludingMaterials()) {
			boolean add = true;
			for (String keyword : keys) if (!item.hasKeyword(keyword)) {
				add = false;
				break;
			}
			if (add) items.add(item.clone());
		}
		return items;
	}
	
	public static List<AxItem> getAllAxItems() {
		return AxItems.stream().map(item -> item.clone()).collect(Collectors.toList());
		/*List<AxItem> items = new ArrayList<AxItem>();
		for (AxItem item : AxItems) items.add(item.clone());
		return items;*/
	}
	
	public static List<AxItem> getAllMaterials() {
		List<AxItem> items = new ArrayList<AxItem>();
		for (Material material : Material.values()) {
			AxItem item = getMaterialItem(material);
			if (item != null) items.add(item);
		}
		return items;
	}
	
	public static List<AxItem> getAllAxItemsIncludingMaterials() {
		return Utils.joinLists(getAllAxItems(),getAllMaterials());
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
			item.belowBottomLore = new ArrayList<Component>(this.belowBottomLore);
			item.stats = new ArrayList<AxStat>(this.stats);
			item.restrictions = new ArrayList<Restriction>(this.restrictions);
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