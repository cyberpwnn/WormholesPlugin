package com.volmit.wormholes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import org.bukkit.entity.EntityType;
import com.volmit.wormholes.config.CMax;
import com.volmit.wormholes.config.CMin;
import com.volmit.wormholes.config.CName;
import com.volmit.wormholes.config.Experimental;
import com.volmit.wormholes.util.Comment;
import com.volmit.wormholes.util.DataCluster;
import com.volmit.wormholes.util.GList;

public class Settings
{
	@CName("BUNGEECORD_SEND_ONLY")
	@Comment("Just send the player and let the other server handle where to spawn the player")
	public static boolean BUNGEECORD_SEND_ONLY = false;
	
	@CName("ALLOW_ENTITIY_TYPES")
	@Comment("Allowed entity types. (bungeecord portals cannot support entities)")
	public static ArrayList<String> ALLOW_ENTITIY_TYPES = new GList<String>();
	
	@CName("APERTURE_ENTITIY_TYPES")
	@Comment("Allowed entity types for the aperture (entity capture)")
	public static ArrayList<String> APERTURE_ENTITIY_TYPES = new GList<String>();
	
	@CName("ENABLE_PROJECTIONS")
	@Comment("Should Wormholes project blocks from the other side?")
	public static boolean ENABLE_PROJECTIONS = true;
	
	@CName("ALLOW_ENTITIES")
	@Comment("Should Local Wormholes allow non-player entities to pass through?")
	public static boolean ALLOW_ENTITIES = true;
	
	@CName("SPLASH")
	@Comment("Should wormholes splash the console window on enable?")
	public static boolean SPLASH = true;
	
	@CName("AUTOBUILD_PORTALS")
	@Comment("Automatically light portals when the last color block is placed\nPermission is still needed for this to work.")
	public static boolean AUTOBUILD_PORTALS = false;
	
	@CName("ENABLE_APERTURE")
	@Comment("Should Wormholes project entities from the other side?")
	public static boolean ENABLE_APERTURE = true;
	
	@CName("SKIN_CACHE_PURGE_THRESHOLD")
	@CMax(10000)
	@CMin(1)
	@Comment("The max days a skin can be cached before being purged")
	public static int SKIN_CACHE_PURGE_THRESHOLD = 30;
	
	@CName("MAX_VELOCITY_CAPTURE_RANGE")
	@CMax(128)
	@CMin(12)
	@Comment("The max distance a portal will start raycasting players to determine velocity intersections.")
	public static int MAX_VELOCITY_CAPTURE_RANGE = 36;
	
	@CName("MAX_PORTAL_SIZE")
	@CMax(33)
	@CMin(3)
	@Comment("The max size (width/height) a portal can be.\nMust be an odd number")
	public static int MAX_PORTAL_SIZE = 9;
	
	@CName("APERTURE_MAX_SPEED")
	@CMin(1)
	@CMax(20)
	@Experimental
	@Comment("Modify the interval in which entities are updated, sent through bungee, and sent to players\nMust be 1 or higher")
	public static int APERTURE_MAX_SPEED = 2;
	
	@CName("USE_LIGHTMAPS")
	@Comment("Send skylight chunk maps instead of multiple block changes for block projections to counteract client hitching due to lighting issues\nNote, this can cause loss of emitted light (such as torches etc) in and around the portal projection.")
	@Experimental
	public static boolean USE_LIGHTMAPS = true;
	
	@CName("WORMHOLE_WORKER_THREADS")
	@CMax(16)
	@CMin(1)
	@Experimental
	@Comment("Low usage worker threads for low priority tasks along with flushing the rasterer (sending packets & compression)")
	public static int WORMHOLE_WORKER_THREADS = 2;
	
	@CName("WORMHOLE_POWER_THREADS")
	@CMin(1)
	@CMax(32)
	@Experimental
	@Comment("High priority power threads used for block projections and map permutations. More threads can make projections faster for large populations, but could also use more processor cores.")
	public static int WORMHOLE_POWER_THREADS = 4;
	
	@CName("WORMHOLE_SKIN_FLUSH")
	@CMin(5)
	@CMax(200)
	@Experimental
	@Comment("Using worker threads, wormholes will communicate with the mojang api to get profile skins for players through portals.")
	public static int WORMHOLE_SKIN_FLUSH = 100;
	
