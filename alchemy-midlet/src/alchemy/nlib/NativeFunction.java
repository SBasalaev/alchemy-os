/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alchemy.nlib;

import alchemy.core.Function;

/**
 * Skeleton for functions loaded by native libraries.
 * For speed and compactness all functions of native
 * library are implemented in single class. exec()
 * function is a switch where requested function is
 * chosen by integer index.
 * 
 * @author Sergey Basalaev
 */
public abstract class NativeFunction extends Function {
	
	/** Index of this function. */
	protected final int index;
	
	public NativeFunction(String name, int index) {
		super(name);
		this.index = index;
	}
}
