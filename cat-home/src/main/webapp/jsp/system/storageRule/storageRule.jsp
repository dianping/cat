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
			<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
			<thead>
				<tr>
					<c:if test="${empty payload.type or payload.type eq 'database'}">
						<th width="20%">数据库</th>
					</c:if>
					<c:if test="${payload.type eq 'cache'}">
						<th width="20%">缓存</th>
					</c:if>
					<th width="30%">机器</th>
					<th width="20%">方法</th>
					<th width="20%">监控项</th>
					<th width="10%">操作 <a href="?op=storageRuleUpdate" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.rules}" varStatus="status">
					<c:set var="conditions" value="${fn:split(item.id, ';')}" />
					<c:set var="name" value="${conditions[0]}" />
					<c:set var="machine" value="${conditions[1]}" />
					<c:set var="method" value="${conditions[2]}" />
					<c:set var="attribute" value="${conditions[3]}" />
					<tr class="">
						<td>${name}</td>
						<td>${machine}</td>
						<td>${method}</td>
						<td>
						<c:if test="${attribute eq 'error'}">错误率</c:if>
						<c:if test="${attribute eq 'avg'}">响应时间</c:if>
						</td>
						<td><a href="?op=storageRuleUpdate&ruleId=${item.id}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=storageRuleDelete&ruleId=${item.id}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#alert_config').addClass('active open');
			<c:if test="${empty payload.type or payload.type eq 'database'}">
				$('#storageDatabaseRule').addClass('active');
			</c:if>
			<c:if test="${payload.type eq 'cache'}">
				$('#storageCacheRule').addClass('active');
			</c:if>
 		});
	</script>
</a:config>
