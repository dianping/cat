package com.dianping.cat.report.page.task;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.hadoop.dal.TaskEntity;
import com.dianping.cat.report.ReportPage;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;
	
	@Inject
	private TaskDao taskDao;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "task")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "task")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload=ctx.getPayload();
		//TODO initialize 
//		initialize(payload,model);
		
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.TASK);
		m_jspViewer.view(ctx, model);
	}
	
	//TODO getTotal page
	public int getTotalPage(Payload payload){
		int pagesize=payload.getPagesize();
		//TODO getAll conut;
		int sum=0;//taskDao.getAllbyStartAndEnd();
		
		return (int) Math.floor((double)sum/(double)pagesize);
	}
	
	public void initialize(Payload payload,Model model){
		List<String> domains=null;
		try {
	      taskDao.findAllReportName(TaskEntity.READSET_REPORT_NAME);
//	      model.setDomains(domains);
			List<String> names=null;
//			model.setNames(names);
      } catch (DalException e) {
	      e.printStackTrace();
      }
		
	}
}
