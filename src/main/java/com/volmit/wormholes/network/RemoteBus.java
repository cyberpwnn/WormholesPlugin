package com.volmit.wormholes.network;

import java.io.IOException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.volmit.wormholes.Wormholes;
import wraith.ForwardedPluginMessage;

public class RemoteBus extends BaseBus implements PluginMessageListener
{
	public RemoteBus()
	{
		super();
		
		Wormholes.instance.getServer().getMessenger().registerIncomingPluginChannel(Wormholes.instance, "BungeeCord", this);
	}
	
	@Override
	public void flush()
	{
		for(Transmission i : getOutbox())
		{
			cancel(i);
			
			try
			{
				sendTransmission(i);
			}
			
			catch(IOException e)
			{
				System.out.println("Failed to transmit data: " + i.toJSON().toString());
				e.printStackTrace();
			}
		}
	}
	
	private void sendTransmission(Transmission t) throws IOException
	{
		new ForwardedPluginMessage(Wormholes.instance, CL.L1.get(), t.getDestination(), t.compress()).send();
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] message)
	{
		try
		{
			if(!channel.equals("BungeeCord"))
			{
				return;
			}
			
			ByteArrayDataInput in = ByteStreams.newDataInput(message);
			String subchannel = in.readUTF();
			
			if(subchannel.equals(CL.L1.get()))
			{
				short len = in.readShort();
				byte[] msgbytes = new byte[len];
				in.readFully(msgbytes);
				inbox(new Transmission(msgbytes));
			}
			
			if(subchannel.equals(CL.L2.get()))
			{
				short len = in.readShort();
				byte[] msgbytes = new byte[len];
				in.readFully(msgbytes);
				Wormholes.host.layer2Stream(msgbytes);
			}
			
			if(subchannel.equals(CL.L3.get()))
			{
				short len = in.readShort();
				byte[] msgbytes = new byte[len];
				in.readFully(msgbytes);
				Wormholes.aperture.layer3Stream(msgbytes);
			}
		}
		
		catch(IOException e)
		{
			System.out.println("Failed to receive Transmission");
			e.printStackTrace();
		}
	}
	
	public void remoteForceSend(Transmission transmission)
	{
		try
		{
			sendTransmission(transmission);
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
