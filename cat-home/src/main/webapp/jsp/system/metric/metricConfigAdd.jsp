<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<a:config>
	<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<res:useJs value="${res.js.local['dependencyConfig_js']}" target="head-js" />
	<res:useCss value="${res.css.local['select2.css']}" target="head-css" />
	<res:useJs value="${res.js.local['select2.min.js']}" target="head-js" />
	<res:useCss value="${res.css.local['multiple-select.css']}" target="head-css" />
	<res:useJs value="${res.js.local['jquery.multiple.select.js']}" target="head-js" />
	
		<h4 class="text-success text-center">修改业务监控规则</h4>
		<form name="metricConfigAddSubmit" id="form" method="post" action="${model.pageUri}?op=metricConfigAddSubmit">
			<span class="text-center text-danger" id="state">&nbsp;</span>
			<input name="productLineName" value="${payload.productLineName}" type="hidden"/>
			<table class="table table-striped table-condensed  ">
				<tr>
					<td width="15%" style="text-align:right" class="text-success" width="50%">项目名称</td>
					<td width="35%" >
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
					<td width="20%"  style="text-align:right" class="text-success">类型</td>
					<td width="30%"  >
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
					<td  style="text-align:right" class="text-success">产品线内显示顺序（数字）</td>
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
					<td style="text-align:right" class="text-success" >显示次数曲线</td>
					<td colspan='3'>
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
						<select class="tags" id="countSelect" multiple="multiple">
							<c:choose> 
      							<c:when test="${empty model.tags}"> 
      								<option value="业务大盘">业务大盘</option>	
      							</c:when>
      							<c:otherwise>
      								<c:forEach var="item" items="${model.tags}">
						            	<option value="${item}">${item}</option>						
									</c:forEach>
      							</c:otherwise>
      						</c:choose>
						</select>
						<button class="btn btn-success btn-xs" id="addCountTag" type="button">
			                添加其他标签<i class="icon-plus icon-white"></i>
			            </button>
			            <button class="btn btn-danger btn-xs" id="deleteCountTag" type="button">
				            删除<i class="ace-icon fa fa-trash-o bigger-120"></i>
				        </button>				     
				        <span class="text-danger"><strong>【添加业务大盘标签会自动进行基线告警】</strong></span>
			            <input name="countTags" type="hidden"/>
					</td>
				</tr>
				<tr>
					<td style="text-align:right" class="text-success" >显示平均曲线</td>
					<td colspan='3'>
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
						<select class="tags" id="avgSelect" multiple="multiple">
							<c:choose> 
      							<c:when test="${empty model.tags}"> 
      								<option value="业务大盘">业务大盘</option>	
      							</c:when>
      							<c:otherwise>
      								<c:forEach var="item" items="${model.tags}">
						            	<option value="${item}">${item}</option>						
									</c:forEach>
      							</c:otherwise>
      						</c:choose>
						</select>
						<button class="btn btn-success btn-xs" id="addAvgTag" type="button">
			                添加其他标签<i class="icon-plus icon-white"></i>
			            </button>
			            <button class="btn btn-danger btn-xs" id="deleteAvgTag" type="button">
				            删除<i class="ace-icon fa fa-trash-o bigger-120"></i>
				        </button>
				        
			            <input name="avgTags" type="hidden"/>
					</td>
				</tr>
				<tr>
					<td style="text-align:right" class="text-success">显示求和曲线</td>
					<td colspan='3'>
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
						<select class="tags" id="sumSelect" multiple="multiple">
							<c:choose> 
      							<c:when test="${empty model.tags}"> 
      								<option value="业务大盘">业务大盘</option>	
      							</c:when>
      							<c:otherwise>
      								<c:forEach var="item" items="${model.tags}">
						            	<option value="${item}">${item}</option>						
									</c:forEach>
      							</c:otherwise>
      						</c:choose>
						</select>
						<button class="btn btn-success btn-xs" id="addSumTag" type="button">
			                添加其他标签<i class="icon-plus icon-white"></i>
			            </button>
			            <button class="btn btn-danger btn-xs" id="deleteSumTag" type="button">
				            删除<i class="ace-icon fa fa-trash-o bigger-120"></i>
				        </button>
			            <input name="sumTags" type="hidden"/>
					</td>
				</tr>
				<tr>
					<td style="text-align:right" class="text-success">业务大盘顺序(数字)</td>
					<td colspan='3'><input name="metricItemConfig.showDashboardOrder" value="${model.metricItemConfig.showDashboardOrder}" required/></td>
				</tr>
				<tr>
					<td style="text-align:center" colspan='4'><input class='btn btn-primary btn-xs' id="addOrUpdateNodeSubmit" type="submit" name="submit" value="提交" /></td>
				</tr>
			</table>
		</form>

	<script>
		$(document).ready(function(){
			$('#application_config').addClass('active open');
			$('#metricConfigList').addClass('active');
			
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
				width: 300,
	            multiple: true,
	            multipleWidth: 300,
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
				
				return true;
			})
			
			$("#addCountTag").click(function(){
				var existTags = $("#countSelect").multipleSelect('getSelects', 'text');
				$(this).parent().find(".tags").remove();
				if($(this).parent().find(".tagsInput").length == 0){
					$(this).before($('<input class="tagsInput span4" id="countInput" type="text" value="'+existTags+'"></input>'));
				}
				$(this).addClass("disabled")
			})
			
			$("#addSumTag").click(function(){
				var existTags = $("#sumSelect").multipleSelect('getSelects', 'text');
				$(this).parent().find(".tags").remove();
				if($(this).parent().find(".tagsInput").length == 0){
					$(this).before($('<input class="tagsInput span4" id="sumInput" type="text" value="'+existTags+'"></input>'));
				}
				$(this).addClass("disabled")
			})
			
			$("#addAvgTag").click(function(){
				var existTags = $("#avgSelect").multipleSelect('getSelects', 'text');
				$(this).parent().find(".tags").remove();
				if($(this).parent().find(".tagsInput").length == 0){
					$(this).before($('<input class="tagsInput span4" id="avgInput" type="text" value="'+existTags+'"></input>'));
				}
				$(this).addClass("disabled")
			})
			
			$("#deleteCountTag").click(function(){
				var addButton = $(this).prev();
				$(this).parent().find(".tags").remove();
				$(this).parent().find(".tagsInput").remove();
				addButton.before($('<input class="tagsInput span4" id="countInput" type="text" disabled></input>'));
				addButton.addClass("disabled");
				$(this).addClass("disabled");
			})
			
			$("#deleteAvgTag").click(function(){
				var addButton = $(this).prev();
				$(this).parent().find(".tags").remove();
				$(this).parent().find(".tagsInput").remove();
				addButton.before($('<input class="tagsInput span4" id="avgInput" type="text" disabled></input>'));
				addButton.addClass("disabled");
				$(this).addClass("disabled");
			})
			
			$("#deleteSumTag").click(function(){
				var addButton = $(this).prev();
				$(this).parent().find(".tags").remove();
				$(this).parent().find(".tagsInput").remove();
				addButton.before($('<input class="tagsInput span4" id="sumInput" type="text" disabled></input>'));
				addButton.addClass("disabled");
				$(this).addClass("disabled");
			})
		})
	</script>
</a:config>