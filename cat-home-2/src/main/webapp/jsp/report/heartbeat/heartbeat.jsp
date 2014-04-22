<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"
	trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.heartbeat.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.heartbeat.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.heartbeat.Model" scope="request" />
<c:set var="report" value="${model.report}" />

<a:report title="HeartBeat Report" navUrlPrefix="ip=${model.ipAddress}&domain=${model.domain}" timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">
	<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
<table class="machines">
	<th style="text-align:left">机器: 
		<c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
				<c:when test="${payload.realIp eq ip}">
					<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}" class="current">${ip}</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}">${ip}</a>
				</c:otherwise>
			</c:choose>
   	 		&nbsp;]&nbsp;
		</c:forEach>
		</th>
</table>
<table class="graph">
<tr>
	<th>Framework Thread Info</th>
</tr>
<tr>
	<td>
		<svg version="1.1" width="1400" height="190" xmlns="http://www.w3.org/2000/svg">
		  ${model.httpThreadGraph}
		  ${model.catThreadGraph}
		  ${model.pigeonThreadGraph}
		</svg>
	</td>
</tr>
<tr>
	<th>JVM Thread Info</th>
</tr>
<tr>
	<td>
		<svg version="1.1" width="1400" height="190" xmlns="http://www.w3.org/2000/svg">
		  ${model.activeThreadGraph}
		  ${model.startedThreadGraph}
		  ${model.totalThreadGraph}
		</svg>
	</td>
</tr>
<tr>
	<th>System Info</th>
</tr>
<tr>
	<td>
		<svg version="1.1" width="1400" height="190" xmlns="http://www.w3.org/2000/svg">
		  ${model.newGcCountGraph}
		  ${model.oldGcCountGraph}
		  ${model.systemLoadAverageGraph}
		</svg>
	</td>
</tr>
<tr>
	<th>Memery Info</th>
</tr>
<tr>
	<td>
		<svg version="1.1" width="1400" height="190" xmlns="http://www.w3.org/2000/svg">
		  ${model.memoryFreeGraph}
		  ${model.heapUsageGraph}
		  ${model.noneHeapUsageGraph}
		</svg>
	</td>
</tr>
<tr>
	<th>Disk Info</th>
</tr>
<tr>
	<td>
		<svg version="1.1" width="1400" height="${model.diskRows * 190 }" xmlns="http://www.w3.org/2000/svg">
		  ${model.disksGraph}
		</svg>
	</td>
</tr>
<tr>
	<th>Cat Message Info</th>
</tr>
<tr>
	<td>
		<svg version="1.1" width="1400" height="190" xmlns="http://www.w3.org/2000/svg">
		  ${model.catMessageProducedGraph}
		  ${model.catMessageOverflowGraph}
		  ${model.catMessageSizeGraph}
		</svg>
	</td>
</tr>
</table>
<table class="heartbeat">
	<tr>
		<th>Minute</th>
		<th>ActiveThread</th>
		<th>DeamonThread</th>
		<th>StartedThead</th>
		<th>CatThead</th>
		<th>PigeonThead</th>
		<th>NewGcCount</th>
		<th>OldGcCount</th>
		<th>SystemLoad</th>
		<th>HeapUsage</th>
		<th>NoneHeapUsage</th>
		<th>MemoryFree</th>
		<th>DiskFree</th>
		<th>CatProduced</th>
		<th>CatOverflow</th>
		<th>CatSize</th>
	</tr>
	<c:forEach var="item" items="${model.result.periods}" varStatus="status">
		<tr class="${status.index  mod 2==1 ? 'even' : 'odd'} right">
		<td class="center">${item.minute}</td>
		<td>${item.threadCount}</td>
		<td>${item.daemonCount}</td>
		<td>${item.totalStartedCount}</td>
		<td>${item.catThreadCount}</td>
		<td>${item.pigeonThreadCount}</td>
		<td>${item.newGcCount}</td>
		<td>${item.oldGcCount}</td>
		<td>${w:format(item.systemLoadAverage,'0.00')}</td>
		<td>${w:format(item.heapUsage,'0.0MB')}</td>
		<td>${w:format(item.noneHeapUsage,'0.0MB')}</td>
		<td>${w:format(item.memoryFree,'0.0MB')}</td>
		<td><c:forEach var="disk" items="${item.disks}" varStatus="vs">${w:formatNumber(disk.free,'0.0', 'B')}<c:if test="${not vs.last}">/</c:if></c:forEach></td>
		<td>${w:format(item.catMessageProduced,'#,###,###,###,##0')}</td>
		<td>${w:format(item.catMessageOverflow,'#,###,###,###,##0')}</td>
		<td>${w:format(item.catMessageSize,'#,##0.0MB')}</td>
		</tr>
	</c:forEach>
</table>


<script type="text/javascript">
	$(document).ready(function() {
		$($('.text-error')[3]).hide();
	});
</script>
</jsp:body>
</a:report>
