<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<form name="metricConfigAddSumbit" id="form" method="post" action="${model.pageUri}?op=metricConfigAddSumbit">
	<span class="text-center text-error" id="state">&nbsp;</span>
	<input name="productLineName" value="${payload.productLineName}" type="hidden"/>
	<table class="table table-striped table-bordered table-condensed">
		<tr>
			<td width="10%" style="text-align:right" class="text-success" width="50%">项目名称</td>
			<td width="40%" >
				<c:if test="${not empty model.metricItemConfig.domain}">
					<input name="metricItemConfig.domain" value="${model.metricItemConfig.domain}" readonly required/>
				</c:if>
				<c:if test="${empty  model.metricItemConfig.domain}">
					<select style="width:200px;" name="metricItemConfig.domain" id="id">
						<c:forEach var="item" items="${model.productLineToDomains}">
	                        <option value="${item.key}">${item.key}</option> 							
						</c:forEach>
                 	 </select>
				</c:if>
			</td>
			<td width="25%"  style="text-align:right" class="text-success">类型</td>
			<td width="25%"  >
				<c:if test="${not empty model.metricItemConfig.domain}">
					<input name="metricItemConfig.type" value="${model.metricItemConfig.type}"  readonly required/>
				</c:if>
				<c:if test="${empty  model.metricItemConfig.type}">
					<select name="metricItemConfig.type">
						<option value="URL">URL</option>
						<option value="PigeonService">PigeonService</option>
					</select>
				</c:if>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success">MetricKey</td>
			<td >
				<c:if test="${not empty model.metricItemConfig.domain}">
					<input name="metricItemConfig.metricKey" value="${model.metricItemConfig.metricKey}" readonly required/>
				</c:if>
				<c:if test="${empty  model.metricItemConfig.type}">
					<input name="metricItemConfig.metricKey" value="${model.metricItemConfig.metricKey}" required/>
				</c:if>
			</td>
			<td  style="text-align:right" class="text-success">显示顺序（数字）</td>
			<td ><input  name="metricItemConfig.viewOrder" value="${model.metricItemConfig.viewOrder}" required/></td>
		</tr>
		<tr>
			<td  style="text-align:right" class="text-success">显示标题</td>
			<td ><input name="metricItemConfig.title" value="${model.metricItemConfig.title}" required/></td>
			<td style="text-align:right" class="text-success">是否告警</td>
			<td >
				<c:choose>
					<c:when test="${model.metricItemConfig.alarm}">
						<input type="radio" name="metricItemConfig.alarm" value="true" checked />是	
						<input type="radio" name="metricItemConfig.alarm" value="false" />否
					</c:when>
					<c:otherwise>
				    	<input type="radio" name="metricItemConfig.alarm" value="true" />是
						<input type="radio" name="metricItemConfig.alarm" value="false" checked />否
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success"  width="25%">显示次数曲线</td>
			<td  width="25%">
				<c:choose>
					<c:when test="${model.metricItemConfig.showCount}">
						<input type="radio" name="metricItemConfig.showCount" value="true" checked />是	
						<input type="radio" name="metricItemConfig.showCount" value="false" />否
					</c:when>
					<c:otherwise>
				    	<input type="radio" name="metricItemConfig.showCount" value="true" />是
						<input type="radio" name="metricItemConfig.showCount" value="false" checked />否
					</c:otherwise>
				</c:choose>
				<br><br>
				<select class="tags" id="countSelect" multiple="multiple">
					<c:forEach var="item" items="${model.tags}">
		            	<option value="${item}">${item}</option>						
					</c:forEach>
				</select>
				<button class="btn btn-success btn-small" id="addCountTag" type="button">
	                添加标签<i class="icon-plus icon-white"></i>
	            </button>
	            <button class="btn btn-danger btn-small" id="deleteCountTag" type="button">
		            删除<i class="icon-trash icon-white"></i>
		        </button>
	            <input name="countTags" type="hidden"/>
			</td>
			<td  width="25%" style="text-align:right" class="text-success">显示业务监控大盘</td>
			<td  width="25%">
				<c:choose>
					<c:when test="${model.metricItemConfig.showCountDashboard}">
						<input type="radio" name="metricItemConfig.showCountDashboard" value="true" checked />是	
						<input type="radio" name="metricItemConfig.showCountDashboard" value="false" />否
					</c:when>
					<c:otherwise>
				    	<input type="radio" name="metricItemConfig.showCountDashboard" value="true" />是
						<input type="radio" name="metricItemConfig.showCountDashboard" value="false" checked />否
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success"  width="25%">显示平均曲线</td>
			<td  width="25%">
				<c:choose>
					<c:when test="${model.metricItemConfig.showAvg}">
						<input type="radio" name="metricItemConfig.showAvg" value="true" checked />是	
						<input type="radio" name="metricItemConfig.showAvg" value="false" />否
					</c:when>
					<c:otherwise>
				    	<input type="radio" name="metricItemConfig.showAvg" value="true" />是
						<input type="radio" name="metricItemConfig.showAvg" value="false" checked />否
					</c:otherwise>
				</c:choose>
				<br><br>
				<select class="tags" id="avgSelect" multiple="multiple">
					<c:forEach var="item" items="${model.tags}">
		            	<option value="${item}">${item}</option>						
					</c:forEach>
				</select>
				<button class="btn btn-success btn-small" id="addAvgTag" type="button">
	                添加标签<i class="icon-plus icon-white"></i>
	            </button>
	            <button class="btn btn-danger btn-small" id="deleteAvgTag" type="button">
		            删除<i class="icon-trash icon-white"></i>
		        </button>
	            <input name="avgTags" type="hidden"/>
			</td>
			<td style="text-align:right" class="text-success"  width="25%">显示业务监控大盘</td>
			<td  width="25%">
				<c:choose>
					<c:when test="${model.metricItemConfig.showAvgDashboard}">
						<input type="radio" name="metricItemConfig.showAvgDashboard" value="true" checked />是	
						<input type="radio" name="metricItemConfig.showAvgDashboard" value="false" />否
					</c:when>
					<c:otherwise>
				    	<input type="radio" name="metricItemConfig.showAvgDashboard" value="true" />是
						<input type="radio" name="metricItemConfig.showAvgDashboard" value="false" checked />否
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success" width="25%">显示求和曲线</td>
			<td  width="25%">
				<c:choose>
					<c:when test="${model.metricItemConfig.showSum}">
						<input type="radio" name="metricItemConfig.showSum" value="true" checked />是	
						<input type="radio" name="metricItemConfig.showSum" value="false" />否
					</c:when>
					<c:otherwise>
				    	<input type="radio" name="metricItemConfig.showSum" value="true" />是
						<input type="radio" name="metricItemConfig.showSum" value="false" checked />否
					</c:otherwise>
				</c:choose>
				<br><br>
				<select class="tags" id="sumSelect" multiple="multiple">
					<c:forEach var="item" items="${model.tags}">
		            	<option value="${item}">${item}</option>						
					</c:forEach>
				</select>
				<button class="btn btn-success btn-small" id="addSumTag" type="button">
	                添加标签<i class="icon-plus icon-white"></i>
	            </button>
	            <button class="btn btn-danger btn-small" id="deleteSumTag" type="button">
		            删除<i class="icon-trash icon-white"></i>
		        </button>
	            <input name="sumTags" type="hidden"/>
			</td>
			<td style="text-align:right" class="text-success"  width="25%">显示业务监控大盘</td>
			<td  width="25%">
				<c:choose>
					<c:when test="${model.metricItemConfig.showSumDashboard}">
						<input type="radio" name="metricItemConfig.showSumDashboard" value="true" checked />是	
						<input type="radio" name="metricItemConfig.showSumDashboard" value="false" />否
					</c:when>
					<c:otherwise>
				    	<input type="radio" name="metricItemConfig.showSumDashboard" value="true" />是
						<input type="radio" name="metricItemConfig.showSumDashboard" value="false" checked />否
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<td style="text-align:right" class="text-success">业务监控大盘顺序</td>
			<td><input class="input-mini" name="metricItemConfig.showDashboardOrder" value="${model.metricItemConfig.showDashboardOrder}" required/></td>
		</tr>
		<tr>
			<td style="text-align:center" colspan='4'><input class='btn btn-primary' id="addOrUpdateNodeSubmit" type="submit" name="submit" value="提交" /></td>
		</tr>
	</table>
