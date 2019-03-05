<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="model"	type="com.dianping.cat.report.page.state.Model" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.state.Payload" scope="request" />
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/highcharts.js"></script>
<script type="text/javascript" src="/cat/js/baseGraph.js"></script>

<style type="text/css">
.graph {
 	width: 47%;
	height: 400px;
    margin: 0px;
}
</style>
<c:choose>
<c:when test="${payload.ipAddress eq 'All' and payload.key ne 'delayAvg'}">
<table>
	<tr>
		<td width="50%"><div id="trendGraph" class="graph"></div></td>
		<td width="50%"><div id="distributionChart" class="graph"></div></td>
	</tr>
	<tr>
	<td  style="display:none">
		<div id ="trendMeta">${model.graph}</div>
	</td>
	<td>
	<div id ="distributionMeta" style="display:none">${model.pieChart}</div>
	</td></tr>
</table>
</c:when>
<c:otherwise>
<table>
	<tr>
		<td><div id="trendGraph" class="graph"></div></td>
	</tr>
	<tr>
	<td  style="display:none">
		<div id ="trendMeta">${model.graph}</div>
	</td>
	</tr>
</table>
</c:otherwise>
</c:choose>
<script type="text/javascript">
	var data = ${model.graph};
	graphLineChart(document.getElementById('trendGraph'), data);	
	
	data = ${model.pieChart};
	
	if (data != null && data.length > 0) {
		graphPieChart($('#distributionChart', cell)[0], eval('(' + data + ')'));
	}
</script>
