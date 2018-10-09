<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.web.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.web.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.web.Model" scope="request"/>

<a:web_body>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#Web_config').addClass('active open');
			$('#webRule').addClass('active');
 		});
	</script>
			<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
			<thead>
				<tr >
					<th width="15%">告警名</th>
					<th width="30%">链接</th>
					<th width="10%">返回码</th>
					<th width="6%">地区</th>
					<th width="6%">运营商</th>
					<th width="6%">网络类型</th>
					<th width="6%">告警指标</th>
					<th width="8%">操作 <a href="?op=webRuleUpdate" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.rules}" varStatus="status">
					<c:set var="strs" value="${fn:split(item.id, ':')}" />
					<c:set var="conditions" value="${fn:split(strs[0], ';')}" />
					<c:set var="command" value="${conditions[0]}" />
					<c:set var="code" value="${conditions[1]}" />
					<c:set var="city" value="${conditions[2]}" />
					<c:set var="operator" value="${conditions[3]}" />
					<c:set var="network" value="${conditions[4]}" />
					<c:set var="type" value="${strs[1]}" />
					<c:set var="name" value="${strs[2]}" />
					<tr class="">
						<td>${name}</td>
						<c:choose>
							<c:when test="${command ne -1}">
							<td>
							<c:forEach var="i" items="${model.patternItems}">
								<c:if test="${i.value.id eq command}">${i.value.pattern}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${code ne -1}">
							<td>
							<c:forEach var="i" items="${model.webCodes}">
								<c:if test="${i.value.id eq code}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${city ne -1}">
							<td>
							<c:forEach var="i" items="${model.webCities}">
							<c:if test="${i.value.id eq city}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${operator ne -1}">
							<td>
							<c:forEach var="i" items="${model.webOperators}">
							<c:if test="${i.value.id eq operator}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
							<c:choose>
							<c:when test="${network ne -1}">
							<td>
							<c:forEach var="i" items="${model.webNetworks}">
							<c:if test="${i.value.id eq network}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						<td>
							<c:if test="${type eq 'request'}">请求数</c:if> 
							<c:if test="${type eq 'success'}">成功率</c:if>  
							<c:if test="${type eq 'delay'}">响应时间</c:if>
						</td>
						<td><a href="?op=webRuleUpdate&ruleId=${item.id}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=webRuleDelete&ruleId=${item.id}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
</a:web_body>
