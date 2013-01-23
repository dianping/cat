<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.event.Context"	scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.event.Payload"	scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.event.Model" scope="request" />
<link rel="stylesheet" type="text/css" href="/cat/css/graph.css">
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/flotr2.js"></script>
<script type="text/javascript" src="/cat/js/baseTools.js"></script>
<script type="text/javascript" src="/cat/js/trendGraph.js"></script>
<style type="text/css">
.graph {
	width: 550px;
	height: 250px;
	margin: 4px auto;
}
</style>

<table><c:choose>
	<c:when test="${payload.reportType eq 'day'}">
		<tr>
			<td colspan="3">日报表：<a href="#" class="first">&nbsp;&nbsp;</a>表示${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm')} ~ ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm')}；<a href="#" class="second">&nbsp;&nbsp;</a>表示上一天；<a href="#"  class="third">&nbsp;&nbsp;</a>表示上周这一天</td>
		</tr>
	</c:when></c:choose><c:choose>
	<c:when test="${payload.reportType eq 'week'}">
		<tr>
			<td colspan="3">周报表：<a href="#" class="first">&nbsp;&nbsp;</a>表示当前From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm')}；<a href="#" class="second">&nbsp;&nbsp;</a>表示上一周</td>
		</tr>
	</c:when></c:choose>
	<tr>
		<td><div id="hitTrend" class="graph"></div></td>
		<td><div id="failureTrend" class="graph"></div></td>
	</tr>
	<tr><td  style="display:none">
		<div id ="hitTrendMeta">${model.hitTrend}</div>
		<div id ="failureTrendMeta">${model.failureTrend}</div>
	</td></tr>
</table>


<script type="text/javascript">
	var hitTrendData = ${model.hitTrend};
	graph(document.getElementById('hitTrend'), hitTrendData);
	var failureTrendData = ${model.failureTrend};
	graph(document.getElementById('failureTrend'), failureTrendData);
</script>
