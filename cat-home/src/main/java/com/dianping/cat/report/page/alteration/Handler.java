package com.dianping.cat.report.page.alteration;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.home.dal.report.AlterationDao;
import com.dianping.cat.home.dal.report.AlterationEntity;
import com.dianping.cat.report.ReportPage;
import com.site.lookup.util.StringUtils;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AlterationDao m_alterationDao;

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

			try {
				alts = m_alterationDao.findByDtdh(startTime, endTime, payload.getType(), payload.getDomain(),
				      payload.getHostname(), AlterationEntity.READSET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				break;
			}

			model.setAlterations(alts);
			break;
		}

		model.setAction(action);
		model.setPage(ReportPage.ALTERATION);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

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
		alt.setUrl(url);
		alt.setHostname(hostname);
		alt.setDate(date);
		return alt;
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
			model.setInsertResult("{\"status\":200, \"errorMessage\":\"lack args\"}");
		}
	}
	
}
