	<%@ page session="false" language="java" pageEncoding="UTF-8" %>
	<h4 class='text-error'>浏览器分布情况 </h4>
	<res:useJs value="${res.js.local['highcharts.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
	<div class="span6">
		<div id="browserGraph" class="pieChart"></div>
		<div id="osGraph" class="pieChart"></div>
		
		<script type="text/javascript">
			var browserData = ${model.browserChart};
			var osData = ${model.osChart};
			graphPieChart(document.getElementById('browserGraph'), browserData);
			graphPieChart(document.getElementById('osGraph'), osData);

		</script>
		
	</div>