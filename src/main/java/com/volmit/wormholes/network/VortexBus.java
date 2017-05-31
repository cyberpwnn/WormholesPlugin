package com.volmit.wormholes.network;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.service.TimingsService;
import wraith.GList;
import wraith.M;
import wraith.PluginMessage;
import wraith.Timer;

public class VortexBus implements TransmissionBus, PluginMessageListener
{
	private LocalBus l;
	private RemoteBus r;
	private String serverName;
	private GList<String> servers;
	private Boolean online;
	private Long throttle;
	
	public VortexBus()
	{
		l = new LocalBus();
		r = new RemoteBus();
		serverName = null;
		online = false;
		servers = new GList<String>();
		throttle = M.ms();
		Wormholes.instance.getServer().getMessenger().registerIncomingPluginChannel(Wormholes.instance, "BungeeCord", this);
	}
	
	@Override
	public GList<Transmission> getInbox()
	{
		GList<Transmission> t = new GList<Transmission>();
		t.add(l.getInbox());
		t.add(r.getInbox());
		
		return t;
	}
	
	@Override
	public GList<Transmission> getOutbox()
	{
		GList<Transmission> t = new GList<Transmission>();
		t.add(l.getOutbox());
		t.add(r.getOutbox());
		
		return t;
	}
	
	@Override
	public void inbox(Transmission t)
	{
		if(t.getSource().equals(""))
		{
			l.inbox(t);
		}
		
		else
		{
			r.inbox(t);
		}
	}
	
	@Override
	public void outbox(Transmission t)
	{
		if(t.getDestination().equals(""))
		{
			l.outbox(t);
		}
		
		else
		{
			r.outbox(t);
		}
	}
	
	@Override
	public void read(Transmission t)
	{
		l.read(t);
		r.read(t);
	}
	
	@Override
	public void cancel(Transmission t)
	{
		l.cancel(t);
		r.cancel(t);
	}
	
	@Override
	public void flush()
	{
		Timer t = new Timer();
		t.start();
		l.flush();
		r.flush();
		
		if(M.ms() - throttle > Settings.NETWORK_POLL_THRESHOLD)
		{
			throttle = M.ms();
			online = serverName != null;
			
			if(Bukkit.getOnlinePlayers().isEmpty())
			{
				online = false;
				t.stop();
				TimingsService.root.get("net-assist").hit("vortex-bus", t.getTime());
				return;
			}
			
			if(online)
			{
				requestServers();
			}
			
			else
			{
				requestName();
			}
		}
		
		t.stop();
		TimingsService.root.get("net-assist").hit("vortex-bus", t.getTime());
	}
	
	private void requestName()
	{
		new PluginMessage(Wormholes.instance, "GetServer").send();
	}
	
	private void requestServers()
	{
		new PluginMessage(Wormholes.instance, "GetServers").send();
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] message)
	{
		if(!channel.equals("BungeeCord"))
		{
			return;
		}
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		
		if(subchannel.equals("GetServers"))
		{
			servers = new GList<String>(in.readUTF().split(", "));
		}
		
		else if(subchannel.equals("GetServer"))
		{
			serverName = in.readUTF();
		}
	}
	
	public LocalBus getL()
	{
		return l;
	}
	
	public RemoteBus getR()
	{
		return r;
	}
	
	public String getServerName()
	{
		return serverName;
	}
	
	public GList<String> getServers()
	{
		return servers;
	}
	
	public Boolean isOnline()
	{
		return online;
	}
	
	public Long getThrottle()
	{
		return throttle;
	}
	
	public void forceFlush(Transmission transmission)
	{
		r.remoteForceSend(transmission);
	}
}
