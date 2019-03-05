<%@ page contentType="text/html; charset=utf-8" isELIgnored="false"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.server.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.server.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.server.Model"
	scope="request" />
<a:serverBody>
	<link rel="stylesheet" type="text/css"
		href="${model.webapp}/js/jquery.datetimepicker.css" />
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	<div class="breadcrumbs" id="breadcrumbs">
		<script type="text/javascript">
			try{ace.settings.check('breadcrumbs' , 'fixed')}catch(e){}
		</script>

		<ul class="breadcrumb">
		<table>
			<tr>
				<th>
					<div>
						&nbsp;开始 <input type="text" id="startTime" style="width: 150px;" />
						结束 <input type="text" id="endTime" style="width: 150px;" />
					</div>
				</th>
				<th>分类
				<select id="category" style="width: 150px">
					<c:forEach var="item" items="${model.serverMetricConfig.groups}" varStatus="status">
					  <option value="${item.key}">${item.key}</option>
					</c:forEach>
				</select>
				</th>
				<th>分组
					<select id="group" style="width: 100px">
					</select>
				</th>
				<th>
 				<div class="navbar-header pull-left position" style="width:350px;">
					<form id="wrap_search" style="margin-bottom:0px;">
					<div class="input-group">
						<span class="input-icon" style="width:300px;">
							<input type="text" placeholder="input endPoint for search" value="${payload.endPoint}" class="search-input search-input form-control ui-autocomplete-input" id="endPoint" autocomplete="off" />
								<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
						<span class="input-group-btn" style="width:50px">
							<button class="btn btn-sm btn-primary" type="button" id="search_go">
							Go
							</button>
						</span>
						</div>
					</form>
				</div>
 				</th>
			</tr>
		</table>

		</ul><!-- /.breadcrumb -->
	</div>
	
	<div class="page-content">
	<div class="page-content-area">
	<div class="row">
	<div class="col-xs-12">
	<div class="tabbable">
	<br>
	<div>
		<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
			<div style="float: left;">
				<div id="${item.id}" class="metricGraph" style="width:450px;height:350px;"></div>
			</div>
		</c:forEach>
	</div></div></div></div></div></div>


	<script type="text/javascript">
		function query() {
			var start = $("#startTime").val();
			var end = $("#endTime").val();
			var category = $("#category").val();
			var group = $("#group").val();
			var endPoint = $("#endPoint").val();

			window.location.href = "?op=view&category=" + category +"&group="+ group +"&startDate=" 
					+ start + "&endDate=" + end +"&endPoint=" + endPoint;
		}
		
		function groupChange() {
			var category = $("#category").val();
			var group = ${model.serverMetricConfigJson}[category];
			
			$("#group").empty();
			
			var opt = $('<option />');
			opt.html("All");
			opt.val("");
			opt.appendTo($("#group"));
			
			for ( var prop in group.items) {
				var opt = $('<option />');
				opt.html(group.items[prop].id);
				opt.val(group.items[prop].id);
				opt.appendTo($("#group"));
			}
			
			tagRefresh();
		}
		
		function tagRefresh(){
			$.widget( "custom.catcomplete", $.ui.autocomplete, {
				_renderMenu: function( ul, items ) {
					var that = this,
					currentCategory = "";
					$.each( items, function( index, item ) {
						if ( item.category != currentCategory ) {
							ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
							currentCategory = item.category;
						}
						that._renderItemData( ul, item );
					});
				}
			});
			
			var data = [];
			var category = $("#category").val();
			var endPoints = ${model.endPointsJson}[category];
			
			for ( var prop in endPoints) {
				var item = {};
				item['label'] = endPoints[prop] + ' ';
				item['category'] = category;
				data.push(item);
			}
					
			$( "#endPoint" ).catcomplete({
				delay: 0,
				source: data
			});
			
			if('${payload.endPoint}' == ''  || '${payload.category}' != category){
				$( "#endPoint" ).val(endPoints[0]);
			}
		}

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
			
			$('#startTime').val("${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm')}");
			$('#endTime').val("${w:format(payload.historyEndDate,'yyyy-MM-dd HH:mm')}");
			
			var category = '${payload.category}';
			
			if(category!=''){
				$("#category").val(category);
			}
			groupChange();
			$("#group").val("${payload.group}");
			$("#category").on('change',groupChange);
			
			$('#serverChart').addClass('active open');
			$('#view').addClass('active');
			
			tagRefresh();
			
			$("#search_go").bind("click",function(e){
				query();
			});
			
			$('#wrap_search').submit(
				function(){
					query();
					return false;
				}		
			);

			<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
				var data = ${item.jsonString};
				graphMetricChart(document.getElementById('${item.id}'), data);
			</c:forEach>
		});
	</script>

</a:serverBody>