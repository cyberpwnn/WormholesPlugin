package com.volmit.wormholes.aperture;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.P;
import com.volmit.wormholes.util.ParticleEffect;
import com.volmit.wormholes.util.VectorMath;
import com.volmit.wormholes.util.VersionBukkit;
import com.volmit.wormholes.wrapper.WrapperPlayServerAnimation;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityDestroy;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityEquipment;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityEquipment18;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityHeadRotation;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityHeadRotation18;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityMetadata;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityMetadata18;
import com.volmit.wormholes.wrapper.WrapperPlayServerEntityMoveLook18;
import com.volmit.wormholes.wrapper.WrapperPlayServerNamedEntitySpawn;
import com.volmit.wormholes.wrapper.WrapperPlayServerNamedEntitySpawn18;
import com.volmit.wormholes.wrapper.WrapperPlayServerPlayerInfo;
import com.volmit.wormholes.wrapper.WrapperPlayServerRelEntityMove;
import com.volmit.wormholes.wrapper.WrapperPlayServerRelEntityMove18;
import com.volmit.wormholes.wrapper.WrapperPlayServerRelEntityMoveLook;

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
	private Boolean missingSkin;
	private MaterialBlock a;
	private MaterialBlock h;
	private MaterialBlock c;
	private MaterialBlock l;
	private MaterialBlock b;
	
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
		missingSkin = true;
		a = new MaterialBlock(Material.AIR);
		h = new MaterialBlock(Material.AIR);
		c = new MaterialBlock(Material.AIR);
		l = new MaterialBlock(Material.AIR);
		b = new MaterialBlock(Material.AIR);
	}
	
	public void spawn(Location location)
	{
		this.location = location;
		nextLocation = location;
		sendPlayerInfo();
		sendNamedEntitySpawn();
		sendEntityMetadata();
		sendEntityHeadLook();
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
		
		if(missingSkin && Wormholes.skin.hasProperties(uuid))
		{
			despawn();
			spawn(getLocation());
			
			for(double i = 0.0; i < 1.9; i += 0.15)
			{
				ParticleEffect.SPELL_WITCH.display(0.5f, 12, getLocation().clone().add(0, i, 0), viewer);
			}
		}
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
	
	private String mark()
	{
		return name;
	}
	
	private void sendPlayerInfoRemove()
	{
		for(Player i : P.onlinePlayers())
		{
			if(i.getUniqueId().equals(uuid))
			{
				return;
			}
		}
		
		WrapperPlayServerPlayerInfo w = new WrapperPlayServerPlayerInfo();
		w.setAction(PlayerInfoAction.REMOVE_PLAYER);
		GList<PlayerInfoData> l = new GList<PlayerInfoData>();
		WrappedGameProfile profile = new WrappedGameProfile(uuid, mark());
		PlayerInfoData pid = new PlayerInfoData(profile, 1, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(mark()));
		l.add(pid);
		w.setData(l);
		w.sendPacket(viewer);
	}
	
	private void sendPlayerInfo()
	{
		WrapperPlayServerPlayerInfo w = new WrapperPlayServerPlayerInfo();
		w.setAction(PlayerInfoAction.ADD_PLAYER);
		GList<PlayerInfoData> l = new GList<PlayerInfoData>();
		WrappedGameProfile profile = new WrappedGameProfile(uuid, mark());
		
		if(Wormholes.skin.hasProperties(uuid))
		{
			missingSkin = false;
			profile.getProperties().put("textures", Wormholes.skin.getProperty(uuid).sign());
		}
		
		else
		{
			Wormholes.skin.requestProperties(uuid);
		}
		
		PlayerInfoData pid = new PlayerInfoData(profile, 1, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(mark()));
		l.add(pid);
		w.setData(l);
		w.sendPacket(viewer);
	}
	
	private void sendNamedEntitySpawn18()
	{
		WrapperPlayServerNamedEntitySpawn18 w = new WrapperPlayServerNamedEntitySpawn18();
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
	
	private void sendNamedEntitySpawn()
	{
		if(VersionBukkit.get().equals(VersionBukkit.V8))
		{
			sendNamedEntitySpawn18();
			return;
		}
		
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
	
	public void animationSwingMainArm()
	{
		sendAnimation(0);
	}
	
	public void animationTakeDamage()
	{
		sendAnimation(1);
	}
	
	private void sendAnimation(int animation)
	{
		WrapperPlayServerAnimation w = new WrapperPlayServerAnimation();
		w.setAnimation(animation);
		w.setEntityID(id);
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
	
	@SuppressWarnings("deprecation")
	public boolean setMainHand(ItemStack is)
	{
		if(new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData()).equals(a))
		{
			return false;
		}
		
		a = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
		sendEntityEquipment(is, ItemSlot.MAINHAND);
		
		return true;
	}
	
	public void setOffHand(ItemStack is)
	{
		sendEntityEquipment(is, ItemSlot.OFFHAND);
	}
	
	@SuppressWarnings("deprecation")
	public boolean setHelmet(ItemStack is)
	{
		if(new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData()).equals(h))
		{
			return false;
		}
		
		h = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
		sendEntityEquipment(is, ItemSlot.HEAD);
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public boolean setChestplate(ItemStack is)
	{
		if(new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData()).equals(c))
		{
			return false;
		}
		
		c = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
		sendEntityEquipment(is, ItemSlot.CHEST);
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public boolean setLeggings(ItemStack is)
	{
		if(new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData()).equals(l))
		{
			return false;
		}
		
		l = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
		sendEntityEquipment(is, ItemSlot.LEGS);
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public boolean setBoots(ItemStack is)
	{
		if(new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData()).equals(b))
		{
			return false;
		}
		
		b = new MaterialBlock(is.getType(), is.getData().getData() < 0 ? 0 : is.getData().getData());
		
		sendEntityEquipment(is, ItemSlot.FEET);
		
		return true;
	}
	
	public void animationSneaking(boolean sneaking)
	{
		sendEntityMetadataSneaking(sneaking);
	}
	
	private void sendEntityMetadataSneaking18(boolean sneaking)
	{
		WrapperPlayServerEntityMetadata18 w = new WrapperPlayServerEntityMetadata18();
		GList<WrappedWatchableObject> watch = new GList<WrappedWatchableObject>();
		watch.add(new WrappedWatchableObject(0, (byte) (sneaking ? 2 : 0)));
		w.setEntityID(id);
		w.setMetadata(watch);
		w.sendPacket(viewer);
	}
	
	private void sendEntityEquipment18(ItemStack item, ItemSlot slot)
	{
		if(slot.equals(ItemSlot.OFFHAND))
		{
			return;
		}
		
		WrapperPlayServerEntityEquipment18 w = new WrapperPlayServerEntityEquipment18();
		w.setEntityID(id);
		
		if(!(item == null || item.getType().equals(Material.AIR)))
		{
			w.setItem(item);
		}
		
		w.setSlot(slot.ordinal() != 0 ? slot.ordinal() - 1 : 0);
		w.sendPacket(viewer);
	}
	
	private void sendEntityEquipment(ItemStack item, ItemSlot slot)
	{
		if(VersionBukkit.get().equals(VersionBukkit.V8))
		{
			sendEntityEquipment18(item, slot);
			return;
		}
		
		WrapperPlayServerEntityEquipment w = new WrapperPlayServerEntityEquipment();
		w.setEntityID(id);
		
		if(!(item == null || item.getType().equals(Material.AIR)))
		{
			w.setItem(item);
		}
		
		w.setSlot(slot);
		w.sendPacket(viewer);
	}
	
	private void sendEntityMetadataSneaking(boolean sneaking)
	{
		if(VersionBukkit.get().equals(VersionBukkit.V8))
		{
			sendEntityMetadataSneaking18(sneaking);
			return;
		}
		
		WrapperPlayServerEntityMetadata w = new WrapperPlayServerEntityMetadata();
		GList<WrappedWatchableObject> watch = new GList<WrappedWatchableObject>();
		watch.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), sneaking ? (byte) 2 : (byte) 0));
		w.setEntityID(id);
		w.setMetadata(watch);
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
			sendEntityHeadLook();
		}
	}
	
	private void sendEntityRelativeMove18(Vector velocity)
	{
		WrapperPlayServerRelEntityMove18 w = new WrapperPlayServerRelEntityMove18();
		w.setEntityID(id);
		w.setDx(getCompressedDiff18(location.getX(), location.getX() + velocity.getX()));
		w.setDy(getCompressedDiff18(location.getY(), location.getY() + velocity.getY()));
		w.setDz(getCompressedDiff18(location.getZ(), location.getZ() + velocity.getZ()));
		w.setOnGround(onGround);
		w.sendPacket(viewer);
	}
	
	private void sendEntityRelativeMove(Vector velocity)
	{
		if(VersionBukkit.get().equals(VersionBukkit.V8))
		{
			sendEntityRelativeMove18(velocity);
			return;
		}
		
		WrapperPlayServerRelEntityMove w = new WrapperPlayServerRelEntityMove();
		w.setEntityID(id);
		w.setDx(getCompressedDiff(location.getX(), location.getX() + velocity.getX()));
		w.setDy(getCompressedDiff(location.getY(), location.getY() + velocity.getY()));
		w.setDz(getCompressedDiff(location.getZ(), location.getZ() + velocity.getZ()));
		w.setOnGround(onGround);
		w.sendPacket(viewer);
	}
	
	private void sendEntityRelativeMoveLook18(Vector velocity)
	{
		WrapperPlayServerEntityMoveLook18 w = new WrapperPlayServerEntityMoveLook18();
		w.setEntityID(id);
		w.setDx(getCompressedDiff18(location.getX(), location.getX() + velocity.getX()));
		w.setDy(getCompressedDiff18(location.getY(), location.getY() + velocity.getY()));
		w.setDz(getCompressedDiff18(location.getZ(), location.getZ() + velocity.getZ()));
		w.setOnGround(onGround);
		w.setYaw(nextLocation.getYaw());
		w.setPitch(nextLocation.getPitch());
		w.sendPacket(viewer);
	}
	
	private void sendEntityRelativeMoveLook(Vector velocity)
	{
		if(VersionBukkit.get().equals(VersionBukkit.V8))
		{
			sendEntityRelativeMoveLook18(velocity);
			return;
		}
		
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
	
	private void sendEntityHeadLook18()
	{
		WrapperPlayServerEntityHeadRotation18 w = new WrapperPlayServerEntityHeadRotation18();
		w.setEntityID(id);
		w.setHeadYaw((byte) (nextLocation.getYaw() * 256.0F / 360.0F));
		w.sendPacket(viewer);
	}
	
	private void sendEntityHeadLook()
	{
		if(VersionBukkit.get().equals(VersionBukkit.V8))
		{
			sendEntityHeadLook18();
			return;
		}
		
		WrapperPlayServerEntityHeadRotation w = new WrapperPlayServerEntityHeadRotation();
		w.setEntityID(id);
		w.setHeadYaw((byte) (nextLocation.getYaw() * 256.0F / 360.0F));
		w.sendPacket(viewer);
	}
	
	private int getCompressedDiff(double from, double to)
	{
		return (int) (((to * 32) - (from * 32)) * 128);
	}
	
	private double getCompressedDiff18(double from, double to)
	{
		return to - from;
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
