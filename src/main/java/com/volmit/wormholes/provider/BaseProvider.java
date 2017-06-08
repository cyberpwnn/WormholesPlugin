package com.volmit.wormholes.provider;

import java.io.File;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.config.Permissable;
import com.volmit.wormholes.event.WormholeCreateEvent;
import com.volmit.wormholes.exception.DuplicatePortalKeyException;
import com.volmit.wormholes.exception.InvalidPortalKeyException;
import com.volmit.wormholes.exception.InvalidPortalPositionException;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalIdentity;
import com.volmit.wormholes.portal.PortalKey;
import com.volmit.wormholes.portal.PortalPosition;
import com.volmit.wormholes.projection.BoundingBox;
import com.volmit.wormholes.projection.NulledViewport;
import com.volmit.wormholes.projection.RasteredSystem;
import com.volmit.wormholes.projection.Viewport;
import com.volmit.wormholes.util.BaseHud;
import com.volmit.wormholes.util.C;
import com.volmit.wormholes.util.Click;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.DataCluster;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.GSound;
import com.volmit.wormholes.util.Hud;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.MSound;
import com.volmit.wormholes.util.NMSX;
import com.volmit.wormholes.util.PlayerHud;
import com.volmit.wormholes.util.TXT;
import com.volmit.wormholes.util.TaskLater;
import com.volmit.wormholes.util.Title;
import com.volmit.wormholes.util.VectorMath;
import com.volmit.wormholes.util.W;
import com.volmit.wormholes.util.Wraith;

public abstract class BaseProvider implements PortalProvider
{
	private RasteredSystem rasterer;
	private GList<Player> moved;
	private GList<Portal> conf;
	private long lastms = M.ms();
	protected GList<Player> debug;
	
	public BaseProvider()
	{
		rasterer = new RasteredSystem();
		moved = new GList<Player>();
		conf = new GList<Portal>();
		debug = new GList<Player>();
	}
	
	public void dedebug(Player p)
	{
		if(isDebugging(p))
		{
			debug.remove(p);
		}
	}
	
	public void debug(Player p)
	{
		if(!isDebugging(p))
		{
			debug.add(p);
		}
	}
	
	public void errorMessage(Player p, String title, String msg)
	{
		Title t = new Title();
		t.setTitle("    ");
		t.setSubTitle(title);
		t.setAction(msg);
		t.setFadeIn(5);
		t.setFadeOut(50);
		t.setStayTime(1);
		t.send(p);
	}
	
	public boolean isDebugging(Player p)
	{
		return debug.contains(p);
	}
	
	@Override
	public void flush()
	{
		onFlush();
	}
	
	public abstract void onFlush();
	
	public GList<Integer> getBase(int max)
	{
		GList<Integer> base = new GList<Integer>();
		max = max % 2 == 0 ? max + 1 : max;
		
		while(max >= 3)
		{
			base.add(max * max);
			max -= 2;
		}
		
		return base;
	}
	
	public GList<Integer> getBaseSqrt(int max)
	{
		GList<Integer> base = new GList<Integer>();
		max = max % 2 == 0 ? max + 1 : max;
		
		while(max >= 3)
		{
			base.add(max);
			max -= 2;
		}
		
		return base;
	}
	
	@Override
	public PortalKey buildKey(PortalPosition p) throws InvalidPortalKeyException
	{
		DyeColor u = W.getColor(p.getCenterUp());
		DyeColor d = W.getColor(p.getCenterDown());
		DyeColor l = W.getColor(p.getCenterLeft());
		DyeColor r = W.getColor(p.getCenterRight());
		
		if(u != null && d != null && l != null && r != null)
		{
			return new PortalKey(u, d, l, r);
		}
		
		else
		{
			throw new InvalidPortalKeyException("Invalid portal key");
		}
	}
	
