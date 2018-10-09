<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>

<a:mobile>
<script type="text/javascript">
	$(document).ready(function(){
		$('#userMonitor_config').addClass('active open');
		$('#appConstants').addClass('active');
		var type = "${payload.type}";
		
		if(typeof type != "undefined" && type.length > 0) {
			$('#tab-'+ type).addClass('active');
			$('#tabContent-'+ type).addClass('active');
		}else{
			$('#tab-版本').addClass('active');
			$('#tabContent-版本').addClass('active');
		}
	})
</script>
		<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
			  <ul class="nav nav-tabs padding-12 ">
			  	<c:forEach var="entry" items="${model.configItems}" varStatus="status">
				    <li id="tab-${entry.key}" class="text-right"><a href="#tabContent-${entry.key}" data-toggle="tab"> ${entry.key}</a></li>
				</c:forEach>
			  </ul>
			  <div class="tab-content">
			  	<c:forEach var="entry" items="${model.configItems}" varStatus="status">
				  	<div class="tab-pane" id="tabContent-${entry.key}">
					    <table class="table table-striped table-condensed table-bordered table-hover">
						    <thead><tr>
									<th>ID</th>
									<th>值</th>
									<c:if test="${entry.key eq '版本' or entry.key eq '来源'}">
										<th width="5%"><a href="?op=appConstantAdd&type=${entry.key}" class="btn btn-primary btn-xs" >
										<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
									</c:if>
									<c:if test="${entry.key eq 'APP类型'}">
									<th>desc</th>
								</c:if>
								</tr>
							</thead>
							
					    	<c:forEach var="e" items="${entry.value.items}">
						    	<tr><td>${e.value.id}</td>
								<td>${e.value.value}</td>
								<c:if test="${entry.key eq '版本' or entry.key eq '来源'}">
									<td><a href="?op=appConstantUpdate&id=${e.key}&type=${entry.key}" class="btn btn-primary btn-xs">
										<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
									</td>
								</c:if>
								<c:if test="${entry.key eq 'APP类型'}">
									<td>${e.value.des}</td>
								</c:if>
					    	</c:forEach>
					    </table>
				    </div>
				</c:forEach>
			  </div>
			</div>
</a:mobile>