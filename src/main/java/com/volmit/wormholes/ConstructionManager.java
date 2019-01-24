package com.volmit.wormholes;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.volmit.wormholes.portal.GatewayPortal;
import com.volmit.wormholes.portal.ILocalPortal;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.PortalStructure;
import com.volmit.wormholes.portal.PortalType;
import com.volmit.wormholes.portal.WormholePortal;
import com.volmit.wormholes.util.Cuboid;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GSet;
import com.volmit.wormholes.util.S;

public class ConstructionManager implements Listener
{
	public ConstructionManager()
	{

	}

	public void constructPortal(Player player, Set<Block> blocks, PortalType type, Direction d)
	{
		new S(25)
		{
			@Override
			public void run()
			{
				Cuboid c = null;

				for(Block i : blocks)
				{
					if(c == null)
					{
						c = new Cuboid(i.getLocation());
					}

					else
					{
						c = c.getBoundingCuboid(new Cuboid(i.getLocation()));
					}
				}

				boolean success = true;
				Iterator<Block> it = c.iterator();

				while(it.hasNext())
				{
					if(!blocks.contains(it.next()))
					{
						success = false;
						break;
					}
				}

				if(success)
				{
					Wormholes.effectManager.playNotificationSuccess(ChatColor.GREEN + "Right Click the Portal to Configure it.", c.getCenter());
					Wormholes.effectManager.playPortalOpen(blocks);
					PortalStructure s = new PortalStructure();
					s.setWorld(c.getWorld());
					s.setArea(c);
					ILocalPortal portal = createPortal(s, type);
					portal.setDirection(d);
					Wormholes.portalManager.addLocalPortal(portal);
				}

				else
				{
					Wormholes.effectManager.playNotificationFail(ChatColor.RED + "Portal shape must be rectangular or square.", new GList<Block>(blocks).pickRandom().getLocation());
					Wormholes.effectManager.playPortalFailOpen(blocks);
					Wormholes.blockManager.refund(blocks, type);
				}
			}
		};
	}

	private ILocalPortal createPortal(PortalStructure s, PortalType type)
	{
		switch(type)
		{
			case GATEWAY:
				return new GatewayPortal(UUID.randomUUID(), s);
			case PORTAL:
				return new LocalPortal(UUID.randomUUID(), type, s);
			case WORMHOLE:
				return new WormholePortal(UUID.randomUUID(), type, s);
			default:
				break;
		}

		return null;
	}

	public void destroy(ILocalPortal localPortal)
	{
		GSet<Block> blocks = localPortal.getStructure().toBlocks();
		Wormholes.effectManager.playNotificationFail(ChatColor.RED + localPortal.getName() + " Destroyed", localPortal.getStructure().getCenter());
		Wormholes.effectManager.playPortalFailOpen(blocks);
		Wormholes.blockManager.refund(blocks, localPortal.getType());
	}
}