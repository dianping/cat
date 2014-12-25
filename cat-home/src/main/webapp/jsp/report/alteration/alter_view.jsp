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

<a:body>
	<jsp:body>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<%@ include file="alter_query.jsp"%>
	<table	class="problem table table-striped table-condensed   table-hover">
		<tr class="text-success">
			<th width="15%">时间</th>
			<th width="5%">类型</th>
			<th width="56%">标题</th>
			<th width="12%">项目名</th>
			<th width="12%">机器名</th>
		</tr>
		<c:forEach var="item" items="${model.alterations}" varStatus="status">
			<tr class="content_${item.type}" style="display:table-row">
				<td>${w:format(item.date,'yyyy-MM-dd HH:mm:ss')}</td>
				<td class="aleration_${item.type}">${item.type}</td>
				<td class="text-info">
				<c:choose>
					<c:when test="${empty item.url}">
						<span class="hreftip"  data-toggle="tooltip" data-placement="top" title="" data-original-title="${item.content}">${item.title}</span>
					</c:when>
					<c:otherwise>
						<a class="hreftip out_url" target="_blank" href="${item.url}" data-toggle="tooltip" data-placement="top" title="" data-original-title="${item.content}">${item.title}</a>
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
		$('#startTime').datetimepicker({
			format:'Y-m-d H:i',
			step:10,
			maxDate:0
		});
		$('#endTime').datetimepicker({
			format:'Y-m-d H:i',
			step:10,
			maxDate:0
		});
		$('#startTime').val("${w:format(payload.startTime,'yyyy-MM-dd HH:mm')}");
		$('#endTime').val("${w:format(payload.endTime,'yyyy-MM-dd HH:mm')}");
		$('i[tips]').popover();
		$('.hreftip').popover({trigger:"hover", html:true, delay:{show:0, hide:0}});
		
		$('#System_report').addClass('active open');
		$('#system_alteration').addClass('active');
	
		$(".out_url").each(function(){
			var cur = $(this);
			cur.attr("href", decodeURIComponent(cur.attr("href")));
		});
		
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
			$("#show_puppet").attr("checked",false);
			$(".content_puppet").css("display","none");
		</c:if>
		
		<c:if test="${!payload.showWorkflow}">
			$("#show_workflow").attr("checked",false);
			$(".content_workflow").css("display","none");
		</c:if>
	
		<c:if test="${!payload.showLazyman}">
			$("#show_lazyman").attr("checked",false);
			$(".content_lazyman").css("display","none");
		</c:if>
		
		$(".typeCheckbox").click(function(){
			var typeContent = ".content_"+this.id.replace("show_","");
			var isChecked = document.getElementById(this.id).checked;
			
			if(!isChecked){
				$(this).attr("checked",false);
				$(typeContent).css("display","none");
			}else{
				$(this).attr("checked",true);
				$(typeContent).css("display","table-row");
			}
		})
		
	});
</script>
</jsp:body>

</a:body>