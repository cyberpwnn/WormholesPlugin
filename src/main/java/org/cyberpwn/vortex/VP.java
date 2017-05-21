package org.cyberpwn.vortex;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cyberpwn.vortex.network.VortexBus;
import org.cyberpwn.vortex.portal.Portal;
import org.cyberpwn.vortex.provider.AutomagicalProvider;
import org.cyberpwn.vortex.provider.PortalProvider;
import org.cyberpwn.vortex.service.ApertureService;
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
	private SubGroup sub;
	
	@Override
	public void onStart()
	{
		Direction.calculatePermutations();
		instance = this;
		timings = new TimingsService();
		VP.instance.getServer().getMessenger().registerOutgoingPluginChannel(VP.instance, "BungeeCord");
		bus = new VortexBus();
		registry = new PortalRegistry();
		host = new MutexService();
		aperture = new ApertureService();
		projector = new ProjectionService();
		provider = new AutomagicalProvider();
		entity = new EntityService();
		io = new IOService();
		provider.loadAllPortals();
		sub = new SubGroup("w");
		buildSubs();
	}
	
	@Override
	public void onStop()
	{
		
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
			
			if(TICK.tick % 2 == 0)
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
				p.sendMessage(C.GRAY + "Listing " + host.getLocalPortals().size() + " Portals");
				
				for(Portal i : host.getLocalPortals())
				{
					String state = i.hasWormhole() ? i.isWormholeMutex() ? "mutex" : "local" : "no";
					p.sendMessage(i.getKey().toString() + C.GRAY + " (" + state + " link" + ") @ " + i.getPosition().getCenter().getWorld().getName() + ": " + i.getPosition().getCenter().getBlockX() + ", " + i.getPosition().getCenter().getBlockY() + ", " + i.getPosition().getCenter().getBlockZ());
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
		
		sub.add(new SubCommand("Destroys the portal looked at", "delte", "destroy", "wipe")
		{
			@Override
			public void cs(CommandSender p, String[] args)
			{
				p.sendMessage(C.RED + "Player only command");
			}
			
			@Override
			public void cp(Player p, String[] args)
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
