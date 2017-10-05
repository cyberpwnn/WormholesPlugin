package com.volmit.wormholes.portal;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.wormholes.Lang;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.aperture.AperturePlane;
import com.volmit.wormholes.config.Permissable;
import com.volmit.wormholes.event.PortalActivatePlayerEvent;
import com.volmit.wormholes.event.PortalDeactivatePlayerEvent;
import com.volmit.wormholes.event.WormholeLinkEvent;
import com.volmit.wormholes.event.WormholeUnlinkEvent;
import com.volmit.wormholes.exception.DuplicatePortalKeyException;
import com.volmit.wormholes.exception.InvalidPortalKeyException;
import com.volmit.wormholes.exception.InvalidPortalPositionException;
import com.volmit.wormholes.projection.ProjectionMask;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.provider.PortalProvider;
import com.volmit.wormholes.service.MutexService;
import com.volmit.wormholes.util.A;
import com.volmit.wormholes.util.Axis;
import com.volmit.wormholes.util.C;
import com.volmit.wormholes.util.CommandScript;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.DB;
import com.volmit.wormholes.util.DataCluster;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.Hologram;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.RayTrace;
import com.volmit.wormholes.util.TICK;
import com.volmit.wormholes.util.TaskLater;
import com.volmit.wormholes.util.VectorMath;
import com.volmit.wormholes.util.W;
import com.volmit.wormholes.util.Wraith;

public class LocalPortal implements Portal
{
	protected Cuboid ip;
	protected PortalIdentity identity;
	protected PortalPosition position;
	protected ProjectionPlane plane;
	protected String server;
	protected Boolean hasBeenValid;
	protected Boolean hasHadWormhole;
	protected AperturePlane apature;
	protected Boolean saved;
	protected PortalSettings settings;
	protected ProjectionMask mask;
	protected Boolean sided;
	protected String displayName;
	protected GMap<Player, Hologram> holograms;
	protected GList<Player> activatedEntities;
	protected GList<Location> rtpQueue;
	protected long age;

	public LocalPortal(PortalIdentity identity, PortalPosition position) throws InvalidPortalKeyException
	{
		saved = false;
		hasBeenValid = true;
		hasHadWormhole = false;
		this.identity = identity;
		this.position = position;
		plane = new ProjectionPlane();
		server = "";
		apature = new AperturePlane();
		settings = new PortalSettings();
		mask = new ProjectionMask();
		sided = false;
		displayName = "Wormhole";
		holograms = new GMap<Player, Hologram>();
		activatedEntities = new GList<Player>();
		ip = getPosition().getIPane();
		age = 0;
		rtpQueue = new GList<Location>();
	}

	public GList<Player> getPlayers()
	{
		return getPosition().getArea().getPlayers();
	}

	public int getPlayerCount()
	{
		return getPlayers().size();
	}

	public void checkKey()
	{
		if(sided)
		{
			return;
		}

		if(!hasValidIshKey())
		{
			Wormholes.host.removeLocalPortal(this);
			return;
		}
	}

	public List<Chunk> getChunks()
	{
		return getPosition().getArea().getChunks();
	}

	private boolean validateChunks()
	{
		for(Chunk i : getChunks())
		{
			if(!i.isLoaded())
			{
				return false;
			}
		}

		return true;
	}

	private void doSave()
	{
		if(!saved)
		{
			if(hasValidKey())
			{
				Wormholes.provider.save(this);
				saved = true;
			}
		}
	}

	private void doLink()
	{
		if(!hasHadWormhole)
		{
			hasHadWormhole = true;
			Wraith.callEvent(new WormholeLinkEvent(this, getWormhole().getDestination()));
		}
	}

	private void doPortalTick()
	{
		if(!sided)
		{
			if(M.r(0.9))
			{
				Wormholes.fx.rise(this);
			}

			if(M.r(0.07))
			{
				Wormholes.fx.ambient(this);
			}

			checkFrame();
			GList<Player> ac = getPosition().getActivation().getPlayers();

			for(Player i : ac)
			{
				if(!activatedEntities.contains(i) && isPlayerLookingAt(i))
				{
					activatedEntities.add(i);
					Wraith.callEvent(new PortalActivatePlayerEvent(this, i));
				}
			}

			for(Player i : activatedEntities.copy())
			{
				if(!ac.contains(i) && activatedEntities.contains(i))
				{
					activatedEntities.remove(i);
					Wraith.callEvent(new PortalDeactivatePlayerEvent(this, i));
				}
			}
		}
	}

