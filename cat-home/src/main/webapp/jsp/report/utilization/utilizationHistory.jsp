<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.statistics.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.statistics.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.statistics.Model" scope="request"/>

<a:body>
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>

<script type="text/javascript">
		$(document).ready(function() {
			$('#utilization').addClass('active');
			initTable($('#web_content'));
			initTable($('#service_content'));
		});
	</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title"><span class="text-success"><span class="text-error">【报表时间】</span>&nbsp;&nbsp;From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</td>
			</td>
			<td class="nav">
					<a class="switch" href="?domain=${model.domain}&op=utilization"><span class="text-error">【切到小时模式】</span></a>
					<c:forEach var="nav" items="${model.historyNavs}">
					<c:choose>
						<c:when test="${nav.title eq model.reportType}">
								&nbsp;&nbsp;[ <a href="?op=historyUtilization&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}" class="current">${nav.title}</a> ]
						</c:when>
						<c:otherwise>
								&nbsp;&nbsp;[ <a href="?op=historyUtilization&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}">${nav.title}</a> ]&nbsp;&nbsp;
						</c:otherwise>
					</c:choose>
				</c:forEach>
				&nbsp;&nbsp;[ <a href="?op=historyUtilization&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${model.reportType}&step=-1">${model.currentNav.last}</a> ]&nbsp;&nbsp;
				&nbsp;&nbsp;[ <a href="?op=historyUtilization&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${model.reportType}&step=1">${model.currentNav.next}</a> ]&nbsp;&nbsp;
				&nbsp;&nbsp;[ <a href="?op=historyUtilization&domain=${model.domain}&ip=${model.ipAddress}&reportType=${model.reportType}&nav=next">now</a> ]&nbsp;&nbsp;
			</td>
		</tr>
	</table>
	<%@ include file="detail.jsp"%>
</div>
</a:body>