</form>
<script>
	$(document).ready(function(){
		var tagName, tagType;
		<c:forEach var="tag" items="${model.metricItemConfig.tags}">
			tagName = '${tag.name}';
			tagType = '${tag.type}'.toLowerCase();
			$("#"+tagType+"Select").children().each(function(){
				if($(this).text() == tagName){
					$(this).prop("selected","selected")
				}
			});
		</c:forEach>
		
		$('select').multipleSelect({
			width: 100,
            multiple: true,
            multipleWidth: 100,
            selectAll: false
		});
		
		$("#form").submit(function(){
			var countTagStr;
			var countSelect = $("#countSelect");
			if(countSelect.length > 0){
				countTagStr = countSelect.multipleSelect('getSelects', 'text');
			}else{
				countTagStr = $("#countInput").val();
			}
			$("input[name='countTags']").val(countTagStr);
			
			var avgTagStr;
			var avgSelect = $("#avgSelect");
			if(avgSelect.length > 0){
				avgTagStr = avgSelect.multipleSelect('getSelects', 'text');
			}else{
				avgTagStr = $("#avgInput").val();
			}
			$("input[name='avgTags']").val(avgTagStr);
			
			var sumTagStr;
			var sumSelect = $("#sumSelect");
			if(sumSelect.length > 0){
				sumTagStr = sumSelect.multipleSelect('getSelects', 'text');
			}else{
				sumTagStr = $("#sumInput").val();
			}
			$("input[name='sumTags']").val(sumTagStr);
			
			console.log("count:"+countTagStr)
			console.log("avg:"+avgTagStr)
			console.log("sum:"+sumTagStr)
			return true;
		})
		
		$("#addCountTag").click(function(){
			$(this).parent().find(".tags").remove();
			if($(this).parent().find(".tagsInput").length == 0){
				$(this).before($('<input class="tagsInput span5" id="countInput" type="text"></input>'));
			}
			$(this).addClass("disabled")
		})
		
		$("#addSumTag").click(function(){
			$(this).parent().find(".tags").remove();
			if($(this).parent().find(".tagsInput").length == 0){
				$(this).before($('<input class="tagsInput span5" id="sumInput" type="text"></input>'));
			}
			$(this).addClass("disabled")
		})
		
		$("#addAvgTag").click(function(){
			$(this).parent().find(".tags").remove();
			if($(this).parent().find(".tagsInput").length == 0){
				$(this).before($('<input class="tagsInput span5" id="avgInput" type="text"></input>'));
			}
			$(this).addClass("disabled")
		})
		
		$("#deleteCountTag").click(function(){
			var addButton = $(this).prev();
			$(this).parent().find(".tags").remove();
			$(this).parent().find(".tagsInput").remove();
			addButton.before($('<input class="tagsInput span5" id="countInput" type="text" disabled></input>'));
			addButton.addClass("disabled");
			$(this).addClass("disabled");
		})
		
		$("#deleteAvgTag").click(function(){
			var addButton = $(this).prev();
			$(this).parent().find(".tags").remove();
			$(this).parent().find(".tagsInput").remove();
			addButton.before($('<input class="tagsInput span5" id="avgInput" type="text" disabled></input>'));
			addButton.addClass("disabled");
			$(this).addClass("disabled");
		})
		
		$("#deleteSumTag").click(function(){
			var addButton = $(this).prev();
			$(this).parent().find(".tags").remove();
			$(this).parent().find(".tagsInput").remove();
			addButton.before($('<input class="tagsInput span5" id="sumInput" type="text" disabled></input>'));
			addButton.addClass("disabled");
			$(this).addClass("disabled");
		})
	})
</script>