package tests;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.volmit.wormholes.portal.Portal;
import com.volmit.wormholes.portal.PortalIdentity;
import com.volmit.wormholes.util.Direction;
import com.volmit.wormholes.util.M;
import com.volmit.wormholes.util.VectorMath;

public class TVP
{
	private Player player;
	private Portal portal;
	private Direction direction;
	private Location a;
	private Location d;
	private Location e;
	private Location iris;

	public TVP(Player player, Portal portal)
	{
		this.player = player;
		this.portal = portal;
		direction = portal.getIdentity().getFront();
	}

	public void rebuild()
	{
		iris = player.getLocation();
		e = portal.getPosition().getCenter();
		Location a = portal.getPosition().getCornerDL();
		Location d = portal.getPosition().getCornerUR();
		Vector va = VectorMath.direction(iris, a);
		Vector vd = VectorMath.direction(iris, d);
		Vector ve = VectorMath.direction(iris, e);
		PortalIdentity id = portal.getIdentity();
		this.direction = Direction.closest(ve, id.getFront(), id.getBack());
		this.a = a.clone().add(va).setDirection(va);
		this.d = d.clone().add(vd).setDirection(vd);
		this.e = e.clone().add(ve).setDirection(ve);
	}

	public boolean contains(Location l)
	{
		Vector vx = VectorMath.direction(e, l);
		PortalIdentity id = portal.getIdentity();
		Direction dir = Direction.closest(vx, id.getFront(), id.getBack());

		if(dir.equals(direction))
		{
			Vector va = a.getDirection();
			Vector vb = d.getDirection();
			double minX = M.min(va.getX(), vb.getX());
			double maxX = M.max(va.getX(), vb.getX());
			double minY = M.min(va.getY(), vb.getY());
			double maxY = M.max(va.getY(), vb.getY());
			double minZ = M.min(va.getZ(), vb.getZ());
			double maxZ = M.max(va.getZ(), vb.getZ());

			switch(dir.getAxis())
			{
				case X:
					return vx.getY() >= minY && vx.getY() <= maxY && vx.getZ() >= minZ && vx.getZ() <= maxZ;
				case Y:
					return vx.getX() >= minX && vx.getX() <= maxX && vx.getZ() >= minZ && vx.getZ() <= maxZ;
				case Z:
					return vx.getX() >= minX && vx.getX() <= maxX && vx.getX() >= minX && vx.getX() <= maxX;
			}
		}

		return false;
	}
}
