package com.volmit.wormholes.aperture;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.volmit.wormholes.util.VectorMath;
import com.volmit.wormholes.util.VersionBukkit;
import com.volmit.wormholes.wrapper.AbstractPacket;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityDestroy;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityHeadRotation;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityLook;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityTeleport;
import com.volmit.wormholes.wrapper.WrapperPlayServerRelEntityMove;
import com.volmit.wormholes.wrapper.WrapperPlayServerRelEntityMoveLook;
import com.volmit.wormholes.wrapper.WrapperPlayServerSpawnEntityLiving;

public class VEntity
{
	private EntityType type;
	private int id;
	private Location location;
	private Location last;
	private Player viewer;
	private UUID uuid;
	private float yaw;
	private float pit;
	private float lya;
	private float lpi;
	private VirtualPlayer vp;
	
	public VEntity(Player viewer, EntityType type, int id, UUID uuid, Location location, String name)
	{
		this.viewer = viewer;
		this.type = type;
		this.id = id;
		this.location = location;
		last = location.clone();
		this.uuid = uuid;
		yaw = location.getYaw();
		pit = location.getPitch();
		lya = yaw;
		lpi = pit;
		vp = type.equals(EntityType.PLAYER) ? new VirtualPlayer(viewer, uuid, id, name, name) : null;
	}
	
	public void prelativeMove(double x, double y, double z)
	{
		WrapperPlayServerRelEntityMove w = new WrapperPlayServerRelEntityMove();
		w.setDx((int) ((((location.getBlockX() + x) * 32) - (location.getBlockX() * 32)) * 128));
		w.setDy((int) ((((location.getBlockY() + y) * 32) - (location.getBlockY() * 32)) * 128));
		w.setDz((int) ((((location.getBlockZ() + z) * 32) - (location.getBlockZ() * 32)) * 128));
		w.setEntityID(id);
		w.setOnGround(location.clone().add(new Vector(x, y, z)).getBlock().getType().isSolid());
		send(w);
	}
	
	public void prelativeMoveLook(double x, double y, double z, float yaw, float pitch)
	{
		if(VersionBukkit.get().equals(VersionBukkit.V8))
		{
			//			WrapperPlayServerRelEntityMoveLook w = new WrapperPlayServerRelEntityMoveLook();
			//			w.setDx((int) ((((location.getBlockX() + x)) - (location.getBlockX()))));
			//			w.setDy((int) ((((location.getBlockY() + y)) - (location.getBlockY()))));
			//			w.setDz((int) ((((location.getBlockZ() + z)) - (location.getBlockZ()))));
			//			w.setEntityID(id);
			//			w.setOnGround(location.clone().add(new Vector(x, y, z)).getBlock().getType().isSolid());
			//			w.setPitch(pitch);
			//			w.setYaw(yaw);
			//			send(w);
			//			return;
		}
		
		WrapperPlayServerRelEntityMoveLook w = new WrapperPlayServerRelEntityMoveLook();
		w.setDx((int) ((((location.getBlockX() + x) * 32) - (location.getBlockX() * 32)) * 128));
		w.setDy((int) ((((location.getBlockY() + y) * 32) - (location.getBlockY() * 32)) * 128));
		w.setDz((int) ((((location.getBlockZ() + z) * 32) - (location.getBlockZ() * 32)) * 128));
		w.setEntityID(id);
		w.setOnGround(location.clone().add(new Vector(x, y, z)).getBlock().getType().isSolid());
		w.setPitch(pitch);
		w.setYaw(yaw);
		send(w);
	}
	
	public void pteleport(double x, double y, double z, float ya, float pi)
	{
		WrapperPlayServerEntityTeleport w = new WrapperPlayServerEntityTeleport();
		w.setEntityID(id);
		w.setOnGround(location.clone().getBlock().getType().isSolid());
		w.setX(x);
		w.setY(y);
		w.setZ(z);
		w.setYaw(ya);
		w.setPitch(pi);
		send(w);
	}
	
	public void plook(float y, float p)
	{
		WrapperPlayServerEntityLook w = new WrapperPlayServerEntityLook();
		w.setEntityID(id);
		w.setOnGround(true);
		w.setPitch(p);
		w.setYaw(y);
		send(w);
		
		WrapperPlayServerEntityHeadRotation ww = new WrapperPlayServerEntityHeadRotation();
		ww.setHeadYaw((byte) ((yaw * 256.0F) / 360.0F));
		send(ww);
	}
	
	public void despawn()
	{
		if(vp != null)
		{
			vp.despawn();
		}
		
		else
		{
			WrapperPlayServerEntityDestroy w = new WrapperPlayServerEntityDestroy();
			w.setEntityIds(new int[] {id});
			send(w);
		}
	}
	
