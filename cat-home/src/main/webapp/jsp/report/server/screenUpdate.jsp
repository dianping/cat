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
	<h3 class="text-center text-success">
		<c:choose>
			<c:when test="${not empty payload.screen}">
				编辑Screen[${payload.screen}]配置
			</c:when>
			<c:otherwise>
				添加Screen
			</c:otherwise>
		</c:choose>
	</h3>
	<table class="table table-striped table-condensed " id="content">
		<input type="hidden" name="op" value="screenSubmit" />
		<tr>
			<th width="10%">名字</th>
			<c:choose>
			<c:when test="${not empty payload.screen}">
				<th><input type="text" id="screenName" value="${payload.screen}" size="50" readonly/></th>
			</c:when>
			<c:otherwise>
				<th><input type="text" id="screenName" value="" size="50"/></th>
			</c:otherwise>
			</c:choose>
		</tr>
		<tr>
			<td width="10%">graphs</td>
			<td>
				<input type="text" name="graphs" class="tag" id="tag_graphs" placeholder="Enter endPoints ..." />
            </td>
		</tr>
	</table>
	<input class='btn btn-primary btn-sm' style="MARGIN-LEFT:45%" type="button" value="提交" onclick="submit();"/>
</a:serverBody>

<script type="text/javascript">
function submit(){
	var screen = $('#screenName').val();
	console.log(screen)
	var graphs = $('#tag_graphs').val();
	window.location.href = "?op=screenSubmit&screen="+screen+"&graph="+graphs;
}

$(document).ready(function() {
	$('#serverConfig').addClass('active open');
	$('#serverScreens').addClass('active');
	
	var tag_input = $('#tag_graphs');
	try{
		tag_input.tag(
		  {
			placeholder:tag_input.attr('placeholder'),
		  }
		)

		//programmatically add a new
		var $tag_obj = tag_input.data('tag');
		<c:forEach var="item" items="${model.graphs}" varStatus="status">
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