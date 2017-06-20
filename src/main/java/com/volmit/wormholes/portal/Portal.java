package com.volmit.wormholes.portal;

import com.volmit.wormholes.aperture.AperturePlane;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.service.MutexService;
import com.volmit.wormholes.util.DataCluster;
import com.volmit.wormholes.wormhole.Wormhole;

public interface Portal
{
	public void update();
	
	public PortalIdentity getIdentity();
	
	public PortalPosition getPosition();
	
	public boolean hasValidKey();
	
	public PortalKey getKey();
	
	public boolean hasWormhole();
	
	public boolean isWormholeMutex();
	
	public Wormhole getWormhole();
	
	public MutexService getService();
	
	public DataCluster toData();
	
	public void fromData(DataCluster cc);
	
	public String getServer();
	
	public ProjectionPlane getProjectionPlane();
	
	public AperturePlane getApature();
	
	public Boolean getSided();
	
	public void setSided(Boolean sided);
	
	public String getDisplayName();
	
	public void updateDisplayName(String n);
	
	public boolean hasDisplayName();
	
	public void save();
}
