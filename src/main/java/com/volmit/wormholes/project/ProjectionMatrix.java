package com.volmit.wormholes.project;

import org.bukkit.util.Vector;

import com.volmit.wormholes.portal.ILocalPortal;
import com.volmit.wormholes.portal.IPortal;
import com.volmit.wormholes.portal.IRemotePortal;
import com.volmit.wormholes.util.MaterialBlock;

public class ProjectionMatrix
{
	private IPortal portal;
	private IWorldAccess access;

	public ProjectionMatrix(IPortal portal)
	{
		this.portal = portal;
		this.access = portal.isRemote() ? new ServerWorldAccess(((IRemotePortal) portal).getServer()) : new LocalWorldAccess(((ILocalPortal) portal).getWorld());
	}

	public MaterialBlock getBlock(int x, int y, int z, ILocalPortal context)
	{
		Vector angle = portal.getDirection().angle(new Vector(x, y, z), context.getDirection()).add(portal.getOrigin());
		return access.getBlock(angle.getBlockX(), angle.getBlockY(), angle.getBlockZ());
	}

	public byte getSkyLight(int x, int y, int z, ILocalPortal context)
	{
		Vector angle = portal.getDirection().angle(new Vector(x, y, z), context.getDirection()).add(portal.getOrigin());
		return access.getSkyLight(angle.getBlockX(), angle.getBlockY(), angle.getBlockZ());
	}

	public byte getBlockLight(int x, int y, int z, ILocalPortal context)
	{
		Vector angle = portal.getDirection().angle(new Vector(x, y, z), context.getDirection()).add(portal.getOrigin());
		return access.getBlockLight(angle.getBlockX(), angle.getBlockY(), angle.getBlockZ());
	}
}
