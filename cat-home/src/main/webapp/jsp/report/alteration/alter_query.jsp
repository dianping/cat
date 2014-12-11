<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html; charset=utf-8"%>
	<div class="text-left"></div>
	<div id="startDatePicker" class="input-append  date" style="margin-bottom: 0px;float:left;">
        开始<input id="startTime" name="startTime"  value="<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm"/>"  size="16" class="{required:true,date:true}"
           data-format="yyyy-MM-dd HH:mm" type="text"></input> <span class="add-on">
           <i class="ace-icon fa fa-calendar"></i>
        </span>
     </div>
     <div id="endDatePicker" class="input-append  date" style="margin-bottom: 0px;float:left;">
        &nbsp;&nbsp;结束<input id="endTime" name="endTime"  value="<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm"/>"  size="16" class="{required:true,date:true}"
           data-format="yyyy-MM-dd HH:mm" type="text"></input> <span class="add-on">
           <i class="ace-icon fa fa-calendar"></i>
        </span>
     </div>
	应用名
	<input type="text" name="domain" id="domain" value="${payload.domain}" style="height:auto" class="input-small">
	机器名
	<input type="text" name="hostname" id="hostname" value="${payload.hostname}" style="height:auto" class="input-small"> 
	<input class="btn btn-primary  btn-sm"  value="查询" onclick="queryNew()" type="submit">
		
	类型
	<label class="btn btn-info btn-sm">
	<input type="checkbox" checked="checked" id="show_puppet" class="typeCheckbox"/>puppet 
	</label><label class="btn btn-info btn-sm">
	<input type="checkbox" checked="checked" id="show_workflow" class="typeCheckbox"/>workflow 
	</label><label class="btn btn-info btn-sm">
	<input type="checkbox" checked="checked" id="show_lazyman" class="typeCheckbox"/>lazyman 
	</label>
	<br>
	</div>

<script>
	function typeCheckStr(){
		var result = "";
		if(!document.getElementById("show_puppet").checked){
			result += "showPuppet=false&";
		}
		if(!document.getElementById("show_workflow").checked){
			result += "showWorkflow=false&";
		}
		if(!document.getElementById("show_lazyman").checked){
			result += "showLazyman=false&";
		}
		return result;
	}
	function queryNew(){
		var startTime=$("#startTime").val();
		var endTime=$("#endTime").val();
		var domain=$("#domain").val();
		var hostname=$("#hostname").val();
		window.location.href="?op=view&domain="+domain+"&startTime="+startTime+"&endTime="+endTime+"&hostname="+hostname;
	}
	function queryFullScreen(isFullScreen){
		var typeStatus = typeCheckStr();
		<c:if test="${payload.refresh}">
			window.location.href="?"+typeStatus+"domain=${payload.domain}&hostname=${payload.hostname}&fullScreen="+isFullScreen+"&refresh=${payload.refresh}&frequency=${payload.frequency}";
		</c:if>
		<c:if test="${!payload.refresh}">
			window.location.href="?"+typeStatus+"domain=${payload.domain}&hostname=${payload.hostname}&fullScreen="+isFullScreen+"&refresh=${payload.refresh}&frequency=${payload.frequency}&startTime=<fmt:formatDate value="${payload.startTime}" pattern="yyyy-MM-dd HH:mm"/>&endTime=<fmt:formatDate value="${payload.endTime}" pattern="yyyy-MM-dd HH:mm"/>";
		</c:if>
	}
	function queryFrequency(frequency){
		var typeStatus = typeCheckStr();
		window.location.href="?"+typeStatus+"domain=${payload.domain}&hostname=${payload.hostname}&fullScreen=${payload.fullScreen}&refresh=true&frequency="+frequency;
	}
</script>