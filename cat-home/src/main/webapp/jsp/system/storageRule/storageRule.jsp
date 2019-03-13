<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>
	
<a:config>
			<c:if test="${payload.type eq 'SQL'}"><c:set var="name" value="数据库" /></c:if>
		  	<c:if test="${payload.type eq 'Cache'}"><c:set var="name" value="缓存" /></c:if>
		  	<c:if test="${payload.type eq 'RPC'}"><c:set var="name" value="服务" /></c:if>
			<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
			<thead>
				<tr>
					<th width="20%"  class="center">${name}</th>
					<th width="20%"  class="center">机器</th>
					<th width="20%"  class="center">方法</th>
					<th width="20%" class="center">监控项</th>
					<th width="10%" class="center">与条件</th>
					<th width="10%" class="center">操作 <a href="?op=storageRuleUpdate&type=${payload.type}" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.rules}" varStatus="status">
					<c:set var="conditions" value="${fn:split(item.id, ';')}" />
					<c:set var="name" value="${conditions[0]}" />
					<c:set var="machine" value="${conditions[1]}" />
					<c:set var="method" value="${conditions[2]}" />
					<c:set var="attribute" value="${conditions[3]}" />
					<c:set var="andStr" value="${conditions[4]}" />
					<tr class="center">
						<td>${name}</td>
						<td>${machine}</td>
						<td>${method}</td>
						<td>
						<c:if test="${attribute eq 'error'}">错误数</c:if>
						<c:if test="${attribute eq 'errorPercent'}">错误率</c:if>
						<c:if test="${attribute eq 'avg'}">响应时间</c:if>
						</td>
						<td>
						<c:choose>
							<c:when test="${andStr eq 'true'}">
								<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
							</c:when>
						<c:otherwise>
							<i class="ace-icon glyphicon glyphicon-remove bigger-120 btn-danger"></i>
						</c:otherwise>
						</c:choose>
						</td>
						<td><a href="?op=storageRuleUpdate&ruleId=${item.id}&type=${payload.type}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=storageRuleDelete&ruleId=${item.id}&type=${payload.type}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#alert_config').addClass('active open');
			<c:if test="${payload.type eq 'SQL'}">
				$('#storageDatabaseRule').addClass('active');
			</c:if>
			<c:if test="${payload.type eq 'Cache'}">
				$('#storageCacheRule').addClass('active');
			</c:if>
			<c:if test="${payload.type eq 'RPC'}">
				$('#storageRPCRule').addClass('active');
			</c:if>
 		});
	</script>
</a:config>
