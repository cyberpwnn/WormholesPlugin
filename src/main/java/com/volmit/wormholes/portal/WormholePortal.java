package com.volmit.wormholes.portal;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.wormholes.Settings;
import com.volmit.wormholes.geometry.Frustum4D;
import com.volmit.wormholes.inventory.Element;
import com.volmit.wormholes.inventory.UIElement;
import com.volmit.wormholes.inventory.Window;
import com.volmit.wormholes.project.BoundingBoxTracker;
import com.volmit.wormholes.project.IBoundingBoxTracker;
import com.volmit.wormholes.project.IProjectionTracker;
import com.volmit.wormholes.project.ProjectionTracker;
import com.volmit.wormholes.util.AxisAlignedBB;
import com.volmit.wormholes.util.JSONObject;
import com.volmit.wormholes.util.MaterialBlock;

import mortar.util.text.C;

public class WormholePortal extends LocalPortal implements IWormholePortal
{
	private boolean projecting;
	private IProjectionTracker tracker;
	private IBoundingBoxTracker<Player> ptracker;
	private AxisAlignedBB view;

	public WormholePortal(UUID id, PortalType type, PortalStructure structure)
	{
		super(id, type, structure);
		projecting = false;
		tracker = new ProjectionTracker(this);
		view = new AxisAlignedBB(getStructure().getArea().min().add(new Vector(-Settings.PROJECTION_RANGE, -Settings.PROJECTION_RANGE, -Settings.PROJECTION_RANGE)), getStructure().getArea().max().add(new Vector(Settings.PROJECTION_RANGE, Settings.PROJECTION_RANGE, Settings.PROJECTION_RANGE)));
		ptracker = new BoundingBoxTracker(getView(), getWorld());
	}

	@Override
	public void saveJSON(JSONObject j)
	{
		super.saveJSON(j);
		j.put("projecting", projecting);
	}

	@Override
	public void loadJSON(JSONObject j)
	{
		super.loadJSON(j);
		projecting = j.getBoolean("projecting");
		view = new AxisAlignedBB(getStructure().getArea().min().add(new Vector(-Settings.PROJECTION_RANGE, -Settings.PROJECTION_RANGE, -Settings.PROJECTION_RANGE)), getStructure().getArea().max().add(new Vector(Settings.PROJECTION_RANGE, Settings.PROJECTION_RANGE, Settings.PROJECTION_RANGE)));
		ptracker = new BoundingBoxTracker(getView(), getWorld());
	}

	@Override
	public JSONObject toJSON()
	{
		JSONObject o = new JSONObject();
		saveJSON(o);

		return o;
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
		ptracker.update();

		for(Player i : getPlayerTracker().getEntering())
		{
			getTracker().startTracking(i);
		}

		for(Player i : getPlayerTracker().getExiting())
		{
			getTracker().stopTracking(i);
		}

		for(Player i : getPlayerTracker().getInside())
		{
			getTracker().getTrackedProjectors().get(i).swapBuffers(new Frustum4D(i.getEyeLocation(), getStructure(), (int) Settings.PROJECTION_RANGE));
		}
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
		save();
	}

	@Override
	public IProjectionTracker getTracker()
	{
		return tracker;
	}

	@Override
	public AxisAlignedBB getView()
	{
		return view;
	}

	@Override
	public IBoundingBoxTracker<Player> getPlayerTracker()
	{
		return ptracker;
	}
}
