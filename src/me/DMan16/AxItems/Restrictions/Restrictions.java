package me.DMan16.AxItems.Restrictions;

import me.Aldreda.AxUtils.Classes.Listener;
import me.Aldreda.AxUtils.Events.ArmorEquipEvent;
import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxItems.AxItems;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Restrictions {
	public static Restriction Unequippable = new Unequippable();
	public static Restriction Unplaceable = new Unplaceable();
	public static Restriction Undroppable = new Undroppable();
	public static Restriction Unenchantable = new Unenchantable();
	public static Restriction Uncraftable = new Uncraftable();
	public static Restriction DropRemove = new DropRemove();
	public static Restriction RecipeRemove = new RecipeRemove();
	public static Restriction RecipeMust = new RecipeMust();
	
	private static List<Restriction> restrictions = Arrays.asList(Unequippable,Unplaceable,Undroppable,Unenchantable,Uncraftable,DropRemove,RecipeRemove,RecipeMust);
	
	public static List<Restriction> getRestrictions() {
		return restrictions;
	}
	
	public static List<Restriction> getRestrictions(ItemStack item) {
		return getRestrictions(Utils.isNull(item) ? null : item.getItemMeta());
	}
	
	public static List<Restriction> getRestrictions(ItemMeta meta) {
		if (meta == null) return null;
		List<Restriction> restrictions = new ArrayList<Restriction>();
		for (Restriction restriction : Restrictions.restrictions) if (restriction.is(meta)) restrictions.add(restriction);
		return restrictions;
	}
	
	public static abstract class Restriction extends Listener {
		private final NamespacedKey key;
		
		private Restriction(String key) {
			this.key = new NamespacedKey(AxItems.getInstance(),"axitem_restriction_" + key);
			register(AxItems.getInstance());
		}
		
		public ItemMeta add(ItemMeta meta) {
			if (meta != null) meta.getPersistentDataContainer().set(key,PersistentDataType.STRING,"");
			return meta;
		}
		
		public ItemStack add(ItemStack item) {
			if (Utils.isNull(item)) return item;
			item.setItemMeta(add(item.getItemMeta()));
			return item;
		}
		
		public ItemMeta remove(ItemMeta meta) {
			if (is(meta)) meta.getPersistentDataContainer().remove(key);
			return meta;
		}
		
		public ItemStack remove(ItemStack item) {
			if (Utils.isNull(item)) return item;
			item.setItemMeta(remove(item.getItemMeta()));
			return item;
		}
		
		public boolean is(ItemStack item) {
			if (Utils.isNull(item)) return false;
			ItemMeta meta = item.getItemMeta();
			return is(meta);
		}
		
		private boolean is(ItemMeta meta) {
			if (meta == null) return false;
			return meta.getPersistentDataContainer().has(key,PersistentDataType.STRING);
		}
		
		protected boolean restrictionEvent(Event event, ItemStack item, HumanEntity player, boolean cancelIfPossible) {
			ItemRestrictedEvent restrictionEvent = new ItemRestrictedEvent(this,item);
			restrictionEvent.callEvent();
			if (restrictionEvent.isCancelled()) return false;
			if (cancelIfPossible && (event instanceof Cancellable)) ((Cancellable) event).setCancelled(true);
			if (restrictionEvent.getCancelMSG() != null) player.sendMessage(restrictionEvent.getCancelMSG());
			return true;
		}
	}
	
	private static class Unequippable extends Restriction {
		public Unequippable() {
			super("unequippable");
		}
		
		@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
		public void onEquip(ArmorEquipEvent event) {
			if (event.isCancelled() || !is(event.getNewArmor())) return;
			restrictionEvent(event,event.getNewArmor(),event.getPlayer(),true);
		}
	}
	
	private static class Unplaceable extends Restriction {
		public Unplaceable() {
			super("unplaceable");
		}
		
		@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
		public void onPlace(BlockPlaceEvent event) {
			if (event.isCancelled() || !is(event.getItemInHand())) return;
			restrictionEvent(event,event.getItemInHand(),event.getPlayer(),true);
		}
	}
	
	private static class Undroppable extends Restriction {
		public Undroppable() {
			super("undroppable");
		}
		
		@EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
		public void onDropItemMainMenu(PlayerDropItemEvent event) {
			if (event.isCancelled() || !is(event.getItemDrop().getItemStack())) return;
			restrictionEvent(event,event.getItemDrop().getItemStack(),event.getPlayer(),true);
		}
	}
	
	private static class Unenchantable extends Restriction {
		public Unenchantable() {
			super("unenchantable");
		}
		
		@EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
		public void onEnchantItem(EnchantItemEvent event) {
			if (event.isCancelled() || !is(event.getItem())) return;
			restrictionEvent(event,event.getItem(),event.getEnchanter(),true);
		}
	}
	
	private static class Uncraftable extends Restriction {
		public Uncraftable() {
			super("uncraftable");
		}
		
		@EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
		public void onCraftItem(PrepareItemCraftEvent event) {
			if (event.getRecipe() == null || Utils.isNull(event.getInventory().getResult())) return;
			for (ItemStack item : event.getInventory().getMatrix()) if (is(item)) if (restrictionEvent(event,item,event.getView().getPlayer(),true)) {
				event.getInventory().setResult(null);
				break;
			}
		}
	}
	
	private static class DropRemove extends Restriction {
		public DropRemove() {
			super("drop_remove");
		}
		
		@EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
		public void onDropItemMainMenu(PlayerDropItemEvent event) {
			if (!event.isCancelled() && is(event.getItemDrop().getItemStack())) event.getItemDrop().remove();
		}
	}
	
	private static class RecipeRemove extends Restriction {
		public RecipeRemove() {
			super("recipe_remove");
		}
		
		@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
		public void onCraftItem(CraftItemEvent event) {
			if (event.isCancelled() || event.getRecipe() == null || !is(event.getRecipe().getResult())) return;
			if (restrictionEvent(event,event.getRecipe().getResult(),event.getWhoClicked(),false)) new BukkitRunnable() {
				public void run() {
					event.getWhoClicked().undiscoverRecipe((event.getRecipe() instanceof ShapedRecipe) ? ((ShapedRecipe) event.getRecipe()).getKey() :
							((ShapelessRecipe) event.getRecipe()).getKey());
				}
			}.runTask(AxItems.getInstance());
		}
	}
	
	private static class RecipeMust extends Restriction {
		public RecipeMust() {
			super("recipe_must");
		}
		
		@EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
		public void onCraftItem(PrepareItemCraftEvent event) {
			if (event.getRecipe() == null || !is(event.getInventory().getResult())) return;
			if (!event.getView().getPlayer().hasDiscoveredRecipe((event.getRecipe() instanceof ShapedRecipe) ? ((ShapedRecipe) event.getRecipe()).getKey() :
					((ShapelessRecipe) event.getRecipe()).getKey()))
				if (restrictionEvent(event,event.getRecipe().getResult(),event.getView().getPlayer(),true)) event.getInventory().setResult(null);
		}
	}
}