	@Override
	public void wipe(LocalPortal p)
	{
		if(Wormholes.host.isKeyValidAlready(p.getKey()))
		{
			File f = new File(Wormholes.instance.getDataFolder(), "data");
			f.mkdirs();
			
			for(File i : f.listFiles())
			{
				if(i.getName().endsWith(".k"))
				{
					try
					{
						DataCluster cc = Wormholes.io.load(i);
						World w = Bukkit.getWorld(cc.getString("g"));
						Location a = new Location(w, cc.getInt("a"), cc.getInt("b"), cc.getInt("c"));
						Location b = new Location(w, cc.getInt("d"), cc.getInt("e"), cc.getInt("f"));
						PortalKey k = PortalKey.fromSName(cc.getString("i"));
						
						if(k.equals(p.getKey()) && new Cuboid(a, b).equals(p.getPosition().getPane()))
						{
							i.delete();
						}
					}
					
					catch(Exception e)
					{
						
					}
				}
			}
		}
	}
	
	public void forceWipe(LocalPortal p)
	{
		File f = new File(Wormholes.instance.getDataFolder(), "data");
		f.mkdirs();
		
		for(File i : f.listFiles())
		{
			if(i.getName().endsWith(".k"))
			{
				try
				{
					DataCluster cc = Wormholes.io.load(i);
					World w = Bukkit.getWorld(cc.getString("g"));
					Location a = new Location(w, cc.getInt("a"), cc.getInt("b"), cc.getInt("c"));
					Location b = new Location(w, cc.getInt("d"), cc.getInt("e"), cc.getInt("f"));
					PortalKey k = PortalKey.fromSName(cc.getString("i"));
					
					if(k.equals(p.getKey()) && new Cuboid(a, b).equals(p.getPosition().getPane()))
					{
						i.delete();
					}
				}
				
				catch(Exception e)
				{
					
				}
			}
		}
	}
	
	@Override
	public void save(LocalPortal p)
	{
		if(Wormholes.host.isKeyValidAlready(p.getKey()))
		{
			DataCluster cc = new DataCluster();
			PortalKey key = p.getKey();
			PortalPosition pos = p.getPosition();
			Cuboid c = pos.getPane();
			Direction d = pos.getIdentity().getBack();
			cc.set("a", c.getLowerX());
			cc.set("b", c.getLowerY());
			cc.set("c", c.getLowerZ());
			cc.set("d", c.getUpperX());
			cc.set("e", c.getUpperY());
			cc.set("f", c.getUpperZ());
			cc.set("g", c.getWorld().getName());
			cc.set("h", d.ordinal());
			cc.set("i", key.getSName());
			cc.set("j", p.getSettings().isAllowEntities());
			cc.set("k", p.getSettings().isAparture());
			cc.set("l", p.getSettings().isProject());
			cc.set("m", p.getSettings().isHasCustomName());
			cc.set("n", p.getSettings().getCustomName());
			Wormholes.io.save(cc, new File(new File(Wormholes.instance.getDataFolder(), "data"), UUID.randomUUID().toString() + ".k"));
		}
	}
	
	@Override
	public void loadAllPortals()
	{
		File f = new File(Wormholes.instance.getDataFolder(), "data");
		f.mkdirs();
		
		for(File i : f.listFiles())
		{
			if(i.getName().endsWith(".k"))
			{
				try
				{
					DataCluster cc = Wormholes.io.load(i);
					World w = Bukkit.getWorld(cc.getString("g"));
					Location a = new Location(w, cc.getInt("a"), cc.getInt("b"), cc.getInt("c"));
					Location b = new Location(w, cc.getInt("d"), cc.getInt("e"), cc.getInt("f"));
					Direction d = Direction.values()[cc.getInt("h")];
					LocalPortal lp = createPortal(d, new Cuboid(a, b));
					lp.getSettings().setAllowEntities(cc.getBoolean("j"));
					lp.getSettings().setAparture(cc.getBoolean("k"));
					lp.getSettings().setProject(cc.getBoolean("l"));
					lp.getSettings().setHasCustomName(cc.getBoolean("m"));
					lp.getSettings().setCustomName(cc.getString("n"));
					i.delete();
				}
				
				catch(Exception e)
				{
					i.delete();
				}
			}
		}
	}
	
