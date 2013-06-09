<%@ page contentType="text/html; charset=utf-8" %>

<div class="tabbable  " id="topMetric"> <!-- Only required for left/right tabs -->
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
    <div class="tab-pane active" id="tab1">
      <table><tr>
      <c:forEach var="item" items="${model.topMetric.error.result}" varStatus="status">
      <td><table style="float:left" border=1>  
            <tr><th colspan="3">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>个</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
                  <td>
                  <a class="hreftip" href="/cat/r/p?domain=${detail.domain}&date=${date}" data-toggle="tooltip" data-placement="top" title="" data-original-title="${detail.errorInfo}">${detail.domain}</a>
                  <td style="text-align:right">${w:format(detail.value,'0')}</td>
               </tr>
            </c:forEach>
      	</table></td>
      </c:forEach></tr></table>
    </div>
    <div class="tab-pane" id="tab2">
      <table><tr>
      <c:forEach var="item" items="${model.topMetric.url.result}" varStatus="status">
      <td><table style="float:left" border=1>  
            <tr><th colspan="2">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>ms</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
                  <td><a href="/cat/r/t?domain=${detail.domain}&date=${date}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
               </tr>
            </c:forEach>
      </table></td>
   </c:forEach></tr></table>
    </div>
    <div class="tab-pane" id="tab3">
      <table><tr>
      <c:forEach var="item" items="${model.topMetric.service.result}" varStatus="status">
      <td><table style="float:left" border=1>  
            <tr><th colspan="2">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>ms</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
                  <td><a href="/cat/r/t?domain=${detail.domain}&date=${date}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
               </tr>
            </c:forEach>
      </table></td>
   </c:forEach></tr></table>
    </div>
    <div class="tab-pane" id="tab4">
      <table><tr>
      <c:forEach var="item" items="${model.topMetric.sql.result}" varStatus="status">
      <td><table style="float:left" border=1>  
            <tr><th colspan="2">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>ms</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
                  <td><a href="/cat/r/t?domain=${detail.domain}&date=${date}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
               </tr>
            </c:forEach>
      </table></td>
   </c:forEach></tr></table>
    </div>
    <div class="tab-pane" id="tab5">
     <table><tr>
       <c:forEach var="item" items="${model.topMetric.call.result}" varStatus="status">
      <td><table style="float:left" border=1>  
            <tr><th colspan="2">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>ms</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
                  <td><a href="/cat/r/t?domain=${detail.domain}&date=${date}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
               </tr>
            </c:forEach>
      </table></td>
   </c:forEach></tr></table>
    </div>
    <div class="tab-pane" id="tab6">
     <table><tr>
      <c:forEach var="item" items="${model.topMetric.cache.result}" varStatus="status">
      <td><table style="float:left" border=1>  
            <tr><th colspan="2">${item.key}</th></tr>
            <tr><th width="80%">系统</th>      <th>ms</th></tr>
            <c:forEach var="detail" items="${item.value}" varStatus="status">
               <tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
                  <td><a href="/cat/r/t?domain=${detail.domain}&date=${date}" target="_blank">${detail.domain}</a></td><td>${w:format(detail.value,'0.0')}</td>
               </tr>
            </c:forEach>
      </table></td>
   </c:forEach></tr></table>
    </div>
  </div></div>