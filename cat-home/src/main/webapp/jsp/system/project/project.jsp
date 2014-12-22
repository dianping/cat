<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	
	<script type="text/javascript">
		$(document).ready(function() {
			$('#projects_config').addClass('active open');
			$('#projects').addClass('active');
			
			$.widget( "custom.catcomplete", $.ui.autocomplete, {
				_renderMenu: function( ul, items ) {
					var that = this,
					currentCategory = "";
					$.each( items, function( index, item ) {
						if ( item.category != currentCategory ) {
							ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
							currentCategory = item.category;
						}
						that._renderItemData( ul, item );
					});
				}
			});
			
			var data = [];
			<c:forEach var="project" items="${model.projects}">
						var item = {};
						item['label'] = '${project.domain}';
						item['category'] ='${project.department} - ${project.projectLine}';
						
						data.push(item);
			</c:forEach>
					
			$( "#search" ).catcomplete({
				delay: 0,
				source: data
			});
			
			$("#search_go").bind("click",function(e){
				var newUrl = '/cat/s/config?op=projects&domain='+$( "#search" ).val() +'&date=${model.date}';
				window.location.href = newUrl;
			});
			$('#wrap_search').submit(
				function(){
					var newUrl = '/cat/s/config?op=projects&domain='+$( "#search" ).val() +'&date=${model.date}';
					window.location.href = newUrl;
					return false;
				}		
			);
		});
	</script>
	
	<div class="navbar-header pull-left position" style="width:350px;MARGIN-LEFT:20%;MARGIN-TOP:5px;">
		<form id="wrap_search" style="margin-bottom:0px;">
			<div class="input-group">
			<c:if test="${not empty payload.project.domain}">
				<c:set var="domain" value="${payload.project.domain}"/>
			</c:if>
			<c:if test="${not empty payload.domain}">
				<c:set var="domain" value="${payload.domain}"/>
			</c:if>
			<c:if test="${empty domain}">
				<c:set var="domain" value="cat"/>
			</c:if>
				<input id="search" type="text" value="${domain}" class="search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off"/>
				<span class="input-group-btn">
					<button class="btn btn-sm btn-pink" type="button" id="search_go">
						Go!
					</button> 
				</span>
			</div>
		</form>
	</div>
	<br/>
	<br/>
	<br/>
	
	<div>
	<table class="table table-striped table-condensed table-bordered">
		<tr>
			<td width="20%">项目名称</td>
			<td>${model.project.domain}</td>
		</tr>
		<tr>
			<td width="20%">CMDB项目名称</td>
			<td>${model.project.cmdbDomain}</td>
		</tr>
		<tr>
			<td width="20%">所属部门</td>
			<td>${model.project.department}</td>
		</tr>
		<tr>
			<td width="20%">产品线</td>
			<td>${model.project.projectLine}</td>
		</tr>
		<tr>
			<td width="20%">负责人</td>
			<td>${model.project.owner}</td>
		</tr>
		<tr>
			<td width="20%">项目组邮件</td>
			<td>${model.project.email}</td>
		</tr>
		<tr>
			<td width="20%">项目组号码</td>
			<td>${model.project.phone}</td>
		</tr>
		<tr><td width="20%">操作</td>
		<td><a href="?op=update&projectId=${model.project.id}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=projectDelete&projectId=${model.project.id}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
		</tr>
	</table>
						
		</div>
</a:config>
