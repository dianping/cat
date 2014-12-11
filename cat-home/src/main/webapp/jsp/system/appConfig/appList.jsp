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
			
			$(document).delegate('.update', 'click', function(e){
				var anchor = this,
					el = $(anchor);
				
				if(e.ctrlKey || e.metaKey){
					return true;
				}else{
					e.preventDefault();
				}
				$.ajax({
					type: "post",
					url: anchor.href,
					success : function(response, textStatus) {
						$('#modalBody').html(response);
						$('#modal').modal();
					}
				});
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
			<div id="modal" class="modal hide fade" style="width:650px" tabindex="-1" role="dialog" aria-labelledby="ruleLabel" aria-hidden="true">
				<div class="modal-header text-center">
				    <h3>App Command编辑</h3>
				</div>
				<div class="modal-body" id="modalBody">
				</div>
				<div class="modal-footer">
				    <button class="btn btn-primary" id="updateSubmit">提交</button>
				    <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
				</div>
			</div>
			<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
				<ul class="nav nav-tabs span2" id="myTab">
				    <li id="tab-api" class="text-right"><a href="#tabContent-api" data-toggle="tab"> <h5 class="text-danger">API命令字</h5></a></li>
				    <li id="tab-activity" class="text-right"><a href="#tabContent-activity" data-toggle="tab"> <h5 class="text-danger">活动命令字</h5></a></li>
				    <li id="tab-code" class="text-right"><a href="#tabContent-code" data-toggle="tab"> <h5 class="text-danger">返回码</h5></a></li>
				    <li id="tab-speed" class="text-right"><a href="#tabContent-speed" data-toggle="tab"> <h5 class="text-danger">测速配置</h5></a></li>
				</ul>
				<div class="tab-content">
					<div class="tab-pane" id="tabContent-api">
						<table class="table table-striped table-condensed   table-hover" id="contents" width="100%">
							<thead>
							<tr >
								<th width="30%">名称</th>
								<th width="30%">项目</th>
								<th width="32%">标题</th>
								<th width="8%">操作 <a href="?op=appUpdate&type=api" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
							</tr>
							</thead>
							<tbody>
							<c:forEach var="item" items="${model.commands}">
								<c:if test="${item.id lt 1000}">
									<tr>
										<td>${item.name }</td>
										<td>${item.domain }</td>
										<td>${item.title }</td>
										<td><a href="?op=appUpdate&id=${item.id}&type=api" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="??op=appPageDelete&id=${item.id}&type=api" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
									</tr>
								</c:if>
							</c:forEach>
							</tbody>
						</table>
					</div>
					<div class="tab-pane" id="tabContent-activity">
						<table class="table table-striped table-condensed   table-hover" id="contents" width="100%">
							<thead>
							<tr >
								<th width="40%">名称</th>
								<th width="15%">项目</th>
								<th width="27%">标题</th>
								<th width="8%">操作 <a href="?op=appUpdate&type=activity" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
							</tr></thead>
							
							<tbody>
							<c:forEach var="item" items="${model.commands}">
								<c:if test="${item.id ge 1000}">
									<tr>
										<td>${item.name }</td>
										<td>${item.domain }</td>
										<td>${item.title }</td>
										<td><a href="?op=appUpdate&id=${item.id}&type=activity" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=appPageDelete&id=${item.id}&type=activity" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
									</tr>
								</c:if>
							</c:forEach>
							</tbody>
						</table>
					</div>
					<div class="tab-pane" id="tabContent-code">
						<%@include file="code.jsp"%>
					</div>
					<div class="tab-pane" id="tabContent-speed">
						<table class="table table-striped table-condensed   table-hover" id="contents" width="100%">
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
									<td>${item.page }</td>
									<td>${item.step }</td>
									<td>${item.title }</td>
									<td>${item.threshold }</td>
									<td><a href="?op=appSpeedUpdate&id=${item.id}&type=speed" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=appSpeedDelete&id=${item.id}&type=speed" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
								</tr>
							</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
			</div>
</a:config>
