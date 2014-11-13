<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
	<div>
		<div class="row-fluid">
        <div class="span2">
		<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
		
		</br>
			<h3 class="text-center text-success">编辑Transaction监控规则</h3>
			<form name="appRuleUpdate" id="form" method="post">
				<table style='width:100%' class='table table-striped table-bordered'>
				<c:set var="conditions" value="${fn:split(payload.ruleId, ';')}" />
				<c:set var="domain" value="${conditions[0]}" />
				<c:set var="type" value="${conditions[1]}" />
				<c:set var="name" value="${conditions[2]}" />
				<tr>
					<td>&nbsp;&nbsp;项目&nbsp;&nbsp;<input name="domain" id="domain" value="${domain}"/>
					&nbsp;&nbsp;Type&nbsp;&nbsp;<input name="type" id="type" value="${type}"/>
					&nbsp;&nbsp;Name&nbsp;&nbsp;<input name="name" id="name" value="${name}"/>（默认为All）</td>
				</tr>
				<tr><th>${model.content}</th></tr>
					<tr>
						<td style='text-align:center' colspan='2'><input class="btn btn-primary btn-mini" id="ruleSubmitButton" type="text" name="submit" value="提交"></button></td>
					</tr>
				</table>
			</form> </div></div></div>
</a:body>

<script type="text/javascript">
function update() {
    var configStr = generateConfigsJsonString();
    var domain = $("#domain").val();
    if(domain == "undefined" || domain == ""){
		if($("#errorMessage").length == 0){
			$("#domain").after($("<span class=\"text-error\" id=\"errorMessage\">  该字段不能为空</span>"));
		}
		return;
	}
    var type = $("#type").val();
    if(type == "undefined" || type == ""){
		if($("#errorMessage").length == 0){
			$("#type").after($("<span class=\"text-error\" id=\"errorMessage\">  该字段不能为空</span>"));
		}
		return;
	}
    var name = $("#name").val();
    if(name == "undefined" || name == ""){
		name = "All";
		$("#domain").val("All");
	}
    var split = ";";
    var id = domain + split + type + split + name;
    window.location.href = "?op=transactionRuleSubmit&configs=" + configStr + "&ruleId=" + id;
}

	$(document).ready(function() {
		initRuleConfigs(["DescVal","DescPer","AscVal","AscPer","FluAscPer", "FluDescPer", "MinVal", "SumMaxVal", "SumMinVal"]);
		var ruleId = "${payload.ruleId}";
		if(ruleId.length > 0){
			document.getElementById("domain").disabled = true;
			document.getElementById("type").disabled = true;
			document.getElementById("name").disabled = true;
		}
		var name = $("#name").val();
		if(name == "" || name.length == 0){
			$("#name").val("All");
		}
		
		$('#transactionRule').addClass('active');
		$(document).delegate("#ruleSubmitButton","click",function(){
			update();
		})
	});
</script>