/**
 * Project: whale-engine
 * 
 * File Created at 2012-8-14
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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;

import com.alibaba.cobar.Fields;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.recognizer.SQLParserDelegate;
import com.dianping.bee.engine.visitor.DefaultVisitor;
import com.dianping.whale.engine.IQueryInterface;
import com.dianping.whale.storage.RowSet;
import com.dianping.whale.storage.RowSetMetaData;
import com.dianping.whale.storage.model.Department;
import com.dianping.whale.storage.model.User;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SampleDB1 implements IQueryInterface {

	private static final SampleDB1 INSTANCE = new SampleDB1();

	private static final int SAMPLE_DATA_BASE_SIZE = 10;

	private static Map<String, Object[][]> tableData;

	private static Map<String, String[]> columnMeta;

	private static final String TABLE_USER_NAME = "USER";

	private static final String TABLE_DEPARTMENT_NAME = "DEPARTMENT";

	static {
		loadSampleData();
	}

	public static SampleDB1 getInstance() {
		return INSTANCE;
	}

	private static void loadSampleData() {
		tableData = new HashMap<String, Object[][]>();
		tableData.put(TABLE_USER_NAME, new Object[SAMPLE_DATA_BASE_SIZE * 5][]);
		tableData.put(TABLE_DEPARTMENT_NAME, new Object[SAMPLE_DATA_BASE_SIZE][]);

		columnMeta = new HashMap<String, String[]>();
		columnMeta.put(TABLE_USER_NAME, new String[] { "ID", "NAME", "ADDRESS", "DEPARTMENTID" });
		for (int i = 0; i < SAMPLE_DATA_BASE_SIZE * 5; i++) {
			User user = new User();
			user.setId(i + 1);
			user.setName(RandomStringUtils.randomAlphabetic(5));
			user.setAddress(RandomStringUtils.randomAlphabetic(10));
			user.setDepartmentId(i % SAMPLE_DATA_BASE_SIZE + 1);

			Object[] row = new Object[4];
			row[0] = user.getId();
			row[1] = user.getName();
			row[2] = user.getAddress();
			row[3] = user.getDepartmentId();
			tableData.get(TABLE_USER_NAME)[i] = row;
		}

		columnMeta.put(TABLE_DEPARTMENT_NAME, new String[] { "ID", "NAME" });
		for (int i = 0; i < SAMPLE_DATA_BASE_SIZE; i++) {
			Department dept = new Department();
			dept.setId(i + 1);
			dept.setName(RandomStringUtils.randomAlphabetic(3));

			Object[] row = new Object[2];
			row[0] = dept.getId();
			row[1] = dept.getName();
			tableData.get(TABLE_DEPARTMENT_NAME)[i] = row;
		}
	}

	public RowSet query(String sql) throws SQLException {
		SQLStatement statement = null;
		statement = SQLParserDelegate.parse(sql);

		DefaultVisitor visitor = new DefaultVisitor();
		statement.accept(visitor);
		Map<String, Identifier> tables = visitor.getTables();
		Identifier table = tables.values().iterator().next();
		String tableName = table.getIdText().toUpperCase();

		Object[][] queryData = tableData.get(tableName);

		RowSet rowSet = new RowSet();
		rowSet.setData(queryData);

		String[] columnNames = columnMeta.get(tableName);
		int[] columnTypes = new int[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) {
			columnTypes[i] = Fields.FIELD_TYPE_STRING;
		}

		RowSetMetaData metaData = new RowSetMetaData();
		metaData.setColumnNames(columnNames);
		metaData.setColumnTypes(columnTypes);
		rowSet.setMetaData(metaData);

		return rowSet;
	}

}
