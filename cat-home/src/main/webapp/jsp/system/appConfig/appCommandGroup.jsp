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
		$('#appCommandGroup').addClass('active');
		
		$("#tab-group-all").addClass('active');
		$("#tabContent-group-0").addClass('active');
	})
</script>
		<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
		
		  <ul class="nav nav-tabs padding-12 ">
		  	<c:forEach var="entry" items="${model.commandGroupConfig.commands}" varStatus="status">
			    <li id="tab-group-${entry.key}" class="text-right"><a href="#tabContent-group-${status.index}" data-toggle="tab"> ${entry.key}</a></li>
			</c:forEach>
		  </ul>
		  <div class="tab-content">
		  	<c:forEach var="entry" items="${model.commandGroupConfig.commands}" varStatus="status">
			  	<div class="tab-pane" id="tabContent-group-${status.index}">
				    <table class="table table-striped table-condensed table-bordered table-hover">
					    <thead>
					    <tr>
							<th width="90%">命令字</th>
							<th width="10%" class="center"><a href="?op=appCommandGroupAdd&name=${entry.key}" class="btn btn-primary btn-xs" >
							<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
						</tr>
						</thead>
				    	<c:forEach var="command" items="${entry.value.subCommands}">
					    	<tr><td>${command.key}</td>
								<td  class="center">
									<a href="?op=appCommandGroupDelete&parent=${entry.key}&name=${command.key}&type=group" class="btn btn-danger btn-xs delete" >
									<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
							</tr>
				    	</c:forEach>
				    </table>
			    </div>
			</c:forEach>
		  </div>
		</div>
</a:mobile>