package me.DMan16.AxItems.Listeners;

import me.Aldreda.AxUtils.Utils.ListenerInventoryPages;
import me.Aldreda.AxUtils.Utils.Utils;
import me.DMan16.AxItems.AxItems;
import me.DMan16.AxItems.Items.AxItem;
import me.DMan16.AxItems.Items.AxSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandListener implements CommandExecutor,TabCompleter {
	private static final ItemStack border = Utils.makeItem(Material.BLACK_STAINED_GLASS_PANE,Component.empty(),ItemFlag.values());
	private List<String> base = Arrays.asList("key","keywords","set");
	
	public CommandListener() {
		PluginCommand command = AxItems.getInstance().getCommand("axitems");
		command.setExecutor(this);
		command.setTabCompleter(this);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player) || args.length < 2) return false;
		Player player = (Player) sender;
		if (args[0].equalsIgnoreCase(base.get(0))) {
			AxItem item = AxItem.getAxItem(args[1]);
			new ShowItems(player,item == null ? Arrays.asList() : Arrays.asList(item));
		} else if (args[0].equalsIgnoreCase(base.get(1))) {
			List<AxItem> items = new ArrayList<AxItem>();
			List<String> keys = new ArrayList<String>();
			List<List<List<String>>> keywords = new ArrayList<List<List<String>>>();
			for (int i = 1; i < args.length; i++) {
				String[] list = args[i].split(",");
				for (AxItem item : AxItem.getByKeywords(list)) {
					int idx = keys.indexOf(item.key());
					if (idx < 0) {
						keys.add(item.key());
						idx = items.size();
						items.add(item);
						keywords.add(new ArrayList<List<String>>());
					}
					keywords.get(idx).add(Arrays.asList(list));
				}
			}
			new ShowItemsKeywords(player,items,keywords);
		}
		else if (args[0].equalsIgnoreCase(base.get(2))) {
			AxSet set = AxSet.getAxSet(args[1]);
			new ShowItems(player,set == null ? Arrays.asList() : set.items());
		}
		return true;
	}
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> resultList = new ArrayList<String>();
		if (args.length == 1) {
			for (String cmd : base) if (contains(args[0],cmd)) resultList.add(cmd.toLowerCase());
		} else if (args.length > 1 && args[0].equalsIgnoreCase(base.get(1))) {
			String[] strs = (args[args.length - 1] + " ").split(",");
			for (String name : AxItem.getAllKeywords()) if (contains(strs[strs.length - 1].trim(),name)) {
				List<String> list = new ArrayList<String>(Arrays.asList(strs));
				list.remove(list.size() - 1);
				list.add(name);
				resultList.add(String.join(",",list));
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase(base.get(0))) {
				if (args[1].equalsIgnoreCase("vanilla")) {
					for (Material material : Material.values()) if (!AxItem.isDisabledVanilla(material)) resultList.add(material.name().toLowerCase());
				} else for (String name : AxItem.getAllItemKeys()) if (contains(args[1],name)) resultList.add(name);
			} else if (args[0].equalsIgnoreCase(base.get(2))) {
				for (String name : AxSet.getAllSetNames()) if (contains(args[1],name)) resultList.add(name);
			}
		}
		return resultList;
	}
	
	private boolean contains(String arg1, String arg2) {
		return (arg1 == null || arg1.isEmpty() || arg2.toLowerCase().contains(arg1.toLowerCase()));
	}
	
	private class ShowItems extends ListenerInventoryPages {
		protected List<AxItem> items;
		protected Player player;
		
		public ShowItems(Player player, List<AxItem> items, Object ... objs) {
			super(player,player,Math.max(2,Math.min((int) Math.ceil(items.size() / 7.0) + 1,5)),
					Component.text("Items").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD),AxItems.getInstance(),items,objs,player);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void first(Object ... objs) {
			this.items = new ArrayList<AxItem>((List<AxItem>) objs[0]);
			this.player = (Player) objs[2];
		}
		
		@Override
		protected void reset() {
			for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i,isBorder(i) ? border : null);
		}
		
		protected ItemStack editItem(int idx) {
			if (idx >= items.size()) return null;
			return items.get(idx).item(player);
		}
		
		protected void setPageContents(int page) {
			for (int i = 0; i < 28; i++) try {
				inventory.addItem(editItem(i + 28 * (page - 1)));
			} catch (Exception e) {}
		}
		
		public int maxPage() {
			return Math.max(1,(int) Math.ceil(this.items.size() / 28.0));
		}
		
		protected void otherSlot(InventoryClickEvent event, int slot, ItemStack slotItem) {
			if (slot >= inventory.getSize() || isBorder(slot)) return;
			int line = (slot / 9) - 1;
			int row = slot - ((line + 1) * 9) - 1;
			Utils.givePlayer((Player) event.getWhoClicked(),this.items.get(28 * (currentPage - 1) + (line * 7) + row).item(player),null,false);
		}
	}
	
	private class ShowItemsKeywords extends ShowItems {
		private List<List<List<String>>> keywords;
		
		public ShowItemsKeywords(Player player, List<AxItem> items, List<List<List<String>>> keywords) {
			super(player,items,keywords);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void first(Object ... objs) {
			super.first(objs);
			this.keywords = new ArrayList<List<List<String>>>((List<List<List<String>>>) ((Object[]) objs[1])[0]);
		}
		
		@Override
		protected ItemStack editItem(int idx) {
			ItemStack item = super.editItem(idx);
			if (item == null) return null;
			List<Component> lore = item.lore();
			if (lore == null) lore = new ArrayList<Component>();
			lore.add(Component.text("----------------").color(NamedTextColor.GRAY));
			List<String> keywords = new ArrayList<String>();
			for (List<String> list : this.keywords.get(idx)) keywords.add(list.size() > 1 ? "(" + String.join(",",list) + ")" : String.join(",",list));
			lore.add(Component.text("Keywords: ").color(NamedTextColor.YELLOW).append(Component.text(String.join(", ",
					keywords)).color(NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC,false));
			item.lore(lore);
			return item;
		}
		
		@Override
		protected void next(ClickType click) {
			changePage(click.isRightClick() ? 5 : 1);
		}
		
		@Override
		protected void previous(ClickType click) {
			changePage(click.isRightClick() ? -5 : -1);
		}
	}
}