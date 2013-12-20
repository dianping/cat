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
<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>

<script type="text/javascript">
	$(document).ready(function() {
		$('#utilization').addClass('active');
		initTable($('#web_content'));
		initTable($('#service_content'));
		$('i[tips]').popover();
	});
</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title text-success"><span class="text-success"><span class="text-error">【报表时间】</span>&nbsp;&nbsp;From ${w:format(model.utilizationReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.utilizationReport.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
			</td>
			<td class="nav" >
				<a href="?op=historyUtilization&domain=${model.domain}" class="switch"><span class="text-error">【切到历史模式】</span></a>
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&op=utilization">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?op=utilization">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
</div>

<%@ include file="detail.jsp"%>
</a:body>