	public boolean configure(LocalPortal l, Player p)
	{
		if(new Permissable(p).canConfigure() && !conf.contains(l))
		{
			if(M.ms() - lastms > 50)
			{
				lastms = M.ms();
			}
			
			else
			{
				return false;
			}
			
			new TaskLater(2)
			{
				@Override
				public void run()
				{
					PlayerHud hud = new PlayerHud(p, true)
					{
						@Override
						public void onUpdate()
						{
							
						}
						
						@Override
						public void onSelect(String selection, int slot)
						{
							new GSound(MSound.WOOD_CLICK.bukkitSound(), 0.3f, 1.6f).play(p);
							
							String s = selection;
							
							if(s.startsWith("Entities"))
							{
								NMSX.sendActionBar(p, C.YELLOW + "Toggle the permission for entities to use this portal");
							}
							
							else if(s.startsWith("Project Entities"))
							{
								NMSX.sendActionBar(p, C.YELLOW + "Toggle entity projections");
							}
							
							else if(s.startsWith("Project Blocks"))
							{
								NMSX.sendActionBar(p, C.YELLOW + "Toggle block projections");
							}
							
							else if(s.startsWith("Reverse Polarity"))
							{
								NMSX.sendActionBar(p, C.YELLOW + "Reverse the 'front facing' direction of this portal.");
							}
							
							else if(s.equalsIgnoreCase("Destroy"))
							{
								NMSX.sendActionBar(p, C.YELLOW + "Destroy this portal?");
							}
							
							else if(s.equalsIgnoreCase("Exit"))
							{
								NMSX.sendActionBar(p, C.YELLOW + "Exit this menu (or just walk away from it)");
							}
						}
						
						@Override
						public void onOpen()
						{
							conf.add(l);
							new GSound(MSound.ENDERDRAGON_WINGS.bukkitSound(), 0.3f, 0.9f).play(p);
						}
						
						@Override
						public String onEnable(String s)
						{
							if(s.startsWith("Entities: "))
							{
								s = "Entities: " + (l.getSettings().isAllowEntities() ? C.GREEN + "Allowed" : C.RED + "Denied");
							}
							
							else if(s.startsWith("Project Entities: "))
							{
								s = "Project Entities: " + (l.getSettings().isAparture() ? C.GREEN + "ON" : C.RED + "OFF");
							}
							
							else if(s.startsWith("Project Blocks: "))
							{
								s = "Project Blocks: " + (l.getSettings().isProject() ? C.GREEN + "ON" : C.RED + "OFF");
							}
							
							else if(s.equalsIgnoreCase("Destroy"))
							{
								s = C.RED + s + " Portal";
							}
							
							return C.LIGHT_PURPLE + "> " + C.WHITE + s + C.LIGHT_PURPLE + " <";
						}
						
						@Override
						public String onDisable(String s)
						{
							if(s.startsWith("Entities: "))
							{
								s = "Entities: " + (l.getSettings().isAllowEntities() ? C.GREEN + "Allowed" : C.RED + "Denied");
							}
							
							else if(s.startsWith("Project Entities: "))
							{
								s = "Project Entities: " + (l.getSettings().isAparture() ? C.GREEN + "ON" : C.RED + "OFF");
							}
							
							else if(s.startsWith("Project Blocks: "))
							{
								s = "Project Blocks: " + (l.getSettings().isProject() ? C.GREEN + "ON" : C.RED + "OFF");
							}
							
							else if(s.equalsIgnoreCase("Destroy"))
							{
								s = s + " Portal";
							}
							
							return C.GRAY + s;
						}
						
						@Override
						public void onClose()
						{
							conf.remove(l);
							new GSound(MSound.ENDERDRAGON_WINGS.bukkitSound(), 0.3f, 0.9f).play(p);
						}
						
						@Override
						public void onClick(Click c, Player p, String selection, int slot, Hud h)
						{
							if(M.ms() - lastms > 50)
							{
								lastms = M.ms();
							}
							
							else
							{
								return;
							}
							
							new GSound(MSound.WOOD_CLICK.bukkitSound(), 0.3f, 0.8f).play(p);
							
							if(selection.startsWith("Entities"))
							{
								l.getSettings().setAllowEntities(!l.getSettings().isAllowEntities());
								update();
							}
							
							if(selection.startsWith("Project Entities"))
							{
								l.getSettings().setAparture(!l.getSettings().isAparture());
								update();
							}
							
							if(selection.startsWith("Reverse Polarity"))
							{
								close();
								Wormholes.projector.deproject(l);
								
								new TaskLater(20)
								{
									@Override
									public void run()
									{
										l.reversePolarity();
									}
								};
							}
							
							if(selection.startsWith("Project Blocks"))
							{
								l.getSettings().setProject(!l.getSettings().isProject());
								
								if(!l.getSettings().isProject())
								{
									Wormholes.projector.deproject(l);
								}
								
								update();
							}
							
							if(selection.startsWith("Destroy"))
							{
								close();
								
								Hud confirm = new PlayerHud(p, true)
								{
									@Override
									public void onUpdate()
									{
										
									}
									
									@Override
									public void onSelect(String selection, int slot)
									{
										new GSound(MSound.WOOD_CLICK.bukkitSound(), 0.3f, 1.6f).play(p);
									}
									
									@Override
									public void onOpen()
									{
										new GSound(MSound.ENDERDRAGON_WINGS.bukkitSound(), 0.3f, 0.9f).play(p);
									}
									
									@Override
									public String onEnable(String s)
									{
										return C.LIGHT_PURPLE + "> " + C.RED + s + C.LIGHT_PURPLE + " <";
									}
									
									@Override
									public String onDisable(String s)
									{
										return C.GRAY + s;
									}
									
									@Override
									public void onClose()
									{
										new GSound(MSound.ENDERDRAGON_WINGS.bukkitSound(), 0.3f, 0.9f).play(p);
									}
									
									@Override
									public void onClick(Click c, Player p, String selection, int slot, Hud h)
									{
										new GSound(MSound.WOOD_CLICK.bukkitSound(), 0.3f, 0.8f).play(p);
										close();
										
										if(selection.equalsIgnoreCase("Yes"))
										{
											Wormholes.host.removeLocalPortal(l);
										}
										
										if(selection.equalsIgnoreCase("No"))
										{
											configure(l, p);
										}
									}
								};
								
								GList<String> opv = new GList<String>();
								opv.add("Are You Sure?");
								opv.add("YES");
								opv.add("NO");
								confirm.setContents(opv);
								((BaseHud) confirm).setHasTitle(true);
								confirm.open();
							}
							
							if(selection.equalsIgnoreCase("Exit"))
							{
								close();
							}
						}
					};
					
					GList<String> op = new GList<String>();
					op.add(TXT.line(C.LIGHT_PURPLE, 5) + C.GRAY + " Options " + TXT.line(C.LIGHT_PURPLE, 5));
					op.add("Entities: " + (l.getSettings().isAllowEntities() ? C.GREEN + "Allowed" : C.RED + "Denied"));
					op.add("Project Entities: " + (l.getSettings().isAparture() ? C.GREEN + "ON" : C.RED + "OFF"));
					op.add("Project Blocks: " + (l.getSettings().isProject() ? C.GREEN + "ON" : C.RED + "OFF"));
					op.add(TXT.line(C.LIGHT_PURPLE, 5) + C.GRAY + " Actions " + TXT.line(C.LIGHT_PURPLE, 5));
					op.add("Reverse Polarity");
					op.add("Destroy");
					op.add(TXT.line(C.LIGHT_PURPLE, 5) + C.GRAY + " Status " + TXT.line(C.LIGHT_PURPLE, 5));
					op.add("Facing Direction: " + C.YELLOW + l.getIdentity().getFront());
					op.add("Portal Link State: " + (l.hasWormhole() ? (l.isWormholeMutex() ? C.YELLOW + "Bungeecord" : C.YELLOW + "Local") : C.RED + "Unlinked"));
					op.add("Portal Key: " + l.getKey());
					op.add("Portal Bounds: " + C.YELLOW + l.getIdentity().getFront() + "" + l.getIdentity().getBack() + " <" + l.getIdentity().getUp() + "" + l.getIdentity().getDown() + "> " + l.getIdentity().getLeft() + "" + l.getIdentity().getRight());
					op.add("Exit");
					hud.setContent(op);
					hud.open();
				}
			};
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void dfs()
	{
		for(Portal i : Wormholes.host.getLocalPortals())
		{
			LocalPortal p = (LocalPortal) i;
			forceWipe(p);
		}
	}
	
	@Override
	public void dfd()
	{
		for(Portal i : Wormholes.host.getLocalPortals())
		{
			LocalPortal p = (LocalPortal) i;
			save(p);
		}
	}
	
	public LocalPortal createPortal(Direction d, Cuboid c) throws InvalidPortalKeyException, InvalidPortalPositionException, DuplicatePortalKeyException
	{
		PortalIdentity id = new PortalIdentity(d, null);
		PortalPosition pp = new PortalPosition(id, c);
		id.setKey(buildKey(pp));
		
		return createPortal(id, pp);
	}
	
	@Override
	public LocalPortal createPortal(PortalIdentity identity, PortalPosition position) throws InvalidPortalKeyException, InvalidPortalPositionException, DuplicatePortalKeyException
	{
		if(Wormholes.host.isKeyValid(identity.getKey()))
		{
			if(Wormholes.host.isPositionValid(position))
			{
				LocalPortal p = new LocalPortal(identity, position);
				Wormholes.host.addLocalPortal(p);
				Wraith.callEvent(new WormholeCreateEvent(p));
				return p;
			}
			
			else
			{
				throw new InvalidPortalPositionException("Portal already resides in this area");
			}
		}
		
		else
		{
			throw new DuplicatePortalKeyException("There are already two portal keys of this type that are linked");
		}
	}
	
	@Override
	public void destroyPortal(LocalPortal portal)
	{
		Wormholes.host.removeLocalPortal(portal);
	}
	
	@Override
	public RasteredSystem getRasterer()
	{
		return rasterer;
	}
	
	@Override
	public Viewport getViewport(Player p, Portal portal)
	{
		Viewport v = new Viewport(p, portal);
		v.rebuild();
		return v;
	}
	
	@Override
	public boolean isNear(Player p, Portal portal)
	{
		return portal.getPosition().getBoundingBox().isEnteringOrInside(p);
	}
	
	@Override
	public boolean canSee(Player p, Portal portal)
	{
		return isNear(p, portal) && VectorMath.isLookingNear(p.getLocation(), portal.getPosition().getCenter(), 0.65);
	}
	
	@Override
	public void movePlayer(Player p)
	{
		if(!moved.contains(p))
		{
			moved.add(p);
		}
	}
	
	@Override
	public boolean hasMoved(Player p)
	{
		return moved.contains(p);
	}
	
	@Override
	public GMap<Player, Viewport> getViewport(Portal portal)
	{
		GMap<Player, Viewport> views = new GMap<Player, Viewport>();
		BoundingBox box = portal.getPosition().getBoundingBox();
		box.flush();
		
		for(Entity i : box.getInside())
		{
			if(i instanceof Player)
			{
				if(portal instanceof LocalPortal)
				{
					if(((LocalPortal) portal).getPosition().getPane().contains(i.getLocation()))
					{
						Player p = (Player) i;
						views.put(p, new NulledViewport(p, portal));
					}
					
					else
					{
						Player p = (Player) i;
						views.put(p, getViewport(p, portal));
					}
				}
			}
		}
		
		for(Entity i : box.getExiting())
		{
			if(i instanceof Player)
			{
				Player p = (Player) i;
				views.put(p, new NulledViewport(p, portal));
			}
		}
		
		return views;
	}
}
