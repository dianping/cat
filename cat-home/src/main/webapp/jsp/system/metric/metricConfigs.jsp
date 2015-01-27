<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useCss value="${res.css.local['multiple-select.css']}" target="head-css" />
	<res:useJs value="${res.js.local['jquery.multiple.select.js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#application_config').addClass('active open');
			$('#metricConfigList').addClass('active');
			
			var productLine = '${payload.productLineName}';
			$('#tab-'+productLine).addClass('active');
			$('#tabContent-'+productLine).addClass('active');
			
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
			<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
			  <ul class="nav nav-tabs padding-12 ">
			  	<c:forEach var="item" items="${model.productMetricConfigs}" varStatus="status">
			  		<c:set var="product" value="${item.key}"/>
				     <c:set var="key" value="${product.id}"/>
				    <li id="tab-${key}" class="text-right"><a href="#tabContent-${key}" data-toggle="tab"> ${product.title}</a></li>
				</c:forEach>
			  </ul>
			  <div class="tab-content">
			  	<c:forEach var="item" items="${model.productMetricConfigs}" varStatus="status">
				     <c:set var="product" value="${item.key}"/>
				     <c:set var="key" value="${product.id}"/>
				     <c:set var="value" value="${item.value}"/>
				     <div class="tab-pane" id="tabContent-${key}">
					    <h4 class="text-center text-danger">${product.title}：产品线内业务监控配置&nbsp;&nbsp;&nbsp;&nbsp;业务大盘标签会默认进行基线告警</h4>
				     	<table class="table table-striped table-condensed table-bordered table-hover">
				     		<tr class="text-success">
				     			<th width="9%"><h5 class='text-center'>项目</h5></th>
				     			<th width="4%"><h5 class='text-center'>类型</h5></th>
				     			<th width="6%"><h5 class='text-center'>显示顺序</h5></th>
				     			<th width="6%"><h5 class='text-center'>是否告警</h5></th>
				     			<th width="14%"><h5 class='text-center'>MetricKey</h5></th>
				     			<th width="16%"><h5 class='text-center'>标题</h5></th>
				     			<th width="6%"><h5 class='text-center'>次数</h5></th>
				     			<th width="6%"><h5 class='text-center'>平均值</h5></th>
				     			<th width="6%"><h5 class='text-center'>总和</h5></th>
				     			<th width="20%"><h5 class='text-center'>标签</h5></th>
				     			<th width="13%"><h5 class='text-center'>操作
								<%--&nbsp;&nbsp;<a class="btn update btn-primary btn-xs" href="?op=metricConfigAdd&metricKey=${config.metricKey}&domain=${config.domain}&productLineName=${key}">新增</a>
 								--%></h5></th>
				     		</tr>
					     	<c:forEach var="config" items="${value}">
				     			<tr>
				     			<td>${config.domain}</td>
				     			<td>${config.type}</td>
				     			<td>${config.viewOrder}</td>
				     			<td>
				     				<c:if test="${config.alarm}">
				     					<span class="text-danger">是</span>
				     				</c:if>
				     				<c:if test="${config.alarm == false}">
				     					<span>否</span>
				     				</c:if>
				     			</td>
				     			<td style="word-wrap:break-word;word-break:break-all;">${config.metricKey}</td>
				     			<td style="word-wrap:break-word;word-break:break-all;">${config.title}</td>
				     			<td>
				     				<c:if test="${config.showCount}">
				     					<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
				     				</c:if>
				     			</td>
				     			<td>
				     				<c:if test="${config.showAvg}">
				     					<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
				     				</c:if>
				     			</td>
				     			<td>
				     				<c:if test="${config.showSum}">
				     					<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
				     				</c:if>
				     			</td>
				     			<td>
				     				<c:if test="${config.tags!=null}">
				     					<c:forEach var="tag" items="${config.tags}">
				     						<span class="label label-info">${tag.name}</span>, 
				     					</c:forEach>
				     				</c:if>
				     			</td>
					     		<td style="text-align:center;white-space: nowrap">
						     	<a href="?op=metricConfigAdd&metricKey=${config.metricKey}&type=${config.type}&domain=${config.domain}&productLineName=${key}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=metricConfigDelete&metricKey=${config.metricKey}&type=${config.type}&domain=${config.domain}&productLineName=${key}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a>
					     			<a href="?op=metricRuleAdd&metricKey=${config.metricKey}&type=${config.type}&domain=${config.domain}&productLineName=${key}" id="alertRule" class="btn btn-primary btn-xs">告警</a>
						     	</td>
					     		</tr>
					     	</c:forEach>
				     	</table>
				     </div>
				</c:forEach>
			  </div>
		   </div>
</a:config>
