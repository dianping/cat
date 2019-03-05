<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.business.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.business.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.business.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />

			<form method="post">
				<h3 class="text-center text-success">编辑应用监控规则</h3>
				
				<div class="config" style="display:none">
				<strong class="text-success">规则ID</strong> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="ruleId" type="text" value="${model.id}" /> <span class="text-danger">String，唯一性</span>
				</div>
				<div id="metrics" class="config">
					<h4 class="btn btn-success btn-xs" >
						匹配对象<i class="icon-plus icon-white"></i>
					</h4>
					<div id="metricItem" class="metric config">
						监控类型： <label class="checkbox inline"> <input name="metricType" value="COUNT"
							id="COUNT" type="radio">count
						</label> <label class="checkbox inline"> <input name="metricType" value="AVG"
							id="AVG" type="radio">avg
						</label> <label class="checkbox inline"> <input name="metricType" value="SUM"
							id="SUM" type="radio">sum
						</label>
					</div>
				</div>
				${model.content}
				<div style='text-align: center'>
					<input class="btn btn-primary btn-sm" id="ruleSubmitButton" type="text"
						name="submit" value="提交">
				</div>
			</form>
	
	<script type="text/javascript">
		$(document).ready(function() {
			initRuleConfigs();
			$('#application_config').addClass('active open');
			$('#businessConfig').addClass('active');
			var attributes = '${payload.attributes}';
			
			if (attributes != null && attributes != '') {
				document.getElementById(attributes).checked = true;
			}
			
			$(document).delegate("#ruleSubmitButton","click",function(){
				var domain = '${payload.domain}';
				var key = '${payload.key}';
				var metrics = $('input:radio[name="metricType"]:checked').val(); 
				
				var configStr = generateConfigsJsonString();
			    window.location.href = "?op=alertRuleAddSubmit&content=" + encodeURIComponent(configStr) + "&key=" + encodeURIComponent(key) +"&attributes=" + encodeURIComponent(metrics) + "&domain=" + encodeURIComponent(domain);
			});
		});
	</script>
</a:config>