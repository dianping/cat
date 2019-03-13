<%@ page contentType="text/html; charset=utf-8" isELIgnored="false"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.server.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.server.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.server.Model" scope="request"/>
<a:serverBody>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/datatable/datatables.min.css"/>
	<script src="${model.webapp}/js/datatable/datatables.min.js"></script>
	
	
	<div class="col-xs-12 col-sm-4">
		<div class="widget-box">
			<div class="widget-header">
				<h4 class="widget-title">搜索EndPoint</h4>
			</div>

			<div class="widget-body">
				<div class="widget-main">
					<div>
						<label for="wrap_search_endPoint">EndPoint</label>
						<form id="wrap_search_endPoint" style="margin-bottom:0px;">
							<div class="input-group">
							<input id="endPoint_keywords" type="text" class="search-input form-control ui-autocomplete-input" placeholder="空格分隔多个搜索关键字" autocomplete="off"/>
							<span class="input-group-btn">
								<button class="btn btn-sm btn-primary" type="button" id="search_endPoint_go">
									Go
								</button> 
							</span>
							</div>
							</form>
					</div>
					<hr>
					<div>
						<div>
						<label for="wrap_search_tag">标签&nbsp;<strong class="text-danger">(eg: domain='cat'，单引号！！！)</strong></label>
						<form id="wrap_search_tag" style="margin-bottom:0px;">
							<div class="input-group">
							<input id="tag_keywords" type="text" class="search-input form-control ui-autocomplete-input" placeholder="空格分隔多个键值对" autocomplete="off"/>
							<span class="input-group-btn">
								<button class="btn btn-sm btn-primary" type="button" id="search_tag_go">
									Go
								</button> 
							</span>
							</div>
							</form>
					</div>
					</div>
					<hr>
					<div>
						<table id="endPoints" class="table table-striped  table-hover">
							<thead>
								<tr><th class="center">
									<label class="position-relative">
										<input type="checkbox" class="ace">
										<span class="lbl"></span>
									</label>
								</th>
								<th style="text-align:right"><a href="javascript:refreshMeasure()">刷新指标列表</a></th></tr>
							</thead>
							<tbody id="endPointBody"></tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<div class="col-xs-8">
		<div class="widget-box">
			<div class="widget-header">
				<h4 class="widget-title">搜索Measurement</h4>
			</div>

			<div class="widget-body">
				<div class="widget-main">
					<div >
						<table width="100%">
							<tr><th class="right">
							<span>查看不同视角的趋势图</span>
							</th><th class="right">
							<div class="btn-group">
								<button data-toggle="dropdown" class="btn btn-info btn-sm dropdown-toggle" data-rel="popover" data-trigger="hover" data-placement="top"
							    data-content="<div class='row text-danger center'>测试失败</div>" title="${device}" data-original-title="">看图
									<span class="ace-icon fa fa-caret-down icon-only"></span>
							    </button>
								<ul class="dropdown-menu dropdown-info dropdown-menu-right">
									<li><a href="javascript:buildview('endPoint')">EndPoint视角</a></li>
									<li><a href="javascript:buildview('measurement')">Measure视角</a></li>
								</ul>
							</div>
							</th></tr>
						</table>
					</div>
					<hr>
					
					<table id="measurement" class="table table-striped  table-hover">
						<thead>
							<tr>
								<th class="center">
									<label class="position-relative">
										<input type="checkbox" class="ace" />
										<span class="lbl"></span>
									</label>
								</th>
								<th>Measurement</th>
							</tr>
						</thead>
						<tbody id="measurementBody">
						</tbody>
					</table>
				</div>
			</div>
		</div>
	</div>
	
	<style type="text/css">
	.dataTables_filter input[type=search], .dataTables_filter input[type=text]{
		width: 200px;
	}
	</style>
	
	<style media="all" type="text/css">
    	.alignLeft { text-align: left; }
   		.alignCenter { text-align: center; }
	</style>
	
	<script type="text/javascript">
		function queryEndPoints(type){
			var keywords = $("#"+type+"_keywords").val();
			
			$.getJSON( "?op=endPoint&keywords="+keywords+"&search="+type, function( data ) {
				$("#endPointBody").empty();
   			  	$.each( data, function( key, val ) {
   			  	 $.each(val, function (index, data) {
   			  		$("#endPointBody").append("<tr> <td class=\"center\">" +
						"<label class=\"position-relative\"> <input type=\"checkbox\" class=\"ace\" id="+ data +"> "+
						"<span class=\"lbl\"></span></label></td><td>" + data + "</td></tr>");
   			    	});
   			  	});	
 			});
		}
		
		function buildview(view){
			var endPoints = [];
			$( "#endPointBody" ).find( ":checkbox" ).each(function(){    
				if($(this).prop('checked')){
					endPoints.push($(this).attr("id"));
				}
			});
			
			var measures = [];
			$( "#measurementBody" ).find( ":checkbox" ).each(function(){    
				if($(this).prop('checked')){
					measures.push($(this).attr("id"));
				}
			});
			
		    var url = "?op=buildview&endPoints="+endPoints+"&measurements="+measures+"&view="+view+"&graphId="+new Date().getTime();
		    $.getJSON( url, function( data ) {
   			  	$.each( data, function( key, val ) {
   			  		window.open("?op=graph&&view="+view+"&graphId="+val,'_blank');
   			  	});	
 			});
		;
		}
		
		function refreshMeasure(){
			var ids = [];
			$( "#endPointBody" ).find( ":checkbox" ).each(function(){    
				if($(this).prop('checked')){
					ids.push($(this).attr("id"));
				}
			});
			
			$.getJSON( "?op=measurement&endPoints="+ids, function( data ) {
				$("#measurementBody").find('tr').remove();
				var t = $("#measurement").DataTable();
				
				t.clear();
   			  	$.each( data, function( key, val ) {
   			  		$.each(val, function (index, data) {
						t.row.add(["<label class=\"position-relative\"> <input type=\"checkbox\" class=\"ace\" id="+ data +"> "+
										"<span class=\"lbl\"></span></label>", data]).draw();
   			    	});
   			  	});	
 			});
		}
		
		function dataTableInit(){
			$('#measurement').dataTable( {
				bAutoWidth: false,
				"aoColumns": [
				  { "bSortable": false , sClass: "alignCenter"},
				  { "bSortable": false , sClass: "alignLeft"}
				],
				"aaSorting": [],
		
				//,
				//"sScrollY": "200px",
				//"bPaginate": false,
		
				//"sScrollX": "100%",
				//"sScrollXInner": "120%",
				//"bScrollCollapse": true,
				//Note: if you are applying horizontal scrolling (sScrollX) on a "."
				//you may want to wrap the table inside a "div.dataTables_borderWrap" element
		
				"iDisplayLength": 50
		    } );
		}

		$(document).ready(
			function() {
				$('#serverChart').addClass('active open');
				$('#serverAggregate').addClass('active');
				
				$(document).on('click', 'th input:checkbox' , function(){
					var that = this;
					$(this).closest('table').find('tr > td:first-child input:checkbox')
					.each(function(){
						this.checked = that.checked;
						$(this).closest('tr').toggleClass('selected');
					});
				});
				
				$("#search_endPoint_go").bind("click",function(e){
					queryEndPoints('endPoint');
				});
				$('#wrap_search_endPoint').submit(
					function(){
						queryEndPoints('endPoint');
						return false;
					}		
				);
				$("#search_tag_go").bind("click",function(e){
					queryEndPoints('tag');
				});
				$('#wrap_search_tag').submit(
					function(){
						queryEndPoints('tag');
						return false;
					}		
				);
				dataTableInit();
			});		
	</script>
	
</a:serverBody>