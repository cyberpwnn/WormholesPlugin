package com.volmit.wormholes;

import com.volmit.wormholes.util.Average;
import com.volmit.wormholes.util.C;
import com.volmit.wormholes.util.F;

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
	
	public static String inf = "";
	public static int lightFault = 0;
	public static int lightFaulted = 0;
	
	public static void sample()
	{
		avgPower.put(WAPI.getPowerPoolInfo().getUtilization());
		avgWrk.put(WAPI.getWorkerPoolInfo().getUtilization());
		inf = "";
		inf += C.GOLD + "POW: " + C.WHITE + F.pc(avgPower.getAverage()) + " ";
		inf += C.GOLD + "WRK: " + C.WHITE + F.pc(avgWrk.getAverage()) + " ";
		inf += C.GOLD + "NET: " + C.WHITE + F.fileSize((long) avgBPS.getAverage()) + "/s ";
		inf += C.GOLD + "PRJ: " + C.WHITE + F.f(projectionTime, 0) + "ms";
	}
}
