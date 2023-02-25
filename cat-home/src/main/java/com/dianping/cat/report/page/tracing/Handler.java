package com.dianping.cat.report.page.tracing;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.logview.Context;
import com.dianping.cat.report.page.logview.Payload;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * TODO
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.x
 */
public class Handler implements PageHandler<Context> {

    @Inject
    private JspViewer jspViewer;

    @PayloadMeta(Payload.class)
    @InboundActionMeta(name = Constants.TRACE_META_NAME)
    @Override
    public void handleInbound(Context context) throws ServletException, IOException {

    }

    @OutboundActionMeta(name = Constants.TRACE_META_NAME)
    @Override
    public void handleOutbound(Context context) throws ServletException, IOException {
        Model model = new Model(context);
        Payload payload = context.getPayload();
        model.setAction(payload.getAction());
        model.setPage(ReportPage.LOGVIEW);
        model.setDomain(payload.getDomain());
        model.setDate(payload.getDate());
        jspViewer.view(context, model);
    }
}