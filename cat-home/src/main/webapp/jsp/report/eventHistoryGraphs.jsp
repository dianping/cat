<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.event.Context"	scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.event.Payload"	scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.event.Model" scope="request" />
<link rel="stylesheet" type="text/css" href="/cat/css/graph.css">
<script type="text/javascript" src="/cat/js/svgchart.latest.min.js"></script>
<script type="text/javascript" src="/cat/js/baseGraph.js"></script>
<style type="text/css">
.graph {
	width: 550px;
	height: 250px;
	margin: 4px auto;
}
</style>
<table>
	<tr>
		<td><h5 style="text-align:center" class='text-center text-info'>访问量</h5>
		<div id="hitTrend" class="graph"></div></td>
		<td><h5 style="text-align:center"  class='text-center text-info'>错误量</h5>
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
