package com.volmit.wormholes.util;

public abstract class A
{
	public A()
	{
		Wraith.poolManager.queue(new Execution()
		{
			@Override
			public void run()
			{
				async();
			}
		});
	}
	
	public abstract void async();
}
