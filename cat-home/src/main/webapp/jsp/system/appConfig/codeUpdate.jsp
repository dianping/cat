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
			$('#userMonitor_config').addClass('active open');
			$('#appList').addClass('active');
			$('#codeStatus').val(${model.code.status});
		});
		
		$(document).delegate('#updateSubmit', 'click', function(e){
			var commandId = $("#commandId").val();
			var codeId = $("#codeId").val();
			var codeName = $("#codeName").val();
			var codeStatus = $("#codeStatus").val();
			
			if(codeId == "undefined" || codeId.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#codeId").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			
			if(codeName == "undefined" || codeName.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#codeName").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			if(codeStatus == "undefined" || codeStatus.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#codeStatus").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			
			window.location.href = "/cat/s/config?op=appCodeSubmit&type=code&constant=${payload.constant}&id="+${payload.id}+"&domain=${payload.domain}&content="+codeId.trim()+":"+codeName.trim()+":"+codeStatus;
		}) 
	</script>
	
	<table class="table table-striped table-condensed table-bordered ">
		<c:if test="${payload.action.name eq 'appCodeUpdate' and model.updateCommand != null}">
		<tr>
			<td>命令字</td><td><input name="commandId" value="${model.updateCommand.name}" id="commandId" disabled /><br/>
		</td>
		</c:if>
		<tr>
		<c:choose>
		<c:when test="${payload.action.name eq 'appCodeUpdate' }">
			<td>返回码</td><td><input name="codeId" value="${model.code.id}" id="codeId" disabled /><br/>
		</td>
		</c:when>
		<c:otherwise>
			<td>返回码</td><td><input name="codeId" value="${model.code.id}" id="codeId" /><span class="text-danger">（* 仅支持数字）</span><br/>
		</c:otherwise>
		</c:choose>
		<tr>
			<td>返回码说明</td><td><input name="codeName" value="${model.code.name}" id="codeName" /><span class="text-danger">（* 支持数字、字符）</span><br/>
</td>
</tr>
<tr>
			<td>返回码状态</td><td><select id="codeStatus" />
									<option value='0'>成功</option>
									<option value='1'>失败</option>
									</select><br/>
</td>
		</tr>
		<tr>
			<td colspan="2" style="text-align:center;"><button class="btn btn-primary" id="updateSubmit">提交</button></td>
		</tr>
	</table>

</a:config>
