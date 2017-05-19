package wraith;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class Wraith
{
	public static ParallelPoolManager poolManager;
	public static Plugin instance = null;
	
	public static void registerListener(Listener l)
	{
		instance.getServer().getPluginManager().registerEvents(l, instance);
	}
	
	public static void callEvent(Event e)
	{
		Bukkit.getServer().getPluginManager().callEvent(e);
	}
	
	public static void unregisterListener(Listener l)
	{
		HandlerList.unregisterAll(l);
	}
	
	/**
	 * Schedule a repeating sync task
	 * 
	 * @param delay
	 *            the delay
	 * @param interval
	 *            the interval
	 * @param runnable
	 *            the runnable
	 * @return the task id
	 */
	public static int scheduleSyncRepeatingTask(int delay, int interval, Runnable runnable)
	{
		return instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, runnable, delay, interval);
	}
	
	/**
	 * Schedule a sync task
	 * 
	 * @param delay
	 *            the delay
	 * @param runnable
	 *            the runnable
	 * @return the task id
	 */
	public static int scheduleSyncTask(int delay, Runnable runnable)
	{
		return instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, runnable, delay);
	}
	
	/**
	 * Cancel a task
	 * 
	 * @param tid
	 *            the task id
	 */
	public static void cancelTask(int tid)
	{
		instance.getServer().getScheduler().cancelTask(tid);
	}
	
}
