package com.volmit.wormholes.util.lang;

import java.io.InputStream;

@FunctionalInterface
public interface InputHandler
{
	public void read(InputStream in);
}
