package me.DMan16.AxItems.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

public class AxSet implements Iterable<AxItem>,Cloneable {
	private static HashMap<String,AxSet> sets = new HashMap<String,AxSet>();
	
	private final String name;
	private List<AxItem> items;
	
	public AxSet(@Nullable String name, String ... itemNames) {
		if (name != null) name = name.toLowerCase();
		this.name = name;
		this.items = new ArrayList<AxItem>();
		for (String itemName : Objects.requireNonNull(itemNames.length < 2 ? null : itemNames,"Set must contain at least 2 items!")) {
			AxItem item = AxItem.getAxItem(Objects.requireNonNull(itemName,"Set items cannot be null!"));
			items.add(Objects.requireNonNull(Objects.requireNonNull(item,"Set items cannot be null!").hasKeyword("vanilla") ? null : item,"Set items cannot be vanilla!"));
			if (name != null) AxItem.getAxItemOriginal(itemName).addKeywords("set","set_" + name);
		}
	}
	
	public List<AxItem> items() {
		return new ArrayList<AxItem>(items);
	}
	
	@Override
	public Iterator<AxItem> iterator() {
		Iterator<AxItem> iter = new Iterator<AxItem>() {
			private int currentIndex = 0;
			
			@Override
			public boolean hasNext() {
				return currentIndex < items.size();
			}
			
			@Override
			public AxItem next() {
				return items.get(currentIndex++);
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
	
	/**
	 * IMPORTANT!!!
	 * Once a Set has been registered its registered form can no longer be changed!!!
	 */
	public AxSet register() {
		sets.put(Objects.requireNonNull(sets.containsKey(Objects.requireNonNull(name())) ? null :
			Objects.requireNonNull(name(),"Set key cannot be NULL!"),"The key: \"" + name() + "\" is already being used!"),this.getClass().cast(clone()));
		return this;
	}
	
	public static Set<AxSet> getAxSet(AxItem item) {
		if (!item.hasKeyword("set")) return null;
		Set<AxSet> sets = new HashSet<AxSet>();
		for (String keyword : item.getKeywords()) if (keyword.startsWith("set_")) {
			AxSet set = getAxSet(keyword.replace("set_",""));
			if (set != null) sets.add(set);
		}
		return sets.isEmpty() ? null : sets;
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
			set.items = new ArrayList<AxItem>(this.items);
			return set;
		} catch (Exception e) {}
		return null;
	}
}