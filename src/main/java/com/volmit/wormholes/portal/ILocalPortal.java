package com.volmit.wormholes.portal;

import org.bukkit.entity.Player;

import com.volmit.wormholes.inventory.Window;
import com.volmit.wormholes.util.Direction;

public interface ILocalPortal extends IPortal
{
	public PortalStructure getStructure();

	public PortalType getType();

	public void update();

	public void close();

	public boolean isOpen();

	public void open();

	public void setOpen(boolean open);

	public void onLooking(Player p, boolean holdingWand);

	public void onWanded(Player p);

	public boolean isLookingAt(Player p);

	public void receive(Traversive t);

	public void setDirection(Direction d);

	public ITunnel getTunnel();

	public boolean hasTunnel();

	public void setDestination(IPortal portal);

	public void openPortalMenu(Player p);

	public Window createPortalMenu(Player p);

	public void chooseDestination(Player p);

	public void destroy();

	public void changeName(Player p);

	public String getRouter(boolean dark);

	public String getRouter(boolean dark, IPortal source);
}
