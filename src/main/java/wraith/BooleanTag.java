package wraith;

/**
 * The <code>TAG_Boolean</code> tag.
 * 
 * @author cyberpwn
 */
public final class BooleanTag extends Tag
{
	
	/**
	 * The value.
	 */
	private final boolean value;
	
	/**
	 * Creates the tag.
	 * 
	 * @param name
	 *            The name.
	 * @param value
	 *            The value.
	 */
	public BooleanTag(String name, boolean value)
	{
		super(name);
		this.value = value;
	}
	
	@Override
	public Boolean getValue()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		String name = getName();
		String append = "";
		if(name != null && !name.equals(""))
		{
			append = "(\"" + this.getName() + "\")";
		}
		return "TAG_Boolean" + append + ": " + value;
	}
	
}
