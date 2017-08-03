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
import com.volmit.wormholes.util.ParticleEffect;
import com.volmit.wormholes.util.ParticleEffect.ParticleProperty;

public class Settings
{
	@CName("BUNGEECORD_SEND_ONLY")
	@Comment("Just send the player and let the other server handle where to spawn the player")
	public static boolean BUNGEECORD_SEND_ONLY = false;
	
	@CName("ENABLE_PARTICLES")
	@Comment("Toggle this to toggle particles")
	public static boolean ENABLE_PARTICLES = true;
	
	@CName("ENABLE_SOUND")
	@Comment("Toggle this to toggle sounds")
	public static boolean ENABLE_SOUND = true;
	
	@CName("WAND_COOLDOWN")
	@CMin(5)
	@CMax(100)
	@Comment("In Ticks, Prevent block spam from portal wands (quickly at least)")
	public static int WAND_COOLDOWN = 36;
	
	@CName("SHOW_TIPS")
	@Comment("Show tips to people who can create and configure wormholes")
	public static boolean SHOW_TIPS = true;
	
	@Experimental
	@CName("RTP_SEARCH_INTERVAL")
	@CMin(2)
	@CMax(40)
	@Comment("If not forcing async, setting this too low will hit the timings pretty hard.")
	public static int RTP_SEARCH_INTERVAL = 20;
	
	@CName("RTP_AUTO_REFRESH_INTERVAL")
	@CMin(50)
	@CMax(1200)
	@Comment("Portal Auto Refresh interval (in ticks)")
	public static int RTP_AUTO_REFRESH_INTERVAL = 200;
	
	@CName("RTP_DEFAULT_MIN_DISTANCE")
	@CMin(0)
	@CMax(Integer.MAX_VALUE / 2)
	@Comment("Define the default minimum distance for rtp portals")
	public static int RTP_DEFAULT_MIN_DISTANCE = 1000;
	
	@CName("RTP_DEFAULT_MAX_DISTANCE")
	@CMin(1)
	@CMax(Integer.MAX_VALUE)
	@Comment("Define the default maximum distance for rtp portals")
	public static int RTP_DEFAULT_MAX_DISTANCE = 8000;
	
	@CName("PORTAL_COOLDOWN")
	@CMin(0)
	@CMax(102400)
	@Comment("The time (in ticks) a player must wait to use any portal after using any portal.")
	public static int PORTAL_COOLDOWN = 100;
	
	@Experimental
	@CName("RTP_FORCE_ASYNC_SEARCH")
	@Comment("Force async searching. WARNING: Can crash the server when using a custom generator, but it's much faster!")
	public static boolean RTP_FORCE_ASYNC_SEARCH = false;
	
	@Experimental
	@CName("RTP_MAX_PREQUEUE")
	@CMin(2)
	@CMax(32)
	@Comment("Wormholes automatically caches new pending locations in case a lot of entities/players enter at the same time.")
	public static int RTP_MAX_PREQUEUE = 3;
	
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
	
	@CName("WAND_DEFAULT_MATERIAL")
	@Comment("The default frame material for placing frames with the wand")
	public static String WAND_DEFAULT_MATERIAL = "COAL_BLOCK";
	
	@CName("WAND_ENABLED")
	@Comment("Set this to false to disable portal wands")
	public static boolean WAND_ENABLED = true;
	
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
	
	@CName("USE_OLD_RENDER_METHOD")
	@Experimental
	@Comment("Use the old rendering method (slower, but slightly safer)")
	public static boolean USE_OLD_RENDER_METHOD = false;
	
	@CName("FULL_BRIGHT_PROJECTIONS")
	@Experimental
	@Comment("Brighten the projections to max skylight levels.")
	public static boolean FULL_BRIGHT_PROJECTIONS = true;
	
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
	
	@CName("APERTURE_SLOWDOWN_AMOUNT")
	@CMin(3)
	@CMax(10)
	@Experimental
	@Comment("The slowdown is addedto the aperture max speed when the threshold is reached.")
	public static int APERTURE_SLOWDOWN_AMOUNT = 5;
	
	@CName("APERTURE_SLOWDOWN_THRESHOLD")
	@CMin(4)
	@CMax(30)
	@Experimental
	@Comment("The ammount of entities/players projected threshold\nIf the count is higher than this, the slowdown is applied")
	public static int APERTURE_SLOWDOWN_THRESHOLD = 10;
	
