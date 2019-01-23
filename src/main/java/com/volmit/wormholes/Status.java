package com.volmit.wormholes;

import com.volmit.wormholes.util.Average;

public class Status
{
	public static int packetBytesPerSecond = 0;
	public static double projectionTime = 0;
	public static int permutationsPerSecond = 0;
	public static int pps;
	public static boolean fdq;
	public static Average avgBPS = new Average(2);
	public static Average avgPower = new Average(5);
	public static Average avgWrk = new Average(5);
	public static Average avgBGY = new Average(5);
	public static long bgg = 0;
	public static String inf = "";
	public static int lightFault = 0;
	public static int lightFaulted = 0;

	public static void sample()
	{
		inf = "hmmm....";
	}

	public static void reportPacket(long size)
	{
		bgg += size;
	}
}
