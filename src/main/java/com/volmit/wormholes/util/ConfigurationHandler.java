package com.volmit.wormholes.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * The heart of dataclusters. Most should not have to use this. It is
 * essentially a utility class for controllers and any other components of
 * Phantom to use automatically when you need it called. For example,
 * loadCluster would call parts of this when needed.
 * 
 * @author cyberpwn
 */
public class ConfigurationHandler
{
	/**
	 * Read data from config files and dataclusters and put them into the keyed
	 * config fields in the configurable class supplied
	 * 
	 * @param c
	 *            the configurable object
	 */
	public static void toFields(Configurable c)
	{
		for(Field i : c.getClass().getFields())
		{
			if(i.isAnnotationPresent(Keyed.class))
			{
				if(isValidType(i.getType()))
				{
					if(Modifier.isPublic(i.getModifiers()) && !Modifier.isStatic(i.getModifiers()))
					{
						try
						{
							String key = i.getDeclaredAnnotation(Keyed.class).value();
							Object value = c.getConfiguration().getAbstract(key);
							
							if(value instanceof List)
							{
								List<?> l = (List<?>) value;
								GList<String> k = new GList<String>();
								
								for(Object j : l)
								{
									k.add(j.toString());
								}
								
								i.set(c, k);
							}
							
							else
							{
								i.set(c, value);
							}
						}
						
						catch(IllegalArgumentException e)
						{
							e.printStackTrace();
						}
						
						catch(IllegalAccessException e)
						{
							e.printStackTrace();
						}
					}
					
					else
					{
						new DB(c.getCodeName() + "/" + i.getType().getSimpleName() + " " + i.getName()).w("INVALID MODIFIERS. MUST BE PUBLIC NON STATIC");
					}
				}
				
				else
				{
					new DB(c.getCodeName() + "/" + i.getType().getSimpleName() + " " + i.getName()).w("INVALID TYPE. NOT SUPPORTED FOR KEYED CONFIGS");;
				}
			}
		}
	}
	
	/**
	 * Write data to the cluster from the keyed fields from the configurable
	 * object
	 * 
	 * @param c
	 *            the configurable object
	 */
	public static void fromFields(Configurable c)
	{
		for(Field i : c.getClass().getFields())
		{
			if(i.isAnnotationPresent(Keyed.class))
			{
				if(isValidType(i.getType()))
				{
					if(Modifier.isPublic(i.getModifiers()) && !Modifier.isStatic(i.getModifiers()))
					{
						try
						{
							String key = i.getDeclaredAnnotation(Keyed.class).value();
							Object value = i.get(c);
							c.getConfiguration().trySet(key, value);
							
							if(i.isAnnotationPresent(Comment.class))
							{
								c.getConfiguration().comment(key, i.getDeclaredAnnotation(Comment.class).value());
							}
						}
						
						catch(IllegalArgumentException e)
						{
							e.printStackTrace();
						}
						
						catch(IllegalAccessException e)
						{
							e.printStackTrace();
						}
					}
					
					else
					{
						new DB(c.getCodeName() + "/" + i.getType().getSimpleName() + " " + i.getName()).w("INVALID MODIFIERS. MUST BE PUBLIC NON STATIC");
					}
				}
				
				else
				{
					new DB(c.getCodeName() + "/" + i.getType().getSimpleName() + " " + i.getName()).w("INVALID TYPE. NOT SUPPORTED FOR KEYED CONFIGS");;
				}
			}
		}
	}
	
	/**
	 * But is this type valid?
	 * 
	 * @param type
	 *            the class type
	 * @return true if can be saved
	 */
	public static boolean isValidType(Class<?> type)
	{
		if(type.equals(String.class))
		{
			return true;
		}
		
		else if(type.equals(Integer.class))
		{
			return true;
		}
		
		else if(type.equals(int.class))
		{
			return true;
		}
		
		else if(type.equals(Long.class))
		{
			return true;
		}
		
		else if(type.equals(long.class))
		{
			return true;
		}
		
		else if(type.equals(Double.class))
		{
			return true;
		}
		
		else if(type.equals(double.class))
		{
			return true;
		}
		
		else if(type.equals(GList.class))
		{
			return true;
		}
		
		else if(type.equals(Boolean.class))
		{
			return true;
		}
		
		else if(type.equals(boolean.class))
		{
			return true;
		}
		
		else
		{
			return false;
		}
	}
	
