package com.volmit.wormholes.portal;

import com.volmit.wormholes.Settings;

public class PortalSettings
{
	private boolean project = Settings.ENABLE_PROJECTIONS;
	private boolean aparture = Settings.ENABLE_APERTURE;
	private String customName = "null";
	private boolean hasCustomName = false;
	private boolean allowEntities = Settings.ALLOW_ENTITIES;
	private boolean randomTp = false;
	private int rtpDist = Settings.RTP_DEFAULT_MAX_DISTANCE;
	private int rtpMinDist = Settings.RTP_DEFAULT_MIN_DISTANCE;
	private String rtpBiome = "ALL_BIOMES";
	private boolean rtpRefresh = false;
	
	public boolean isRtpRefresh()
	{
		return rtpRefresh;
	}
	
	public void setRtpRefresh(boolean rtpRefresh)
	{
		this.rtpRefresh = rtpRefresh;
	}
	
	public void setRtpBiome(String rtpBiome)
	{
		this.rtpBiome = rtpBiome;
	}
	
	public String getRtpBiome()
	{
		return rtpBiome;
	}
	
	public PortalSettings()
	{
		
	}
	
	public int getRtpMinDist()
	{
		return rtpMinDist;
	}
	
	public void setRtpMinDist(int rtpMinDist)
	{
		this.rtpMinDist = rtpMinDist;
	}
	
	public boolean isRandomTp()
	{
		return randomTp;
	}
	
	public void setRandomTp(boolean randomTp)
	{
		this.randomTp = randomTp;
	}
	
	public int getRtpDist()
	{
		return rtpDist;
	}
	
	public void setRtpDist(int rtpDist)
	{
		this.rtpDist = rtpDist;
	}
	
	public boolean isProject()
	{
		return project;
	}
	
	public boolean isAparture()
	{
		return aparture;
	}
	
	public String getCustomName()
	{
		return customName;
	}
	
	public boolean isHasCustomName()
	{
		return hasCustomName;
	}
	
	public boolean isAllowEntities()
	{
		return allowEntities;
	}
	
	public void setProject(boolean project)
	{
		this.project = project;
	}
	
	public void setAparture(boolean aparture)
	{
		this.aparture = aparture;
	}
	
	public void setCustomName(String customName)
	{
		this.customName = customName;
	}
	
	public void setHasCustomName(boolean hasCustomName)
	{
		this.hasCustomName = hasCustomName;
	}
	
	public void setAllowEntities(boolean allowEntities)
	{
		this.allowEntities = allowEntities;
	}
}
