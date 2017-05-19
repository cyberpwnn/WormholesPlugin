package wraith;

public class Transmission extends DataCluster
{
	public Transmission(String type, String destination, String source)
	{
		super();
		
		set("t", type);
		set("d", destination);
		set("s", source);
	}
	
	public String getType()
	{
		return getString("t");
	}
	
	public String getDestination()
	{
		return getString("d");
	}
	
	public String getSource()
	{
		return getString("s");
	}
	
	public void setType(String type)
	{
		set("t", type);
	}
	
	public void setSource(String source)
	{
		set("s", source);
	}
	
	public void setDestination(String destination)
	{
		set("d", destination);
	}
}
