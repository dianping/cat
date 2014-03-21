<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.alteration.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.alteration.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.alteration.Model" scope="request"/>
<c:set var="barrels" value="${model.barrels}" />

<a:report title="Alteration Report"
	navUrlPrefix="">
	<jsp:body>
	<res:useJs value="${res.js.local['highcharts.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
<table class="machines">		
	<tr>
		<th>
		<%@ include file="alter_query.jsp" %>
		</th>
	</tr>
</table>

<table class="problem table table-striped table-bordered table-condensed table-hover"  style="width:100%">
	<tr class="text-success">
		<th width="15%">时间</th>
		<th width="85%">详细信息</th>
	</tr>
	<c:forEach var="barrel" items="${model.barrels}" varStatus="typeIndex">
		<tr style="width:85%">
			<td>
				${barrel.startTime}</br>${barrel.endTime}
			</td>
			<td>
				<table class="table table-striped table-bordered table-condensed table-hover">
					<tr class="text-success">
						<th width="15%">标题</th>
						<th width="5%">类型</th>
						<th width="5%">应用</th>
						<th width="8%">主机名</th>
						<th width="15%">变更时间</th>
						<th width="5%">变更用户</th>
						<th width="5%">详情</th>
					</tr>
					<c:forEach var="item" items="${barrel.alterations}" varStatus="index">
						<tr>
						<td class="text-info">
							<i tips="" data-trigger="hover" class="icon-question-sign" data-toggle="popover" data-placement="top" data-content="${item.content}"></i>
							${item.title}
						</td>
						<td class="alertation${item.type}">
							${item.type}
						</td>
						<td >
							${item.domain}
						</td>
						<td >
							${item.hostname}
						</td>
						<td >
							${item.date}
						</td>
						<td >
							${item.user}
						</td>
						<td >
							<c:if test=" ${empty item.url}">
								<a href="${item.url}">link</a>
							</c:if>
						</td>						
					</tr>
			</c:forEach>
				</table>
			</td>
		</tr>
	</c:forEach>
</table>

<script type="text/javascript">
	$(document).ready(function() {
		$('i[tips]').popover();
		$(".header").hide();
		
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
		
		/*
		$('#fullScreen').click(function(){
			if($('#fullScreen').hasClass('btn-danger')){
				$('#fullScreen').removeClass('btn-danger');
				$('#fullScreen').val('full');
				$('.navbar').show();
				$('.footer').show();
			}else{
				$('#fullScreen').addClass('btn-danger');
				$('#fullScreen').val('exit');
				$('.navbar').hide();
				$('.footer').hide();
			}
		});*/
		
		var refresh = ${payload.refresh};
		var frequency = ${payload.frequency};
		if(refresh){
			$('#refresh${payload.frequency}').addClass('btn-danger');
			setInterval(function(){
				location.reload();				
			},frequency*1000);
		};
		
		var value = ${payload.granularity};
		$("#granularity").val(value);
	});
</script>

<res:useJs value="${res.js.local.problem_js}" target="buttom-js" />
<res:useJs value="${res.js.local.problemHistory_js}" target="bottom-js" />
</jsp:body>

</a:report>