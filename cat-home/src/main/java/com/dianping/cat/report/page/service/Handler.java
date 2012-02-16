package com.dianping.cat.report.page.service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.service.provider.FailureModelProvider;
import com.dianping.cat.report.page.service.provider.ModelProvider;
import com.dianping.cat.report.page.service.provider.TransactionModelProvider;
import com.dianping.cat.tool.Constant;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelProvider.class, value = "failure")
	private ModelProvider m_failureModel;

	@Inject(type = ModelProvider.class, value = "transaction")
	private ModelProvider m_transactionModel;

	@Inject(type = ModelProvider.class, value = "ip")
	private ModelProvider m_ipModel;

	
	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "service")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "service")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.SERVICE);
		String index = payload.getIndex();
		String modelStr = payload.getModel();
		String domain = payload.getDomain();

		if (modelStr == null || modelStr.equals(Constant.FAILURE)) {
			String domains = getDomains(m_failureModel);
			model.setDomains(domains);
			if (null == domain) {
				domain = m_failureModel.getDefaultDomain();
			}
			FailureModelProvider provider = (FailureModelProvider) m_failureModel;
			String ips = getFailureIps(provider, domain);
			model.setIps(ips);
			String ip = payload.getIp();
			if (null == ip) {
				ip = provider.getDefaultIpByDomain(domain);
			}
			if (ip != null && domain != null) {
				String xmlData = getModelXMLData(domain, ip, index, m_failureModel);
				model.setXmlData(xmlData);
			} else {
				model.setXmlData("No domain or no ip in failure report!");
			}

		} else if (modelStr.equals(Constant.TRANSACTION)) {
			String domains = getDomains(m_transactionModel);
			model.setDomains(domains);
			if (null == domain) {
				domain = m_transactionModel.getDefaultDomain();
			}
			if (null != domain) {
				String xmlData = getModelXMLData(domain, "", index, m_transactionModel);
				model.setXmlData(xmlData);
			} else {
				model.setXmlData("No domain in transaction report!");
			}
		} else if (modelStr.equalsIgnoreCase("ip")) {
			String domains = getDomains(m_ipModel);
			model.setDomains(domains);
			if (null == domain) {
				domain = m_ipModel.getDefaultDomain();
			}
			if (null != domain) {
				String xmlData = getModelXMLData(domain, "", index, m_ipModel);
				model.setXmlData(xmlData);
			} else {
				model.setXmlData("No domain in ip report!");
			}
		}
		m_jspViewer.view(ctx, model);
	}

	private String getFailureIps(FailureModelProvider failureModel, String domain) {
		List<String> ips = failureModel.getIpsByDomain(domain);
		Collections.sort(ips);
		StringBuffer ipsBuffer = new StringBuffer();
		for (String temp : ips) {
			ipsBuffer.append(temp).append("\t");
		}
		return ipsBuffer.toString();
	}

	private String getDomains(ModelProvider provider) {
		List<String> domains = provider.getDomains();
		Collections.sort(domains);
		StringBuffer domainsBuffer = new StringBuffer();
		for (String temp : domains) {
			domainsBuffer.append(temp).append("\t");
		}
		return domainsBuffer.toString();
	}

	private String getModelXMLData(String domain, String ip, String index, ModelProvider provider) {
		if (index == null) {
			index = Constant.MEMORY_CURRENT;
		}
		if (index.equals(Constant.MEMORY_CURRENT) || index.equals(Constant.MEMORY_LAST)) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("domain", domain);
			map.put("ip", ip);
			map.put("index", index);
			return provider.getModel(map);
		}
		return null;
	}
}
