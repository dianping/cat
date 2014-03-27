<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<jsp:useBean id="ctx"	type="com.dianping.cat.report.page.alteration.Context" scope="request" />
<jsp:useBean id="payload"	type="com.dianping.cat.report.page.alteration.Payload" scope="request" />
<jsp:useBean id="model"	type="com.dianping.cat.report.page.alteration.Model" scope="request" />

<a:report title="Alteration Report" navUrlPrefix="">
	<jsp:body>
	<res:useJs value="${res.js.local['highcharts.js']}" target="head-js" />
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	<%@ include file="alter_query.jsp"%>
	<table	class="problem table table-striped table-bordered table-condensed table-hover">
		<tr class="text-success">
			<th width="15%">时间</th>
			<th width="5%">类型</th>
			<th width="60%">标题</th>
			<th width="8%">项目名</th>
			<th width="8%">机器名</th>
		</tr>
		<c:forEach var="item" items="${model.alterations}" varStatus="status">
			<tr class="aleration_${item.type}">
				<td>${w:format(item.date,'yyyy-MM-dd HH:mm:ss')}</td>
				<td>${item.type}</td>
				<td class="text-info">
				<c:choose>
					<c:when test="${empty item.url}">
						<span class="hreftip"  data-toggle="tooltip" data-placement="top" title="" data-original-title="${item.content}">${item.title}</span>
					</c:when>
					<c:otherwise>
						<a class="hreftip" target="_blank" href="${item.url}" data-toggle="tooltip" data-placement="top" title="" data-original-title="${item.content}">${item.title}</a>
					</c:otherwise>
					</c:choose>
				</td>
				<td>${item.domain}</td>
				<td>${item.hostname}</td>			
			</tr>
		</c:forEach>
	</table>
<script type="text/javascript">
	$(document).ready(function() {
		$(".header").hide();
		$('i[tips]').popover();
		$('.hreftip').tooltip({container:'body', html:true, delay:{show:0, hide:0}});
		
		<c:if test="${payload.fullScreen}">
			$('#fullScreen').addClass('btn-danger');
			$('.navbar').hide();
			$('.footer').hide();
		</c:if>
		<c:if test="${!payload.fullScreen}">
			$('#fullScreen').removeClass('btn-danger');
			$('.navbar').show();
			$('.footer').show();
		</c:if>
		
		<c:if test="${!payload.showPuppet}">
			$('#puppetButton').removeClass('btn-primary');
			$('.puppet').css("display","none");
		</c:if>
		
		<c:if test="${!payload.showWorkflow}">
			$('#workflowButton').removeClass('btn-primary');
			$('.workflow').css("display","none");
		</c:if>
	
		<c:if test="${!payload.showLazyman}">
			$('#lazymanButton').removeClass('btn-primary');
			$('.lazyman').css("display","none");
		</c:if>
		
		$(".typeButton").click(function(){
			var type = "."+this.id.replace("Button","");
			if($(type).css("display")=="table-cell"){
				$(this).removeClass("btn-primary");
				$(type).css("display","none");
			}else if($(type).css("display")=="none"){
				$(this).addClass("btn-primary");
				$(type).css("display","table-cell");
			}
		})
		
		var refresh = ${payload.refresh};
		var frequency = ${payload.frequency};
		if(refresh){
			$('#refresh${payload.frequency}').addClass('btn-danger');
			setInterval(function(){
				location.reload();				
			},frequency*1000);
		};
	});
</script>
</jsp:body>

</a:report>