/**
 * Project: whale-engine
 * 
 * File Created at 2012-8-17
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.engine.spi.impl;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class User {

	private Integer Id;

	private String name;

	private String address;

	private Integer departmentId;

	public String getAddress() {
		return address;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public Integer getId() {
		return Id;
	}

	public String getName() {
		return name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public void setId(Integer id) {
		Id = id;
	}

	public void setName(String name) {
		this.name = name;
	}
}
