package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.dianping.bee.engine.Cell;
import com.dianping.bee.engine.RowSet;
import com.dianping.bee.engine.internal.DefaultCell;
import com.dianping.bee.engine.internal.DefaultRow;
import com.dianping.bee.engine.internal.DefaultRowSet;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.RowListener;

public class DefaultRowListener implements RowListener {
	private RowFilter m_filter;

	private RowSet m_rowset;

	private SelectField[] m_fields;

	public DefaultRowListener(List<SelectField> fields) {
		m_fields = fields.toArray(new SelectField[0]);
		m_rowset = new DefaultRowSet(m_fields);
	}

	public RowSet getRowSet() {
		return m_rowset;
	}

	@Override
	public void onRow(RowContext ctx) {
		if (m_filter == null || m_filter.filter(ctx)) {
			int len = m_fields.length;
			Cell[] cells = new Cell[len];

			for (int i = 0; i < len; i++) {
				SelectField field = m_fields[i];
				Object value = field.evaluate(ctx, i);

				cells[i] = new DefaultCell(field, value);
			}

			m_rowset.addRow(new DefaultRow(cells));
		}
	}

	public void setRowFilter(RowFilter filter) {
		m_filter = filter;
	}
}
