package com.dianping.cat.report.page.crash.display;

import java.util.List;

import com.dianping.cat.home.crash.entity.Module;

public class DisplayVersion {
	
	private String m_id;

	private int m_dau;

	private int m_crashCount;

	private double m_crashCountMoM;

	private double m_crashCountYoY;

	private double m_percent;

	private double m_percentMoM;

	private double m_percentYoY;

	private transient List<Module> m_modules;

	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}

	public int getDau() {
		return m_dau;
	}

	public void setDau(int dau) {
		m_dau = dau;
	}

	public int getCrashCount() {
		return m_crashCount;
	}

	public void setCrashCount(int crashCount) {
		m_crashCount = crashCount;
	}

	public double getCrashCountMoM() {
		return m_crashCountMoM;
	}

	public void setCrashCountMoM(double crashCountMoM) {
		m_crashCountMoM = crashCountMoM;
	}

	public double getCrashCountYoY() {
		return m_crashCountYoY;
	}

	public void setCrashCountYoY(double crashCountYoY) {
		m_crashCountYoY = crashCountYoY;
	}

	public double getPercent() {
		return m_percent;
	}

	public void setPercent(double percent) {
		m_percent = percent;
	}

	public double getPercentMoM() {
		return m_percentMoM;
	}

	public void setPercentMoM(double percentMoM) {
		m_percentMoM = percentMoM;
	}

	public double getPercentYoY() {
		return m_percentYoY;
	}

	public void setPercentYoY(double percentYoY) {
		m_percentYoY = percentYoY;
	}

	public List<Module> getModules() {
		return m_modules;
	}

	public void setModules(List<Module> modules) {
		m_modules = modules;
	}
	
	
}
