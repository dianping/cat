<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#appList').addClass('active');
			
			var state = '${model.opState}';
			if(state=='Success'){
				$('#state').html('操作成功');
			}else{
				$('#state').html('操作失败');
			}
			setTimeout(function(){
				$('#state').html('&nbsp;');
			},3000);
			
			var type = "${payload.type}";
			
			if(typeof type != "undefined" && type.length > 0) {
				$('#tab-'+ type).addClass('active');
				$('#tabContent-'+ type).addClass('active');
			}else {
				$('#tab-api').addClass('active');
				$('#tabContent-api').addClass('active');
			}
			
			$("#tab-api-default").addClass('active');
			$("#tabContent-api-default").addClass('active');
			$("#tab-activity-default").addClass('active');
			$("#tabContent-activity-default").addClass('active');
			$("#tab-constant-版本").addClass('active');
			$("#tabContent-constant-版本").addClass('active');
			
			$('#batchInsert').bind("click",function(e){
				if (confirm("确认要进行批量添加吗？") == true){
					var items = document.getElementsByClassName('deleteItem');
					var content = "";
					var length = items.length;
					
					for(var i=0;i<length;i++){
						var item = items[i];
						if(item.checked == true){
							content = content + item.value + ",";
						}
					}
					window.location.href = "?op=appRuleBatchUpdate&type=batch&content="+content;
				}		
			});
			
			$(document).delegate('#updateSubmit', 'click', function(e){
				var name = $("#commandName").val();
				var title = $("#commandTitle").val();
				var domain = $("#commandDomain").val();
				var id = $("#commandId").val();
				
				if(name == undefined || name == ""){
					if($("#errorMessage").length == 0){
						$("#commandName").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
					}
					return;
				}
				if(title==undefined){
					title = "";
				}
				if(domain==undefined){
					domain="";
				}
				if(id==undefined){
					id="";
				}
				
				window.location.href = "/cat/s/config?op=appSubmit&name="+name+"&title="+title+"&domain="+domain+"&id="+id;
			})
 		});
	</script>
			<div class="tabbable" id="content"> <!-- Only required for left/right tabs -->
				<ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height:50px;" id="myTab">
				    <li id="tab-api" class="text-right"><a href="#tabContent-api" data-toggle="tab"> <strong>API命令字</strong></a></li>
				    <li id="tab-batch" class="text-right"><a href="#tabContent-batch" data-toggle="tab"><strong>批量添加命令字</strong></a></li>
				    <li id="tab-code" class="text-right"><a href="#tabContent-code" data-toggle="tab"> <strong>返回码</strong></a></li>
				    <li id="tab-speed" class="text-right"><a href="#tabContent-speed" data-toggle="tab"><strong>测速配置</strong></a></li>
				    <li id="tab-constant" class="text-right"><a href="#tabContent-constant" data-toggle="tab"><strong>常量配置</strong></a></li>
				</ul>
				<div class="tab-content">
					<div class="tab-pane" id="tabContent-api">
						<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
						
						  <ul class="nav nav-tabs padding-12 ">
						  	<c:forEach var="entry" items="${model.apiCommands}" varStatus="status">
							    <li id="tab-api-${entry.key}" class="text-right"><a href="#tabContent-api-${entry.key}" data-toggle="tab"> ${entry.key}</a></li>
							</c:forEach>
						  </ul>
						  <div class="tab-content">
						  	<c:forEach var="entry" items="${model.apiCommands}" varStatus="status">
							  	<div class="tab-pane" id="tabContent-api-${entry.key}">
								    <table class="table table-striped table-condensed table-bordered table-hover">
									    <thead><tr>
												<th width="30%">名称</th>
												<th width="32%">标题</th>
												<th width="10%">加入全量统计</th>
												<th width="10%">过滤阈值</th>
												<th width="8%">操作 <a href="?op=appUpdate&type=api&id=-1" class="btn btn-primary btn-xs" >
												<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
											</tr>
										</thead>
										
								    	<c:forEach var="command" items="${entry.value}">
									    	<tr><td>${command.name}</td>
											<td>${command.title}</td>
											<td class="center">
												<c:choose>
												<c:when test="${command.all}">
													<button class="btn btn-xs btn-success">
													<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
													</button>
												</c:when>
												<c:otherwise>
													<i class="ace-icon glyphicon glyphicon-remove bigger-120"></i>
												</c:otherwise>
												</c:choose>
											</td>
											<td>${command.threshold}</td>
											<c:if test="${command.id ne 0 }">
												<td><a href="?op=appUpdate&id=${command.id}&type=api" class="btn btn-primary btn-xs">
													<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
													<a href="?op=appPageDelete&id=${command.id}&type=api" class="btn btn-danger btn-xs delete" >
													<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
												
											</c:if></tr>
								    	</c:forEach>
								    </table>
							    </div>
							</c:forEach>
						  </div>
						</div>
						
					</div>
					<div class="tab-pane"  id="tabContent-batch">
						<h4 class="text-center text-danger">合法的命令字&nbsp;&nbsp;${w:size(model.validatePaths)}</h4>
						<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
								<tr><td></td><td><button class="btn btn-xs btn-danger" id="batchInsert">批量添加</button></td></tr>
							<c:forEach var="item" items="${model.validatePaths}">
								<tr><td width="10%"><input type="checkbox" class="deleteItem" value="${item}" checked></td><td>${item}</td><tr>
							</c:forEach>
						</table>
						
						<h4 class="text-center text-danger">非法命令字&nbsp;&nbsp;${w:size(model.invalidatePaths)}</h4>
						
						<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
							<c:forEach var="item" items="${model.invalidatePaths}">
								<tr><td width="10%"><input type="checkbox" class="deleteItem" value="${item}"></td><td>${item}</td><tr>
							</c:forEach>
						</table>
					</div>
					<div class="tab-pane" id="tabContent-code">
						<%@include file="code.jsp"%>
					</div>
					<div class="tab-pane" id="tabContent-speed">
						<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
							<thead>
							<tr >
								<th width="20%">页面</th>
								<th width="20%">加载阶段</th>
								<th width="20%">说明</th>
								<th width="20%">延时阈值(毫秒)</th>
								<th width="8%">操作 <a href="?op=appSpeedAdd&type=speed" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
							</tr>
							</thead>
							<tbody>
							<c:forEach var="entry" items="${model.speeds}">
							<c:set var="item" value="${entry.value}"/>
								<tr>
									<td>${item.page}</td>
									<td>${item.step}</td>
									<td>${item.title}</td>
									<td>${item.threshold}</td>
									<td><a href="?op=appSpeedUpdate&id=${item.id}&type=speed" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=appSpeedDelete&id=${item.id}&type=speed" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
								</tr>
							</c:forEach>
							</tbody>
						</table>
					</div>
					
					<div class="tab-pane" id="tabContent-constant">
					
						<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
							  <ul class="nav nav-tabs padding-12 ">
							  	<c:forEach var="entry" items="${model.configItems}" varStatus="status">
								    <li id="tab-constant-${entry.key}" class="text-right"><a href="#tabContent-constant-${entry.key}" data-toggle="tab"> ${entry.key}</a></li>
								</c:forEach>
							  </ul>
							  <div class="tab-content">
							  	<c:forEach var="entry" items="${model.configItems}" varStatus="status">
								  	<div class="tab-pane" id="tabContent-constant-${entry.key}">
									    <table class="table table-striped table-condensed table-bordered table-hover">
										    <thead><tr>
													<th>ID</th>
													<th>值</th>
													<c:if test="${entry.key eq '版本'}">
														<th width="5%"><a href="?op=appConstantAdd&type=${entry.key}" class="btn btn-primary btn-xs" >
														<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
													</c:if>
												</tr>
											</thead>
											
									    	<c:forEach var="e" items="${entry.value.items}">
										    	<tr><td>${e.value.id}</td>
												<td>${e.value.name}</td>
												<c:if test="${entry.key eq '版本'}">
													<td><a href="?op=appConstantUpdate&id=${e.key}&type=${entry.key}" class="btn btn-primary btn-xs">
														<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
													</td>
												</c:if>
									    	</c:forEach>
									    </table>
								    </div>
								</c:forEach>
							  </div>
							</div>
					</div>
					
				</div>
			</div>
</a:config>
