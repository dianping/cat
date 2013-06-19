package com.dianping.cat.consumer.advanced;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

public class BussinessConfigManager implements Initializable {

	// key is domain
	private Map<String, Map<Integer, Map<String, BusinessConfig>>> m_configs = new ConcurrentHashMap<String, Map<Integer, Map<String, BusinessConfig>>>();

	public Map<String, BusinessConfig> getUrlConfigs(String domain) {
		return getMetricConfigsByType(domain, BusinessConfig.URL);
	}

	public Map<String, BusinessConfig> getMetricConfigs(String domain) {
		return getMetricConfigsByType(domain, BusinessConfig.METRIC);
	}

	public List<BusinessConfig> getConfigs(List<String> domains) {
		List<BusinessConfig> configs = new ArrayList<BusinessConfig>();

		for (String domain : domains) {
			Map<Integer, Map<String, BusinessConfig>> value = m_configs.get(domain);

			for (Entry<Integer, Map<String, BusinessConfig>> internalEntry : value.entrySet()) {
				configs.addAll(internalEntry.getValue().values());
			}
		}

		Collections.sort(configs, new BusinessConfigCompator());
		return configs;
	}

	private Map<String, BusinessConfig> getMetricConfigsByType(String domain, int type) {
		Map<Integer, Map<String, BusinessConfig>> configMap = m_configs.get(domain);

		if (configMap != null) {
			Map<String, BusinessConfig> config = configMap.get(type);

			if (config != null) {
				return config;
			}
		}
		return new HashMap<String, BusinessConfig>();
	}

	private BussinessConfigManager addConfig(BusinessConfig config) {
		String domain = config.getDomain();
		Map<Integer, Map<String, BusinessConfig>> configsMap = m_configs.get(domain);

		if (configsMap == null) {
			configsMap = new ConcurrentHashMap<Integer, Map<String, BusinessConfig>>();
			m_configs.put(config.getDomain(), configsMap);
		}

		int type = config.getType();
		Map<String, BusinessConfig> configs = configsMap.get(type);

		if (configs == null) {
			configs = new ConcurrentHashMap<String, BusinessConfig>();
			configsMap.put(config.getType(), configs);
		}
		configs.put(config.getMainKey(), config);
		return this;
	}

	@Override
	public void initialize() throws InitializationException {
		String TuanGouWeb = "TuanGouWeb";
		String PayOrder = "PayOrder";
		String Cat = "Cat";

		BusinessConfig config = new BusinessConfig();

		config.setDomain(Cat).setType(BusinessConfig.URL);
		config.setViewOrder(1).setMainKey("t").setClassifications(null);
		config.setTitle("Transaction").setShowCount(true).setShowAvg(true).setShowSum(true);
		addConfig(config);

		config = new BusinessConfig();

		config.setDomain(Cat).setType(BusinessConfig.URL);
		config.setViewOrder(2).setMainKey("e").setClassifications(null);
		config.setTitle("Event").setShowCount(true).setShowAvg(false).setShowSum(false);

		addConfig(config);

		config = new BusinessConfig();
		config.setDomain(Cat).setType(BusinessConfig.URL);
		config.setViewOrder(3).setMainKey("home").setClassifications(null);
		config.setTitle("Home").setShowCount(true).setShowAvg(false).setShowSum(false);

		addConfig(config);

		config = new BusinessConfig();
		config.setDomain(TuanGouWeb).setType(BusinessConfig.URL);
		config.setViewOrder(1).setMainKey("/index").setClassifications("channel");
		config.setTitle(MetricTitle.INDEX).setShowCount(true).setShowAvg(false).setShowSum(false);

		addConfig(config);

		config = new BusinessConfig();
		config.setDomain(TuanGouWeb).setType(BusinessConfig.URL);
		config.setViewOrder(2).setMainKey("/detail").setClassifications("channel");
		config.setTitle(MetricTitle.DETAIL).setShowCount(true).setShowAvg(false).setShowSum(false);

		addConfig(config);

		config = new BusinessConfig();
		config.setDomain(PayOrder).setType(BusinessConfig.URL);
		config.setViewOrder(3).setMainKey("/order/submitOrder").setClassifications("channel");
		config.setTitle(MetricTitle.PAY).setShowCount(true).setShowAvg(false).setShowSum(false);

		addConfig(config);

		config = new BusinessConfig();
		config.setDomain(PayOrder).setType(BusinessConfig.METRIC);
		config.setViewOrder(4).setMainKey("order").setClassifications("channel").setTarget("quantity");
		config.setTitle(MetricTitle.ORDER).setShowCount(true).setShowAvg(false).setShowSum(false);

		addConfig(config);

		config = new BusinessConfig();
		config.setDomain(PayOrder).setType(BusinessConfig.METRIC);
		config.setViewOrder(5).setMainKey("payment.pending").setClassifications("channel").setTarget("amount");
		config.setTitle(MetricTitle.SUCCESS).setShowCount(false).setShowAvg(false).setShowSum(false);

		addConfig(config);

		config = new BusinessConfig();
		config.setDomain(PayOrder).setType(BusinessConfig.METRIC);
		config.setViewOrder(6).setMainKey("payment.success").setClassifications("channel").setTarget("amount");
		config.setTitle(MetricTitle.SUCCESS).setShowCount(false).setShowAvg(false).setShowSum(true);

		addConfig(config);
	}

