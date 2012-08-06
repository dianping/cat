package com.dianping.cat.report.page.ip.location;

/**
 * <pre>
 * 一条IP范围记录，不仅包括国家和区域，也包括起始IP和结束IP
 * </pre>
 */
public class IPEntry {
	public String area;

	public String beginIp;

	public String country;

	public String endIp;

	/**
	 * 构造函数
	 */
	public IPEntry() {
		beginIp = endIp = country = area = "";
	}
}
