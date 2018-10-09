<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>

<a:mobile>
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
			
			var namespace = "${payload.namespace}";
			
			if(namespace == ""){
				for (var ns in ${model.domain2CommandsJson}) {
					namespace = ns;
					break;
				}
			}
			
			if(typeof namespace != "undefined" && namespace.length > 0) {
				$('#tab-'+ namespace).addClass('active');
				$('#tabContent-'+ namespace).addClass('active');
			}else{
				$('#tab-点评主APP').addClass('active');
				$('#tabContent-点评主APP').addClass('active');
			}
			
			<c:forEach var="item" items="${model.apiCommands}">
				$("#tab-${item.key}-default").addClass('active');
				$("#tabContent-${item.key}-default").addClass('active');
			</c:forEach>
			
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
				
				window.location.href = "/cat/s/app?op=appSubmit&name="+name+"&title="+title+"&domain="+domain+"&id="+id;
			})
 		});
	</script>
			<div class="tabbable" id="content"> <!-- Only required for left/right tabs -->
				<ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height:50px;" id="myTab">
					<c:forEach var="item" items="${model.apiCommands}">
						<li id="tab-${item.key}" class="text-right"><a href="#tabContent-${item.key}" data-toggle="tab"> <strong>${item.key}</strong></a></li>
					</c:forEach>
				</ul>
				<div class="tab-content">
					<c:forEach var="item" items="${model.apiCommands}">
					<div class="tab-pane" id="tabContent-${item.key}">
						<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
						
						  <ul class="nav nav-tabs padding-12 ">
						  	<c:forEach var="entry" items="${item.value.commands}" varStatus="status">
							    <li id="tab-${item.key}-${entry.key}" class="text-right"><a href="#tabContent-${item.key}-${status.index}" data-toggle="tab"> ${entry.key}</a></li>
							</c:forEach>
						  </ul>
						  <div class="tab-content">
						  	<c:forEach var="entry" items="${item.value.commands}" varStatus="status">
							  	<div class="tab-pane" id="tabContent-${item.key}-${status.index}">
								    <table class="table table-striped table-condensed table-bordered table-hover">
									    <thead><tr>
												<th width="30%">名称</th>
												<th width="32%">标题</th>
												<th width="10%">过滤阈值</th>
												<th width="15%">
													<a href="?op=appBatchAdd&type=api&id=-1&namespace=${item.key}" class="btn btn-primary btn-xs"><i class="ace-icon glyphicon glyphicon-plus bigger-120"></i>批量</a>
												 	<a href="?op=appUpdate&type=api&id=-1&namespace=${item.key}" class="btn btn-primary btn-xs" >
												<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i>单个</a></th>
											</tr>
										</thead>
										
								    	<c:forEach var="command" items="${entry.value}">
									    	<tr><td>${command.name}</td>
											<td>${command.title}</td>
											<td>${command.threshold}</td>
											<c:if test="${command.id ne 0 }">
												<td><a href="?op=appUpdate&id=${command.id}&type=api&namespace=${item.key}" class="btn btn-primary btn-xs">
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
					</c:forEach>
				</div>
			</div>
</a:mobile>
