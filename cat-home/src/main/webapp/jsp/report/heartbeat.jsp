<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"
	trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<jsp:useBean id="ctx"
	type="com.dianping.cat.report.page.heartbeat.Context" scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.heartbeat.Payload" scope="request" />
<jsp:useBean id="model"
	type="com.dianping.cat.report.page.heartbeat.Model" scope="request" />
<c:set var="report" value="${model.report}" />

<a:report title="Heartbeat Report"
	navUrlPrefix="domain=${model.domain}&ip=${model.ipAddress}"
	timestamp="${w:format(model.currentTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

	<jsp:body>

<res:useCss value="${res.css.local.heartbeat_css}" target="head-css" />
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />

<table class="machines">
<th>Machines: 
		<c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}"
								class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
		</c:forEach>
		</th>
</table>
<br>
<table class="graph">
<tr>
	<th>System Thread Info</th>
</tr>
<tr>
	<td>
		<svg version="1.1" width="1400" height="380"	xmlns="http://www.w3.org/2000/svg">
		  ${model.activeThreadGraph}
		  ${model.daemonThreadGraph}
		  ${model.totalThreadGraph}
		  ${model.startedThreadGraph}
		  ${model.catThreadGraph}
		  ${model.pigeonThreadGraph}
		</svg>
	</td>
</tr>
<tr>
	<th>System Info</th>
</tr>
<tr>
	<td>
		<svg version="1.1" width="1400" height="190"	xmlns="http://www.w3.org/2000/svg">
		  ${model.gcCountGraph}
		  ${model.systemLoadAverageGraph}
		</svg>
	</td>
</tr>
<tr>
	<th>Memery Info</th>
</tr>
<tr>
	<td>
		<svg version="1.1" width="1400" height="190"	xmlns="http://www.w3.org/2000/svg">
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
		<svg version="1.1" width="1400" height="190"	xmlns="http://www.w3.org/2000/svg">
		  ${model.diskFreeGraph}
		  ${model.diskUseableGraph}
		</svg>
	</td>
</tr>
<tr>
	<th>Cat Info</th>
</tr>
<tr>
	<td>
		<svg version="1.1" width="1400" height="190"	xmlns="http://www.w3.org/2000/svg">
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
		<th>GcCount</th>
		<th>SystemLoad</th>
		<th>HeapUsage</th>
		<th>NoneHeapUsage</th>
		<th>DiskFree</th>
		<th>DiskUseable</th>
		<th>CatMessageProduced</th>
		<th>CatMessageOverflow</th>
		<th>CatMessageSize</th>
	</tr>
	<c:forEach var="item" items="${model.result.periods}"
				varStatus="status">
		<tr class="${status.index  mod 2==1 ? 'even' : 'odd'}">
		<td>${item.minute}</td>
		<td>${item.threadCount}</td>
		<td>${item.daemonCount}</td>
		<td>${item.totalStartedCount}</td>
		<td>${item.catThreadCount}</td>
		<td>${item.pigeonThreadCount}</td>
		<td>${item.gcCount}</td>
		<td>${item.systemLoadAverage}</td>
		<td>${w:format(item.heapUsage,'0.#M')}</td>
		<td>${w:format(item.noneHeapUsage,'0.#M')}</td>
		<td>${w:format(item.diskFree,'0.#G')}</td>
		<td>${w:format(item.diskUseable,'0.#G')}</td>
		<td>${item.catMessageProduced}</td>
		<td>${item.catMessageOverflow}</td>
		<td>${w:format(item.catMessageSize,'0.#M')}</td>
		</tr>
	</c:forEach>
</table>

</jsp:body>
</a:report>