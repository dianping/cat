<%@ page contentType="text/html; charset=utf-8" %>
    <div class="row-fluid">
      <div class="span2">
        <div class="well sidebar-nav">
          <ul class="nav nav-list">
           <li class='nav-header' id="all"><a href="?op=dashboard&date=${model.date}&domain=${model.domain}&timeRange=${payload.timeRange}"><strong>业务大盘</strong></a></li>
	       <c:forEach var="item" items="${model.tags}" varStatus="status">
            	 <li class='nav-header' id="metric_${item}"><a href="?op=dashboard&tag=${item}&timeRange=${payload.timeRange}&date=${model.date}&domain=${model.domain}"><strong>${item}</strong></a></li>
           </c:forEach>
           <c:forEach var="item" items="${model.productLines}" varStatus="status">
            		 <li class='nav-header' id="metric_${item.id}"><a href="?date=${model.date}&domain=${model.domain}&product=${item.id}&timeRange=${payload.timeRange}"><strong>${item.title}</strong></a></li>
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
 	var tag = '${payload.tag}';
 	
 	if(tag==''){
 		$('#all').addClass('active');
 	}else{
 		$('#metric_'+tag).addClass('active');
 	}
 });
 </script>