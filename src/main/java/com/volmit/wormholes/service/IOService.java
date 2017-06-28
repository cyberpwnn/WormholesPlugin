package com.volmit.wormholes.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.FileUtils;
import com.volmit.wormholes.Settings;
import com.volmit.wormholes.Wormholes;
import com.volmit.wormholes.util.DataCluster;
import com.volmit.wormholes.util.JSONObject;
import com.volmit.wormholes.util.SkinProperties;
import com.volmit.wormholes.util.YAMLDataInput;
import com.volmit.wormholes.util.YAMLDataOutput;

public class IOService
{
	public IOService()
	{
		doConfig();
	}
	
	public boolean hasSkin(UUID id)
	{
		File f = new File(Wormholes.instance.getDataFolder(), "cache");
		File fx = new File(f, "skins");
		File fz = new File(fx, id.toString() + ".usw");
		fx.mkdirs();
		
		return fz.exists();
	}
	
	public SkinProperties loadSkin(UUID id)
	{
		File f = new File(Wormholes.instance.getDataFolder(), "cache");
		File fx = new File(f, "skins");
		File fz = new File(fx, id.toString() + ".usw");
		fx.mkdirs();
		
		if(fz.exists())
		{
			try
			{
				FileInputStream fin = new FileInputStream(fz);
				GZIPInputStream gzi = new GZIPInputStream(fin);
				DataInputStream dis = new DataInputStream(gzi);
				String uvd = dis.readUTF();
				String val = dis.readUTF();
				String sig = dis.readUTF();
				dis.close();
				
				return new SkinProperties(uvd, val, sig);
			}
			
			catch(IOException e)
			{
				
			}
		}
		
		return null;
	}
	
	public void saveSkin(UUID id, SkinProperties s)
	{
		File f = new File(Wormholes.instance.getDataFolder(), "cache");
		File fx = new File(f, "skins");
		File fz = new File(fx, id.toString() + ".usw");
		fx.mkdirs();
		
		try
		{
			FileOutputStream fos = new FileOutputStream(fz, false);
			GZIPOutputStream gzi = new GZIPOutputStream(fos);
			DataOutputStream dos = new DataOutputStream(gzi);
			dos.writeUTF(s.getUvd());
			dos.writeUTF(s.getValue());
			dos.writeUTF(s.getSignature());
			dos.close();
		}
		
		catch(IOException e)
		{
			
		}
	}
	
	public void doConfig()
	{
		try
		{
			Wormholes.instance.getDataFolder().mkdirs();
			doConfigBasic();
			doConfigExperimental();
			Settings.chkConfig();
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
