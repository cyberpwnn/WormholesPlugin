package com.volmit.wormholes.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;

public class SkinProperties
{
	private String uvd;
	private String value;
	private String signature;
	
	public SkinProperties(UUID uuid) throws SkinErrorException
	{
		uvd = uuid.toString().replaceAll("-", "");
		
		JSONObject j = get();
		
		if(j.has("error"))
		{
			throw new SkinErrorException();
		}
		
		if(j.has("properties"))
		{
			JSONArray ja = j.getJSONArray("properties");
			JSONObject properties = ja.getJSONObject(0);
			value = properties.getString("value");
			signature = properties.getString("signature");
		}
	}
	
	private JSONObject get()
	{
		URL url;
		String k = "";
		
		try
		{
			url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uvd + "?unsigned=false");
			System.out.println("USING URL: " + url.toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			
			while((line = reader.readLine()) != null)
			{
				System.out.println(line);
				k += line;
			}
			
			reader.close();
		}
		
		catch(Exception e)
		{
			
		}
		
		return new JSONObject(k);
	}
	
	public WrappedSignedProperty sign()
	{
		return new WrappedSignedProperty("textures", value, signature);
	}
}
