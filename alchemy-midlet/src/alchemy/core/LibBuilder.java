package alchemy.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * Library builder.
 * 
 * @author Sergey Basalaev
 */
public interface LibBuilder {

	/**
	 * Builds library from the given input stream.
	 * Be aware that the first two bytes are already read
	 * (FIXME see LibraryLoadingMechanism).
	 *
	 * @param c   context in which this library is loaded
	 * @param in  input stream
	 */
	Library build(Context c, InputStream in) throws IOException, InstantiationException;
}
