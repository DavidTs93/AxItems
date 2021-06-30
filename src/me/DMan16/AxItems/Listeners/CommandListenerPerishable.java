package me.DMan16.AxItems.Listeners;

import me.DMan16.AxItems.AxItems;
import me.DMan16.AxItems.Items.AxItemPerishable;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandListenerPerishable implements CommandExecutor, TabCompleter {
	public CommandListenerPerishable() {
		PluginCommand command = AxItems.getInstance().getCommand("axitemsperishable");
		command.setExecutor(this);
		command.setTabCompleter(this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || args.length < 1) return false;
		Player player = (Player) sender;
		if (args[0].equalsIgnoreCase("damage")) {
			try {
				AxItemPerishable item = AxItemPerishable.getAxItem(player.getInventory().getItemInMainHand());
				int amount = Integer.parseInt(args[1]);
				player.getInventory().setItemInMainHand(item.damage(amount).item(player));
			} catch (Exception e) {}
		} else if (args[0].equalsIgnoreCase("repair")) {
			try{
				AxItemPerishable item = AxItemPerishable.getAxItem(player.getInventory().getItemInMainHand());
				int amount = args.length > 1 ? Integer.parseInt(args[1]) : item.maxDurability;
				player.getInventory().setItemInMainHand(item.repair(amount).item(player));
			} catch (Exception e) {}
		}
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> resultList = new ArrayList<String>();
		if (args.length == 1) {
			List<String> base = Arrays.asList("damage","repair");
			for (String str : base) if (contains(args[0],str)) resultList.add(str);
		}
		return resultList;
	}
	
	private boolean contains(String arg1, String arg2) {
		return (arg1 == null || arg1.isEmpty() || arg2.toLowerCase().contains(arg1.toLowerCase()));
	}
}