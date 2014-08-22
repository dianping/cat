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

<a:navbar title="AlertReport" navUrlPrefix="">
	<jsp:body>
		<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
		<res:useCss value="${res.css.local['alert.css']}" target="head-css" />
		<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
		<div id="queryBar">
			<div class="text-left"></div>
			开始
			<div id="startDatePicker" class="input-append date" >
				<input name="startTime" id="startTime" style="height:auto; width: 150px;" readonly
				value="<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm"/>" type="text"></input> 
				<span class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"></i> </span>
			</div>
			结束
			<div id="endDatePicker" class="input-append date" >
				<input name="endTime" id="endTime" style="height:auto; width: 150px;" readonly
				value="<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm"/>" type="text"></input> 
				<span class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"></i> </span>
			</div>
			告警级别
			<input type="text" name="level" id="level" value="${payload.level}" style="height:auto" class="input-small">
			项目
			<input type="text" name="domain" id="domain" value="${payload.domain}" style="height:auto" class="input-small">
			指标
			<input type="text" name="metric" id="metric" value="${payload.metric}" style="height:auto" class="input-small"> 
			<input class="btn btn-primary  btn-small"  value="查询" onclick="queryNew()" type="submit">
			<input type='hidden' id='fullScreenStr' value='${payload.fullScreen}'/>
			<a id="fullScreen" class='btn btn-small btn-primary'>全屏</a>
			<a id="refresh10" class='btn btn-small btn-primary' onclick="queryFrequency(10)">10秒</a>
			<a id="refresh20" class='btn btn-small btn-primary' onclick="queryFrequency(20)">20秒</a>
			<a id="refresh30" class='btn btn-small btn-primary' onclick="queryFrequency(30)">30秒</a>
			<br>
			告警类型（可多选）&nbsp;&nbsp;
			<div class="btn-group" data-toggle="buttons-checkbox">
			  <button id="networkButton" type="button" class="btn btn-info">网络告警</button>
			  <button id="businessButton" type="button" class="btn btn-info">业务告警</button>
			  <button id="systemButton" type="button" class="btn btn-info">系统告警</button>
			  <button id="exceptionButton" type="button" class="btn btn-info">异常告警</button>
			  <button id="frontEndExceptionButton" type="button" class="btn btn-info">前端告警</button>
			  <button id="thirdPartyButton" type="button" class="btn btn-info">第三方告警</button>
			  <input type='hidden' id='networkStatus' value='${payload.showNetwork}'/>
			  <input type='hidden' id='businessStatus' value='${payload.showBusiness}'/>
			  <input type='hidden' id='systemStatus' value='${payload.showSystem}'/>
			  <input type='hidden' id='exceptionStatus' value='${payload.showException}'/>
			  <input type='hidden' id='frontEndExceptionStatus' value='${payload.showFrontEndException}'/>
			  <input type='hidden' id='thirdPartyStatus' value='${payload.showThirdParty}'/>
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
				
				<c:if test="${payload.showNetwork == false}">
					toggleButton("network", true);
				</c:if>
				<c:if test="${payload.showException == false}">
					toggleButton("exception", true);
				</c:if>
				<c:if test="${payload.showSystem == false}">
					toggleButton("system", true);
				</c:if>
				<c:if test="${payload.showBusiness == false}">
					toggleButton("business", true);
				</c:if>
				<c:if test="${payload.showThirdParty == false}">
					toggleButton("thirdParty", true);
				</c:if>
				<c:if test="${payload.showFrontEndException == false}">
					toggleButton("frontEndException", true);
				</c:if>
				
				$('#startDatePicker').datetimepicker({format: 'yyyy-MM-dd hh:mm'});
				$('#endDatePicker').datetimepicker({format: 'yyyy-MM-dd hh:mm'});
				
				$("#fullScreen").click(clickFullScreen);
				
				$("#networkButton").click(function(){
					toggleButton("network", false);
				});
				$("#businessButton").click(function(){
					toggleButton("business", false);
				});
				$("#systemButton").click(function(){
					toggleButton("system", false);
				});
				$("#exceptionButton").click(function(){
					toggleButton("exception", false);
				});
				$("#frontEndExceptionButton").click(function(){
					toggleButton("frontEndException", false);
				});
				$("#thirdPartyButton").click(function(){
					toggleButton("thirdParty", false);
				});
				
				var refresh = ${payload.refresh};
				var frequency = ${payload.frequency};
				if(refresh){
					$('#refresh'+frequency).addClass('btn-danger');
					setTimeout(refreshPage,frequency*1000);
				};
			});
			
			function clickFullScreen(){
				var isFullScreen = $('#fullScreenStr').val() === 'true';
				if(isFullScreen){
					$('#fullScreen').removeClass('btn-danger');
					$('.navbar').show();
					$('.footer').show();
				}else{
					$('#fullScreen').addClass('btn-danger');
					$('.navbar').hide();
					$('.footer').hide();
				}
				$('#fullScreenStr').val(!isFullScreen);
			}
			function toggleButton(button, isInitialized){
				var targetStatus = $("#"+button+"Status").val() === 'false';
				if(isInitialized){
					$("#"+button+"Button").button('toggle');
					targetStatus = !targetStatus;
				}
				
				if(targetStatus){
					$("."+button).each(function(){
						var counter = $(this).prevAll().filter(".noter").first().children().first();
						
						$(this).css("display","table-row");
						var count = Number(counter.attr('rowspan'))+1;
						counter.attr('rowspan', count);
					});
				}else{
					$("."+button).each(function(){
						var counter = $(this).prevAll().filter(".noter").first().children().first();
						
						$(this).css("display","none");
						var count = Number(counter.attr('rowspan'))-1;
						counter.attr('rowspan', count);
					});
				}
				$("#"+button+"Status").val(String(targetStatus));
			}
			function getType(){
				var networkStr=$('#networkStatus').val();
				var businessStr=$('#businessStatus').val();
				var systemStr=$('#systemStatus').val();
				var exceptionStr=$('#exceptionStatus').val();
				var frontEndExceptionStr=$('#frontEndExceptionStatus').val();
				var thirdPartyStr=$('#thirdPartyStatus').val();
				return "showNetwork="+networkStr+"&showBusiness="+businessStr+"&showSystem="+systemStr+"&showException="+exceptionStr+
				"&showFrontEndException="+frontEndExceptionStr+"&showThirdParty="+thirdPartyStr;
			}
			function queryNew(){
				var startTime=$("#startTime").val();
				var endTime=$("#endTime").val();
				var domain=$("#domain").val();
				var level=$("#level").val();
				var metric=$("#metric").val();
				var isFullScreen=$('#fullScreenStr').val();
				window.location.href="?op=view&domain="+domain+"&level="+level+"&metric="+metric+"&startTime="+startTime+"&endTime="+endTime+"&fullScreen="+isFullScreen+"&"+getType();
			}
			function queryFrequency(frequency){
				var domain=$("#domain").val();
				var level=$("#level").val();
				var metric=$("#metric").val();
				var isFullScreen=$('#fullScreenStr').val();
				window.location.href="?op=view&domain="+domain+"&level="+level+"&metric="+metric+"&fullScreen="+isFullScreen+"&refresh=true&frequency="+frequency+"&"+getType();
			}
			function refreshPage(){
				var domain=$("#domain").val();
				var level=$("#level").val();
				var metric=$("#metric").val();
				var isFullScreen=$('#fullScreenStr').val();
				window.location.href="?op=view&domain="+domain+"&level="+level+"&metric="+metric+"&fullScreen="+isFullScreen+"&refresh=true&frequency="+${payload.frequency}+"&"+getType();
			}
		</script>
	</jsp:body>
</a:navbar>