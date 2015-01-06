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
					<th width="20%">项目组</th>
					<th width="72%">组</th>
					<th width="8%">操作 <a href="?op=domainGroupConfigUpdate" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead>
				<tbody>

				<c:forEach var="item" items="${model.domainGroup.domains}" varStatus="status">
					<tr class="">
						<td>${item.value.id}</td>
						<td>
						<c:forEach var="entry" items="${item.value.groups}">
						[${entry.value.id}]&nbsp;
						</c:forEach>
						</td>
						<td><a href="?op=domainGroupConfigUpdate&domain=${item.value.id}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=domainGroupConfigDelete&domain=${item.value.id}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
			</table>
</a:config>
<script type="text/javascript">
		$(document).ready(function() {
			$('#projects_config').addClass('active open');
			$('#domainGroupConfigUpdate').addClass('active');
		});
</script>