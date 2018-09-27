<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>

<a:mobile>
<style type="text/css">
	.dataTables_filter input[type=search], .dataTables_filter input[type=text]{
		width: 200px;
	}
</style>
	
<style media="all" type="text/css">
    	.alignLeft { text-align: left; }
   		.alignCenter { text-align: center; }
</style>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/datatable/datatables.min.css"/>
	<script src="${model.webapp}/js/datatable/datatables.min.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#appCommandGroup').addClass('active');
			
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
				<c:forEach var="command" items="${model.commands}">
					var item = {};
					item['label'] = '${command.value.name}|${command.value.title}';
					if('${command.value.domain}'.length >0 ){
						item['category'] ='${command.value.domain}';
					}else{
						item['category'] ='未知项目';
					}
					
					data.push(item);
				</c:forEach>
						
				$( "#command" ).catcomplete({
					delay: 0,
					source: data
				});
				$('#namespace').change(function(){
					namespaceChange();
				})
				
				namespaceChange();
				
				$(document).on('click', 'th input:checkbox' , function(){
					var that = this;
					$(this).closest('table').find('tr > td:first-child input:checkbox')
					.each(function(){
						this.checked = that.checked;
						$(this).closest('tr').toggleClass('selected');
					});
				});
				
				$("#search_go").bind("click",function(e){
					window.location.href = "/cat/s/app?op=appCommandGroupAdd&name="+$("#command").val();
				});
				
				$('#wrap_search').submit(
						function(){
							window.location.href = "/cat/s/app?op=appCommandGroupAdd&name="+$("#command").val();
							return false;
						}		
					);
		});
		
		var namespaceChange = function namespaceChange() {
			var namespace = $("#namespace").val();
 			var cmds = ${model.namespace2CommandsJson}[namespace];
 			var subCommands = ${model.subCommandsJson};
 			var t = $("#commands").DataTable();
			
			t.clear();
 			
 			if(typeof(cmds)!="undefined" && cmds != ""){
 				$("#commandsBody").find('tr').remove();	
 				
				for ( var prop in cmds) {
					if(subCommands != null && subCommands.indexOf(cmds[prop].name) > -1){
						t.row.add(["<label class=\"position-relative\"> <input type=\"checkbox\" class=\"ace\" id="+ cmds[prop].id +" checked> "+
									"<span class=\"lbl\"></span></label>", cmds[prop].name]).draw();
					}else{
						t.row.add(["<label class=\"position-relative\"> <input type=\"checkbox\" class=\"ace\" id="+ cmds[prop].id +"> "+
									"<span class=\"lbl\"></span></label>", cmds[prop].name]).draw();
					}
				}
			}
		}
		
		function dataTableInit(){
			$('#commandsBody').dataTable( {
				bAutoWidth: false,
				"aoColumns": [
				  { "bSortable": false , sClass: "alignCenter"},
				  { "bSortable": true , sClass: "alignLeft"}
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
		
		$(document).delegate('#updateSubmit', 'click', function(e){
			var commands = [];
			$( "#commandsBody" ).find( ":checkbox" ).each(function(){    
				if($(this).prop('checked')){
					commands.push($(this).attr("id"));
				}
			});
			var parent = $("#command").val().split('|')[0];
			
			if(typeof parent != "undefined" && parent != "undefined"){
				window.location.href = "/cat/s/app?op=appCommandGroupSubmit&type=group&parent="+parent+"&name="+commands;
			}
		}) 
	</script>
	
	<table class="table table-striped table-condensed table-bordered ">
		<tr><td>父命令字</td><td>
		<div class="input-group" style="float:left;width:350px">
		            <form id="wrap_search" style="margin-bottom:0px;">
						<span class="input-icon" style="width:350px;">
							<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="command" autocomplete="on" value="${payload.name}"/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form> 
					<span class="input-group-btn">
						<button class="btn btn-sm btn-pink" type="button" id="search_go">
							Go
						</button> 
					</span><span class="input-group-addon">找不到想要的命令字？试试先<a href="/cat/s/app?op=appList" target="_blank">添加！</a></span>
	            </div></td>
		<tr>
			<td>子命令字</td><td>
			<select id="namespace">
				<c:forEach var="item" items="${model.namespace2Commands}">
					<option id="item.key">${item.key}</option>
				</c:forEach>
			</select>
			<div>
			<table id="commands" class="table table-striped  table-hover">
						<thead>
							<tr>
								<th class="left">
									<label class="position-relative">
										<input type="checkbox" class="ace" />
										<span class="lbl"></span>
									</label>
								</th>
								<th>命令字</th>
							</tr>
						</thead>
						<tbody id="commandsBody">
						</tbody>
					</table>
					</div>
		</td></tr>
		<tr>
			<td colspan="2" style="text-align:center;"><button class="btn btn-primary" id="updateSubmit">提交</button></td>
		</tr>
	</table>

</a:mobile>
