package org.cyberpwn.vortex.aperture;

import org.cyberpwn.vortex.Wormholes;

public class Aperture extends EntityHider
{
	public Aperture(Policy policy)
	{
		super(Wormholes.instance, policy);
	}
}
