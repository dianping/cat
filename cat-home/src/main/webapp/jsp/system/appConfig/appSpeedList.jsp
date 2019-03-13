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
			$('#appSpeedList').addClass('active');
			
			var state = '${model.opState}';
			if(state=='Success'){
				$('#state').html('操作成功');
			}else{
				$('#state').html('操作失败');
			}
			setTimeout(function(){
				$('#state').html('&nbsp;');
			},3000);
 		});
	</script>
			<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
				<thead>
				<tr >
					<th width="20%">页面</th>
					<th width="20%">加载阶段</th>
					<th width="20%">说明</th>
					<th width="20%">延时阈值(毫秒)</th>
					<th width="8%">操作 <a href="?op=appSpeedAdd&type=speed" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr>
				</thead>
				<tbody>
				<c:forEach var="entry" items="${model.speeds}">
				<c:set var="item" value="${entry.value}"/>
					<tr>
						<td>${item.page}</td>
						<td>${item.step}</td>
						<td>${item.title}</td>
						<td>${item.threshold}</td>
						<td><a href="?op=appSpeedUpdate&id=${item.id}&type=speed" class="btn btn-primary btn-xs">
			<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
			<a href="?op=appSpeedDelete&id=${item.id}&type=speed" class="btn btn-danger btn-xs delete" >
			<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach>
				</tbody>
			</table>
</a:mobile>
