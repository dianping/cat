package com.dianping.cat.notify.model;

public class Subscriber {
	private int id;

	private String domain;

	private String address;

	private int type;

	private String group;

	private String owner;

	public static int MAIL = 0;

	public static int SMS = 1;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getGroup() {
   	return group;
   }

	public void setGroup(String group) {
   	this.group = group;
   }

	public String getOwner() {
   	return owner;
   }

	public void setOwner(String owner) {
   	this.owner = owner;
   }

}
