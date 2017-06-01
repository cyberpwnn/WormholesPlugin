package com.volmit.wormholes.provider;

import org.bukkit.entity.Player;
import com.volmit.wormholes.exception.DuplicatePortalKeyException;
import com.volmit.wormholes.exception.InvalidPortalKeyException;
import com.volmit.wormholes.exception.InvalidPortalPositionException;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalIdentity;
import com.volmit.wormholes.portal.PortalKey;
import com.volmit.wormholes.portal.PortalPosition;
import com.volmit.wormholes.projection.RasteredSystem;
import com.volmit.wormholes.projection.Viewport;
import wraith.GMap;

public interface PortalProvider
{
	public void flush();
	
	public void loadAllPortals();
	
	public void save(LocalPortal p);
	
	public LocalPortal createPortal(PortalIdentity identity, PortalPosition position) throws InvalidPortalKeyException, InvalidPortalPositionException, DuplicatePortalKeyException;
	
	public boolean hasMoved(Player p);
	
	public void movePlayer(Player p);
	
	public void destroyPortal(LocalPortal portal);
	
	public RasteredSystem getRasterer();
	
	public Viewport getViewport(Player p, Portal portal);
	
	public boolean isNear(Player p, Portal portal);
	
	public boolean canSee(Player p, Portal portal);
	
	public GMap<Player, Viewport> getViewport(Portal portal);
	
	public PortalKey buildKey(PortalPosition p) throws InvalidPortalKeyException;
	
	public void wipe(LocalPortal localPortal);
	
	public void dfs();
	
	public void dfd();
}