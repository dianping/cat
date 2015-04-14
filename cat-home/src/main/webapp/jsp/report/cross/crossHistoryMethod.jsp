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
	<jsp:attribute name="subtitle">${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
<%@ include file="crossQuery.jsp" %>
<table class="machines">
	<tr style="text-align: left">
		<th>&nbsp;[&nbsp; <c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?op=history&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&remote=${payload.remoteIp}${model.customDate}"
								class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?op=history&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&remote=${payload.remoteIp}${model.customDate}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?op=history&domain=${model.domain}&reportType=${payload.reportType}&ip=${ip}&date=${model.date}&remote=${payload.remoteIp}${model.customDate}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?op=history&domain=${model.domain}&reportType=${payload.reportType}&ip=${ip}&date=${model.date}&remote=${payload.remoteIp}${model.customDate}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<table class='table table-striped table-condensed '>
		<tr><th style="text-align: left" colspan='17'><input type="text" name="queryname" id="queryname" size="40" value="${model.queryName}">
		    <input style="WIDTH: 60px" value="Filter" onclick="filterByName('${model.date}','${model.domain}','${model.ipAddress}')" type="submit">
			支持多个字符串查询，例如sql|url|task，查询结果为包含任一sql、url、task的列
			</th></tr>
		<script type="text/javascript" src="/cat/js/appendHostname.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				appendHostname(${model.ipToHostnameStr});
			});
		</script>
		<script>
			function filterByName(date,domain,ip){
				var queryName=$("#queryname").val();
				var serviceSort='${model.serviceSort}';
				var callSort='${model.callSort}';
				var remote='${payload.remoteIp}';
				var customDate = '${model.customDate}';
				var reportType ='${payload.reportType}';
				var project = '${payload.projectName}';
				window.location.href="?op=historyMethod&domain="+domain+"&project="+project+"&reportType="+reportType+"&ip="+ip+"&date="
						+date+"&queryName="+queryName+"&remote="+remote+"&serviceSort="+serviceSort+"&callSort"+callSort+customDate;
			}
		</script>
		<c:if test="${!empty model.methodInfo.callProjectsInfo}">
		<tr><td colspan="8" style="text-align:center"><strong>调用其他Pigeon服务</strong></td></tr>
		<tr>
			<th class="left">Type</th>
			<th class="left">RemoteId</th>
			<th class="left">Method</th>
			<th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&serviceSort=${model.serviceSort}&callSort=total&queryName=${model.queryName}${model.customDate}">Total</a></th>
			<th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&serviceSort=${model.serviceSort}&callSort=failure&queryName=${model.queryName}${model.customDate}">Failure</a></th>
			<th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&serviceSort=${model.serviceSort}&callSort=failurePercent&queryName=${model.queryName}${model.customDate}">Failure%</a></th>
			<th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&serviceSort=${model.serviceSort}&callSort=avg&queryName=${model.queryName}${model.customDate}">Avg(ms)</a></th>
			<th class="right">QPS</th>
		</tr>
		<c:forEach var="callInfo" items="${model.methodInfo.callProjectsInfo}" varStatus="status">
			<tr class=" right">
		         	<td class="left">${callInfo.type}</td>
					<td class="left">${callInfo.ip}</td>
					<td class="left">${callInfo.id}</td>
		         	<td>${w:format(callInfo.totalCount,'#,###,###,###,##0')}</td>
		         	<td>${w:format(callInfo.failureCount,'#,###,###,###,##0')}</td>
		        	<td>${w:format(callInfo.failurePercent,'0.0000%')}</td>
		            <td>${w:format(callInfo.avg,'0.00')}</td>
		            <td>${w:format(callInfo.tps,'0.00')}</td>
		         </tr>
		</c:forEach>
		</c:if>

		<c:if test="${!empty model.methodInfo.serviceProjectsInfo}">
			<tr><td colspan="8" style="text-align:center"><strong>提供Pigeon服务 [ 服务器端数据 ]</strong></td>
			<c:if test="${!empty model.methodInfo.callerProjectsInfo}">
				<td></td>
				<td colspan="8" style="text-align:center"><strong>提供Pigeon服务 [ 客户端数据 ]</strong></td>
			</c:if>
			</tr>
		      <tr>
		         <th class="left">Type</th>
				 <th class="left">RemoteId</th>
				 <th class="left">Method</th>
		         <th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=total&queryName=${model.queryName}${model.customDate}">Total</a></th>
		         <th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=failure&queryName=${model.queryName}${model.customDate}">Failure</a></th>
		         <th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=failurePercent&queryName=${model.queryName}${model.customDate}">Failure%</a></th>
		         <th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=avg&queryName=${model.queryName}${model.customDate}">Avg(ms)</a></th>
		         <th class="right">QPS</th>
		         <c:if test="${!empty model.methodInfo.callerProjectsInfo}">
		         	 <th></th>
					 <th class="left">Type</th>
					 <th class="left">RemoteId</th>
					 <th class="left">Method</th>
			         <th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=total&queryName=${model.queryName}${model.customDate}">Total</a></th>
			         <th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=failure&queryName=${model.queryName}${model.customDate}">Failure</a></th>
			         <th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=failurePercent&queryName=${model.queryName}${model.customDate}">Failure%</a></th>
			         <th class="right"><a href="?op=historyMethod&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&ip=${model.ipAddress}&remote=${payload.remoteIp}&callSort=${model.callSort}&serviceSort=avg&queryName=${model.queryName}${model.customDate}">Avg(ms)</a></th>
			         <th class="right">QPS</th>
				</c:if>
		      </tr>
		      <c:forEach var="serviceInfo" items="${model.methodInfo.serviceProjectsInfo}" varStatus="status">
		         <tr class=" right">
		            <td class="left">${serviceInfo.type}</td>
					<td class="left">${serviceInfo.ip}</td>
					<td class="left">${serviceInfo.id}</td>
		            <td>${w:format(serviceInfo.totalCount,'#,###,###,###,##0')}</td>
		            <td>${w:format(serviceInfo.failureCount,'#,###,###,###,##0')}</td>
		            <td>${w:format(serviceInfo.failurePercent,'0.0000%')}</td>
		            <td>${w:format(serviceInfo.avg,'0.00')}</td>
		            <td>${w:format(serviceInfo.tps,'0.00')}</td>
		            <c:set var="id" value="${serviceInfo.id}"/>
		            <c:set var="callerInfo" value="${model.methodInfo.callerProjectsInfo}"/>
		            <c:if test="${!empty callerInfo}">
		            	<td></td>
		             	<td class="left">${callerInfo[id].type}</td>
						<td class="left">${callerInfo[id].ip}</td>
						<td class="left">${callerInfo[id].id}</td>
			            <td>${w:format(callerInfo[id].totalCount,'#,###,###,###,##0')}</td>
			            <td>${w:format(callerInfo[id].failureCount,'#,###,###,###,##0')}</td>
			            <td>${w:format(callerInfo[id].failurePercent,'0.0000%')}</td>
			            <td>${w:format(callerInfo[id].avg,'0.00')}</td>
			            <td>${w:format(callerInfo[id].tps,'0.00')}</td>
		            </c:if>
		         </tr>
		      </c:forEach>
		</c:if>
</table>
</jsp:body>
</a:historyReport>
