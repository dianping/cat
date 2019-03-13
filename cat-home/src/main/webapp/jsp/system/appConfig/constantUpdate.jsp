<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>

<a:mobile>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#appConstants').addClass('active');
			$('#codeStatus').val(${model.code.status});
		});
		
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
			
			window.location.href = "/cat/s/app?op=appConstantSubmit&type=${payload.type}&domain=${model.domain}&content=${payload.type}:"+constantId.trim()+":"+constantName.trim();
		}) 
	</script>
	
	<table class="table table-striped table-condensed table-bordered ">
		<c:choose>
		<c:when test="${payload.action.name eq 'appConstantUpdate' }">
			<td>ID</td><td><input value="${model.appItem.id}" id="constantId" disabled />
		</td>
		</c:when>
		<c:otherwise>
			<td>ID</td><td><input value="${model.appItem.id}" id="constantId" /><span class="text-danger">（* 仅支持数字）</span><br/></td>
		</c:otherwise>
		</c:choose>
		<tr>
			<td>值</td><td><input value="${model.appItem.value}" id="constantName" /></td>
		</tr>
		<tr>
			<td colspan="2" style="text-align:center;"><button class="btn btn-primary" id="updateSubmit">提交</button></td>
		</tr>
	</table>

</a:mobile>
