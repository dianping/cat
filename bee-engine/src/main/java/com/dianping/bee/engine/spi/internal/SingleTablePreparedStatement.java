package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.dianping.bee.engine.spi.PreparedStatement;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.RowSet;

public class SingleTablePreparedStatement extends SingleTableStatement implements PreparedStatement {
	private Object[] m_params;

	private ColumnMeta[] m_paramMetas;

	@Override
	public int getParameterSize() {
		return m_params.length;
	}

	@Override
	public ColumnMeta getParameterMeta(int paramIndex) {
		if (paramIndex >= 0 && paramIndex < m_paramMetas.length) {
			return m_paramMetas[paramIndex];
		} else {
			throw new IndexOutOfBoundsException("size: " + m_paramMetas.length + ", index: " + paramIndex);
		}
	}

	public void setParameterMetas(List<ColumnMeta> paramMetas) {
		int len = paramMetas.size();
		ColumnMeta[] columns = new ColumnMeta[len];

		for (int i = 0; i < len; i++) {
			ColumnMeta column = paramMetas.get(i);

			columns[i] = column;
		}

		m_paramMetas = columns;
		m_params = new Object[m_paramMetas.length];
	}

	public void setParameter(int index, Object param) {
		if (index >= 0 && index < m_params.length) {
			m_params[index] = param;
		} else {
			throw new IndexOutOfBoundsException("size: " + m_params.length + ", index: " + index);
		}
	}

	@Override
	public RowSet query() {
		if (m_params != null) {
			ctx.setParameters(m_params);
		}
		return super.query();
	}

}
