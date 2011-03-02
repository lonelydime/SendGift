package com.lonelydime.SendGift;

import java.util.logging.Logger;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.anjocaido.groupmanager.GroupManager;

public class SendGift extends JavaPlugin{
	private final Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	public static GroupManager gm = null;

	public void onDisable() {
		log.info("[SendGift] Disabled");
	}

	public void onEnable() {        
        //Get the infomation from the plugin.yml file.
        PluginDescriptionFile pdfFile = this.getDescription();
        
        setupPermissions();
        
        //Print that the plugin has been enabled!
        log.info("[SendGift] version " + pdfFile.getVersion() + " by lonelydime is enabled!");
	}
	
	public void setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		Plugin p = this.getServer().getPluginManager().getPlugin("GroupManager");
		
		if(Permissions == null) {
		    if(test != null) {
		    	Permissions = ((Permissions)test).getHandler();
		    }
		}
		
		if (p != null) {
            if (!p.isEnabled()) {
                this.getServer().getPluginManager().enablePlugin(p);
            }
            gm = (GroupManager) p;
        } 
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String command = cmd.getName();
		boolean canUseCommand = true;
		
		if (sender instanceof Player) {
			Player player = (Player)sender;
			
			if (Permissions != null) {
				canUseCommand = Permissions.has(player, "sendgift.send");
			}
			else if (gm != null) {
				canUseCommand = gm.getHandler().has(player, "sendgift.send");
			}

			if ((command.equals("gift")) && canUseCommand) {
				if (args.length == 3) {
					String playername = args[0];
					String itemcode = args[1];
					String itemamount = args[2];
					String errormsg = "";
					int giveamount = Integer.parseInt(itemamount);
					int givetypeid = Integer.parseInt(itemcode);
								
					Player testplayer = getServer().getPlayer(playername);
					if (testplayer == null || !testplayer.isOnline()) {
						errormsg = "The player '"+playername+"' is not online.";
					}
					else if (!player.getInventory().contains(givetypeid, giveamount)) {
						errormsg = "You do not have that item with that amount.";
					}
					
					if (errormsg.length() > 0) {
						player.sendMessage(ChatColor.GRAY+errormsg);
					}
					
					//start the transfer
					else {
						 //remove the item
						player.getInventory().removeItem(new ItemStack(givetypeid, giveamount));
						testplayer.getInventory().addItem(new ItemStack(givetypeid, giveamount));
						//currentinventory.removeItem(new ItemStack(ID,AMOUNT));
						String materialname = Material.getMaterial(Integer.parseInt(itemcode)).toString().toLowerCase().replace("_", " ");
						if (giveamount > 1) 
							materialname = materialname+"s";

						player.sendMessage(ChatColor.GRAY+"You gave "+ChatColor.GREEN+testplayer.getName()+" "+ChatColor.GRAY+itemamount+" "+ ChatColor.RED+materialname);
						testplayer.sendMessage(ChatColor.GREEN+player.getName()+ChatColor.GRAY+" gave you "+itemamount+" "+ChatColor.RED+materialname);
					}
				}
				else {
					return false;
				}
			}
		}
		else {
			sender.sendMessage("This is a player only command.");
		}
		return true;
	}
}
