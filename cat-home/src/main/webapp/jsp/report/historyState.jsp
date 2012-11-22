<%@ page contentType="text/html; charset=utf-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.state.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.state.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.state.Model" scope="request"/>

<a:report title="CAT State Report" navUrlPrefix="domain=${model.domain}">
	<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}  &nbsp;&nbsp;&nbsp;&nbsp;CAT项目指标</jsp:attribute>
	<jsp:body>	
	<res:useCss value="${res.css.local.matrix_css}" target="head-css" />
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['flotr2_js']}" target="head-js"/>
<br>

<table class="machines">
	<tr style="text-align:left">
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
</br>

<table width="50%">
	<tr class='odd'>
		<td width="30%">处理消息总量</td>
		<td width="20%">${w:format(model.state.total.total,'0.#')}</td>
		<td width="40%">丢失消息总量</td>
		<td width="10%">${w:format(model.state.total.totalLoss,'0.#')}</td>
	</tr>
	<tr class='even'>
		<td>每分钟平均处理数</td>
		<td>${w:format(model.state.total.avgTps,'0.#')}</td>
		<td>每分钟最大处理数</td>
		<td>${w:format(model.state.total.maxTps,'0.#')}</td>
	</tr>
	<tr class='odd'>
		<td>存储消息数量</td>
		<td>${w:format(model.state.total.dump,'0.#')}</td>
		<td>压缩前消息大小(GB)</td>
		<td>${w:format(model.state.total.size/1024/1024/1024,'0.00#')}</td>
	</tr>
	<tr class='even'>
		<td>系统处理延迟(ms)</td>
		<td>${w:format(model.state.total.delayAvg,'0.#')}</td>
	</tr>
</table>
</br>
<table width="100%">
	<tr  class='odd'>
		<td width="15%">处理项目列表</td><td>项目对应机器列表</td>
	</tr>
	<c:forEach var="item" items="${model.state.processDomains}"
				varStatus="status">
		<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
			<td>${item.name}</td>
			<td>${item.ips}</td>
		</tr>
	</c:forEach>
</table>
<br>
</jsp:body>
</a:report>

<script type="text/javascript">
$(document).ready(function(){
	$('.position').hide();
});
</script>

