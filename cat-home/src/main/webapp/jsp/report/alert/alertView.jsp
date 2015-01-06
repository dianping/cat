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
		  <span class="text-info">点击项目名可查看该项目下的具体告警</span>
		  <br/>
		  <c:set var="count" value="${payload.count}" />
		  <c:set var="modalId" value="0" />
	      <c:forEach var="minuteEntry" items="${model.alertMinutes}"  varStatus="itemStatus">
		      <table class="smallTable" style="float:left" border=1>  
		           <tr><th colspan="2" class="text-danger">${minuteEntry.key}</th></tr>
		           <tr><th>项目名</th><th>个</th></tr>
		           <c:set var="length" value="${fn:length(minuteEntry.value.alertDomains)}" />
		           <c:forEach var="alertDomain" items="${minuteEntry.value.alertDomains}" end="${count-1}">
			              <tr>
							 <td>
							 	<c:set var="id" value="modal${modalId}" />
							 	<c:set var="modalId" value="${modalId+1}" />
							 	<span data-id="${id}" class="alert-modal">
							 		${w:shorten(alertDomain.name, 18)}
							 	</span>
							 	<div class="modal fade" id="${id}" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
								  <div class="modal-dialog modal-lg">
								    <div class="modal-content">
								      <div class="modal-header">
								        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
								      </div>
								      <div class="modal-body">
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
								      </div>
								      <div class="modal-footer">
								        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
								      </div>
								    </div>
								  </div>
								</div>
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
		<script type="text/javascript">
			$(document).ready(function(){
				initType("${payload.alertType}");
				$(".alert-modal").click(function(){
					var targetId = $(this).data("id");
					$("#"+targetId).modal();
				});
				
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