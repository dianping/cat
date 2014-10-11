package com.dianping.cat.agent.monitor.paas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.CommandUtils;
import com.dianping.cat.agent.monitor.DataEntity;

public class DataBuilder {

	@Inject
	private CommandUtils m_commandUtils;

	private Map<String, Double> m_lastValues = new HashMap<String, Double>();

	private Map<String, String> m_ip2Md5 = new HashMap<String, String>();

	private void add2Entities(List<DataEntity> entities, DataEntity entity) {
		if (entity != null) {
			entities.add(entity);
		}
	}

	public List<DataEntity> buildData(String id) {
		String cmd = "/usr/bin/python " + getPaasMonintor() + " " + id;
		List<String> outputs = null;

		try {
			outputs = m_commandUtils.runShell(cmd);
			
			return convert2DataEntities(outputs);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new ArrayList<DataEntity>();
	}

	private String buildGroup(String domain) {
		return "system-" + domain;
	}

	private DataEntity buildMd5Info(String domain, String type, String realKey, String key, String value) {
		String md5Info = findOrCreateMd5Info(key);
		DataEntity entity = null;

		if (StringUtils.isNotEmpty(md5Info)) {
			entity = new DataEntity();
			entity.setGroup(buildGroup(domain)).setDomain(domain).setId(realKey).setTime(System.currentTimeMillis())
			      .setType(type);
			if (md5Info.equals(value)) {
				entity.setValue(1);
			} else {
				entity.setValue(0);
			}
		} else {
			m_ip2Md5.put(key, value);
		}
		return entity;
	}

	private DataEntity buildSumEntity(String domain, String type, String realKey, String key, String value) {
		DataEntity entity = null;

		try {
			double currentValue = Double.parseDouble(value);
			double lastValue = findOrCreateSumValue(key);

			if (lastValue >= 0) {
				double gap = currentValue - lastValue;
				entity = new DataEntity();

				entity.setGroup(buildGroup(domain)).setDomain(domain).setId(realKey).setTime(System.currentTimeMillis())
				      .setType(type).setValue(gap);
			}
			m_lastValues.put(key, currentValue);
		} catch (Exception e) {
			Cat.logError(e);
		}

		return entity;
	}

	private List<DataEntity> convert2DataEntities(List<String> lines) {
		List<DataEntity> dataEntities = new ArrayList<DataEntity>();
		String domain = null;

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
				} else if ("sum".equals(type)) {
					DataEntity inFlow = buildSumEntity(domain, type, realKey, key, value);

					add2Entities(dataEntities, inFlow);
				} else if (line.startsWith("system_md5Change")) {
					DataEntity md5Info = buildMd5Info(domain, type, realKey, key, value);

					add2Entities(dataEntities, md5Info);
				} else if ("avg".equals(type)) {
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

	private String findOrCreateMd5Info(String key) {
		String md5Info = m_ip2Md5.get(key);

		if (md5Info == null) {
			md5Info = "";

			m_ip2Md5.put(key, md5Info);
		}
		return md5Info;
	}

	private double findOrCreateSumValue(String key) {
		Double value = m_lastValues.get(key);

		if (value == null) {
			value = new Double(-1D);
			m_lastValues.put(key, value);
		}
		return value;
	}

	public String getPaasMonintor() {
		return System.getProperty("user.dir") + "/paas-monitor.py";
	}

	public List<String> queryInstances() {
		String cmd = "/usr/bin/python " + getPaasMonintor() + " instance_ids";
		List<String> outputs = null;

		try {
			outputs = m_commandUtils.runShell(cmd);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return outputs;
	}
}
