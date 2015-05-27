<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.web.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.web.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.web.Model" scope="request" />
<c:set var="report" value="${model.problemReport}" />

<a:body>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
<div class="breadcrumbs">
		<span class="text-danger title">【报表时间】</span><span class="text-success">${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</span>
		<div class="nav-search nav" id="nav-search">
		<c:forEach var="nav" items="${model.navs}">
			&nbsp;[ <a href="${model.baseUri}?op=problem&date=${model.date}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]
		</c:forEach>
		&nbsp;[ <a href="${model.baseUri}?op=${payload.action.name}&domain=${model.domain}&ip=${model.ipAddress}">now</a> ]&nbsp;
		</div>
</div>
<table class="machines">
	<tr style="text-align:left"> 
		<th>&nbsp;[&nbsp; <c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?op=problem&domain=${model.domain}&date=${model.date}"
						class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?op=problem&domain=${model.domain}&date=${model.date}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?op=problem&domain=${model.domain}&ip=${ip}&date=${model.date}"
							class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?op=problem&domain=${model.domain}&ip=${ip}&date=${model.date}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th></tr>
		<tr><th>
		<script type="text/javascript" src="/cat/js/appendHostname.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				appendHostname(${model.ipToHostnameStr});
				$('#Web_report').addClass('active open');
				$('#web_problem').addClass('active');
			});
		</script>
		</th>
	</tr>
</table>
<table class="table table-hover table-striped table-condensed"  style="width:100%">
	<tr>
		<th width="30%">Status</th>
		<th width="5%">Count</th>
		<th width="55%">SampleLinks</th>
	</tr>
	<c:forEach var="statistics" items="${model.allStatistics.status}"
		varStatus="typeIndex">
		<tr><td><strong><a href="/cat/r/p?op=hourlyGraph&domain=FrontEnd&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${statistics.value.type}${model.customDate}" class="history_graph_link" data-status="${typeIndex.index}">[:: show ::]</a>
		Total</strong></td>
		<td class="right">${w:format(statistics.value.count,'#,###,###,###,##0')}&nbsp;</td>
		<tr class="graphs"><td colspan="5" style="display:none"><div id="${typeIndex.index}" style="display:none"></div></td></tr>
		<tr>
			<c:forEach var="status" items="${statistics.value.status}" varStatus="index">
				<c:if test="${index.index != 0}">
					<tr>
				</c:if>
				<td>
					<a href="/cat/r/p?op=hourlyGraph&domain=FrontEnd&date=${model.date}&ip=${model.ipAddress}&reportType=${payload.reportType}&type=${statistics.value.type}&status=${status.value.status}${model.customDate}" class="problem_status_graph_link" data-status="${statistics.value.type}${status.value.status}">[:: show ::]</a>
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
	</c:forEach>
</table>
<res:useJs value="${res.js.local.problem_js}" target="buttom-js" />
<res:useJs value="${res.js.local.problemHistory_js}" target="bottom-js" />
</a:body>