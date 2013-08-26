<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

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
						<%-- <img style="vertical-align: text-bottom;" height="15" width="15" src="${res.img.local['star_black_small.png']}">
						Summary
					</a>
				</li>
				<li>
					<a href="#detail"> --%> <img style="vertical-align: text-bottom;"
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
		
		<form class="form-inline" id="form">
			<c:forEach var="item" items="${model.report.goals}">
				<a class="btn btn-small ${payload.selectMetricType eq item.name ? 'btn-primary' : ''}">${item.name}</a>
			</c:forEach>
			<input id="metricType" name="selectMetricType" value="" type="hidden" ></input>
			<input name="op" value="report" type="hidden" ></input>
			<input name="id" value="${payload.id }" type="hidden" ></input>
			<div class="pull-right">
				<div id="datetimepicker1" class="input-append date">
					<input name="startDate" value="${w:format(payload.startDate,'yyyy-MM-dd HH:mm')}" style="height: 30px;"
						placeholder="begin time" data-format="yyyy-MM-dd hh:mm" type="text"></input> 
					<span class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"> </i></span>
				</div>
				
				<div id="datetimepicker2" class="input-append date">
					<input name="endDate" value="${w:format(payload.endDate,'yyyy-MM-dd HH:mm')}" style="height: 30px;"
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
		<div style="margin-bottom:40px;margin-left:40px;">
			<span class="label label-success">&nbsp;&nbsp;&nbsp;</span>&nbsp;Control&nbsp;&nbsp;&nbsp;
			<span class="label label-info">&nbsp;&nbsp;&nbsp;</span>&nbsp;A&nbsp;&nbsp;&nbsp;
			<span class="label label-important">&nbsp;&nbsp;&nbsp;</span>&nbsp;B&nbsp;&nbsp;&nbsp;
		</div>

		<table class="table">
			<thead>
				<tr>
					<th>Variation</th>
					<c:forEach var="goal" items="${model.report.goals}">
						<th>${goal.name }</th>
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
								<td>${goal.count }</td>
							</c:if>
							<c:if test="${goal.type eq 'S' }">
								<td>${goal.sum }</td>
							</c:if> 
						</c:forEach>
					</tr>
				</c:forEach>
			</tbody>

		</table>
	</div>
	
	<script type="text/javascript" >
	$(function(){
		var lineChartData = {
				labels : ${model.report.chart.labels},
				datasets : ${model.report.chart.datasets}

			}
		
		var myLine = new Chart(document.getElementById("canvas").getContext("2d")).Line(lineChartData);
		
		$('#datetimepicker1').datetimepicker();
		$('#datetimepicker2').datetimepicker();
		
		$('.form-inline a').each(function(){
			$(this).click(function(){
				$('#metricType').val($(this).text());
				$('#form').submit();
			});
		});
	});
	
	</script>
</a:body>