package com.dianping.cat.report.tool;

import java.text.SimpleDateFormat;

public class DateUtil {
	public static final long SECOND = 1000L;

	public static final long MINUTE = SECOND * 60;

	public static final long HOUR = MINUTE * 60;

	public static final SimpleDateFormat SDF_URL = new SimpleDateFormat("yyyyMMddHHmm");

	public static final SimpleDateFormat SDF_SEG = new SimpleDateFormat("yyyy-MM-dd HH:mm");

}
