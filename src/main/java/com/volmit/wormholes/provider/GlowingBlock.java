package com.volmit.wormholes.provider;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.volmit.volume.bukkit.U;
import com.volmit.volume.bukkit.nms.NMSSVC;
import com.volmit.volume.bukkit.util.text.C;
import com.volmit.volume.bukkit.util.world.MaterialBlock;
import com.volmit.volume.lang.collections.GList;
import com.volmit.volume.math.M;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityMetadata;

public class GlowingBlock
{
	private static int idd = 123456789;
	private int id;
	private UUID uid;
	private Location location;
	private Location current;
	private Player player;
	private double factor;
	private Vector velocity;
	private boolean active;
	private long mv = M.ms();
	private MaterialBlock mb;
	private ChatColor c;

	public GlowingBlock(Player player, Location init, MaterialBlock mb, ChatColor c)
	{
		this.mb = mb;
		this.uid = UUID.randomUUID();
		this.id = idd--;
		location = init;
		current = init.clone();
		this.player = player;
		factor = 1;
		active = false;
		velocity = new Vector();
		this.c = c;
	}

	public UUID getUid()
	{
		return uid;
	}

	public int getId()
	{
		return id;
	}

	public Vector getVelocity()
	{
		return velocity;
	}

	public void setVelocity(Vector velocity)
	{
		this.velocity = velocity;
	}

	private void sendMetadata(boolean glowing)
	{
		WrapperPlayServerEntityMetadata w = new WrapperPlayServerEntityMetadata();
		GList<WrappedWatchableObject> watch = new GList<WrappedWatchableObject>();
		//watch.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (glowing ? 0x40 : 0)));
		//watch.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), (boolean) (true)));

		w.setEntityID(id);
		w.setMetadata(watch);
		w.sendPacket(getPlayer());
	}

	public void sendMetadata(ChatColor c)
	{
		U.getService(NMSSVC.class).sendGlowingColorMetaEntity(getPlayer(), uid, C.values()[c.ordinal()]);
	}

	public void update()
	{
		if(M.ms() - mv < 50)
		{
			return;
		}

		if(location.getX() == current.getX() && location.getY() == current.getY() && location.getZ() == current.getZ())
		{
			return;
		}

		mv = M.ms();

		if(location.distanceSquared(current) > 16)
		{
			sendTeleport(location);
			current = location;
		}

		else
		{
			double dx = location.getX() - current.getX();
			double dy = location.getY() - current.getY();
			double dz = location.getZ() - current.getZ();
			dx += velocity.getX();
			dy += velocity.getY();
			dz += velocity.getZ();
			dx = M.clip(dx, -8, 8);
			dy = M.clip(dy, -8, 8);
			dz = M.clip(dz, -8, 8);
			sendMove(dx / factor, dy / factor, dz / factor);
			current.add(dx / factor, dy / factor, dz / factor);
			current.setX(Math.abs(location.getX() - current.getX()) < 0.00001 ? location.getX() : current.getX());
			current.setY(Math.abs(location.getY() - current.getY()) < 0.00001 ? location.getY() : current.getY());
			current.setZ(Math.abs(location.getZ() - current.getZ()) < 0.00001 ? location.getZ() : current.getZ());

			if(location.getX() == current.getX() && location.getY() == current.getY() && location.getZ() == current.getZ())
			{
				sendTeleport(location);
				current = location;
			}
		}
	}

	public Location getPosition()
	{
		return location.clone();
	}

	public void setPosition(Location l)
	{
		location = l;
	}

	public Player getPlayer()
	{
		return player;
	}

	public void destroy()
	{
		sendDestroy();
	}

	public void create()
	{
		sendSpawn();
	}

	public boolean isActive()
	{
		return active;
	}

	public void setFactor(int i)
	{
		factor = i;
	}

	private void sendTeleport(Location l)
	{
		U.getService(NMSSVC.class).teleportEntity(id, player, l, false);
	}

	private void sendMove(double x, double y, double z)
	{
		U.getService(NMSSVC.class).moveEntityRelative(id, player, x, y, z, false);
	}

	public void sendDestroy()
	{
		active = false;
		U.getService(NMSSVC.class).removeEntity(id, player);
		U.getService(NMSSVC.class).sendRemoveGlowingColorMetaEntity(getPlayer(), uid);
		sendMetadata(false);
	}

	public void sendSpawn()
	{
		U.getService(NMSSVC.class).spawnFallingBlock(id, uid, location, player, mb);
		sendMetadata(c);
		sendMetadata(true);
		active = true;
	}

	public Location getLocation()
	{
		return location;
	}
}
