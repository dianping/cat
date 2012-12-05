<%@ page contentType="text/html; charset=utf-8"%>
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<style type="text/css">
.graph {
	width: 450px;
	height: 200px;
	margin: 4px auto;
}
</style>
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/flotr2.js"></script>
<script type="text/javascript" src="/cat/js/baseTools.js"></script>
<script type="text/javascript" src="/cat/js/trendGraph.js"></script>

<table>
	<tr>
		<td><div id="responseTrend" class="graph"></div></td>
		<td><div id="hitTrend" class="graph"></div></td>
		<td><div id="errorTrend" class="graph"></div></td>
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
	graph(document.getElementById('responseTrend'), responseTrendData);	
	graph(document.getElementById('hitTrend'), hitTrendData);
	graph(document.getElementById('errorTrend'), errorTrendData);
</script>
