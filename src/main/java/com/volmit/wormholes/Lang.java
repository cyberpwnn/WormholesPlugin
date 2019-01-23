package com.volmit.wormholes;

import java.lang.reflect.Field;
import com.volmit.wormholes.config.CName;
import com.volmit.wormholes.util.DataCluster;

public class Lang
{
	@CName("STATUS_POW")
	public static String STATUS_POW = "POW";
	
	@CName("STATUS_WRK")
	public static String STATUS_WRK = "WRK";
	
	@CName("STATUS_NET")
	public static String STATUS_NET = "NET";
	
	@CName("STATUS_PRJ")
	public static String STATUS_PRJ = "PRJ";
	
	@CName("STATUS_BNJ")
	public static String STATUS_BNJ = "BNJ";
	
	@CName("STATUS_MCV")
	public static String STATUS_MCV = "MCV";
	
	@CName("STATUS_APV")
	public static String STATUS_APV = "APV";
	
	@CName("STATUS_LPV")
	public static String STATUS_LPV = "LPV";
	
	@CName("DESCRIPTION_LIST")
	public static String DESCRIPTION_LIST = "Lists all portals & links";
	
	@CName("DESCRIPTION_TELEPORT")
	public static String DESCRIPTION_TELEPORT = "Teleport to this portal";
	
	@CName("DESCRIPTION_DELETE")
	public static String DESCRIPTION_DELETE = "Lists all portals & links";
	
	@CName("DESCRIPTION_NOPERMISSION")
	public static String DESCRIPTION_NOPERMISSION = "No Permission";
	
	@CName("DESCRIPTION_TIMINGS")
	public static String DESCRIPTION_TIMINGS = "Pull internal timings data";
	
	@CName("DESCRIPTION_VERSION")
	public static String DESCRIPTION_VERSION = "Version Information";
	
	@CName("DESCRIPTION_WAND")
	public static String DESCRIPTION_WAND = "Get a Portal Building Wand";
	
	@CName("DESCRIPTION_INGAMEONLY")
	public static String DESCRIPTION_INGAMEONLY = "Ingame (player) only";
	
	@CName("DESCRIPTION_SYSTEM")
	public static String DESCRIPTION_SYSTEM = "System Information";
	
	@CName("DESCRIPTION_DEBUG")
	public static String DESCRIPTION_DEBUG = "Realtime sample information";
	
	@CName("DESCRIPTION_UPDATENAME")
	public static String DESCRIPTION_UPDATENAME = "Updated Portal Name";
	
	@CName("DESCRIPTION_INVALIDKEY")
	public static String DESCRIPTION_INVALIDKEY = "Invalid Portal Key";
	
	@CName("DESCRIPTION_INVALIDPOS")
	public static String DESCRIPTION_INVALIDPOS = "Invalid Portal Position";
	
	@CName("DESCRIPTION_DUPEKEY")
	public static String DESCRIPTION_DUPEKEY = "Duplicate Portal Key";
	
	@CName("DESCRIPTION_RELOAD")
	public static String DESCRIPTION_RELOAD = "Reloads Wormholes & Configs";
	
	@CName("DESCRIPTION_RELOADED")
	public static String DESCRIPTION_RELOADED = "All Wormholes & Configs Reloaded";
	
	@CName("DESCRIPTION_SHIFTSCROLL")
	public static String DESCRIPTION_SHIFTSCROLL = "Shift + Scroll";
	
	@CName("DESCRIPTION_CHANGESIZE")
	public static String DESCRIPTION_CHANGESIZE = "Change portal size";
	
	@CName("DESCRIPTION_LEFTCLICK")
	public static String DESCRIPTION_LEFTCLICK = "Left Click";
	
	@CName("DESCRIPTION_PLACEFRAME")
	public static String DESCRIPTION_PLACEFRAME = "Place frame";
	
