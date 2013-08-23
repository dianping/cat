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
<!--
#content {
	width: 1200px;
	margin: 0 auto;
}
-->
</style>

<a:body>
<res:useCss value="${res.css.local['bootstrap-datetimepicker.min.css']}" target="head-css" />
<res:useCss value="${res.css.local['rickshaw.min.css']}" target="head-css" />
<res:useJs value="${res.js.local['bootstrap-datetimepicker.min.js']}" target="head-js" />
<res:useJs value="${res.js.local['rickshaw.min.js']}" target="head-js" />
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
		<div class="inline">
			<span class="label label-success">order</span>
			<span class="label label-info">payment.pending</span>
			<span class="label label-info">/order/submitOrder</span>
			<span class="label label-info">/detail</span>
			
			<div class="pull-right">
				<form action=""  class="form-inline">
					<div id="datetimepicker1" class="input-append date">
						<input name="startDate" value="${payload.startDateStr}" style="height: 30px;"
							placeholder="begin time" data-format="yyyy-MM-dd hh:mm" type="text"></input> 
						<span class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"> </i></span>
					</div>
					
					<div id="datetimepicker2" class="input-append date">
						<input name="endDate" value="${payload.endDateStr}" style="height: 30px;"
							placeholder="end time" data-format="yyyy-MM-dd hh:mm" type="text"></input> 
						<span class="add-on"> <i data-time-icon="icon-time" data-date-icon="icon-calendar"> </i></span>
					</div>
				
					<label class="radio"> <input type="radio" name="period"
						value="week" checked>week
					</label> <label class="radio"> <input type="radio" name="period"
						value="day">day
					</label> <label class="radio"> <input type="radio" name="period"
						value="hour">hour
					</label>
				</form>
			</div>
		</div>
		
		
		<div id="chart_container">
			<div id="chart"></div>
		</div>

		<table class="table">
			<thead>
				<tr>
					<th>Variation</th>
					<c:forEach var="goal" items="${model.report.variations[0].goals}">
						<th>${goal.name }</th>
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="item" items="${model.report.variations}">
					<tr>
						<td>${item.name}</td>
						<c:forEach var="goal" items="${item.goals}">
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
	var seriesData = [ [], [], [] ];
	var random = new Rickshaw.Fixtures.RandomData(150);

	for (var i = 0; i < 150; i++) {
		random.addData(seriesData);
	}

	// instantiate our graph!

	var graph = new Rickshaw.Graph( {
		element: document.getElementById("chart"),
		width: 960,
		height: 500,
		renderer: 'line',
		series: [
			{
				color: "#c05020",
				data: seriesData[0],
				name: 'New York'
			}, {
				color: "#30c020",
				data: seriesData[1],
				name: 'London'
			}, {
				color: "#6060c0",
				data: seriesData[2],
				name: 'Tokyo'
			}
		]
	} );


	
	$(function(){
		$('#datetimepicker1').datetimepicker();
		$('#datetimepicker2').datetimepicker();
	});
	
	</script>
</a:body>