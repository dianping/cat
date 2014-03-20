package com.dianping.cat.report.page.alteration;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.report.ReportPage;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

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
		String title = payload.getTitle();
		String domain = payload.getDomain();
		String ip = payload.getIp();
		String user = payload.getUser();
				
		switch(action){
		case INSERT: 
			String content = payload.getContent();
			String url = payload.getUrl();
			
			/*Alteration alt = new Alteration();						
			try {
				return m_projectDao.findByDomain(domain, ProjectEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}*/

			model.setStatus("{\"status\":\"success\"}");
			model.setStatus("{\"status\":\"fail\"}");	
			break;
		case VIEW:
			String startTime = payload.getStartTime();
			String endTime = payload.getEndTime();
			long granularity = payload.getGranularity();
			
			//model.setResult(m_manager.getEmailPassword()+  " action2  " + domain+" " +ip);
			break;
		}

		model.setAction(action);
		model.setPage(ReportPage.ALTERATION);

		if (!ctx.isProcessStopped()) {
		   m_jspViewer.view(ctx, model);
		}
	}
}
