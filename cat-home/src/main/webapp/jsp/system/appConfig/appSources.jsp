<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context"
	scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.system.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model"
	scope="request" />

<a:mobile>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#appSources').addClass('active');
		
		})
		
			$(document).delegate('#updateSubmit', 'click', function(e){
			var constantId = $("#constantId").val();
			var constantName = $("#constantName").val();
			
			if(constantId == "undefined" || constantId.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#constantId").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			
			if(constantName == "undefined" || constantName.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#constantName").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			
			window.location.href = "/cat/s/app?op=appSourcesSubmit&content=来源:"+constantId.trim()+":"+constantName.trim();
		}) 
	</script>
	</script>
	<div style="margin-top:30px">
		<table style="width:75%;font-size:17px;" align="center" >
		<tr>
			<td>App ID</td>
			<td><input value="${model.appItem.id}" id="constantId" /><span class="text-danger">（* 仅支持数字）</span></td>
			<td>App名称</td>
			<td><input value="${model.appItem.value}" id="constantName" /></td>
			<td><button class="btn btn-primary btn-sm" id="updateSubmit">新增</button>
			</td>
		</tr>
		</table>
	</div>
	<div class="tab-content" style="margin-top:30px">
		<table class="table table-striped table-condensed table-bordered table-hover">
			<thead>
				<tr>
					<th>App ID</th>
					<th>App名称</th>
				</tr>
			</thead>

			<c:forEach var="e" items="${model.apps}">
				<tr>
					<td>${e.value.id}</td>
					<td>${e.value.value}</td>
			</c:forEach>
		</table>
	</div>
</a:mobile>