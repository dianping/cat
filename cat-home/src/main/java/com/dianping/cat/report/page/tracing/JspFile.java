package com.dianping.cat.report.page.tracing;

/**
 * TODO
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.x
 */
public enum JspFile {

    LOGVIEW("/jsp/report/tracing/tracing.jsp");

    private String path;

    JspFile(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}