	/**
	 * Handle reading in configs. Also adds new paths that do not exist in the
	 * file from the onNewConfig(), and adds default values
	 * 
	 * @param base
	 *            the base directory
	 * @param c
	 *            the configurable object
	 * @throws IOException
	 *             1337
	 */
	public static void read(File base, Configurable c) throws IOException
	{
		File config = new File(base, c.getCodeName() + ".yml");
		
		if(!config.getParentFile().exists())
		{
			config.getParentFile().mkdirs();
		}
		
		if(!config.exists())
		{
			config.createNewFile();
		}
		
		if(config.isDirectory())
		{
			throw new IOException("Cannot read config (it's a folder)");
		}
		
		fromFields(c);
		c.onNewConfig();
		new YAMLDataInput().load(c.getConfiguration(), config);
		toFields(c);
		
		new TaskLater()
		{
			@Override
			public void run()
			{
				c.onReadConfig();
			}
		};
		
		new S()
		{
			@Override
			public void sync()
			{
				new A()
				{
					@Override
					public void async()
					{
						try
						{
							new YAMLDataOutput().save(c.getConfiguration(), config);
						}
						
						catch(IOException e)
						{
							e.printStackTrace();
						}
					}
				};
			}
			
		};
	}
	
	/**
	 * Compat read for standalone applications
	 * 
	 * @param base
	 *            the base file
	 * @param c
	 *            the configurable object
	 * @throws IOException
	 *             shit happens
	 */
	public static void compatRead(File base, Configurable c) throws IOException
	{
		File config = new File(base, c.getCodeName() + ".yml");
		
		if(!config.getParentFile().exists())
		{
			config.getParentFile().mkdirs();
		}
		
		if(!config.exists())
		{
			config.createNewFile();
		}
		
		if(config.isDirectory())
		{
			throw new IOException("Cannot read config (it's a folder)");
		}
		
		fromFields(c);
		c.onNewConfig();
		new YAMLDataInput().load(c.getConfiguration(), config);
		toFields(c);
		c.onReadConfig();
		
		try
		{
			new YAMLDataOutput().save(c.getConfiguration(), config);
		}
		
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Fast read json
	 * 
	 * @param base
	 *            the base file
	 * @param c
	 *            the configurable object
	 * @throws IOException
	 *             shit happens
	 */
	public static void fastRead(File base, Configurable c) throws IOException
	{
		File config = new File(base, c.getCodeName() + ".json");
		
		if(!config.getParentFile().exists())
		{
			config.getParentFile().mkdirs();
		}
		
		if(!config.exists())
		{
			config.createNewFile();
		}
		
		if(config.isDirectory())
		{
			throw new IOException("Cannot read config (it's a folder)");
		}
		
		fromFields(c);
		c.onNewConfig();
		new JSONDataInput().load(c.getConfiguration(), config);
		toFields(c);
		c.onReadConfig();
		
		new S()
		{
			@Override
			public void sync()
			{
				new A()
				{
					@Override
					public void async()
					{
						try
						{
							new JSONDataOutput().save(c.getConfiguration(), config);
						}
						
						catch(IOException e)
						{
							e.printStackTrace();
						}
					}
				};
			}
			
		};
	}
	
	/**
	 * Fast write json
	 * 
	 * @param base
	 *            the base file
	 * @param c
	 *            the configurable object
	 * @throws IOException
	 *             shit happens
	 */
	public static void fastWrite(File base, Configurable c) throws IOException
	{
		File config = new File(base, c.getCodeName() + ".json");
		
		if(!config.getParentFile().exists())
		{
			config.getParentFile().mkdirs();
		}
		
		if(!config.exists())
		{
			config.createNewFile();
		}
		
		if(config.isDirectory())
		{
			throw new IOException("Cannot save config (it's a folder)");
		}
		
		fromFields(c);
		new JSONDataOutput().save(c.getConfiguration(), config);
	}
	
	/**
	 * Handle saving configs
	 * 
	 * @param base
	 *            the base directory
	 * @param c
	 *            the configurable object
	 * @throws IOException
	 *             1337
	 */
	public static void save(File base, Configurable c) throws IOException
	{
		File config = new File(base, c.getCodeName() + ".yml");
		
		if(!config.getParentFile().exists())
		{
			config.getParentFile().mkdirs();
		}
		
		if(!config.exists())
		{
			config.createNewFile();
		}
		
		if(config.isDirectory())
		{
			throw new IOException("Cannot save config (it's a folder)");
		}
		
		fromFields(c);
		new YAMLDataOutput().save(c.getConfiguration(), config);
	}
	
	public static void savenc(File base, Configurable c) throws IOException
	{
		File config = base;
		
		if(!config.getParentFile().exists())
		{
			config.getParentFile().mkdirs();
		}
		
		if(!config.exists())
		{
			config.createNewFile();
		}
		
		if(config.isDirectory())
		{
			throw new IOException("Cannot save config (it's a folder)");
		}
		
		fromFields(c);
		new YAMLDataOutput().save(c.getConfiguration(), config);
	}
}