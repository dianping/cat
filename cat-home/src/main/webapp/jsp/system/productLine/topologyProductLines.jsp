<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:body>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#topologyProductLines').addClass('active');
			$('#content .nav-tabs a').mouseenter(function (e) {
				  e.preventDefault();
				  $(this).tab('show');
			});
			
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});

			var action = '${payload.action.name}';
			if(action=='topologyProductLineDelete'||action=='topologyProductLineAddSubmit'){
				var state = '${model.opState}';
				if(state=='Success'){
					$('#state').html('操作成功');
				}else{
					$('#state').html('操作失败');
				}
				setInterval(function(){
					$('#state').html('&nbsp;');
				},3000);
			}
		});
	</script>
	<div class="row-fluid">
        <div class="span2">
			<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
			<!-- Modal -->
			<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			</div>
			<h4 id="state" class="text-center text-error">&nbsp;</h4>
			<table class="table table-striped table-bordered">
				<tr class="text-success">
					<th>产品线</th><th>标题</th><th>顺序</th><th>监控大盘显示</th><th>项目列表</th>
					<th>操作 <a href="?op=topologyProductLineAdd" class='update btn btn-primary btn-small'>新增</a></th>
				</tr>
				<c:forEach var="entry" items="${model.productLines}" varStatus="status">
					<c:set var='item' value='${entry.value}'/>
					<tr><td>${item.id}</td><td>${item.title}</td>
					<td>${item.order}</td><td>${item.dashboard }</td>
					<td>
						<c:forEach var="domain" items="${item.domains}"> 
							${domain.key},
						</c:forEach>
					</td>
					<td><a href="?op=topologyProductLineAdd&productLineName=${item.id}" class='update btn btn-primary btn-small'>修改</a>
					<a href="?op=topologyProductLineDelete&productLineName=${item.id}" class='delete btn-danger btn btn-primary btn-small'>删除</a></td>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</a:body>