package me.DMan16.AxItems.Listeners;

import me.Aldreda.AxUtils.Classes.Listener;
import me.Aldreda.AxUtils.Classes.Pair;
import me.Aldreda.AxUtils.Events.ArmorEquipEvent;
import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxItems.AxItems;
import me.DMan16.AxItems.Items.AxItem;
import me.DMan16.AxItems.Items.AxSet;
import me.DMan16.AxStats.AxStat;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AxSetListener extends Listener {
	
	public AxSetListener() {
		register(AxItems.getInstance());
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (!event.isCancelled()) updateView(event.getView());
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onArmorEquip(ArmorEquipEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		double oldMaxHP = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		double oldHP = player.getHealth();
		updatePlayer(player,event.getOldArmor(),event.getNewArmor());
		updateView(player.getOpenInventory());
		try {
			event.getOldArmor().setItemMeta(AxItem.update(event.getOldArmor(),player).getItemMeta());
		} catch (Exception e) {}
		new BukkitRunnable() {
			public void run() {
				double newMaxHP = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				if (newMaxHP > oldMaxHP && oldHP >= oldMaxHP) player.setHealth(newMaxHP);
				if (newMaxHP == oldMaxHP) player.setHealth(Math.min(Math.max(oldHP,player.getHealth()),newMaxHP));
			}
		}.runTaskLater(AxItems.getInstance(),2);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPickupItem(EntityPickupItemEvent event) {
		if (!event.isCancelled() && (event.getEntity() instanceof Player)) event.getItem().setItemStack(AxItem.update(event.getItem().getItemStack(),(Player) event.getEntity()));
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPickup(EntityPickupItemEvent event) {
		if (!event.isCancelled()) event.getItem().setItemStack(AxItem.update(event.getItem().getItemStack(),(event.getEntity() instanceof Player) ? (Player) event.getEntity() : null));
	}
	
	private void updateView(InventoryView view) {
		new BukkitRunnable() {
			public void run() {
				for (int i = 0; i < view.getTopInventory().getSize(); i++) view.setItem(i,AxItem.update(view.getItem(i),(Player) view.getPlayer()));
			}
		}.runTask(AxItems.getInstance());
	}
	
	private void updatePlayer(Player player, ItemStack oldItem, ItemStack newItem) {
		AxItem oldAxItem = AxItem.getAxItem(oldItem);
		AxItem newAxItem = AxItem.getAxItem(newItem);
		List<AxSet> oldSets = oldAxItem == null ? new ArrayList<AxSet>() : oldAxItem.getSets();
		List<AxSet> newSets = newAxItem == null ? new ArrayList<AxSet>() : newAxItem.getSets();
		HashMap<Attribute,List<AttributeModifier>> remove = new HashMap<Attribute,List<AttributeModifier>>();
		for (AxSet set : oldSets) {
			for (List<AxStat> stats : set.getStatMap().values()) {
				for (AxStat stat : stats) {
					Pair<Attribute,AttributeModifier> attribute = stat.attribute();
					Attribute att = attribute.first();
					String name = attribute.second().getName();
					try {
						for (AttributeModifier modifier : player.getAttribute(att).getModifiers()) if (modifier.getName().equals(name)) {
							if (!remove.containsKey(att)) remove.put(att, new ArrayList<AttributeModifier>());
							remove.get(att).add(modifier);
						}
					} catch (Exception e) {}
				}
			}
			newSets.remove(set);
		}
		for (AxSet set : newSets) {
			for (List<AxStat> stats : set.getStatMap().values()) {
				for (AxStat stat : stats) {
					Pair<Attribute,AttributeModifier> attribute = stat.attribute();
					Attribute att = attribute.first();
					String name = attribute.second().getName();
					try {
						for (AttributeModifier modifier : player.getAttribute(att).getModifiers()) if (modifier.getName().equals(name)) {
							if (!remove.containsKey(att)) remove.put(att, new ArrayList<AttributeModifier>());
							remove.get(att).add(modifier);
						}
					} catch (Exception e) {}
				}
			}
		}
		for (Map.Entry<Attribute,List<AttributeModifier>> entry : remove.entrySet()) for (AttributeModifier mod : entry.getValue()) player.getAttribute(entry.getKey()).removeModifier(mod);
		new BukkitRunnable() {
			public void run() {
				List<AxStat> add = new ArrayList<AxStat>();
				for (AxSet set : oldSets) add.addAll(set.getStats(player));
				for (AxSet set : newSets) add.addAll(set.getStats(player));
				for (AxStat stat : add) {
					Pair<Attribute,AttributeModifier> attribute = stat.attribute();
					player.getAttribute(attribute.first()).addModifier(attribute.second());
				}
				ItemStack[] armor = player.getEquipment().getArmorContents();
				for (int i = 0; i < armor.length; i++) armor[i] = AxItem.update(armor[i],player);
				player.getEquipment().setArmorContents(armor);
				player.setItemOnCursor(AxItem.update(player.getItemOnCursor(),player));
				for (int i : Utils.getPlayerInventorySlots()) Utils.setItemSlot(player,AxItem.update(Utils.getFromSlot(player,i),player),i);
			}
		}.runTask(AxItems.getInstance());
	}
}