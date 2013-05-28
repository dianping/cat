<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<link rel="stylesheet" type="text/css" href="/cat/css/graph.css">
<script type="text/javascript" src="/cat/js/svgchart.latest.min.js"></script>
<script type="text/javascript" src="/cat/js/baseGraph.js"></script>
<table>
	<tr>
		<td>
			<h5 style="text-align:center" class='text-center text-info'>访问量</h5>
			<div id="responseTrend" class="graph"></div></td>
		<td><h5 style="text-align:center"  class='text-center text-info'>错误量</h5>
			<div id="hitTrend" class="graph"></div></td>
		<td><h5 style="text-align:center"  class='text-center text-info'>响应时间</h5>
			<div id="errorTrend" class="graph"></div></td>
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
