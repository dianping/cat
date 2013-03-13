<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.problem.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.problem.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.problem.Model" scope="request" />
<link rel="stylesheet" type="text/css" href="/cat/css/graph.css">
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/flotr2.js"></script>
<script type="text/javascript" src="/cat/js/baseTools.js"></script>
<script type="text/javascript" src="/cat/js/problemHistory.js"></script>
<script type="text/javascript" src="/cat/js/trendGraph.js"></script>
<style type="text/css">
.graph {
	width: 550px;
	height: 250px;
	margin: 4px auto;
}
</style>

<table>
	<c:choose>
	<c:when test="${payload.reportType eq 'day'}">
		<tr>
			<td colspan="3">日报表：<a href="#" class="first">&nbsp;&nbsp;</a>表示${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm')} ~ ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm')}；<a href="#" class="second">&nbsp;&nbsp;</a>表示上一天；<a href="#"  class="third">&nbsp;&nbsp;</a>表示上周这一天</td>
		</tr>
	</c:when></c:choose><c:choose>
	<c:when test="${payload.reportType eq 'week'}">
		<tr>
			<td colspan="3">周报表：<a href="#" class="first">&nbsp;&nbsp;</a>表示当前From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm')}；<a href="#" class="second">&nbsp;&nbsp;</a>表示上一周</td>
		</tr>
	</c:when></c:choose>
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
