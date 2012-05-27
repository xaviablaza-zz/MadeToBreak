package me.menexia.madetobreak;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Inspired by: http://www.youtube.com/watch?v=5_6Q1yYDouo
 * "Comprar, Tirar, Comprar - un documental sobre la Obsolescencia Programada"
 * 
 * @author MeneXia aka AblazaX
 *
 */
public class MadeToBreak extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	public Map<Location, Integer> b = new HashMap<Location, Integer>(); // Key: Location of Block, Value: Amount of times used
	public Set<Player> infiniPlace = new HashSet<Player>(); // Track players who can place infinite usage blocks
	
	public void onDisable() {
		saveLocations();
		info("disabled");
	}
	
	public void onEnable() {
		// Config checks
		this.checkConfig();
		
		// Initialize listener(s)
		new MBListener(this);
		
		// Load locations if present
		loadLocations();
		
		// Enable message
		info("enabled");
	}
	
	@SuppressWarnings("unchecked")
	public void loadLocations() {
		try {
			File passData = new File(getDataFolder(), "locations.dat");
			if (passData.exists()) {
				Map<Map<String, Object>, Integer> map = new HashMap<Map<String, Object>, Integer>();
				map = (Map<Map<String, Object>, Integer>) SLAPI.load(
						"plugins" + File.separator + "MadeToBreak" + File.separator + "locations.dat");
				for (Map<String, Object> s : map.keySet()) {
					b.put(new LocationSerialProxy(s, this).getLocation(this.getServer()), map.get(s));
				}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} // end of checking if location data exists. if not, will do nothing.
	}
	
	public void saveLocations() {
		try {
			Map<Map<String, Object>, Integer> map = new HashMap<Map<String, Object>, Integer>();
			for (Location l : b.keySet()) {
				map.put(new LocationSerialProxy(l, this).serialize(), b.get(l));
			}
			SLAPI.save(map, "plugins" + File.separator + "MadeToBreak" + File.separator + "locations.dat");
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public String colorize(String message) {
		return message.replaceAll("&([a-f0-9])", ChatColor.COLOR_CHAR + "$1");
	}

	public void info(String status) {
		PluginDescriptionFile pdf = this.getDescription();
		this.logger.info("[MadeToBreak] version " + pdf.getVersion() + " is now " + status + "!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String zhf, String args[]) {
		if (cmd.getName().equalsIgnoreCase("po")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be used in-game!");
			}
			Player player = (Player)sender;
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("infinite")) {
					if (infiniPlace.contains(player)) {
						infiniPlace.remove(player);
						player.sendMessage(ChatColor.GOLD + "[MTB] Infinite Usage Placement is now: " + ChatColor.DARK_RED + "OFF");
					} else {
						infiniPlace.add(player);
						player.sendMessage(ChatColor.GOLD + "[MTB] Infinite Usage Placement is now " + ChatColor.DARK_RED + "ON");
					}
					
				}
			}
		}
		return false;
	}
	
	private void checkConfig() {
		String name = "config.yml";
		File actual = new File(getDataFolder(), name);
		if (!actual.exists()) {
			getDataFolder().mkdir();
			InputStream input = this.getClass().getResourceAsStream("/defaults/config.yml");
			if (input != null) {
				FileOutputStream output = null;

				try {
					output = new FileOutputStream(actual);
					byte[] buf = new byte[4096]; //[8192]?
					int length = 0;
					while ((length = input.read(buf)) > 0) {
						output.write(buf, 0, length);
					}
					this.logger.info("[MadeToBreak] Default configuration file written: " + name);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (input != null)
							input.close();
					} catch (IOException e) {}

					try {
						if (output != null)
							output.close();
					} catch (IOException e) {}
				}
			}
		}
	}

}
