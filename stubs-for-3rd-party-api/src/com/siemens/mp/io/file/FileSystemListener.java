package com.siemens.mp.io.file;

public interface FileSystemListener {
	public static final int ROOT_ADDED = 0;
	public static final int ROOT_REMOVED = 1;
	
	public void rootChanged(int state, String rootName);
}
