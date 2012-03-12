package com.dianping.tkv.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoKit {
	/**
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public static void copyAndClose(InputStream input, OutputStream output) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = input.read(buf)) > 0) {
			output.write(buf, 0, len);
		}
		input.close();
		output.close();
	}
}
