package com.dianping.cat.report.graph;

public interface GraphPayload {
	public String getAxisXLabel();

	public String getAxisYLabel();

	public int getColumns();

	public String getDescription();

	public int getHeight();

	public int getMarginBottom();

	public int getMarginLeft();

	public int getMarginRight();

	public int getMarginTop();

	public int getRows();

	public String getTitle();

	public double[] getValues();

	public int getWidth();

	public int getDisplayHeight();

	public int getDisplayWidth();
}
