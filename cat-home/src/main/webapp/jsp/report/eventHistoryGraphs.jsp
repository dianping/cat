<%@ page contentType="text/html; charset=utf-8"%>
<jsp:useBean id="model"	type="com.dianping.cat.report.page.event.Model" scope="request" />
<style type="text/css">
.graph {
	width: 550px;
	height: 250px;
	margin: 4px auto;
}
</style>
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/flotr2.js"></script>
<script type="text/javascript" src="/cat/js/trendGraph.js"></script>

<table>
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
