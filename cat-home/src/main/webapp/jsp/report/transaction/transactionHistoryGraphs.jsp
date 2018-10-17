<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/highcharts.js"></script>
<script type="text/javascript" src="/cat/js/baseGraph.js"></script>
<script type="text/javascript" src="/cat/js/transaction.js"></script>
<style type="text/css">
.graph {
	width: 400px;
	height: 300px;
	margin: 4px auto;
}
</style>
<table>
	<tr>
		<td>
			<div id="responseTrend" class="graph"></div></td>
		<td>
			<div id="hitTrend" class="graph"></div></td>
		<td>
			<div id="errorTrend" class="graph"></div></td>
		<td>
	</tr>
	<tr><td  style="display:none">
		<div id ="responseTrendMeta">${model.responseTrend}</div>
		<div id ="hitTrendMeta">${model.hitTrend}</div>
		<div id ="errorTrendMeta">${model.errorTrend}</div>
	</td></tr>
</table>

<script type="text/javascript">
	var responseTrendData = ${model.responseTrend};
	var hitTrendData = ${model.hitTrend};
	var errorTrendData = ${model.errorTrend};
	graphLineChart(document.getElementById('responseTrend'),responseTrendData);
	graphLineChart(document.getElementById('hitTrend'),hitTrendData);
	graphLineChart(document.getElementById('errorTrend'),errorTrendData);
</script>
<c:if test="${payload.ipAddress eq 'All' }">
<table  class='table table-hover table-striped table-condensed '  style="width:60%;">
	<tr><td colspan="8"><h5 style="text-align:center"  class='text-center text-info'>分布统计</h5></td></tr>
	<tr>
		<th class="right">Ip</th>
		<th class="right">Total</th>
		<th class="right">Failure</th>
		<th class="right">Failure%</th>
		<th class="right">Min(ms)</th>
		<th class="right">Max(ms)</th>
		<th class="right">Avg(ms)</th>
		<th class="right">Std(ms)</th>
	</tr>
	<c:forEach var="item" items="${model.distributionDetails}" varStatus="status">
	<tr class=" right">
		<td>${item.ip}</td>
		<td>${w:format(item.totalCount,'#,###,###,###,##0')}</td>
		<td>${w:format(item.failCount,'#,###,###,###,##0')}</td>
		<td>${w:format(item.failPercent/100,'0.0000%')}</td>
		<td>${w:format(item.min,'###,##0.#')}</td>
		<td>${w:format(item.max,'###,##0.#')}</td>
		<td>${w:format(item.avg,'###,##0.0')}</td>
		<td>${w:format(item.std,'###,##0.0')}</td>
	</tr>
	</c:forEach>
</table>
<br>
<div id="distributionChart" class="pieChart"></div>
<div id ="distributionChartMeta" style="display:none">${model.distributionChart}</div>
<script type="text/javascript">
	graphPieChart(document.getElementById('distributionChart'), ${model.distributionChart});
</script>
</c:if>
