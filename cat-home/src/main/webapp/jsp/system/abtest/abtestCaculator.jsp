<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

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

div.controls input {
	height: 30px;
}
</style>
<a:body>
	<div id="content" class="row-fluid">
		<div class="span12 column">
			<h3>A/B Test Caculator</h3>
		</div>
	</div>
	<div style="width: 950px; margin: 0 auto; margin-bottom: 250px;">
		<form method="post" class="form-horizontal"
			action="abtest?op=caculator">
			<div class="control-group">
				<label class="control-label">Current Conversion Rate</label>
				<div class="controls input-append" style="margin-left: 20px;">
					<input type="text" name="conversionRate" placeholder="5"
						class="input-small" style="width: 40px;"
						value="${payload.conversionRate}"> <span class="add-on">%</span>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">Desired Confidence Level</label>
				<div class="controls input-append" style="margin-left: 20px;">
					<label class="control-label" style="width: 40px;">95%</label>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">Current PV/Day</label>
				<div class="controls">
					<input type="text" name="pv" placeholder="1000" class="input-small"
						value="${payload.pv}">
				</div>
			</div>
			<div class="control-group">
				<label class="control-label"></label>
				<div class="controls input-append" style="margin-left: 10px;">
					<button class="btn btn-primary" type="submit">Caculate</button>
				</div>
			</div>
		</form>
		<div>
			<c:if test="${not empty ctx.advice }">
				<table class="table">
					<thead>
						<tr>
							<td>"A" Conversion Rate</td>
							<td>"B" Conversion Rate</td>
							<td>Difference</td>
							<td>Size Per Group</td>
							<td>Total Participants</td>
							<td>Confidence Level</td>
							<td>Days</td>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="item" items="${ctx.advice}">
							<tr>
								<td><fmt:formatNumber type="number"
										value="${item.ctrOfVariationA }" maxFractionDigits="2" /></td>
								<td><fmt:formatNumber type="number"
										value="${item.ctrOfVariationB }" maxFractionDigits="2" /></td>
								<td>${item.difference }</td>
								<td>${item.sizePerGroup }</td>
								<td>${item.totalParticipants }</td>
								<td>${item.confidenceInterval }</td>
								<td>${item.days }</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</c:if>
		</div>
	</div>

</a:body>