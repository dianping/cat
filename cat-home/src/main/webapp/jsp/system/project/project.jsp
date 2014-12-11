<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	
	<script type="text/javascript">
		$(document).ready(function() {
			$('#projects_config').addClass('active open');
			$('#projects').addClass('active');
			$(".btn-danger").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});
		});
	</script>
	
	<div>
			<table class="table table-striped table-condensed table-bordered table-hover">
			<thead>
				<tr >
					<th width="20%">项目名</th>
					<th width="15%">CMDB</th>
					<th width="5%">级别</th>
					<th width="10%">BU</th>
					<th width="10%">CMDB产品线</th>
					<th width="15%">组邮件</th>
					<th width="15%">组号码</th>
					<th width="10%">操作</th>
				</tr></thead><tbody>
				<c:forEach var="item" items="${model.projects}"
					varStatus="status">
					<tr class="">
						<td>${item.domain}</td>
						<td>${item.cmdbDomain}</td>
						<td>${item.level}</td>
						<td>${item.bu}</td>
						<td>${item.cmdbProductline}</td>
						<td>${item.email}</td>
						<td>${item.phone}</td>
						<td><a href="?op=update&projectId=${item.id}" class="btn btn-primary btn-sm">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=projectDelete&projectId=${item.id}" class="btn btn-danger btn-sm" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
				</c:forEach></tbody>
			</table>
		</div>
</a:config>