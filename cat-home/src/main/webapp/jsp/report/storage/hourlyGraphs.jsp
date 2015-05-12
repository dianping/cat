<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.storage.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.storage.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.storage.Model" scope="request" />
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/highcharts.js"></script>
<script type="text/javascript" src="/cat/js/baseGraph.js"></script>
<style type="text/css">
.graph {
	width: 100%;
	height: 300px;
}
</style>

<table width="100%">
	<tr>
		<td width="50%"><h5 style="text-align:center"  class='text-center text-info'>错误量</h5>
		<div id="errorTrend" class="graph"></div></td>
		<td width="50%"><h5 style="text-align:center"  class='text-center text-info'>响应时间</h5>
		<div id="avgTrend" class="graph"></div></td>
	</tr>
	<tr>
		<td width="50%"><h5 style="text-align:center"  class='text-center text-info'>操作量</h5>
		<div id="countTrend" class="graph"></div></td>
		<td width="50%"><h5 style="text-align:center"  class='text-center text-info'>长响应</h5>
		<div id="longTrend" class="graph"></div></td>
	</tr>
	<c:if test="${payload.ipAddress eq 'All' and payload.project eq 'All' and model.distributionChart != null}">
	<tr><td colspan="2" width="90%"><h5 style="text-align:center"  class='text-center text-info'>错误分布</h5>
		<div id="piechart" class="graph"></div></td></tr>
	<tr><td  style="display:none">
		<div id ="piechartMeta">${model.distributionChart}</div>
	</td>
	</tr>
	</c:if>
	<tr><td  style="display:none">
		<div id ="longTrendMeta">${model.longTrend}</div>
	</td>
	<tr><td  style="display:none">
		<div id ="errorTrendMeta">${model.errorTrend}</div>
	</td>
	<tr><td  style="display:none">
		<div id ="countTrendMeta">${model.countTrend}</div>
	</td>
	</tr>
	<tr><td  style="display:none">
		<div id ="avgTrendMeta">${model.avgTrend}</div>
	</td>
	</tr>
</table>
<script type="text/javascript">
	var countTrend = ${model.countTrend};
	graphLineChart(document.getElementById('countTrend'), countTrend);
	var avgTrend = ${model.avgTrend};
	graphLineChart(document.getElementById('avgTrend'), avgTrend);
	var errorTrend = ${model.errorTrend};
	graphLineChart(document.getElementById('errorTrend'), errorTrend);
	var longTrend = ${model.longTrend};
	graphLineChart(document.getElementById('longTrend'), longTrend);
	<c:if test="${payload.ipAddress eq 'All' and payload.project eq 'All' and model.distributionChart != null}">
		var piechart = ${model.distributionChart};
		graphPieChart(document.getElementById('piechart'), piechart);
	</c:if>
</script>
