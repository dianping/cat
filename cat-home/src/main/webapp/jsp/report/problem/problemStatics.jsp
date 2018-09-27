<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.problem.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.problem.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.problem.Model" scope="request" />
<c:set var="report" value="${model.report}" />

<a:hourly_report title="Problem Report"
	navUrlPrefix="op=${payload.action.name}&domain=${model.domain}&ip=${model.ipAddress}${payload.queryString}"
	timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>

	<jsp:body>
<table class="machines">
	<tr style="text-align:left"> 
		<th>&nbsp;[&nbsp; <c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?domain=${model.domain}&date=${model.date}${payload.queryString}"
						class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&date=${model.date}${payload.queryString}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?op=view&domain=${model.domain}&ip=${ip}&date=${model.date}${payload.queryString}"
							class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?op=view&domain=${model.domain}&ip=${ip}&date=${model.date}${payload.queryString}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th></tr>
		
		<tr class="left">
			<th>
				<c:forEach var="group" items="${model.groups}">
		   	  		&nbsp;[&nbsp;
		   	  			<a href="?op=groupReport&domain=${model.domain}&date=${model.date}&group=${group}${payload.queryString}">${group}</a>
		   	 		&nbsp;]&nbsp;
				 </c:forEach>
			</th>
		</tr>
		
		<tr><th>
		<%@ include file="problemQuery.jsp" %>
		<script type="text/javascript" src="/cat/js/appendHostname.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				appendHostname(${model.ipToHostnameStr});
			});
		</script>
		<script>
			function longTimeChange(date,domain,ip){
				var longUrlTime=$("#p_longUrl").val();
				var longSqlTime=$("#p_longSql").val();
				var longServiceTime=$("#p_longService").val();
				var longCacheTime=$("#p_longCache").val();
				var longCallTime=$("#p_longCall").val();
				window.location.href="?op=view&domain="+domain+"&ip="+ip+"&date="+date+"&urlThreshold="+longUrlTime+"&sqlThreshold="+longSqlTime+"&serviceThreshold="+longServiceTime
						+"&cacheThreshold="+longCacheTime+"&callThreshold="+longCallTime;
			}
		</script>
		</th>
	</tr>
</table>
<table class="table table-hover table-striped table-condensed"  style="width:100%">
	<tr>
		<th width="7%">Type</th>
		<th width="4%">Total</th>
		<th width="30%">Status</th>
		<th width="4%">Count</th>
		<th width="55%">SampleLinks</th>
	</tr>
	<c:forEach var="statistics" items="${model.allStatistics.status}"
		varStatus="typeIndex">
		<tr>
			<td rowspan="${w:size(statistics.value.status)*3}" class="top">
				&nbsp;<a href="#" class="${statistics.value.type}">&nbsp;&nbsp;</a>
				&nbsp;&nbsp;${statistics.value.type}
				</br>
				<a href="?op=hourlyGraph&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${statistics.value.type}${model.customDate}" class="history_graph_link" data-status="${typeIndex.index}">[:: show ::]</a>
			</td>
			<td rowspan="${w:size(statistics.value.status)*3}" class="right top">${w:format(statistics.value.count,'#,###,###,###,##0')}&nbsp;</td>
			<c:forEach var="status" items="${statistics.value.status}"
				varStatus="index">
				<c:if test="${index.index != 0}">
					<tr>
				</c:if>
				<td>
					<a href="?op=hourlyGraph&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${statistics.value.type}&status=${status.value.status}${model.customDate}" class="problem_status_graph_link" data-status="${statistics.value.type}${status.value.status}">[:: show ::]</a>
					&nbsp;${status.value.status}
				</td>
				<td  class="right">${w:format(status.value.count,'#,###,###,###,##0')}&nbsp;</td>
				<td >
					<c:forEach var="links" items="${status.value.links}" varStatus="linkIndex">
						<a href="/cat/r/m/${links}?domain=${model.domain}">${linkIndex.first?'L':(linkIndex.last?'g':'o')}</a>
					</c:forEach></td>
						
				<c:if test="${index.index != 0}">
				</tr>
				</c:if>
				<tr><td colspan="3"  style="display:none"></td></tr>
				<tr><td colspan="3"  style="display:none"> <div id="${statistics.value.type}${status.value.status}" style="display:none"></div></td></tr>
			</c:forEach>
		</tr>
		<tr class="graphs"><td colspan="5" style="display:none"><div id="${typeIndex.index}" style="display:none"></div></td></tr>
		<tr></tr>
	</c:forEach>
</table>


<c:if test="${model.ipAddress ne 'All'}">
<a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&op=group" onclick="return requestGroupInfo(this)">Threads Details</a>

<div id="machineThreadGroupInfo"></div>
</c:if>
<res:useJs value="${res.js.local.problem_js}" target="buttom-js" />
<res:useJs value="${res.js.local.problemHistory_js}" target="bottom-js" />
</jsp:body>

</a:hourly_report>