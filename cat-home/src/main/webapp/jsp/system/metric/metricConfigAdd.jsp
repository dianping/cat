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
	<p class="text-center text-error"><strong>修改业务监控节点配置信息</strong></p>
	<input name="productLineName" value="${payload.productLineName}" type="hidden"/>
	<table class="table table-striped table-bordered table-condensed">
		<tr>
			<td width="25%" style="text-align:right" class="text-success" width="50%">项目名称</td>
			<td width="25%" >
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
			<td  style="text-align:right" class="text-success">标签</td>
			<td >
				<span id="inputTag">
					<select class="span6" id="tags" name="metricItemConfig.tag">
						<option value="">无标签</option>
						<c:forEach var="item" items="${model.tags}">
			            	<option value="${item}">${item}</option> 							
						</c:forEach>
					</select>
				</span>
				<button class="btn btn-success btn-small" id="addTag" type="button">
	                添加新标签<i class="icon-plus icon-white"></i>
	            </button>
			</td>
			<td style="text-align:right" class="text-success">标签类型</td>
			<td >
				<select class="span8" name="metricItemConfig.monitorTagType">
					<option value="COUNT">次数</option>
					<option value="AVG">平均值</option>
					<option value="SUM">总和</option>
				</select>
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
			</td><td  width="25%" style="text-align:right" class="text-success">显示业务监控大盘</td>
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
			</td><td style="text-align:right" class="text-success"  width="25%">显示业务监控大盘</td>
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
			</td><td style="text-align:right" class="text-success"  width="25%">显示业务监控大盘</td>
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
		var tag = "${model.metricItemConfig.tag}";
		if(tag != "" && tag != "null"){
			$('select[name="metricItemConfig.tag"]').val(tag);
		}
		
		var tagType = "${model.metricItemConfig.monitorTagType}";
		if(tagType != "" && tagType != "null"){
			$('select[name="metricItemConfig.monitorTagType"]').val(tagType);
		}
		
		$("#addTag").click(function(){
			$("#inputTag").empty();
			$("#inputTag").append($('<input class="span6" name="metricItemConfig.tag" type="text"></input>'));
		})
	})
</script>