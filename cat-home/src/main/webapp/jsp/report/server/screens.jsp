<%@ page contentType="text/html; charset=utf-8" isELIgnored="false"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.server.Context"
	scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.server.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.server.Model"
	scope="request" />
<a:serverBody> 
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	<table class="table table-striped table-condensed table-bordered  table-hover" id="contents">
			<thead>
				<tr >
					<th width="20%">项目组</th>
					<th width="72%">组</th>
					<th width="8%">操作 <a href="?op=screenUpdate" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead>
				<tbody>

				<c:forEach var="entry" items="${model.metricScreenInfos}" varStatus="status">
					<tr class="">
						<td>${entry.key}</td>
						<td>
						<c:forEach var="e" items="${entry.value}">
							[<a href="?op=graphUpdate&screen=${entry.key}&graph=${e.key}">${e.key}</a>]&nbsp;
						</c:forEach>
						</td>
						<td><a href="?op=screenUpdate&screen=${entry.key}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=screenDelete&screen=${entry.key}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
			</table>

	<script type="text/javascript">
		$(document).ready(function() {
			$('#serverConfig').addClass('active open');
			$('#serverScreens').addClass('active');
		});
	</script>

</a:serverBody>