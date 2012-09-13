package com.dianping.bee.engine.spi;

import java.util.List;
import java.util.Map;

import com.dianping.bee.engine.evaluator.Evaluator;

public interface RowContext {
	public void afterQuery();

	public void applyRow();

	public void beforeQuery();

	public <T> List<T> getAttributeValues(String name);

	public <T extends ColumnMeta> T getColumn(int colIndex);

	public int getColumnSize();

	public Evaluator<?, ?> getEvaluator(String name);

	public <T> T getFirstAttribute(String name, T defaultValue);

	public <T> T getParameter(int colIndex);

	public <T> T getValue(int colIndex);

	public <T> T getValue(String columnName);

	public void setAttributes(Map<String, List<Object>> m_attributes);

	public void setColumns(ColumnMeta[] columns);

	public void setColumnValue(int colIndex, Object value);

	public void setParameters(Object[] params);

	public void setRowListener(RowListener listener);
}
