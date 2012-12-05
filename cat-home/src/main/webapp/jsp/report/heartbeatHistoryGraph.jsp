<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.heartbeat.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.heartbeat.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.heartbeat.Model" scope="request" />

<a:historyReport title="HeartBeat History Report">
	<jsp:attribute name="subtitle">From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	<res:useCss value="${res.css.local.transaction_css}" target="head-css" />
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['flotr2_js']}" target="head-js"/>
	<res:useJs value="${res.js.local['baseTools_js']}" target="head-js"/>
	<res:useJs value="${res.js.local['trendGraph_js']}" target="head-js"/>
	<res:useJs value="${res.js.local['heartbeatHistory_js']}" target="head-js"/>
<br>
<table class="machines">
	<tr style="text-align: left">
		<th>Machines:
   	  		 <c:forEach var="ip" items="${model.ips}">&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&reportType=${model.reportType}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&reportType=${model.reportType}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<br>
<table class="graph" id="graph">
<tr>
	<th	colspan="3">Thread Info</th>
</tr>
<tr>
	
	<td><div id="ActiveThread" class="graph"></div></td>
	<td><div id="StartedThread" class="graph"></div></td>
	<td><div id="TotalStartedThread" class="graph"></div></td>
</tr>
<tr>
	
	<td><div id="HttpStartedThread" class="graph"></div></td>
	<td><div id="CatStartedThread" class="graph"></div></td>
	<td><div id="PigeonStartedThread" class="graph"></div></td>
</tr>

<tr>
	<th colspan="3">System Info</th>
</tr>
<tr>
	
	<td><div id="NewGcCount" class="graph"></div></td>
	<td><div id="OldGcCount" class="graph"></div></td>
	<td><div id="SystemLoadAverage" class="graph"></div></td>
</tr>
<tr>
	<th colspan="3">Memery Info</th>
</tr>
<tr id="memoryGraph">
	<td><div id="MemoryFree" class="graph"></div></td>
	<td><div id="HeapUsage" class="graph"></div></td>
	<td><div id="NoneHeapUsage" class="graph"></div></td>
</tr>

<tr>
	<th colspan="3">Cat Message Info</th>
</tr>
<tr>
	<td><div id="CatMessageProduced" class="graph"></div></td>
	<td><div id="CatMessageOverflow" class="graph"></div></td>
	<td><div id="CatMessageSize" class="graph"></div></td>
</tr>
</table>
<script>
	//01
	var activeThreadGraphData = ${model.activeThreadGraph};
	graphOnMinute(document.getElementById('ActiveThread'), activeThreadGraphData);
	//02
	var startedThreadGraphData = ${model.startedThreadGraph};
	graphOnMinute(document.getElementById('StartedThread'), startedThreadGraphData);
	//03
	var totalThreadGraphData = ${model.totalThreadGraph};
	graphOnMinute(document.getElementById('TotalStartedThread'), totalThreadGraphData);
	//04
	var httpThreadGraphData = ${model.httpThreadGraph};
	graphOnMinute(document.getElementById('HttpStartedThread'), httpThreadGraphData);
	//05
	var catThreadGraphData = ${model.catThreadGraph};
	graphOnMinute(document.getElementById('CatStartedThread'), catThreadGraphData);
	//06
	var pigeonThreadGraphData = ${model.pigeonThreadGraph};
	graphOnMinute(document.getElementById('PigeonStartedThread'), pigeonThreadGraphData);
	//07
	var newGcCountGraphData = ${model.newGcCountGraph};
	graphOnMinute(document.getElementById('NewGcCount'), newGcCountGraphData);
	//08
	var oldGcCountGraphData = ${model.oldGcCountGraph};
	graphOnMinute(document.getElementById('OldGcCount'), oldGcCountGraphData);
	//09
	var systemLoadAverageGraphData = ${model.systemLoadAverageGraph};
	graphOnMinute(document.getElementById('SystemLoadAverage'), systemLoadAverageGraphData);
	//10
	var memoryFreeGraphData = ${model.memoryFreeGraph};
	graphOnMinute(document.getElementById('MemoryFree'), memoryFreeGraphData);
	//11
	var heapUsageGraphData = ${model.heapUsageGraph};
	graphOnMinute(document.getElementById('HeapUsage'), heapUsageGraphData);
	//12
	var noneHeapUsageGraphData = ${model.noneHeapUsageGraph};
	graphOnMinute(document.getElementById('NoneHeapUsage'), noneHeapUsageGraphData);
	//19
	
	//16
	var catMessageProducedGraphData = ${model.catMessageProducedGraph};
	graphOnMinute(document.getElementById('CatMessageProduced'), catMessageProducedGraphData);
	//17
	var catMessageOverflowGraphData = ${model.catMessageOverflowGraph};
	graphOnMinute(document.getElementById('CatMessageOverflow'), catMessageOverflowGraphData);
	//18
	var catMessageSizeGraphData = ${model.catMessageSizeGraph};
	graphOnMinute(document.getElementById('CatMessageSize'), catMessageSizeGraphData);

	var diskHistoryGraph=${model.diskHistoryGraph};
	var size=${model.disks};
	disksGraph(size,diskHistoryGraph);
	
</script>
</jsp:body>

</a:historyReport>