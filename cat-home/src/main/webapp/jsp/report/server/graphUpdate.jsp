<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.server.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.server.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.server.Model" scope="request" />

<a:serverBody>
	<script src="${model.webapp}/assets/js/bootstrap-tag.min.js"></script>
	<h3 class="text-center text-success">编辑Screen[${model.metricScreenInfo.name}]配置</h3>
	<form name="graphSubmit" id="graphForm" method="post" action="${model.pageUri}?op=graphSubmit">
	<table class="table table-striped table-condensed " id="content">
		<input type="hidden" name="op" value="graphSubmit" />
		<input type="hidden" name="graphParam.name" value="${payload.screen}" />
		<tr>
			<th width="10%">名字</th>
			<c:choose>
			<c:when test="${not empty model.metricScreenInfo.graphName}">
				<th><input type="text" id="domain" name="graphParam.graphName" value="${model.metricScreenInfo.graphName}" size="50" readonly/></th>
			</c:when>
			<c:otherwise>
				<th><input type="text" id="domain" name="graphParam.graphName" size="50"/></th>
			</c:otherwise>
			</c:choose>
		</tr>
		<tr>
			<td width="10%">endPoints</td>
			<td>
				<input type="text" name="graphParam.endPoints" class="tag" id="tag_endPoints" placeholder="Enter endPoints ..." />
            </td>
		</tr>
		<tr>
			<td width="10%">measurements</td>
			<td>
				<input type="text" name="graphParam.measurements" class="tag" id="tag_measurements" placeholder="Enter measurements ..." />
            </td>
		</tr>
		<tr>
			<td width="10%">视角</td>
			<td>
				<select id="viewGroup" name="graphParam.view" style="width: 200px">
				  <option value="endPoint">endPoint</option>
				  <option value="measurement">measurement</option>
				  <option value="">组合视角</option>
				</select>
			</td>
		</tr>
	</table>
	<input class='btn btn-primary btn-sm' style="MARGIN-LEFT:45%" value="提交" type="submit" name="submit" />
	</form>
</a:serverBody>

<script type="text/javascript">
$(document).ready(function() {
	$('#serverChart').addClass('active open');
	$('#serverScreens').addClass('active');
	
	if('${model.metricScreenInfo.view}'!=''){
		$('#viewGroup').val('${model.metricScreenInfo.view}');
	}
	
	var tag_input = $('#tag_endPoints');
	try{
		tag_input.tag(
		  {
			placeholder:tag_input.attr('placeholder'),
		  }
		)

		//programmatically add a new
		var $tag_obj = $('#tag_endPoints').data('tag');
		<c:forEach var="item" items="${model.metricScreenInfo.endPoints}" varStatus="status">
			$tag_obj.add("${item}");
		</c:forEach>
	}
	catch(e) {
		//display a textarea for old IE, because it doesn't support this plugin or another one I tried!
		tag_input.after('<textarea id="'+tag_input.attr('id')+'" name="'+tag_input.attr('name')+'" rows="3">'+tag_input.val()+'</textarea>').remove();
		//$('#form-field-tags').autosize({append: "\n"});
	}
	
	var tag_input = $('#tag_measurements');
	try{
		tag_input.tag(
		  {
			placeholder:tag_input.attr('placeholder'),
		  }
		)

		//programmatically add a new
		var $tag_obj = $('#tag_measurements').data('tag');
		<c:forEach var="item" items="${model.metricScreenInfo.measures}" varStatus="status">
			$tag_obj.add("${item}");
		</c:forEach>
	}
	catch(e) {
		//display a textarea for old IE, because it doesn't support this plugin or another one I tried!
		tag_input.after('<textarea id="'+tag_input.attr('id')+'" name="'+tag_input.attr('name')+'" rows="3">'+tag_input.val()+'</textarea>').remove();
		//$('#form-field-tags').autosize({append: "\n"});
	}
	
});
</script>
<style>
.tags {
	width:95%;
}
.tags 
</style>