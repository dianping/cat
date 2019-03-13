<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.applog.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.applog.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.applog.Model" scope="request" />
	
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
	<tr><td><div id="appVersions" class="graph"></div></td>
		<td><div id="platformVersions" class="graph"></div></td></tr>
	<tr>
		<td><div id="devices" class="graph"></div></td></tr>	
	<tr><td  style="display:none">
		<div id ="appVersionsMeta">${model.appLogDisplayInfo.msgDistributions['appVersions'].jsonString}</div>
	</td>
	<td  style="display:none">
		<div id ="platformVersionsMeta">${model.appLogDisplayInfo.msgDistributions['platformVersions'].jsonString}</div>
	</td>
	</tr>	
	<tr>
	<td  style="display:none">
		<div id ="devicesMeta">${model.appLogDisplayInfo.msgDistributions['devices'].jsonString}</div>
	</td>
	</tr>			
</table>

<script type="text/javascript">
$(document).ready(
	function(){
		graphPieChartWithName(document.getElementById('appVersions'), ${model.appLogDisplayInfo.msgDistributions['appVersions'].jsonString},  'APP版本分布');
		graphPieChartWithName(document.getElementById('platformVersions'), ${model.appLogDisplayInfo.msgDistributions['platformVersions'].jsonString},  '平台版本分布');
		graphPieChartWithName(document.getElementById('devices'), ${model.appLogDisplayInfo.msgDistributions['devices'].jsonString},  '设备分布');
});
</script>