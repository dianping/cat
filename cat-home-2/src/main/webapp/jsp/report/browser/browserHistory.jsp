<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.statistics.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.statistics.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.statistics.Model" scope="request"/>

<a:browserHistoryReport title="History Report" navUrlPrefix="op=historyBrowser&domain=${model.domain}&date=${model.date}" timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">
<jsp:attribute name="subtitle">From ${w:format(model.browserReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.browserReport.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

<jsp:body>
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>
<script type="text/javascript">
	$(document).ready(function() {
		$('#browser').addClass('active');
	});
</script>
	<div class="row-fluid">
    	<div class="span2">
			<%@include file="../bugTree.jsp"%>
		</div>
		<div class="span10">
			<div class="report">
				<%@ include file="detail.jsp"%>
			</div>
		</div>
	</div>
</jsp:body>
</a:browserHistoryReport>
