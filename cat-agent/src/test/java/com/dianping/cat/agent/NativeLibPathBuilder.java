package com.dianping.cat.agent;

import java.io.File;

public class NativeLibPathBuilder {

	public static void build() {
		try {
			String root = System.getProperty("user.dir");
			File libDir = new File(root + "/lib");

			if (libDir.exists() && libDir.isDirectory()) {
				String path = System.getProperty("java.library.path");

				if (OsCheck.getOperatingSystemType() == OsCheck.OSType.WINDOWS) {
					path += ";" + libDir.getCanonicalPath();
				} else {
					path += ":" + libDir.getCanonicalPath();
				}
				System.setProperty("java.library.path", path);
			} else {
				throw new RuntimeException("There exists no sigar native library");
			}
		} catch (Exception e) {
			throw new RuntimeException("Can't set java.library.path for sigar", e);
		}
	}

	public static final class OsCheck {

		public enum OSType {
			WINDOWS, MACOS, LINUX, OTHER
		}

		protected static OSType m_detectedOS;

		public static OSType getOperatingSystemType() {
			if (m_detectedOS == null) {
				String OS = System.getProperty("os.name", "generic").toLowerCase();

				if (OS.indexOf("win") >= 0) {
					m_detectedOS = OSType.WINDOWS;
				} else if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
					m_detectedOS = OSType.MACOS;
				} else if (OS.indexOf("nux") >= 0) {
					m_detectedOS = OSType.LINUX;
				} else {
					m_detectedOS = OSType.OTHER;
				}
			}
			return m_detectedOS;
		}
	}

}
