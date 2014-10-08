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
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#networkRuleConfigList').addClass('active');
			
			$(".delete").bind("click", function() {
				return confirm("确定要删除此规则吗(不可恢复)？");
			});
			
			var state = '${model.opState}';
			if(state=='Success'){
				$('#state').html('操作成功');
			}else{
				$('#state').html('操作失败');
			}
			setInterval(function(){
				$('#state').html('&nbsp;');
			},3000);
		});
	</script>
	<div class="row-fluid">
        <div class="span2">
			<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
			<div id="ruleModal" class="modal hide fade" style="width:650px" tabindex="-1" role="dialog" aria-labelledby="ruleLabel" aria-hidden="true">
				<div class="modal-header text-center">
				    <h3 id="ruleLabel">网络规则配置</h3>
				</div>
				<div class="modal-body" id="ruleModalBody">
				</div>
				<div class="modal-footer">
				    <button class="btn btn-primary" id="ruleSubmitButton">提交</button>
				    <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
				</div>
			</div>
			<h4 id="state" class="text-center text-error">&nbsp;</h4>
			<table class="table table-striped table-bordered table-condensed table-hover">
	     		<tr class="text-success">
	     			<th width="20%"><h5 class='text-center'>规则id</h5></th>
	     			<th width="26%"><h5 class='text-center'>产品线配置</h5></th>
	     			<th width="29%"><h5 class='text-center'>指标配置</h5></th>
	     			<th width="4%"><h5 class='text-center'>次数</h5></th>
	     			<th width="7%"><h5 class='text-center'>平均值</h5></th>
	     			<th width="4%"><h5 class='text-center'>总和</h5></th>
	     			<th width="10%"><h5 class='text-center'>操作 <a href="?op=networkRuleUpdate&key=${item.id}" class="btn update btn-primary btn-small btn-primary">新增</a></h5></th>
	     		</tr>
		     	<c:forEach var="item" items="${model.ruleItems}" varStatus="status">
	     			<tr>
	     			<td>${item.id}</td>
	     			<td>${item.productlineText}</td>
	     			<td>${item.metricText}</td>
	     			<td>
	     				<c:if test="${item.monitorCount}">
	     					<span class="text-error">是</span>
	     				</c:if>
	     				<c:if test="${item.monitorCount == false}">
	     					<span>否</span>
	     				</c:if>
	     			</td>
	     			<td>
	     				<c:if test="${item.monitorAvg}">
	     					<span class="text-error">是</span>
	     				</c:if>
	     				<c:if test="${item.monitorAvg == false}">
	     					<span>否</span>
	     				</c:if>
	     			</td>
	     			<td>
	     				<c:if test="${item.monitorSum}">
	     					<span class="text-error">是</span>
	     				</c:if>
	     				<c:if test="${item.monitorSum == false}">
	     					<span>否</span>
	     				</c:if>
	     			</td>
		     		<td style="text-align:center;white-space: nowrap">
		     			<a href="?op=networkRuleUpdate&key=${item.id}" class="btn update btn-primary btn-small">修改规则</a>
			     		<a href="?op=networkRulDelete&key=${item.id}" class="btn btn-primary btn-small btn-danger delete">删除</a>
			     	</td>
		     		</tr>
		     	</c:forEach>
	     	</table>
		</div>
	</div>
</a:body>