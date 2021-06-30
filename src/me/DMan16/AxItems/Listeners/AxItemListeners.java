package me.DMan16.AxItems.Listeners;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import me.Aldreda.AxUtils.Classes.Listener;
import me.Aldreda.AxUtils.Classes.Pair;
import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxItems.AxItems;
import me.DMan16.AxItems.Items.AxItem;
import me.DMan16.AxItems.Items.AxItemPerishable;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AxItemListeners extends Listener {
	
	public AxItemListeners() {
		register(AxItems.getInstance());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onAxItemClick(PlayerInteractEvent event) {
		if (event.useInteractedBlock() == Result.DENY && event.useItemInHand() == Result.DENY) return;
		if (!event.hasItem() || Utils.isNull(event.getItem()) || event.getAction() == Action.PHYSICAL) return;
		AxItem item = AxItem.getAxItem(event.getItem());
		if (item == null) return;
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) item.rightClick(Pair.of(item,event));
		else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) item.leftClick(Pair.of(item,event));
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onAxItemEnchant(EnchantItemEvent event) {
		if (event.isCancelled()) return;
		event.getItem().setItemMeta(AxItem.getAxItem(event.getItem()).addEnchantments(Pair.fromMap(event.getEnchantsToAdd())).item((Player) event.getView().getPlayer()).getItemMeta());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onAxItemPrepare(PrepareResultEvent event) {
		event.setResult(AxItem.update(event.getResult(),(Player) event.getView().getPlayer()));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void fixCreativeItemsEvent(InventoryClickEvent event) {
		if (event.isCancelled() || !(event.getWhoClicked() instanceof Player) || !(event.getInventory() instanceof CraftingInventory) || event.getView().getType() != InventoryType.CREATIVE) return;
		Player player = (Player) event.getWhoClicked();
		if (player.getGameMode() != GameMode.CREATIVE) return;
		ItemStack item = event.getCursor();
		new BukkitRunnable() {
			public void run() {
				try {
					if (AxItem.getAxItem(item) == null) try {
						event.getClickedInventory().setItem(event.getSlot(),AxItem.getLegal(item).item(player));
					} catch (Exception e) {
						event.getClickedInventory().setItem(event.getSlot(),null);
					} else event.getClickedInventory().setItem(event.getSlot(),AxItem.update(item,player));
				} catch (Exception e) {}
			}
		}.runTask(AxItems.getInstance());
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void changeCreativeItemDropEvent(PlayerDropItemEvent event) {
		if (event.isCancelled() || event.getPlayer().getGameMode() != GameMode.CREATIVE) return;
		Item drop = event.getItemDrop();
		ItemStack item = event.getItemDrop().getItemStack();
		if (Utils.isNull(item)) return;
		if (AxItem.getAxItem(item) == null) try {
			drop.setItemStack(AxItem.getLegal(item).item(event.getPlayer()));
		} catch (Exception e) {
			drop.remove();
		} else drop.setItemStack(AxItem.update(item,event.getPlayer()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void fixLootGenerateItemsEvent(LootGenerateEvent event) {
		if (event.getLoot() == null || event.getLoot().isEmpty()) return;
		List<ItemStack> loot = new ArrayList<ItemStack>();
		ItemStack item;
		for (int i = 0; i < event.getLoot().size(); i++) {
			item = event.getLoot().get(i);
			if (Utils.isNull(item)) continue;
			if (AxItem.getAxItem(item) == null) {
				try {
					loot.add(AxItem.getLegal(item).item(null));
				} catch (Exception e) {}
			}
		}
		event.setLoot(loot);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void disableGiveArmorsCommandEvent(PlayerCommandPreprocessEvent event) {
		String cmd = event.getMessage().toLowerCase().replace("/","");
		String command = cmd.split(" ")[0];
		if (command.contains(":")) command = command.split(":")[1];
		//String[] cmdSplit = cmd.split(" ");
		if (/*cmdSplit.length >= 3 && */command.equals("give")) {
			event.setCancelled(true);
			Utils.chatColors(event.getPlayer(),"&cCommand disabled. Use &f/AxItems &cinstead.");
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void customDurabilityEvent(PlayerItemBreakEvent event) {
		ItemStack original = event.getBrokenItem();
		if (event.getPlayer() == null || Utils.isNull(original)) return;
		Damageable meta = (Damageable) original.getItemMeta();
		meta.setDamage(0);
		original.setItemMeta((ItemMeta) meta);
		AxItem item = AxItem.getAxItem(original);
		if (item != null && (item instanceof AxItemPerishable)) Utils.givePlayer(event.getPlayer(),item.item(event.getPlayer()),false);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void customDurabilityEvent(PlayerItemDamageEvent event) {
		int dmg = event.getDamage();
		if (event.isCancelled() || dmg <= 0 || event.getPlayer() == null) return;
		AxItemPerishable item = AxItemPerishable.getAxItem(event.getItem());
		if (item == null || item.isUnbreakable) return;
		item.damage(dmg);
		if (item.isBroken()) event.setDamage(event.getItem().getType().getMaxDurability());
		else {
			event.setCancelled(true);
			event.setDamage(0);
			event.getItem().setItemMeta(item.item(event.getPlayer()).getItemMeta());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void disableGrindstoneRepairPerishableEvent(InventoryClickEvent event) {
		if (event.isCancelled() || event.getInventory().getType() != InventoryType.GRINDSTONE) return;
		new BukkitRunnable() {
			public void run() {
				ItemStack item1 = event.getInventory().getItem(0);
				ItemStack item2 = event.getInventory().getItem(1);
				ItemStack result = event.getInventory().getItem(2);
				if (Utils.isNull(result) || (Utils.isNull(item1) && Utils.isNull(item2))) return;
				AxItem item = AxItem.getAxItem(result);
				if (item == null) return;
				if (Utils.isNull(item1) || Utils.isNull(item2)) event.getInventory().setItem(2,item.item((Player) event.getWhoClicked()));
			}
		}.runTask(AxItems.getInstance());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void fixPerishableAnvilEvent(PrepareAnvilEvent event) {
		if (event.getViewers().isEmpty()) return;
		Player player = (Player) event.getView().getPlayer();
		new BukkitRunnable() {
			public void run() {
				AxItemPerishable item1 = AxItemPerishable.getAxItem(event.getInventory().getItem(0));
				AxItemPerishable result = AxItemPerishable.getAxItem(event.getInventory().getItem(2));
				if (item1 == null || result == null) return;
				if (Utils.isNull(event.getInventory().getItem(1))) {
					if (result != null) event.getInventory().setItem(2,result.item(player));
				} else if (result.repairItemKey != null){
					AxItem item2 = AxItem.getAxItem(event.getInventory().getItem(1));
					if (item2 != null && result.repairItemKey.equals(item2.key())) {
						int amount = item1.itemMaterialAmountToFull();
						amount = Math.min(amount,item2.getAmount());
						if (amount > 0) {
							result.repairWithMaterial(amount);
							event.getInventory().setItem(2,result.item(player));
							player.updateInventory();
						}
					}
				}
			}
		}.runTask(AxItems.getInstance());
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void clickAnvilArmorPerishableEvent(InventoryClickEvent event) {
		if (event.isCancelled() || event.getInventory().getType() != InventoryType.ANVIL) return;
		AxItemPerishable item1 = AxItemPerishable.getAxItem(event.getInventory().getItem(0));
		if (item1 == null) return;
		ItemStack item2 = event.getInventory().getItem(1);
		if (!Utils.isNull(item2) && item1.repairItemKey != null) new BukkitRunnable() {
			public void run() {
				if (event.isCancelled()) return;
				if (item1.repairItemKey.equals(AxItem.getAxItem(item2).key()))
					Utils.uniqueCraftingHandle(event,Math.min(item2.getAmount(),item1.itemMaterialAmountToFull()),1);
				else if (item2.getType() == Material.ENCHANTED_BOOK) Utils.uniqueCraftingHandle(event,1,1);
			}
		}.runTask(AxItems.getInstance());
	}
}