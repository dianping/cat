<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
	<script type="text/javascript">
		$(document).ready(function() {
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
			
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});
			
			$(document).delegate('#updateSubmit', 'click', function(e){
				var name = $("#commandName").val();
				var title = $("#commandTitle").val();
				var domain = $("#commandDomain").val();
				var id = $("#commandId").val();
				
				if(name == undefined || name == ""){
					if($("#errorMessage").length == 0){
						$("#commandName").after($("<span class=\"text-error\" id=\"errorMessage\">  该字段不能为空</span>"));
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
	<div>
		<div class="row-fluid">
	        <div class="span2">
			<%@include file="../configTree.jsp"%>
			</div>
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
			<div class="span10">
				<h4 id="state" class="text-center text-error">&nbsp;</h4>
				<div>
				</br>
				<table class="table table-striped table-bordered table-condensed table-hover" id="contents" width="100%">
				<thead>
					<tr class="odd">
						<th width="20%">名称</th>
						<th width="35%">项目</th>
						<th width="30%">标题</th>
						<th width="15%">操作&nbsp;&nbsp;  <a class='btn btn-primary btn-small update' href="?op=appUpdate">新增</a></th>
					</tr></thead><tbody>
	
					<c:forEach var="item" items="${model.commands}">
						<tr>
							<td>${item.name }</td>
							<td>${item.domain }</td>
							<td>${item.title }</td>
							<td><a class='btn  btn-small btn-primary update' href="?op=appUpdate&id=${item.id}">编辑</a>
							<a class='delete btn  btn-small btn-danger' href="?op=appPageDelete&id=${item.id}">删除</a></td>
						</tr>
					</c:forEach></tbody>
					</tbody>
				</table>
				</div>
			</div>
		</div>
	</div>
</a:body>