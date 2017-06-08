package com.volmit.wormholes.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.util.DataCluster;
import com.volmit.wormholes.util.JSONObject;
import com.volmit.wormholes.util.YAMLDataInput;
import com.volmit.wormholes.util.YAMLDataOutput;

public class IOService
{
	public IOService()
	{
		doConfig();
	}
	
	public void doConfig()
	{
		try
		{
			doConfigBasic();
			doConfigExperimental();
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void doConfigBasic() throws IOException
	{
		File f = new File(Wormholes.instance.getDataFolder(), "config.yml");
		DataCluster def = Settings.getConfig();
		DataCluster lod = new DataCluster();
		
		if(!f.exists())
		{
			f.createNewFile();
		}
		
		new YAMLDataInput().load(lod, f);
		
		for(String i : def.keys())
		{
			if(lod.contains(i))
			{
				def.trySet(i, lod.getAbstract(i), def.getComment(i));
			}
		}
		
		new YAMLDataOutput().save(def, f);
		Settings.setConfig(def);
	}
	
	public void doConfigExperimental() throws IOException
	{
		File f = new File(Wormholes.instance.getDataFolder(), "config-experimental.yml");
		DataCluster def = Settings.getExperimentalConfig();
		DataCluster lod = new DataCluster();
		
		if(!f.exists())
		{
			f.createNewFile();
		}
		
		new YAMLDataInput().load(lod, f);
		
		for(String i : def.keys())
		{
			if(lod.contains(i))
			{
				def.trySet(i, lod.getAbstract(i), def.getComment(i));
			}
		}
		
		new YAMLDataOutput().save(def, f);
		Settings.setExperimentalConfig(def);
	}
	
	public DataCluster load(File f)
	{
		try
		{
			return new DataCluster(new JSONObject(new String(FileUtils.readFileToByteArray(f), StandardCharsets.UTF_8)));
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void save(DataCluster cc, File f)
	{
		try
		{
			FileUtils.writeByteArrayToFile(f, cc.toJSON().toString().getBytes(StandardCharsets.UTF_8));
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
