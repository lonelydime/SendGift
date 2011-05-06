//craftbukkit 513

package com.lonelydime.SendGift;

//java stuff
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;
//bukkit stuff
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
//permissions stuff
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.anjocaido.groupmanager.GroupManager;

public class SendGift extends JavaPlugin{
	private final Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	public static GroupManager gm = null;
	private final SGPlayerListener playerListener = new SGPlayerListener(this);
	private int maxradius = 0;
	private String allowoffline = null;
	
	public void onDisable() {
		log.info("[SendGift] Disabled");
	}

	public void onEnable() {     
		if (!new File(getDataFolder().toString()).exists() ) {
        	new File(getDataFolder().toString()).mkdir();
        }
		
		File yml = new File(getDataFolder()+"/config.yml");
		File offlinesends = new File(getDataFolder()+"/offline.txt");
		
		if (!yml.exists()) {
        	new File(getDataFolder().toString()).mkdir();
    	    try {
    	    	yml.createNewFile();
    	    }
    	    catch (IOException ex) {
    	    	System.out.println("cannot create file "+yml.getPath());
    	    }
    	    
    	    try {
    	    BufferedWriter out = new BufferedWriter(new FileWriter(yml, true));
			
			out.write("max-range: 100");
			out.newLine();
			out.write("allow-offline: 'true'");
			out.newLine();

		    //Close the output stream
		    out.close();
    	    }
    	    catch (Exception e) {
    	    	System.out.println("cannot write config file: "+e);
    	    }
        }	
		
		if (!offlinesends.exists()) {
			try {
				offlinesends.createNewFile();
			} catch (IOException e) {
				System.out.println("cannot create file "+offlinesends.getPath()+"/"+offlinesends.getName());
			}
		}
		
		maxradius = getConfiguration().getInt("max-range", 0);
		allowoffline = getConfiguration().getString("allow-offline", "false");
		
		PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		
        //Get the information from the plugin.yml file.
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

		HashMap<Integer, ? extends ItemStack> itemsarray = new HashMap<Integer, ItemStack>();
		
		if (sender instanceof Player) {
			Player player = (Player)sender;
			
			if (Permissions != null) {
				canUseCommand = Permissions.has(player, "sendgift.send");
			}
			else if (gm != null) {
				canUseCommand = gm.getWorldsHolder().getWorldPermissions((Player)sender).has((Player)sender,"sendgift.send");
			}

			if ((command.equals("gift") || command.equals("send")) && canUseCommand) {
				
				if (args.length >= 3) {
					String playername = args[0];
					String itemamount = args[1];
					String itemstring = args[2];
					String itemstring2 = null;
					if (args.length > 3) {
						itemstring2 = args[3];
					}
					
					String errormsg = "";
					int giveamount = Integer.parseInt(itemamount);
					int givetypeid = 0;
					
					//checks to see if the item works
					try {
						givetypeid = Integer.parseInt(itemstring);
					}
					catch(NumberFormatException e) {
						if (itemstring2 != null)
							itemstring = itemstring+"_"+itemstring2;
					  	
						itemstring = itemstring.toUpperCase();
	
					    try {
					    	givetypeid = Material.getMaterial(itemstring).getId();
					    }
					    catch (NullPointerException n) {
					    	errormsg = "The item '"+itemstring+"' does not exist.";
					    }
					}
					
					//allows offline transfers
					Player testplayer = getServer().getPlayer(playername);
					String materialname;
					
					//Checks to see if you have enough
					itemsarray = player.getInventory().all(Material.getMaterial(givetypeid));
					int playerHasInInventory = 0;
					
					for (Entry<Integer, ? extends ItemStack> entry : itemsarray.entrySet()) {
				        ItemStack value = entry.getValue();
				        
				        //if it's wool
				        if (value.getTypeId() == 35) {
				        	Wool wool = (Wool)value.getData();
				        	player.sendMessage(wool.getColor().toString());
				        	player.sendMessage(value.getDurability()+"");
				        }
				        playerHasInInventory = playerHasInInventory + value.getAmount();
					}
					
					//Checks to see if players are close enough
					if (testplayer != null) {
						if (maxradius > 0) {
							int totaldistance =0;
							
							int x1 = player.getLocation().getBlockX();
							int y1 = player.getLocation().getBlockY();
							int z1 = player.getLocation().getBlockZ();
							int x2 = testplayer.getLocation().getBlockX();
							int y2 = testplayer.getLocation().getBlockY();
							int z2 = testplayer.getLocation().getBlockZ();
							
							totaldistance = ((x1 - x2)^2 + (y1 - y2)^2 +(z1 - z2)^2);
							if (!(totaldistance < (maxradius^2))) {
								errormsg = "That player too far away.";
							}
						}
					}
					else {
						if (allowoffline.matches("false")) {
							errormsg = "That player is not online.";
						}
					}
					
					
					//outputs error
					if (giveamount > playerHasInInventory) {
						errormsg = "You do not have that item with that amount.";
					}
					
					if (errormsg.length() > 0) {
						player.sendMessage(ChatColor.GRAY+errormsg);
					}
					
					//start the transfer
					else {
						ItemStack itemstack = player.getInventory().getItem(player.getInventory().first(new ItemStack(givetypeid, giveamount)));
					    
						//player is not online, store in offline.txt
						if (testplayer == null || !testplayer.isOnline()) {
							
							File offlineFile = new File(getDataFolder()+"/offline.txt");
							// Write the send to file
							try {
								
								BufferedWriter out = new BufferedWriter(new FileWriter(offlineFile, true));
								
								String textToWrite = playername+":"+givetypeid+":"+giveamount+":"+player.getName()+":"+itemstack.getDurability();
								
								out.write(textToWrite);
								out.newLine();
		
							    //Close the output stream
							    out.close();
							    
							    
							    materialname = Material.getMaterial(givetypeid).toString().toLowerCase().replace("_", " ");
							    if (giveamount > 1) {
									if (materialname.endsWith("s") || materialname.endsWith("z"))
										materialname = materialname+"es";
									else
										materialname = materialname+"s";
								}
							    
							    
							    player.getInventory().removeItem(itemstack);
							    
							    player.sendMessage(ChatColor.GRAY+"You gave "+ChatColor.GREEN+playername+" "+ChatColor.GRAY+itemamount+" "+ ChatColor.RED+materialname);
							    player.sendMessage(ChatColor.GRAY+"They will receive it when they log in.");
							}
							catch (Exception e) {
								log.info("[SendGift] Offline transfer to "+playername+" failed: " + e);
							}
						    
						}
						
						//both online, do in real time
						else {
							//make sure that the receiving player's inventory isn't full
							if (testplayer.getInventory().firstEmpty() >= 0) {
							
								 //remove the item
								player.getInventory().removeItem(itemstack);
								testplayer.getInventory().addItem(itemstack);
								//currentinventory.removeItem(new ItemStack(ID,AMOUNT));
								materialname = Material.getMaterial(givetypeid).toString().toLowerCase().replace("_", " ");
								if (giveamount > 1) {
									if (materialname.endsWith("s") || materialname.endsWith("z"))
										materialname = materialname+"es";
									else
										materialname = materialname+"s";
								}
								
								player.sendMessage(ChatColor.GRAY+"You gave "+ChatColor.GREEN+testplayer.getName()+" "+ChatColor.GRAY+itemamount+" "+ ChatColor.RED+materialname);
								testplayer.sendMessage(ChatColor.GREEN+player.getName()+ChatColor.GRAY+" gave you "+itemamount+" "+ChatColor.RED+materialname);
						
							}
							else {
								player.sendMessage(ChatColor.GREEN+testplayer.getName()+"'s "+ChatColor.GRAY+" inventory is full.  Try again later.");
								testplayer.sendMessage(ChatColor.GREEN+player.getName()+ChatColor.GRAY+" tried to send you something, but you have no space.");
							}
						}
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
