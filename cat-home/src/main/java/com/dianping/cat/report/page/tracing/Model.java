package com.dianping.cat.report.page.tracing;

import com.dianping.cat.report.page.logview.Context;
import org.unidal.web.mvc.view.annotation.ModelMeta;

/**
 * TODO
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 4.0
 */
@ModelMeta(Constants.TRACE_META_NAME)
public class Model extends com.dianping.cat.report.page.logview.Model {

    private String traceId;

    public Model(Context ctx) {
        super(ctx);
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
