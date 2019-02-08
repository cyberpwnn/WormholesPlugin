package com.volmit.wormholes.portal;

import com.volmit.wormholes.util.JSONObject;

public interface IWritable
{
	public void loadJSON(JSONObject j);

	public void saveJSON(JSONObject j);

	public JSONObject toJSON();
}
