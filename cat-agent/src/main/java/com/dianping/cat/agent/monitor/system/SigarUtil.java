package com.dianping.cat.agent.monitor.system;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.hyperic.sigar.Sigar;

public class SigarUtil implements Initializable {

	private Sigar m_sigar;

	public Sigar getSigar() {
		return m_sigar;
	}

	@Override
	public void initialize() throws InitializationException {
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
				m_sigar = new Sigar();
			} else {
				m_sigar = new Sigar();
			}
		} catch (IOException e) {
			throw new RuntimeException("Can't init sigar", e);
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
