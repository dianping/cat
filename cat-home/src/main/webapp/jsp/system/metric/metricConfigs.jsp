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
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#metricConfigList').addClass('active');
			
			var productLine = '${payload.productLineName}';
			if(productLine ==''){
				 productLine = 'TuanGou';
			}
			$('#tab-'+productLine).addClass('active');
			$('#tabContent-'+productLine).addClass('active');

			
			$(".delete").bind("click", function() {
				return confirm("确定要删除此项目吗(不可恢复)？");
			});
			
			$(document).delegate('.update', 'click', function(e){
				var anchor = this,
					el = $(anchor);
				
				if(e.ctrlKey || e.metaKey){
					return true;
				}else{
					e.preventDefault();
				}
				//var cell = document.getElementById('');
				$.ajax({
					type: "post",
					url: anchor.href,
					success : function(response, textStatus) {
						$('#myModal').html(response);
						$('#myModal').modal();
						$("#id").select2();
						metricValidate();
					}
				});
			});
			
			var action = '${payload.action.name}';
			if(action=='metricConfigDelete'||action=='metricConfigAddSumbit'||action=='metricRuleAddSubmit'){
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
			<div id="myModal" class="modal hide fade" style="width:800px" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
			</div>
			<h4 id="state" class="text-center text-error">&nbsp;</h4>
			<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
			  <ul class="nav nav-tabs span2">
			  	<c:forEach var="item" items="${model.productMetricConfigs}" varStatus="status">
			  		<c:set var="product" value="${item.key}"/>
				     <c:set var="key" value="${product.id}"/>
				    <li id="tab-${key}" class="text-right"><a href="#tabContent-${key}" data-toggle="tab"> <h5 class="text-error">${product.title}</h5></a></li>
				</c:forEach>
			  </ul>
			  <div class="tab-content">
			  	<c:forEach var="item" items="${model.productMetricConfigs}" varStatus="status">
				     <c:set var="product" value="${item.key}"/>
				     <c:set var="key" value="${product.id}"/>
				     <c:set var="value" value="${item.value}"/>
				     <div class="tab-pane" id="tabContent-${key}">
					    <h4 class="text-center text-error">${product.title}：产品线内业务监控配置&nbsp;&nbsp;&nbsp;&nbsp;<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>表示放入总的业务监控大盘</h4>
				     	<table class="table table-striped table-bordered table-condensed table-hover">
				     		<tr class="text-success">
				     			<th width="10%"><h5 class='text-center'>项目</h5></th>
				     			<th width="5%"><h5 class='text-center'>类型</h5></th>
				     			<th width="8%"><h5 class='text-center'>显示顺序</h5></th>
				     			<th width="8%"><h5 class='text-center'>是否告警</h5></th>
				     			<th width="16%"><h5 class='text-center'>MetricKey</h5></th>
				     			<th width="16%"><h5 class='text-center'>标题</h5></th>
				     			<th width="8%"><h5 class='text-center'>显示次数</h5></th>
				     			<th width="8%"><h5 class='text-center'>显示平均值</h5></th>
				     			<th width="8%"><h5 class='text-center'>显示总和</h5></th>
				     			<th width="13%"><h5 class='text-center'>操作
								<%--&nbsp;&nbsp;<a class="btn update btn-primary btn-small" href="?op=metricConfigAdd&metricKey=${config.metricKey}&domain=${config.domain}&productLineName=${key}">新增</a>
 								--%></h5></th>
				     		</tr>
					     	<c:forEach var="config" items="${value}">
				     			<tr>
				     			<td>${config.domain}</td>
				     			<td>${config.type}</td>
				     			<td>${config.viewOrder}</td>
				     			<td>
				     				<c:if test="${config.alarm}">
				     					<span class="text-error">是</span>
				     				</c:if>
				     				<c:if test="${config.alarm == false}">
				     					<span>否</span>
				     				</c:if>
				     			</td>
				     			<td style="word-wrap:break-word;word-break:break-all;">${config.metricKey}</td>
				     			<td style="word-wrap:break-word;word-break:break-all;">${config.title}</td>
				     			<td>${config.showCount}
				     				<c:if test="${config.showCountDashboard}">
				     					<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
				     				</c:if>
				     			</td>
				     			<td>${config.showAvg}
				     				<c:if test="${config.showAvgDashboard}">
				     					<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
				     				</c:if>
				     			</td>
				     			<td>${config.showSum}
				     				<c:if test="${config.showSumDashboard}">
				     					<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
				     				</c:if>
				     			</td>
					     		<td style="text-align:center;white-space: nowrap">
					     			<a href="?op=metricConfigAdd&metricKey=${config.metricKey}&type=${config.type}&domain=${config.domain}&productLineName=${key}" class="btn update btn-primary btn-small">修改</a>
						     		<a href="?op=metricConfigDelete&metricKey=${config.metricKey}&type=${config.type}&domain=${config.domain}&productLineName=${key}" class="btn btn-primary btn-small btn-danger delete">删除</a>
					     			<a href="?op=metricRuleAdd&metricKey=${config.metricKey}&type=${config.type}&domain=${config.domain}&productLineName=${key}" class="btn update btn-primary btn-small">告警规则</a>
						     	</td>
					     		</tr>
					     	</c:forEach>
				     	</table>
				     </div>
				</c:forEach>
			  </div>
		   </div>
		</div>
	</div>
</a:body>