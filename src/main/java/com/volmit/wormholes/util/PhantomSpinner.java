package com.volmit.wormholes.util;

import mortar.util.text.C;

/**
 * Colored circle spinner
 * 
 * @author cyberpwn
 */
public class PhantomSpinner
{
	private ProgressSpinner s;
	private ProgressSpinner c;
	
	public PhantomSpinner(C light, C mid, C dark)
	{
		s = new ProgressSpinner();
		c = new ProgressSpinner(light.toString(), light.toString(), light.toString(), mid.toString(), dark.toString(), dark.toString(), dark.toString(), mid.toString());
	}
	
	@Override
	public String toString()
	{
		return c.toString() + s.toString();
	}
}
