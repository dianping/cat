<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<a:config>
	<script src="${model.webapp}/assets/js/bootstrap-tag.min.js"></script>

			<table class="table table-striped table-condensed table-bordered  table-hover" id="contents">
			<thead>
				<tr >
					<th width="67%">url (http监控)</th>
					<th width="5%">类型</th>
					<th width="20%">项目组</th>
					<th width="8%">操作 <a href="?op=thirdPartyRuleUpdate&type=http" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead>
				<tbody>

				<c:forEach var="item" items="${model.thirdPartyConfig.https}" varStatus="status">
					<tr class="">
						<td>${item.url}</td>
						<td>${item.type}</td>
						<td>${item.domain}</td>
						<td><a href="?op=thirdPartyRuleUpdate&ruleId=${item.url}&type=http" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=thirdPartyRuleDelete&ruleId=${item.url}&type=http" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
			</table>
			<table class="table table-striped table-condensed table-bordered  table-hover" id="contents">
			<thead>
				<tr >
					<th width="67%">Ip (socket监控)</th>
					<th width="5%">端口</th>
					<th width="20%">项目组</th>
					<th width="8%">操作 <a href="?op=thirdPartyRuleUpdate&type=socket" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead>
				<tbody>

				
				<c:forEach var="item" items="${model.thirdPartyConfig.sockets}" varStatus="status">
					<tr class="">
						<td>${item.ip}</td>
						<td>${item.port}</td>
						<td>${item.domain}</td>
						<td><a href="?op=thirdPartyRuleUpdate&ruleId=${item.ip}-${item.port}&type=socket" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=thirdPartyRuleDelete&ruleId=${item.ip}-${item.port}&type=socket" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach>
				</tbody>
			</table>
			
</a:config>
<script type="text/javascript">
		$(document).ready(function() {
			$('#alert_config').addClass('active open');
			$('#thirdPartyConfigUpdate').addClass('active');
		});
</script>