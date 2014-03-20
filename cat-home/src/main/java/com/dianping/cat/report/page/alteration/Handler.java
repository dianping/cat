package com.dianping.cat.report.page.alteration;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.Alteration;
import com.dianping.cat.home.dal.report.AlterationDao;
import com.dianping.cat.home.dal.report.AlterationEntity;
import com.dianping.cat.report.ReportPage;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;
	
	@Inject
	private AlterationDao m_alterationDao;
	
	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
		
		String type = payload.getType();
		String domain = payload.getDomain();
		String hostname = payload.getHostname();
				
		switch(action){
		case INSERT: 
			String title = payload.getTitle();
			String ip = payload.getIp();
			String user = payload.getUser();
			String content = payload.getContent();
			String url = payload.getUrl();
			String date = payload.getDate();			
			
			Alteration alt = new Alteration();	
			alt.setType(type);
			alt.setDomain(domain);
			alt.setTitle(title);
			alt.setIp(ip);
			alt.setUser(user);
			alt.setContent(content);
			alt.setUrl(url);		
			alt.setHostname(hostname);
			
			try {
				alt.setDate(m_sdf.parse(date));
				
				m_alterationDao.insert(alt);
				model.setStatus("{\"status\":200}");
			} catch (Exception e) {
				Cat.logError(e);
				model.setStatus("{\"status\":500}");	
			}
			break;
		case VIEW:
			String startTime = payload.getStartTime();
			String endTime = payload.getEndTime();
			long granularity = payload.getGranularity();
			
			try {
				m_alterationDao.findByDtdh(m_sdf.parse(startTime), m_sdf.parse(endTime), type, domain, hostname, AlterationEntity.READSET_FULL);
				model.setViewDataSuccess(true);
			} catch (Exception e) {
				Cat.logError(e);
				model.setViewDataSuccess(false);
				break;
			}			
			break;
		}
		if(action!=null)
			model.setAction(action);
		else
			model.setAction(Action.VIEW);
		model.setPage(ReportPage.ALTERATION);

		if (!ctx.isProcessStopped()) {
		   m_jspViewer.view(ctx, model);
		}
	}
}
