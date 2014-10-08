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
		function drawMetricItems(metricsStr) {
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
	                    $("#add-metric-button").trigger("click");
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
				$('#networkRuleConfigList').addClass('active');
				
				var configHeader = '${model.configHeader}';
				drawMetricItems(configHeader);
				
				$(document).delegate("#ruleSubmitButton","click",function(){
					var metrics = generateMetricsJsonString();
					var key = $('#ruleId').val();
					var configStr = generateConfigsJsonString();
				    window.location.href = "?op=networkRuleSubmit&configs=" + configStr + "&ruleId=" + key +"&metrics="+metrics;
				});
				
				$("#add-metric-button").click(function () {
		            var newMetric = $('<div class="metric"> 网络设备：<textarea name="productlineText" class="productlineText " type=" text" placeholder="支持正则"></textarea> 指标：<textarea name="metricText" class="metricText" type=" text" placeholder="支持正则"></textarea> 监控类型： <label class="checkbox inline"> <input name="count" class="count" type="checkbox">count </label> <label class="checkbox inline"> <input name="sum" class="sum" type="checkbox">sum </label> <label class="checkbox inline"> <input name="avg" class="avg" type="checkbox">avg </label> <button class="btn btn-danger btn-small delete-metric-button" type="button"> <i class="icon-trash icon-white"></i> </button> </div>');
		            $("#metrics").append(newMetric);
		        });
				
				$("#metrics").delegate(".delete-metric-button", "click", function () {
		            $(this).parent().remove();
		        });
			});
	</script>

	<div class="row-fluid">
		<div class="span2">
			<%@include file="../configTree.jsp"%>
		</div>
		<div class="span10">
			</br>
			<form method="post">
				规则ID： <input id="ruleId" type="text" value="${model.id}" />
				<div id="metrics">
					<strong class="text-success">匹配对象：</strong> <input id="metricsStr"
						type="hidden"></> <br>
					<div id="metricItem" class="metric">
						网络设备：
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
						<button class="btn btn-danger btn-small delete-metric-button"
							type="button">
							<i class="icon-trash icon-white"></i>
						</button>
					</div>
				</div>
				<button class="btn btn-success btn-small" id="add-metric-button"
					type="button">
					添加匹配对象<i class="icon-plus icon-white"></i>
				</button>
				<br> ${model.content}
				<div style='text-align: center'>
					<input class="btn btn-primary" id="ruleSubmitButton" type="text"
						name="submit" value="提交">
					</button>
				</div>
			</form>
		</div>
	</div>
</a:body>