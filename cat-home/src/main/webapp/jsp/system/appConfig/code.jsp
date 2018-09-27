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
	function query(namespace){
		var domain = $("#domain-"+namespace).val();
		var command = $("#command-"+namespace).val();
		var href = "?op=appCodes&type=code&domain=" + domain + "&id=" + command + "&namespace=" + namespace;
		
		window.location.href = href;
	}
	
	var domain2CommandsJson = ${model.domain2CommandsJson};

	function changeDomain(domainId, commandId, domainInitVal, commandInitVal, namespace){
		if(domainInitVal == ""){
			domainInitVal = $("#"+domainId).val()
		}
		var commandSelect = $("#"+commandId);
		var commandsJson = domain2CommandsJson[namespace];
		
		if(typeof commandsJson != "undefined"){
			var commands = commandsJson.commands[domainInitVal];
			
			$("#"+domainId).val(domainInitVal);
			commandSelect.empty();
			for(var cou in commands){
				var command = commands[cou];
				if(command['title'] != undefined && command['title'].trim().length > 0){
					commandSelect.append($("<option value='"+command['id']+"'>"+command['title']+"</option>"));
				}else{
					commandSelect.append($("<option value='"+command['id']+"'>"+command['name']+"</option>"));
				}
			}
			if(commandInitVal != ''){
				commandSelect.val(commandInitVal);
			}
		}
	}
	
	function changeCommandByDomain(){
		var namespace = $(this).attr('namespace');
		var domain = $("#domain-"+namespace).val();
		var commandSelect = $("#command-"+namespace);
		var domain2Commands = domain2CommandsJson[namespace];
		
		if(typeof domain2Commands != "undefined") {
			var commands = domain2Commands.commands[domain];
			commandSelect.empty();
			
			for(var cou in commands){
				var command = commands[cou];
				if(command['title'] != undefined && command['title'].trim().length > 0){
					commandSelect.append($("<option value='"+command['id']+"'>"+command['title']+"</option>"));
				}else{
					commandSelect.append($("<option value='"+command['id']+"'>"+command['name']+"</option>"));
				}
			}
		}
	}
	
	function initDomain(domainSelectId, commandSelectId, domainInitVal, commandInitVal, namespace){
		var domainsSelect = $("#"+domainSelectId);
		var commands = domain2CommandsJson[namespace];
		
		if(typeof commands != "undefined"){
			for(var domain in commands.commands){
				domainsSelect.append($("<option value='"+domain+"'>"+domain+"</option>"))
			}
		}
		changeDomain(domainSelectId, commandSelectId, domainInitVal, commandInitVal, namespace);
		domainsSelect.on('change', changeCommandByDomain);
	}

	$(document).ready(function(){
		$('#userMonitor_config').addClass('active open');
		$('#appCodes').addClass('active');
		var domain = '${payload.domain}';
		var id = '${payload.id}';
		
		var namespace = "${payload.namespace}";
		
		if(namespace == ""){
			for (var ns in ${model.codesJson}) {
				namespace = ns;
				break;
			}
		}
		
		if(typeof namespace != "undefined" && namespace.length > 0) {
			$('#tab-'+ namespace).addClass('active');
			$('#tabContent-'+ namespace).addClass('active');
		}else{
			$('#tab-点评主APP').addClass('active');
			$('#tabContent-点评主APP').addClass('active');
		}
		
		<c:forEach var="item" items="${model.codes}">
			if("${payload.namespace}" == "${item.key}") {
				initDomain('domain-${item.key}', 'command-${item.key}', domain, id, '${item.key}');
			}else{
				initDomain('domain-${item.key}', 'command-${item.key}', "", "", '${item.key}');
			}
		</c:forEach>
	})
