package com.volmit.wormholes.project;

import org.bukkit.entity.Entity;

import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GSet;

public interface IBoundingBoxTracker<T extends Entity>
{
	public GSet<T> getInside();

	public GSet<T> getEntering();

	public GSet<T> getExiting();

	public GList<T> get();

	public void update();
}
