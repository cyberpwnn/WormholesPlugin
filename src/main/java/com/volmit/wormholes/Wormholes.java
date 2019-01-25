package com.volmit.wormholes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.volmit.wormholes.nms.Catalyst;
import com.volmit.wormholes.nms.CatalystPlugin;
import com.volmit.wormholes.nms.NMP;
import com.volmit.wormholes.nms.NMSVersion;
import com.volmit.wormholes.portal.PortalType;
import com.volmit.wormholes.util.C;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.MSound;

public class Wormholes extends JavaPlugin implements Listener
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
	public void onEnable()
	{
		tag = C.DARK_GRAY + "[" + C.GOLD + C.BOLD + "W" + C.RESET + C.DARK_GRAY + "]: " + C.GRAY;
		handleNMS();
		Direction.calculatePermutations();
		registerListener(instance = this);
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
		CatalystPlugin.plugin = this;
		Catalyst.host.start();
		NMP.host = Catalyst.host;
		NMSVersion v = NMSVersion.current();

		if(v != null)
		{
			getLogger().info("Selected " + NMSVersion.current().name());
		}

		else
		{
			getLogger().info("Could not find a suitable binder for this server version!");
		}
	}

	@Override
	public void onDisable()
	{
		try
		{
			blockManager.destroyAll();
			Catalyst.host.stop();
		}

		catch(Throwable e)
		{

		}
	}

	public static void registerListener(Listener l)
	{
		Bukkit.getPluginManager().registerEvents(l, instance);
	}

	public static void unregisterListener(Listener l)
	{
		HandlerList.unregisterAll(l);
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
}
