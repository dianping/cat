<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:web_body>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#Web_config').addClass('active open');
			$('#aggregations').addClass('active');
		});
	</script>
			<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
			<thead>
				<tr >
					<th width="15%">报表类型</th>
					<th width="10%">域名</th>
					<th width="40%">规则</th>
					<th width="10%">告警阈值</th>
					<th width="15%">联系邮件</th>
					<!-- <th width="15%">显示名称</th>
					<th width="8%">示例</th> -->
					<th width="13%">操作 <a href="?op=aggregationUpdate" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.aggregationRules}"
					varStatus="status">
					<tr class="">
						<c:choose>
							<c:when test="${item.type == 1}">
								<td>transaction</td>
							</c:when>
							<c:when test="${item.type == 2}">
								<td>event</td>
							</c:when>
							<c:when test="${item.type == 3}">
								<td>problem
							</c:when>
						</c:choose>
						<td>${item.domain}</td>
						<td>${item.pattern}</td>
						<td>${item.warn}</td>
						<td>${item.mails}</td><%-- 
						<td>${item.displayName}</td>
						<td>${item.sample}</td> --%>
						<td><a href="?op=aggregationUpdate&pattern=${item.pattern}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=aggregationDelete&pattern=${item.pattern}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
</a:web_body>
