package com.volmit.wormholes.util;

public class TextBlock
{
	private GList<String> block;
	
	public TextBlock()
	{
		block = new GList<String>();
	}
	
	public void add(String s)
	{
		block.add(s);
	}
	
	public GList<String> getBlock()
	{
		return block;
	}
}
