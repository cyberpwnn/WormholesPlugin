package com.volmit.wormholes;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import com.volmit.wormholes.config.Permissable;
import com.volmit.wormholes.network.VortexBus;
import com.volmit.wormholes.portal.LocalPortal;
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
import com.volmit.wormholes.service.SkinService;
import com.volmit.wormholes.service.TimingsService;
import com.volmit.wormholes.util.C;
import com.volmit.wormholes.util.ColoredString;
import com.volmit.wormholes.util.ControllablePlugin;
import com.volmit.wormholes.util.DB;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.EntityHologram;
import com.volmit.wormholes.util.F;
import com.volmit.wormholes.util.P;
import com.volmit.wormholes.util.ParallelPoolManager;
import com.volmit.wormholes.util.QueueMode;
import com.volmit.wormholes.util.RTEX;
import com.volmit.wormholes.util.RTX;
import com.volmit.wormholes.util.SYM;
import com.volmit.wormholes.util.SubCommand;
import com.volmit.wormholes.util.SubGroup;
import com.volmit.wormholes.util.TICK;
import com.volmit.wormholes.util.TickHandle;
import com.volmit.wormholes.util.TickHandler;
import com.volmit.wormholes.util.Ticked;

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
	public static SkinService skin;
	public static IOService io;
	public static EffectService fx;
	public static ParallelPoolManager pool;
	private SubGroup sub;
	private DB dispatcher;
	
	@Override
	public void onStart()
	{
		DB.rdebug = new File(getDataFolder(), "debug").exists();
		DB.d(this, "Starting Wormholes");
		instance = this;
		Direction.calculatePermutations();
		io = new IOService();
		pool = new ParallelPoolManager("Power Thread", Settings.WORMHOLE_POWER_THREADS, QueueMode.ROUND_ROBIN);
		timings = new TimingsService();
		Wormholes.instance.getServer().getMessenger().registerOutgoingPluginChannel(Wormholes.instance, "BungeeCord");
		bus = new VortexBus();
		registry = new PortalRegistry();
		host = new MutexService();
		aperture = new ApertureService();
		projector = new ProjectionService();
		provider = new AutomagicalProvider();
		entity = new EntityService();
		skin = new SkinService();
		provider.loadAllPortals();
		sub = new SubGroup("w");
		fx = new EffectService();
		buildSubs();
		pool.start();
		dispatcher = new DB("Wormholes");
		Info.buildBlocks();
		DB.d(this, "Initial Startup Complete");
		Info.splash();
	}
	
	@Override
	public void onStop()
	{
		DB.d(this, "Stopping Wormholes");
		DB.d(this, "Clearing Portals");
		for(Portal i : host.getLocalPortals())
		{
			((LocalPortal) i).clearHolograms();
		}
		
		DB.d(this, "Dequeue Host");
		Status.fdq = true;
		host.dequeueAll();
		DB.d(this, "Shut down power thread pool");
		pool.shutdown();
		DB.d(this, "Shut down entity service");
		entity.shutdown();
		DB.d(this, "Shut down");
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
				Status.avgBGY.put(Status.bgg);
				Status.bgg = 0;
			}
			
			if(TICK.tick % Settings.WORMHOLE_IDLE_FLUSH == 0)
			{
				for(Player i : P.onlinePlayers())
				{
					provider.movePlayer(i);
					
					for(Portal j : registry.getLocalPortals())
					{
						((LocalPortal) j).getMask().sched(i);
					}
				}
			}
			
			if(TICK.tick % Settings.WORMHOLE_SKIN_FLUSH == 0)
			{
				skin.flush();
			}
			
			Status.sample();
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
		for(Entity j : EntityHologram.lock)
		{
			j.remove();
		}
		Bukkit.getPluginManager().disablePlugin(Wormholes.instance);
		Bukkit.getPluginManager().enablePlugin(Wormholes.instance);
	}
	
	private void buildSubs()
	{
		DB.d(this, "Building Sub commands");
		sub.add(new SubCommand(Lang.DESCRIPTION_LIST, "list", "li", "l")
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
					
					p.sendMessage(Info.hrn(host.getLocalPortals().size() + " " + Lang.WORD_PORTALS));
					
					for(Portal i : host.getLocalPortals())
					{
						RTX r = new RTX();
						RTEX b = new RTEX(new ColoredString(C.dyeToChat(i.getKey().getU()), SYM.SHAPE_SQUARE + ""), new ColoredString(C.dyeToChat(i.getKey().getD()), SYM.SHAPE_SQUARE + ""), new ColoredString(C.dyeToChat(i.getKey().getL()), SYM.SHAPE_SQUARE + ""), new ColoredString(C.dyeToChat(i.getKey().getR()), SYM.SHAPE_SQUARE + "\n"), new ColoredString(C.GOLD, Lang.WORD_LINK + ": "), new ColoredString(C.WHITE, i.hasWormhole() ? i.isWormholeMutex() ? Lang.WORD_MUTEX + " " + Lang.WORD_LINK + "\n" : Lang.WORD_LOCAL + " " + Lang.WORD_LINK + "\n" : Lang.WORD_NO + " " + Lang.WORD_LINK + "\n"), new ColoredString(C.GOLD, Lang.WORD_POLARITY + ": "), new ColoredString(C.WHITE, i.getIdentity().getFront().toString()));
						
						if(i.getSided())
						{
							r.addText(Lang.WORD_ENDPOINT + " <", C.GRAY);
						}
						
						else
						{
							r.addText(Lang.WORD_PORTAL + " <", C.GRAY);
						}
						
						r.addTextHover(SYM.SHAPE_SQUARE + "", b, C.dyeToChat(i.getKey().getU()));
						r.addTextHover(SYM.SHAPE_SQUARE + "", b, C.dyeToChat(i.getKey().getD()));
						r.addTextHover(SYM.SHAPE_SQUARE + "", b, C.dyeToChat(i.getKey().getL()));
						r.addTextHover(SYM.SHAPE_SQUARE + "", b, C.dyeToChat(i.getKey().getR()));
						r.addText("> ", C.GRAY);
						
						if(i.hasDisplayName())
						{
							r.addText("(" + i.getDisplayName() + ")", C.WHITE);
						}
						
						r.addTextFireHoverCommand(" [" + Lang.WORD_TP.toUpperCase() + "]", new RTEX(new ColoredString(C.GREEN, Lang.DESCRIPTION_TELEPORT)), "/w list -tp " + i.getPosition().getCenter().getWorld().getName() + "," + i.getPosition().getCenter().getBlockX() + "," + i.getPosition().getCenter().getBlockY() + "," + i.getPosition().getCenter().getBlockZ(), C.GREEN);
						r.addTextFireHoverCommand(" [" + Lang.WORD_DELETE.toUpperCase() + "]", new RTEX(new ColoredString(C.RED, Lang.DESCRIPTION_DELETE)), "/w list -dl " + i.getPosition().getCenter().getWorld().getName() + "," + i.getPosition().getCenter().getBlockX() + "," + i.getPosition().getCenter().getBlockY() + "," + i.getPosition().getCenter().getBlockZ(), C.RED);
						r.tellRawTo((Player) p);
					}
					
					p.sendMessage(Info.hr());
				}
				
				else
				{
					p.sendMessage(Info.TAG + Lang.DESCRIPTION_NOPERMISSION);
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
		
		sub.add(new SubCommand(Lang.DESCRIPTION_TIMINGS, "timings", "t", "perf")
		{
			private void list(CommandSender p)
			{
				if(new Permissable(p).canList())
				{
					p.sendMessage(Info.hrn(Lang.WORD_WORKER + " " + Lang.WORD_THREADS));
					
					p.sendMessage(C.GOLD + Lang.WORD_THREADS + ": " + C.WHITE + WAPI.getWorkerPool().getThreadCount());
					p.sendMessage(C.GOLD + Lang.WORD_UTILIZATION + ": " + C.WHITE + F.pc(WAPI.getWorkerPoolInfo().getUtilization(), 0));
					p.sendMessage(C.GOLD + Lang.WORD_EFFECTIVETPS + ": " + C.WHITE + F.f(WAPI.getWorkerPoolInfo().getTicksPerSecond(), 2));
					
					p.sendMessage(Info.hrn(Lang.WORD_POWER + " " + Lang.WORD_THREADS));
					
					p.sendMessage(C.GOLD + Lang.WORD_THREADS + ": " + C.WHITE + WAPI.getPowerPool().getThreadCount());
					p.sendMessage(C.GOLD + Lang.WORD_UTILIZATION + ": " + C.WHITE + F.pc(WAPI.getPowerPoolInfo().getUtilization(), 0));
					p.sendMessage(C.GOLD + Lang.WORD_EFFECTIVETPS + ": " + C.WHITE + F.f(WAPI.getPowerPoolInfo().getTicksPerSecond(), 2));
					
					p.sendMessage(Info.hrn(Lang.WORD_SYNC));
					for(String i : TimingsService.root.toLines(0, 2))
					{
						p.sendMessage(i);
					}
					p.sendMessage(Info.hrn(Lang.WORD_ASYNC));
					
					for(String i : TimingsService.asyn.toLines(0, 2))
					{
						p.sendMessage(i);
					}
					
					p.sendMessage(Info.HR);
				}
				
				else
				{
					p.sendMessage(Info.TAG + Lang.DESCRIPTION_NOPERMISSION);
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
		
		sub.add(new SubCommand(Lang.DESCRIPTION_VERSION, "version", "v", "ver")
		{
			private void v(CommandSender p)
			{
				if(new Permissable(p).canList())
				{
					p.sendMessage(C.GOLD + Lang.WORD_RUNNING + " " + C.WHITE + Wormholes.this.getDescription().getVersion());
				}
				
				else
				{
					p.sendMessage(Info.TAG + Lang.DESCRIPTION_NOPERMISSION);
				}
			}
			
			@Override
			public void cs(CommandSender p, String[] args)
			{
				v(p);
			}
			
			@Override
			public void cp(Player p, String[] args)
			{
				v(p);
			}
		});
		
		sub.add(new SubCommand(Lang.DESCRIPTION_WAND, "wand", "w", "wan")
		{
			@Override
			public void cs(CommandSender p, String[] args)
			{
				p.sendMessage(C.RED + Lang.DESCRIPTION_INGAMEONLY);
			}
			
			@Override
			public void cp(Player p, String[] args)
			{
				if(new Permissable(p).canBuild())
				{
					provider.getBuilder().giveWand(p, 3);
				}
				
				else
				{
					p.sendMessage(Info.TAG + Lang.DESCRIPTION_NOPERMISSION);
				}
			}
		});
		
		sub.add(new SubCommand(Lang.DESCRIPTION_SYSTEM, "platform", "plat", "pl")
		{
			private void v(CommandSender p)
			{
				if(new Permissable(p).canReload())
				{
					p.sendMessage(Lang.STATUS_MCV + ": " + Bukkit.getVersion());
					p.sendMessage(Lang.STATUS_APV + ": " + Bukkit.getBukkitVersion());
					p.sendMessage(Lang.STATUS_LPV + ": " + instance.getDescription().getVersion());
				}
				
				else
				{
					p.sendMessage(Info.TAG + Lang.DESCRIPTION_NOPERMISSION);
				}
			}
			
			@Override
			public void cs(CommandSender p, String[] args)
			{
				v(p);
			}
			
			@Override
			public void cp(Player p, String[] args)
			{
				v(p);
			}
		});
		
		sub.add(new SubCommand(Lang.DESCRIPTION_DEBUG, "debug", "db", "vb")
		{
			@Override
			public void cs(CommandSender p, String[] args)
			{
				p.sendMessage(Info.TAG + Lang.DESCRIPTION_INGAMEONLY);
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
					p.sendMessage(Info.TAG + Lang.DESCRIPTION_NOPERMISSION);
				}
			}
		});
		
		sub.add(new SubCommand(Lang.DESCRIPTION_RELOAD, "reload", "reset")
		{
			public void go(CommandSender p)
			{
				if(new Permissable(p).canReload())
				{
					doReload();
					p.sendMessage(Info.TAG + Lang.DESCRIPTION_RELOADED);
				}
				
				else
				{
					p.sendMessage(Info.TAG + Lang.DESCRIPTION_NOPERMISSION);
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
	
	public static Wormholes getInstance()
	{
		return instance;
	}
	
	public static VortexBus getBus()
	{
		return bus;
	}
	
	public static MutexService getHost()
	{
		return host;
	}
	
	public static PortalProvider getProvider()
	{
		return provider;
	}
	
	public static PortalRegistry getRegistry()
	{
		return registry;
	}
	
	public static ApertureService getAperture()
	{
		return aperture;
	}
	
	public static ProjectionService getProjector()
	{
		return projector;
	}
	
	public static TimingsService getTimings()
	{
		return timings;
	}
	
	public static EntityService getEntity()
	{
		return entity;
	}
	
	public static SkinService getSkin()
	{
		return skin;
	}
	
	public static IOService getIo()
	{
		return io;
	}
	
	public static EffectService getFx()
	{
		return fx;
	}
	
	public static ParallelPoolManager getPool()
	{
		return pool;
	}
	
	public SubGroup getSub()
	{
		return sub;
	}
	
	public DB getDispatcher()
	{
		return dispatcher;
	}
}
