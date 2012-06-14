<%@ page contentType="text/html; charset=utf-8"%>
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<style type="text/css">
.graph {
	width: 550px;
	height: 250px;
	margin: 4px auto;
}
</style>
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/flotr2.js"></script>
<script type="text/javascript" src="/cat/js/transactionGraph.js"></script>

<table>
	<tr>
		<td><div id="errorTrend" class="graph"></div></td>
	</tr>
	<tr><td  style="display:none">
		<div id ="errorTrendMeta">${model.hitTrend}</div>
	</td></tr>
</table>
<script type="text/javascript">
	var errorData = ${model.errorTrend};
	graph(document.getElementById('errorTrend'), errorData);
</script>
