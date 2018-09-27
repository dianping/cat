package com.dianping.cat.influxdb.dto;

import com.google.common.base.MoreObjects;

/**
 * Representation of the response for a influxdb ping.
 * 
 * @author stefan.majer [at] gmail.com
 * 
 */
public class Pong {
	private String version;
	private long responseTime;

	/**
	 * @return the status
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * @param version
	 *            the status to set
	 */
	public void setVersion(final String version) {
		this.version = version;
	}

	/**
	 * @return the responseTime
	 */
	public long getResponseTime() {
		return this.responseTime;
	}

	/**
	 * @param responseTime
	 *            the responseTime to set
	 */
	public void setResponseTime(final long responseTime) {
		this.responseTime = responseTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return MoreObjects
				.toStringHelper(this.getClass())
				.add("version", this.version)
				.add("responseTime", this.responseTime)
				.toString();
	}

}