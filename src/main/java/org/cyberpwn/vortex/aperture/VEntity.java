package org.cyberpwn.vortex.aperture;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.cyberpwn.vortex.wrapper.WrapperPlayServerEntityDestroy;
import org.cyberpwn.vortex.wrapper.WrapperPlayServerEntityLook;
import org.cyberpwn.vortex.wrapper.WrapperPlayServerEntityTeleport;
import org.cyberpwn.vortex.wrapper.WrapperPlayServerRelEntityMove;
import org.cyberpwn.vortex.wrapper.WrapperPlayServerRelEntityMoveLook;
import org.cyberpwn.vortex.wrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import wraith.AbstractPacket;
import wraith.VectorMath;

public class VEntity
{
	private EntityType type;
	private int id;
	private Location location;
	private Location last;
	private Player viewer;
	private UUID uuid;
	
	public VEntity(Player viewer, EntityType type, int id, UUID uuid, Location location)
	{
		this.viewer = viewer;
		this.type = type;
		this.id = id;
		this.location = location;
		last = location.clone();
		this.uuid = uuid;
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
		w.setOnGround(location.clone().getBlock().getType().isSolid());
		w.setPitch(p);
		w.setYaw(y);
		send(w);
	}
	
	public void despawn()
	{
		WrapperPlayServerEntityDestroy w = new WrapperPlayServerEntityDestroy();
		w.setEntityIds(new int[] {id});
		send(w);
	}
	
	public void spawn()
	{
		if(getType().equals(EntityType.PLAYER))
		{
			WrapperPlayServerSpawnEntityLiving w = new WrapperPlayServerSpawnEntityLiving();
			w.setEntityID(id);
			w.setX(location.getX());
			w.setY(location.getY());
			w.setZ(location.getZ());
			w.setYaw(location.getYaw());
			w.setPitch(location.getPitch());
			w.setHeadPitch(location.getPitch());
			w.setType(EntityType.VILLAGER);
			w.setUniqueId(uuid);
			w.setMetadata(new WrappedDataWatcher());
			send(w);
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
	
	public void move(double x, double y, double z)
	{
		location = location.clone().add(new Vector(x, y, z));
	}
	
	public void look(float y, float p)
	{
		location.setYaw(y);
		location.setPitch(p);
	}
	
	public void teleport(double x, double y, double z, float ya, float pi)
	{
		location = new Location(location.getWorld(), x, y, z);
		look(ya, pi);
	}
	
	public void flush()
	{
		double distance = last.distance(location);
		
		if(distance > 7)
		{
			pteleport(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		}
		
		else
		{
			Vector dir = VectorMath.directionNoNormal(last, location);
			prelativeMoveLook(dir.getX(), dir.getY(), dir.getZ(), location.getYaw(), location.getPitch());
		}
		
		if(location.getYaw() != last.getYaw() || location.getPitch() != last.getPitch())
		{
			plook(location.getYaw(), location.getPitch());
		}
		
		last = location.clone();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((last == null) ? 0 : last.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		result = prime * result + ((viewer == null) ? 0 : viewer.hashCode());
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
		return true;
	}
}
