package com.volmit.wormholes.portal;

import java.util.UUID;

import com.volmit.wormholes.portal.shape.PortalStructure;

public class LocalPortal extends Portal implements ILocalPortal
{
	private final PortalStructure structure;
	private final PortalType type;
	private boolean openCurrent;
	private boolean open;

	public LocalPortal(UUID id, PortalType type, PortalStructure structure)
	{
		super(id);
		this.type = type;
		this.structure = structure;
		open = false;
		openCurrent = false;
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

	@Override
	public void update()
	{

	}

	@Override
	public void close()
	{
		setOpen(false);
	}

	@Override
	public boolean isOpen()
	{
		return openCurrent;
	}

	@Override
	public void open()
	{
		setOpen(true);
	}

	@Override
	public void setOpen(boolean open)
	{
		this.open = open;
	}

	@Override
	public boolean isClosing()
	{
		return openCurrent && !open;
	}

	@Override
	public boolean isOpening()
	{
		return !openCurrent && open;
	}
}
