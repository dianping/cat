package com.dianping.cat.report.page.nettopo;

import org.w3c.dom.Node;
import org.unidal.tuple.Pair;

import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.task.metric.RemoteMetricReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class Interface {
	private String group;
	private String domain;
	private String key;
	private double in;
	private double out;
	
	public Interface(Node node) {
		DomOp domOp = new DomOp(node);
		group = domOp.getAttribute("group");
		domain = domOp.getAttribute("domain");
		key = domOp.getAttribute("key");
		UpdateData();
	}
	
	public void UpdateData() {
		try {
			RemoteMetricReportService service = new RemoteMetricReportService();
	        ModelRequest request = new ModelRequest(group, ModelPeriod.CURRENT.getStartTime());
	        MetricReport report = service.invoke(request, new Pair<String, Integer>("10.1.1.167", 80));
	        MetricItem metricItem = report.findMetricItem(domain+":Metric:"+key + "-in");
	        in = metricItem.getSegments().get(metricItem.getSegments().size()-1).getAvg();
	        metricItem = report.findMetricItem(domain+":Metric:"+key + "-out");
	        out = metricItem.getSegments().get(metricItem.getSegments().size()-1).getAvg();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public double getIn() {
		return in;
	}
	
	public void setIn(double in) {
		this.in = in;
	}
	
	public double getOut() {
		return out;
	}
	
	public void setOut(double out) {
		this.out = out;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
