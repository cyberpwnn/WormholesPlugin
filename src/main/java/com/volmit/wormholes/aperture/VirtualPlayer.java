package com.volmit.wormholes.aperture;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.VectorMath;
import com.volmit.wormholes.util.WrapperPlayServerEntityDestroy;
import com.volmit.wormholes.util.WrapperPlayServerEntityHeadRotation;
import com.volmit.wormholes.util.WrapperPlayServerEntityMetadata;
import com.volmit.wormholes.util.WrapperPlayServerNamedEntitySpawn;
import com.volmit.wormholes.util.WrapperPlayServerPlayerInfo;
import com.volmit.wormholes.util.WrapperPlayServerRelEntityMove;
import com.volmit.wormholes.util.WrapperPlayServerRelEntityMoveLook;

public class VirtualPlayer
{
	private Player viewer;
	private UUID uuid;
	private Integer id;
	private String name;
	private String displayName;
	private Location location;
	private Location nextLocation;
	private Boolean onGround;
	
	public VirtualPlayer(Player viewer, UUID uuid, Integer id, String name, String displayName)
	{
		this.viewer = viewer;
		this.uuid = uuid;
		this.id = id;
		this.name = name;
		this.displayName = displayName;
		location = null;
		nextLocation = null;
		onGround = false;
	}
	
	public void spawn(Location location)
	{
		this.location = location;
		nextLocation = location;
		sendPlayerInfo();
		sendNamedEntitySpawn();
		sendEntityMetadata();
	}
	
	public void despawn()
	{
		sendPlayerInfoRemove();
		sendEntityDestroy();
	}
	
	public void tick()
	{
		onGround = nextLocation.getY() == nextLocation.getBlock().getLocation().add(0.5, 1, 0.5).getY();
		sendEntityMove();
		location = nextLocation;
	}
	
	public void move(Location location)
	{
		nextLocation = location.clone();
	}
	
	public void teleport(Location location)
	{
		despawn();
		spawn(location);
	}
	
	public Player getViewer()
	{
		return viewer;
	}
	
	public UUID getUuid()
	{
		return uuid;
	}
	
	public Integer getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public Location getNextLocation()
	{
		return nextLocation;
	}
	
	public Boolean getOnGround()
	{
		return onGround;
	}
	
	private void sendPlayerInfo()
	{
		System.out.println("Virtual: " + getName() + " >> " + getUuid().toString());
		WrapperPlayServerPlayerInfo w = new WrapperPlayServerPlayerInfo();
		w.setAction(PlayerInfoAction.ADD_PLAYER);
		GList<PlayerInfoData> l = new GList<PlayerInfoData>();
		WrappedGameProfile profile = new WrappedGameProfile(UUID.nameUUIDFromBytes(uuid.toString().getBytes()), name + " *");
		
		if(Wormholes.skin.hasProperties(uuid))
		{
			profile.getProperties().put("textures", Wormholes.skin.getProperty(uuid).sign());
		}
		
		else
		{
			Wormholes.skin.requestProperties(uuid);
		}
		
		PlayerInfoData pid = new PlayerInfoData(profile, 1, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(displayName + " *"));
		l.add(pid);
		w.setData(l);
		w.sendPacket(viewer);
	}
	
	private void sendNamedEntitySpawn()
	{
		WrapperPlayServerNamedEntitySpawn w = new WrapperPlayServerNamedEntitySpawn();
		w.setEntityID(id);
		w.setPlayerUUID(uuid);
		w.setYaw(location.getYaw());
		w.setPitch(location.getPitch());
		w.setX(location.getX());
		w.setY(location.getY());
		w.setZ(location.getZ());
		WrappedDataWatcher wd = new WrappedDataWatcher();
		w.setMetadata(wd);
		w.sendPacket(viewer);
	}
	
	private void sendEntityMetadata()
	{
		WrapperPlayServerEntityMetadata w = new WrapperPlayServerEntityMetadata();
		GList<WrappedWatchableObject> watch = new GList<WrappedWatchableObject>();
		w.setEntityID(id);
		w.setMetadata(watch);
		w.sendPacket(viewer);
	}
	
