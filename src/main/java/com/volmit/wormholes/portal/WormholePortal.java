package com.volmit.wormholes.portal;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.volmit.wormholes.inventory.Element;
import com.volmit.wormholes.inventory.MaterialBlock;
import com.volmit.wormholes.inventory.UIElement;
import com.volmit.wormholes.inventory.Window;
import com.volmit.wormholes.util.C;

public class WormholePortal extends LocalPortal implements IWormholePortal
{
	private boolean projecting;

	public WormholePortal(UUID id, PortalType type, PortalStructure structure)
	{
		super(id, type, structure);
		projecting = false;
	}

	@Override
	public void update()
	{
		super.update();

		if(isOpen() && isProjecting())
		{
			flushProjections();
		}
	}

	private void flushProjections()
	{
		// TODO mhm
	}

	@Override
	public Window uiCreatePortalMenu(Player p)
	{
		Window w = super.uiCreatePortalMenu(p);
		//@builder
		w.setElement(-1, 1, new UIElement("toggle-projections")
				.setName(isProjecting() ? C.LIGHT_PURPLE + "" + C.BOLD + "Projections Enabled" : C.DARK_PURPLE + "" + C.BOLD + "Projections Disabled")
				.setEnchanted(isProjecting())
				.setMaterial(new MaterialBlock(isProjecting() ? Material.REDSTONE_TORCH_ON : Material.TORCH))
				.addLore(C.GRAY + "Projections show blocks from the destination")
				.addLore(C.GRAY + "portal relative to this portal.")
				.onLeftClick((e) -> toggle(w, e)));
		//@done
		return w;
	}

	private void toggle(Window w, Element e)
	{
		setProjecting(!isProjecting());
		e.setName(isProjecting() ? C.LIGHT_PURPLE + "" + C.BOLD + "Projections Enabled" : C.DARK_PURPLE + "" + C.BOLD + "Projections Disabled");
		e.setEnchanted(isProjecting());
		e.setMaterial(new MaterialBlock(isProjecting() ? Material.REDSTONE_TORCH_ON : Material.TORCH));
		w.updateInventory();
	}

	@Override
	public boolean isProjecting()
	{
		return projecting;
	}

	@Override
	public void setProjecting(boolean projecting)
	{
		this.projecting = projecting;
	}
}
