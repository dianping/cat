<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="metricOpNav.jsp" %>
    <div class="row-fluid">
      <div class="span2">
        <div class="well sidebar-nav">
          <ul class="nav nav-list">
           <li class='nav-header' id="all"><a href="?op=dashboard&date=${model.date}&domain=${model.domain}&timeRange=${payload.timeRange}"><strong>业务大盘</strong></a></li>
           
           <c:forEach var="item" items="${model.metricGroups}" varStatus="status">
		              <li class='nav-header' id="${item}"><a href="?op=dashboard&group=${item}&timeRange=${payload.timeRange}&date=${model.date}&domain=${model.domain}"><strong>${item}</strong></a></li>
	       </c:forEach>
           <c:forEach var="item" items="${model.productLines}" varStatus="status">
            		 <li class='nav-header' id="${item.id}"><a href="?date=${model.date}&domain=${model.domain}&product=${item.id}&timeRange=${payload.timeRange}"><strong>${item.title}</strong></a></li>
           </c:forEach>
            <li >&nbsp;</li>
          </ul>
        </div><!--/.well -->
      </div><!--/span-->
      <div class="span10">
      	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
   			<div style="float:left;">
   				<div id="${item.title}" class="metricGraph"></div>
   			</div>
	</c:forEach>
 </div></div>
 
<script type="text/javascript">

$(document).ready(function() {
 	var group = '${payload.group}';
 	
 	if(group==''){
 		$('#all').addClass('active');
 	}else{
 		$('#'+group).addClass('active');
 	}
 });
 </script>