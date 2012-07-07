// This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.	
// To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/	
// or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
package me.menexia.madetobreak;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.block.Block;

/**
 * Courtesy of Sagacious_Zed from the Bukkit Forums
 * http://forums.bukkit.org/threads/best-way-to-store-this-hashmap-in-a-file.46543/
 * 
 * Saving blocks in a file for further use (Bukkit Forums)
 * http://forums.bukkit.org/threads/saving-blocks-in-a-file-for-further-use.65169/
 *
 */
public class BlockSerialProxy {
	
	private final String world, uuid;
	private final int x, y, z;
	private transient Block blk;
	private static MadeToBreak plugin;
	
	public BlockSerialProxy(Block b, MadeToBreak os) {
		plugin = os;
		this.world = b.getWorld().getName();
		this.uuid = b.getWorld().getUID().toString();
		this.x = b.getX();
		this.y = b.getY();
		this.z = b.getZ();
	}
	
	public BlockSerialProxy(Map<String, Object> map, MadeToBreak os) {
		plugin = os;
		this.world = (String) map.get("world");
		this.uuid = (String) map.get("uuid");
		this.x = (Integer) map.get("x");
		this.y = (Integer) map.get("y");
		this.z = (Integer) map.get("z");
	}
	
	public static BlockSerialProxy deserialize(Map<String, Object> map) {
		return new BlockSerialProxy(map, plugin);
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
	
	public final Block getBlock(Server server) {
		if (blk == null){
			blk = server.getWorld(this.world).getBlockAt(x, y, z);
		}
		return blk;
	}

}
