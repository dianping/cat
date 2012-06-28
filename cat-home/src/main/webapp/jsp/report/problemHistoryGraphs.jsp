<%@ page contentType="text/html; charset=utf-8"%>
<jsp:useBean id="model"	type="com.dianping.cat.report.page.problem.Model" scope="request" />
<style type="text/css">
.graph {
	width: 550px;
	height: 250px;
	margin: 4px auto;
}
</style>
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/flotr2.js"></script>
<script type="text/javascript" src="/cat/js/baseTools.js"></script>
<script type="text/javascript" src="/cat/js/problemHistory.js"></script>

<table>
	<tr>
		<td><div id="errorTrend" class="graph"></div></td>
	</tr>
	<tr><td  style="display:none">
		<div id ="errorTrendMeta">${model.errorsTrend}</div>
	</td></tr>
</table>
<script type="text/javascript">
	var errorData = ${model.errorsTrend};
	graph(document.getElementById('errorTrend'), errorData);
</script>
