package com.volmit.wormholes.projection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalIdentity;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.MaterialBlock;

public class VRBuilder
{
	protected Player player;
	protected RasteredSystem rast;
	protected Viewport view;
	protected RenderStage stage;
	protected RenderMode mode;
	protected PortalIdentity ida;
	protected PortalIdentity idb;
	protected GMap<Vector, MaterialBlock> dimension;
	protected Location focii;
	
	public VRBuilder(Portal portal, Player player)
	{
		this.player = player;
		this.ida = portal.getIdentity();
		this.idb = portal.getWormhole().getDestination().getIdentity();
		this.rast = Wormholes.provider.getRasterer();
		this.focii = portal.getPosition().getCenter();
	}
	
	public VRBuilder setView(Viewport view)
	{
		this.view = view;
		
		return this;
	}
	
	public VRBuilder setMode(RenderMode mode)
	{
		this.mode = mode;
		
		return this;
	}
	
	public VRBuilder setStage(RenderStage stage)
	{
		this.stage = stage;
		
		return this;
	}
	
	public VRBuilder setDimension(GMap<Vector, MaterialBlock> dimension)
	{
		this.dimension = dimension;
		
		return this;
	}
	
	public ViewportRenderer build()
	{
		return new ViewportRendererPortal(player, ida, idb, view, stage, mode, dimension, focii);
	}
}
