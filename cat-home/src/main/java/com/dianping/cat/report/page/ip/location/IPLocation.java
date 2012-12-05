package com.dianping.cat.report.page.ip.location;

/**
 * 
 * @category 用来封装ip相关信息，目前只有两个字段，ip所在的国家和地区
 */
public class IPLocation {
	private String area;

	private String country;

	public IPLocation() {
		country = area = "";
	}

	public String getArea() {
		return area;
	}

	public IPLocation getCopy() {
		IPLocation ret = new IPLocation();
		ret.country = country;
		ret.area = area;
		return ret;
	}

	public String getCountry() {
		return country;
	}

	public void setArea(String area) {
		// 如果为局域网，纯真IP地址库的地区会显示CZ88.NET,这里把它去掉
		if (area.trim().equals("CZ88.NET")) {
			this.area = "本机或本网络";
		} else {
			this.area = area;
		}
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
