<%@ page contentType="text/html; charset=utf-8" %>

<script type="text/javascript">
	function query(){
		var domain = $("#domain").val();
		var command = $("#command").val();
		var href = "?op=appList&type=code&domain=" + domain + "&id=" + command;
		
		window.location.href = href;
	}
	
	var domain2CommandsJson = ${model.domain2CommandsJson};

	function changeDomain(domainId, commandId, domainInitVal, commandInitVal){
		if(domainInitVal == ""){
			domainInitVal = $("#"+domainId).val()
		}
		var commandSelect = $("#"+commandId);
		var commands = domain2CommandsJson[domainInitVal];
		
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
	
	function changeCommandByDomain(){
		var domain = $("#domain").val();
		var commandSelect = $("#command");
		var commands = domain2CommandsJson[domain];
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
	
	function initDomain(domainSelectId, commandSelectId, domainInitVal, commandInitVal){
		var domainsSelect = $("#"+domainSelectId);
		for(var domain in domain2CommandsJson){
			domainsSelect.append($("<option value='"+domain+"'>"+domain+"</option>"))
		}
		changeDomain(domainSelectId, commandSelectId, domainInitVal, commandInitVal);
		domainsSelect.on('change', changeCommandByDomain);
	}

	$(document).ready(function(){
		var domain = '';
		var id = '';
		if('${payload.type}' == 'code'){
			domain = '${payload.domain}';
			id = '${payload.id}';
		}
		initDomain('domain', 'command', domain, id);
	})
</script>

项目<select id="domain" style="width: 100px;"></select>
	命令字 <select id="command" style="width: 240px;"></select>
	&nbsp;&nbsp;
	<input class="btn btn-primary btn-xs"
					value="&nbsp;&nbsp;&nbsp;返回码&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" />
	<br/>
	<br/>
<table class="table table-striped table-condensed  table-bordered table-hover" id="contents" width="100%">
	<thead>
	<tr >
		<th width="20%" class="text-info">返回码</th>
		<th width="50%" class="text-info">局部设置</th>
		<th width="20%" class="text-info">局部状态</th>
		<th width="10%" class="text-info">操作 <a href="?op=appCodeAdd&id=${model.id eq '0' ? '1' : model.id}&domain=${payload.domain}&type=code" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
	</tr></thead>
	
	<tbody>
	<c:forEach var="code" items="${model.updateCommand.codes}">
			<tr>
				<td>${code.value.id}</td>
				<td>${code.value.name}</td>
				<td class="center">
					<c:choose>
					<c:when test="${code.value.status eq 0}">
						<button class="btn btn-xs btn-success">
						<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
						</button>
					</c:when>
					<c:otherwise>
						<i class="ace-icon glyphicon glyphicon-remove bigger-120"></i>
					</c:otherwise>
					</c:choose>
				</td>
				<td><a href="?op=appCodeUpdate&id=${model.id}&domain=${payload.domain}&code=${code.value.id}&type=code" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=appCodeDelete&id=${model.id}&domain=${payload.domain}&code=${code.value.id}&type=code" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
			</tr>
	</c:forEach>
	
	<tr><td colspan="4"></td></tr>
	<thead>
	<tr >
		<th width="20%" class="text-info">返回码</th>
		<th width="50%" class="text-info">全局设置</th>
		<th width="20%" class="text-info">全局状态</th>
		<th width="10%" class="text-info">操作 <a href="?op=appCodeAdd&id=${model.id eq '0' ? '1' : model.id}&domain=${payload.domain}&type=code&constant=true" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
	</tr></thead>
		<c:forEach var="code" items="${model.codes}">
			<tr>
				<td>${code.value.id}</td>
				<td>${code.value.name}</td>
				<td class="center">
					<c:choose>
					<c:when test="${code.value.status eq 0}">
						<button class="btn btn-xs btn-success">
						<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
						</button>
					</c:when>
					<c:otherwise>
						<i class="ace-icon glyphicon glyphicon-remove bigger-120"></i>
					</c:otherwise>
					</c:choose>
				</td>
				<td><a href="?op=appCodeUpdate&id=${model.id}&domain=${payload.domain}&code=${code.value.id}&type=code&constant=true" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=appCodeDelete&id=${model.id}&domain=${payload.domain}&code=${code.value.id}&type=code&constant=true" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
			</tr>
	</c:forEach>
	</tbody>
</table>