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
	<link rel="stylesheet" href="${model.webapp}/assets/css/bootstrap-datetimepicker.css">
	<script src="${model.webapp}/assets/js/bootstrap.datetimepicker.min.js" type="text/javascript"></script>
		<div id="queryBar">
			<div id="startDatePicker" class="input-append  date" style="margin-bottom: 0px;float:left;">
	           开始<input id="startTime" name="startTime"  size="16" 
	              data-format="yyyy-MM-dd hh:mm" value="<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm"/>"  type="text"></input> <span class="add-on">
	              <i class="ace-icon fa fa-calendar"></i>
	           </span>
	        </div>
	        <div id="endDatePicker" class="input-append  date" style="margin-bottom: 0px;float:left;">
	           &nbsp;&nbsp;结束<input id="endTime" name="endTime"  size="16" 
	              data-format="yyyy-MM-dd hh:mm" value="<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm"/>"  type="text"></input> <span class="add-on">
	              <i class="ace-icon fa fa-calendar"></i>
	           </span>
	        </div>
			&nbsp;&nbsp;项目
			<input type="text" name="domain" id="domain" value="${payload.domain}" style="height:auto" class="input-small">
			<input class="btn btn-primary  btn-sm"  value="查询" onclick="queryNew()" type="submit">
			<div>
				<label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="business"> 业务告警
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="network"> 网络告警
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="system"> 系统告警
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="exception"> 异常告警
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="heartbeat"> 心跳告警
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="thirdParty"> 第三方告警
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="frontEnd"> 前端告警
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="app"> App告警
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="web"> Web告警
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="zabbix"> Zabbix告警
				</label>
			</div>
		</div>
		<div id="alertReport">
			<table	class="table table-striped table-condensed   table-hover">
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
				initType("${payload.alertType}");
				
				$('#startDatePicker').datetimepicker({format: 'yyyy-MM-dd hh:mm'});
				$('#endDatePicker').datetimepicker({format: 'yyyy-MM-dd hh:mm'});
				
				$('#System_report').addClass('active open');
				$('#system_alert').addClass('active');
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
		</script>
</a:body>