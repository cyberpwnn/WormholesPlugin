package com.volmit.wormholes.chunk;

import org.apache.commons.lang3.Validate;

import net.minecraft.server.v1_8_R3.MathHelper;

public class DataBits18
{
	private final long[] a;
	private final int b;
	private final long c;
	private final int d;

	public DataBits18(int arg0, int arg1)
	{
		Validate.inclusiveBetween(1L, 32L, (long) arg0);
		this.d = arg1;
		this.b = arg0;
		this.c = (1L << arg0) - 1L;
		this.a = new long[MathHelper.c(arg1 * arg0, 64) / 64];
	}

	public void a(int arg0, int arg1)
	{
		Validate.inclusiveBetween(0L, (long) (this.d - 1), (long) arg0);
		Validate.inclusiveBetween(0L, this.c, (long) arg1);
		int arg2 = arg0 * this.b;
		int arg3 = arg2 / 64;
		int arg4 = ((arg0 + 1) * this.b - 1) / 64;
		int arg5 = arg2 % 64;
		this.a[arg3] = this.a[arg3] & ~(this.c << arg5) | ((long) arg1 & this.c) << arg5;
		if(arg3 != arg4)
		{
			int arg6 = 64 - arg5;
			int arg7 = this.b - arg6;
			this.a[arg4] = this.a[arg4] >>> arg7 << arg7 | ((long) arg1 & this.c) >> arg6;
		}

	}

	public int a(int arg0)
	{
		Validate.inclusiveBetween(0L, (long) (this.d - 1), (long) arg0);
		int arg1 = arg0 * this.b;
		int arg2 = arg1 / 64;
		int arg3 = ((arg0 + 1) * this.b - 1) / 64;
		int arg4 = arg1 % 64;
		if(arg2 == arg3)
		{
			return (int) (this.a[arg2] >>> arg4 & this.c);
		}
		else
		{
			int arg5 = 64 - arg4;
			return (int) ((this.a[arg2] >>> arg4 | this.a[arg3] << arg5) & this.c);
		}
	}

	public long[] a()
	{
		return this.a;
	}

	public int b()
	{
		return this.d;
	}
}