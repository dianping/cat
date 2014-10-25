<%@ page contentType="text/html; charset=utf-8"%>
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
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useCss value="${res.css.local['jqx.base.css']}" target="head-css" />
	<res:useJs value="${res.js.local['jqxcore.js']}" target="head-js" />
	<res:useJs value="${res.js.local['jqxbuttons.js']}" target="head-js" />
	<res:useJs value="${res.js.local['jqxscrollbar.js']}" target="head-js" />
	<res:useJs value="${res.js.local['jqxlistbox.js']}" target="head-js" />
    <res:useJs value="${res.js.local['jqxcombobox.js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#exceptionConfigList').addClass('active');
			var type = '${payload.type}';
			if (type == '') {
				type = '异常阈值';
			}
			$('#tab-' + type).addClass('active');
			$('#tabContent-' + type).addClass('active');
			
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});

			var action = '${payload.action.name}';
			if (action == 'exceptionThresholdDelete'
				|| action == 'exceptionThresholdUpdateSubmit'
				|| action == 'exceptionExcludeDelete'
				|| action == 'exceptionExcludeUpdateSubmit') {
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
			
			$(document).delegate('.update,.create', 'click', function(e){
				var anchor = this,
					el = $(anchor);
				
				if(e.ctrlKey || e.metaKey){
					return true;
				}else{
					e.preventDefault();
				}
				$.ajax({
					type: "get",
					url: anchor.href,
					success : function(response, textStatus) {
						var responseTrim = response.trim();
						$('#myModal').html(responseTrim);
						$('#myModal').modal();
 						$("#domainId").select2();
 						$("#smsSending").select2();
 						exceptionValidate();
					}
				});
			});
		});
	</script>
	
	<div class="row-fluid">
		<div class="span2">
			<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
			<!-- Modal -->
			<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true"></div>
			<h4 id="state" class="text-center text-error">&nbsp;</h4>
			<div class="tabbable tabs-left" id="content">
				<!-- Only required for left/right tabs -->
				<ul class="nav nav-tabs">
					<c:set var="keys" value="异常阈值,异常过滤" />
					<c:forEach var="key" items="${keys}" varStatus="status">
						<li id="tab-${key}" class="text-right"><a
							href="#tabContent-${key}" data-toggle="tab">
								<h5 class="text-error">${key}</h5>
						</a></li>
					</c:forEach>
				</ul>
				<div class="tab-content">
					<div class="tab-pane" id="tabContent-异常阈值">
						<h4 class="text-center text-error">异常阈值配置</h4>
						<table
							class="table table-striped table-bordered table-condensed table-hover"
							id="content-异常阈值" width="100%">
							<thead>
								<tr class="odd">
									<th width="25%">域名</th>
									<th width="40%">异常名称</th>
									<th width="10%">Warning阈值</th>
									<th width="10%">Error阈值</th>
									<th width="15%">操作&nbsp;&nbsp; <a
										class='create btn btn-primary btn-small'
										href="?op=exceptionThresholdAdd">新增</a></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="item" items="${model.exceptionLimits}"
									varStatus="status">
									<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
										<td>${item.domain}</td>
										<td>${item.id}</td>
										<td>${item.warning}</td>
										<td>${item.error}</td>
										<td>
											<a class='update btn  btn-small btn-primary' href="?op=exceptionThresholdUpdate&domain=${item.domain}&exception=${item.id}">编辑</a>
											<a class='delete btn  btn-small btn-danger' href="?op=exceptionThresholdDelete&domain=${item.domain}&exception=${item.id}&type=异常阈值">删除</a>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>

					<div class="tab-pane" id="tabContent-异常过滤">
						<h4 class="text-center text-error">异常过滤配置</h4>
						<table
							class="table table-striped table-bordered table-condensed table-hover"
							id="contents-异常过滤" width="100%">
							<thead>
								<tr class="odd">
									<th width="25%">域名</th>
									<th width="60%">异常名称</th>
									<th width="15%">操作&nbsp;&nbsp; <a
										class='create btn btn-primary btn-small'
										href="?op=exceptionExcludeAdd">新增</a></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="item" items="${model.exceptionExcludes}"
									varStatus="status">
									<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
										<td>${item.domain}</td>
										<td>${item.id}</td>
										<td>
											<a class='update btn  btn-small btn-primary' href="?op=exceptionExcludeUpdate&domain=${item.domain}&exception=${item.id}">编辑</a>
											<a class='delete btn  btn-small btn-danger' href="?op=exceptionExcludeDelete&domain=${item.domain}&exception=${item.id}&type=异常过滤">删除</a>
										</td>
									</tr>
								</c:forEach>
							</tbody>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
</a:body>