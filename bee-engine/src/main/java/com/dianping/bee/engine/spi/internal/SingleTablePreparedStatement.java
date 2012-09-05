package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.dianping.bee.engine.spi.PreparedStatement;

public class SingleTablePreparedStatement extends SingleTableStatement implements PreparedStatement {
	private int m_parameterSize;

	@Override
	public int getParameterSize() {
		return m_parameterSize;
	}

	public void setParameterSize(int parameterSize) {
		m_parameterSize = parameterSize;
	}

	@Override
	public void setParameters(List<Object> params) {
	}

}
