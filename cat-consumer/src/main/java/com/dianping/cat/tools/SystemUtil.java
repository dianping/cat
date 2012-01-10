package com.dianping.cat.tools;

public class SystemUtil {
	private static boolean IS_WINDOWS;

	static {
		String osName = System.getProperty("os.name");
		if (osName != null && osName.toLowerCase().indexOf("windows") > -1) {
			IS_WINDOWS = true;
		}
	}
	
	public static boolean isWindows(){
		return IS_WINDOWS;
	}
}
