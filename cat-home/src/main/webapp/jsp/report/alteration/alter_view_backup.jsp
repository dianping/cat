<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%> 
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.alteration.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.alteration.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.alteration.Model" scope="request"/>

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

<table class="problem table table-striped table-bordered table-condensed table-hover"  style="width:100%;word-wrap: break-word; word-break: break-all;">
	<tr class="text-success">
		<th width="15%">时间</th>
		<th width="28%" class="puppet">puppet</th>
		<th width="28%" class="workflow">workflow</th>
		<th width="28%" class="lazyman">lazyman</th>
	</tr>
	<c:forEach var="barrelMap" items="${model.barrels}" varStatus="typeIndex">	
	<c:set var="barrel" value="${barrelMap.value}" />
		<tr style="width:85%">
			<td width="10%">
				${barrel.startTime}</br>${barrel.endTime}
			</td>
			<td width="30%" class="puppet">
				<table class="table table-striped table-bordered table-condensed table-hover"  border="0" cellpadding="0" cellspacing="0">
					<tr class="text-success">
						<th width="20%">标题</th>
						<th width="5%">类型</th>
						<th width="5%">应用</th>
						<th width="8%">主机名</th>
						<th width="10%">变更时间</th>
						<th width="5%">变更用户</th>
						<th width="5%">详情</th>
					</tr>
					<c:forEach var="item" items="${barrel.alterationMap['puppet']}" varStatus="index" begin="0" end="9">
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
					<c:if test="${fn:length(barrel.alterationMap['puppet'])>10}">
						<tr class='showMenu' id='${barrel.key}_show' style="display:table-row;">
							<td><a href='' onclick="return false">[:: show all ::]</a></td>
						</tr>
						<c:forEach var="item" items="${barrel.alterationMap['puppet']}" varStatus="index" begin="10">
						<tr style="display:none;" class='${barrel.key}_content'>
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
						<tr class='hideMenu' id='${barrel.key}_hide' style="display:none;">
							<td><a href='' onclick="return false">[:: hide ::]</a></td>
						</tr>
					</c:if>
				</table>
			</td>
			<td width="30%" class="workflow">
				<table class="table table-striped table-bordered table-condensed table-hover"  border="0" cellpadding="0" cellspacing="0">
					<tr class="text-success">
						<th width="20%">标题</th>
						<th width="5%">类型</th>
						<th width="5%">应用</th>
						<th width="8%">主机名</th>
						<th width="10%">变更时间</th>
						<th width="5%">变更用户</th>
						<th width="5%">详情</th>
					</tr>
					<c:forEach var="item" items="${barrel.alterationMap['workflow']}" varStatus="index" begin="0" end="9">
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
					<c:if test="${fn:length(barrel.alterationMap['workflow'])>10}">
						<tr class='showMenu' id='${barrel.key}_show' style="display:table-row;">
							<td><a href='' onclick="return false">[:: show all ::]</a></td>
						</tr>
						<c:forEach var="item" items="${barrel.alterationMap['workflow']}" varStatus="index" begin="10">
						<tr style="display:none;" class='${barrel.key}_content'>
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
						<tr class='hideMenu' id='${barrel.key}_hide' style="display:none;">
							<td><a href='' onclick="return false">[:: hide ::]</a></td>
						</tr>
					</c:if>
				</table>
			</td>
			<td width="30%" class="lazyman">
				<table class="table table-striped table-bordered table-condensed table-hover"  border="0" cellpadding="0" cellspacing="0">
					<tr class="text-success">
						<th width="20%">标题</th>
						<th width="5%">类型</th>
						<th width="5%">应用</th>
						<th width="8%">主机名</th>
						<th width="10%">变更时间</th>
						<th width="5%">变更用户</th>
						<th width="5%">详情</th>
					</tr>
					<c:forEach var="item" items="${barrel.alterationMap['lazyman']}" varStatus="index" begin="0" end="9">
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
					<c:if test="${fn:length(barrel.alterationMap['lazyman'])>10}">
						<tr class='showMenu' id='${barrel.key}_show' style="display:table-row;">
							<td><a href='' onclick="return false">[:: show all ::]</a></td>
						</tr>
						<c:forEach var="item" items="${barrel.alterationMap['lazyman']}" varStatus="index" begin="10">
						<tr style="display:none;" class='${barrel.key}_content'>
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
						<tr class='hideMenu' id='${barrel.key}_hide' style="display:none;">
							<td><a href='' onclick="return false">[:: hide ::]</a></td>
						</tr>
					</c:if>
				</table>
			</td>
		</tr>
	</c:forEach>
</table>

<c:if test="${model.totalPages>1}">
	<div class="pagination pagination-centered">
	  <ul>
	  <c:forEach varStatus="idx" begin="1" end="${model.totalPages}">
	    <li id='page${idx.index}'><a href="?fullScreen=${payload.fullScreen}&refresh=true&frequency=30&startTime=<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&endTime=<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm:ss"/>&granularity=${payload.granularity}&pages=${idx.index}">${idx.index}</a></li>
	  </c:forEach>
	  </ul>
	</div>
</c:if>

<script type="text/javascript">
	$(document).ready(function() {
		$(".header").hide();
		$('i[tips]').popover();
		
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
		
		<c:if test="${model.totalPages>1}">
			$('#page'+${payload.pages}).addClass('active')
		</c:if>
		
		$(".showMenu").click(function(){
			var timeStamp = this.id.replace("_show","");
			var content = "."+timeStamp+"_content";
			var hide = "#"+timeStamp+"_hide";
			
			$(this).css("display","none");
			$(content).css("display","table-row");
			$(hide).css("display","table-row");
			
		})
		
		$(".hideMenu").click(function(){
			var timeStamp = this.id.replace("_hide","");
			var content = "."+timeStamp+"_content";
			var show = "#"+timeStamp+"_show";
			
			$(this).css("display","none");
			$(content).css("display","none");
			$(show).css("display","table-row");
			
		})
		
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