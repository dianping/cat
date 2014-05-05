<%@ page contentType="text/html; charset=utf-8" %>
    <div class="row-fluid">
      <div class="span2">
        <div class="well sidebar-nav">
          <ul class="nav nav-list">
           <li class='nav-header' id="all">
            <li class='nav-header' id="metric_nettopology"><a href="?op=topo"><strong>核心拓扑</strong></a></li>
							
            <c:forEach var="item" items="${model.metricAggregationGroup}" varStatus="status">
				              <li class='nav-header' id="metric_${item.id}"><a href="?op=aggregation&group=${item.id}&timeRange=${payload.timeRange}&date=${model.date}&domain=${model.domain}"><strong>${item.id}</strong></a></li>
			</c:forEach>
			<c:forEach var="item" items="${model.productLines}" varStatus="status">
			              <li class='nav-header' id="metric_${item.id}"><a href="?op=view&date=${model.date}&domain=${model.domain}&product=${item.id}&timeRange=${payload.timeRange}"><strong>${item.id}</strong></a></li>
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
 		$('#all').addClass('active');
 	}else{
 		$('#metric_'+group).addClass('active');
 	}
 });
 </script>