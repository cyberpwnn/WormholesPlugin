package org.cyberpwn.vortex.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import wraith.DataCluster;
import wraith.JSONObject;

public class IOService
{
	public IOService()
	{
		
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
