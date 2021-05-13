package me.DMan16.AxItems.Listeners;

import me.Aldreda.AxUtils.Classes.Listener;
import me.Aldreda.AxUtils.Classes.Pair;
import me.Aldreda.AxUtils.Events.ArmorEquipEvent;
import me.DMan16.AxItems.AxItems;
import me.DMan16.AxItems.Items.AxItem;
import me.DMan16.AxItems.Items.AxSet;
import me.DMan16.AxStats.AxStat;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

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
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onInventoryOpen(ArmorEquipEvent event) {
		if (event.isCancelled()) return;
		updateView(event.getPlayer().getOpenInventory());
		updatePlayer(event.getPlayer());
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		updatePlayer(event.getPlayer());
	}
	
	private void updateView(InventoryView view) {
		new BukkitRunnable() {
			public void run() {
				for (int i = 0; i < view.getTopInventory().getSize(); i++) view.setItem(i,AxItem.update(view.getItem(i),(Player) view.getPlayer()));
			}
		}.runTask(AxItems.getInstance());
	}
	
	private void updatePlayer(Player player) {
		new BukkitRunnable() {
			public void run() {
				player.setItemOnCursor(AxItem.update(player.getItemOnCursor(),player));
				for (int i = 0; i < player.getInventory().getSize(); i++) player.getInventory().setItem(i,AxItem.update(player.getInventory().getItem(i),player));
				ItemStack[] armor = player.getEquipment().getArmorContents();
				for (int i = 0; i < armor.length; i++) armor[i] = AxItem.update(armor[i],player);
				player.getEquipment().setArmorContents(armor);
				player.getInventory().setItemInOffHand(AxItem.update(player.getInventory().getItemInOffHand(),player));
				HashMap<AxSet,List<AxItem>> sets = AxSet.getEquippedSets(player);
				if (sets != null && !sets.isEmpty()) {
					for (Map.Entry<AxSet,List<AxItem>> set : sets.entrySet()) {
						int count = set.getValue().size();
						for (Map.Entry<Integer,List<AxStat>> stats : set.getKey().getStatMap().entrySet()) {
							int amount = stats.getKey();
							for (AxStat stat : stats.getValue()) {
								Pair<Attribute,AttributeModifier> attribute = stat.attribute();
								try {
									AttributeModifier remove = null;
									for (AttributeModifier modifier : player.getAttribute(attribute.first()).getModifiers()) if (modifier.getName().equals(attribute.second().getName())) {
										remove = modifier;
										break;
									}
									if (count >= amount) {
										if (remove == null) player.getAttribute(attribute.first()).addModifier(attribute.second());
									} else {
										if (remove != null) player.getAttribute(attribute.first()).removeModifier(remove);
									}
								} catch (Exception e) {}
							}
						}
					}
				}
			}
		}.runTask(AxItems.getInstance());
	}
}