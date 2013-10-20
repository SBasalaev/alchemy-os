package alchemy.apps;

import alchemy.util.Strings;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

class TerminalInputStream extends InputStream {

	private ByteArrayInputStream buf = new ByteArrayInputStream(new byte[0]);
	private String prompt = ">";
	private final BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
	private final boolean isXterm = "xterm".equals(System.getenv("TERM"));

	public TerminalInputStream() {
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public synchronized void clearScreen() {
		if (isXterm) System.out.println("\033c");
	}

	public int available() throws IOException {
		return buf.available();
	}

	public int read() throws IOException {
		int b = buf.read();
		if (b == -1) {
			if (isXterm) System.out.print("\033[32m");
			System.out.print(prompt);
			if (isXterm) System.out.print("\033[0m");
			String line = r.readLine();
			if (line != null) {
				buf = new ByteArrayInputStream(Strings.utfEncode(line + '\n'));
				b = buf.read();
			}
		}
		return b;
	}
}
