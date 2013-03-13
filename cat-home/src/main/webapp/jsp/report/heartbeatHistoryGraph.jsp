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
	<th	colspan="3" style="text-align:left">Framework Thread Info</th>
</tr>
<tr>
	
	<td><div id="HttpStartedThread" class="graph"></div></td>
	<td><div id="CatStartedThread" class="graph"></div></td>
	<td><div id="PigeonStartedThread" class="graph"></div></td>
</tr>
<tr>
	<th colspan="3" style="text-align:left"><a  data-status="thread" class="graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=thread">JVM Thread Info</a></th>
</tr>
<tr>
	<td colspan="3"><iframe id="thread" style="display:none;" width="100%" height="260px"></iframe></td>
</tr>

<tr>
	<th colspan="3" style="text-align:left"><a  data-status="system" class="graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=system">System Info</a>&nbsp;&nbsp;&nbsp;&nbsp;(New Gc,Old Gc,System Load)</th>
</tr>
<tr>
	<td colspan="3"><iframe id="system" style="display:none;" width="100%" height="260px"></iframe></td>
</tr>
<tr>
	<th colspan="3" style="text-align:left"><a  data-status="memory" class="graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=memory">Memory Info</a>&nbsp;&nbsp;&nbsp;&nbsp;(Memory Free,Heap Usage,None Heap Usage)</th>
</tr>
<tr>
	<td colspan="3"><iframe id="memory" style="display:none;" width="100%" height="260px"></iframe></td>
</tr>
<tr>
	<th colspan="3" style="text-align:left"><a  data-status="disk" class="graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=disk">Disk Info</a></th>
</tr>
<tr>
	<td colspan="3"><iframe id="disk" style="display:none;" width="100%" height="260px"></iframe></td>
</tr>

<tr>
	<th colspan="3" style="text-align:left"><a  data-status="cat" class="graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=cat">Cat Info</a></th>
</tr>
<tr>
	<td colspan="3"><iframe id="cat" style="display:none;" width="100%" height="260px"></iframe></td>
</tr>
</table>
	<res:useJs value="${res.js.local['heartbeatHistory_js']}" target="head-buttom"/>
	<script>
	//04
	var httpThreadGraphData = ${model.httpThreadGraph};
	graphOnMinute(document.getElementById('HttpStartedThread'), httpThreadGraphData);
	//05
	var catThreadGraphData = ${model.catThreadGraph};
	graphOnMinute(document.getElementById('CatStartedThread'), catThreadGraphData);
	//06
	var pigeonThreadGraphData = ${model.pigeonThreadGraph};
	graphOnMinute(document.getElementById('PigeonStartedThread'), pigeonThreadGraphData);
	</script>
</jsp:body>
</a:historyReport>