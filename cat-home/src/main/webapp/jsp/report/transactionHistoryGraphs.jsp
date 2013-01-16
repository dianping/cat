<%@ page contentType="text/html; charset=utf-8"%>
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<link rel="stylesheet" type="text/css" href="/cat/css/graph.css">
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/flotr2.js"></script>
<script type="text/javascript" src="/cat/js/baseTools.js"></script>
<script type="text/javascript" src="/cat/js/trendGraph.js"></script>

<table>
	<tr>
		<td colspan="3">日报表：<a href="#" class="first">&nbsp;&nbsp;</a>表示当前这一天；<a href="#" class="second">&nbsp;&nbsp;</a>表示上一天；<a href="#"  class="third">&nbsp;&nbsp;</a>表示上周这一天</td>
	</tr>
	<tr>
		<td colspan="3">周报表：<a href="#" class="first">&nbsp;&nbsp;</a>表示当前这一周；<a href="#" class="second">&nbsp;&nbsp;</a>表示上一周</td>
	</tr>
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
