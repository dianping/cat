<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.web.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.web.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.web.Model" scope="request"/>

<a:web_body>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#Web_config').addClass('active open');
			$('#urlPatterns').addClass('active');
		});
	</script>
			<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
			<thead>
				<tr >
					<th width="15%">唯一ID</th>
					<th width="15%">属于组</th>
					<th width="42%">Pattern内容</th>
					<th width="15%">项目名</th>
					<th width="8%">操作 <a href="?op=urlPatternUpdate" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead><tbody>

				<c:forEach var="item" items="${model.patternItems}"
					varStatus="status">
					<tr class="">
						<td>${item.value.name}</td>
						<td>${item.value.group}</td>
						<td>${item.value.pattern}</td>
						<td>${item.value.domain}</td>
						<td><a href="?op=urlPatternUpdate&key=${item.value.name}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=urlPatternDelete&key=${item.value.name}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
				</tbody>
			</table>
</a:web_body>
