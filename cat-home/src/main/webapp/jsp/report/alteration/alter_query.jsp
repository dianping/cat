<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html; charset=utf-8"%>
	<div class="text-left"></div>
     <div style="float:left;">
		&nbsp;开始
		<input type="text" id="startTime" style="width:150px;"/>
		结束
		<input type="text" id="endTime" style="width:150px;"/></div>
	应用名
	<input type="text" name="domain" id="domain" value="${payload.domain}" style="height:auto" class="input-small">
	机器名
	<input type="text" name="hostname" id="hostname" value="${payload.hostname}" style="height:auto" class="input-small"> 
	每分钟显示个数
	<input type="text" name="count" id="count" value="${payload.count}" style="height:auto" class="input-small"> 
	<input class="btn btn-primary  btn-sm"  style="margin-bottom:4px;" value="查询" onclick="queryNew()" type="submit">
	
	<br/>
	<div id="label-group">
		<label class="btn btn-info btn-sm">
		<input type="checkbox" style="margin-bottom:0px;" id="select-all-type" />All 
		</label><label class="btn btn-info btn-sm">
		<input type="checkbox" style="margin-bottom:0px;" class="altType" data-type="puppet"/>puppet 
		</label><label class="btn btn-info btn-sm">
		<input type="checkbox" style="margin-bottom:0px;" class="altType" data-type="workflow"/>workflow 
		</label><label class="btn btn-info btn-sm">
		<input type="checkbox" style="margin-bottom:0px;" class="altType" data-type="lazyman"/>lazyman 
		</label>	
	</div>
	<br>
	</div>

<script>
	function getAltTypeStr(){
		var result = "&altType=";
		
		$(".altType").filter(function(){
			return $(this).prop("checked");
		}).each(function(){
			var data = $(this).data("type");
			result += data + ",";
		});
		return result;
	}
	function queryNew(){
		var startTime=$("#startTime").val();
		var endTime=$("#endTime").val();
		var domain=$("#domain").val();
		var hostname=$("#hostname").val();
		var count=$("#count").val();
		window.location.href="?op=view&domain="+domain+"&startTime="+startTime+"&endTime="+endTime+"&hostname="+hostname+"&count="+count+getAltTypeStr();
	}
	$(document).ready(function(){
		var types = '${payload.altType}';
		
		if(types == null || types == ""){
			$(".altType").each(function(){
				$(this).prop("checked", true);
			});
		}else{
			var strs = types.split(",");
			
			for(var count in strs){
				var str = strs[count];
				if(str !=null && str !=""){
					$("[data-type='"+str+"']").prop("checked", true);
				}
			}
		}
		
		checkAllType();
		$("#select-all-type").click(dealAllType);
		$("#label-group").click(checkAllType);
	})
	function dealAllType(){
		var isAllButtonChecked = $("#select-all-type").prop("checked");
		
		$(".altType").each(function(){
			$(this).prop("checked", isAllButtonChecked);
		})
	}
	function checkAllType(){
		var isAllChecked = true;
		
		$(".altType").each(function(){
			if(!$(this).prop("checked")){
				isAllChecked = false;
			}
		});
		$("#select-all-type").prop("checked", isAllChecked);
	}
</script>