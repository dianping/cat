package com.dianping.cat.report.page.test;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;

import com.dianping.cat.home.dal.report.Test;
import com.dianping.cat.home.dal.report.TestDao;
import com.dianping.cat.home.dal.report.TestEntity;
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
	private TestDao m_testDao;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "test")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "test")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();
		
		Test test = m_testDao.createLocal();
		String name = payload.getName();
		test.setName(name);
		
		switch (action) {
		case INSERT:
			try {
	         m_testDao.insert(test);
	         model.setName(name);
         } catch (DalException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
         }
			break;
		case QUERY:
			try {
	         Test result = m_testDao.queryAll(TestEntity.READSET_FULL);
	         model.setTestList(result);
         } catch (DalException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
         }
			break;
		default:
			throw new RuntimeException("Unknown action: " + action);			
		}

		model.setAction(action);
		model.setPage(ReportPage.TEST);

		if (!ctx.isProcessStopped()) {
		   m_jspViewer.view(ctx, model);
		}
	}
}
