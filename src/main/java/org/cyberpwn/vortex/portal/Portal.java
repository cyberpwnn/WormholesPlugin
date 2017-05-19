package org.cyberpwn.vortex.portal;

import org.cyberpwn.vortex.aperture.AperturePlane;
import org.cyberpwn.vortex.projection.ProjectionPlane;
import org.cyberpwn.vortex.service.MutexService;
import org.cyberpwn.vortex.wormhole.Wormhole;
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
