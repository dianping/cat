package com.dianping.bee.engine.spi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.bee.engine.spi.meta.Cell;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.Row;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class SpiTest extends ComponentTestCase {
	@Test
	public void sample() throws Exception {
		StatementManager statementManager = lookup(StatementManager.class);
		Statement stmt = statementManager.build("select type, sum(failures) from transaction where domain=? and starttime=?");
		RowSet rowset = stmt.query();

		display(rowset);
	}
	
	private void display(RowSet rowset) {
		StringBuilder sb = new StringBuilder(1024);
		int cols = rowset.getColumns();

		for (int i = 0; i < cols; i++) {
			ColumnMeta column = rowset.getColumn(i);

			sb.append(column.getName()).append('|');
		}

		sb.append('\n');

		int rows = rowset.getRows();

		for (int i = 0; i < rows; i++) {
			Row row = rowset.getRow(i);

			for (int j = 0; j < cols; j++) {
				Cell cell = row.getCell(j);

				sb.append(cell.getValue()).append('|');
			}

			sb.append('\n');
		}

		sb.append(rows).append(" rows selected.");

		System.out.println(sb);
	}
}
