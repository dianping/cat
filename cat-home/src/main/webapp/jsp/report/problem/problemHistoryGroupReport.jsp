<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.problem.Context"	scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.problem.Payload"	scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.problem.Model" scope="request" />

<a:historyReport title="History Report" navUrlPrefix="ip=${model.ipAddress}${payload.queryString}">

	<jsp:attribute name="subtitle">From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
	<res:useJs value="${res.js.local['highcharts.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
<table class="machines">
	<tr style="text-align:left">
		<th>机器: &nbsp;[&nbsp; 
					<a href="?op=history&domain=${model.domain}&date=${model.date}${payload.queryString}&ip=All&reportType=${model.reportType}${model.customDate}"
					>All</a>
			&nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
						<a href="?op=history&domain=${model.domain}&ip=${ip}&date=${model.date}${payload.queryString}&reportType=${model.reportType}${model.customDate}"
						>${ip}</a>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th></tr>
		
		<tr class="left">
			<th>机器分组: &nbsp;&nbsp; 
				<c:if test="${empty model.groups}">
			    <span class="text-error">将几台机器的IP合并成为一个组，可以方便查询这个组内的几台机器相关信息，比如微信组。
				<a href="/cat/s/config?op=domainGroupConfigUpdate">配置link</a></span>
			</c:if> 
				<c:forEach var="group" items="${model.groups}">
				 <c:choose><c:when test="${payload.group eq group}">
		   	  		&nbsp;[&nbsp;
		   	  			<a class='current' href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${group}${payload.queryString}">${group}</a>
		   	 		&nbsp;]&nbsp;
	   	 		</c:when>
	   	 		<c:otherwise>
		   	  		&nbsp;[&nbsp;
		   	  			<a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${group}${payload.queryString}">${group}</a>
		   	 		&nbsp;]&nbsp;
	   	 		</c:otherwise></c:choose>
				 </c:forEach>
			</th>
		</tr>
		
		<tr><th>
		<%@ include file="problemQuery.jsp" %></th>
		<script>
			function longTimeChange(date,domain,ip){
				var customDate ='${model.customDate}';
				var reportType = '${model.reportType}';
				var longUrlTime=$("#p_longUrl").val();
				var longSqlTime=$("#p_longSql").val();
				var longServiceTime=$("#p_longService").val();
				var longCacheTime=$("#p_longCache").val();
				var longCallTime=$("#p_longCall").val();
				window.location.href="?op=historyGroupReport&domain="+domain+"&ip="+ip+"&date="+date+"&urlThreshold="+longUrlTime+"&sqlThreshold="+longSqlTime+'&reportType='+reportType+customDate+"&serviceThreshold="+longServiceTime
				+"&cacheThreshold="+longCacheTime+"&callThreshold="+longCallTime+"&group=${payload.group}";
			}
		</script>
	</tr>
</table>

<table class="problem" style="width:100%">
	<tr>
		<th width="15%">Type</th>
		<th width="5%">Total</th>
		<th width="45%">Status</th>
		<th width="5%">Count</th>
		<th width="30%">SampleLinks</th>
	</tr>
	<c:forEach var="statistics" items="${model.allStatistics.status}"
		varStatus="typeIndex">
		<tr>
			<td rowspan="${w:size(statistics.value.status)*2}"
				class="${typeIndex.index mod 2 != 0 ? 'even' : 'odd'} top">
				<a href="?op=historyGroupGraph&group=${payload.group}&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${statistics.value.type}${model.customDate}" class="history_graph_link" data-status="${typeIndex.index}">[:: show ::]</a>
				&nbsp;<a href="#" class="${statistics.value.type}">&nbsp;&nbsp;</a>
				&nbsp;&nbsp;${statistics.value.type}
			</td>
			<td rowspan="${w:size(statistics.value.status)*2}"
				class="${typeIndex.index mod 2 != 0 ? 'even' : 'odd'} top right">${w:format(statistics.value.count,'#,###,###,###,##0')}</td>
			<c:forEach var="status" items="${statistics.value.status}"
				varStatus="index">
				<c:if test="${index.index != 0}">
					<tr>
				</c:if>
				<td class="${index.index mod 2 != 0 ? 'even' : 'odd'}">
					<a href="?op=historyGroupGraph&group=${payload.group}&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${model.reportType}&type=${statistics.value.type}&status=${status.value.status}${model.customDate}" class="problem_status_graph_link" data-status="${statistics.value.type}${status.value.status}">[:: show ::]</a>
					&nbsp;${status.value.status}
				</td>
				<td class="${index.index mod 2 != 0 ? 'even' : 'odd'} right"> ${w:format(status.value.count,'#,###,###,###,##0')}</td>
				<td class="${index.index mod 2 != 0 ? 'even' : 'odd'}"><c:forEach
						var="links" items="${status.value.links}" varStatus="linkIndex">
						<a href="${model.logViewBaseUri}/${links}?domain=${model.domain}">${linkIndex.first?'L':(linkIndex.last?'g':'o')}</a>
					</c:forEach></td>
						
				<c:if test="${index.index != 0}">
				</tr>
				</c:if>
				<tr><td colspan="3"> <div id="${statistics.value.type}${status.value.status}" style="display:none"></div></td></tr>
			</c:forEach>
			</tr>
		<tr class="graphs"><td colspan="5"><div id="${typeIndex.index}" style="display:none"></div></td></tr>
	</c:forEach>
</table>

<res:useJs value="${res.js.local.problemHistory_js}" target="bottom-js" />
</jsp:body>

</a:historyReport>