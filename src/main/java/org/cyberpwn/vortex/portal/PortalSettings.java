package org.cyberpwn.vortex.portal;

import org.cyberpwn.vortex.Settings;

public class PortalSettings
{
	private boolean project = Settings.ENABLE_PROJECTIONS;
	private boolean aparture = Settings.ENABLE_APERTURE;
	private String customName = "null";
	private boolean hasCustomName = false;
	private boolean allowEntities = Settings.ALLOW_ENTITIES;
	
	public PortalSettings()
	{
		
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
