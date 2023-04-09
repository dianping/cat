<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<style>
	.tooltip-inner {
		max-width:36555px;
	 }
	 .smallTable{
	 	font-size:small;
	 }
</style>
<script type="text/javascript">
	$('.hreftip').tooltip({container:'body', html:true, delay:{show:0, hide:0}});
</script>

<c:if test="${not empty model.message}">
	<h3 class="text-center text-danger">CAT服务端异常:${model.message}</h3>
</c:if>
<c:if test="${ empty model.message}">
	<h3 class="text-center text-success">CAT服务端正常</h3>
</c:if>

 <c:set var="date" value="${w:format(model.topReport.startTime,'yyyyMMddHH')}"/>
 <c:forEach var="item" items="${model.topMetric.error.result}"  varStatus="itemStatus">
      <table class="smallTable" style="margin:15px 3px;float:left" border="1">
           <tr style="height: 25px;"><th colspan="2" class="text-danger" style="padding-left: 5px;">${item.key}</th></tr>
           <tr style="height: 25px;"><th style="padding-left: 5px">系统</th><th style="text-align: center">个</th></tr>
           <c:forEach var="detail" items="${item.value}" varStatus="status">
              <tr class="">
                 <c:choose>
					 <c:when test="${detail.alert == 2}">
						 <td style="background-color:#f8d7da;padding-left: 5px"><a class="hreftip" style="color:#b02a37;" href="/cat/r/p?domain=${detail.domain}&date=${date}"  title="${detail.errorInfo}">${w:shorten(detail.domain, 18)}</a></td>
						 <td style="background-color:#f8d7da;color:#b02a37;padding-left: 5px">${w:format(detail.value,'0')}</td>
					 </c:when>
					 <c:when test="${detail.alert == 1}">
						 <td style="background-color:#fff3cd;padding-left: 5px"><a class="hreftip" style="color:#997404;" href="/cat/r/p?domain=${detail.domain}&date=${date}" title="${detail.errorInfo}">${w:shorten(detail.domain, 18)}</a></td>
						 <td style="background-color:#fff3cd;color:#997404;padding-left: 5px">${w:format(detail.value,'0')}</td>
					 </c:when>
					<c:otherwise>
						 <td style="background-color:#e9ecef;padding-left: 5px"><a class="hreftip" style="color:#6c757d;" href="/cat/r/p?domain=${detail.domain}&date=${date}" title="${detail.errorInfo}">${w:shorten(detail.domain, 18)}</a></td>
                		 <td style="background-color:#e9ecef;color:#6c757d;padding-left: 5px">${w:format(detail.value,'0')}</td>
					</c:otherwise>
				 </c:choose>
              </tr>
           </c:forEach>
      </table>
     </c:forEach>
