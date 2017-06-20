package com.volmit.wormholes.portal;

import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.aperture.AperturePlane;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.service.MutexService;
import com.volmit.wormholes.util.DataCluster;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.wormhole.Wormhole;

public class RemotePortal implements Portal
{
	private String server;
	private PortalIdentity identity;
	private Boolean sided;
	private String displayName;
	private Boolean wait;
	
	public RemotePortal(String server, PortalIdentity identity, String displayName)
	{
		this.identity = identity;
		this.server = server;
		sided = false;
		this.displayName = displayName;
		wait = false;
	}
	
	public void setWait()
	{
		wait = true;
	}
	
	@Override
	public void update()
	{
		
	}
	
	@Override
	public PortalIdentity getIdentity()
	{
		return identity;
	}
	
	@Override
	public PortalPosition getPosition()
	{
		return null;
	}
	
	@Override
	public PortalKey getKey()
	{
		return identity.getKey();
	}
	
	@Override
	public boolean hasWormhole()
	{
		return false;
	}
	
	@Override
	public boolean isWormholeMutex()
	{
		return false;
	}
	
	@Override
	public Wormhole getWormhole()
	{
		return null;
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
		PortalKey k = new PortalKey(new byte[] {cc.getInt("ku").byteValue(), cc.getInt("kd").byteValue(), cc.getInt("kl").byteValue(), cc.getInt("kr").byteValue()});
		PortalIdentity i = new PortalIdentity(Direction.values()[cc.getInt("if")], k);
		setSided(cc.getBoolean("ks"));
		identity = i;
	}
	
	@Override
	public String getServer()
	{
		return server;
	}
	
	@Override
	public ProjectionPlane getProjectionPlane()
	{
		if(!Wormholes.projector.getRemotePlanes().containsKey(getKey()))
		{
			Wormholes.projector.getRemotePlanes().put(getKey(), new ProjectionPlane());
		}
		
		return Wormholes.projector.getRemotePlanes().get(getKey());
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identity == null) ? 0 : identity.hashCode());
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
		RemotePortal other = (RemotePortal) obj;
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
	
	@Override
	public boolean hasValidKey()
	{
		return true;
	}
	
	@Override
	public AperturePlane getApature()
	{
		return Wormholes.aperture.getRemoteApaturePlanes().get(getKey());
	}
	
	@Override
	public Boolean getSided()
	{
		return sided;
	}
	
	@Override
	public void setSided(Boolean sided)
	{
		this.sided = sided;
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
	}
	
	@Override
	public boolean hasDisplayName()
	{
		return displayName != null && displayName.length() > 0;
	}
	
	@Override
	public void save()
	{
		
	}
	
	public Boolean getWait()
	{
		return wait;
	}
}
