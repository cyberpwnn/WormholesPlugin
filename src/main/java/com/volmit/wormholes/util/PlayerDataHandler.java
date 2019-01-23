package com.volmit.wormholes.util;

import org.bukkit.entity.Player;

/**
 * A PlayerDataController
 * 
 * @author cyberpwn
 * @param <C>
 *            the playerdata object
 */
@SuppressWarnings("hiding")
public abstract class PlayerDataHandler<C extends Configurable> extends DataController<C, Player>
{
	public PlayerDataHandler(Controllable parentController)
	{
		super(parentController);
	}
	
	@Override
	public abstract C onLoad(Player identifier);
	
	@Override
	public abstract void onSave(Player identifier);
}
