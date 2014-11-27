package com.dianping.cat.report.graph.svg;

public interface GraphPayload {
	public String getAxisXLabel(int index);

	public String getAxisXTitle();

	public String getAxisYTitle();

	public int getColumns();

	public String getDescription();

	public int getDisplayHeight();

	public int getDisplayWidth();

	public int getHeight();

	public String getIdPrefix();

	public int getMarginBottom();

	public int getMarginLeft();

	public int getMarginRight();

	public int getMarginTop();

	public int getOffsetX();

	public int getOffsetY();

	public int getRows();

	public String getTitle();

	public double[] getValues();

	public int getWidth();

	public boolean isAxisXLabelRotated();

	public boolean isAxisXLabelSkipped();

	public boolean isStandalone();
}
