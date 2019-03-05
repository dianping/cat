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
			$('#appSpeedList').addClass('active');
		});
		
		$(document).delegate('#updateSubmit', 'click', function(e){
			var page = $("#page").val();
			var step = $("#step").val();
			var title = $("#title").val();
			var threshold = $("#threshold").val();
			
			if(typeof page != "undefined" && page.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#codeId").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			
			if(typeof step != "undefined" && step.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#codeName").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			if(typeof title != "undefined" && step.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#codeStatus").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			if(typeof threshold != "undefined" && threshold.trim().length == 0){
				if($("#errorMessage").length == 0){
					$("#codeStatus").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			
			window.location.href = "/cat/s/app?op=appSpeedSubmit&type=speed&id="+${payload.id}+"&content="+page+":"+step+":"+title+":"+threshold;
		}) 
	</script>
	
	<table class="table table-striped table-condensed  ">
		<c:choose>
		<c:when test="${payload.action.name eq 'appSpeedAdd' }">
		<tr><td>页面URL</td><td><input name="page" value="${model.speed.page}" id="page" />&nbsp;&nbsp;<span class="text-danger">*</span></td><td>支持数字、字符，例如：index.bin<br/></td></tr>
		</c:when>
		<c:otherwise>
		<tr>
			<tr><td>页面URL</td><td><input name="page" value="${model.speed.page}" id="page" readonly required/></td><td>支持数字、字符，例如：index.bin<br/></td></tr>
		</c:otherwise>
		</c:choose>
		<tr>
		<td>加载阶段</td><td><input name="step" value="${model.speed.step}" id="step" />&nbsp;&nbsp;<span class="text-danger">*</span></td><td>仅支持数字，表明属于页面加载的第几阶段，例如：1<br/>
		</td>
		<tr>
		<td>加载说明</td><td><input name="title" value="${model.speed.title}" id="title" /></td><td>支持数字、字符，例如：index.bin加载第一阶段<br/></td>
		</tr>
		<tr>
		<td>延时阈值</td><td><input name="threshold" value="${model.speed.threshold}" id="threshold" />&nbsp;&nbsp;<span class="text-danger">*</span></td><td>仅支持数字，单位毫秒，例如：100<br/></td>
		</tr>
		<tr>
			<td colspan="3" style="text-align:center;"><button class="btn btn-primary" id="updateSubmit">提交</button></td>
		</tr>
	</table>

</a:mobile>