	@CName("APERTURE_ICE_AMOUNT")
	@CMin(7)
	@CMax(11)
	@Experimental
	@Comment("The slowdown is addedto the aperture max speed when the threshold is reached.")
	public static int APERTURE_ICE_AMOUNT = 8;
	
	@CName("APERTURE_ICE_THRESHOLD")
	@CMin(8)
	@CMax(60)
	@Experimental
	@Comment("The ammount of entities/players projected threshold\nIf the count is higher than this, the slowdown is applied")
	public static int APERTURE_ICE_THRESHOLD = 50;
	
	@CName("USE_LIGHTMAPS")
	@Comment("Send skylight chunk maps instead of multiple block changes for block projections to counteract client hitching due to lighting issues\nNote, this can cause loss of emitted light (such as torches etc) in and around the portal projection.")
	@Experimental
	public static boolean USE_LIGHTMAPS = true;
	
	@CName("WORMHOLES_DROP_KEY_ON_BREAK")
	@Comment("Drop key blocks as item drops when portal is broken")
	public static boolean WORMHOLES_DROP_KEY_ON_BREAK = true;
	
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
	public static int NETWORK_MAX_PACKET_SIZE = 32750;
	
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
	public static int NETWORK_FLUSH_THRESHOLD = 250;
	
	@CName("NETWORK_POLL_THRESHOLD")
	@CMin(100)
	@CMax(10000)
	@Experimental
	@Comment("Time Threshold in milliseconds to poll for servers and online status.\nEnsure it is at least half the time of the push threshold.")
	public static int NETWORK_POLL_THRESHOLD = 1000;
	
	@CName("PARTICLE_TYPE_LIGHTNING")
	@Comment("Particle Effect used for making lightning\nParticle Types: https://volmit.com/docs/particle-types/")
	public static String PARTICLE_TYPE_LIGHTNING = "CRIT_MAGIC";
	
	@CName("PARTICLE_TYPE_AMBIENT")
	@Comment("Particle Effect used for making ambient meshes\nParticle Types: https://volmit.com/docs/particle-types/")
	public static String PARTICLE_TYPE_AMBIENT = "SUSPENDED_DEPTH";
	
	@CName("PARTICLE_TYPE_RIPPLE")
	@Comment("Particle Effect used for making teleport ripples\nParticle Types: https://volmit.com/docs/directional-particle-types/")
	public static String PARTICLE_TYPE_RIPPLE = "CRIT_MAGIC";
	
	@CName("PARTICLE_TYPE_DENY_RIPPLE")
	@Comment("Particle Effect used for making deny ripples\nParticle Types: https://volmit.com/docs/directional-particle-types/")
	public static String PARTICLE_TYPE_DENY_RIPPLE = "CRIT";
	
	public static ParticleEffect getLightningParticle()
	{
		try
		{
			ParticleEffect p = ParticleEffect.valueOf(PARTICLE_TYPE_LIGHTNING);
			
			if(p != null)
			{
				return p;
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		return ParticleEffect.CRIT_MAGIC;
	}
	
	public static ParticleEffect getAmbientParticle()
	{
		try
		{
			ParticleEffect p = ParticleEffect.valueOf(PARTICLE_TYPE_AMBIENT);
			
			if(p != null)
			{
				return p;
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		return ParticleEffect.SUSPENDED_DEPTH;
	}
	
	public static ParticleEffect getRippleParticle()
	{
		try
		{
			ParticleEffect p = ParticleEffect.valueOf(PARTICLE_TYPE_RIPPLE);
			
			if(p != null)
			{
				if(p.hasProperty(ParticleProperty.DIRECTIONAL))
				{
					return p;
				}
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		return ParticleEffect.CRIT_MAGIC;
	}
	
	public static ParticleEffect getRippleDenyParticle()
	{
		try
		{
			ParticleEffect p = ParticleEffect.valueOf(PARTICLE_TYPE_DENY_RIPPLE);
			
			if(p != null)
			{
				if(p.hasProperty(ParticleProperty.DIRECTIONAL))
				{
					return p;
				}
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		return ParticleEffect.CRIT;
	}
	
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