	@CName("DESCRIPTION_PORTALWAND")
	public static String DESCRIPTION_PORTALWAND = "Portal Wand";
	
	@CName("DESCRIPTION_POSSELECT")
	public static String DESCRIPTION_POSSELECT = "Position Selected";
	
	@CName("DESCRIPTION_LEFTCLICKCONFIRM")
	public static String DESCRIPTION_LEFTCLICKCONFIRM = "Left click to confirm & place";
	
	@CName("DESCRIPTION_FRAMEPLACED")
	public static String DESCRIPTION_FRAMEPLACED = "Frame Placed";
	
	@CName("DESCRIPTION_POSCANCEL")
	public static String DESCRIPTION_POSCANCEL = "Position Cancelled";
	
	@CName("DESCRIPTION_WIPEHOLO")
	public static String DESCRIPTION_WIPEHOLO = "Remove dead holograms within 6 blocks of you.";
	
	@CName("DESCRIPTION_COOLDOWNACTIVE")
	public static String DESCRIPTION_COOLDOWNACTIVE = "Cooldown Active";
	
	@CName("DESCRIPTION_WAITFORTELEPORT")
	public static String DESCRIPTION_WAITFORTELEPORT = "You must wait before teleporting.";
	
	@CName("DESCRIPTION_HIDETIPS")
	public static String DESCRIPTION_HIDETIPS = "Click to hide tips for just you.";
	
	@CName("DESCRIPTION_PLEASEWAIT")
	public static String DESCRIPTION_PLEASEWAIT = "Please Wait...";
	
	@CName("DESCRIPTION_UNABLETOCONFIGURE")
	public static String DESCRIPTION_UNABLETOCONFIGURE = "Unable to Configure";
	
	@CName("DESCRIPTION_SOMEONEELSECONFIGURING")
	public static String DESCRIPTION_SOMEONEELSECONFIGURING = "Someone else is configuring this portal.";
	
	@CName("DESCRIPTION_MENU_ENTITIES")
	public static String DESCRIPTION_MENU_ENTITIES = "Toggle the permission for entities to use this portal";
	
	@CName("DESCRIPTION_MENU_APERTURE")
	public static String DESCRIPTION_MENU_APERTURE = "Toggle entity projections";
	
	@CName("DESCRIPTION_MENU_PROJECT")
	public static String DESCRIPTION_MENU_PROJECT = "Toggle block projections";
	
	@CName("DESCRIPTION_MENU_POLARITY")
	public static String DESCRIPTION_MENU_POLARITY = "Reverse the 'front facing' direction of this portal.";
	
	@CName("DESCRIPTION_MENU_DESTROYPORTAL")
	public static String DESCRIPTION_MENU_DESTROYPORTAL = "Destroy this portal?";
	
	@CName("DESCRIPTION_MENU_EXIT")
	public static String DESCRIPTION_MENU_EXIT = "Exit this menu (or just walk away from it)";
	
	@CName("DESCRIPTION_MENU_DIRECTIONAL")
	public static String DESCRIPTION_MENU_DIRECTIONAL = "Setting portals to omni-directional will hide the destination.";
	
	@CName("WORD_PORTALS")
	public static String WORD_PORTALS = "Portals";
	
	@CName("WORD_SIZE")
	public static String WORD_SIZE = "Size";
	
	@CName("WORD_PORTAL")
	public static String WORD_PORTAL = "Portal";
	
	@CName("WORD_ENDPOINT")
	public static String WORD_ENDPOINT = "Endpoint";
	
	@CName("WORD_LINK")
	public static String WORD_LINK = "Link";
	
	@CName("WORD_MUTEX")
	public static String WORD_MUTEX = "Mutex";
	
	@CName("WORD_LOCAL")
	public static String WORD_LOCAL = "Local";
	
	@CName("WORD_NO")
	public static String WORD_NO = "No";
	
	@CName("WORD_POLARITY")
	public static String WORD_POLARITY = "Polarity";
	
