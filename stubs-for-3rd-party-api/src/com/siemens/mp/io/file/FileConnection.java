package com.siemens.mp.io.file;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.microedition.io.StreamConnection;

public interface FileConnection extends StreamConnection {

	public boolean isOpen();
	public InputStream openInputStream() throws IOException;
	public DataInputStream openDataInputStream() throws IOException;
	public OutputStream openOutputStream() throws IOException;
	public DataOutputStream openDataOutputStream() throws IOException;
	public OutputStream openOutputStream(long byteOffset) throws IOException;
	public long totalSize();
	public long availableSize();
	public long usedSize();
	public long directorySize(boolean includeSubDirs) throws IOException;
	public long fileSize() throws IOException;
	public boolean canRead();
	public boolean canWrite();
	public boolean isHidden();
	public void setReadable(boolean readable) throws IOException;
	public void setWritable(boolean writable) throws IOException;
	public void setHidden(boolean hidden) throws IOException;
	public Enumeration list() throws IOException;
	public Enumeration list(String filter, boolean includeHidden) throws IOException;
	public void create() throws IOException;
	public void mkdir() throws IOException;
	public boolean exists();
	public boolean isDirectory();
	public void delete() throws IOException;
	public void rename(java.lang.String newName) throws IOException;
	public void truncate(long byteOffset) throws IOException;
	public void setFileConnection(String fileName) throws IOException;
	public String getName();
	public String getPath();
	public String getURL();
	public long lastModified();
}
