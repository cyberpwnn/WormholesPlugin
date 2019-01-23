package com.volmit.wormholes.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.volmit.wormholes.portal.Portal;

public class CommandScript
{
	private GList<String> commands;

	public CommandScript()
	{
		commands = new GList<String>();
	}

	public GList<String> getCommands()
	{
		return commands;
	}

	public void setCommands(GList<String> commands)
	{
		this.commands = commands;
	}

	public GList<String> parseFor(Location l, Player p, Portal o)
	{
		GList<String> c = new GList<String>();

		for(String i : commands)
		{
			String k = i;
			k = k.replace("$player", p.getName());
			k = k.replace("$portal", o.getDisplayName());
			k = k.replace("$x", l.getBlockX() + "");
			k = k.replace("$y", l.getBlockY() + "");
			k = k.replace("$z", l.getBlockZ() + "");
			c.add(k);
		}

		return c;
	}

	public void write(File f) throws IOException
	{
		JSONObject j = new JSONObject();
		JSONArray ja = new JSONArray();

		for(String i : commands)
		{
			ja.put(i);
		}

		j.put("commands", ja);
		writeJSON(j, f);
	}

	public void load(File f) throws IOException
	{
		JSONObject j = readJSON(f);
		JSONArray ar = j.getJSONArray("commands");
		commands.clear();

		for(int i = 0; i < ar.length(); i++)
		{
			commands.add(ar.getString(i));
		}
	}

	private static void writeJSON(JSONObject o, File f) throws IOException
	{
		FileWriter fw = new FileWriter(f);
		PrintWriter pw = new PrintWriter(fw);

		pw.println(o.toString(4));

		pw.close();
	}

	private static JSONObject readJSON(File man) throws IOException
	{
		FileReader fr = new FileReader(man);
		BufferedReader bu = new BufferedReader(fr);

		String line;
		String content = "";

		while((line = bu.readLine()) != null)
		{
			content += line;
		}

		bu.close();

		return new JSONObject(content);
	}
}
