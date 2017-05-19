package wraith;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class CustomGZIPOutputStream extends GZIPOutputStream
{
	public CustomGZIPOutputStream(OutputStream out) throws IOException
	{
		super(out);
	}
	
	public void setLevel(int level)
	{
		def.setLevel(level);
	}
}