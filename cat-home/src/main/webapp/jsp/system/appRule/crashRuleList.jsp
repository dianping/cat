<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>

<a:mobile>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#crashRule').addClass('active');
 		});
	</script>
	<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
			<thead>
				<tr >
					<th width="10%">App Id</th>
					<th width="15%">平台</th>
					<th width="10%">模块</th>
					<th width="10%">Warning阈值</th>
					<th width="10%">Error阈值</th>
					<th width="35%">联系邮件</th>
					<th width="10%">操作 <a href="?op=crashRuleUpdate" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.crashLimits}"
					varStatus="status">
					<tr class="">
						<td>${item.appId}</td>
						<td>${item.platform}</td>
						<td>${item.module}</td>
						<td>${item.warnings}</td>
						<td>${item.errors}</td>
						<td>${item.mails}</td>
						<td><a href="?op=crashRuleUpdate&ruleId=${item.id}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=crashRuleDelete&ruleId=${item.id}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
</a:mobile>
