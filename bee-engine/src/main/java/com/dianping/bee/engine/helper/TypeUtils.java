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
package com.dianping.bee.engine.helper;

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

	public static String convertFieldTypeToString(int field) {
		switch (field) {
		case Fields.FIELD_TYPE_STRING:
			return "CHAR";
		case Fields.FIELD_TYPE_INT24:
			return "INTEGER";
		case Fields.FIELD_TYPE_DECIMAL:
			return "DECIMAL";
		case Fields.FIELD_TYPE_TINY:
			return "TINYINT";
		case Fields.FIELD_TYPE_SHORT:
			return "SMALLINT";
		case Fields.FIELD_TYPE_LONG:
			return "BIGINT";
		case Fields.FIELD_TYPE_FLOAT:
			return "FLOAT";
		case Fields.FIELD_TYPE_DOUBLE:
			return "DOUBLE";
		case Fields.FIELD_TYPE_NULL:
			return "NULL";
		case Fields.FIELD_TYPE_TIMESTAMP:
			return "TIMESTAMP";
		case Fields.FIELD_TYPE_LONGLONG:
			return "BIGINT";
		case Fields.FIELD_TYPE_DATE:
			return "DATE";
		case Fields.FIELD_TYPE_TIME:
			return "TIME";
		case Fields.FIELD_TYPE_DATETIME:
			return "DATETIME";
		case Fields.FIELD_TYPE_YEAR:
			return "YEAR";
		case Fields.FIELD_TYPE_NEWDATE:
			return "NEWDATE";
		case Fields.FIELD_TYPE_VARCHAR:
			return "VARCHAR";
		case Fields.FIELD_TYPE_BIT:
			return "BIT";
		case Fields.FIELD_TYPE_NEW_DECIMAL:
			return "NEWDECIMAL";
		case Fields.FIELD_TYPE_ENUM:
			return "ENUM";
		case Fields.FIELD_TYPE_SET:
			return "SET";
		case Fields.FIELD_TYPE_TINY_BLOB:
			return "TINYBLOB";
		case Fields.FIELD_TYPE_MEDIUM_BLOB:
			return "MEDIUMBLOB";
		case Fields.FIELD_TYPE_LONG_BLOB:
			return "LONGBLOB";
		case Fields.FIELD_TYPE_BLOB:
			return "BLOBL";
		case Fields.FIELD_TYPE_VAR_STRING:
			return "VARCHAR";
		case Fields.FIELD_TYPE_GEOMETRY:
			return "GEOMETRY";
		default:
			return "UNKNOWN";
		}
	}
}