</script>

	<div class="tabbable" id="content"> <!-- Only required for left/right tabs -->
				<ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height:50px;" id="myTab">
					<c:forEach var="item" items="${model.codes}">
						<li id="tab-${item.key}" class="text-right"><a href="#tabContent-${item.key}" data-toggle="tab"> <strong>${item.key}</strong></a></li>
					</c:forEach>
				</ul>
				<div class="tab-content">
					<c:forEach var="item" items="${model.codes}">
					<div class="tab-pane" id="tabContent-${item.key}">
						<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
						项目<select id="domain-${item.key}" namespace="${item.key}" style="width: 100px;"></select>
							命令字 <select id="command-${item.key}" style="width: 240px;"></select>
							&nbsp;&nbsp;
							<input class="btn btn-primary btn-xs"
									value="&nbsp;&nbsp;&nbsp;返回码&nbsp;&nbsp;&nbsp;" onclick="query('${item.key}')" type="submit" />
							<br/>
							<br/>
						<table class="table table-striped table-condensed  table-bordered table-hover" id="contents" width="100%">
							<thead>
							<tr >
								<th width="20%" class="text-info">返回码</th>
								<th width="50%" class="text-info">局部设置</th>
								<th width="10%" class="text-info center">网络状态</th>
								<th width="10%" class="text-info center">业务状态</th>
								<th width="8%" class="text-info">操作 <a href="?op=appCodeAdd&id=${model.id eq '0' ? '1' : model.id}&domain=${payload.domain}&type=code&namespace=${item.key}" class="btn btn-primary btn-xs" >
												<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
							</tr></thead>
							
							<tbody>
							<c:if test="${model.updateCommand.namespace eq item.key}">
								<c:forEach var="code" items="${model.updateCommand.codes}">
										<tr>
											<td>${code.value.id}</td>
											<td>${code.value.name}</td>
											<td class="center">
											<c:choose>
											<c:when test="${code.value.networkStatus eq 0}">
												<button class="btn btn-xs btn-success">
												<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
												</button>
											</c:when>
											<c:otherwise>
												<i class="ace-icon glyphicon glyphicon-remove bigger-120"></i>
											</c:otherwise>
											</c:choose>
										</td>
										<td class="center">
											<c:choose>
											<c:when test="${code.value.businessStatus eq 0}">
												<button class="btn btn-xs btn-success">
												<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
												</button>
											</c:when>
											<c:otherwise>
												<i class="ace-icon glyphicon glyphicon-remove bigger-120"></i>
											</c:otherwise>
											</c:choose>
										</td>
											<td><a href="?op=appCodeUpdate&id=${model.id}&domain=${payload.domain}&code=${code.value.id}&type=code&namespace=${item.key }" class="btn btn-primary btn-xs">
													<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
													<a href="?op=appCodeDelete&id=${model.id}&domain=${payload.domain}&code=${code.value.id}&type=code&namespace=${item.key }" class="btn btn-danger btn-xs delete" >
													<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
										</tr>
								</c:forEach>
							</c:if>
							<tr><td colspan="4"></td></tr>
							<thead>
							<tr >
								<th width="20%" class="text-info">返回码</th>
								<th width="50%" class="text-info">全局设置</th>
								<th width="10%" class="text-info center">网络状态</th>
								<th width="10%" class="text-info center">业务状态</th>
								<th width="8%" class="text-info">操作 <a href="?op=appCodeAdd&id=${model.id eq '0' ? '1' : model.id}&domain=${payload.domain}&type=code&constant=true&namespace=${item.key}" class="btn btn-primary btn-xs" >
												<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
							</tr></thead>
								<c:forEach var="code" items="${item.value.codes}">
									<tr>
										<td>${code.value.id}</td>
										<td>${code.value.name}</td>
										<td class="center">
											<c:choose>
											<c:when test="${code.value.networkStatus eq 0}">
												<button class="btn btn-xs btn-success">
												<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
												</button>
											</c:when>
											<c:otherwise>
												<i class="ace-icon glyphicon glyphicon-remove bigger-120"></i>
											</c:otherwise>
											</c:choose>
										</td>
										<td class="center">
											<c:choose>
											<c:when test="${code.value.businessStatus eq 0}">
												<button class="btn btn-xs btn-success">
												<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
												</button>
											</c:when>
											<c:otherwise>
												<i class="ace-icon glyphicon glyphicon-remove bigger-120"></i>
											</c:otherwise>
											</c:choose>
										</td>
										<td><a href="?op=appCodeUpdate&domain=${payload.domain}&code=${code.value.id}&type=code&constant=true&namespace=${item.key}" class="btn btn-primary btn-xs">
												<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
												<a href="?op=appCodeDelete&domain=${payload.domain}&code=${code.value.id}&type=code&constant=true&namespace=${item.key}" class="btn btn-danger btn-xs delete" >
												<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
									</tr>
							</c:forEach>
							</tbody>
						</table>
						</div></div>
					</c:forEach>
				</div>
			</div>

</a:mobile>