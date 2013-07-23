<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<style>
	.tooltip-inner {
		max-width:36555px;
	 }
</style>
<script type="text/javascript">
	$('.hreftip').tooltip({container:'body', html:true, delay:{show:0, hide:0}});
</script>
<div class="tabbable tabs-left " id="topMetric"> <!-- Only required for left/right tabs -->
  <ul class="nav nav-tabs alert-info">
    <li class="text-right active"><a href="#tab1" data-toggle="tab">异常最多Top${payload.topCounts}</a></li>
    <li class='text-right'><a href="#tab2" data-toggle="tab">URL最慢Top${payload.topCounts}</a></li>
    <li class='text-right'><a href="#tab3" data-toggle="tab">Service最慢Top${payload.topCounts}</a></li>
    <li class='text-right'><a href="#tab4" data-toggle="tab">SQL最慢Top${payload.topCounts}</a></li>
    <li class='text-right'><a href="#tab5" data-toggle="tab">Call最慢Top${payload.topCounts}</a></li>
    <li class='text-right'><a href="#tab6" data-toggle="tab">Cache最慢Top${payload.topCounts}</a></li>
  </ul>
  <c:set var="date" value="${w:format(model.topReport.startTime,'yyyyMMddHH')}"/>
  <div class="tab-content">
    <div class="tab-pane  active" id="tab1">
      <c:forEach var="item" items="${model.topMetric.error.result}"  varStatus="itemStatus">
	      <table width="12%" style="float:left" border=1>  
	           <tr><th colspan="2" class="text-error" class="text-error">${item.key}</th></tr>
	           <tr><th width="80%">系统</th>      <th>个</th></tr>
	           <c:forEach var="detail" items="${item.value}" varStatus="status">
	              <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
	                 <c:choose>
						<c:when test="${detail.alert == 2}">
							 <td><a class="hreftip" style="color:red" href="/cat/r/p?domain=${detail.domain}&date=${date}" data-toggle="tooltip" data-placement="top" title="" data-original-title="${detail.errorInfo}">${detail.domain}</a></td>
	                		 <td style="text-align:right;color:red">${w:format(detail.value,'0')}</td>
						</c:when>
						<c:when test="${detail.alert == 1}">
							 <td><a class="hreftip" style="color:rgb(213, 96, 51)" href="/cat/r/p?domain=${detail.domain}&date=${date}" data-toggle="tooltip" data-placement="top" title="" data-original-title="${detail.errorInfo}">${detail.domain}</a></td>
	                		 <td style="text-align:right;color:red">${w:format(detail.value,'0')}</td>
						</c:when>
						<c:otherwise>
							 <td><a class="hreftip" href="/cat/r/p?domain=${detail.domain}&date=${date}" data-toggle="tooltip" data-placement="top" title="" data-original-title="${detail.errorInfo}">${detail.domain}</a></td>
	                		 <td style="text-align:right">${w:format(detail.value,'0')}</td>
						</c:otherwise>
					 </c:choose>
	              </tr>
	           </c:forEach>
	      </table>
      </c:forEach>
    </div>
    <div class="tab-pane" id="tab2">
      <c:forEach var="item" items="${model.topMetric.url.result}" varStatus="itemStatus">
      <table width="12%" style="float:left" border=1>  
            <tr><th colspan="2" class="text-error">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>ms</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}"> 
                  <td><a href="/cat/r/t?domain=${detail.domain}&date=${date}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
               </tr>
            </c:forEach>
      </table>
   </c:forEach>
    </div>
    <div class="tab-pane" id="tab3">
      <c:forEach var="item" items="${model.topMetric.service.result}" varStatus="itemStatus">
      <table width="12%" style="float:left" border=1>  
            <tr><th colspan="2" class="text-error">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>ms</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
                  <td><a href="/cat/r/t?domain=${detail.domain}&date=${date}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
               </tr>
            </c:forEach>
      </table>
   </c:forEach>
    </div>
    <div class="tab-pane" id="tab4">
      <c:forEach var="item" items="${model.topMetric.sql.result}" varStatus="itemStatus">
      <table width="12%" style="float:left" border=1>  
            <tr><th colspan="2" class="text-error">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>ms</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
                  <td><a href="/cat/r/t?domain=${detail.domain}&date=${date}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
               </tr>
            </c:forEach>
      </table>
   </c:forEach>
    </div>
    <div class="tab-pane" id="tab5">
      <c:forEach var="item" items="${model.topMetric.call.result}" varStatus="itemStatus">
      <table width="12%" style="float:left" border=1>  
            <tr><th colspan="2" class="text-error">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>ms</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
                  <td><a href="/cat/r/t?domain=${detail.domain}&date=${date}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
               </tr>
            </c:forEach>
      </table>
   </c:forEach>
    </div>
    <div class="tab-pane" id="tab6">
      <c:forEach var="item" items="${model.topMetric.cache.result}" varStatus="itemStatus">
      <table width="12%" style="float:left" border=1>  
            <tr><th colspan="2" class="text-error">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>ms</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
                  <td><a href="/cat/r/t?domain=${detail.domain}&date=${date}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
               </tr>
            </c:forEach>
      </table>
   </c:forEach>
    </div>
  </div></div>