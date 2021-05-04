package me.DMan16.AxItems.Listeners;

import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.Aldreda.AxUtils.Classes.Listener;
import me.Aldreda.AxUtils.Classes.Pair;
import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxItems.AxItems;
import me.DMan16.AxItems.Items.AxItem;

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
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (item.rightClick != null) item.rightClick.accept(Pair.of(item,event));
		} else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (item.leftClick != null) item.leftClick.accept(Pair.of(item,event));
		}
	}
}