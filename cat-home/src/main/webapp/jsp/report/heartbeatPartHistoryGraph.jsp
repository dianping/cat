<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.heartbeat.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.heartbeat.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.heartbeat.Model" scope="request" />

<link rel="stylesheet" type="text/css" href="/cat/css/graph.css">
<script type="text/javascript" src="/cat/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="/cat/js/flotr2.js"></script>
<script type="text/javascript" src="/cat/js/baseTools.js"></script>
<script type="text/javascript" src="/cat/js/trendGraph.js"></script>
<script type="text/javascript" src="/cat/js/heartbeatHistory.js"></script>
<style type="text/css">
.graph{
width: 430px;
height: 200px;
margin: 4px auto;
}
</style>
<br>
<table class="graph" id="graph">
<c:choose>
	<c:when test="${payload.type eq 'thread'}">
		<tr>
			<td><div id="ActiveThread" class="graph"></div></td>
			<td><div id="StartedThread" class="graph"></div></td>
			<td><div id="TotalStartedThread" class="graph"></div></td>
		</tr>
		<script>
		//01
		var activeThreadGraphData = ${model.activeThreadGraph};
		graphOnMinute(document.getElementById('ActiveThread'), activeThreadGraphData);
		//02
		var startedThreadGraphData = ${model.startedThreadGraph};
		graphOnMinute(document.getElementById('StartedThread'), startedThreadGraphData);
		//03
		var totalThreadGraphData =${model.totalThreadGraph};
		graphOnMinute(document.getElementById('TotalStartedThread'), totalThreadGraphData);
		
	</script>
	</c:when>
	<c:when test="${payload.type eq 'system'}">
		<tr>
			<td><div id="NewGcCount" class="graph"></div></td>
			<td><div id="OldGcCount" class="graph"></div></td>
			<td><div id="SystemLoadAverage" class="graph"></div></td>
			<script>
			//07
			var newGcCountGraphData = ${model.newGcCountGraph};
			graphOnMinute(document.getElementById('NewGcCount'), newGcCountGraphData);
			//08
			var oldGcCountGraphData = ${model.oldGcCountGraph};
			graphOnMinute(document.getElementById('OldGcCount'), oldGcCountGraphData);
			//09
			var systemLoadAverageGraphData = ${model.systemLoadAverageGraph};
			graphOnMinute(document.getElementById('SystemLoadAverage'), systemLoadAverageGraphData);
			</script>
		</tr>
	</c:when>
	<c:when test="${payload.type eq 'memory'}">
		<tr id="memoryGraph">
			<td><div id="MemoryFree" class="graph"></div></td>
			<td><div id="HeapUsage" class="graph"></div></td>
			<td><div id="NoneHeapUsage" class="graph"></div></td>
			<script>
			//10
			var memoryFreeGraphData = ${model.memoryFreeGraph};
			graphOnMinute(document.getElementById('MemoryFree'), memoryFreeGraphData);
			//11
			var heapUsageGraphData = ${model.heapUsageGraph};
			graphOnMinute(document.getElementById('HeapUsage'), heapUsageGraphData);
			//12
			var noneHeapUsageGraphData = ${model.noneHeapUsageGraph};
			graphOnMinute(document.getElementById('NoneHeapUsage'), noneHeapUsageGraphData);
			</script>
		</tr>
	</c:when>
	<c:when test="${payload.type eq 'disk'}">
		<tr id="memoryGraph"></tr>
		<script>
			var diskHistoryGraph=${model.diskHistoryGraph};
			var size=${model.disks};
			disksGraph(size,diskHistoryGraph);
		</script>
	</c:when>
	<c:when test="${payload.type eq 'cat'}">
		<tr>
			<td><div id="CatMessageProduced" class="graph"></div></td>
			<td><div id="CatMessageOverflow" class="graph"></div></td>
			<td><div id="CatMessageSize" class="graph"></div></td>
			<script>
			//16
			var catMessageProducedGraphData = ${model.catMessageProducedGraph};
			graphOnMinute(document.getElementById('CatMessageProduced'), catMessageProducedGraphData);
			//17
			var catMessageOverflowGraphData = ${model.catMessageOverflowGraph};
			graphOnMinute(document.getElementById('CatMessageOverflow'), catMessageOverflowGraphData);
			//18
			var catMessageSizeGraphData = ${model.catMessageSizeGraph};
			graphOnMinute(document.getElementById('CatMessageSize'), catMessageSizeGraphData);
			</script>
		</tr>
	</c:when>
</c:choose>