	@CName("WORD_TP")
	public static String WORD_TP = "TP";
	
	@CName("WORD_DELETE")
	public static String WORD_DELETE = "Delete";
	
	@CName("WORD_THREADS")
	public static String WORD_THREADS = "Threads";
	
	@CName("WORD_POWER")
	public static String WORD_POWER = "Power";
	
	@CName("WORD_WORKER")
	public static String WORD_WORKER = "Worker";
	
	@CName("WORD_UTILIZATION")
	public static String WORD_UTILIZATION = "Utilization";
	
	@CName("WORD_EFFECTIVETPS")
	public static String WORD_EFFECTIVETPS = "Effective TPS";
	
	@CName("WORD_SYNC")
	public static String WORD_SYNC = "Sync";
	
	@CName("WORD_ASYNC")
	public static String WORD_ASYNC = "Async";
	
	@CName("WORD_RUNNING")
	public static String WORD_RUNNING = "Running";
	
	@CName("BUTTON_HIDETIPS")
	public static String BUTTON_HIDETIPS = "HIDE TIPS";
	
	@CName("MENU_ENTITIES")
	public static String MENU_ENTITIES = "Entities";
	
	@CName("MENU_APERTURE")
	public static String MENU_APERTURE = "Project Entities";
	
	@CName("MENU_PROJECT")
	public static String MENU_PROJECT = "Project Blocks";
	
	@CName("MENU_REVERSE")
	public static String MENU_REVERSE = "Reverse Polarity";
	
	@CName("MENU_DESTROY")
	public static String MENU_DESTROY = "Destroy";
	
	@CName("MENU_EXIT")
	public static String MENU_EXIT = "Exit";
	
	@CName("MENU_SET")
	public static String MENU_SET = "Set";
	
	@CName("MENU_UNIDIRECTIONAL")
	public static String MENU_UNIDIRECTIONAL = "Uni-Directional";
	
	@CName("MENU_BIDIRECTIONAL")
	public static String MENU_BIDIRECTIONAL = "Bi-Directional";
	
	@CName("MENU_RTP_AUTOREFRESH")
	public static String MENU_MENU_RTP_AUTOREFRESH = "Auto Refresh";
	
	@CName("MENU_RTP_RANDOMTP")
	public static String MENU_RTP_RANDOMTP = "Random Teleport";
	
	@CName("MENU_RTP_TARGET")
	public static String MENU_RTP_TARGET = "Target Biome";
	
	@CName("MENU_RTP_MAX")
	public static String MENU_RTP_MAX = "Max Distance";
	
	@CName("MENU_RTP_MIN")
	public static String MENU_RTP_MIN = "Min Distance";
	
	@CName("MENU_RANDOM_TP")
	public static String MENU_RANDOM_TP = "Random TP";
	
	@CName("MENU_OPTIONS")
	public static String MENU_OPTIONS = "Options";
	
	@CName("MENU_ACTIONS")
	public static String MENU_ACTIONS = "Actions";
	
	@CName("MENU_OTHER")
	public static String MENU_OTHER = "Other";
	
	public static DataCluster getConfig()
	{
		DataCluster cc = new DataCluster();
		
		for(Field i : Lang.class.getDeclaredFields())
		{
			try
			{
				CName n = i.getAnnotation(CName.class);
				
				if(n != null)
				{
					String name = n.value().toLowerCase().replaceAll("_", ".");
					Object value = i.get(null);
					
					cc.trySet("lang." + name, value, "Default: '" + value.toString() + "'");
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
		for(Field i : Lang.class.getDeclaredFields())
		{
			try
			{
				CName n = i.getAnnotation(CName.class);
				
				if(n != null)
				{
					String name = i.getName().toLowerCase().replaceAll("_", ".");
					
					if(cc.contains("lang." + name))
					{
						i.set(null, cc.getAbstract("lang." + name));
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
