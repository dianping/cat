<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.problem.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.problem.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.problem.Model" scope="request" />
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/highcharts.js"></script>
<script type="text/javascript" src="/cat/js/baseGraph.js"></script>
<style type="text/css">
.graph {
	width: 500px;
	height: 300px;
	margin: 4px auto;
}
</style>

<table>
	<tr>
		<td><h5 style="text-align:center"  class='text-center text-info'>错误量</h5>
		<div id="errorTrend" class="graph"></div></td>
		<c:if test="${payload.ipAddress eq 'All' }">
		<td><h5 style="text-align:center"  class='text-center text-info'>错误分布</h5>
		<div id="distributionChart" class="graph"></div></td>
		</c:if>
	</tr>
	<tr><td  style="display:none">
		<div id ="errorTrendMeta">${model.errorsTrend}</div>
	</td>
	<td  style="display:none">
		<div id ="distributionChartMeta">${model.distributionChart}</div>
	</td>
	</tr>
</table>
<script type="text/javascript">
	var errorData = ${model.errorsTrend};
	graphLineChart(document.getElementById('errorTrend'), errorData);
</script>
<c:if test="${payload.ipAddress eq 'All' }">
	<script>
	var distributionChart = ${model.distributionChart};

	if(distributionChart!=null){
		graphPieChart(document.getElementById('distributionChart'), distributionChart);
	}
	</script>
</c:if>
