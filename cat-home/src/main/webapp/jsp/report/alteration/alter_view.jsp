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

<a:application>
	<jsp:body>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<%@ include file="alter_query.jsp"%>
	<div id="alt-minutes">
		  <c:set var="count" value="${payload.count}" />
		  <c:set var="modalId" value="0" />
		  <c:choose>
		  	<c:when test="${fn:length(model.alterationMinuites) == 0 }">
		  		<h3 class="text-center text-danger">该项目在该时间段内没有变更信息。</h3>
		 	</c:when>
		 	<c:otherwise>
			      <c:forEach var="minuteEntry" items="${model.alterationMinuites}"  varStatus="itemStatus">
				      <table class="smallTable" style="float:left" border=1>  
				           <tr><th colspan="2" class="text-danger">${minuteEntry.key}</th></tr>
				           <tr><th>项目名</th><th>个</th></tr>
				           <c:set var="length" value="${fn:length(minuteEntry.value.alterationDomains)}" />
				           <c:forEach var="alterDomain" items="${minuteEntry.value.alterationDomains}" end="${count-1}">
					              <tr>
									 <td>
									 	<c:set var="id" value="modal${modalId}" />
									 	<c:set var="modalId" value="${modalId+1}" />
									 	<span class="alter-modal" data-toggle="modal" data-target="#${id}">
									 		${w:shorten(alterDomain.name, 18)}
									 	</span>
									 	<div class="modal fade" id="${id}" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
										  <div class="modal-dialog" style="width:1100px">
										    <div class="modal-content">
										      <div class="modal-body">
										      	<h4 class="text-danger text-center">项目名：${alterDomain.name}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;变更时间：${minuteEntry.key}</h4>
										      	<c:forEach var="alterType" items="${alterDomain.alterationTypes}">
										 			<h5 class="text-warning text-center">变更类型：${alterType.key}</h5>
										 			<table	class="table table-striped table-condensed table-hover">
														<tr class="text-success">
															<th width="25%">机器名</th>
															<th width="75%">内容</th>
														</tr>
														<c:forEach var="item" items="${alterType.value}">
															<tr>
																<td>${item.hostname}</td>	
																<td>
																	<c:choose>
																		<c:when test="${empty item.url}">
																			<span class="text-primary">${item.title}</span>
																		</c:when>
																		<c:otherwise>
																			<a class="hreftip out_url" target="_blank" href="${item.url}">${item.title}</a>
																		</c:otherwise>
																	</c:choose>
																	<br/>
																	${item.content}
																</td>
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
			                		 <td style="text-align:right">${w:format(alterDomain.count,'0')}</td>
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
	$(document).ready(function() {
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
		$('#system_alteration').addClass('active');
	
		$(".out_url").each(function(){
			var cur = $(this);
			cur.attr("href", decodeURIComponent(cur.attr("href")));
		});
	});
</script>
</jsp:body>

</a:application>