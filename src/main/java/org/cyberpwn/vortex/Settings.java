package org.cyberpwn.vortex;

import java.lang.reflect.Field;
import org.cyberpwn.vortex.config.Experimental;
import wraith.Comment;
import wraith.DataCluster;

public class Settings
{
	@Comment("Should Wormholes project blocks from the other side?")
	public static boolean ENABLE_PROJECTIONS = true;
	
	@Comment("Should Local Wormholes allow non-player entities to pass through?")
	public static boolean ALLOW_ENTITIES = true;
	
	@Comment("Should Wormholes project entities from the other side?")
	public static boolean ENABLE_APERTURE = true;
	
	@Comment("The max size (width/height) a portal can be.\nMust be an odd number")
	public static int MAX_PORTAL_SIZE = 9;
	
	@Experimental
	@Comment("Modify the interval in which entities are updated, sent through bungee, and sent to players\nMust be 1 or higher")
	public static int APERTURE_MAX_SPEED = 2;
	
	@Experimental
	@Comment("Modify the distance blocks will be sampled\nEnsure this value matches across all servers.")
	public static int PROJECTION_SAMPLE_RADIUS = 25;
	
	@Experimental
	@Comment("Change the maximum tickrate projections can run\nNote: This is in place to prevent overprojecting faster than 1/4th of a second.")
	public static int PROJECTION_MAX_SPEED = 5;
	
	@Experimental
	@Comment("The interval in which portals are checked to ensure they are populated with projection maps\nThis is only called one time when the portal is created.")
	public static int NETWORK_POPULATE_MAPPING_INTERVAL = 20;
	
	@Experimental
	@Comment("The max packet size in bytes.\nLower values will force wormholes to send more packets\nIncreasing this could cause bungeecord to reject the packet.")
	public static int NETWORK_MAX_PACKET_SIZE = 40000;
	
	@Experimental
	@Comment("This adds compression to projection packets through bungeecord.\nIncreasing this past 4 increases processing time and slightly reduces the size\nTesting shows comp 4 shows the best improvement in size for compression time\nMust be from 1-9")
	public static int NETWORK_COMPRESSION_LEVEL = 4;
	
	public static DataCluster getConfig()
	{
		DataCluster cc = new DataCluster();
		
		for(Field i : Settings.class.getDeclaredFields())
		{
			try
			{
				String name = i.getName().toLowerCase().replaceAll("-", "_");
				Object value = i.get(null);
				
				if(!i.isAnnotationPresent(Experimental.class))
				{
					cc.trySet("wormholes." + name, value, ((Comment) i.getDeclaredAnnotation(Comment.class)).value());
				}
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return cc;
	}
	
	public static void setConfig(DataCluster cc)
	{
		for(Field i : Settings.class.getDeclaredFields())
		{
			try
			{
				String name = i.getName().toLowerCase().replaceAll("-", "_");
				
				if(!i.isAnnotationPresent(Experimental.class))
				{
					if(cc.contains("wormholes." + name))
					{
						i.set(null, cc.getAbstract("wormholes." + name));
					}
				}
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static DataCluster getExperimentalConfig()
	{
		DataCluster cc = new DataCluster();
		
		for(Field i : Settings.class.getDeclaredFields())
		{
			try
			{
				String name = i.getName().toLowerCase().replaceAll("-", "_");
				Object value = i.get(null);
				
				if(i.isAnnotationPresent(Experimental.class))
				{
					cc.trySet("experimental." + name, value, ((Comment) i.getDeclaredAnnotation(Comment.class)).value());
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
				String name = i.getName().toLowerCase().replaceAll("-", "_");
				
				if(i.isAnnotationPresent(Experimental.class))
				{
					if(cc.contains("experimental." + name))
					{
						i.set(null, cc.getAbstract("experimental." + name));
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
