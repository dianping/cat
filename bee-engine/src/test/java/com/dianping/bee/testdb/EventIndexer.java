/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-3
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
package com.dianping.bee.testdb;

import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.testdb.CatDatabase.EventColumn;

public class EventIndexer implements Index {

	@Override
	public void query(RowContext ctx) throws Exception {
		Object[][] sampleData = EventSampleData.getSampleData();
		for (int i = 0; i < sampleData.length; i++) {
			applyRow(ctx, sampleData[i]);
		}
	}

	/**
	 * @param objects
	 */
	private void applyRow(RowContext ctx, Object[] row) {
		int cols = ctx.getColumnSize();

		for (int i = 0; i < cols; i++) {
			EventColumn column = ctx.getColumn(i);

			switch (column) {
			case Type:
				ctx.setColumnValue(i, row[2]);
				break;
			case Name:
				ctx.setColumnValue(i, row[3]);
				break;
			case Domain:
				ctx.setColumnValue(i, row[1]);
				break;
			case StartTime:
				ctx.setColumnValue(i, row[0]);
				break;
			case MinDuration:
				ctx.setColumnValue(i, row[7]);
				break;
			case MaxDuration:
				ctx.setColumnValue(i, row[8]);
				break;
			case SampleMessage:
				ctx.setColumnValue(i, row[6]);
				break;
			case TotalCount:
				ctx.setColumnValue(i, row[4]);
				break;
			case SumDuration:
				ctx.setColumnValue(i, row[9]);
				break;
			case Sum2Duration:
				ctx.setColumnValue(i, row[10]);
				break;
			case Line95:
				ctx.setColumnValue(i, row[11]);
				break;
			case Failures:
				ctx.setColumnValue(i, row[5]);
			default:
				// TODO more here
			}
		}

		ctx.apply();
	}

	static class EventSampleData {
		private static Object[][] sampleData;

		static {
			sampleData = new Object[10][];
			Random random = new Random();
			int columnSize = EventColumn.values().length;
			for (int i = 0; i < sampleData.length; i++) {
				sampleData[i] = new Object[columnSize];
				for (int j = 0; j < columnSize; j++) {

					if (EventColumn.values()[j].getName().equalsIgnoreCase("Domain")) {
						sampleData[i][j] = random.nextBoolean() ? "MobileApi" : RandomStringUtils.randomAlphabetic(5);
						continue;
					} else if (EventColumn.values()[j].getName().equalsIgnoreCase("StartTime")) {
						sampleData[i][j] = random.nextBoolean() ? "20120822" : RandomStringUtils.randomNumeric(8);
						continue;
					}

					if (EventColumn.values()[j].getType().getSimpleName().equals("String")) {
						sampleData[i][j] = RandomStringUtils.randomAlphabetic(5);
					} else if (EventColumn.values()[j].getType().getSimpleName().equals("Integer")) {
						sampleData[i][j] = RandomStringUtils.randomNumeric(3);
					} else if (EventColumn.values()[j].getType().getSimpleName().equals("Long")) {
						sampleData[i][j] = RandomStringUtils.randomNumeric(6);
					} else {
						sampleData[i][j] = RandomStringUtils.randomAlphanumeric(5);
					}
				}
			}
		}

		public static Object[][] getSampleData() {
			return sampleData;
		}
	}

}