	public void spawn()
	{
		if(getType().equals(EntityType.PLAYER))
		{
			viewer.sendMessage("Spawn?");
			
			vp.spawn(location);
		}
		
		else
		{
			WrapperPlayServerSpawnEntityLiving w = new WrapperPlayServerSpawnEntityLiving();
			w.setEntityID(id);
			w.setX(location.getX());
			w.setY(location.getY());
			w.setZ(location.getZ());
			w.setYaw(location.getYaw());
			w.setPitch(location.getPitch());
			w.setHeadPitch(location.getPitch());
			w.setType(type);
			w.setUniqueId(uuid);
			w.setMetadata(new WrappedDataWatcher());
			send(w);
		}
	}
	
	public void send(AbstractPacket w)
	{
		try
		{
			ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, w.getHandle());
		}
		
		catch(InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}
	
	public EntityType getType()
	{
		return type;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void move(double x, double y, double z, float ya, float pi)
	{
		location = location.clone().add(new Vector(x, y, z));
		look(ya, pi);
	}
	
	public void look(float y, float p)
	{
		location.setYaw(y);
		location.setPitch(p);
		yaw = location.getYaw();
		pit = location.getPitch();
	}
	
	public void teleport(double x, double y, double z, float ya, float pi)
	{
		location = new Location(location.getWorld(), x, y, z);
		look(ya, pi);
	}
	
	public void setSneaking(boolean s)
	{
		if(vp != null)
		{
			vp.animationSneaking(s);
		}
	}
	
	public void swingArm()
	{
		if(vp != null)
		{
			vp.animationSwingMainArm();
		}
	}
	
	public void takeDamage()
	{
		if(vp != null)
		{
			vp.animationTakeDamage();
		}
	}
	
	public void flush()
	{
		double distance = last.distance(location);
		
		if(distance > 7)
		{
			if(vp != null)
			{
				vp.teleport(location);
			}
			
			else
			{
				pteleport(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			}
		}
		
		else
		{
			Vector dir = VectorMath.directionNoNormal(last, location);
			
			if(vp != null)
			{
				vp.move(vp.getLocation().clone().add(dir));
			}
			
			else
			{
				prelativeMove(dir.getX(), dir.getY(), dir.getZ());
			}
		}
		
		if(yaw != lya || pit != lpi)
		{
			if(vp != null)
			{
				vp.getNextLocation().setYaw(yaw);
				vp.getNextLocation().setPitch(pit);
			}
			
			else
			{
				plook(yaw, pit);
			}
		}
		
		last = location.clone();
		
		if(vp != null)
		{
			vp.tick();
		}
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((last == null) ? 0 : last.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + Float.floatToIntBits(lpi);
		result = prime * result + Float.floatToIntBits(lya);
		result = prime * result + Float.floatToIntBits(pit);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		result = prime * result + ((viewer == null) ? 0 : viewer.hashCode());
		result = prime * result + ((vp == null) ? 0 : vp.hashCode());
		result = prime * result + Float.floatToIntBits(yaw);
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
		VEntity other = (VEntity) obj;
		if(id != other.id)
		{
			return false;
		}
		if(last == null)
		{
			if(other.last != null)
			{
				return false;
			}
		}
		else if(!last.equals(other.last))
		{
			return false;
		}
		if(location == null)
		{
			if(other.location != null)
			{
				return false;
			}
		}
		else if(!location.equals(other.location))
		{
			return false;
		}
		if(Float.floatToIntBits(lpi) != Float.floatToIntBits(other.lpi))
		{
			return false;
		}
		if(Float.floatToIntBits(lya) != Float.floatToIntBits(other.lya))
		{
			return false;
		}
		if(Float.floatToIntBits(pit) != Float.floatToIntBits(other.pit))
		{
			return false;
		}
		if(type != other.type)
		{
			return false;
		}
		if(uuid == null)
		{
			if(other.uuid != null)
			{
				return false;
			}
		}
		else if(!uuid.equals(other.uuid))
		{
			return false;
		}
		if(viewer == null)
		{
			if(other.viewer != null)
			{
				return false;
			}
		}
		else if(!viewer.equals(other.viewer))
		{
			return false;
		}
		if(vp == null)
		{
			if(other.vp != null)
			{
				return false;
			}
		}
		else if(!vp.equals(other.vp))
		{
			return false;
		}
		if(Float.floatToIntBits(yaw) != Float.floatToIntBits(other.yaw))
		{
			return false;
		}
		return true;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public Location getLast()
	{
		return last;
	}
	
	public Player getViewer()
	{
		return viewer;
	}
	
	public UUID getUuid()
	{
		return uuid;
	}
}
