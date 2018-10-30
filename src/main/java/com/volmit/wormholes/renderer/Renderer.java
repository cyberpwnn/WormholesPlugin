package com.volmit.wormholes.renderer;

import org.bukkit.entity.Player;

import com.volmit.volume.lang.collections.GList;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.projection.Viewport;

public interface Renderer
{
	public Viewport getViewport(Player p, LocalPortal portal);

	public Viewport getLastViewport(Player p, LocalPortal portal);

	public Renderlet getRenderlet(Player p, LocalPortal portal);

	public GList<Renderlet> getActiveRenderlets();

	public double getMsPerIteration();

	public void render(double maxMs);
}
