package wraith;

import wraith.DataCluster.ClusterType;

/**
 * 
 * @author cyberpwn
 *
 */
public class ClusterString extends Cluster
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String string;
	
	public ClusterString(String value)
	{
		super(ClusterType.STRING, 0.0);
		this.string = value;
	}
	
	public String get()
	{
		return string;
	}
	
	public void set(String s)
	{
		value = 0.0;
		string = s;
	}
}
