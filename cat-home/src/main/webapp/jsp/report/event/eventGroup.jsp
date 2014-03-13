<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.event.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.event.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.event.Model" scope="request" />
<c:set var="report" value="${model.report}" />

<a:report
	title="Event Report${empty payload.type ? '' : ' :: '}<a href='?op=groupReport&domain=${model.domain}&date=${model.date}&group=${payload.group}&type=${payload.type}'>${payload.type}</a>"
	navUrlPrefix="op=groupReport&op=groupReport&domain=${model.domain}${empty payload.type ? '' : '&type='}${payload.type}"
	timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">From ${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
<res:useJs value="${res.js.local['highcharts.js']}" target="head-js"/>
<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>

<table class="machines">
	<tr class="left">
		<th>机器: &nbsp;[&nbsp; 
					<a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&type=${payload.type}" >All</a>
		     &nbsp;]&nbsp; 
			<c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
					<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}&group=${payload.group}&type=${payload.type}">${ip}</a>
   	 		&nbsp;]&nbsp;
			</c:forEach>
		</th>
	</tr>
</table>
<table class="groups">
	<tr class="left">
		<th>机器分组: &nbsp;&nbsp; 
			<c:if test="${empty model.groups}">
			    <span class="text-error">将几台机器的IP合并成为一个组，可以方便查询这个组内的几台机器相关信息，比如微信组。
				<a href="/cat/s/config?op=domainGroupConfigUpdate">配置link</a></span>
			</c:if> 
			<c:forEach var="group" items="${model.groups}">
				<c:choose><c:when test="${payload.group eq group}">
		   	  		&nbsp;[&nbsp;
		   	  			<a class="current" href="?op=groupReport&domain=${model.domain}&group=${group}&date=${model.date}&group=${payload.group}">${group}</a>
		   	 		&nbsp;]&nbsp;
	   	 		</c:when>
	   	 		<c:otherwise>
		   	  		&nbsp;[&nbsp;
		   	  			<a href="?op=groupReport&domain=${model.domain}&group=${group}&date=${model.date}&group=${payload.group}">${group}</a>
		   	 		&nbsp;]&nbsp;
	   	 		</c:otherwise></c:choose>
			 </c:forEach>
		</th>
	</tr>
</table>
<table class='data'>
	<c:choose>
		<c:when test="${empty payload.type}">
			<tr>
			<th class="left"><a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&sort=type">Type</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&sort=total">Total Count</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&sort=failure">Failure Count</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&sort=failurePercent">Failure%</a></th>
			<th>Sample Link</th>
			<th>QPS</th>
			</tr>
			<c:forEach var="item" items="${model.displayTypeReport.results}" varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'} right">
					<td class="left">
						<a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&sort=type"><a href="?op=groupGraphs&domain=${model.domain}&date=${model.date}&group=${payload.group}&type=${item.type}" class="graph_link" data-status="${status.index}">[:: show ::]</a>
						&nbsp;&nbsp;<a href="?domain=${report.domain}&date=${model.date}&group=${payload.group}&op=groupReport&type=${item.type}">${item.type}</a>
					</td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${w:format(e.failCount,'#,###,###,###,##0')}</td>
					<td>&nbsp;${w:format(e.failPercent/100,'0.0000%')}</td>
					<td class="center"><a href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${model.domain}">Log View</a></td>
					<td>${w:format(e.tps,'###,##0.0')}</td>
				</tr>
				<tr class="graphs">
					<td colspan="7"><div id="${status.index}" style="display: none"></div></td>
				</tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
			<tr>
			<th class="left"><a href="?op=groupGraphs&domain=${model.domain}&date=${model.date}&group=${payload.group}&type=${payload.type}&op=groupReport" class="graph_link" data-status="-1">[:: show ::]</a>
			<a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&type=${payload.type}&sort=type"> Name</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&type=${payload.type}&sort=total">Total Count</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&type=${payload.type}&sort=failure">Failure Count</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&type=${payload.type}&sort=failurePercent">Failure%</a></th>
			<th>Sample Link</th>
			<th><a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&type=${payload.type}&sort=total">QPS</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&group=${payload.group}&op=groupReport&type=${payload.type}&sort=total">Percent%</a></th>
			</tr>
			<tr class="graphs"><td colspan="7"><div id="-1" style="display: none"></div></td></tr>
			<c:forEach var="item" items="${model.displayNameReport.results}" varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'} right">
					<td class="left">
					<c:choose>
					<c:when test="${status.index > 0}">
						<a	href="?op=groupGraphs&domain=${report.domain}&op=groupReport&date=${model.date}&group=${payload.group}&type=${payload.type}&name=${e.id}" class="graph_link" data-status="${status.index}">[:: show ::]</a>
					</c:when>
					</c:choose>
					&nbsp;&nbsp;${e.id}
					</td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${w:format(e.failCount,'#,###,###,###,##0')}</td>
					<td>&nbsp;${w:format(e.failPercent/100,'0.0000%')}</td>
					<td class="center"><a href="${model.logViewBaseUri}/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${model.domain}">Log View</a></td>
					<td>${w:format(e.tps,'0.0')}</td>
					<td>${w:format(e.totalPercent,'0.0000%')}</td>
				</tr>
				<tr class="graphs">
					<td colspan="5"><div id="${status.index}" style="display: none"></div></td>
				</tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>

<font color="white">${lastIndex+1}</font>
<res:useJs value="${res.js.local.event_js}" target="bottom-js" />
<c:choose>
	<c:when test="${not empty payload.type}">
		<table>
			<tr>
				<td><div id="eventGraph" class="pieChart"></div>
				</td>
			</tr>
		</table>
		<script type="text/javascript">
			var data = ${model.pieChart};
			graphPieChart(document.getElementById('eventGraph'), data );
		</script>
	</c:when>
</c:choose>

</jsp:body>
</a:report>
