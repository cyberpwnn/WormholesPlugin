package com.volmit.wormholes.projection;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalIdentity;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.MaterialBlock;
import com.volmit.wormholes.util.VectorMath;

public class ViewportRendererPortal extends ViewportRendererBase
{
	public ViewportRendererPortal(Player player, Portal portal, Viewport view, RenderStage stage, RenderMode mode, GMap<Vector, MaterialBlock> dimension)
	{
		super(player, portal, view, stage, mode, dimension);
	}

	public ViewportRendererPortal(Player player, PortalIdentity ida, PortalIdentity idb, Viewport view, RenderStage stage, RenderMode mode, GMap<Vector, MaterialBlock> dimension, Location focii)
	{
		super(player, ida, idb, view, stage, mode, dimension, focii);
	}

	@Override
	public void render()
	{
		if(stage.hasNextStage())
		{
			int s = stage.getCurrentStage();
			Cuboid c = view.getProjectionSet().get(s);
			Iterator<Block> it = c.iterator();
			stage.pop();

			while(it.hasNext())
			{
				try
				{
					Block b = it.next();
					Location l = b.getLocation();

					if(mode.equals(RenderMode.DIALATE) && view.contains(l))
					{
						Vector dir = VectorMath.directionNoNormal(focii, l);
						Vector vec = dir.clone().add(new Vector(0.5, 0.5, 0.5));
						ida.getFront().angle(vec, idb.getFront());
						MaterialBlock mb = dimension.get(vec);

						if(mb == null)
						{
							continue;
						}

						Wormholes.provider.getRasterer().queue(player, l, mb);
					}

					else if(mode.equals(RenderMode.ERODE))
					{
						rast.dequeue(player, l);
					}
				}

				catch(Exception e)
				{

				}
			}
		}
	}
}
