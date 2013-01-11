<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<jsp:useBean id="model"	type="com.dianping.cat.report.page.state.Model" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.state.Payload" scope="request" />
<style type="text/css">
.graph {
	width: 450px;
	height: 200px;
	margin: 4px auto;
}
</style>
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/flotr2.js"></script>
<script type="text/javascript" src="/cat/js/trendStateGraph.js"></script>

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
<script type="text/javascript">
	var data = ${model.graph};
	graph(document.getElementById('trendGraph'), data);	
</script>
