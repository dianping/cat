package com.dianping.cat.influxdb;

import com.dianping.cat.influxdb.impl.InfluxDBImpl;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class InfluxDBFactory {

	public static InfluxDB connect(final String url, final String username, final String password) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(url), "The URL may not be null or empty.");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "The username may not be null or empty.");
		return new InfluxDBImpl(url, username, password);
	}

}
