package com.volmit.wormholes.portal;

import java.util.UUID;

import com.volmit.wormholes.portal.shape.PortalStructure;

public class LocalPortal extends Portal implements ILocalPoral
{
	private final PortalStructure structure;
	private final PortalType type;

	public LocalPortal(UUID id, PortalType type, PortalStructure structure)
	{
		super(id);
		this.type = type;
		this.structure = structure;
	}

	@Override
	public PortalStructure getStructure()
	{
		return structure;
	}

	@Override
	public PortalType getType()
	{
		return type;
	}
}
