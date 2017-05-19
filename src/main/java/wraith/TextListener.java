package wraith;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public abstract class TextListener implements Listener
{
	private Player player;
	
	public TextListener(Player p, String instructions)
	{
		player = p;
		
		if(instructions != null)
		{
			p.sendMessage(instructions);
		}
		
		Wraith.registerListener(this);
	}
	
	@EventHandler
	public void on(AsyncPlayerChatEvent e)
	{
		if(e.getPlayer().equals(player))
		{
			Wraith.unregisterListener(this);
			
			new TaskLater()
			{
				@Override
				public void run()
				{
					onTextEntered(e.getMessage());
				}
			};
		}
	}
	
	public abstract void onTextEntered(String text);
}
