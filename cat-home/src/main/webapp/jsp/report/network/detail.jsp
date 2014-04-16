<%@ page contentType="text/html; charset=utf-8" %>
    <div class="row-fluid">
      <div class="span2">
        <div class="well sidebar-nav">
          <ul class="nav nav-list">
           <li class='nav-header' id="all">
           <c:set var="index" value="0" />
           <c:forEach var="item" items="${model.productLines}" varStatus="status">
           			 <c:set var="index" value="${index+1}" />
            		 <li class='nav-header' id="metric_${item.id}" name="metric_${index}"><a href="?date=${model.date}&domain=${model.domain}&product=${item.id}&timeRange=${payload.timeRange}"><strong>${item.title}</strong></a></li>
           </c:forEach>
            <li >&nbsp;</li>
          </ul>
        </div><!--/.well -->
      </div><!--/span-->
      <div class="span10">
      	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
   			<div style="float:left;">
   				<div id="${item.id}" class="metricGraph"></div>
   			</div>
		</c:forEach>
 </div></div>
 
<script type="text/javascript">

$(document).ready(function() {
 	var group = '${payload.group}';
 	
 	if(group==''){
 		//$('#metric_1').addClass('active');
 		$("li[name=metric_1]").addClass('active');
 	}else{
 		$('#metric_'+group).addClass('active');
 	}
 });
 </script>