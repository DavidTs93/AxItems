package me.DMan16.AxItems.Restrictions;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import me.DMan16.AxItems.Restrictions.Restrictions.RestrictionType;
import net.kyori.adventure.text.Component;

public final class ItemRestrictedEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancel;
	public final RestrictionType type;
	public final ItemStack item;
	private Component cancelMSG = null;
	
	public ItemRestrictedEvent(RestrictionType type, ItemStack item) {
		this.type = type;
		this.item = item.clone();
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public final HandlerList getHandlers() {
		return handlers;
	}
	
	public final void setCancelled(final boolean cancel) {
		this.cancel = cancel;
	}
	
	public final boolean isCancelled() {
		return cancel;
	}
	
	public Component getCancelMSG() {
		return cancelMSG;
	}
	
	public void setCancelMSG(Component msg) {
		cancelMSG = msg;
	}
}