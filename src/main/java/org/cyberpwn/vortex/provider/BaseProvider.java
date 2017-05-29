package org.cyberpwn.vortex.provider;

import java.io.File;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.cyberpwn.vortex.VP;
import org.cyberpwn.vortex.config.Permissable;
import org.cyberpwn.vortex.event.WormholeCreateEvent;
import org.cyberpwn.vortex.exception.DuplicatePortalKeyException;
import org.cyberpwn.vortex.exception.InvalidPortalKeyException;
import org.cyberpwn.vortex.exception.InvalidPortalPositionException;
import org.cyberpwn.vortex.portal.LocalPortal;
import org.cyberpwn.vortex.portal.Portal;
import org.cyberpwn.vortex.portal.PortalIdentity;
import org.cyberpwn.vortex.portal.PortalKey;
import org.cyberpwn.vortex.portal.PortalPosition;
import org.cyberpwn.vortex.projection.BoundingBox;
import org.cyberpwn.vortex.projection.NulledViewport;
import org.cyberpwn.vortex.projection.RasteredSystem;
import org.cyberpwn.vortex.projection.Viewport;
import wraith.BaseHud;
import wraith.C;
import wraith.Click;
import wraith.Cuboid;
import wraith.DataCluster;
import wraith.Direction;
import wraith.GList;
import wraith.GMap;
import wraith.GSound;
import wraith.Hud;
import wraith.M;
import wraith.MSound;
import wraith.NMSX;
import wraith.PlayerHud;
import wraith.TaskLater;
import wraith.VectorMath;
import wraith.W;
import wraith.Wraith;

public abstract class BaseProvider implements PortalProvider
{
	private RasteredSystem rasterer;
	private GList<Player> moved;
	private GList<Portal> conf;
	private long lastms = M.ms();
	
	public BaseProvider()
	{
		rasterer = new RasteredSystem();
		moved = new GList<Player>();
		conf = new GList<Portal>();
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
		if(VP.host.isKeyValidAlready(p.getKey()))
		{
			File f = new File(VP.instance.getDataFolder(), "data");
			f.mkdirs();
			
			for(File i : f.listFiles())
			{
				if(i.getName().endsWith(".k"))
				{
					try
					{
						DataCluster cc = VP.io.load(i);
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
	
	@Override
	public void save(LocalPortal p)
	{
		if(VP.host.isKeyValidAlready(p.getKey()))
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
			VP.io.save(cc, new File(new File(VP.instance.getDataFolder(), "data"), UUID.randomUUID().toString() + ".k"));
		}
	}
	
	@Override
	public void loadAllPortals()
	{
		File f = new File(VP.instance.getDataFolder(), "data");
		f.mkdirs();
		
		for(File i : f.listFiles())
		{
			if(i.getName().endsWith(".k"))
			{
				try
				{
					DataCluster cc = VP.io.load(i);
					World w = Bukkit.getWorld(cc.getString("g"));
					Location a = new Location(w, cc.getInt("a"), cc.getInt("b"), cc.getInt("c"));
					Location b = new Location(w, cc.getInt("d"), cc.getInt("e"), cc.getInt("f"));
					Direction d = Direction.values()[cc.getInt("h")];
					PortalKey k = PortalKey.fromSName(cc.getString("i"));
					LocalPortal lp = createPortal(d, new Cuboid(a, b));
					lp.getSettings().setAllowEntities(cc.getBoolean("j"));
					lp.getSettings().setAparture(cc.getBoolean("k"));
					lp.getSettings().setProject(cc.getBoolean("l"));
					lp.getSettings().setHasCustomName(cc.getBoolean("m"));
					lp.getSettings().setCustomName(cc.getString("n"));
					System.out.println("Loading Portal: " + k.getSName().toUpperCase());
					i.delete();
				}
				
				catch(Exception e)
				{
					System.out.println("Failed to load data file: " + i.getPath() + ". Deleting");
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
							
							if(s.equalsIgnoreCase("Allow Entities"))
							{
								NMSX.sendActionBar(p, C.YELLOW + "Toggle the permission for entities to use this portal");
							}
							
							else if(s.equalsIgnoreCase("Project Entities"))
							{
								NMSX.sendActionBar(p, C.YELLOW + "Toggle entity projections");
							}
							
							else if(s.equalsIgnoreCase("Project Blocks"))
							{
								NMSX.sendActionBar(p, C.YELLOW + "Toggle block projections");
							}
							
							else if(s.equalsIgnoreCase("Visualize Direction"))
							{
								NMSX.sendActionBar(p, C.YELLOW + "Show the direction players enter and exit.");
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
							if(s.equalsIgnoreCase("Allow Entities"))
							{
								s = l.getSettings().isAllowEntities() ? C.GREEN + s : C.RED + s;
							}
							
							else if(s.equalsIgnoreCase("Project Entities"))
							{
								s = l.getSettings().isAparture() ? C.GREEN + s : C.RED + s;
							}
							
							else if(s.equalsIgnoreCase("Project Blocks"))
							{
								s = l.getSettings().isProject() ? C.GREEN + s : C.RED + s;
							}
							
							else if(s.equalsIgnoreCase("Destroy"))
							{
								s = C.RED + s + " Portal";
							}
							
							return C.LIGHT_PURPLE + "> " + C.GRAY + s + C.LIGHT_PURPLE + " <";
						}
						
						@Override
						public String onDisable(String s)
						{
							if(s.equalsIgnoreCase("Allow Entities"))
							{
								s = l.getSettings().isAllowEntities() ? C.GREEN + s : C.RED + s;
							}
							
							else if(s.equalsIgnoreCase("Project Entities"))
							{
								s = l.getSettings().isAparture() ? C.GREEN + s : C.RED + s;
							}
							
							else if(s.equalsIgnoreCase("Project Blocks"))
							{
								s = l.getSettings().isProject() ? C.GREEN + s : C.RED + s;
							}
							
							else if(s.equalsIgnoreCase("Destroy"))
							{
								s = C.RED + s + " Portal";
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
							
							if(selection.equalsIgnoreCase("Allow Entities"))
							{
								l.getSettings().setAllowEntities(!l.getSettings().isAllowEntities());
								update();
							}
							
							if(selection.equalsIgnoreCase("Project Entities"))
							{
								l.getSettings().setAparture(!l.getSettings().isAparture());
								update();
							}
							
							if(selection.equalsIgnoreCase("Project Blocks"))
							{
								l.getSettings().setProject(!l.getSettings().isProject());
								
								if(!l.getSettings().isProject())
								{
									VP.projector.deproject(l);
								}
								
								update();
							}
							
							if(selection.equalsIgnoreCase("Destroy"))
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
											VP.host.removeLocalPortal(l);
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
					op.add("Allow Entities");
					op.add("Project Entities");
					op.add("Project Blocks");
					op.add("Destroy");
					op.add("Exit");
					hud.setContent(op);
					hud.open();
				}
			};
			
			return true;
		}
		
		return false;
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
		if(VP.host.isKeyValid(identity.getKey()))
		{
			if(VP.host.isPositionValid(position))
			{
				LocalPortal p = new LocalPortal(identity, position);
				VP.host.addLocalPortal(p);
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
		VP.host.removeLocalPortal(portal);
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
