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

<a:application>
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
			<div style="float:left;" id="type-group">
				<label class="btn btn-info btn-sm">
				  <input id="select-all" type="checkbox"> All
				</label><label class="btn btn-info btn-sm">
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
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="database"> DB告警
				</label><label class="btn btn-info btn-sm">
				  <input class="type" type="checkbox" value="transaction"> Transaction告警
				</label>
			</div>
		</div>
		<br/><br/>
		<div id="alert-minutes">
		  <br/><br/>
		  <c:set var="count" value="${payload.count}" />
		  <c:set var="modalId" value="0" />
		  <c:choose>
		  	<c:when test="${fn:length(model.alertMinutes) == 0 }">
		  		<h3 class="text-center text-danger">该项目在该时间段内状态正常，没有告警信息。</h3>
		 	</c:when>
		 	<c:otherwise>
		 		<c:forEach var="minuteEntry" items="${model.alertMinutes}"  varStatus="itemStatus">
				      <table class="smallTable" style="float:left" border=1>  
				           <tr><th colspan="2" class="text-danger">${minuteEntry.key}</th></tr>
				           <tr><th>项目名</th><th>个</th></tr>
				           <c:set var="length" value="${fn:length(minuteEntry.value.alertDomains)}" />
				           <c:forEach var="alertDomain" items="${minuteEntry.value.alertDomains}" end="${count-1}">
				              <tr>
								 <td style="background-color:red;color:white;">
								 	<c:set var="id" value="modal${modalId}" />
								 	<c:set var="modalId" value="${modalId+1}" />
								 	<span data-id="${id}" class="alert-modal">
								 		${w:shorten(alertDomain.name, 30)}
								 	</span>
								 	<div class="modal fade" id="${id}" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
									  <div class="modal-dialog" style="width:1100px">
									    <div class="modal-content">
									      <div class="modal-body">
									      	<h4 class="text-danger text-center">项目：${alertDomain.name}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;告警时间：${minuteEntry.key}</h4>
									      	<c:forEach var="alertCategory" items="${alertDomain.alertCategories}">
									 			<h5 class="text-warning text-center">告警类型：${alertCategory.key}</h5>
									 			<table	class="table table-striped table-condensed table-hover">
													<tr class="text-success">
														<th width="8%">级别</th>
														<th width="72%">内容</th>
													</tr>
													<c:forEach var="alert" items="${alertCategory.value}">
														<tr>
															<td>${alert.type}</td>
															<td><span class="text-primary">${alert.metric}</span><br/>${alert.content}</td>
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
		                		 <td style="background-color:red;color:white;text-align:right">${w:format(alertDomain.count,'0')}</td>
				              </tr>
				           </c:forEach>
				           <c:if test="${length lt count}">
				           		<c:forEach begin="1" end="${count-length}">
				           			<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				           		</c:forEach>
						   </c:if>
				      </table>
			      </c:forEach>
			 	</c:otherwise>
		  </c:choose>
	    </div>
		<script type="text/javascript">
			$(document).ready(function(){
				initType("${payload.alertType}");
				$(".alert-modal").click(function(){
					var targetId = $(this).data("id");
					$("#"+targetId).modal();
				});
				checkIfAllChecked();
				$('#type-group').click(checkIfAllChecked);
				$("#select-all").click(function(){
					var originVal = $(this).prop("checked");
					
					$(".type").each(function(){
						$(this).prop("checked", originVal);
					});
				})
				
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
			function checkIfAllChecked(){
				var isAllChecked = true;
				
				$('.type').each(function(){
					if($(this).prop('checked')==false){
						isAllChecked = false;
					}
				});
				$('#select-all').prop("checked", isAllChecked);
			}
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
</a:application>