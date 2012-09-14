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
import com.site.lookup.ContainerHolder;

public class DefaultRowListener extends ContainerHolder implements RowListener {
	private RowFilter m_filter;

	private RowSet m_rowset;

	private SelectField[] m_fields;

	private boolean m_aggregator;

	private Object[] m_values;

	public DefaultRowListener(List<SelectField> fields) {
		m_fields = fields.toArray(new SelectField[0]);
		m_rowset = new DefaultRowSet(m_fields);
	}

	public RowSet getRowSet() {
		return m_rowset;
	}

	@Override
	public void onRow(RowContext ctx) {
		if (m_filter != null && !m_filter.filter(ctx)) {
			return;
		}

		int len = m_fields.length;

		if (!m_aggregator) {
			Cell[] cells = new Cell[len];

			for (int i = 0; i < len; i++) {
				SelectField field = m_fields[i];
				Object value = field.evaluate(ctx, i);

				cells[i] = new DefaultCell(field, value);
			}

			m_rowset.addRow(new DefaultRow(cells));
		} else {
			for (int i = 0; i < len; i++) {
				SelectField field = m_fields[i];

				m_values[i] = field.evaluate(ctx, i);
			}
		}
	}

	public void setRowFilter(RowFilter filter) {
		m_filter = filter;
	}

	@Override
	public void onBegin(RowContext ctx) {
		m_values = new Object[m_fields.length];

		for (SelectField field : m_fields) {
			if (field.isAggregator(ctx)) {
				m_aggregator = true;
				break;
			}
		}
	}

	@Override
	public void onEnd(RowContext ctx) {
		if (m_aggregator) {
			int len = m_fields.length;
			Cell[] cells = new Cell[len];

			for (int i = 0; i < len; i++) {
				SelectField field = m_fields[i];

				if (field.isAggregator(ctx)) {
					m_values[i] = field.getAggregatedValue();
					field.reset(ctx);
				}

				cells[i] = new DefaultCell(field, m_values[i]);
			}

			m_rowset.addRow(new DefaultRow(cells));
		}
	}
}
