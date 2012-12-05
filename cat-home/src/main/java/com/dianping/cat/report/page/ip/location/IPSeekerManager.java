package com.dianping.cat.report.page.ip.location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.unidal.helper.Files;
import org.unidal.helper.Files.AutoClose;

public class IPSeekerManager {
	private static IPSeeker s_seeker;

	public static synchronized String getLocation(String ip) {
		IPLocation loc = s_seeker.getIPLocation(ip);
		String area = loc.getArea();
		String country = loc.getCountry();

		if (area.trim().isEmpty() && country.trim().isEmpty()) {
			return "";
		} else {
			return area + "@" + country;
		}
	}

	public static void initailize(File baseDir) throws IOException {
		File file = new File(IPSeekerManager.class.getResource("qqwry.dat").getFile());

		if (baseDir != null) { // need extract gzip file
			File tmp = new File(baseDir, "qqwry.dat");

			try {
				InputStream in = IPSeekerManager.class.getResourceAsStream("qqwry.dat");

				if (in == null) {
					throw new IllegalStateException("Resource(qqwry.dat.gz) is not found in the classpath!");
				}

				tmp.getParentFile().mkdirs();
				GZIPInputStream gis = new GZIPInputStream(in);
				FileOutputStream fos = new FileOutputStream(tmp);

				Files.forIO().copy(gis, fos, AutoClose.INPUT_OUTPUT);
				file = tmp;
			} catch (IOException e) { // not gzip file

			}
		}

		s_seeker = new IPSeeker(file.getAbsolutePath(), null);
	}
}
