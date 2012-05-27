package me.menexia.madetobreak;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Server;

/**
 * Courtesy of Sagacious_Zed from the Bukkit Forums
 * http://forums.bukkit.org/threads/best-way-to-store-this-hashmap-in-a-file.46543/
 *
 */
public class LocationSerialProxy {
	
	private final String world;
	private final String uuid;
	private final double x;
	private final double y;
	private final double z;
	private transient Location loc;
	private static MadeToBreak plugin;
	
	public LocationSerialProxy(Location l, MadeToBreak os) {
		plugin = os;
		this.world = l.getWorld().getName();
		this.uuid = l.getWorld().getUID().toString();
		this.x = l.getX();
		this.y = l.getY();
		this.z = l.getZ();
	}
	
	public LocationSerialProxy(Map<String, Object> map, MadeToBreak os) {
		plugin = os;
		this.world = (String) map.get("world");
		this.uuid = (String) map.get("uuid");
		this.x = (Double) map.get("x");
		this.y = (Double) map.get("y");
		this.z = (Double) map.get("z");
	}
	
	public static LocationSerialProxy deserialize(Map<String, Object> map) {
		return new LocationSerialProxy(map, plugin);
	}
	
	public final Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("world", this.world);
		map.put("uuid", this.uuid);
		map.put("x", this.x);
		map.put("y", this.y);
		map.put("z", this.z);
		return map;
	}
	
	public final Location getLocation(Server server) {
		if (loc == null){
			loc = new Location(server.getWorld(this.world), x, y, z);
		}
		return loc;
	}

}
