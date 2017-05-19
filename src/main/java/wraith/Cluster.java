package wraith;

import java.io.Serializable;
import wraith.DataCluster.ClusterType;

/**
 * 
 * @author cyberpwn
 *
 */
public class Cluster implements Serializable
{
	private static final long serialVersionUID = 1L;
	protected final ClusterType type;
	protected Double value;
	
	public Cluster(ClusterType type, Double value)
	{
		this.type = type;
		this.value = value;
	}

	public ClusterType getType()
	{
		return type;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof Cluster)
		{
			return ((Cluster)o).getType().equals(getType());
		}
		
		return false;
	}
}
