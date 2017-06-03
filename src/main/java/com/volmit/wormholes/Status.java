package com.volmit.wormholes;

import wraith.Average;
import wraith.C;
import wraith.F;

public class Status
{
	public static int packetBytesPerSecond = 0;
	public static double projectionTime = 0;
	public static int permutationsPerSecond = 0;
	public static int pps;
	public static boolean fdq;
	public static Average avgBPS = new Average(2);
	
	public static String inf = "";
	
	public static void sample()
	{
		inf = "";
		inf += C.LIGHT_PURPLE + "NET: " + C.WHITE + F.fileSize((long) avgBPS.getAverage()) + "/s ";
		inf += C.LIGHT_PURPLE + "PRJ: " + C.WHITE + F.f(projectionTime, 0) + "ms (" + F.f(pps) + " permute)";
	}
}
