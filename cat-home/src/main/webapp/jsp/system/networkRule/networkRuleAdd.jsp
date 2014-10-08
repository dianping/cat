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
			$('#networkRuleConfigList').addClass('active');
			
			$(document).delegate("#ruleSubmitButton","click",function(){
				$("#modalSubmit").trigger("click");
			});
			
		});
	</script>
	
	<div class="row-fluid">
        <div class="span2">
			<%@include file="../configTree.jsp"%>
		</div>
        <div class="span10">
		
	<form method="post">
	    规则ID： <input id="ruleId" type="text" value="${model.id}"/>
	    <div id="metrics">
    <strong class="text-success">匹配对象：</strong>
    <input id="metricsStr" type="hidden"></>
    <br>
    <div class="metric">
        网络设备：<textarea name="productlineText" class="productlineText " type=" text" placeholder="支持正则"></textarea>
        指标：<textarea name="metricText" class="metricText " type=" text" placeholder="支持正则"></textarea>
        监控类型：
        <label class="checkbox inline">
            <input name="count" class="count" type="checkbox">count
        </label>
        <label class="checkbox inline">
            <input name="sum" class="sum" type="checkbox">sum
        </label>
        <label class="checkbox inline">
            <input name="avg" class="avg" type="checkbox">avg
        </label>
        <button class="btn btn-danger btn-small delete-metric-button" type="button">
            <i class="icon-trash icon-white"></i>
        </button>
    </div>
</div>
<button class="btn btn-success btn-small" id="add-metric-button" type="button">
    添加匹配对象<i class="icon-plus icon-white"></i>
</button>
<br><br>
		${model.content}
		<div style='text-align:center'><input class="btn btn-primary" id="ruleSubmitButton" type="text" name="submit" value="提交"></button></div>
	</form></div></div>
</a:body>