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

			<form method="post">
				<div class="config">
				<strong class="text-success">规则ID</strong> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="ruleId" type="text" value="${model.id}" /> <span class="text-danger">String，唯一性</span>
				</div>
				<div id="metrics" class="config">
					<button class="btn btn-success btn-xs" id="add-metric-button"
						type="button">
						添加匹配对象<i class="icon-plus icon-white"></i>
					</button>
					<div id="metricItem" class="metric config">
						数据库：
						<textarea name="productlineText" class="productlineText "
							type=" text" placeholder="支持正则"></textarea>
						指标：
						<textarea name="metricText" class="metricText " type=" text"
							placeholder="支持正则"></textarea>
						监控类型： <label class="checkbox inline"> <input name="count"
							class="count" type="checkbox">count
						</label> <label class="checkbox inline"> <input name="sum"
							class="sum" type="checkbox">sum
						</label> <label class="checkbox inline"> <input name="avg"
							class="avg" type="checkbox">avg
						</label>
						<button class="btn btn-danger btn-xs delete-metric-button"
							type="button">
							<i class="ace-icon fa fa-trash-o bigger-120"></i>
						</button>
					</div>
				</div>
				${model.content}
				<div style='text-align: center'>
					<input class="btn btn-primary btn-xs" id="ruleSubmitButton" type="text"
						name="submit" value="提交">
					</button>
				</div>
			</form>
	
	<script type="text/javascript">
		function drawMetricItems(metricsStr, newMetric) {
	        if (metricsStr == undefined || metricsStr == "") {
	            return;
	        }
	        try {
	            metrics = JSON.parse(metricsStr);
	        } catch (e) {
	            alert("读取规则错误！请刷新重试或联系leon.li@dianping.com");
	            return;
	        }
	        if (metrics != undefined) {
	            for (count in metrics) {
	                var metric = metrics[count];
	                var productlineText = metric["productText"];
	                var metricText = metric["metricItemText"];

	                if (count > 0) {
	                	addMetricHeader(newMetric);
	                }
	                var metricForm = $(".metric").last();
	                if (productlineText) {
	                    metricForm.find(".productlineText").val(productlineText);
	                }
	                if (metricText) {
	                    metricForm.find(".metricText").val(metricText);
	                }
	                if (metric["monitorCount"]) {
	                    metricForm.find(".count").prop("checked", "true");
	                }
	                if (metric["monitorSum"]) {
	                    metricForm.find(".sum").prop("checked", "true");
	                }
	                if (metric["monitorAvg"]) {
	                    metricForm.find(".avg").prop("checked", "true");
	                }
	            }
	        }
	    }
		
		function addMetricHeader(newMetric){
			$("#metrics").append(newMetric.clone());
		}
		
		function generateMetricsJsonString() {
		        var metricLength = $(".metric").length;
		        if (metricLength > 0) {
		            var metricList = [];
		            $(".metric").each(function () {
		                var metric = {};
		                var hasPro = false;
		                var productLineText = $(this).find(".productlineText").val();
		                var metricText = $(this).find(".metricText").val()

		                if (productLineText != "") {
		                    metric["productText"] = productLineText;
		                    hasPro = true;
		                }
		                if (metricText != "") {
		                    metric["metricItemText"] = metricText;
		                    hasPro = true;
		                }
		                if ($(this).find($("input[name='count']")).prop("checked") == true) {
		                    metric["monitorCount"] = true;
		                    hasPro = true;
		                }
		                if ($(this).find($("input[name='sum']")).prop("checked") == true) {
		                    metric["monitorSum"] = true;
		                    hasPro = true;
		                }
		                if ($(this).find($("input[name='avg']")).prop("checked") == true) {
		                    metric["monitorAvg"] = true;
		                    hasPro = true;
		                }

		                if (hasPro) {
		                    metricList.push(metric);
		                }
		            });
		            if (metricList.length > 0) {
		                return JSON.stringify(metricList);
		            } else {
		                return "";
		            }
		        }
		    }
		 $(document).ready(function() {
			initRuleConfigs(["DescVal","DescPer","AscVal","AscPer"]);
			$('#alert_config').addClass('active open');
			$('#databaseRuleConfigList').addClass('active');
			var newMetric = $('#metricItem').clone();
			
			var configHeader = '${model.configHeader}';
			drawMetricItems(configHeader, newMetric);
			
			$(document).delegate("#ruleSubmitButton","click",function(){
				var key = $('#ruleId').val();
				var metrics = generateMetricsJsonString();
				var configStr = generateConfigsJsonString();
			    window.location.href = "?op=databaseRuleSubmit&configs=" + configStr + "&ruleId=" + key +"&metrics="+metrics;
			});
			
			$("#add-metric-button").click(function(){
				addMetricHeader(newMetric);
			});
			
			$("#metrics").delegate(".delete-metric-button", "click", function () {
	            $(this).parent().remove();
	        });
		});
	</script>
</a:config>