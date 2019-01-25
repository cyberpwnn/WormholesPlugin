package com.volmit.wormholes;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.volmit.wormholes.nms.NMP;
import com.volmit.wormholes.nms.PacketBuffer;
import com.volmit.wormholes.util.GMap;
import com.volmit.wormholes.util.J;

public class NetworkManager implements Listener
{
	private GMap<Player, PacketBuffer> buffers;

	public NetworkManager()
	{
		buffers = new GMap<>();
		J.ar(() -> flush(), 0);
	}

	private void flush()
	{
		for(Player i : buffers.k())
		{
			flush(i);
		}
	}

	public void queue(Player p, PacketBuffer buffer)
	{
		if(!buffers.containsKey(p))
		{
			buffers.put(p, new PacketBuffer());
		}

		buffers.get(p).q(buffer.get());
	}

	private void flush(Player i)
	{
		PacketBuffer b = buffers.get(i);

		if(b.hasNext())
		{
			NMP.host.sendPacket(i, b.next());
		}
	}
}
