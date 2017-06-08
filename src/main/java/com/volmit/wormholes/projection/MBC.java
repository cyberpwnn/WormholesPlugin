package com.volmit.wormholes.projection;

import java.nio.ByteBuffer;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import com.volmit.wormholes.util.MaterialBlock;

public class MBC
{
	private MaterialBlock mb;
	private Vector v;
	
	public MBC(long l)
	{
		fromLong(l);
	}
	
	@Override
	public String toString()
	{
		return "MBC: " + v.toString() + " (" + mb.toString() + ") = " + toLong();
	}
	
	public MBC(MaterialBlock mb, Vector v)
	{
		this.v = v;
		this.mb = mb;
	}
	
	@SuppressWarnings("deprecation")
	public long toLong()
	{
		byte data = mb.getData();
		byte[] pos = new byte[] {(byte) v.getBlockX(), (byte) v.getBlockY(), (byte) v.getBlockZ()};
		byte[] material = ByteBuffer.allocate(4).putInt(mb.getMaterial().getId()).array();
		byte[] pack = new byte[] {pos[0], pos[1], pos[2], material[0], material[1], material[2], material[3], data};
		
		return ByteBuffer.wrap(pack).getLong();
	}
	
	@SuppressWarnings("deprecation")
	public void fromLong(long l)
	{
		byte[] pack = ByteBuffer.allocate(8).putLong(l).array();
		int materialId = ByteBuffer.wrap(new byte[] {pack[3], pack[4], pack[5], pack[6]}).getInt();
		Vector v = new Vector(pack[0], pack[1], pack[2]);
		Material m = Material.getMaterial(materialId);
		byte d = pack[7];
		MaterialBlock mb = new MaterialBlock(m, d);
		this.v = v;
		this.mb = mb;
	}
	
	public MaterialBlock getMb()
	{
		return mb;
	}
	
	public Vector getV()
	{
		return v;
	}
	
	public void setMb(MaterialBlock mb)
	{
		this.mb = mb;
	}
	
	public void setV(Vector v)
	{
		this.v = v;
	}
}