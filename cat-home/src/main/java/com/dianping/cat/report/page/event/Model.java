package com.dianping.cat.report.page.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	private EventReport m_report;

	private String m_type;

	private String m_graph1;

	private String m_graph2;

	private String m_graph3;

	private String m_graph4;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
   public String getDomain() {
		if (m_report == null) {
			return null;
		} else {
			return m_report.getDomain();
		}
   }

	// required by report tag
	@Override
	public List<String> getDomains() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			List<String> result = new ArrayList<String>();
			Set<String> domains = m_report.getDomains();
			
			for (String domain : domains) {
				result.add(domain);
			}
			Collections.sort(result);
			return result;
		}
	}

	public String getGraph1() {
		return m_graph1;
	}

	public String getGraph2() {
		return m_graph2;
	}

	public String getGraph3() {
		return m_graph3;
	}

	public String getGraph4() {
		return m_graph4;
	}

	public EventReport getReport() {
		return m_report;
	}

	public String getType() {
		return m_type;
	}

	public void setGraph1(String graph1) {
		m_graph1 = graph1;
	}

	public void setGraph2(String graph2) {
		m_graph2 = graph2;
	}

	public void setGraph3(String graph3) {
		m_graph3 = graph3;
	}

	public void setGraph4(String graph4) {
		m_graph4 = graph4;
	}

	public void setReport(EventReport report) {
		m_report = report;
	}

	public void setType(String type) {
		m_type = type;
	}
}
