package com.volmit.wormholes.util;

public class Timed
{
	private long time;
	private String id;
	private GList<Timed> timers;
	
	public Timed(String id, long time)
	{
		this.id = id;
		this.time = time;
		timers = new GList<Timed>();
	}
	
	public long getTime()
	{
		return time;
	}
	
	public void setTime(long time)
	{
		this.time = time;
	}
	
	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public GList<Timed> getTimers()
	{
		return timers;
	}
	
	public void setTimers(GList<Timed> timers)
	{
		this.timers = timers;
	}
	
	public double getTimeMS()
	{
		return (double) getTime() / 1000000.0;
	}
	
	public double getTotalTimeMS()
	{
		return (double) getTotalTime() / 1000000.0;
	}
	
	public long getTotalTime()
	{
		long f = time;
		
		for(Timed i : timers)
		{
			f += i.getTotalTime();
		}
		
		return f;
	}
	
	public GList<String> toLines()
	{
		return toLines(0, 1);
	}
	
	public GList<String> toLines(int startIndent, int indentShift)
	{
		return toLines(startIndent, indentShift, new GList<String>());
	}
	
	public GList<String> toLines(int startIndent, int indentShift, GList<String> cache)
	{
		GList<String> l = cache.copy();
		String ind = TXT.repeat(" ", startIndent);
		String r = ind + C.GOLD + getId() + " " + C.AQUA + F.f(getTotalTimeMS(), 4) + "ms" + C.YELLOW + " (" + F.f(getTimeMS(), 4) + "ms)";
		l.add(r);
		
		for(Timed i : getTimers())
		{
			l = i.toLines(startIndent + indentShift, indentShift, l);
		}
		
		return l;
	}
	
	public Timed get(String id)
	{
		for(Timed i : getTimers())
		{
			if(i.getId().equals(id))
			{
				return i;
			}
		}
		
		hit(id, 0);
		return get(id);
	}
	
	public void hit(String id, long time)
	{
		hit(new Timed(id, time));
	}
	
	public void hit(Timed t)
	{
		for(Timed i : getTimers().copy())
		{
			if(t.getId().equals(i.getId()))
			{
				i.setTime((i.getTime() + i.getTime() + t.getTime()) / 3);
				return;
			}
		}
		
		getTimers().add(t);
	}
}
