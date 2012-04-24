/**
 * 
 */
package com.dianping.cat.consumer.ip.location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author sean.wang
 * @since Apr 23, 2012
 */
public class IPSeekerFactory {

	private static IPSeeker seeker;

	private static final File datFile = new File(System.getProperty("java.io.tmpdir") + "/qqwry.dat");

	static {
		if (!datFile.exists()) { // if dat deleted
			seeker = null;
		}
		if (seeker == null) {
			try {
				extract();
			} catch (IOException e) {
				e.printStackTrace();
			}
			seeker = new IPSeeker(datFile.getAbsolutePath(), null);
		}
	}

	public static String getLocation(String ip) {
		IPLocation loc = seeker.getIPLocation(ip);
		return loc.getCountry() + " " + loc.getArea();
	}

	private static void extract() throws IOException {
		InputStream input = IPSeekerFactory.class.getResourceAsStream("qqwry.dat.gz");
		GZIPInputStream ginstream = null;
		FileOutputStream outstream = null;
		try {
			ginstream = new GZIPInputStream(input);
			outstream = new FileOutputStream(datFile);
			byte[] buf = new byte[1024];
			int len;
			while ((len = ginstream.read(buf)) > 0) {
				outstream.write(buf, 0, len);
			}
		} catch (Exception e) {
			if (ginstream != null) {
				ginstream.close();
			}
			if (outstream != null) {
				outstream.close();
			}
		}
	}

	public static void destroy() {
		seeker = null;
		datFile.delete();
	}

	public static IPSeeker getInstance() {
		return seeker;
	}

}
