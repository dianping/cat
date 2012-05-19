<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.heatmap.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.heatmap.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.heatmap.Model" scope="request" />

<a:report title="HeatMap Report" navUrlPrefix="domain=${model.domain}"
	timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">
<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
<jsp:body>

<br>
</jsp:body>
</a:report>