	@CName("WORMHOLE_IDLE_FLUSH")
	@CMin(20)
	@CMax(1200)
	@Experimental
	@Comment("Update portal information to players even if they are not moving, slowly\nThis prevents visual artifacts if the viewer is not moving.")
	public static int WORMHOLE_IDLE_FLUSH = 100;
	
	@CName("PROJECTION_CHANGE_THROTTLE")
	@CMin(2048)
	@CMax(Integer.MAX_VALUE / 2)
	@Experimental
	@Comment("Max block changes per chunk projection packet")
	public static int PROJECTION_CHANGE_THROTTLE = 16728;
	
	@CName("PROJECTION_SAMPLE_RADIUS")
	@CMin(8)
	@CMax(128)
	@Experimental
	@Comment("Modify the distance blocks will be sampled\nEnsure this value matches across all servers.")
	public static int PROJECTION_SAMPLE_RADIUS = 16;
	
	@CName("PROJECTION_MAX_SPEED")
	@CMin(1)
	@CMax(100)
	@Experimental
	@Comment("Change the maximum tickrate projections can run\nNote: This is in place to prevent overprojecting faster than 1/4th of a second.")
	public static int PROJECTION_MAX_SPEED = 25;
	
	@CName("CHUNK_SEND_RATE")
	@CMin(1)
	@CMax(20)
	@Experimental
	@Comment("The interval in which wormholes will send a chunk packet to players. Must be higher than 0.\nInterval in ticks")
	public static int CHUNK_SEND_RATE = 2;
	
	@CName("CHUNK_SEND_MAX")
	@CMin(1)
	@CMax(100)
	@Experimental
	@Comment("The maximum partial chunks which can be sent in one interval")
	public static int CHUNK_SEND_MAX = 12;
	
	@CName("CHUNK_MAX_CHANGE")
	@CMin(1024)
	@CMax(Integer.MAX_VALUE / 2)
	@Experimental
	@Comment("The maximum potential blocks to change per packet.")
	public static int CHUNK_MAX_CHANGE = 10240;
	
	@CName("NETWORK_POPULATE_MAPPING_INTERVAL")
	@CMin(1)
	@CMax(200)
	@Experimental
	@Comment("The interval in which portals are checked to ensure they are populated with projection maps\nThis is only called one time when the portal is created.")
	public static int NETWORK_POPULATE_MAPPING_INTERVAL = 40;
	
	@CName("NETWORK_MAX_PACKET_SIZE")
	@CMin(4096)
	@CMax(40128)
	@Experimental
	@Comment("The max packet size in bytes.\nLower values will force wormholes to send more packets\nIncreasing this could cause bungeecord to reject the packet.")
	public static int NETWORK_MAX_PACKET_SIZE = 40000;
	
	@CName("NETWORK_COMPRESSION_LEVEL")
	@CMin(1)
	@CMax(9)
	@Experimental
	@Comment("This adds compression to projection packets through bungeecord.\nIncreasing this past 4 increases processing time and slightly reduces the size\nTesting shows comp 4 shows the best improvement in size for compression time\nMust be from 1-9")
	public static int NETWORK_COMPRESSION_LEVEL = 4;
	
	@CName("NETWORK_PUSH_THRESHOLD")
	@CMin(500)
	@CMax(10000)
	@Experimental
	@Comment("Time Threshold in milliseconds to push online servers")
	public static int NETWORK_PUSH_THRESHOLD = 5000;
	
	@CName("NETWORK_FLUSH_THRESHOLD")
	@CMin(100)
	@CMax(5000)
	@Experimental
	@Comment("Time Threshold in milliseconds to push excess queue packets to players")
	public static int NETWORK_FLUSH_THRESHOLD = 750;
	
	@CName("NETWORK_POLL_THRESHOLD")
	@CMin(100)
	@CMax(10000)
	@Experimental
	@Comment("Time Threshold in milliseconds to poll for servers and online status.\nEnsure it is at least half the time of the push threshold.")
	public static int NETWORK_POLL_THRESHOLD = 1000;
	
