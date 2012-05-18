package com.dianping.cat.report.page.heatmap;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.heatmap.LocationData.Location;
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
	@InboundActionMeta(name = "heatmap")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "heatmap")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		
		System.out.println(payload.getSw());
		System.out.println(payload.getNe());
		System.out.println(payload.getWidth());
		System.out.println(payload.getHeight());
		
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.HEATMAP);
		model.setCb(payload.getCb());
		LocationData data = new LocationData();
		setMockData(data);
		model.setData(data);
		m_jspViewer.view(ctx, model);
	}

	private void setMockData(LocationData data) {
		for (int i = 0; i < 2000; i++) {
			double x = 30 + i * 0.0005;
			double y = 120 + i * 0.0005;
			Location location = new Location(x, y, i);
			data.addLocationInfo(location);
		}
	}
}
