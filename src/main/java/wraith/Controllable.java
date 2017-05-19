package wraith;

import org.bukkit.event.Listener;

public interface Controllable extends Listener
{
	public void tick();
	
	public void start();
	
	public void stop();
	
	public Controllable getParent();
	
	public GList<Controllable> getChildren();
	
	public String getName();
	
	public boolean isRoot();
	
	public boolean isActive();
	
	public void register(Controllable controllable);
	
	public boolean isTicked();
	
	public double getTickRate();
	
	public TickHandler getTickHandler();
}
