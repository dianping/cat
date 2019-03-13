<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.server.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.server.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.server.Model" scope="request"/>

<a:serverBody>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />

			<form method="post">
				<h3 class="text-center text-success">修改网络监控规则</h3>
				
				<div id="metrics">
					<div id="metricItem" class="metric config">
						EndPoint：
						<input id="endPoint" value="${model.serverAlarmRule.endPoint}" class="productlineText" type="text" placeholder="支持正则" class="col-xs-10 col-sm-5">
						指标：
						<input id="measurement" value="${model.serverAlarmRule.measurement}" class="metricText" type="text" placeholder="" class="col-xs-10 col-sm-5">
						标签：
						<input id="tags" value="${model.serverAlarmRule.tags}" class="metricText" type="text" placeholder="" class="col-xs-10 col-sm-5">
						监控类型：
						<select id="metricType" name="rule.type"> 
							<c:forEach var="item" items="${model.metricTypes}">
								<option value="${item.name}">${item.title}</option>
							</c:forEach>
						</select>
					</div>
				</div>
				${model.content}
				<div style='text-align: center'>
					<input class="btn btn-primary" id="ruleSubmitButton" type="text"
						name="submit" value="提交">
					</button>
				</div>
			</form>
	
	<script type="text/javascript">
		 $(document).ready(function() {
			initRuleConfigs(["DescVal","DescPer","AscVal","AscPer"]);
			$('#serverConfig').addClass('active open');
			$('#server_${payload.type}').addClass('active');
			
			if("${model.serverAlarmRule.type}" != ""){
				$("#metricType").val("${model.serverAlarmRule.type}");
			}
			
			$(document).delegate("#ruleSubmitButton","click",function(){
				var configStr = generateConfigsJsonString();
				console.log(configStr)
				var endPoint = $("#endPoint").val();
				var measurement = $("#measurement").val();
				var tags = $("#tags").val();
				var metricType = $("#metricType").val();
			    window.location.href = "?op=serverAlarmRuleSubmit&type=${payload.type}&rule.endPoint=" +
			   		 endPoint + "&rule.measurement="+ measurement + "&rule.tags="+ encodeURIComponent(tags) + "&rule.type=" + 
			   		 metricType + "&rule.id=${payload.ruleId}&content=" + configStr;
			});
			
			$("#metrics").delegate(".delete-metric-button", "click", function () {
	            $(this).parent().remove();
	        });
		});
	</script>
</a:serverBody>