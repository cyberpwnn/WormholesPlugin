package com.volmit.wormholes;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.event.Listener;

import com.volmit.wormholes.portal.GatewayPortal;
import com.volmit.wormholes.portal.ILocalPortal;
import com.volmit.wormholes.portal.IPortal;
import com.volmit.wormholes.portal.LocalPortal;
import com.volmit.wormholes.portal.PortalStructure;
import com.volmit.wormholes.portal.PortalType;
import com.volmit.wormholes.portal.WormholePortal;
import com.volmit.wormholes.util.A;
import com.volmit.wormholes.util.GList;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.J;
import com.volmit.wormholes.util.JSONObject;
import com.volmit.wormholes.util.VIO;

public class PortalManager implements Listener
{
	private GMap<UUID, ILocalPortal> portals;

	public PortalManager()
	{
		Wormholes.v("Starting Portal Manager");
		portals = new GMap<>();
		J.ar(() -> updateLocalPortals(), 0);
		J.a(() -> loadExistingPortals());
	}

	private void loadExistingPortals()
	{
		Wormholes.v("Loading existing portals...");
		File portalFolder = new File(Wormholes.instance.getDataFolder(), "portals");
		portalFolder.mkdirs();

		for(File i : portalFolder.listFiles())
		{
			if(i.isDirectory())
			{
				for(File j : i.listFiles())
				{
					if(j.isDirectory())
					{
						for(File k : j.listFiles())
						{
							if(k.isFile() && k.getName().endsWith(".json"))
							{
								loadPortal(k);
							}
						}
					}
				}
			}
		}
	}

	private void loadPortal(File k)
	{
		Wormholes.v("Loading Portal " + k.getName());

		try
		{
			JSONObject j = new JSONObject(VIO.readAll(k));
			PortalType type = PortalType.valueOf(j.getString("type"));
			ILocalPortal portal = null;
			PortalStructure structure = new PortalStructure();
			structure.loadJSON(j.getJSONObject("structure"));

			switch(type)
			{
				case GATEWAY:
					portal = new GatewayPortal(UUID.fromString(j.getString("id")), structure);
					break;
				case PORTAL:
					portal = new LocalPortal(UUID.fromString(j.getString("id")), type, structure);
					break;
				case WORMHOLE:
					portal = new WormholePortal(UUID.fromString(j.getString("id")), type, structure);
					break;
				default:
					break;
			}

			if(portal != null)
			{
				portal.loadJSON(j);
				portal.save();
				Wormholes.portalManager.addLocalPortal(portal);
				Wormholes.v("Loaded local portal from file " + portal.getId() + " (" + portal.getName() + ")");
			}

			else
			{
				Wormholes.f("Failed to load portal via type " + type);
			}
		}

		catch(Throwable e)
		{
			Wormholes.f("Failed to load portal file " + k.getName());
			e.printStackTrace();
		}
	}

	public void saveAll()
	{
		for(ILocalPortal i : portals.v())
		{
			i.save();
		}
	}

	public void saveAllNow()
	{
		for(ILocalPortal i : portals.v())
		{
			try
			{
				i.saveNow();
			}

			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void updateLocalPortals()
	{
		for(ILocalPortal i : getLocalPortals())
		{
			updateLocalPortal(i);
		}
	}

	private void updateLocalPortal(ILocalPortal i)
	{
		i.update();

		if(i.needsSaving())
		{
			i.willSave();

			new A()
			{
				@Override
				public void run()
				{
					try
					{
						i.saveNow();
					}

					catch(IOException e)
					{
						e.printStackTrace();
					}
				}
			};
		}
	}

	public GList<ILocalPortal> getLocalPortals()
	{
		return portals.v();
	}

	public boolean hasLocalPortal(UUID id)
	{
		return portals.containsKey(id);
	}

	public boolean hasLocalPortal(IPortal portal)
	{
		return hasLocalPortal(portal.getId());
	}

	public void addLocalPortal(ILocalPortal portal)
	{
		if(!hasLocalPortal(portal))
		{
			portals.put(portal.getId(), portal);
			Wormholes.instance.registerListener(portal);
		}
	}

	public void removeLocalPortal(UUID portal)
	{
		if(portals.containsKey(portal))
		{
			Wormholes.instance.unregisterListener(portals.get(portal));
		}

		portals.remove(portal);
	}

	public void removeLocalPortal(IPortal portal)
	{
		removeLocalPortal(portal.getId());
	}

	public int getTotalPortalCount()
	{
		return getLocalPortals().size();
	}

	public int getAccessableCount(PortalType t)
	{
		if(t.equals(PortalType.GATEWAY))
		{
			return getGatewayCount();
		}

		return getTotalPortalCount() - getGatewayCount();
	}

	public int getGatewayCount()
	{
		int g = 0;

		for(ILocalPortal i : portals.v())
		{
			if(i.isGateway())
			{
				g++;
			}
		}

		return g;
	}

	public File getSaveFile(UUID id)
	{
		return new File(new File(new File(new File(Wormholes.instance.getDataFolder(), "portals"), id.toString().split("-")[1]), id.toString().split("-")[0]), id.toString() + ".json");
	}

	public void shutDown()
	{
		Wormholes.v("Shutting down portal manager");
		saveAllNow();
		portals.clear();
	}
}