	public static DataCluster getConfig()
	{
		DataCluster cc = new DataCluster();
		
		for(Field i : Settings.class.getDeclaredFields())
		{
			try
			{
				CName n = i.getAnnotation(CName.class);
				
				if(n != null)
				{
					String name = n.value().toLowerCase().replaceAll("-", "_");
					Object value = i.get(null);
					
					if(!i.isAnnotationPresent(Experimental.class))
					{
						cc.trySet("wormholes." + name, value, ((Comment) i.getDeclaredAnnotation(Comment.class)).value());
					}
				}
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return cc;
	}
	
	public static void chkConfig()
	{
		for(Field i : Settings.class.getFields())
		{
			try
			{
				chkField(i);
			}
			
			catch(IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void chkField(Field f) throws IllegalArgumentException, IllegalAccessException
	{
		if(f.getType().equals(double.class))
		{
			if(f.isAnnotationPresent(CMax.class))
			{
				CMax m = f.getAnnotation(CMax.class);
				double value = (double) f.get(null);
				
				if(value > m.value())
				{
					System.out.println("WARNING: " + f.getName() + " is set to " + value + " which is higher than the allowed maximum (" + m.value() + "). FIXING...");
					value = m.value();
					f.set(null, value);
				}
			}
			
			if(f.isAnnotationPresent(CMin.class))
			{
				CMin m = f.getAnnotation(CMin.class);
				double value = (double) f.get(null);
				
				if(value < m.value())
				{
					System.out.println("WARNING: " + f.getName() + " is set to " + value + " which is lower than the allowed minimum (" + m.value() + "). FIXING...");
					value = m.value();
					f.set(null, value);
				}
			}
		}
		
		if(f.getType().equals(int.class))
		{
			if(f.isAnnotationPresent(CMax.class))
			{
				CMax m = f.getAnnotation(CMax.class);
				int value = (int) f.get(null);
				
				if(value > m.value())
				{
					System.out.println("WARNING: " + f.getName() + " is set to " + value + " which is higher than the allowed maximum (" + m.value() + "). FIXING...");
					value = (int) m.value();
					f.set(null, value);
				}
			}
			
			if(f.isAnnotationPresent(CMin.class))
			{
				CMin m = f.getAnnotation(CMin.class);
				int value = (int) f.get(null);
				
				if(value < m.value())
				{
					System.out.println("WARNING: " + f.getName() + " is set to " + value + " which is lower than the allowed minimum (" + m.value() + "). FIXING...");
					value = (int) m.value();
					f.set(null, value);
				}
			}
		}
	}
	
	public static void setConfig(DataCluster cc)
	{
		for(Field i : Settings.class.getDeclaredFields())
		{
			try
			{
				
				CName n = i.getAnnotation(CName.class);
				
				if(n != null)
				{
					String name = n.value().toLowerCase().replaceAll("-", "_");
					
					if(!i.isAnnotationPresent(Experimental.class))
					{
						if(cc.contains("wormholes." + name))
						{
							i.set(null, cc.getAbstract("wormholes." + name));
						}
					}
				}
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	static
	{
		for(EntityType i : EntityType.values())
		{
			APERTURE_ENTITIY_TYPES.add(i.toString());
			
			if(i.equals(EntityType.PLAYER))
			{
				continue;
			}
			
			ALLOW_ENTITIY_TYPES.add(i.toString());
		}
	}
	
	public static DataCluster getExperimentalConfig()
	{
		DataCluster cc = new DataCluster();
		
		for(Field i : Settings.class.getDeclaredFields())
		{
			try
			{
				CName n = i.getAnnotation(CName.class);
				
				if(n != null)
				{
					String name = n.value().toLowerCase().replaceAll("-", "_");
					Object value = i.get(null);
					
					if(i.isAnnotationPresent(Experimental.class))
					{
						cc.trySet("experimental." + name, value, ((Comment) i.getDeclaredAnnotation(Comment.class)).value());
					}
				}
				
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return cc;
	}
	
	public static void setExperimentalConfig(DataCluster cc)
	{
		for(Field i : Settings.class.getDeclaredFields())
		{
			try
			{
				CName n = i.getAnnotation(CName.class);
				
				if(n != null)
				{
					String name = i.getName().toLowerCase().replaceAll("-", "_");
					
					if(i.isAnnotationPresent(Experimental.class))
					{
						if(cc.contains("experimental." + name))
						{
							i.set(null, cc.getAbstract("experimental." + name));
						}
					}
				}
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
