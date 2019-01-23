package com.volmit.wormholes.util;

import com.volmit.wormholes.Wormholes;

public class TextMap
{
	private String name;
	private GMap<Character, TextBlock> mapping;
	private int height;
	
	public TextMap(String name)
	{
		height = -1;
		this.name = name;
		mapping = new GMap<Character, TextBlock>();
	}
	
	public void addBlock(Character c, TextBlock block)
	{
		mapping.put(c, block);
	}
	
	public String getName()
	{
		return name;
	}
	
	public GMap<Character, TextBlock> getMapping()
	{
		return mapping;
	}
	
	public void check()
	{
		DB d = Wormholes.instance.getDispatcher();
		
		d.s("Scanning " + mapping.k().size() + " Blocks...");
		
		int len = -1;
		int hei = -1;
		
		for(Character i : mapping.k())
		{
			TextBlock b = mapping.get(i);
			
			if(len == -1)
			{
				len = b.getBlock().get(0).length();
				hei = b.getBlock().size();
				d.w("Forcing Constraints: L:" + len + " H:" + hei);
			}
			
			else
			{
				int line = 1;
				
				for(String j : b.getBlock())
				{
					if(b.getBlock().get(0).length() != j.length())
					{
						d.f("Char '" + i + "' Line " + line + " Does not conform to length " + b.getBlock().get(0).length() + " (" + j.length() + ")");
					}
					
					line++;
				}
				
				if(hei != b.getBlock().size())
				{
					d.f("Char '" + i + "' Does not match height " + hei + " (" + b.getBlock().size() + ")");
				}
			}
		}
		
		height = hei;
	}
	
	public String[] build(String s)
	{
		if(height < 0)
		{
			return null;
		}
		
		GList<String> strings = new GList<String>();
		
		for(int i = 0; i < height; i++)
		{
			String current = "";
			
			for(Character j : s.toCharArray())
			{
				current += build(j)[i];
			}
			
			strings.add(current);
		}
		
		return strings.toArray(new String[strings.size()]);
	}
	
	public String[] build(String s, C... c)
	{
		if(height < 0)
		{
			return null;
		}
		
		CNum n = new CNum(c.length);
		GList<String> strings = new GList<String>();
		
		for(int i = 0; i < height; i++)
		{
			String current = "";
			n.set(0);
			
			for(Character j : s.toCharArray())
			{
				n.add(1);
				current += build(j, c[n.get()])[i];
			}
			
			strings.add(current);
		}
		
		return strings.toArray(new String[strings.size()]);
	}
	
	public String[] build(char c, C color)
	{
		if(!getMapping().containsKey(c))
		{
			return build('?', color);
		}
		
		GList<String> lines = new GList<String>();
		
		for(String i : getMapping().get(c).getBlock())
		{
			lines.add(color + i);
		}
		
		return lines.toArray(new String[lines.size()]);
	}
	
	public String[] build(char c)
	{
		if(!getMapping().containsKey(c))
		{
			return build('?');
		}
		
		GList<String> lines = new GList<String>();
		
		for(String i : getMapping().get(c).getBlock())
		{
			lines.add(i);
		}
		
		return lines.toArray(new String[lines.size()]);
	}
}