	private void samplePlane()
	{
		if(!plane.hasContent())
		{
			plane.sample(getPosition().getCenter().clone(), Settings.PROJECTION_SAMPLE_RADIUS, getIdentity().getFront().isVertical());

			if(!sided)
			{
				specialUpdate();
			}
		}
	}

	private void doUnlink()
	{
		hasHadWormhole = false;
		Wormholes.projector.deproject(this);
		Wraith.callEvent(new WormholeUnlinkEvent(this));
	}

	@Override
	public void update()
	{
		if(!validateChunks())
		{
			return;
		}

		age++;
		checkKey();
		doSave();
		handleRtp();

		if(hasWormhole())
		{
			doLink();
			doPortalTick();
		}

		else if(hasHadWormhole)
		{
			doUnlink();
		}

		samplePlane();
	}

	private void wipeRtp()
	{
		if(getSettings().isRandomTp() && hasWormhole() && getWormhole().getDestination().getSided())
		{
			Wormholes.provider.destroyPortal((LocalPortal) getWormhole().getDestination());

		}
	}

	private void handleRtp()
	{
		if(getSettings().isRandomTp())
		{
			if(!hasWormhole())
			{
				if(!rtpQueue.isEmpty())
				{
					Location l = rtpQueue.pop();
					Direction d = Direction.N;

					if(getIdentity().getAxis().equals(Axis.Y))
					{
						d = getIdentity().getFront();
					}

					else
					{
						d = Direction.news().pickRandom();
					}

					int osiz = getPosition().getPane().getSizeY();
					int size = (osiz - 1) / 2;

					Location base = l.clone().add(new Vector(0, size, 0));
					Cuboid c = new Cuboid(base);

					for(Axis i : Axis.values())
					{
						if(!i.equals(d.getAxis()))
						{
							c = c.e(i, size);
						}
					}

					PortalIdentity pi = new PortalIdentity(d, getKey());
					PortalPosition pp = new PortalPosition(pi, c);

					try
					{
						LocalPortal lx = Wormholes.provider.createPortal(pi, pp);
						lx.setSided(true);
						getProjectionPlane().wipe();
						lx.getProjectionPlane().wipe();
					}

					catch(InvalidPortalKeyException e)
					{

					}

					catch(InvalidPortalPositionException e)
					{

					}

					catch(DuplicatePortalKeyException e)
					{

					}
				}
			}

			else if(getWormhole().getDestination().getSided())
			{
				if(!rtpQueue.isEmpty() && getSettings().isRtpRefresh() && TICK.tick % Settings.RTP_AUTO_REFRESH_INTERVAL == 0)
				{
					wipeRtp();
				}
			}

			if(rtpQueue.size() < Settings.RTP_MAX_PREQUEUE)
			{
				if(TICK.tick % Settings.RTP_SEARCH_INTERVAL == 0)
				{
					findRTPLocation();
				}
			}
		}
	}

	public void clearRTPCache()
	{
		rtpQueue.clear();
		wipeRtp();
	}

	private void findRTPLocation()
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				int min = getSettings().getRtpMinDist();
				int max = getSettings().getRtpDist();
				Vector direction = Vector.getRandom().subtract(Vector.getRandom());
				direction.multiply(min + (double) (Math.random() * (double) (max - min)));
				Location l = getPosition().getCenter().clone().add(direction);

				for(int i = 256; i > 0; i--)
				{
					try
					{
						Location b = l.clone();
						b.setY(i);

						Material m = b.getBlock().getType();
						Material k = b.getBlock().getRelative(BlockFace.UP).getType();
						Material v = b.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType();

						if(m.isSolid() && k.equals(Material.AIR) && v.equals(Material.AIR) && !b.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).isLiquid() && !b.getBlock().getRelative(BlockFace.UP).isLiquid() && !b.getBlock().isLiquid())
						{
							if(getSettings().getRtpBiome().equals("ALL_BIOMES") || b.getBlock().getBiome().toString().equals(getSettings().getRtpBiome()))
							{
								rtpQueue.add(b.clone().add(new Vector(0, 1, 0)));
								break;
							}
						}
					}

