<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<jsp:useBean id="model"	type="com.dianping.cat.report.page.health.Model" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.health.Payload" scope="request" />
<style type="text/css">
.graph {
	width: 450px;
	height: 200px;
	margin: 4px auto;
}
</style>
<script type="text/javascript" src="/cat/js/svgchart.latest.min.js"></script>
<script type="text/javascript" src="/cat/js/baseGraph.js"></script>

<table>
	<tr>
		<td><div id="trendGraph" class="graph"></div></td>
	</tr>
	<tr>
	<td  style="display:none">
		<div id ="trendMeta">${model.historyGraph}</div>
		<div id ="reportType">${payload.reportType}</div>
	</td>
	</tr>
</table>
<script type="text/javascript">
	var data = ${model.historyGraph};
	var type =${payload.reportType};
	type = type.trim();
	if(type=='day'){
		graphLineChart(document.getElementById('trendGraph'), data);	
	}else if{
		graphLineChart(document.getElementById('trendGraph'), data);	
	}
</script>
