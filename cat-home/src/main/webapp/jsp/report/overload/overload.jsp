<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.overload.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.overload.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.overload.Model" scope="request" />

<a:application>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useCss value='${res.css.local.table_css}' target="head-css" />
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['tableInit.js']}" target="head-js"/>
   <div class="row-fluid">
		<div id="queryBar"">
	    <div style="float:left;">
			&nbsp;开始
			<input type="text" id="startTime" style="width:150px;" value="<fmt:formatDate value='${payload.startTime}' pattern='yyyy-MM-dd HH:mm'/>"/>
			结束
			<input type="text" id="endTime" style="width:150px;" value="<fmt:formatDate value='${payload.endTime}' pattern='yyyy-MM-dd HH:mm'/>"/></div>
			&nbsp;&nbsp;<input class="btn btn-primary  btn-sm"  value="查询" onclick="queryNew()" type="submit">
		</div>
		<div id="DatabaseReport" style="display:inline-flex;padding-top:3px;">
			<table class="table table-striped table-condensed  table-hover" style="width:100%" id="contents">
				<thead>
				<tr class="text-success">
					<th width="20%">日期</th>
					<th width="10%">报表类型</th>
					<th width="15%">报表名称</th>
					<th width="20%">项目</th>
					<th width="15%">ip</th>
					<th width="10%">报表格式</th>
					<th width="10%">报表长度</th>
				</tr>
				</thead>
				<tbody>
				<c:forEach var="report" items="${model.reports}" varStatus="status">
					<tr class="reportType${report.reportType}">
						<td>${report.period}</td>
						<c:choose>
						    <c:when test="${report.reportType eq 1}">
						       <td>小时报表</td>
						    </c:when>
						    <c:when test="${report.reportType eq 2}">
						       <td>天报表</td>
						    </c:when>
						    <c:when test="${report.reportType eq 3}">
						       <td>周报表</td>
						    </c:when>
						    <c:when test="${report.reportType eq 4}">
						       <td>月报表</td>
						    </c:when>
						    <c:otherwise>
						        <td></td>
						    </c:otherwise>
						</c:choose>
						<td>${report.name}</td>
						<td>${report.domain}</td>
						<td>${report.ip}</td>
						<c:choose>
						    <c:when test="${report.type eq 1}">
						       <td>binary</td>
						    </c:when>
						    <c:when test="${report.type eq 2}">
						       <td>xml</td>
						    </c:when>
						    <c:otherwise>
						        <td></td>
						    </c:otherwise>
						</c:choose>
						<td><fmt:formatNumber type="number" maxFractionDigits="1" minFractionDigits="1" value="${report.reportLength}" /></td>
					</tr>
				</c:forEach>
				</tbody>
			</table>
		</div></div>
		<script type="text/javascript">
		  $(document).ready(function(){
			  init();
			  $('#Offline_report').addClass('active open');
			  $('#overload_report').addClass("active");
			  
	        <c:if test="${payload.fullScreen}">
	          $('#fullScreen').addClass('btn-danger');
	          $('.navbar').hide();
	          $('.footer').hide();
	        </c:if>
	        
	        <c:if test="${payload.showHourly == false}">
	          toggleButton("hourly", true);
	        </c:if>
	        <c:if test="${payload.showDaily == false}">
	          toggleButton("daily", true);
	        </c:if>
	        <c:if test="${payload.showWeekly == false}">
	          toggleButton("weekly", true);
	        </c:if>
	        <c:if test="${payload.showMonthly == false}">
	          toggleButton("monthly", true);
	        </c:if>
	        
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
	        
	        $("#fullScreen").click(clickFullScreen);
	        
	        $("#hourlyButton").click(function(){
	          toggleButton("hourly", false);
	        });
	        $("#dailyButton").click(function(){
	          toggleButton("daily", false);
	        });
	        $("#weeklyButton").click(function(){
	          toggleButton("weekly", false);
	        });
	        $("#monthlyButton").click(function(){
	          toggleButton("monthly", false);
	        });
	      });
	      
	      var buttonToInt = {'hourly':1, 'daily':2, 'weekly':3, 'monthly':4};
	      
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
	          $(".reportType"+buttonToInt[button]).css("display","table-row");
	        }else{
	          $(".reportType"+buttonToInt[button]).css("display","none");
	        }
	        $("#"+button+"Status").val(String(targetStatus));
	      }
	      function getType(){
	        var hourlyStr=$('#hourlyStatus').val();
	        var dailyStr=$('#dailyStatus').val();
	        var weeklyStr=$('#weeklyStatus').val();
	        var monthlyStr=$('#monthlyStatus').val();
	        return "showHourly="+hourlyStr+"&showDaily="+dailyStr+"&showWeekly="+weeklyStr+"&showMonthly="+monthlyStr;
	      }
	      function queryNew(){
	        var startTime=$("#startTime").val();
	        var endTime=$("#endTime").val();
	        window.location.href="?op=view&startTime="+startTime+"&endTime="+endTime;
	      }
		</script>
</a:application>