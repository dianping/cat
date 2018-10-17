<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>

<a:mobile>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#appCodes').addClass('active');
			$('#networkStatus').val(${model.code.networkStatus});
			$('#businessStatus').val(${model.code.businessStatus});
			
			var namespace = '${payload.namespace}';
			if (namespace != '') {
				$("#codeNamespace").val(namespace);
			}
		});
		
		$(document).delegate('#updateSubmit', 'click', function(e){
			var commandId = $("#commandId").val();
			var codeId = $("#codeId").val();
			var codeName = $("#codeName").val();
			var networkStatus = $("#networkStatus").val();
			var businessStatus = $("#businessStatus").val();
			var codeNamespace = $("#codeNamespace").val();
			
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
			if(codeNamespace == "undefined" || codeNamespace.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#codeNamespace").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			if(networkStatus == "undefined" || networkStatus.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#codeStatus").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			if(businessStatus == "undefined" || businessStatus.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#codeStatus").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			
			window.location.href = "/cat/s/app?op=appCodeSubmit&constant=${payload.constant}&id="+${payload.id}+"&domain=${payload.domain}&content="+codeId.trim()+":"+codeName.trim()+":"+networkStatus+":"+businessStatus+"&namespace="+codeNamespace;
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
		<c:choose>
		<c:when test="${payload.action.name eq 'appCodeUpdate' }">
			<td>返回码所属域</td><td>
			<select id="codeNamespace" style="width: 150px;" disabled>
				<c:forEach var="item" items="${model.apps}" varStatus="status" >
					<option value='${item.value.value}'>${item.value.value}</option>
				</c:forEach>
			</select>
			</td>
		</c:when>
		<c:otherwise>
			<td>返回码所属域</td><td>
			<select id="codeNamespace" style="width: 150px;">
				<c:forEach var="item" items="${model.apps}" varStatus="status">
					<option value='${item.value.value}'>${item.value.value}</option>
				</c:forEach>
			</select>
			</td>		
		</c:otherwise>
		</c:choose>
		</tr>
		<tr>
			<td>返回码说明</td><td><input name="codeName" value="${model.code.name}" id="codeName" /><span class="text-danger">（* 支持数字、字符，应小于32768，否则将会对其做模30000处理，如32768的返回码将会转换成2768）</span><br/>
		</td>
		</tr>
		<tr>
			<td>网络状态</td><td><select id="networkStatus" />
									<option value='0'>成功</option>
									<option value='1'>失败</option>
									</select><br/>
</td>
		</tr>
		<tr>
			<td>业务状态</td><td><select id="businessStatus" />
									<option value='0'>成功</option>
									<option value='1'>失败</option>
									</select><br/>
</td>
		</tr>
		<tr>
			<td colspan="2" style="text-align:center;"><button class="btn btn-primary" id="updateSubmit">提交</button></td>
		</tr>
	</table>

</a:mobile>
