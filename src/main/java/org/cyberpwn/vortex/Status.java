package org.cyberpwn.vortex;

import wraith.Average;

public class Status
{
	public static int packetBytesPerSecond = 0;
	public static int bungeeBytesPerSecond = 0;
	public static double rasterCompression = 0;
	public static double packetCompression = 0;
	public static double bungeeCompression = 0;
	public static double projectionTime = 0;
	public static Average avgBPS = new Average(40);
}
