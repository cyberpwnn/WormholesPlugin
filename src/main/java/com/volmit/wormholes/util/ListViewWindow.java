package com.volmit.wormholes.util;

import org.bukkit.entity.Player;

/**
 * Its a list view
 * 
 * @author cyberpwn
 */
public class ListViewWindow extends PhantomWindow
{
	private GList<Element> binds;
	
	public ListViewWindow(String title, Player viewer)
	{
		super(title, viewer);
		
		binds = new GList<Element>();
	}
	
	public Window addElement(Element e)
	{
		binds.add(e);
		
		return this;
	}
}
