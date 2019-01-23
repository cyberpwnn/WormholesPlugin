package com.volmit.wormholes.util.lang;

public class TFColor implements TextFilter
{
	private C c;
	
	public TFColor(C c)
	{
		this.c = c;
	}
	
	@Override
	public String onFilter(String initial)
	{
		return c + initial;
	}
}
