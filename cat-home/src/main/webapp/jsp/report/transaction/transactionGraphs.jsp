<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.transaction.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.transaction.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.transaction.Model" scope="request" />
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/highcharts.js"></script>
<script type="text/javascript" src="/cat/js/baseGraph.js"></script>
<script type="text/javascript" src="/cat/js/transaction.js"></script>

<style type="text/css">
.graph {
	width: 500px;
	height: 300px;
	margin: 4px auto;
}
</style>
<svg version="1.1" width="980" height="380" xmlns="http://www.w3.org/2000/svg">
  ${model.graph1}
  ${model.graph2}
  ${model.graph3}
  ${model.graph4}
</svg>
<c:if test="${payload.ipAddress eq 'All' }">
<table  class='table table-hover table-striped table-condensed '  style="width:100%;">
	<tr><td colspan="8"><h5 style="text-align:center"  class='text-center text-info'>分布统计</h5></td></tr>
	<tr>
		<th class="right">Ip</th>
		<th class="right">Total</th>
		<th class="right">Failure</th>
		<th class="right">Failure%</th>
		<th class="right">Min(ms)</th>
		<th class="right">Max(ms)</th>
		<th class="right">Avg(ms)</th>
		<th class="right">Std(ms)</th>
	</tr>
	<c:forEach var="item" items="${model.distributionDetails}" varStatus="status">
	<tr class=" right">
		<td>${item.ip}</td>
		<td>${w:format(item.totalCount,'#,###,###,###,##0')}</td>
		<td>${w:format(item.failCount,'#,###,###,###,##0')}</td>
		<td>${w:format(item.failPercent/100,'0.0000%')}</td>
		<td>${w:format(item.min,'###,##0.#')}</td>
		<td>${w:format(item.max,'###,##0.#')}</td>
		<td>${w:format(item.avg,'###,##0.0')}</td>
		<td>${w:format(item.std,'###,##0.0')}</td>
	</tr>
	</c:forEach>
</table>
<br>

<div id="distributionChart" class="pieChart"></div>
<div id ="distributionChartMeta" style="display:none">${model.distributionChart}</div>
<script type="text/javascript">
	var distributionChartMeta = ${model.distributionChart};
	
	if(distributionChartMeta!=null){
		graphPieChart(document.getElementById('distributionChart'), distributionChartMeta);
	}
</script>
</c:if>

<br/>