	private void sendPlayerInfoRemove()
	{
		WrapperPlayServerPlayerInfo w = new WrapperPlayServerPlayerInfo();
		w.setAction(PlayerInfoAction.REMOVE_PLAYER);
		GList<PlayerInfoData> l = new GList<PlayerInfoData>();
		WrappedGameProfile profile = new WrappedGameProfile(UUID.nameUUIDFromBytes(uuid.toString().getBytes()), name + " *");
		PlayerInfoData pid = new PlayerInfoData(profile, 1, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(displayName + " *"));
		l.add(pid);
		w.setData(l);
		w.sendPacket(viewer);
	}
	
	private void sendEntityDestroy()
	{
		WrapperPlayServerEntityDestroy w = new WrapperPlayServerEntityDestroy();
		w.setEntityIds(new int[] {id});
		w.sendPacket(viewer);
	}
	
	private void sendEntityMove()
	{
		boolean pit = false;
		boolean mov = false;
		Vector dir = new Vector(0, 0, 0);
		
		if(location.getYaw() != nextLocation.getYaw() || location.getPitch() != nextLocation.getPitch())
		{
			pit = true;
		}
		
		if(location.getX() != nextLocation.getX() || location.getY() != nextLocation.getY() || location.getZ() != nextLocation.getZ())
		{
			mov = true;
			dir = VectorMath.direction(location, nextLocation).multiply(location.distance(nextLocation));
		}
		
		if(mov && !pit)
		{
			sendEntityRelativeMove(dir);
		}
		
		else if(pit)
		{
			sendEntityRelativeMoveLook(dir);
		}
	}
	
	private void sendEntityRelativeMove(Vector velocity)
	{
		WrapperPlayServerRelEntityMove w = new WrapperPlayServerRelEntityMove();
		w.setEntityID(id);
		w.setDx(getCompressedDiff(location.getX(), location.getX() + velocity.getX()));
		w.setDy(getCompressedDiff(location.getY(), location.getY() + velocity.getY()));
		w.setDz(getCompressedDiff(location.getZ(), location.getZ() + velocity.getZ()));
		w.setOnGround(onGround);
		w.sendPacket(viewer);
	}
	
	private void sendEntityRelativeMoveLook(Vector velocity)
	{
		WrapperPlayServerRelEntityMoveLook wa = new WrapperPlayServerRelEntityMoveLook();
		wa.setEntityID(id);
		wa.setDx(getCompressedDiff(location.getX(), location.getX() + velocity.getX()));
		wa.setDy(getCompressedDiff(location.getY(), location.getY() + velocity.getY()));
		wa.setDz(getCompressedDiff(location.getZ(), location.getZ() + velocity.getZ()));
		wa.setOnGround(onGround);
		wa.setYaw(nextLocation.getYaw());
		wa.setPitch(nextLocation.getPitch());
		wa.sendPacket(viewer);
		sendEntityHeadLook();
	}
	
	private void sendEntityHeadLook()
	{
		WrapperPlayServerEntityHeadRotation w = new WrapperPlayServerEntityHeadRotation();
		w.setEntityID(id);
		w.setHeadYaw((byte) (nextLocation.getYaw() * 256.0F / 360.0F));
		w.sendPacket(viewer);
	}
	
	private int getCompressedDiff(double from, double to)
	{
		return (int) (((to * 32) - (from * 32)) * 128);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nextLocation == null) ? 0 : nextLocation.hashCode());
		result = prime * result + ((onGround == null) ? 0 : onGround.hashCode());
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
		VirtualPlayer other = (VirtualPlayer) obj;
		if(displayName == null)
		{
			if(other.displayName != null)
			{
				return false;
			}
		}
		else if(!displayName.equals(other.displayName))
		{
			return false;
		}
		if(id == null)
		{
			if(other.id != null)
			{
				return false;
			}
		}
		else if(!id.equals(other.id))
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
		if(name == null)
		{
			if(other.name != null)
			{
				return false;
			}
		}
		else if(!name.equals(other.name))
		{
			return false;
		}
		if(nextLocation == null)
		{
			if(other.nextLocation != null)
			{
				return false;
			}
		}
		else if(!nextLocation.equals(other.nextLocation))
		{
			return false;
		}
		if(onGround == null)
		{
			if(other.onGround != null)
			{
				return false;
			}
		}
		else if(!onGround.equals(other.onGround))
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
