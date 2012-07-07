/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alchemy.nlib;

import alchemy.core.Function;
import alchemy.core.Library;
import alchemy.util.UTFReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Native library.
 * Native function signatures and indices are determined
 * from the resource file which contains function names
 * in implementation order.
 * 
 * @author Sergey Basalaev
 */
public abstract class NativeLibrary extends Library {

	/** Maps function name to function object. */
	private Hashtable functions = new Hashtable();
	
	/** Creates new library using specified symbols file. */
	public NativeLibrary(String symbols) throws IOException {
		UTFReader r = new UTFReader(getClass().getResourceAsStream(symbols));
		String name;
		int index = 0;
		while ((name = r.readLine()) != null) {
			functions.put(name, loadFunction(name, index));
			index++;
		}
		r.close();
	}
	
	/** Returns native function of appropriate class. */
	public abstract NativeFunction loadFunction(String name, int index);

	public Function getFunction(String sig) {
		return (Function)functions.get(sig);
	}
}
