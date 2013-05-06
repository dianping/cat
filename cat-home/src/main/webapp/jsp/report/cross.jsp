<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.cross.Context" scope="request" />
<jsp:useBean id="payload"  	type="com.dianping.cat.report.page.cross.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.cross.Model" scope="request" />

<a:report title="Cross Report"
	navUrlPrefix="ip=${model.ipAddress}&domain=${model.domain}">

	<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>

<res:useCss value="${res.css.local.cross_css}" target="head-css" />

<%@ include file="crossQuery.jsp" %>
</br>
<table class="machines">
	<tr style="text-align: left">
		<th>Machines: &nbsp;[&nbsp; <c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?domain=${model.domain}&date=${model.date}"
								class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&date=${model.date}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
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
	</tr>
</table>
<br>
<table class='cross'>
		<c:if test="${!empty model.projectInfo.callProjectsInfo}">
		<tr>
			<th class="left">Type(本项目调用其他Pigeon服务)</th>
			<th class="left"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&serviceSort=${model.serviceSort}&callSort=name">RemoteProject</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&serviceSort=${model.serviceSort}&callSort=total">Total</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&serviceSort=${model.serviceSort}&callSort=failure">Failure</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&serviceSort=${model.serviceSort}&callSort=failurePercent">Failure%</a></th>
			<th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&serviceSort=${model.serviceSort}&callSort=avg">Avg(ms)</a></th>
			<th>QPS</th>
		</tr>
		<c:forEach var="callInfo" items="${model.projectInfo.callProjectsInfo}" varStatus="status">
			<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
		         	<td class="left">${callInfo.type}</td>
		         	<td class="left"><a href="?op=host&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&project=${callInfo.projectName }">${callInfo.projectName}</a></td>
		            <td>${w:format(callInfo.totalCount,'#,###,###,###,##0')}</td>
		         	<td>${w:format(callInfo.failureCount,'#,###,###,###,##0')}</td>
		        	 <td>${w:format(callInfo.failurePercent,'0.00%')}</td>
		             <td>${w:format(callInfo.avg,'0.00')}</td>
		             <td>${w:format(callInfo.tps,'0.00')}</td>
		         </tr>
		</c:forEach>
		<tr><td>&nbsp</td></tr>
		</c:if>

		<c:if test="${!empty model.projectInfo.serviceProjectsInfo}">
		      <tr>
		         <th class="left">Type(从服务端看，Pigeon服务数据)</th>
		         <th class="left"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&callSort=${model.callSort}&serviceSort=name">RemoteProject</a></th>
		         <th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&callSort=${model.callSort}&serviceSort=total">Total</a></th>
		         <th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&callSort=${model.callSort}&serviceSort=failure">Failure</a></th>
		         <th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&callSort=${model.callSort}&serviceSort=failurePercent">Failure%</a></th>
		         <th><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&callSort=${model.callSort}&serviceSort=avg">Avg(ms)</a></th>
		         <th>QPS</th>
		      </tr>
		      <c:forEach var="serviceInfo" items="${model.projectInfo.serviceProjectsInfo}" varStatus="status">
		         <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
		            <td class="left">${serviceInfo.type}</td>
		            <td class="left"><a href="?op=host&domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&project=${serviceInfo.projectName }">${serviceInfo.projectName}</a></td>
		            <td>${w:format(serviceInfo.totalCount,'#,###,###,###,##0')}</td>
		            <td>${w:format(serviceInfo.failureCount,'#,###,###,###,##0')}</td>
		            <td>${w:format(serviceInfo.failurePercent,'0.00%')}</td>
		             <td>${w:format(serviceInfo.avg,'0.00')}</td>
		             <td>${w:format(serviceInfo.tps,'0.00')}</td>
		         </tr>
		      </c:forEach>
		      </c:if>
		     <tr><td>&nbsp</td></tr>
			 <c:if test="${!empty model.projectInfo.callServiceProjectsInfo}">
		      <tr>
		         <th class="left">Type(从客户端看，Pigeon服务数据)</th>
		         <th class="left">RemoteProject</th>
		         <th>Total</th>
		         <th>Failure</th>
		         <th>Failure%</th>
		         <th>Avg(ms)</th>
		         <th>QPS</th>
		      </tr>
		      <c:forEach var="serviceInfo" items="${model.projectInfo.callServiceProjectsInfo}" varStatus="status">
		         <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
		            <td class="left">${serviceInfo.type}</td>
		            <td class="left">${serviceInfo.projectName}</td>
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
</a:report>
