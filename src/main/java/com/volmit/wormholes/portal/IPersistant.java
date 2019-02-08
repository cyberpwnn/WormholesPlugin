package com.volmit.wormholes.portal;

import java.io.IOException;

public interface IPersistant
{
	public void save();

	public boolean needsSaving();

	public void saveNow() throws IOException;
}
