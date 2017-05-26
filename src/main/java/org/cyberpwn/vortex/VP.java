package org.cyberpwn.vortex;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyberpwn.vortex.config.Permissable;
import org.cyberpwn.vortex.network.VortexBus;
import org.cyberpwn.vortex.portal.Portal;
import org.cyberpwn.vortex.provider.AutomagicalProvider;
import org.cyberpwn.vortex.provider.PortalProvider;
import org.cyberpwn.vortex.service.ApertureService;
import org.cyberpwn.vortex.service.EffectService;
import org.cyberpwn.vortex.service.EntityService;
import org.cyberpwn.vortex.service.IOService;
import org.cyberpwn.vortex.service.MutexService;
import org.cyberpwn.vortex.service.PortalRegistry;
import org.cyberpwn.vortex.service.ProjectionService;
import org.cyberpwn.vortex.service.TimingsService;
import wraith.C;
import wraith.ControllablePlugin;
import wraith.Direction;
import wraith.SubCommand;
import wraith.SubGroup;
import wraith.TICK;
import wraith.TXT;
import wraith.TickHandle;
import wraith.TickHandler;
import wraith.Ticked;

@Ticked(0)
@TickHandle(TickHandler.SYNCED)
public class VP extends ControllablePlugin
{
	public static VP instance;
	public static VortexBus bus;
	public static MutexService host;
	public static PortalProvider provider;
	public static PortalRegistry registry;
	public static ApertureService aperture;
	public static ProjectionService projector;
	public static TimingsService timings;
	public static EntityService entity;
	public static IOService io;
	public static EffectService fx;
	private SubGroup sub;
	
	@Override
	public void onStart()
	{
		Direction.calculatePermutations();
		instance = this;
		io = new IOService();
		timings = new TimingsService();
		VP.instance.getServer().getMessenger().registerOutgoingPluginChannel(VP.instance, "BungeeCord");
		bus = new VortexBus();
		registry = new PortalRegistry();
		host = new MutexService();
		aperture = new ApertureService();
		projector = new ProjectionService();
		provider = new AutomagicalProvider();
		entity = new EntityService();
		provider.loadAllPortals();
		sub = new SubGroup("w");
		fx = new EffectService();
		buildSubs();
	}
	
	@Override
	public void onStop()
	{
		host.dequeueAll();
	}
	
	@Override
	public void onTick()
	{
		try
		{
			bus.flush();
			host.flush();
			provider.flush();
			projector.flush();
			
			if(TICK.tick % Settings.APERTURE_MAX_SPEED == 0)
			{
				aperture.flush();
				entity.flush();
			}
		}
		
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	private void buildSubs()
	{
		sub.add(new SubCommand("Lists all portals & links", "list", "li", "l")
		{
			private void list(CommandSender p)
			{
				if(new Permissable(p).canList())
				{
					p.sendMessage(C.GRAY + "Listing " + host.getLocalPortals().size() + " Portals");
					
					for(Portal i : host.getLocalPortals())
					{
						String state = i.hasWormhole() ? i.isWormholeMutex() ? "mutex" : "local" : "no";
						p.sendMessage(i.getKey().toString() + C.GRAY + " (" + state + " link" + ") @ " + i.getPosition().getCenter().getWorld().getName() + ": " + i.getPosition().getCenter().getBlockX() + ", " + i.getPosition().getCenter().getBlockY() + ", " + i.getPosition().getCenter().getBlockZ());
					}
				}
				
				else
				{
					p.sendMessage(Info.TAG + "No Permission");
				}
			}
			
			@Override
			public void cs(CommandSender p, String[] args)
			{
				list(p);
			}
			
			@Override
			public void cp(Player p, String[] args)
			{
				list(p);
			}
		});
		
		sub.add(new SubCommand("Lists all portals & links", "list", "li", "l")
		{
			private void list(CommandSender p)
			{
				if(new Permissable(p).canList())
				{
					p.sendMessage(C.GRAY + "Listing " + host.getLocalPortals().size() + " Portals");
					
					for(Portal i : host.getLocalPortals())
					{
						String state = i.hasWormhole() ? i.isWormholeMutex() ? "mutex" : "local" : "no";
						p.sendMessage(i.getKey().toString() + C.GRAY + " (" + state + " link" + ") @ " + i.getPosition().getCenter().getWorld().getName() + ": " + i.getPosition().getCenter().getBlockX() + ", " + i.getPosition().getCenter().getBlockY() + ", " + i.getPosition().getCenter().getBlockZ());
					}
				}
				
				else
				{
					p.sendMessage(Info.TAG + "No Permission");
				}
			}
			
			@Override
			public void cs(CommandSender p, String[] args)
			{
				list(p);
			}
			
			@Override
			public void cp(Player p, String[] args)
			{
				list(p);
			}
		});
		
		sub.add(new SubCommand("Pull internal timings data", "timings", "t", "perf")
		{
			private void list(CommandSender p)
			{
				if(new Permissable(p).canList())
				{
					p.sendMessage(TXT.line(C.DARK_GRAY, 24));
					for(String i : TimingsService.root.toLines(0, 2))
					{
						p.sendMessage(i);
					}
					p.sendMessage(TXT.line(C.DARK_GRAY, 24));
					for(String i : TimingsService.asyn.toLines(0, 2))
					{
						p.sendMessage(i);
					}
					p.sendMessage(TXT.line(C.DARK_GRAY, 24));
				}
				
				else
				{
					p.sendMessage(Info.TAG + "No Permission");
				}
			}
			
			@Override
			public void cs(CommandSender p, String[] args)
			{
				list(p);
			}
			
			@Override
			public void cp(Player p, String[] args)
			{
				list(p);
			}
		});
		
		sub.add(new SubCommand("Destroys the portal looked at", "destroy", "del", "wipe")
		{
			@Override
			public void cs(CommandSender p, String[] args)
			{
				p.sendMessage(C.RED + "Player only command");
			}
			
			@Override
			public void cp(Player p, String[] args)
			{
				if(new Permissable(p).canDestroy())
				{
					Portal portal = VP.registry.getPortalLookingAt(p);
					
					if(portal != null)
					{
						p.sendMessage(C.GREEN + "Destroyed Portal");
						VP.host.removeLocalPortal(portal);
					}
					
					else
					{
						p.sendMessage(C.RED + "Must be looking at a portal");
					}
				}
				
				else
				{
					p.sendMessage(Info.TAG + "No Permission");
				}
			}
		});
		
		sub.add(new SubCommand("Reloads Wormholes & Configs", "reload", "reset")
		{
			public void go(CommandSender p)
			{
				if(new Permissable(p).canReload())
				{
					Bukkit.getPluginManager().disablePlugin(VP.instance);
					Bukkit.getPluginManager().enablePlugin(VP.instance);
					p.sendMessage(Info.TAG + "All Wormholes & Configs Reloaded");
				}
				
				else
				{
					p.sendMessage(Info.TAG + "No Permission");
				}
			}
			
			@Override
			public void cs(CommandSender p, String[] args)
			{
				go(p);
			}
			
			@Override
			public void cp(Player p, String[] args)
			{
				go(p);
			}
		});
	}
	
	@Override
	public void onConstruct()
	{
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("wormhole"))
		{
			sub.hit(sender, args);
			
			return true;
		}
		
		return false;
	}
}
