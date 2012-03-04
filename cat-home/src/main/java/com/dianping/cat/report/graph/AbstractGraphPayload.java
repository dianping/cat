package com.dianping.cat.report.graph;

public abstract class AbstractGraphPayload implements GraphPayload {
	private double[] m_values;

	private String m_title;

	private String m_axisXLabel;

	private String m_axisYLabel;

	public AbstractGraphPayload(String title, String axisXLabel, String axisYLabel) {
		m_title = title;
		m_axisXLabel = axisXLabel;
		m_axisYLabel = axisYLabel;
		m_values = getValues();
	}

	@Override
	public String getAxisXLabel() {
		return m_axisXLabel;
	}

	@Override
	public String getAxisYLabel() {
		return m_axisYLabel;
	}

	@Override
	public int getColumns() {
		return m_values.length;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public int getDisplayHeight() {
		return getHeight();
	}

	@Override
	public int getDisplayWidth() {
		return getWidth();
	}

	@Override
	public int getHeight() {
		return 280;
	}

	@Override
	public int getMarginBottom() {
		return 50;
	}

	@Override
	public int getMarginLeft() {
		return 90;
	}

	@Override
	public int getMarginRight() {
		return 10;
	}

	@Override
	public int getMarginTop() {
		return 50;
	}

	@Override
	public int getRows() {
		return 5;
	}

	@Override
	public String getTitle() {
		return m_title;
	}

	@Override
	public int getWidth() {
		return 580;
	}
}
