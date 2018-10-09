<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.event.Context"	scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.event.Payload"	scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.event.Model" scope="request" />
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/highcharts.js"></script>
<script type="text/javascript" src="/cat/js/baseGraph.js"></script>
<style type="text/css">
.graph {
	width: 550px;
	height: 350px;
	margin: 4px auto;
}
</style>
<table>
	<tr>
		<td>
		<div id="hitTrend" class="graph"></div></td>
		<td>
		<div id="failureTrend" class="graph"></div></td>
	</tr>
	<tr><td  style="display:none">
		<div id ="hitTrendMeta">${model.hitTrend}</div>
		<div id ="failureTrendMeta">${model.failureTrend}</div>
	</td></tr>
</table>


<script type="text/javascript">
	var hitTrendData = ${model.hitTrend};
	graphLineChart(document.getElementById('hitTrend'), hitTrendData);
	var failureTrendData = ${model.failureTrend};
	graphLineChart(document.getElementById('failureTrend'), failureTrendData);
</script>
<c:if test="${payload.ipAddress eq 'All' }">
<table  class='table table-hover table-striped table-condensed '  style="width:100%;">
	<tr><td colspan="8"><h5 style="text-align:center"  class='text-center text-info'>分布统计</h5></td></tr>
	<tr>
		<th class="right">Ip</th>
		<th class="right">Total</th>
		<th class="right">Failure</th>
		<th class="right">Failure%</th>
	</tr>
	<c:forEach var="item" items="${model.distributionDetails}" varStatus="status">
	<tr class=" right">
		<td>${item.ip}</td>
		<td>${w:format(item.totalCount,'#,###,###,###,##0')}</td>
		<td>${w:format(item.failCount,'#,###,###,###,##0')}</td>
		<td>${w:format(item.failPercent/100,'0.0000%')}</td>
	</tr>
	</c:forEach>
</table>
<br>

<div id="distributionChart" class="pieChart"></div>
<div id ="distributionChartMeta" style="display:none">${model.distributionChart}</div>
<script>
	var distributionChartMeta = ${model.distributionChart};
	if(distributionChartMeta!=null){
		graphPieChart(document.getElementById('distributionChart'), distributionChartMeta);
	}
</script>
</c:if>
<br>

