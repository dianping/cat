package com.dianping.cat.report.graph;

public interface GraphPayload {
	public String getAxisXTitle();

	public String getAxisYTitle();

	public String getAxisXLabel(int index);

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

	public boolean isAxisXLabelRotated();

	public boolean isAxisXLabelSkipped();
}
