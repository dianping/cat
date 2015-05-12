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
			
			if("${payload.action.name}" != 'projects') {
				var state = '${model.opState}';
				if(state=='Success'){
					$('#state').html('操作成功');
				}else{
					$('#state').html('操作失败');
				}
				setInterval(function(){
					$('#state').html('&nbsp;');
				},3000);
			}
			
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
						item['category'] ='${project.bu} - ${project.cmdbProductline}';
						
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
	<div class="navbar-header pull-left position" style="width:350px;MARGIN-LEFT:10%;MARGIN-TOP:5px;padding:5px;">
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
			<span class="input-icon" style="width:300px;">
				<input type="text" placeholder="input domain for search" value="${domain}" class="search-input search-input form-control ui-autocomplete-input" id="search" autocomplete="off" />
				<i class="ace-icon fa fa-search nav-search-icon"></i>
				</span>
				<span class="input-group-btn" style="width:50px">
				<button class="btn btn-sm btn-primary" type="button" id="search_go">
				Go
				</button>
				</span>
			</div>
		</form>
	</div>
	<br/>
	<br/>
	<br/>
	<div style="padding:5px;">
	<form name="projectUpdate" id="form" method="get" action="${model.pageUri}?op=updateSubmit">
	<table class="table table-striped table-condensed ">
		<input type="hidden" name="project.id" value="${model.project.id}" />
		<input type="hidden" name="project.domain" value="${model.project.domain}" />
		<input type="hidden" name="op" value="updateSubmit" />
		<tr>
			<td style="width:10%;">CAT项目名称</td>
			<td>${model.project.domain}</td>
			<td style="color:red">注意：建议使用统一项目名规范。</td>
		</tr>
		<tr>
			<td style="width:10%;">CMDB项目名称</td>
			<td><input type="name" class="input-xlarge" name="project.cmdbDomain" value="${model.project.cmdbDomain}" /></td>
			<td>cmdb中项目统一名称</td>
		</tr>
		<tr>
			<td style="width:10%;">CMDB项目级别</td>
			<td><input type="name" class="input-xlarge" name="project.level" value="${model.project.level}" /></td>
			<td>cmdb中项目统一级别<span  style="color:red">【此字段会和CMDB信息同步】</span></td>
		</tr>
		<tr>
			<td style="width:10%;">CMDB事业部</td>
			<td><input type="name" class="input-xlarge" name="project.bu" value="${model.project.bu}" /></td>
			<td>cmdb中项目所属事业部名称<span style="color:red">【此字段会和CMDB信息同步】</span></td>
		</tr>
		<tr>
			<td style="width:10%;">CMDB产品线</td>
			<td><input type="name" class="input-xlarge" name="project.cmdbProductline" value="${model.project.cmdbProductline}" /></td>
			<td>cmdb中项目所属产品线名称<span style="color:red">【此字段会和CMDB信息同步】</span></td>
		</tr>
		<tr>
			<td style="width:10%;">负责人</td>
			<td><input type="name" class="input-xlarge" name="project.owner" value="${model.project.owner}"/></td>
			<td>项目负责人<span style="color:red">【此字段会和CMDB信息同步】</span></td>
		</tr>
		<tr>
			<td style="width:10%;">项目组邮件</td>
			<td><input type="name" name="project.email" class="input-xxlarge" value="${model.project.email}"/></td>
			<td>字段(多个，逗号分割)<span  style="color:red">【此字段会和CMDB信息同步】</span></td>
		</tr>
		<tr>
			<td>项目组号码</td>
			<td><input type="name" name="project.phone" class="input-xxlarge" value="${model.project.phone}"/></td>
			<td>字段(多个，逗号分割)<span  style="color:red">【此字段会和CMDB信息同步】</span></td>
		</tr>
		<tr>
			<td colspan="2" align="center"><input class='btn btn-primary btn-sm' type="submit" name="submit" value="提交" />&nbsp;
			<a href="?op=projectDelete&projectId=${model.project.id}" class="btn btn-danger btn-sm delete" >
						<i class="ace-icon fa fa-trash-o bigger-140"></i></a>
						<h4 class="text-center text-danger" id="state">&nbsp;</h4></td>
		</tr>
	</table>
</form>

</div>
</a:config>
<style>
.input-icon>.ace-icon {
	z-index: 0;
}
</style>
