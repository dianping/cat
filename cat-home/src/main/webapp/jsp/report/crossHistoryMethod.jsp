<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.cross.Context" scope="request" />
<jsp:useBean id="payload"  	type="com.dianping.cat.report.page.cross.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.cross.Model" scope="request" />

<a:historyReport title="Cross Report"
	navUrlPrefix="ip=${model.ipAddress}&domain=${model.domain}">

	<jsp:attribute name="subtitle">From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>

<res:useCss value="${res.css.local.cross_css}" target="head-css" />
<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
<%@ include file="crossQuery.jsp" %>
</br>
<table class="machines">
	<tr style="text-align: left">
		<th>Machines: &nbsp;[&nbsp; <c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?op=history&domain=${model.domain}&reportType=${model.reportType}&date=${model.date}&remote=${payload.remoteIp}${model.customDate}"
								class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?op=history&domain=${model.domain}&reportType=${model.reportType}&date=${model.date}&remote=${payload.remoteIp}${model.customDate}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?op=history&domain=${model.domain}&reportType=${model.reportType}&ip=${ip}&date=${model.date}&remote=${payload.remoteIp}${model.customDate}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?op=history&domain=${model.domain}&reportType=${model.reportType}&ip=${ip}&date=${model.date}&remote=${payload.remoteIp}${model.customDate}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<br>
<table class='cross'>
		<tr><th style="text-align: left" colspan='8'><input type="text" name="queryname" id="queryname" size="40" value="${model.queryName}">
		    <input style="WIDTH: 60px" value="Filter" onclick="filterByName('${model.date}','${model.domain}','${model.ipAddress}')" type="submit">
			支持多个字符串查询，例如sql|url|task，查询结果为包含任一sql、url、task的列
			</th></tr>
		<script>
			function filterByName(date,domain,ip){
				var queryName=$("#queryname").val();
				var serviceSort='${model.serviceSort}';
				var callSort='${model.callSort}';
				var remote='${payload.remoteIp}';
				var customDate = '${model.customDate}';
				var reportType ='${model.reportType}';
				var project = '${payload.projectName}';
				window.location.href="?op=historyMethod&domain="+domain+"&project="+project+"&reportType="+reportType+"&ip="+ip+"&date="
						+date+"&queryName="+queryName+"&remote="+remote+"&serviceSort="+serviceSort+"&callSort"+callSort+customDate;
			}
		</script>
		<c:if test="${!empty model.methodInfo.callProjectsInfo}">
		<tr>
			<th class="left">Type</th>
			<th class="left">RemoteIp</th>
			<th class="left">Method</th>
			<th><a href="?op=historyMethod&domain=${model.domain}&reportType=${model.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&serviceSort=${model.serviceSort}&callSort=total&queryName=${model.queryName}${model.customDate}">Total</a></th>
			<th><a href="?op=historyMethod&domain=${model.domain}&reportType=${model.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&serviceSort=${model.serviceSort}&callSort=failure&queryName=${model.queryName}${model.customDate}">Failure</a></th>
			<th><a href="?op=historyMethod&domain=${model.domain}&reportType=${model.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&serviceSort=${model.serviceSort}&callSort=failurePercent&queryName=${model.queryName}${model.customDate}">Failure%</a></th>
			<th><a href="?op=historyMethod&domain=${model.domain}&reportType=${model.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&serviceSort=${model.serviceSort}&callSort=avg&queryName=${model.queryName}${model.customDate}">Avg(ms)</a></th>
			<th>QPS</th>
		</tr>
		<c:forEach var="callInfo" items="${model.methodInfo.callProjectsInfo}" varStatus="status">
			<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
		         	<td class="left">${callInfo.type}</td>
					<td class="left">${callInfo.ip}</td>
					<td class="left">${callInfo.id}</td>
		         	<td>${w:format(callInfo.totalCount,'#,###,###,###,##0')}</td>
		         	<td>${w:format(callInfo.failureCount,'#,###,###,###,##0')}</td>
		        	<td>${w:format(callInfo.failurePercent,'0.00%')}</td>
		            <td>${w:format(callInfo.avg,'0.00')}</td>
		            <td>${w:format(callInfo.tps,'0.00')}</td>
		         </tr>
		</c:forEach>
		<tr><td>&nbsp</td></tr>
		<tr><td>&nbsp</td></tr>
		</c:if>

		<c:if test="${!empty model.methodInfo.serviceProjectsInfo}">
		      <tr>
		         <th class="left">Type</th>
				 <th class="left">RemoteIp</th>
				 <th class="left">Method</th>
		         <th><a href="?op=historyMethod&domain=${model.domain}&reportType=${model.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=total&queryName=${model.queryName}${model.customDate}">Total</a></th>
		         <th><a href="?op=historyMethod&domain=${model.domain}&reportType=${model.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=failure&queryName=${model.queryName}${model.customDate}">Failure</a></th>
		         <th><a href="?op=historyMethod&domain=${model.domain}&reportType=${model.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=failurePercent&queryName=${model.queryName}${model.customDate}">Failure%</a></th>
		         <th><a href="?op=historyMethod&domain=${model.domain}&reportType=${model.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=avg&queryName=${model.queryName}${model.customDate}">Avg(ms)</a></th>
		         <th>QPS</th>
		      </tr>
		      <c:forEach var="serviceInfo" items="${model.methodInfo.serviceProjectsInfo}" varStatus="status">
		         <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
		            <td class="left">${serviceInfo.type}</td>
					<td class="left">${serviceInfo.ip}</td>
					<td class="left">${serviceInfo.id}</td>
		            <td>${w:format(serviceInfo.totalCount,'#,###,###,###,##0')}</td>
		            <td>${w:format(serviceInfo.failureCount,'#,###,###,###,##0')}</td>
		            <td>${w:format(serviceInfo.failurePercent,'0.00%')}</td>
		            <td>${w:format(serviceInfo.avg,'0.00')}</td>
		            <td>${w:format(serviceInfo.tps,'0.00')}</td>
		         </tr>
		      </c:forEach>
		      </c:if>
</table>
</jsp:body>
</a:historyReport>
