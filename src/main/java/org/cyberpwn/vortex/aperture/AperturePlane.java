package org.cyberpwn.vortex.aperture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.cyberpwn.vortex.Settings;
import org.cyberpwn.vortex.portal.LocalPortal;
import wraith.CustomGZIPOutputStream;
import wraith.Direction;
import wraith.GMap;
import wraith.VectorMath;

public class AperturePlane
{
	private GMap<Vector, RemoteInstance> instanceMap;
	private GMap<Vector, Vector> instanceVa;
	
	public AperturePlane()
	{
		instanceMap = new GMap<Vector, RemoteInstance>();
		instanceVa = new GMap<Vector, Vector>();
	}
	
	public void clear()
	{
		instanceMap.clear();
		instanceVa.clear();
	}
	
	public byte[] compress() throws IOException
	{
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		CustomGZIPOutputStream gzo = new CustomGZIPOutputStream(boas);
		DataOutputStream dos = new DataOutputStream(gzo);
		gzo.setLevel(Settings.NETWORK_COMPRESSION_LEVEL);
		dos.writeInt(instanceMap.size());
		
		for(Vector i : instanceMap.k())
		{
			RemoteInstance re = instanceMap.get(i);
			dos.writeDouble(i.getX());
			dos.writeDouble(i.getY());
			dos.writeDouble(i.getZ());
			dos.writeUTF(re.getName());
			dos.writeInt(re.getRemoteId());
			dos.writeInt(re.getActualId());
			
			if(re instanceof RemotePlayer)
			{
				dos.writeUTF(((RemotePlayer) re).getUuid().toString());
			}
			
			else
			{
				dos.writeUTF("?");
			}
			
			dos.writeDouble(instanceVa.get(i).getX());
			dos.writeDouble(instanceVa.get(i).getY());
			dos.writeDouble(instanceVa.get(i).getZ());
		}
		
		dos.close();
		return boas.toByteArray();
	}
	
	public void addCompressed(byte[] data) throws IOException
	{
		ByteArrayInputStream boas = new ByteArrayInputStream(data);
		GZIPInputStream gzi = new GZIPInputStream(boas);
		DataInputStream dis = new DataInputStream(gzi);
		
		int size = dis.readInt();
		
		for(int i = 0; i < size; i++)
		{
			Vector v = new Vector(dis.readDouble(), dis.readDouble(), dis.readDouble());
			String name = dis.readUTF();
			int id = dis.readInt();
			int aid = dis.readInt();
			String uiv = dis.readUTF();
			Vector d = new Vector(dis.readDouble(), dis.readDouble(), dis.readDouble());
			RemoteInstance ri = null;
			
			if(uiv.equals("?"))
			{
				for(EntityType j : EntityType.values())
				{
					if(j.name().equals(name))
					{
						ri = new RemoteEntity(id, j, aid);
						break;
					}
				}
			}
			
			else
			{
				ri = new RemotePlayer(id, name, UUID.fromString(uiv), aid);
			}
			
			if(ri == null)
			{
				continue;
			}
			
			instanceMap.put(v, ri);
			instanceVa.put(v, d);
		}
		
		dis.close();
	}
	
	public GMap<Vector, RemoteInstance> remap(Direction from, Direction to)
	{
		GMap<Vector, RemoteInstance> m = new GMap<Vector, RemoteInstance>();
		
		for(Vector i : instanceMap.k())
		{
			m.put(from.angle(i.clone(), to), instanceMap.get(i));
		}
		
		return m;
	}
	
	public GMap<Vector, Vector> remapLook(Direction from, Direction to)
	{
		GMap<Vector, Vector> m = new GMap<Vector, Vector>();
		
		for(Vector i : instanceMap.k())
		{
			m.put(from.angle(i.clone(), to), from.angle(instanceVa.get(i.clone()).clone(), to));
		}
		
		return m;
	}
	
	public void sample(LocalPortal p)
	{
		try
		{
			if(!Settings.ENABLE_APERTURE)
			{
				return;
			}
			
			instanceMap.clear();
			instanceVa.clear();
			
			for(Entity i : p.getPosition().getBoundingBox().getInside())
			{
				if(i.getType().equals(EntityType.ARMOR_STAND) || !Settings.APERTURE_ENTITIY_TYPES.contains(i.getType().toString()))
				{
					continue;
				}
				
				if(i.getLocation().getWorld().equals(p.getPosition().getCenter().getWorld()))
				{
					Vector f = VectorMath.directionNoNormal(p.getPosition().getCenter(), i.getLocation());
					RemoteInstance r = RemoteInstance.create(i);
					instanceMap.put(f, r);
					instanceVa.put(f, i.getLocation().getDirection());
				}
			}
		}
		
		catch(Exception e)
		{
			
		}
	}
	
	public int size()
	{
		return instanceMap.size();
	}
}