	public static class BusinessConfigCompator implements Comparator<BusinessConfig> {

		@Override
		public int compare(BusinessConfig o1, BusinessConfig o2) {
			return o1.getViewOrder() - o2.getViewOrder();
		}

	}

	public static class BusinessConfig {
		public static final int URL = 1;

		public static final int METRIC = 2;

		public static final String Suffix_SUM = "(总和)";

		public static final String Suffix_COUNT = "(次数)";

		public static final String Suffix_AVG = "(平均)";

		private String m_domain;

		private int m_type;

		private int m_viewOrder;

		private String m_mainKey;

		private String m_target;

		private String m_classifications;

		private String m_title;

		private boolean m_showSum;

		private boolean m_showCount;

		private boolean m_showAvg;

		public String getTarget() {
			return m_target;
		}

		public BusinessConfig setTarget(String target) {
			m_target = target;
			return this;
		}

		public String getClassifications() {
			return m_classifications;
		}

		public String getDomain() {
			return m_domain;
		}

		public String getMainKey() {
			return m_mainKey;
		}

		public String getTitle() {
			return m_title;
		}

		public int getType() {
			return m_type;
		}

		public int getViewOrder() {
			return m_viewOrder;
		}

		public boolean isShowAvg() {
			return m_showAvg;
		}

		public boolean isShowCount() {
			return m_showCount;
		}

		public boolean isShowSum() {
			return m_showSum;
		}

		public BusinessConfig setClassifications(String childKeys) {
			m_classifications = childKeys;
			return this;
		}

		public BusinessConfig setDomain(String domain) {
			m_domain = domain;
			return this;
		}

		public BusinessConfig setMainKey(String mainKey) {
			m_mainKey = mainKey;
			return this;
		}

		public BusinessConfig setShowAvg(boolean showAvg) {
			m_showAvg = showAvg;
			return this;
		}

		public BusinessConfig setShowCount(boolean showCount) {
			m_showCount = showCount;
			return this;
		}

		public BusinessConfig setShowSum(boolean showSum) {
			m_showSum = showSum;
			return this;
		}

		public BusinessConfig setTitle(String title) {
			m_title = title;
			return this;
		}

		public BusinessConfig setType(int type) {
			m_type = type;
			return this;
		}

		public BusinessConfig setViewOrder(int viewOrder) {
			m_viewOrder = viewOrder;
			return this;
		}
	}

	public class MetricTitle {

		public static final String INDEX = "团购首页";

		public static final String DETAIL = "团购详情";

		public static final String PAY = "支付页面";

		public static final String ORDER = "订单创建";

		public static final String SUCCESS = "支付金额(单位:元)";
	}
}
