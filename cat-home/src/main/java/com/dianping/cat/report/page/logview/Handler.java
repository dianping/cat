package com.dianping.cat.report.page.logview;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.webres.helper.Files;
import org.unidal.webres.helper.Joiners;

import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.report.ReportPage;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "m")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "m")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.LOGVIEW);

		Payload payload = ctx.getPayload();
		String[] path = payload.getPath();

		if (path != null && path.length > 0) {
			File baseDir = m_pathBuilder.getLogViewBaseDir();
			String relativePath = Joiners.by('/').join(path);
			File file = new File(baseDir, relativePath);

			if (file.exists()) {
				String content = Files.forIO().readFrom(file, "utf-8");

				model.setTable(content);
			}
		}

		m_jspViewer.view(ctx, model);
	}
}
