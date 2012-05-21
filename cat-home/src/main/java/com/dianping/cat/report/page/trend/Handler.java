package com.dianping.cat.report.page.trend;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.report.ReportPage;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "trend")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "trend")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.TREND);
		
		GraphItem item = new GraphItem();
		item.setTitles("This is a test!");
		String[] xlable = new String[192];
		double[] ylable1 = new double[192];
		double[] ylable2 = new double[192];
		double[] ylable3 = new double[192];
		for(int i = 0;i<192;i++){
			xlable[i] = String.valueOf(i);
			ylable1[i] = Math.random()*192;
			ylable2[i] = Math.random()*192;
			ylable3[i] = Math.random()*192;
		}
		item.setXlabel(xlable);
		item.setYlable(new double[]{0,2,4,6,8,10});
		item.addValue(ylable1);
		item.addValue(ylable2);
		item.addValue(ylable3);
		item.addSubTitle("test1").addSubTitle("test2").addSubTitle("test3");
		model.setGraph(item.getJsonString());
		m_jspViewer.view(ctx, model);
	}
}
