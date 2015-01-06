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
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
		<div id="queryBar">
			 <div style="float:left;">
		&nbsp;开始
		<input type="text" id="startTime" style="width:150px;"/>
		结束
		<input type="text" id="endTime" style="width:150px;"/>
		&nbsp;&nbsp;项目
		<input type="text" name="domain" id="domain" value="${payload.domain}" style="height:auto" class="input-small">
		&nbsp;&nbsp;每分钟显示个数
		<input type="text" id="count" value="${payload.count}" style="width:100px;" style="height:auto" class="input-small"/>
		<input class="btn btn-primary  btn-sm"  style="margin-bottom:4px;" value="查询" onclick="queryNew()" type="submit"></div>
			<div style="float:left;">
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
		<br/><br/>
		<div id="alert-minutes">
		  <br/><br/>
		  <c:set var="count" value="${payload.count}" />
	      <c:forEach var="minuteEntry" items="${model.alertMinutes}"  varStatus="itemStatus">
		      <table class="smallTable" style="float:left" border=1>  
		           <tr><th colspan="2" class="text-danger">${minuteEntry.key}</th></tr>
		           <tr><th>项目名</th><th>个</th></tr>
		           <c:set var="length" value="${fn:length(minuteEntry.value.alertDomains)}" />
		           <c:forEach var="alertDomain" items="${minuteEntry.value.alertDomains}" end="${count-1}">
			              <tr>
							 <td>
							 	<span class="popoverTip" data-container="body" data-toggle="popover" data-html="true" data-placement="bottom" 
							 	data-content="">
							 		${w:shorten(alertDomain.name, 18)}
							 		<c:forEach var="alertCategory" items="${alertDomain.alertCategories}">
							 			告警类型：${alertCategory.key}
							 			<table	class="table table-striped table-condensed   table-hover">
											<tr class="text-success">
												<th width="5%">类型</th>
												<th width="5%">级别</th>
												<th width="10%">项目</th>
												<th width="10%">指标</th>
												<th width="60%">内容</th>
											</tr>
											<c:forEach var="alert" items="${alertCategory.value}">
												<tr>
													<td>${alert.category}</td>
													<td>${alert.type}</td>
													<td>${alert.domain}</td>
													<td>${alert.metric}</td>
													<td>${alert.content}</td>
												</tr>
											</c:forEach>
										</table>
							 		</c:forEach>
							 	</span>
							 </td>
	                		 <td style="text-align:right">${w:format(alertDomain.count,'0')}</td>
			              </tr>
		           </c:forEach>
		           <c:if test="${length lt count}">
		           		<c:forEach begin="1" end="${count-length}">
		           			<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
		           		</c:forEach>
				   </c:if>
		      </table>
	      </c:forEach>
	    </div>
		<!-- <%-- <a class="hreftip" href="/cat/r/p?domain=${detail.domain}&date=${date}" title="${detail.errorInfo}">
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
		--%>
		-->
		<script type="text/javascript">
			$(document).ready(function(){
				initType("${payload.alertType}");
				$('.popoverTip').popover();
				
				$('#startTime').datetimepicker({
					format:'Y-m-d H:i',
					step:30,
					maxDate:0
				});
				$('#endTime').datetimepicker({
					format:'Y-m-d H:i',
					step:30,
					maxDate:0
				});
				$('#startTime').val("${w:format(payload.startTime,'yyyy-MM-dd HH:mm')}");
				$('#endTime').val("${w:format(payload.endTime,'yyyy-MM-dd HH:mm')}");
				
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
				var count=$("#count").val();
				window.location.href="?op=view&domain="+domain+"&startTime="+startTime+"&endTime="+endTime+"&fullScreen=${payload.fullScreen}&alertType="+getType()+"&count="+count;
			}
		</script>
</a:body>