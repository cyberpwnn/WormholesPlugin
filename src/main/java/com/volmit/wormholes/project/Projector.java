package com.volmit.wormholes.project;

import org.bukkit.entity.Player;

import com.volmit.wormholes.portal.LocalPortal;

public interface Projector
{
	public void project(LocalPortal p, Player observer);

	public void deproject(LocalPortal p, Player observer);
}
