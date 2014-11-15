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
			if(command['title'] != undefined && command['title'].length > 0){
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
			if(command['title'] != undefined){
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
		initDomain('domain', 'command', '${payload.domain}', '${payload.id}');
	})
</script>

项目<select id="domain" style="width: 100px;"></select>
	命令字 <select id="command" style="width: 240px;"></select>
	&nbsp;&nbsp;
	<input class="btn btn-primary "
					value="&nbsp;&nbsp;&nbsp;返回码&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" />
	<br/>
	<br/>
<table class="table table-striped table-bordered table-condensed table-hover" id="contents" width="100%">
	<thead>
	<tr class="odd">
		<th width="10%">返回码</th>
		<th width="20%">局部设置</th>
		<th width="20%">局部状态</th>
		<th width="15%">操作&nbsp;&nbsp;  <a class='btn btn-primary btn-small' href="?op=appCodeAdd&id=${payload.id}&type=code">新增</a></th>
	</tr></thead>
	
	<tbody>
	<c:forEach var="code" items="${model.updateCommand.codes}">
			<tr>
				<td>${code.value.id}</td>
				<td>${code.value.name}</td>
				<td>
				<c:choose>
				<c:when test="${code.value.status eq 0}">
					<span class="text-success">成功</span>
				</c:when>
				<c:otherwise>
					<span class="text-error">失败</span>
				</c:otherwise>
				</c:choose>
				</td>
				<td><a class='btn  btn-small btn-primary' href="?op=appCodeUpdate&id=${payload.id}&code=${code.value.id}&type=code">编辑</a>
				<a class='delete btn  btn-small btn-danger' href="?op=appCodeDelete&id=${payload.id}&code=${code.value.id}&type=code">删除</a></td>
			</tr>
	</c:forEach>
	
	<tr><td colspan="4"></td></tr>
	<thead>
	<tr class="odd">
		<th>返回码</th>
		<th>全局设置</th>
		<th colspan="2">全局状态</th>
	</tr></thead>
		<c:forEach var="code" items="${model.codes}">
			<tr>
				<td>${code.value.id}</td>
				<td>${code.value.name}</td>
				<td colspan="2">
				<c:choose>
				<c:when test="${code.value.status eq 0}">
					<span class="text-success">成功</span>
				</c:when>
				<c:otherwise>
					<span class="text-error">失败</span>
				</c:otherwise>
				</c:choose>
				</td>
			</tr>
	</c:forEach>
	</tbody>
</table>