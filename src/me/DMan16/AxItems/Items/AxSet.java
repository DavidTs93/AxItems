package me.DMan16.AxItems.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import me.DMan16.AxStats.AxStat;

public class AxSet implements Iterable<AxItem>,Cloneable {
	private static HashMap<String,AxSet> sets = new HashMap<String,AxSet>();
	
	private final String name;
	private List<AxStat> stats;
	private List<String> keys;
	
	public AxSet(@Nullable String name, List<AxStat> stats, String ... keys) {
		this(name,stats,Arrays.asList(keys));
	}
	
	public AxSet(@Nullable String name, List<AxStat> stats, List<String> keys) {
		if (name != null) name = name.toLowerCase();
		this.name = name;
		this.stats = new ArrayList<AxStat>();
		for (AxStat stat : Objects.requireNonNull(Objects.requireNonNull(stats,"Set stats cannot be null!").size() <= 0 ? null : stats,"Set stats cannot be empty!"))
			if (Objects.requireNonNull(stat,"Set stat cannot be null!").val1() != 0) this.stats.add(stat);
		if (this.stats.isEmpty()) throw new NullPointerException("Set stats cannot be empty!");
		this.keys = new ArrayList<String>();
		for (String key : Objects.requireNonNull(Objects.requireNonNull(keys).size() < 2 ? null : keys,"Set must contain at least 2 items!")) {
			AxItem item = AxItem.getAxItem(Objects.requireNonNull(key,"Set items cannot be null!"));
			this.keys.add(Objects.requireNonNull(this.keys.contains(Objects.requireNonNull(Objects.requireNonNull(item,"Set item not found!").hasKeyword("vanilla") ? null : key,
					"Set items cannot be vanilla!")) ? null : key,"Set cannot contain the same item twice!"));
		}
	}
	
	public List<AxItem> items() {
		List<AxItem> items = new ArrayList<AxItem>();
		for (String key : keys) items.add(AxItem.getAxItem(key));
		return items;
	}
	
	@Override
	public Iterator<AxItem> iterator() {
		Iterator<AxItem> iter = new Iterator<AxItem>() {
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
		return iter;
	}
	
	public String name() {
		return name;
	}
	
	public List<AxStat> getStats() {
		return new ArrayList<AxStat>(stats);
	}
	
	/**
	 * IMPORTANT!!!
	 * Once a Set has been registered its registered form can no longer be changed!!!
	 */
	public AxSet register() {
		sets.put(Objects.requireNonNull(sets.containsKey(Objects.requireNonNull(name())) ? null :
			Objects.requireNonNull(name(),"Set key cannot be NULL!"),"The key: \"" + name() + "\" is already being used!"),this.getClass().cast(clone()));
		for (String key : keys) AxItem.getAxItemOriginal(key).addKeywords(Arrays.asList("set","set_" + name));
		return this;
	}
	
	public static AxSet getAxSet(String name) {
		if (name == null) return null;
		AxSet set = sets.get(name.toLowerCase());
		return set.clone();
	}
	
	public static Set<String> getAllSetNames() {
		return sets.keySet();
	}
	
	@Override
	public AxSet clone() {
		try {
			AxSet set = this.getClass().cast(super.clone());
			set.keys = new ArrayList<String>(this.keys);
			set.stats = new ArrayList<AxStat>(this.stats);
			return set;
		} catch (Exception e) {}
		return null;
	}
}