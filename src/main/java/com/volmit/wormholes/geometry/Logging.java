/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package com.volmit.wormholes.geometry;

/**
 * This class of static methods provides the interface to logging for World Wind components. Logging is performed via
 * {@link java.util.logging}. The default logger name is <code>gov.nasa.worldwind</code>. The logger name is
 * configurable via {@link gov.nasa.worldwind.Configuration}.
 *
 * @author tag
 * @version $Id$
 * @see gov.nasa.worldwind.Configuration
 * @see java.util.logging
 */
public class Logging
{
	private static Logging l;

	public Logging()
	{
		l = this;
	}

	public static String getMessage(String property)
	{
		return property;
	}

	public static String getMessage(String property, String arg)
	{
		return getMessage(property + " with " + arg);
	}

	/**
	 * Retrieves a message from the World Wind message resource bundle formatted with specified arguments. The arguments
	 * are inserted into the message via {@link java.text.MessageFormat}.
	 *
	 * @param property the property identifying which message to retrieve.
	 * @param args     the arguments referenced by the format string identified <code>property</code>.
	 *
	 * @return The requested string formatted with the arguments.
	 *
	 * @see java.text.MessageFormat
	 */
	public static String getMessage(String property, Object... args)
	{
		return getMessage(property + " with " + String.valueOf(args));
	}

	public static Logging logger()
	{
		return l == null ? new Logging() : l;
	}

	public void severe(String message)
	{
		System.out.println(message);
	}

	public void fine(String message)
	{
		System.out.println(message);
	}
}
