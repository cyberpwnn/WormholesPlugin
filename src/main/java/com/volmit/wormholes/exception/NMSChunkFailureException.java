package com.volmit.wormholes.exception;

public class NMSChunkFailureException extends Exception 
{
	private static final long serialVersionUID = 1L;

	public NMSChunkFailureException(String message, Throwable cause) 
	{
		super(message, cause);
	}
}
