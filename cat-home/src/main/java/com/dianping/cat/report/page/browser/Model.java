package com.dianping.cat.report.page.browser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.configuration.web.entity.Item;
import com.dianping.cat.configuration.web.speed.entity.Speed;
import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.browser.display.AjaxDataDisplayInfo;
import com.dianping.cat.report.page.browser.display.JsErrorDetailInfo;
import com.dianping.cat.report.page.browser.display.JsErrorDisplayInfo;
import com.dianping.cat.report.page.browser.display.WebSpeedDetail;
import com.dianping.cat.report.page.browser.display.WebSpeedDisplayInfo;

@ModelMeta("model")
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private Map<String, PatternItem> m_pattermItems;

	private WebSpeedDisplayInfo m_webSpeedDisplayInfo;

	private AjaxDataDisplayInfo m_ajaxDataDisplayInfo;

	private JsErrorDisplayInfo m_jsErrorDisplayInfo;

	private JsErrorDetailInfo m_jsErrorDetailInfo;

	private Map<Integer, Item> m_cities;

	private Map<Integer, Item> m_platforms;

	private Map<Integer, Item> m_operators;

	private Map<Integer, Code> m_codes;

	private Map<Integer, Item> m_networks;

	private Map<Integer, Item> m_sources;

	private Map<String, Speed> m_speeds;

	private String m_defaultApi;

	private String m_fetchData;

	public Model(Context ctx) {
		super(ctx);
	}

	public AjaxDataDisplayInfo getAjaxDataDisplayInfo() {
		return m_ajaxDataDisplayInfo;
	}

	public Map<Integer, Item> getCities() {
		return m_cities;
	}

	public Map<Integer, Code> getCodes() {
		return m_codes;
	}

	@Override
	public Action getDefaultAction() {
		return Action.AJAX_LINECHART;
	}

	public String getDefaultApi() {
		return m_defaultApi;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public String getFetchData() {
		return m_fetchData;
	}

	public JsErrorDetailInfo getJsErrorDetailInfo() {
		return m_jsErrorDetailInfo;
	}

	public JsErrorDisplayInfo getJsErrorDisplayInfo() {
		return m_jsErrorDisplayInfo;
	}

	public Map<Integer, Item> getNetworks() {
		return m_networks;
	}

	public Map<Integer, Item> getOperators() {
		return m_operators;
	}

	public String getPage2StepsJson() {
		return new JsonBuilder().toJson(m_speeds);
	}

	public Map<String, PatternItem> getPattermItems() {
		return m_pattermItems;
	}

	public String getPattern2Items() {
		return new JsonBuilder().toJson(m_pattermItems);
	}

	public Map<Integer, Item> getPlatforms() {
		return m_platforms;
	}

	public Map<Integer, Item> getSources() {
		return m_sources;
	}

	public Map<String, Speed> getSpeeds() {
		return m_speeds;
	}

	public Map<String, Map<Integer, WebSpeedDetail>> getWebSpeedDetails() {
		Map<String, Map<Integer, WebSpeedDetail>> map = new LinkedHashMap<String, Map<Integer, WebSpeedDetail>>();
		Map<String, List<WebSpeedDetail>> details = m_webSpeedDisplayInfo.getWebSpeedDetails();

		if (details != null && !details.isEmpty()) {
			for (Entry<String, List<WebSpeedDetail>> entry : details.entrySet()) {
				Map<Integer, WebSpeedDetail> m = new LinkedHashMap<Integer, WebSpeedDetail>();

				for (WebSpeedDetail detail : entry.getValue()) {
					m.put(detail.getMinuteOrder(), detail);
				}
				map.put(entry.getKey(), m);
			}
		}
		return map;
	}

	public WebSpeedDisplayInfo getWebSpeedDisplayInfo() {
		return m_webSpeedDisplayInfo;
	}

	public Map<String, Map<Integer, WebSpeedDetail>> getWebSpeedSummarys() {
		Map<String, Map<Integer, WebSpeedDetail>> map = new LinkedHashMap<String, Map<Integer, WebSpeedDetail>>();
		Map<String, WebSpeedDetail> details = m_webSpeedDisplayInfo.getWebSpeedSummarys();

		if (details != null && !details.isEmpty()) {
			for (Entry<String, WebSpeedDetail> entry : details.entrySet()) {
				Map<Integer, WebSpeedDetail> m = new LinkedHashMap<Integer, WebSpeedDetail>();
				WebSpeedDetail d = entry.getValue();

				m.put(d.getMinuteOrder(), d);
				map.put(entry.getKey(), m);
			}
		}
		return map;
	}

	public void setAjaxDataDisplayInfo(AjaxDataDisplayInfo ajaxDataDisplayInfo) {
		m_ajaxDataDisplayInfo = ajaxDataDisplayInfo;
	}

	public void setCities(Map<Integer, Item> cities) {
		m_cities = cities;
	}

	public void setCodes(Map<Integer, Code> codes) {
		m_codes = codes;
	}

	public void setDefaultApi(String defaultApi) {
		m_defaultApi = defaultApi;
	}

	public void setFetchData(String fetchData) {
		m_fetchData = fetchData;
	}

	public void setJsErrorDetailInfo(JsErrorDetailInfo jsErrorDetailInfo) {
		m_jsErrorDetailInfo = jsErrorDetailInfo;
	}

	public void setJsErrorDisplayInfo(JsErrorDisplayInfo jsErrorDisplayInfo) {
		m_jsErrorDisplayInfo = jsErrorDisplayInfo;
	}

	public void setNetworks(Map<Integer, Item> networks) {
		m_networks = networks;
	}

	public void setOperators(Map<Integer, Item> operators) {
		m_operators = operators;
	}

	public void setPattermItems(Map<String, PatternItem> pattermItems) {
		m_pattermItems = pattermItems;
	}

	public void setPlatforms(Map<Integer, Item> platforms) {
		m_platforms = platforms;
	}

	public void setSources(Map<Integer, Item> sources) {
		m_sources = sources;
	}

	public void setSpeeds(Map<String, Speed> speeds) {
		m_speeds = speeds;
	}

	public void setWebSpeedDisplayInfo(WebSpeedDisplayInfo webSpeedDisplayInfo) {
		m_webSpeedDisplayInfo = webSpeedDisplayInfo;
	}

}
