<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.heartbeat.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.heartbeat.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.heartbeat.Model" scope="request" />

<a:historyReport title="HeartBeat History Report">
	<jsp:attribute name="subtitle">${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
<table class="machines">
	<tr style="text-align: left">
		<th>
   	  		 <c:forEach var="ip" items="${model.ips}">&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${payload.realIp eq ip}">
						<a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&reportType=${payload.reportType}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${ip}&reportType=${payload.reportType}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<br>
<table style="width:100%">
	<c:forEach var="extensionGroup" items="${model.extensionGroups}">
		<tr>
			<th colspan="3" style="text-align:left"><h5><a  data-status="${extensionGroup}" class="heartbeat_graph_link" href="?op=historyPart&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=extension&extensionType=${extensionGroup}">${extensionGroup}</a></h5></th>
		</tr>
		<tr>
			<td colspan="3"><iframe id="${extensionGroup}" style="display:none;" width="100%" height="400px"></iframe></td>
		</tr>
	</c:forEach>
</table>
	<res:useJs value="${res.js.local['heartbeatHistory_js']}" target="head-buttom"/>
	<script>
	var extensionHistoryGraphs=${model.extensionHistoryGraphs};
	var count=${model.extensionCount};
	buildExtensionGraph(count,extensionHistoryGraphs);
	</script>
</jsp:body>
</a:historyReport>
<script type="text/javascript" src="/cat/js/heartbeatHistory.js"></script>
<script type="text/javascript" src="/cat/js/appendHostname.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		appendHostname(${model.ipToHostnameStr});
		$('a:contains("month")').parent().hide();
		$('a:contains("week")').parent().hide();
	});
</script>