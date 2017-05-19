package org.cyberpwn.vortex.network;

import wraith.GList;

public interface TransmissionBus
{
	public GList<Transmission> getInbox();
	
	public GList<Transmission> getOutbox();
	
	public void inbox(Transmission t);
	
	public void outbox(Transmission t);
	
	public void read(Transmission t);
	
	public void cancel(Transmission t);
	
	public void flush();
}
