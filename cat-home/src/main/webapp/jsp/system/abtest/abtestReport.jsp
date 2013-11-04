<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.abtest.Context"
	scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.system.page.abtest.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model"
	scope="request" />

<style>
#content {
	width: 1200px;
	margin: 0 auto;
}
canvas{}

</style>

<a:body>
<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
<res:useJs value="${res.js.local['Chart.min.js']}" target="head-js" />
	<div id="content" class="row-fluid">
		<div class="span12 column">
			<h3>
				Report <small>${model.abtest.name} #${model.abtest.id}</small>
			</h3>
			<ul class="nav nav-tabs">
				<li class="active"><a href="?op=report&id=${payload.id }">
					 <img  class="img-polaroid"  style="vertical-align: text-bottom;"
						height="15" width="15"
						src="${res.img.local['details_black_small.png']}"> Detail
						Report
				</a></li>
				<li><a href="?op=detail&id=${payload.id}"> <img
						style="vertical-align: text-bottom;" height="15" width="15"
						src="${res.img.local['settings_black_small.png']}"> View/
						Edit ABTest Details
				</a></li>
			</ul>
		</div>
		
		<form class="form-inline" id="form" method="post" action="abtest?op=report">
			<c:forEach var="item" items="${model.report.goals}">
				<a class="btn btn-small ${payload.selectMetricType eq item.name ? 'btn-primary' : ''}" name="${item.name}">${model.metricConfigItem[item.name].title}</a>
			</c:forEach>
			
			<c:if test="${fn:length(model.report.goals) eq 0}">
				<input type="submit" class="btn btn-small" value="查询">
			</c:if>
			
			<input id="metricType" name="selectMetricType" value="" type="hidden" ></input>
			<input name="op" value="report" type="hidden" ></input>
			<input name="id" value="${payload.id }" type="hidden" ></input>
			<div class="pull-right">
				<div id="datetimepicker1" class="input-append date">
					<input name="startDate" value="${w:format(payload.startDate,'yyyy-MM-dd hh:mm')}" style="height: 30px;"
						placeholder="begin time" data-format="yyyy-MM-dd hh:mm" type="text"></input> 
					<span class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"> </i></span>
				</div>
				
				<div id="datetimepicker2" class="input-append date">
					<input name="endDate" value="${w:format(payload.endDate,'yyyy-MM-dd hh:mm')}" style="height: 30px;"
						placeholder="end time" data-format="yyyy-MM-dd hh:mm" type="text"></input> 
					<span class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"> </i></span>
				</div>
				
				<label class="radio"> <input type="radio" name="period"
					value="day" ${model.report.chart.type eq 'day' ? 'checked' : ''}>day
				</label> <label class="radio"> <input type="radio" name="period"
					value="hour" ${model.report.chart.type eq 'hour' ? 'checked' : ''}>hour
				</label>
			</div>
		</form>
		
		<canvas id="canvas" width="1200" height="400" style="margin-top:20px;margin-bottom:20px;"></canvas>
		<div id="variationDiv" style="margin-bottom:40px;margin-left:40px;">
			<c:set var="i" value="0"></c:set>
			<c:forEach var="item" items="${model.report.variations}">
				<a index="${i }" data-background-color="${model.dataSets[i].pointStrokeColor}" data-selected="1"><span class="label" style="background-color:${model.dataSets[i].pointStrokeColor};">&nbsp;&nbsp;&nbsp;</span></a>&nbsp;${item.key} &nbsp;&nbsp;&nbsp;
				<c:set var="i" value="${i+1}"></c:set>
			</c:forEach>
		</div>

		<table class="table table-hover">
			<thead>
				<tr>
					<th>Variation</th>
					<c:forEach var="goal" items="${model.report.goals}">
						<th>${model.metricConfigItem[goal.name].title}</th>
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="item" items="${model.report.variations}">
					<tr>
						<td>${item.key}</td>
						<c:forEach var="key" items="${model.report.goals}">
							<c:set var="goal" value="${item.value.goals[key.name]}"></c:set>
							
							<c:if test="${goal.type eq 'C' }">
								<td>${w:format(goal.count,'#,###,###,###,##0.#')}</td>
							</c:if>
							<c:if test="${goal.type eq 'S' }">
								<td>${w:format(goal.sum,'#,###,###,###,##0.#')}</td>
							</c:if> 
						</c:forEach>
					</tr>
				</c:forEach>
			</tbody>

		</table>
	</div>
	
	<script type="text/javascript" >
	$(function(){
		chart = new Chart(document.getElementById("canvas").getContext("2d"));
		datasets = ${model.report.chart.datasets};
		labels = ${model.report.chart.labels};
		
		var lineChartData = {
				'labels' : labels,
				'datasets' : datasets
			};
		
		chart.Line(lineChartData);
		
		$('#datetimepicker1').datetimepicker();
		
		$('#datetimepicker2').datetimepicker();
		
		$('.form-inline a').each(function(){
			$(this).click(function(){
				$('#metricType').val($(this).attr("name"));
				$('#form').submit();
			});
		});
		
		$('#variationDiv a').each(function(){
			$(this).click(function(){
				if($(this).data("selected") == 1){
					$(this).data("selected",0);
					$('span',$(this)).css("background-color", "#999999");
				}else{
					var color = $(this).data("background-color");
					$(this).data("selected",1);
					$('span',$(this)).css("background-color",color);
				}
				
				var clonedDatasets = datasets.slice(0);
				var count = 0;
				$('#variationDiv a').each(function(){
					var selected = $(this).data("selected");
					
					if(selected == 0){
						var index = $(this).attr("index") - count;
						clonedDatasets.splice(index,1);
						count += 1;
					}
				});
				
				var data = {
					'labels' : labels,
					'datasets' : clonedDatasets
				};
				
				chart.Line(data);
			});
		});
	});
	</script>
</a:body>