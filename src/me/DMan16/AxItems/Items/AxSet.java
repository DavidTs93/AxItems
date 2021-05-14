package me.DMan16.AxItems.Items;

import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxStats.AxStat;
import me.DMan16.AxStats.AxStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AxSet implements Iterable<AxItem> {
	private static final List<String> AxSetKeys = new ArrayList<String>();
	private static final List<AxSet> AxSets = new ArrayList<AxSet>();
	private static final String translatableSet = "item.aldreda.set.";
	private static final TextColor defaultColor = NamedTextColor.GREEN;
	private static final HashMap<String,List<String>> waiting = new HashMap<String,List<String>>();

	private final String key;
	private final TextColor color;
	private final SortedMap<Integer,List<AxStat>> stats;
	private final List<String> keys;

	/**
	 * @param color Cannot be DARK_GRAY, GRAY, or WHITE - null will be used instead
	 */
	public AxSet(@Nullable String key, @Nullable TextColor color,SortedMap<Integer,List<AxStat>> statMap, String ... keys) {
		this(key,color,statMap,Arrays.asList(keys));
	}
	
	public AxSet(@Nullable String key, @Nullable TextColor color,SortedMap<Integer,List<AxStat>> statMap, List<String> keys) {
		if (key != null) key = key.toLowerCase();
		this.key = key;
		if (color == null || color.compareTo(NamedTextColor.GRAY) == 0 || color.compareTo(NamedTextColor.WHITE) == 0 || color.compareTo(NamedTextColor.DARK_GRAY) == 0) this.color = null;
		else this.color = color;
		this.stats = new TreeMap<Integer,List<AxStat>>();
		this.keys = new ArrayList<String>();
		setStatMap(statMap).addKeys(keys);
	}
	
	private AxSet addKeys(List<String> keys) {
		if (keys != null) for (String itemKey : keys) if (itemKey != null && !this.keys.contains(itemKey)) {
			AxItem item = AxItem.getAxItem(itemKey);
			if (item != null && !item.hasKeyword("vanilla")) {
				this.keys.add(itemKey);
				if (isRegistered()) AxItem.addToSets(item.key(),this);
			}
		}
		return this;
	}
	
	public List<AxItem> items() {
		List<AxItem> items = new ArrayList<AxItem>();
		for (String key : keys) items.add(AxItem.getAxItem(key));
		return items;
	}
	
	@Override
	public Iterator<AxItem> iterator() {
		return new Iterator<AxItem>() {
			private int currentIndex = 0;

			@Override
			public boolean hasNext() {
				return currentIndex < keys.size();
			}

			@Override
			public AxItem next() {
				return AxItem.getAxItem(keys.get(currentIndex++));
			}

			@Override
			@Deprecated
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public String key() {
		return key;
	}

	public TextColor color() {
		return color == null ? defaultColor : color;
	}

	public List<Component> lore(@NotNull Player player) {
		List<Component> lore = new ArrayList<Component>();
		int setMax = size();
		SortedMap<Integer,List<AxStat>> statMap = getStatMap();
		List<AxItem> equipped = getEquipped(player);
		int setEquipped = equipped.size();
		TextColor equippedColor = setEquipped == 0 ? NamedTextColor.DARK_GRAY : (setEquipped >= setMax ? color() : NamedTextColor.WHITE);
		lore.add(Component.translatable(translatableSet + key).color(color()).append(Component.text(" (").color(NamedTextColor.GRAY)).append(
				Component.text(setEquipped).color(equippedColor)).append(Component.text("/").color(NamedTextColor.GRAY)).append(Component.text(setMax).color(color())).append(
						Component.text(")").color(NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC,false));
		AxItem foundItem;
		for (String key : keys) {
			foundItem = null;
			for (AxItem item : equipped) if (item.key().equals(key)) {
				foundItem = item;
				break;
			}
			lore.add(Component.text("  - ").color(foundItem == null ? NamedTextColor.GRAY : color()).append(foundItem == null ? AxItem.getAxItem(key).name().color(NamedTextColor.GRAY) :
					foundItem.name()));
		}
		if (setEquipped > 0 && setEquipped >= statMap.firstKey()) {
			List<AxStat> statList = new ArrayList<AxStat>();
			for (Map.Entry<Integer,List<AxStat>> stats : statMap.entrySet()) {
				if (stats.getKey() > setEquipped) break;
				else {
					List<AxStat> add = new ArrayList<AxStat>();
					for (AxStat stat : stats.getValue()) {
						ListIterator<AxStat> iter = statList.listIterator();
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
					statList.addAll(add);
				}
			}
			if (!statList.isEmpty()) {
				lore.add(Component.empty());
				for (AxStat stat : statList) lore.add(Component.text("  ").append(stat.line()));
			}
		}
		return lore;
	}

	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	public List<AxItem> getEquipped(@NotNull Player player) {
		if (player == null) return null;
		Set<String> equippedTemp = new HashSet<String>();
		List<AxItem> equipped = new ArrayList<AxItem>();
		for (ItemStack armor : player.getEquipment().getArmorContents()) {
			if (Utils.isNull(armor)) continue;
			AxItem item = AxItem.getAxItem(armor);
			if (contains(item)) if (equippedTemp.add(item.key())) equipped.add(item);
		}
		return equipped;
	}
	
	public static HashMap<AxSet,List<AxItem>> getEquippedSets(@NotNull Player player) {
		if (player == null) return null;
		HashMap<AxSet,List<AxItem>> sets = new HashMap<AxSet,List<AxItem>>();
		for (AxSet set : AxSets) {
			List<AxItem> equipped = set.getEquipped(player);
			if (!equipped.isEmpty()) sets.put(set,equipped);
		}
		return sets;
	}
	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	public boolean isRegistered() {
		return key() != null && AxSetKeys.contains(key()) && AxSets.contains(this);
	}

	public int size() {
		return keys.size();
	}
	
	public List<AxStat> getStats(@NotNull Player player) {
		if (player == null) return null;
		int amount = getEquipped(player).size();
		return getStats(amount);
	}
	
	public List<AxStat> getStats(int amount) {
		List<AxStat> stats = new ArrayList<AxStat>();
		for (Map.Entry<Integer,List<AxStat>> stat : this.stats.entrySet())
			if (stat.getKey() > amount) break;
			else stats.addAll(stat.getValue());
		return AxStats.joinStats(stats);
	}
	
	public SortedMap<Integer,List<AxStat>> getStatMap() {
		SortedMap<Integer,List<AxStat>> stats = new TreeMap<Integer,List<AxStat>>();
		for (Map.Entry<Integer,List<AxStat>> stat : this.stats.entrySet()) stats.put(stat.getKey(), new ArrayList<AxStat>(stat.getValue()));
		return stats;
	}
	
	public AxSet setStatMap(SortedMap<Integer,List<AxStat>> statMap) {
		this.stats.clear();
		if (statMap != null) for (Map.Entry<Integer,List<AxStat>> stats : statMap.entrySet()) {
			List<AxStat> list = new ArrayList<AxStat>();
			if (stats.getKey() == null || stats.getKey() < 1 || stats.getValue() == null) continue;
			for (AxStat stat : stats.getValue()) if (stat.val1() != 0) list.add(stat);
			if (!list.isEmpty()) this.stats.put(stats.getKey(),list);
		}
		return this;
	}
	
	public boolean contains(AxItem item) {
		return item != null && item.key() != null && keys.contains(item.key());
	}

	/**
	 * IMPORTANT!!!
	 * Once a Set has been registered its registered form can no longer be changed!!!
	 */
	public AxSet register() {
		Objects.requireNonNull(key(),"Set key cannot be NULL!");
		if (AxSetKeys.contains(key())) throw new IllegalArgumentException("The key: \"" + key() + "\" is already being used!");
		AxSetKeys.add(key());
		AxSets.add(this);
		if (waiting.containsKey(key())) {
			addKeys(waiting.get(key()));
			waiting.remove(key());
		}
		return this;
	}
	
	public static AxSet getAxSet(String key) {
		if (key == null) return null;
		int idx = AxSetKeys.indexOf(key.toLowerCase());
		if (idx >= 0) return AxSets.get(idx);
		return null;
	}
	
	public static void addWhenPossible(@NotNull String setKey, String ... itemKeys) {
		if (setKey == null) return;
		setKey = setKey.toLowerCase();
		int idx = AxSetKeys.indexOf(setKey);
		if (idx < 0) {
			if (!waiting.containsKey(setKey)) waiting.put(setKey, new ArrayList<String>());
			for (String itemKey : itemKeys) if (!waiting.get(setKey).contains(itemKey)) waiting.get(setKey).add(itemKey);
		} else AxSets.get(idx).addKeys(Arrays.asList(itemKeys));
	}
	
	public static List<String> getAllSetNames() {
		return new ArrayList<String>(AxSetKeys);
	}
}