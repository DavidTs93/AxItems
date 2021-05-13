package me.DMan16.AxItems;

import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxItems.Items.AxItem;
import me.DMan16.AxItems.Listeners.AxItemListeners;
import me.DMan16.AxItems.Listeners.AxSetListener;
import me.DMan16.AxItems.Listeners.CommandListener;
import me.DMan16.AxItems.Listeners.CommandListenerPerishable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class AxItems extends JavaPlugin {
	private static AxItems instance = null;
	
	public void onEnable() {
		instance = this;
		new CommandListener();
		new CommandListenerPerishable();
		new AxItemListeners();
		new AxSetListener();
		AxItem.addDisabledVanillas(Material.AIR);
		Utils.chatColorsLogPlugin("&fAxItems &aloaded!");
	}
	
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		Utils.chatColorsLogPlugin("&fAxItems &adisabed");
	}
	
	public static FileConfiguration config() {
		return instance.getConfig();
	}
	
	public static AxItems getInstance() {
		return instance;
	}
}