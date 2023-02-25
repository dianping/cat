package com.dianping.cat.report.page.tracing;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.logview.Action;
import com.dianping.cat.report.page.logview.Context;
import org.unidal.web.mvc.view.BaseJspViewer;

/**
 * TODO
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 4.0
 */
public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {

    @Override
    protected String getJspFilePath(Context ctx, Model model) {
        Action action = model.getAction();
        if (action == Action.VIEW) {
            return JspFile.LOGVIEW.getPath();
        }

        throw new RuntimeException("Unknown action: " + action);
    }
}