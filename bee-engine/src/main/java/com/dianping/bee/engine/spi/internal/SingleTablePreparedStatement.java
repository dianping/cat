package com.dianping.bee.engine.spi.internal;

import com.dianping.bee.engine.spi.PreparedStatement;
import com.dianping.bee.engine.spi.meta.RowSet;

public class SingleTablePreparedStatement extends SingleTableStatement implements PreparedStatement {
	private Object[] m_params;

	@Override
	public int getParameterSize() {
		return m_params.length;
	}

	public void setParameterSize(int parameterSize) {
		m_params = new Object[parameterSize];
	}

	@Override
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
