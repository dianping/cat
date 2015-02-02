package com.dianping.cat.report.page.alteration;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.home.dal.report.AlterationDao;
import com.dianping.cat.home.dal.report.AlterationEntity;
import com.dianping.cat.report.ReportPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AlterationDao m_alterationDao;

	private Alteration buildAlteration(Payload payload) {
		String type = payload.getType();
		String domain = payload.getDomain();
		String hostname = payload.getHostname();
		String title = payload.getTitle();
		String ip = payload.getIp();
		String user = payload.getUser();
		String group = payload.getGroup();
		String content = payload.getContent();
		String url = payload.getUrl();

		Date date = payload.getAlterationDate();
		Alteration alt = new Alteration();

		alt.setType(type);
		alt.setDomain(domain);
		alt.setTitle(title);
		alt.setIp(ip);
		alt.setUser(user);
		alt.setAltGroup(group);
		alt.setContent(content);
		alt.setHostname(hostname);
		alt.setDate(date);
		try {
			alt.setUrl(URLDecoder.decode(url, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Cat.logError(e);
			alt.setUrl("");
		}

		return alt;
	}

	private Map<String, AlterationMinute> generateAlterationMinutes(List<Alteration> alts) {
		Map<String, AlterationMinute> alterationMinutes = new LinkedHashMap<String, AlterationMinute>();
		DateFormat df = new SimpleDateFormat("MM-dd HH:mm");

		for (Alteration alt : alts) {
			Date date = alt.getDate();
			String dateStr = df.format(date);
			AlterationMinute alterationMinute = alterationMinutes.get(dateStr);

			if (alterationMinute == null) {
				alterationMinute = new AlterationMinute(dateStr);

				alterationMinutes.put(dateStr, alterationMinute);
			}
			alterationMinute.add(alt);
		}

		return alterationMinutes;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "alteration")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "alteration")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case INSERT:
			if (!isArguComplete(payload)) {
				setInsertResult(model, 2);
			} else {
				Alteration alt = buildAlteration(payload);
				try {
					int count = m_alterationDao.insert(alt);

					if (count == 0) {
						setInsertResult(model, 1);
						break;
					} else {
						setInsertResult(model, 0);
					}
				} catch (Exception e) {
					Cat.logError(e);
					setInsertResult(model, 1);
				}
			}
			break;
		case VIEW:
			List<Alteration> alts;
			Date startTime = payload.getStartTime();
			Date endTime = payload.getEndTime();
			String[] altTypes = payload.getAltTypeArray();
			String type = payload.getType();
			String domain = payload.getDomain();
			String hostname = payload.getHostname();

			try {
				if (altTypes == null) {
					alts = m_alterationDao.findByDtdh(startTime, endTime, type, domain, hostname,
					      AlterationEntity.READSET_FULL);
				} else {
					alts = m_alterationDao.findByDtdhTypes(startTime, endTime, type, domain, hostname, altTypes,
					      AlterationEntity.READSET_FULL);
				}
			} catch (Exception e) {
				Cat.logError(e);
				break;
			}
			model.setAlterationMinuites(generateAlterationMinutes(alts));
			break;
		}

		model.setAction(action);
		model.setPage(ReportPage.ALTERATION);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	public boolean isArguComplete(Payload payload) {
		if (StringUtils.isEmpty(payload.getType())) {
			return false;
		}
		if (StringUtils.isEmpty(payload.getTitle())) {
			return false;
		}
		if (StringUtils.isEmpty(payload.getDomain())) {
			return false;
		}
		if (StringUtils.isEmpty(payload.getHostname())) {
			return false;
		}
		if (payload.getAlterationDate() == null) {
			return false;
		}
		if (StringUtils.isEmpty(payload.getUser())) {
			return false;
		}
		if (StringUtils.isEmpty(payload.getContent())) {
			return false;
		}
		return true;
	}

	/**
	 * status code: 0-success 1-fail 2-fail(lack args)
	 * 
	 * @param model
	 * @param status
	 */
	public void setInsertResult(Model model, int status) {
		if (status == 0) {
			model.setInsertResult("{\"status\":200}");
		} else if (status == 1) {
			model.setInsertResult("{\"status\":500}");
		} else if (status == 2) {
			model.setInsertResult("{\"status\":500, \"errorMessage\":\"lack args\"}");
		}
	}

	public class AlterationDomain {

		private String m_name;

		private Map<String, List<Alteration>> m_alterationsByType = new HashMap<String, List<Alteration>>();

		public AlterationDomain(String domain) {
			m_name = domain;
		}

		public void add(Alteration alt) {
			String type = alt.getType();
			List<Alteration> alts = m_alterationsByType.get(type);

			if (alts == null) {
				alts = new ArrayList<Alteration>();

				m_alterationsByType.put(type, alts);
			}
			alts.add(alt);
		}

		public Map<String, List<Alteration>> getAlterationTypes() {
			return m_alterationsByType;
		}

		public int getCount() {
			int count = 0;

			for (List<Alteration> alts : m_alterationsByType.values()) {
				count += alts.size();
			}
			return count;
		}

		public String getName() {
			return m_name;
		}

	}

	public class AlterationMinute {

		private String m_date;

		private Map<String, AlterationDomain> m_domains = new HashMap<String, AlterationDomain>();

		public AlterationMinute(String dateStr) {
			m_date = dateStr;
		}

		public void add(Alteration alt) {
			String domain = alt.getDomain();
			AlterationDomain alterationDomain = m_domains.get(domain);

			if (alterationDomain == null) {
				alterationDomain = new AlterationDomain(domain);

				m_domains.put(domain, alterationDomain);
			}
			alterationDomain.add(alt);
		}

		public List<AlterationDomain> getAlterationDomains() {
			List<AlterationDomain> domains = new ArrayList<AlterationDomain>(m_domains.values());

			Collections.sort(domains, new Comparator<AlterationDomain>() {
				@Override
				public int compare(AlterationDomain o1, AlterationDomain o2) {
					return o2.getCount() - o1.getCount();
				}
			});
			return domains;
		}

		public String getDate() {
			return m_date;
		}

	}

}
