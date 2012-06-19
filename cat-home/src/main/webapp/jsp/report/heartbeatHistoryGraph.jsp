<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.heartbeat.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.heartbeat.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.heartbeat.Model" scope="request" />

<a:historyReport title="HeartBeat History Report">
	<jsp:attribute name="subtitle">From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	<res:useCss value="${res.css.local.transaction_css}" target="head-css" />
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['flotr2_js']}" target="head-js"/>
	<res:useJs value="${res.js.local['trendGraph_js']}" target="head-js"/>
</br>
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

<table class="graph">
<tr>
	<th	colspan="3">Thread Info</th>
</tr>
<tr>
	
	<td><div id="ActiveThread" class="graph"></div></td>
	<td><div id="DaemonThread" class="graph"></div></td>
	<td><div id="TotalStartedThread" class="graph"></div></td>
</tr>
<tr>
	
	<td><div id="StartedThread" class="graph"></div></td>
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
<tr>
	
	<td><div id="MemoryFree" class="graph"></div></td>
	<td><div id="HeapUsage" class="graph"></div></td>
	<td><div id="NoneHeapUsage" class="graph"></div></td>
</tr>
<tr>
	<td  style="display:none">
		<div id ="MemoryFreeData">${model.memoryFreeGraph}</div>
		<div id ="HeapUsageData">${model.heapUsageGraph}</div>
		<div id ="NoneHeapUsageData">${model.noneHeapUsageGraph}</div>
	</td>
</tr>
<tr>
	<th colspan="3">Disk Info</th>
</tr>
<tr>
	
	<td><div id="diskRoot" class="graph"></div></td>
	<td><div id="diskData" class="graph"></div></td>
</tr>
<tr>
	<th colspan="3">Cat Message Info</th>
</tr>
<tr>
	
	<td><div id="CatMessageProduced" class="graph"></div></td>
	<td><div id="CatMessageOverflow" class="graph"></div></td>
	<td><div id="CatMessageSize" class="graph"></div></td>
</tr>
<tr>
	<td  style="display:none">
		<div id ="CatMessageProducedData">${model.catMessageProducedGraph}</div>
		<div id ="CatMessageOverflowData">${model.catMessageOverflowGraph}</div>
		<div id ="CatMessageSizeData">${model.catMessageSizeGraph}</div>
	</td>
</tr>
</table>
<script>
	//01
	var activeThreadGraphData = ${model.activeThreadGraph};
	graph(document.getElementById('ActiveThread'), activeThreadGraphData);
	//02
	var daemonThreadGraphData = ${model.daemonThreadGraph};
	graph(document.getElementById('DaemonThread'), daemonThreadGraphData);
	//03
	var totalThreadGraphData = ${model.totalThreadGraph};
	graph(document.getElementById('TotalStartedThread'), totalThreadGraphData);
	//04
	var startedThreadGraphData = ${model.startedThreadGraph};
	graph(document.getElementById('StartedThread'), startedThreadGraphData);
	//05
	var catThreadGraphData = ${model.catThreadGraph};
	graph(document.getElementById('CatStartedThread'), catThreadGraphData);
	//06
	var pigeonThreadGraphData = ${model.pigeonThreadGraph};
	graph(document.getElementById('PigeonStartedThread'), pigeonThreadGraphData);
	//07
	var newGcCountGraphData = ${model.newGcCountGraph};
	graph(document.getElementById('NewGcCount'), newGcCountGraphData);
	//08
	var oldGcCountGraphData = ${model.oldGcCountGraph};
	graph(document.getElementById('OldGcCount'), oldGcCountGraphData);
	//09
	var systemLoadAverageGraphData = ${model.systemLoadAverageGraph};
	graph(document.getElementById('SystemLoadAverage'), systemLoadAverageGraphData);
	//10
	var memoryFreeGraphData = ${model.memoryFreeGraph};
	graph(document.getElementById('MemoryFree'), memoryFreeGraphData);
	//11
	var heapUsageGraphData = ${model.heapUsageGraph};
	graph(document.getElementById('HeapUsage'), heapUsageGraphData);
	//12
	var noneHeapUsageGraphData = ${model.noneHeapUsageGraph};
	graph(document.getElementById('NoneHeapUsage'), noneHeapUsageGraphData);
	//13
	var diskRootGraphData = ${model.diskRootGraph};
	graph(document.getElementById('diskRoot'), diskRootGraphData);
	//14
	var diskDataGraphData = ${model.diskDataGraph};
	graph(document.getElementById('diskData'), diskDataGraphData);
	//16
	var catMessageProducedGraphData = ${model.catMessageProducedGraph};
	graph(document.getElementById('CatMessageProduced'), catMessageProducedGraphData);
	//17
	var catMessageOverflowGraphData = ${model.catMessageOverflowGraph};
	graph(document.getElementById('CatMessageOverflow'), catMessageOverflowGraphData);
	//18
	var catMessageSizeGraphData = ${model.catMessageSizeGraph};
	graph(document.getElementById('CatMessageSize'), catMessageSizeGraphData);
</script>
<res:useJs value="${res.js.local.transaction_js}" target="bottom-js" />
</jsp:body>

</a:historyReport>