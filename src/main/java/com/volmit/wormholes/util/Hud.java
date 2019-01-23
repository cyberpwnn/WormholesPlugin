package com.volmit.wormholes.util.lang;

import org.bukkit.entity.Player;

public interface Hud
{
	public void open();
	
	public void close();
	
	public void setContents(GList<String> options);
	
	public GList<String> getContents();
	
	public String getSelection();
	
	public int getSelectionRow();
	
	public void onUpdate();
	
	public void onOpen();
	
	public String onDisable(String s);
	
	public String onEnable(String s);
	
	public void onClose();
	
	public void onSelect(String selection, int slot);
	
	public void onClick(Click c, Player p, String selection, int slot, Hud h);
}
