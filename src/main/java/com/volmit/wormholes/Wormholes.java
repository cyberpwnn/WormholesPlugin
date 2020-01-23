package com.volmit.wormholes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.volmit.wormholes.nms.Catalyst;
import com.volmit.wormholes.nms.CatalystPlugin;
import com.volmit.wormholes.nms.NMP;
import com.volmit.wormholes.nms.NMSVersion;
import com.volmit.wormholes.portal.PortalType;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.J;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.MSound;

import mortar.bukkit.plugin.MortarPlugin;
import mortar.util.text.C;
import mortar.util.text.TXT;

public class Wormholes extends MortarPlugin implements Listener
{
	public static String tag;
	public static Wormholes instance;
	public static TraversableManager traversableManager;
	public static BlockManager blockManager;
	public static EffectManager effectManager;
	public static ConstructionManager constructionManager;
	public static PortalManager portalManager;
	public static NetworkManager networkManager;
	public static ProjectionManager projectionManager;

	@Override
	public void start()
	{
		instance = this;
		tag = C.DARK_GRAY + "[" + C.GOLD + C.BOLD + "W" + C.RESET + C.DARK_GRAY + "]: " + C.GRAY;
		handleNMS();
		Direction.calculatePermutations();
		registerListener(instance);
		registerListener(blockManager = new BlockManager());
		registerListener(effectManager = new EffectManager());
		registerListener(constructionManager = new ConstructionManager());
		registerListener(portalManager = new PortalManager());
		registerListener(traversableManager = new TraversableManager());
		registerListener(networkManager = new NetworkManager());
		registerListener(projectionManager = new ProjectionManager());
	}

	private void handleNMS()
	{
		v("Init NMS");
		CatalystPlugin.plugin = this;
		Catalyst.host.start();
		v("Catalyst Started for NMS");
		NMP.host = Catalyst.host;
		NMSVersion v = NMSVersion.current();

		if(v != null)
		{
			v("Selected " + NMSVersion.current().name());
		}

		else
		{
			w("Could not find a suitable binder for this server version!");
		}
	}

	@Override
	public void stop()
	{
		try
		{
			blockManager.destroyAll();
			Catalyst.host.stop();
			portalManager.shutDown();
		}

		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public void log(String s)
	{
		if(Bukkit.isPrimaryThread())
		{
			Bukkit.getConsoleSender().sendMessage(C.GOLD + "[Wormholes] " + C.GRAY + s);
		}

		else
		{
			J.s(() -> log(s));
		}
	}

	public static void l(String s)
	{
		instance.log(s);
	}

	public static void v(String s)
	{
		if(!Settings.DEBUG)
		{
			return;
		}

		instance.log(C.DARK_AQUA + s);
	}

	public static void w(String s)
	{
		instance.log(C.YELLOW + s);
	}

	public static void f(String s)
	{
		instance.log(C.RED + s);
	}

	@Override
	public boolean onCommand(CommandSender s, Command command, String label, String[] args)
	{
		if(command.getName().equals("wormholes"))
		{
			Player p = s instanceof Player ? (Player) s : null;

			if(args.length == 0)
			{
				s.sendMessage(tag + "/w wand - Obtain portal wand");
				s.sendMessage(tag + "/w rune [gateway/wormhole/portal] - Obtain portal runes.");
				s.sendMessage(tag + "/w reload - Reload Wormholes");
			}

			else if(args.length == 1 && args[0].equalsIgnoreCase("wand"))
			{
				if(prm(s, "wormholes.admin.items") && plr(s))
				{
					p.getInventory().addItem(blockManager.getWand());
					p.playSound(p.getLocation(), MSound.EYE_DEATH.bukkitSound(), 0.75f, 1.25f);
				}
			}

			else if(args.length >= 1 && args[0].equalsIgnoreCase("rune"))
			{
				if(prm(s, "wormholes.admin.items") && plr(s))
				{
					PortalType t = PortalType.GATEWAY;
					int stack = 64;

					if(args.length > 1)
					{
						if(args[1].equalsIgnoreCase("w") || args[1].equalsIgnoreCase("wormhole"))
						{
							t = PortalType.WORMHOLE;
						}

						if(args[1].equalsIgnoreCase("p") || args[1].equalsIgnoreCase("portal"))
						{
							t = PortalType.PORTAL;
						}
					}

					if(args.length > 2)
					{
						try
						{
							stack = M.iclip(Integer.valueOf(args[2]), 1, 64);
						}

						catch(Throwable e)
						{

						}
					}

					ItemStack st = blockManager.get(t, stack);
					p.getInventory().addItem(st);
					p.playSound(p.getLocation(), MSound.EYE_DEATH.bukkitSound(), 0.75f, 1.25f);
				}
			}

			else
			{
				s.sendMessage(tag + "Unknown Subcommand. Use /wormholes.");
			}

			return true;
		}

		return false;
	}

	public boolean prm(CommandSender s, String perm)
	{
		if(s.hasPermission(perm))
		{
			return true;
		}

		s.sendMessage(tag + "Missing Permission: " + C.WHITE + perm);
		return false;
	}

	public boolean plr(CommandSender s)
	{
		if(s instanceof Player)
		{
			return true;
		}

		s.sendMessage(tag + "Ingame only command.");
		return false;
	}

	@Override
	public String getTag(String subTag)
	{
		return TXT.makeTag(C.GOLD, C.DARK_GRAY, C.GRAY, "Wormholes");
	}
}
