<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
			<c:if test="${payload.type eq 'SQL'}"><c:set var="name" value="数据库" /></c:if>
		  	<c:if test="${payload.type eq 'Cache'}"><c:set var="name" value="缓存" /></c:if>
		  	<c:if test="${payload.type eq 'RPC'}"><c:set var="name" value="服务" /></c:if>
			<h3 class="text-center text-success">编辑${name}监控规则</h3>
			<form name="appRuleUpdate" id="form" method="post">
				<table style='width:100%' class='table table-striped table-condensed '>
				<c:set var="conditions" value="${fn:split(payload.ruleId, ';')}" />
				<c:set var="name" value="${conditions[0]}" />
				<c:set var="machine" value="${conditions[1]}" />
				<c:set var="method" value="${conditions[2]}" />
				<c:set var="target" value="${conditions[3]}" />
				<c:set var="andStr" value="${conditions[4]}" />
				<tr>
					<td>名字&nbsp;&nbsp;<input name="name" id="name" value="${name}"/></td>
					<td>机器&nbsp;&nbsp;<input name="machine" id="machine" value="${machine}"/></td>
					<td>方法&nbsp;&nbsp;<input name="method" id="method" value="${method}"/></td>
					<td>监控项&nbsp;&nbsp;<select name="target" id="target" style="width:200px;">
													<option value="avg">响应时间</option>
													<option value="errorPercent">错误率</option>
													<option value="error">错误数</option>
								            	</select></td>
					<td>&nbsp;&nbsp;与条件&nbsp;&nbsp;<select name="and" id="and" style="width:200px;">
													<option value="false">否</option>
													<option value="true">是</option>
								            	</select></td>
				</tr>
				<tr><th colspan="6">${model.content}</th></tr>
					<tr>
						<td style='text-align:center' colspan='6'><input class="btn btn-primary btn-sm" id="ruleSubmitButton" type="text" name="submit" value="提交"></button></td>
					</tr>
				</table>
			</form>
</a:config>

<script type="text/javascript">
function update() {
    var configStr = generateConfigsJsonString();
    var name = $("#name").val().trim();
    if(name == "undefined" || name == ""){
		if($("#errorMessage").length == 0){
			$("#name").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
		}
		return;
	}
    var machine = $("#machine").val().trim();
    if(machine == "undefined" || machine == ""){
		if($("#errorMessage").length == 0){
			$("#machine").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
		}
		return;
	}
    var method = $("#method").val().trim();
    if(method == "undefined" || method == ""){
    	if($("#errorMessage").length == 0){
			$("#machine").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
		}
		return;
	}
    var target = $("#target").val();
    if(target == "undefined" || target == ""){
    	if($("#errorMessage").length == 0){
			$("#target").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
		}
		return;
	}
    var andStr = $("#and").val();
    if(andStr == "undefined" || andStr == ""){
    	if($("#errorMessage").length == 0){
			$("#andStr").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
		}
		return;
	}
    
    var split = ";";
    var id = name + split + machine + split + method + split + target + split + andStr;
    window.location.href = "?op=storageRuleSubmit&configs=" + encodeURIComponent(configStr) + "&type=${payload.type}&ruleId=" + encodeURIComponent(id);
}

	$(document).ready(function() {
		initRuleConfigs(["DescVal","DescPer","AscVal","AscPer"]);
		var ruleId = "${payload.ruleId}";
		if(ruleId.length > 0){
			document.getElementById("name").disabled = true;
			document.getElementById("machine").disabled = true;
			document.getElementById("method").disabled = true;
			document.getElementById("target").disabled = true;
			document.getElementById("and").disabled = true;
			var conditions = ruleId.split(';');
			$('#target').val(conditions[3]);
			$('#and').val(conditions[4]);
		}
		var name = $("#name").val().trim();
		if(name == "" || name.length == 0){
			$("#name").val("*");
		}
		var machine = $("#machine").val().trim();
		if(machine == "" || machine.length == 0){
			$("#machine").val("*");
		}
		$('#alert_config').addClass('active open');
		<c:if test="${empty payload.type or payload.type eq 'SQL'}">
			$('#storageDatabaseRule').addClass('active');
		</c:if>
		<c:if test="${payload.type eq 'Cache'}">
			$('#storageCacheRule').addClass('active');
		</c:if>
		<c:if test="${payload.type eq 'RPC'}">
			$('#storageRPCRule').addClass('active');
		</c:if>
		$(document).delegate("#ruleSubmitButton","click",function(){
			update();
		})
	});
</script>