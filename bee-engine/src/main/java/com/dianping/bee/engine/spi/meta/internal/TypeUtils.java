/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-23
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
package com.dianping.bee.engine.spi.meta.internal;

import com.alibaba.cobar.Fields;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class TypeUtils {
	public static int convertJavaTypeToFieldType(Class<?> clazz) {
		String simpleClassName = clazz.getSimpleName();
		if ("String".equals(simpleClassName)) {
			return Fields.FIELD_TYPE_STRING;
		} else if ("int".equals(simpleClassName) || "Integer".equals(simpleClassName)) {
			return Fields.FIELD_TYPE_INT24;
		} else if ("long".equals(simpleClassName) || "Long".equals(simpleClassName)) {
			return Fields.FIELD_TYPE_LONG;
		} else if ("float".equals(simpleClassName) || "Float".equals(simpleClassName)) {
			return Fields.FIELD_TYPE_FLOAT;
		} else if ("double".equals(simpleClassName) || "Double".equals(simpleClassName)) {
			return Fields.FIELD_TYPE_DOUBLE;
		} else if ("Date".equals(simpleClassName)) {
			return Fields.FIELD_TYPE_DATE;
		} else if ("Timestamp".equals(simpleClassName)) {
			return Fields.FIELD_TYPE_TIMESTAMP;
		} else {
			return Fields.FIELD_TYPE_STRING;
		}
	}
}
