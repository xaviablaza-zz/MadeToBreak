package me.menexia.madetobreak;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MBListener implements Listener {
	private final MadeToBreak plugin;
	public MBListener(MadeToBreak os) {
		plugin = os;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerInteract_Appliance(final PlayerInteractEvent event) {
		if (event.getPlayer().hasPermission("madetobreak.exempt")) { // Check if the player is exempted from depleting usage of appliance
			return;
		}
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		String applianceType;
		switch (event.getClickedBlock().getTypeId()) { // Check and set appliance type
		case 58: applianceType = "CraftingTable"; break;
		case 61: applianceType = "Furnace"; break;
		default:
			return;
		}
		Location loc = event.getClickedBlock().getLocation();
		Player player = event.getPlayer();
		if (plugin.b.containsKey(loc)) {
			
			if (plugin.b.get(loc) == -1) { // Check if the block is exempted (i.e. is an infinite usage block)
				return;
			} else if (plugin.b.get(loc) == -2) {
				event.setCancelled(true);
				player.sendMessage(plugin.colorize(plugin.getConfig().getString(applianceType+"Expiry.Display_Message")));
//				player.getWorld().playEffect(loc.add(0, 1, 0), Effect.SMOKE, 4);
//				player.getWorld().playEffect(loc, Effect.EXTINGUISH, 0);
//				//TODO: play effects prescribed in config
				return;
			}
			
			plugin.b.put(loc, plugin.b.get(loc)+1); // add usage
			
			if (plugin.b.get(loc) >= plugin.getConfig().getInt(applianceType+"_Uses")) { // Compares current usage to max usage in config
				// Deploy any effects prescribed in config
				World world = player.getWorld();
				switch (plugin.getConfig().getInt(applianceType+"Expiry.Explosion.Setting")) {
				case 0: break;
				case 1: event.getClickedBlock().setTypeId(0);
					world.createExplosion(loc, 0F);
					plugin.b.remove(loc); return;
				case 2: event.getClickedBlock().setTypeId(0);
					player.getWorld().createExplosion(loc, (float)plugin.getConfig().getInt("CraftingTableExpiry.Explosion.Force"));
					plugin.b.remove(loc);
					return;
				} // end of explosion checking
				if (plugin.getConfig().getBoolean(applianceType+"Expiry.Disappear")) {
					event.getClickedBlock().setTypeId(0);
					plugin.b.remove(loc);
					return;
				} // end of disappear checking
				
				plugin.b.put(loc, -2); // place 'broken' value (i.e. -2)
			}
			
		} else if (plugin.infiniPlace.contains(player)) { // Checks if the player can place infinite usage blocks
			plugin.b.put(loc, -1); // place 'infinite' value (i.e. -1)
		} else { // It's a new & normal block, put into the map.
			plugin.b.put(loc, 1);
		}
	}
	
	@EventHandler
	public void onBlockBreak_Appliance(final BlockBreakEvent event) {
		if (event.getBlock().getTypeId() == 58 || event.getBlock().getTypeId() == 61) {
			plugin.b.remove(event.getBlock().getLocation()); // remove location from map
			event.setCancelled(true); // cancel entire vanilla event, cancelling the drops as well
			event.getBlock().setTypeId(0); // set the block to air
			// add any extra drops (which are not technically needed in this case)
		}
	}
	
	@EventHandler
	public void onBlockBurn_Appliance(final BlockBurnEvent event) {
		if (event.getBlock().getTypeId() == 58 || event.getBlock().getTypeId() == 61) {
			plugin.b.remove(event.getBlock().getLocation());
		}
	}
}
