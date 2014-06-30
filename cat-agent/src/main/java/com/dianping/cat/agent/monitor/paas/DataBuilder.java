package com.dianping.cat.agent.monitor.paas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.DataEntity;
import com.dianping.cat.agent.monitor.Utils;

public class DataBuilder {

	private static final String PAAS_MONINTOR = System.getProperty("user.dir") + "/paas-monitor.py";

	private static Map<String, String> m_ip2Md5 = new HashMap<String, String>();

	private static Map<String, Pair<Double, Double>> m_lastFlow = new HashMap<String, Pair<Double, Double>>();

	private Pair<Double, Double> findOrCreateFlow(String ip) {
		Pair<Double, Double> flow = m_lastFlow.get(ip);

		if (flow == null) {
			flow = new Pair<Double, Double>(-1D, -1D);
			m_lastFlow.put(ip, flow);
		}
		return flow;
	}

	private String findOrCreateMd5Info(String ip) {
		String md5Info = m_ip2Md5.get(ip);

		if (md5Info == null) {
			md5Info = "";

			m_ip2Md5.put(ip, md5Info);
		}
		return md5Info;
	}

	private List<DataEntity> convert2DataEntities(List<String> lines) {
		List<DataEntity> dataEntities = new ArrayList<DataEntity>();
		Pair<Double, Double> flow = null;
		String domain = null;
		String ip = null;

		for (String line : lines) {
			try {
				int index = line.indexOf("=");
				String key = line.substring(0, index);
				String value = line.substring(index + 1);
				int typeIndex = key.indexOf(":");
				String type = key.substring(typeIndex + 1);
				String realKey = key.substring(0, typeIndex >= 0 ? typeIndex : 0);

				if (line.startsWith("domain")) {
					domain = value;
					ip = key.substring(key.lastIndexOf("_"));
					flow = findOrCreateFlow(ip);
				} else if (line.startsWith("system_eth0-in-flow")) {
					DataEntity inFlow = buildInFlowData(domain, type, realKey, flow, value);

					add2Entities(dataEntities, inFlow);
				} else if (line.startsWith("system_eth0-out-flow")) {
					DataEntity outFlow = buildOutFlowData(domain, type, realKey, flow, value);

					add2Entities(dataEntities, outFlow);
				} else if (line.startsWith("system_md5Change")) {
					DataEntity md5Info = buildMd5Info(domain, type, realKey, ip, value);

					add2Entities(dataEntities, md5Info);
				} else {
					DataEntity entity = new DataEntity();

					entity.setGroup("system-" + domain).setDomain(domain).setId(realKey).setTime(System.currentTimeMillis())
					      .setType(type).setValue(Double.parseDouble(value));
					dataEntities.add(entity);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return dataEntities;
	}

	private void add2Entities(List<DataEntity> entities, DataEntity entity) {
		if (entity != null) {
			entities.add(entity);
		}
	}

	private DataEntity buildInFlowData(String domain, String type, String key, Pair<Double, Double> flow, String value) {
		DataEntity entity = null;

		try {
			double flowValue = Double.parseDouble(value);
			double lastInFlow = flow.getKey();

			if (lastInFlow >= 0) {
				double gap = flowValue - lastInFlow;
				entity = new DataEntity();

				entity.setGroup(buildGroup(domain)).setDomain(domain).setId(key).setTime(System.currentTimeMillis())
				      .setType(type).setValue(gap);
			}
			flow.setKey(flowValue);
		} catch (Exception e) {
			Cat.logError(e);
		}

		return entity;
	}

	private DataEntity buildOutFlowData(String domain, String type, String key, Pair<Double, Double> flow, String value) {
		DataEntity entity = null;
		double flowValue = Double.parseDouble(value);
		double lastFlow = flow.getValue();

		if (lastFlow >= 0) {
			double gap = flowValue - lastFlow;
			entity = new DataEntity();

			entity.setGroup(buildGroup(domain)).setDomain(domain).setId(key).setTime(System.currentTimeMillis())
			      .setType(type).setValue(gap);
		}
		flow.setValue(flowValue);
		return entity;
	}

	private DataEntity buildMd5Info(String domain, String type, String key, String ip, String value) {
		String md5Info = findOrCreateMd5Info(ip);
		DataEntity entity = null;

		if (StringUtils.isNotEmpty(md5Info)) {
			entity = new DataEntity();
			entity.setGroup(buildGroup(domain)).setDomain(domain).setId(key).setTime(System.currentTimeMillis())
			      .setType(type);
			if (md5Info.equals(value)) {
				entity.setValue(1);
			} else {
				entity.setValue(0);
			}
		} else {
			m_ip2Md5.put(ip, value);
		}
		return entity;
	}

	private String buildGroup(String domain) {
		return "system-" + domain;
	}

	public List<String> queryInstances() {
		String cmd = "/usr/bin/python " + PAAS_MONINTOR + " instance_ids";
		List<String> outputs = null;

		try {
			outputs = Utils.runShell(cmd);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return outputs;
	}

	public List<DataEntity> buildData(String id) {
		String cmd = "/usr/bin/python " + PAAS_MONINTOR + " " + id;
		List<String> outputs = null;

		try {
			outputs = Utils.runShell(cmd);
		} catch (Exception e) {
			Cat.logError(e);
		}
		List<DataEntity> dataEntities = convert2DataEntities(outputs);

		return dataEntities;
	}
}
