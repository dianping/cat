<%@ page contentType="text/html; charset=utf-8"%>
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
	width: 450px;
	height: 350px;
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