					catch(Exception e)
					{
						break;
					}
				}
			}
		};

		if(Settings.RTP_FORCE_ASYNC_SEARCH)
		{
			new A()
			{
				@Override
				public void async()
				{
					r.run();
				}
			};
		}

		else
		{
			r.run();
		}
	}

	public void showHologram(Player p, Hologram h)
	{
		if(hasHologram(p))
		{
			removeHologram(p);
		}

		holograms.put(p, h);
	}

	public void removeHologram(Player p)
	{
		if(!hasHologram(p))
		{
			return;
		}

		getHologram(p).destroy();
		holograms.remove(p);
	}

	public void clearHolograms()
	{
		for(Player i : holograms.k())
		{
			removeHologram(i);
		}
	}

	public Hologram getHologram(Player p)
	{
		if(hasHologram(p))
		{
			return holograms.get(p);
		}

		return null;
	}

	public boolean hasHologram(Player p)
	{
		return holograms.containsKey(p);
	}

	public void checkFrame(Entity i, Location ic)
	{
		Wormhole w = getWormhole();

		if(!getService().isThrottled(i))
		{
			if(i instanceof Player)
			{
				if(new Permissable(((Player) i)).canUse(this))
				{
					CommandScript cs = Wormholes.instance.scripts.get(getSettings().getConfig());

					if(cs != null)
					{
						for(String j : cs.parseFor(((Player) i).getLocation(), ((Player) i), this))
						{
							System.out.println("C: " + j);
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), j);
						}
					}

					send(i, w, ic);
				}

				else
				{
					Wormholes.fx.throwBack(i, Wormholes.fx.throwBackVector(i, this), this);
				}
			}

			else
			{
				checkSend(i, w, ic);
			}
		}
	}

	public void throwBack(Entity i)
	{
		DB.d(this, "throw back " + i.getUniqueId() + " #" + toString());
		Wormholes.fx.throwBack(i, Wormholes.fx.throwBackVector(i, this), this);
	}

	public void checkSend(Entity i, Wormhole w, Location ic)
	{
		if(w == null)
		{
			return;
		}

		if(i.getType().equals(EntityType.ARMOR_STAND))
		{
			return;
		}

		DB.d(this, "CHK Send: " + toString() + " " + i.getType() + " > " + i.getUniqueId() + " @" + ic.toString());

		if(!Settings.ALLOW_ENTITIES)
		{
			throwBack(i);
		}

		else if(!settings.isAllowEntities())
		{
			throwBack(i);
		}

		else if(Settings.ALLOW_ENTITIY_TYPES.contains(i.getType().toString()))
		{
			send(i, w, ic);
		}

		else
		{
			throwBack(i);
		}
	}

	public void send(Entity i, Wormhole w, Location v)
	{
		if(getService().isThrottled(i))
		{
			return;
		}

		if(getService().isILocked(i))
		{
			return;
		}

		getService().ilock(i);

		int k = 1;

		if(!i.getType().equals(EntityType.PLAYER))
		{
			k += 20;
		}

		new TaskLater(k)
		{
			@Override
			public void run()
			{
				getService().unILock(i);
			}
		};

		if(i.getLocation().getBlock().getType().isSolid())
		{
			getService().unILock(i);
			return;
		}

		if(i instanceof LivingEntity)
		{
			LivingEntity e = (LivingEntity) i;

			if(e.getLocation().getBlock().getType().isSolid() || e.getEyeLocation().getBlock().getType().isSolid())
			{
				getService().unILock(i);
				return;
			}

			if(!w.getSource().getIdentity().getFront().isVertical())
			{
				if(!w.getSource().getPosition().getPane().contains(e.getLocation()) || !w.getSource().getPosition().getPane().contains(e.getEyeLocation()))
				{
					getService().unILock(i);
					return;
				}
			}

			else if(!w.getSource().getPosition().getPane().contains(e.getLocation()) && !w.getSource().getPosition().getPane().contains(e.getEyeLocation()))
			{
				getService().unILock(i);
				return;
			}
		}

		if(i instanceof Arrow)
		{
			if(((Arrow) i).isOnGround())
			{
				return;
			}
		}

		if(i instanceof Player)
		{
			PortalProvider p = Wormholes.provider;
			Player a = (Player) i;

			if(!p.canTeleport(a) && !((p.getTicksLeftBeforeTeleport(a) / 20 + "s").toString().equals("0s")))
			{
				throwBack(a);
				p.notifMessage(a, C.GOLD + Lang.DESCRIPTION_COOLDOWNACTIVE + ": " + C.LIGHT_PURPLE + (p.getTicksLeftBeforeTeleport(a) / 20 + "s"), C.GOLD + Lang.DESCRIPTION_WAITFORTELEPORT);
				return;
			}

			p.markLast(a);
		}

		getService().addThrottle(i);
		w.push(i, v);
		wipeRtp();
		DB.d(this, "Send " + i.getUniqueId() + " #" + toString());
	}

	public void checkFrame()
	{
		GList<Entity> entities = getPosition().getOPane().getEntities();

		for(Entity i : entities)
		{
			if(getPosition().getPane().contains(i.getLocation()))
			{
				Wormhole w = getWormhole();

				if(!w.getSource().getIdentity().getFront().isVertical() && w.getDestination().getIdentity().getFront().isVertical() && i instanceof LivingEntity)
				{
					checkFrame(i, ((LivingEntity) i).getEyeLocation().getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
					continue;
				}

				checkFrame(i, i.getLocation().getBlock().getLocation().clone().add(0.5, 0.5, 0.5));
				continue;
			}

			if(getPosition().intersects(i.getLocation(), i.getVelocity()))
			{
				Wormhole w = getWormhole();

				if(!w.getSource().getIdentity().getFront().isVertical() && w.getDestination().getIdentity().getFront().isVertical() && i instanceof LivingEntity)
				{
					checkFrame(i, getPosition().intersectsv(((LivingEntity) i).getEyeLocation(), i.getVelocity()));
				}

				else
				{
					checkFrame(i, getPosition().intersectsv(i.getLocation(), i.getVelocity()));
				}
			}
		}
	}

	public void specialUpdate()
	{
		for(Player i : getPosition().getArea().getPlayers())
		{
			mask.sched(i);

			new TaskLater(20)
			{
				@Override
				public void run()
				{
					mask.sched(i);
				}
			};
		}
	}

	public void reversePolarity()
	{
		try
		{
			DB.d(this, "Reverse Polarity: " + toString());
			PortalPosition p = getPosition();
			PortalPosition n = new PortalPosition(new PortalIdentity(p.getIdentity().getFront(), getKey()), p.getPane());
			PortalKey pk;
			pk = Wormholes.provider.buildKey(n);
			n.getIdentity().setKey(pk);

			new TaskLater()
			{
				@Override
				public void run()
				{
					Wormholes.host.removeLocalPortalReverse(LocalPortal.this);

					new TaskLater()
					{
						@Override
						public void run()
						{
							try
							{
								Wormholes.provider.createPortal(n.getIdentity(), n);

								new TaskLater(2)
								{
									@Override
									public void run()
									{
										specialUpdate();
									}
								};
							}

							catch(InvalidPortalKeyException | InvalidPortalPositionException | DuplicatePortalKeyException e)
							{
								e.printStackTrace();
							}
						}
					};
				}
			};
		}

		catch(InvalidPortalKeyException e)
		{
			e.printStackTrace();
		}
	}

	public Direction getThrowDirection(Location l)
	{
		if(!getIdentity().getFront().isVertical())
		{
			l.setY(getPosition().getCenter().getY());
			Vector v = VectorMath.direction(getPosition().getCenter(), l);
			return Direction.getDirection(v);
		}

		Vector v = VectorMath.direction(getPosition().getCenter(), l);
		return Direction.getDirection(v);
	}

	public boolean isPlayerLookingAt(Player i)
	{
		if(!getPosition().getCenter().getWorld().equals(i.getWorld()))
		{
			return false;
		}

		double dis = i.getLocation().clone().add(0, 1.7, 0).distance(getPosition().getCenter()) + 7;
		Vector dir = i.getLocation().getDirection();

		boolean[] b = {false};

		new RayTrace(i.getLocation().clone().add(0, 1.7, 0), dir, dis, 0.75)
		{
			@Override
			public void onTrace(Location location)
			{
				if(getPosition().getPane().contains(location))
				{
					stop();
					b[0] = true;
				}
			}
		}.trace();

		return b[0];
	}

	public GList<Player> getPlayersLookingAt()
	{
		GList<Player> players = new GList<Player>();

		for(Player i : getPosition().getArea().getPlayers())
		{
			double dis = i.getLocation().clone().add(0, 1.7, 0).distance(getPosition().getCenter()) + 7;
			Vector dir = i.getLocation().getDirection();

			new RayTrace(i.getLocation().clone().add(0, 1.7, 0), dir, dis, 0.75)
			{
				@Override
				public void onTrace(Location location)
				{
					if(getPosition().getPane().contains(location))
					{
						stop();
						players.add(i);
					}
				}
			}.trace();
		}

		return players;
	}

	@Override
	public PortalIdentity getIdentity()
	{
		return identity;
	}

	@Override
	public PortalPosition getPosition()
	{
		return position;
	}

	@Override
	public PortalKey getKey()
	{
		return identity.getKey();
	}

	@Override
	public boolean hasWormhole()
	{
		if(!hasValidKey())
		{
			return false;
		}

		return getService().hasWormhole(this);
	}

	@Override
	public boolean isWormholeMutex()
	{
		if(!hasValidKey())
		{
			return false;
		}

		return getService().isMutexWormhole(this);
	}

	@Override
	public Wormhole getWormhole()
	{
		if(!hasValidKey())
		{
			return null;
		}

		return getService().getWormhole(this);
	}

	@Override
	public MutexService getService()
	{
		return Wormholes.host;
	}

	@Override
	public DataCluster toData()
	{
		DataCluster cc = new DataCluster();

		cc.set("ku", getKey().getU().ordinal());
		cc.set("kd", getKey().getD().ordinal());
		cc.set("kl", getKey().getL().ordinal());
		cc.set("kr", getKey().getR().ordinal());
		cc.set("kx", getKey().getSName() + "vxx");
		cc.set("ks", getSided());
		cc.set("if", getIdentity().getFront().ordinal());

		return cc;
	}

	@Override
	public void fromData(DataCluster cc)
	{

	}

	@Override
	public String getServer()
	{
		if(server.equals("") && Wormholes.bus.isOnline())
		{
			server = Wormholes.bus.getServerName();
		}

		return server;
	}

	@Override
	public ProjectionPlane getProjectionPlane()
	{
		return plane;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identity == null) ? 0 : identity.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		LocalPortal other = (LocalPortal) obj;
		if(identity == null)
		{
			if(other.identity != null)
			{
				return false;
			}
		}
		else if(!identity.equals(other.identity))
		{
			return false;
		}
		if(position == null)
		{
			if(other.position != null)
			{
				return false;
			}
		}
		else if(!position.equals(other.position))
		{
			return false;
		}
		if(server == null)
		{
			if(other.server != null)
			{
				return false;
			}
		}
		else if(!server.equals(other.server))
		{
			return false;
		}
		return true;
	}

	public boolean hasValidIshKey()
	{
		try
		{
			Wormholes.provider.buildKey(getPosition());
		}

		catch(InvalidPortalKeyException e)
		{
			return false;
		}

		return true;
	}

	@Override
	public boolean hasValidKey()
	{
		try
		{
			PortalKey k = Wormholes.provider.buildKey(getPosition());

			if(Wormholes.host.isKeyValidAlready(k))
			{
				identity.setKey(k);
				hasBeenValid = true;
				return true;
			}

			else
			{
				if(hasBeenValid)
				{
					getService().dequeue(this);
					hasBeenValid = false;
				}

				return false;
			}
		}

		catch(InvalidPortalKeyException e)
		{
			if(hasBeenValid)
			{
				getService().dequeue(this);
				hasBeenValid = false;
			}

			return false;
		}
	}

	public ProjectionPlane getPlane()
	{
		return plane;
	}

	public Boolean getHasBeenValid()
	{
		return hasBeenValid;
	}

	@Override
	public AperturePlane getApature()
	{
		return apature;
	}

	public void destroy()
	{
		DB.d(this, "Destroy EFXC " + toString());
		Wormholes.fx.destroyed(this);
		DB.d(this, "Wipe key " + toString());

		if(Settings.WORMHOLES_DROP_KEY_ON_BREAK)
		{
			getPosition().getCenterDown().getBlock().breakNaturally();
			getPosition().getCenterUp().getBlock().breakNaturally();
			getPosition().getCenterLeft().getBlock().breakNaturally();
			getPosition().getCenterRight().getBlock().breakNaturally();
		}

		else
		{
			getPosition().getCenterDown().getBlock().setType(Material.AIR);
			getPosition().getCenterUp().getBlock().setType(Material.AIR);
			getPosition().getCenterLeft().getBlock().setType(Material.AIR);
			getPosition().getCenterRight().getBlock().setType(Material.AIR);
		}
	}

	public Boolean getHasHadWormhole()
	{
		return hasHadWormhole;
	}

	public Boolean getSaved()
	{
		return saved;
	}

	public PortalSettings getSettings()
	{
		return settings;
	}

	public ProjectionMask getMask()
	{
		return mask;
	}

	@Override
	public Boolean getSided()
	{
		return sided;
	}

	private void wipeKey()
	{
		Wormholes.projector.deproject(this);
		DB.d(this, "Wipe Key " + toString());

		getPosition().getCenterDown().getBlock().setType(Material.AIR);
		getPosition().getCenterUp().getBlock().setType(Material.AIR);
		getPosition().getCenterLeft().getBlock().setType(Material.AIR);
		getPosition().getCenterRight().getBlock().setType(Material.AIR);
	}

	private void retouchKey()
	{
		PortalKey k = getKey();

		if(!W.isColorable(getPosition().getCenterDown().getBlock()))
		{
			getPosition().getCenterDown().getBlock().setType(Material.WOOL);
		}

		if(!W.isColorable(getPosition().getCenterUp().getBlock()))
		{
			getPosition().getCenterUp().getBlock().setType(Material.WOOL);
		}

		if(!W.isColorable(getPosition().getCenterLeft().getBlock()))
		{
			getPosition().getCenterLeft().getBlock().setType(Material.WOOL);
		}

		if(!W.isColorable(getPosition().getCenterRight().getBlock()))
		{
			getPosition().getCenterRight().getBlock().setType(Material.WOOL);
		}

		W.setColor(getPosition().getCenterDown().getBlock(), k.getD());
		W.setColor(getPosition().getCenterUp().getBlock(), k.getU());
		W.setColor(getPosition().getCenterLeft().getBlock(), k.getL());
		W.setColor(getPosition().getCenterRight().getBlock(), k.getR());
		DB.d(this, "Recolor " + toString());
	}

	@Override
	public void setSided(Boolean sided)
	{
		this.sided = sided;

		if(sided)
		{
			wipeKey();
		}

		else
		{
			retouchKey();
		}

		save();
	}

	@Override
	public String getDisplayName()
	{
		return displayName;
	}

	@Override
	public void updateDisplayName(String n)
	{
		displayName = n;
		save();
		clearHolograms();
	}

	@Override
	public boolean hasDisplayName()
	{
		return displayName != null && displayName.length() > 0;
	}

	@Override
	public void save()
	{
		Wormholes.provider.wipe(this);
		Wormholes.provider.save(this);
		DB.d(this, "Saved " + toString());
	}

	@Override
	public String toString()
	{
		return "LocalPortal: " + getKey().toString() + "";
	}

	public boolean wasJustCreated()
	{
		return age < 2;
	}

	public Cuboid getIp()
	{
		return ip;
	}

	public GMap<Player, Hologram> getHolograms()
	{
		return holograms;
	}

	public GList<Player> getActivatedEntities()
	{
		return activatedEntities;
	}

	public GList<Location> getRtpQueue()
	{
		return rtpQueue;
	}

	public long getAge()
	{
		return age;
	}
}
