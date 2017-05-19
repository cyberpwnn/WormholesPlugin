package wraith;

import wraith.DataCluster.ClusterType;

/**
 * 
 * @author cyberpwn
 *
 */
public class ClusterInteger extends Cluster
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClusterInteger(Integer value)
	{
		super(ClusterType.INTEGER, value.doubleValue());
	}
	
	public int get()
	{
		return value.intValue();
	}
	
	public void set(int i)
	{
		value = (double) i;
	}
}
