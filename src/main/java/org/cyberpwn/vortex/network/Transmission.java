package org.cyberpwn.vortex.network;

import java.io.IOException;
import org.cyberpwn.vortex.Wormholes;
import wraith.DataCluster;
import wraith.M;

public class Transmission extends DataCluster
{
	public Transmission(byte[] data) throws IOException
	{
		super(data);
	}
	
	public Transmission(String source, String destination, String type)
	{
		super();
		setSource(source);
		setDestination(destination);
		setType(type);
		setId(M.ms());
	}
	
	public void send()
	{
		Wormholes.bus.outbox(this);
	}
	
	public void setId(long id)
	{
		set("pi", id);
	}
	
	public void setSource(String s)
	{
		set("ps", s);
	}
	
	public void setDestination(String d)
	{
		set("pd", d);
	}
	
	public void setType(String t)
	{
		set("pt", t);
	}
	
	public long getId()
	{
		return getLong("pi");
	}
	
	public String getSource()
	{
		return getString("ps");
	}
	
	public String getDestination()
	{
		return getString("pd");
	}
	
	public String getType()
	{
		return getString("pt");
	}
	
	public Transmission createResponse()
	{
		Transmission t = new Transmission(getDestination(), getSource(), getType());
		t.setId(getId());
		
		return t;
	}
	
	public void forceSend()
	{
		Wormholes.bus.forceFlush(this);
	}
}
