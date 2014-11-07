<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.alert.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.alert.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.alert.Model" scope="request" />

<a:body>
	<jsp:body>
		<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
		<res:useCss value="${res.css.local['alert.css']}" target="head-css" />
		<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
		<div id="queryBar">
			<div class="text-left"></div>
			开始
			<div id="startDatePicker" class="input-append date" >
				<input name="startTime" id="startTime" style="height:auto; width: 150px;" 
				value="<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm"/>" type="text"></input> 
				<span class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"></i> </span>
			</div>
			结束
			<div id="endDatePicker" class="input-append date" >
				<input name="endTime" id="endTime" style="height:auto; width: 150px;" 
				value="<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm"/>" type="text"></input> 
				<span class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"></i> </span>
			</div>
			项目
			<input type="text" name="domain" id="domain" value="${payload.domain}" style="height:auto" class="input-small">
			<input class="btn btn-primary  btn-small"  value="查询" onclick="queryNew()" type="submit">
			<a id="fullScreen" class='btn btn-small btn-primary' onclick="queryFullScreen()">全屏</a>
			<a id="refresh10" class='btn btn-small btn-primary' onclick="queryFrequency(10)">10秒</a>
			<a id="refresh20" class='btn btn-small btn-primary' onclick="queryFrequency(20)">20秒</a>
			<a id="refresh30" class='btn btn-small btn-primary' onclick="queryFrequency(30)">30秒</a>
			<br>
			告警类型（可多选）&nbsp;&nbsp;
			<div class="types">
				<label class="checkbox inline">
				  <input class="type" type="checkbox" value="business"> 业务告警
				</label>
				<label class="checkbox inline">
				  <input class="type" type="checkbox" value="network"> 网络告警
				</label>
				<label class="checkbox inline">
				  <input class="type" type="checkbox" value="system"> 系统告警
				</label>
				<label class="checkbox inline">
				  <input class="type" type="checkbox" value="exception"> 异常告警
				</label>
				<label class="checkbox inline">
				  <input class="type" type="checkbox" value="heartbeat"> 心跳告警
				</label>
				<label class="checkbox inline">
				  <input class="type" type="checkbox" value="thirdParty"> 第三方告警
				</label>
				<label class="checkbox inline">
				  <input class="type" type="checkbox" value="frontEnd"> 前端告警
				</label>
				<label class="checkbox inline">
				  <input class="type" type="checkbox" value="app"> App告警
				</label>
				<label class="checkbox inline">
				  <input class="type" type="checkbox" value="web"> Web告警
				</label>
			</div>
			<br><br>
		</div>
		<div id="alertReport">
			<table	class="problem table table-striped table-bordered table-condensed table-hover">
				<tr class="text-success">
					<th width="10%">时间</th>
					<th width="5%">类型</th>
					<th width="5%">级别</th>
					<th width="10%">项目</th>
					<th width="10%">指标</th>
					<th width="60%">内容</th>
				</tr>
				<c:forEach var="entry" items="${model.alerts}" varStatus="status">
					<tr class="noter">
						<td rowspan="${fn:length(entry.value)+1}">${entry.key}</td>
						<td style="display:none" colspan="5"></td>
					</tr>
					<c:forEach var="alert" items="${entry.value}" varStatus="status">
						<c:set var="category" value="${alert.category}"/>
						<tr class="${category}">
							<td>${category}</td>
							<td>${alert.type}</td>
							<td>${alert.domain}</td>
							<td>${alert.metric}</td>
							<td>${alert.content}</td>
						</tr>
					</c:forEach>
				</c:forEach>
			</table>
		</div>
		<script type="text/javascript">
			$(document).ready(function(){
				<c:if test="${payload.fullScreen}">
					$('#fullScreen').addClass('btn-danger');
					$('.navbar').hide();
					$('.footer').hide();
				</c:if>
				
				initType("${payload.alertType}");
				
				$('#startDatePicker').datetimepicker({format: 'yyyy-MM-dd hh:mm'});
				$('#endDatePicker').datetimepicker({format: 'yyyy-MM-dd hh:mm'});
				
				var refresh = ${payload.refresh};
				var frequency = ${payload.frequency};
				if(refresh){
					$('#refresh'+frequency).addClass('btn-danger');
					setTimeout(refreshPage,frequency*1000);
				};
			});
			function initType(rawStr){
				if(rawStr == null || rawStr == ""){
					$(".type").each(function(){
						$(this).prop("checked", true);
					});
				}else{
					var strs = rawStr.split(",");
					
					for(var count in strs){
						str = strs[count];
						if(str !=null && str !=""){
							$("input[value='"+str+"']").prop("checked", true);
						}
					}
				}
			}
			function getType(){
				var typeStr = "";
				
				$(".type").filter(function(){
					return $(this).prop("checked");
				}).each(function(){
					typeStr += $(this).val() + ",";
				});
				return typeStr;
			}
			function queryNew(){
				var startTime=$("#startTime").val();
				var endTime=$("#endTime").val();
				var domain=$("#domain").val();
				window.location.href="?op=view&domain="+domain+"&startTime="+startTime+"&endTime="+endTime+"&fullScreen=${payload.fullScreen}&alertType="+getType();
			}
			function queryFullScreen(){
				var domain=$("#domain").val();
				var isFullScreen = ${payload.fullScreen};
				window.location.href="?op=view&domain="+domain+"&startTime="+startTime+"&endTime="+endTime+"&refresh="+${payload.refresh}+"&frequency="+${payload.frequency}+"&fullScreen="+!isFullScreen+"&alertType="+getType();
			}
			function queryFrequency(frequency){
				var domain=$("#domain").val();
				window.location.href="?op=view&domain="+domain+"&fullScreen=${payload.fullScreen}&refresh=true&frequency="+frequency+"&alertType="+getType();
			}
			function refreshPage(){
				var domain=$("#domain").val();
				window.location.href="?op=view&domain="+domain+"&fullScreen=${payload.fullScreen}&refresh=true&frequency="+${payload.frequency}+"&alertType="+getType();
			}
		</script>
	</jsp:body>
</a:body>