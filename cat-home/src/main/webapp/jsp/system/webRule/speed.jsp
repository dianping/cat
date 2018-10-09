<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.web.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.web.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.web.Model" scope="request" />

<a:web_body>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#Web_config').addClass('active open');
			$('#speed').addClass('active');
			
			var page = '${payload.webPage}';
			if(page != null && page.length != 0) {
				$("#speeds").val(page);
			}
		//custom autocomplete (category selection)
		$.widget( "custom.catcomplete", $.ui.autocomplete, {
		_renderMenu: function( ul, items ) {
			var that = this,
			currentCategory = "";
			$.each( items, function( index, item ) {
				that._renderItemData( ul, item );
			});
		}
	});
		
		var data = [];
		<c:forEach var="speed" items="${model.speeds}">
			var item = {};
			item['label'] = '${speed.value.id}|${speed.key}';
			data.push(item);
		</c:forEach>
		
		$("#speeds").catcomplete({
			delay: 0,
			source: data
		});
		
	});	
	function query() {
		var speeds = $("#speeds").val();
		var href = "?op=speed&page=" + speeds;
		window.location.href = href;
	}
	</script>
	<table align="center">
	<tr><th>
	<div class="input-group" style="float:left;">
	<span class="input-group-addon">页面名称</span>
	<span class="input-icon" style="width:250px;">
	<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="speeds" autocomplete="on" data=""/>
	<i class="ace-icon fa fa-search nav-search-icon"></i>
	</span>
	</div>
	 <input class="btn btn-primary btn-sm"
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" />		
	<a href="?op=speedUpdate" class="btn btn-primary btn-sm"> <i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a>
	</th>
			</tr>
	</table>
	<br/>
	<c:if test="${null != model.speed}">
	<table class="table table-striped table-condensed table-bordered table-hover">
	<tr><td>页面ID </td><td>页面名称</td><td>操作</td>
	</tr>
	<tr>
			<td>${model.speed.id }</td>
			<td>${model.speed.page }</td>
			<td>
			<a href="?op=speedUpdate&page=${model.speed.page}" class="btn btn-primary btn-xs"> <i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
			<a href="?op=speedDelete&page=${model.speed.page}" class="btn btn-danger btn-xs delete"> <i class="ace-icon fa fa-trash-o bigger-120"></i></a>
			</td> </tr>
	</table>
	<table class="table table-striped table-condensed table-bordered table-hover">
		<thead>
			<tr>
				<th width="30%">测速点编号</th>
				<th width="32%">名称</th>
			</tr>
		</thead>
		<c:forEach var="step" items="${model.speed.steps}">
			<tr>
				<td>${step.key}</td>
				<td>${step.value.title}</td>
			</tr>
		</c:forEach>
	</table> </c:if>
</a:web_body>