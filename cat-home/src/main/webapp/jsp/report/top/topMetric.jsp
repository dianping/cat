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
	<h3 class="text-center text-danger">出问题CAT的服务端:${model.message}</h3>
</c:if>
<c:if test="${ empty model.message}">
	<h3 class="text-center text-success">CAT服务端正常</h3>
</c:if>
  
 <c:set var="date" value="${w:format(model.topReport.startTime,'yyyyMMddHH')}"/>
 <c:forEach var="item" items="${model.topMetric.error.result}"  varStatus="itemStatus">
      <table  class="smallTable" style="float:left" border=1>  
           <tr><th colspan="2" class="text-danger" class="text-danger">${item.key}</th></tr>
           <tr><th>系统</th>      <th>个</th></tr>
           <c:forEach var="detail" items="${item.value}" varStatus="status">
              <tr class="">
                 <c:choose>
					<c:when test="${detail.alert == 2}">
						 <td style="background-color:red;color:white;"><a class="hreftip"  style="color:white;" href="/cat/r/p?domain=${detail.domain}&date=${date}"  title="${detail.errorInfo}">${w:shorten(detail.domain, 18)}</a></td>
                		 <td style="background-color:red;color:white;text-align:right">${w:format(detail.value,'0')}</td>
					</c:when>
					<c:when test="${detail.alert == 1}">
						 <td style="background-color:#bfa22f;color:white;"><a class="hreftip" style="color:white;" href="/cat/r/p?domain=${detail.domain}&date=${date}" title="${detail.errorInfo}">${w:shorten(detail.domain, 18)}</a></td>
                		 <td style="background-color:#bfa22f;color:white;text-align:right">${w:format(detail.value,'0')}</td>
					</c:when>
					<c:otherwise>
						 <td><a class="hreftip" href="/cat/r/p?domain=${detail.domain}&date=${date}" title="${detail.errorInfo}">${w:shorten(detail.domain, 18)}</a></td>
                		 <td style="text-align:right">${w:format(detail.value,'0')}</td>
					</c:otherwise>
				 </c:choose>
              </tr>
           </c:forEach>
      </table>
     </c:forEach>
