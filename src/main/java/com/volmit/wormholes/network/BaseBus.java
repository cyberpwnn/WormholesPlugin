package com.volmit.wormholes.network;

import com.volmit.wormholes.util.DB;
import com.volmit.wormholes.util.GList;

public abstract class BaseBus implements TransmissionBus
{
	private GList<Transmission> inbox;
	private GList<Transmission> outbox;
	
	public BaseBus()
	{
		inbox = new GList<Transmission>();
		outbox = new GList<Transmission>();
	}
	
	@Override
	public GList<Transmission> getInbox()
	{
		return inbox.copy();
	}
	
	@Override
	public GList<Transmission> getOutbox()
	{
		return outbox.copy();
	}
	
	@Override
	public void inbox(Transmission t)
	{
		DB.d(this, "Received Transmission: " + t.toJSON().toString());
		inbox.add(t);
	}
	
	@Override
	public void outbox(Transmission t)
	{
		DB.d(this, "Sent Transmission: " + t.toJSON().toString());
		outbox.add(t);
	}
	
	@Override
	public void read(Transmission t)
	{
		inbox.remove(t);
	}
	
	@Override
	public void cancel(Transmission t)
	{
		outbox.remove(t);
	}
}
