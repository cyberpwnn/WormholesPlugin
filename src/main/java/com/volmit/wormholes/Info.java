package com.volmit.wormholes;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import com.volmit.wormholes.util.C;
import com.volmit.wormholes.util.TXT;

public class Info
{
	public static String TAG = TXT.makeTag(C.DARK_GRAY, C.GOLD, C.GRAY, "W");
	
	public static String PERM_RELOAD = "wormholes.reload";
	public static String PERM_LIST = "wormholes.list";
	public static String PERM_CREATE = "wormholes.create";
	public static String PERM_DESTROY = "wormholes.destroy";
	public static String PERM_BUILD = "wormholes.build";
	public static String PERM_CONFIGURE = "wormholes.configure";
	public static String PERM_USE = "wormholes.use";
	public static String HR = C.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 75);
	public static String HRN = C.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 28) + ChatColor.RESET + C.GOLD + "  %s  " + C.DARK_GRAY + ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 28);
	
	public static String hr()
	{
		return HR;
	}
	
	public static String hrn(String s)
	{
		return String.format(HRN, s);
	}
}
