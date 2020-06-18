<%@ page contentType="text/html;charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
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
			$('#alert_config').addClass('active open');
			$('#exception').addClass('active');
			var type = "${payload.type}";
			if (type == '') {
				type = 'threshold';
			}
			$('#tab-' + type).addClass('active');
			$('#tabContent-' + type).addClass('active');
			
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
		});
	</script>
			<div class="tabbable tabs-left" id="content">
				<!-- Only required for left/right tabs -->
				<ul class="nav nav-tabs">
					<li id="tab-threshold" class="text-right"><a href="#tabContent-threshold" data-toggle="tab">异常阈值</a></li>
					<li id="tab-exclude" class="text-right"><a href="#tabContent-exclude" data-toggle="tab">异常过滤</a></li>
				</ul>
				<div class="tab-content">
					<div class="tab-pane" id="tabContent-threshold">
						<h5 class="text-center text-danger">异常阈值配置</h5>
						<table class="table table-striped table-condensed table-bordered  table-hover"
							id="content-threshold" width="100%">
							<thead>
								<tr >
									<th width="25%">域名</th>
									<th width="37%">异常名称</th>
									<th width="12%">Warning阈值</th>
									<th width="10%">Error阈值</th>
									<th width="8%">是否告警</th>
									<th width="8%">操作 <a href="?op=exceptionThresholdAdd" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
										
								</tr>
							</thead>
							<tbody>
								<c:forEach var="item" items="${model.exceptionLimits}"
									varStatus="status">
									<tr class="">
										<td>${item.domain}</td>
										<td>${item.name}</td>
										<td>${item.warning}</td>
										<td>${item.error}</td>
										<td>
                                            <c:choose>
                                                <c:when test="${item.available == false}">
                                                    <span>否</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-danger">是</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
										<td>
							<a href="?op=exceptionThresholdUpdate&domain=${item.domain}&exception=${item.name}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=exceptionThresholdDelete&domain=${item.domain}&exception=${item.name}&type=threshold" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a>
										</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>

					<div class="tab-pane" id="tabContent-exclude">
						<h5 class="text-center text-danger">异常过滤配置</h5>
						<table class="table table-striped table-condensed  table-bordered table-hover"
							id="contents-exclude" width="100%">
							<thead>
								<tr >
									<th width="35%">域名</th>
									<th width="60%">异常名称</th>
									<th width="5%"><a href="?op=exceptionExcludeAdd" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="item" items="${model.exceptionExcludes}"
									varStatus="status">
									<tr class="">
										<td>${item.domain}</td>
										<td>${item.name}</td>
										<td><a href="?op=exceptionExcludeDelete&domain=${item.domain}&exception=${item.name}&type=exclude" class="btn btn-danger btn-xs delete" >
										<i class="ace-icon fa fa-trash-o bigger-120"></i></a>
										</td>
									</tr>
								</c:forEach>
							</tbody>
							</tbody>
						</table>
					</div>
		</div>
	</div>
</a:config>
