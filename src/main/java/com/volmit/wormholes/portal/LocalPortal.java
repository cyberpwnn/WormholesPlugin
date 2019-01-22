package com.volmit.wormholes.portal;

import java.util.UUID;

import com.volmit.wormholes.portal.shape.PortalStructure;

public class LocalPortal extends Portal implements ILocalPortal
{
	private final PortalStructure structure;
	private final PortalType type;
	private boolean openCurrent;
	private boolean open;
	private double stateProgress;
	private String state;

	public LocalPortal(UUID id, PortalType type, PortalStructure structure)
	{
		super(id);
		this.type = type;
		this.structure = structure;
		open = false;
		openCurrent = false;
		stateProgress = 0;
		state = "Idle";
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
		if(isOpening())
		{
			stateProgress += 0.003;

			if(getStateProgress() >= 1)
			{
				stateProgress = 0;
				openCurrent = true;
			}
		}

		else if(isClosing())
		{
			stateProgress += 0.007;

			if(getStateProgress() >= 1)
			{
				stateProgress = 0;
				openCurrent = false;
			}
		}
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
		stateProgress = 0;
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

	@Override
	public double getStateProgress()
	{
		return stateProgress;
	}

	@Override
	public String getState()
	{
		return state;
	}

	@Override
	public boolean isShowingProgress()
	{
		return isOpen() || isClosing();
	}
}
