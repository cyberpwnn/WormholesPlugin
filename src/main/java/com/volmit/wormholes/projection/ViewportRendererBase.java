package com.volmit.wormholes.projection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalIdentity;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.MaterialBlock;

public abstract class ViewportRendererBase implements ViewportRenderer
{
	protected Player player;
	protected RasteredSystem rast;
	protected Viewport view;
	protected RenderStage stage;
	protected RenderMode mode;
	protected PortalIdentity ida;
	protected PortalIdentity idb;
	protected Location focii;
	protected GMap<Vector, MaterialBlock> dimension;
	
	public ViewportRendererBase(Player player, PortalIdentity ida, PortalIdentity idb, Viewport view, RenderStage stage, RenderMode mode, GMap<Vector, MaterialBlock> dimension, Location focii)
	{
		this.rast = Wormholes.provider.getRasterer();
		this.view = view;
		this.stage = stage;
		this.mode = mode;
		this.player = player;
		this.player = player;
		this.ida = ida;
		this.idb = idb;
		this.dimension = dimension;
		this.focii = focii;
	}
	
	public ViewportRendererBase(Player player, Portal portal, Viewport view, RenderStage stage, RenderMode mode, GMap<Vector, MaterialBlock> dimension)
	{
		this(player, portal.getIdentity(), portal.getWormhole().getDestination().getIdentity(), view, stage, mode, dimension, portal.getPosition().getCenter());
	}
}
