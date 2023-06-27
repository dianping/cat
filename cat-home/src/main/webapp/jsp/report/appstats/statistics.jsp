<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld" %>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.appstats.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.appstats.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.appstats.Model" scope="request"/>

<a:mobile>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<style>
		.input-group {
			margin: 7px 7px;
			height: 34px;
		}

		select {
			height: 34px;
		}

		.tags {
			height: 34px;
		}
	</style>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<script src="${model.webapp}/assets/js/bootstrap-tag.min.js"></script>
	<res:useCss value='${res.css.local.table_css}' target="head-css"/>
	<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js"/>
	<div id="queryBar">
		<table style="margin-bottom: 7px">
			<tr>
				<td>
					<div class="input-group" style="float:left;width:130px">
						<span class="input-group-addon">APP</span>
						<select id="app" style="width:130px">
							<c:forEach var="item" items="${model.constantsItem.items}">
								<option value="${item.key}">${item.value.value}</option>
							</c:forEach>
						</select>
					</div>
					<div class="input-group" style="float:left;width:130px">
						<span class="input-group-addon">日期</span>
						<input type="text" id="time" style="width:100px;height: 34px"
							   value="<fmt:formatDate value='${payload.dayDate}' pattern='yyyy-MM-dd'/>"/>
					</div>
					<div class="input-group" style="float:left;width:130px">
						<span class="input-group-addon">返回码分布</span>
						<input type="text" name="codes" style="width:100px;height: 34px" class="tag" id="tag_codes"
							   placeholder="输入返回码，默认所有XXX"/>
					</div>
					<div class="input-group" style="float:left;width:130px">
						<span class="input-group-addon">返回码TOP</span>
						<input type="text" name="top" style="width:50px;;height: 34px" value="${payload.top}" id="top"
							   placeholder="Enter top ... 默认20"/>
					</div>
					<input class="btn btn-primary btn-sm" style="margin: 7px 7px;height: 34px"
						   value="查询" onclick="queryNew()" type="submit">
				</td>
			</tr>
		</table>
	</div>
	<%@ include file="statisticsDetail.jsp" %>
	<script type="text/javascript">
		$(document).ready(function () {
			$('[data-rel=tooltip]').tooltip();
			$('#App_report').addClass("active open");
			$('#statistics').addClass("active");
			$('#time').datetimepicker({
				format: 'Y-m-d',
				timepicker: false,
				maxDate: 0
			});

			var type = '${payload.type}';
			if (type == 'request' || type.length == 0) {
				type = 'all';
			}
			$("#li-" + type).addClass("active");
			$("#tabContent-" + type).addClass("active");

			$('#app').val("${payload.appId}");

			var tag_input = $('#tag_codes');
			try {
				tag_input.tag(
					{
						placeholder: tag_input.attr('placeholder'),
					}
				)

				//programmatically add a new
				var $tag_obj = $('#tag_codes').data('tag');
				<c:forEach var="item" items="${payload.codes}" varStatus="status">
				$tag_obj.add("${item}");
				</c:forEach>
			} catch (e) {
				//display a textarea for old IE, because it doesn't support this plugin or another one I tried!
				tag_input.after('<textarea id="' + tag_input.attr('id') + '" name="' + tag_input.attr('name') + '" rows="3">' + tag_input.val() + '</textarea>').remove();
				//$('#form-field-tags').autosize({append: "\n"});
			}

			<c:forEach var="entry" items="${model.piecharts}" varStatus="status">
			graphPieChartWithName(document.getElementById('piechart_${entry.key}'), ${entry.value.jsonString}, '${entry.value.title}');
			</c:forEach>
		});

		function queryNew() {
			var app = $('#app').val();
			var time = $("#time").val();
			var codes = $('#tag_codes').val();
			var top = $('#top').val();

			window.location.href = "?appId=" + app + "&day=" + time + "&domain=${model.domain}&type=${payload.type}&codes=" + codes + "&top=" + top;
		}
	</script>
</a:mobile>
