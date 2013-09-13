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
<res:useJs value="${res.js.local['svgchart.latest.min.js']}" target="head-js"/>
<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>

<script type="text/javascript">
	$(document).ready(function() {
		$('#browser').addClass('active');
	});
</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title">&nbsp;&nbsp;From ${w:format(model.browserReport.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.browserReport.endTime,'yyyy-MM-dd HH:mm:ss')}</td>
			<td class="switch"><a href="?op=historyBrowser">Switch To History Mode</a>
			</td>
			<td class="nav" >
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
			</td>
		</tr>
	</table>
</div>
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
		<script type="text/javascript">
			var data1 = ${model.pieChart1};
			graphPieChart(document.getElementById('browserGraph'), data1);
			var data2 = ${model.pieChart2};
			graphPieChart(document.getElementById('osGraph'), data2);
		</script>
	
<div class="report">
	<table  class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>
</a:body>
