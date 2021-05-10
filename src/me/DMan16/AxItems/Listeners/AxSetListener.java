package me.DMan16.AxItems.Listeners;

import me.Aldreda.AxUtils.Classes.Listener;
import me.Aldreda.AxUtils.Events.ArmorEquipEvent;
import me.DMan16.AxItems.AxItems;
import me.DMan16.AxItems.Items.AxItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class AxSetListener extends Listener {
	
	public AxSetListener() {
		register(AxItems.getInstance());
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (!event.isCancelled()) updateView(event.getView());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onInventoryOpen(ArmorEquipEvent event) {
		if (event.isCancelled()) return;
		updateView(event.getPlayer().getOpenInventory());
		updatePlayer(event.getPlayer());
	}
	
	private void updateView(InventoryView view) {
		new BukkitRunnable() {
			public void run() {
				for (int i = 0; i < view.getTopInventory().getSize(); i++) view.setItem(i,AxItem.updateItem(view.getItem(i),(Player) view.getPlayer()));
			}
		}.runTask(AxItems.getInstance());
	}
	
	private void updatePlayer(Player player) {
		new BukkitRunnable() {
			public void run() {
				for (int i = 0; i < player.getInventory().getSize(); i++) player.getInventory().setItem(i,AxItem.updateItem(player.getInventory().getItem(i),player));
				ItemStack[] armor = player.getEquipment().getArmorContents();
				for (int i = 0; i < armor.length; i++) armor[i] = AxItem.updateItem(armor[i],player);
				player.getEquipment().setArmorContents(armor);
				player.getInventory().setItemInOffHand(AxItem.updateItem(player.getInventory().getItemInOffHand(),player));
			}
		}.runTask(AxItems.getInstance());
	}
}