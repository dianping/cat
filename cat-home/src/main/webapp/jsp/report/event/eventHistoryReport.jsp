<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.event.Context"	scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.event.Payload"	scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.event.Model" scope="request" />

<a:historyReport title="History Report" navUrlPrefix="type=${payload.encodedType}&ip=${model.ipAddress}">
	<jsp:attribute name="subtitle">${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
<table class="machines">
	<tr style="text-align: left">
		<th> 
		<c:forEach items="${model.ips}" var="value">
    		<c:if test="${value == 'All'}">
        	<c:set var="found" value="true" scope="request" />
    		</c:if>
		</c:forEach>
		<c:if test="${found != true}">
		&nbsp;[&nbsp; 
			<c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?op=history&domain=${model.domain}&ip=${ip}&date=${model.date}&reportType=${payload.reportType}${model.customDate}&type=${payload.encodedType}"
						class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?op=history&domain=${model.domain}&ip=${ip}&date=${model.date}&reportType=${payload.reportType}${model.customDate}&type=${payload.encodedType}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp;
		</c:if>
		<c:forEach var="ip" items="${model.ips}">&nbsp;[&nbsp; 
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?op=history&domain=${model.domain}&ip=${ip}&date=${model.date}&reportType=${payload.reportType}${model.customDate}&type=${payload.encodedType}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?op=history&domain=${model.domain}&ip=${ip}&date=${model.date}&reportType=${payload.reportType}${model.customDate}&type=${payload.encodedType}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<script type="text/javascript" src="/cat/js/appendHostname.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		appendHostname(${model.ipToHostnameStr});
	});
</script>
<table class="groups">
	<tr class="left">
		<th>
			<c:forEach var="group" items="${model.groups}">
	   	  		&nbsp;[&nbsp;
	   	  			<a href="?op=historyGroupReport&domain=${model.domain}&date=${model.date}&group=${group}&type=${payload.encodedType}">${group}</a>
	   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>

<table class='table table-hover table-striped table-condensed ' style="width:100%;">
	<c:choose>
		<c:when test="${empty payload.type}">
			<tr>
			<th  style="text-align: left;"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&sort=type${model.customDate}"> Type</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&sort=total${model.customDate}">Total</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&sort=failure${model.customDate}">Failure</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&sort=failurePercent${model.customDate}">Failure%</a></th>
			<th class="right">Sample Link</th><th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&sort=total${model.customDate}">QPS</a></th>
			</tr>
			<c:forEach var="item" items="${model.displayTypeReport.results}"
						varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class=" right">
					<td style="text-align: left">
					<a href="?op=historyGraph&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${item.type}${model.customDate}" class="history_graph_link" data-status="${status.index}">[:: show ::]</a>
					&nbsp;&nbsp;&nbsp;<a href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${item.type}${model.customDate}">${item.detail.id}</a></td>
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent/100,'0.0000%')}</td>
					<td><a href="/cat/r/m/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${model.domain}">Log View</a></td>
					<td>${w:format(e.tps,'0.0')}</td>
				</tr>
				<tr class="graphs"><td colspan="7" style="display:none"><div id="${status.index}" style="display:none"></div></td></tr>
				<tr></tr>
			</c:forEach>
		</c:when>
		<c:otherwise>
		<tr>
			<th  style="text-align: left;"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=type${model.customDate}"> Name</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=total${model.customDate}">Total</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=failure${model.customDate}">Failure</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=failurePercent${model.customDate}">Failure%</a></th>
			<th class="right">Sample Link</th><th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=type${model.customDate}">QPS</a></th>
			<th class="right"><a	href="?op=history&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${payload.encodedType}&sort=type${model.customDate}">Percent%</a></th>
					</tr>
			<c:forEach var="item" items="${model.displayNameReport.results}"
						varStatus="status">
				<c:set var="e" value="${item.detail}" />
				<c:set var="lastIndex" value="${status.index}" />
				<tr class=" right">
					<td style="text-align: left">
					<c:choose>
					<c:when test="${status.index > 0}">
						<a href="?op=historyGraph&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${payload.encodedType}&name=${item.name}${model.customDate}" class="history_graph_link" data-status="${status.index}">[:: show ::]</a>
						&nbsp;&nbsp;&nbsp;${e.id}</td>
					</c:when>
					<c:otherwise>
						${e.id}
					</c:otherwise>
					</c:choose>				
					<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
					<td>${e.failCount}</td>
					<td>${w:format(e.failPercent/100,'0.0000%')}</td>
					<td><a	href="/cat/r/m/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${model.domain}">Log View</a></td>
					<td>${w:format(e.tps,'0.0')}</td>
					<td>${w:format(e.totalPercent,'0.0000%')}</td>
				</tr>
				<tr class="graphs"><td colspan="7" style="display:none"><div id="${status.index}" style="display:none"></div></td></tr>
				<tr></tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
</table>

<font color="white">${lastIndex+1}</font>
</br>
<res:useJs value="${res.js.local.event_js}" target="bottom-js" />
<c:choose>
	<c:when test="${not empty payload.type}">
		<table>
			<tr>
				<td><div id="eventGraph" class="pieChart" ></div>
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
</a:historyReport>