package com.volmit.wormholes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.volmit.wormholes.config.Permissable;
import com.volmit.wormholes.network.VortexBus;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.provider.AutomagicalProvider;
import com.volmit.wormholes.provider.BaseProvider;
import com.volmit.wormholes.provider.PortalProvider;
import com.volmit.wormholes.service.ApertureService;
import com.volmit.wormholes.service.EffectService;
import com.volmit.wormholes.service.EntityService;
import com.volmit.wormholes.service.IOService;
import com.volmit.wormholes.service.MutexService;
import com.volmit.wormholes.service.PortalRegistry;
import com.volmit.wormholes.service.ProjectionService;
import com.volmit.wormholes.service.TimingsService;
import wraith.C;
import wraith.ColoredString;
import wraith.ControllablePlugin;
import wraith.Direction;
import wraith.RTEX;
import wraith.RTX;
import wraith.SYM;
import wraith.SubCommand;
import wraith.SubGroup;
import wraith.TICK;
import wraith.TXT;
import wraith.TickHandle;
import wraith.TickHandler;
import wraith.Ticked;

@Ticked(0)
@TickHandle(TickHandler.SYNCED)
public class Wormholes extends ControllablePlugin
{
	public static Wormholes instance;
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
		Wormholes.instance.getServer().getMessenger().registerOutgoingPluginChannel(Wormholes.instance, "BungeeCord");
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
		Status.fdq = true;
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
			
			if(TICK.tick % Settings.CHUNK_SEND_RATE == 0)
			{
				provider.getRasterer().flushRasterQueue();
			}
			
			if(TICK.tick % 20 == 0)
			{
				Status.avgBPS.put(Status.packetBytesPerSecond);
				Status.packetBytesPerSecond = 0;
				Status.pps = Status.permutationsPerSecond;
				Status.permutationsPerSecond = 0;
				Status.lightFaulted = Status.lightFault;
				Status.lightFault = 0;
			}
		}
		
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	public void doReload()
	{
		Status.fdq = true;
		Wormholes.provider.getRasterer().dequeueAll();
		Wormholes.provider.getRasterer().flush();
		host.globalReload();
		Bukkit.getPluginManager().disablePlugin(Wormholes.instance);
		Bukkit.getPluginManager().enablePlugin(Wormholes.instance);
	}
	
	private void buildSubs()
	{
		sub.add(new SubCommand("Lists all portals & links", "list", "li", "l")
		{
			private void list(CommandSender p, String[] a)
			{
				if(new Permissable(p).canList())
				{
					if(a.length == 2)
					{
						if(a[0].equalsIgnoreCase("-tp"))
						{
							String to = a[1];
							World world = Bukkit.getWorld(to.split(",")[0]);
							int x = Integer.valueOf(to.split(",")[1]);
							int y = Integer.valueOf(to.split(",")[2]);
							int z = Integer.valueOf(to.split(",")[3]);
							Location v = new Location(world, x, y, z);
							Portal por = registry.getPortalsInCloseView(v).get(0);
							((Player) p).teleport(v.clone().add(por.getIdentity().getFront().toVector().clone()));
							return;
						}
						
						if(a[0].equalsIgnoreCase("-dl"))
						{
							if(new Permissable(p).canDestroy())
							{
								String to = a[1];
								World world = Bukkit.getWorld(to.split(",")[0]);
								int x = Integer.valueOf(to.split(",")[1]);
								int y = Integer.valueOf(to.split(",")[2]);
								int z = Integer.valueOf(to.split(",")[3]);
								Location v = new Location(world, x, y, z);
								Portal por = registry.getPortalsInCloseView(v).get(0);
								host.removeLocalPortal(por);
							}
						}
					}
					
					p.sendMessage(Info.hrn(host.getLocalPortals().size() + " Portals"));
					
					for(Portal i : host.getLocalPortals())
					{
						RTX r = new RTX();
						RTEX b = new RTEX(new ColoredString(C.dyeToChat(i.getKey().getU()), SYM.SHAPE_SQUARE + ""), new ColoredString(C.dyeToChat(i.getKey().getD()), SYM.SHAPE_SQUARE + ""), new ColoredString(C.dyeToChat(i.getKey().getL()), SYM.SHAPE_SQUARE + ""), new ColoredString(C.dyeToChat(i.getKey().getR()), SYM.SHAPE_SQUARE + "\n"), new ColoredString(C.LIGHT_PURPLE, "Link: "), new ColoredString(C.WHITE, i.hasWormhole() ? i.isWormholeMutex() ? "Mutex Link\n" : "Local Link\n" : "No Link\n"), new ColoredString(C.LIGHT_PURPLE, "Polarity: "), new ColoredString(C.WHITE, i.getIdentity().getFront().toString()));
						r.addText("Portal <", C.GRAY);
						r.addTextHover(SYM.SHAPE_SQUARE + "", b, C.dyeToChat(i.getKey().getU()));
						r.addTextHover(SYM.SHAPE_SQUARE + "", b, C.dyeToChat(i.getKey().getD()));
						r.addTextHover(SYM.SHAPE_SQUARE + "", b, C.dyeToChat(i.getKey().getL()));
						r.addTextHover(SYM.SHAPE_SQUARE + "", b, C.dyeToChat(i.getKey().getR()));
						r.addText("> ", C.GRAY);
						r.addText("@ " + i.getPosition().getCenter().getWorld().getName() + " [" + i.getPosition().getCenter().getBlockX() + " " + i.getPosition().getCenter().getBlockY() + " " + i.getPosition().getCenter().getBlockZ() + "]", C.GRAY);
						r.addTextFireHoverCommand(" [TP]", new RTEX(new ColoredString(C.GREEN, "Teleport to this portal")), "/w list -tp " + i.getPosition().getCenter().getWorld().getName() + "," + i.getPosition().getCenter().getBlockX() + "," + i.getPosition().getCenter().getBlockY() + "," + i.getPosition().getCenter().getBlockZ(), C.GREEN);
						r.addTextFireHoverCommand(" [DELETE]", new RTEX(new ColoredString(C.RED, "DELETE to this portal")), "/w list -dl " + i.getPosition().getCenter().getWorld().getName() + "," + i.getPosition().getCenter().getBlockX() + "," + i.getPosition().getCenter().getBlockY() + "," + i.getPosition().getCenter().getBlockZ(), C.RED);
						r.tellRawTo((Player) p);
					}
					
					p.sendMessage(Info.hr());
				}
				
				else
				{
					p.sendMessage(Info.TAG + "No Permission");
				}
			}
			
			@Override
			public void cs(CommandSender p, String[] args)
			{
				for(Portal i : host.getLocalPortals())
				{
					p.sendMessage(i.getKey().toString());
				}
			}
			
			@Override
			public void cp(Player p, String[] args)
			{
				list(p, args);
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
		
		sub.add(new SubCommand("Realtime sample information", "debug", "db", "vb")
		{
			@Override
			public void cs(CommandSender p, String[] args)
			{
				p.sendMessage("Ingame Only");
			}
			
			@Override
			public void cp(Player p, String[] args)
			{
				if(new Permissable(p).canReload())
				{
					if(((BaseProvider) provider).isDebugging(p))
					{
						((BaseProvider) provider).dedebug(p);
					}
					
					else
					{
						((BaseProvider) provider).debug(p);
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
					doReload();
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
