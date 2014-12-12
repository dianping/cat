package com.dianping.cat.report.graph.svg;

public abstract class AbstractGraphPayload implements GraphPayload {
	private String m_axisXLabel;

	private String m_axisYLabel;

	private String m_title;

	private double[] m_values;

	public AbstractGraphPayload(String title, String axisXLabel, String axisYLabel) {
		m_title = title;
		m_axisXLabel = axisXLabel;
		m_axisYLabel = axisYLabel;
	}

	@Override
	public String getAxisXLabel(int index) {
		return String.valueOf(index);
	}

	@Override
	public String getAxisXTitle() {
		return m_axisXLabel;
	}

	@Override
	public String getAxisYTitle() {
		return m_axisYLabel;
	}

	@Override
	public int getColumns() {
		return getValues().length;
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
		return getWidth() - 50;
	}

	@Override
	public int getHeight() {
		return 250;
	}

	@Override
	public String getIdPrefix() {
		Class<?> clazz = getClass();

		if (clazz.isAnonymousClass()) {
			return "a";
		} else {
			return clazz.getSimpleName().substring(0, 1);
		}
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
		return 40;
	}

	@Override
	public int getOffsetX() {
		return 0;
	}

	@Override
	public int getOffsetY() {
		return 0;
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
	public final double[] getValues() {
		if (m_values == null) {
			m_values = loadValues();
		}

		return m_values;
	}

	@Override
	public int getWidth() {
		return 580 - 50;
	}

	@Override
	public boolean isAxisXLabelRotated() {
		return false;
	}

	@Override
	public boolean isAxisXLabelSkipped() {
		return getValues().length >= 16;
	}

	@Override
	public boolean isStandalone() {
		return true;
	}

	protected abstract double[] loadValues();
}
