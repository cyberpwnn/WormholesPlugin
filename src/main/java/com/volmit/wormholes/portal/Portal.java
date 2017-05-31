package com.volmit.wormholes.portal;

import com.volmit.wormholes.aperture.AperturePlane;
import com.volmit.wormholes.projection.ProjectionPlane;
import com.volmit.wormholes.service.MutexService;
import com.volmit.wormholes.wormhole.Wormhole;
import wraith.DataCluster;

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
}
