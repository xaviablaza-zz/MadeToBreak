package me.menexia.madetobreak;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MBListener implements Listener {
	private final MadeToBreak plugin;
	public MBListener(MadeToBreak os) {
		plugin = os;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerInteract_Appliance(final PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		String applianceType;
		switch (event.getClickedBlock().getTypeId()) { // Check and set appliance type
		case 58: applianceType = "CraftingTable"; break;
		case 61: applianceType = "Furnace"; break;
		case 116: applianceType = "EnchantmentTable"; break;
		case 117: applianceType = "BrewingStand"; break;
		case 23: applianceType = "Dispenser"; break;
		default:
			return;
		}
		if (event.getPlayer().hasPermission("madetobreak.exempt."+applianceType.toLowerCase())) { // Check if the player is exempted from depleting usage of appliance
			return;
		}
		Block blk = event.getClickedBlock();
		Player player = event.getPlayer();
		if (plugin.l.containsKey(blk)) {
			
			if (plugin.l.get(blk) == -1) { // Check if the block is exempted (i.e. is an infinite usage block)
				return;
			} else if (plugin.l.get(blk) == -2) {
				event.setCancelled(true);
				player.sendMessage(plugin.colorize(plugin.getConfig().getString(applianceType+"Expiry.Display_Message")));
//				player.getWorld().playEffect(loc.add(0, 1, 0), Effect.SMOKE, 4);
//				player.getWorld().playEffect(loc, Effect.EXTINGUISH, 0);
//				//TODO: play effects prescribed in config
				return;
			}
			
			plugin.l.put(blk, plugin.l.get(blk)+1); // add usage
			
			if (plugin.l.get(blk) >= plugin.getConfig().getInt(applianceType+"_Uses")) { // Compares current usage to max usage in config
				// Deploy any effects prescribed in config
				World world = player.getWorld();
				switch (plugin.getConfig().getInt(applianceType+"Effects.Explosion.Setting")) {
				case 0: break;
				case 1: event.getClickedBlock().setTypeId(0);
					world.createExplosion(blk.getLocation(), 0F);
					plugin.l.remove(blk); return;
				case 2: event.getClickedBlock().setTypeId(0);
					player.getWorld().createExplosion(blk.getLocation(), (float)plugin.getConfig().getInt(applianceType+"Effects.Explosion.Force"));
					plugin.l.remove(blk);
					return;
				} // end of explosion checking
				if (plugin.getConfig().getBoolean(applianceType+"Effects.Disappear")) {
					event.getClickedBlock().setTypeId(0);
					plugin.l.remove(blk);
					return;
				} // end of disappear checking
				
				plugin.l.put(blk, -2); // place 'broken' value (i.e. -2)
			}
			
		} else { // It's a new & normal block, put into the map.
			if (plugin.getConfig().getInt(applianceType+"_Uses") == -1) {
				return;
			}
			plugin.l.put(blk, 1);
		}
	}
	
	@EventHandler
	public void checkInfinitePlace_Appliance(final BlockPlaceEvent event) {
		if (plugin.infiniPlace.contains(event.getPlayer()) && MadeToBreak.appliances.contains(event.getBlock().getTypeId())) { // Checks if the player can place infinite usage blocks
			plugin.l.put(event.getBlock(), -1); // place 'infinite' value (i.e. -1)
		}
	}
	
	@EventHandler
	public void onBlockBreak_Appliance(final BlockBreakEvent event) {
		if (MadeToBreak.appliances.contains(event.getBlock().getTypeId())) {
			plugin.l.remove(event.getBlock().getLocation()); // remove location from map
			event.setCancelled(true); // cancel entire vanilla event, cancelling the drops as well
			event.getBlock().setTypeId(0); // set the block to air
			// add any extra drops (which are not technically needed in this case)
		}
	}
	
	@EventHandler
	public void onBlockBurn_Appliance(final BlockBurnEvent event) {
		if (MadeToBreak.appliances.contains(event.getBlock().getTypeId())) {
			plugin.l.remove(event.getBlock().getLocation());
		}
	}
	
	@EventHandler
	public void onPistonPush_Appliance(final BlockPistonExtendEvent event) {
		for (Block b : event.getBlocks()) {
			if (plugin.l.containsKey(b)) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onPistonPull_Appliance(final BlockPistonRetractEvent event) {
		if (plugin.l.containsKey(event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityExplode_Appliance(final EntityExplodeEvent event) {
		for (Block b : event.blockList()) {
			if (plugin.l.containsKey(b)) {
				plugin.l.remove(b);
				b.setType(Material.AIR);
				return;
			}
		}
	}
}
