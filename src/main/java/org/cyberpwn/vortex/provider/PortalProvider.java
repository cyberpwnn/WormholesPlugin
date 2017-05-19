package org.cyberpwn.vortex.provider;

import org.bukkit.entity.Player;
import org.cyberpwn.vortex.exception.DuplicatePortalKeyException;
import org.cyberpwn.vortex.exception.InvalidPortalKeyException;
import org.cyberpwn.vortex.exception.InvalidPortalPositionException;
import org.cyberpwn.vortex.portal.LocalPortal;
import org.cyberpwn.vortex.portal.Portal;
import org.cyberpwn.vortex.portal.PortalIdentity;
import org.cyberpwn.vortex.portal.PortalKey;
import org.cyberpwn.vortex.portal.PortalPosition;
import org.cyberpwn.vortex.projection.RasteredSystem;
import org.cyberpwn.vortex.projection.Viewport;
import wraith.GMap;

public interface PortalProvider
{
	public void flush();
	
	public LocalPortal createPortal(PortalIdentity identity, PortalPosition position) throws InvalidPortalKeyException, InvalidPortalPositionException, DuplicatePortalKeyException;
	
	public void movePlayer(Player p);
	
	public void destroyPortal(LocalPortal portal);
	
	public RasteredSystem getRasterer();
	
	public Viewport getViewport(Player p, Portal portal);
	
	public boolean isNear(Player p, Portal portal);
	
	public boolean canSee(Player p, Portal portal);
	
	public GMap<Player, Viewport> getViewport(Portal portal);
	
	public PortalKey buildKey(PortalPosition p) throws InvalidPortalKeyException;
}
