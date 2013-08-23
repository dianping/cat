<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.config.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.config.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.config.Model" scope="request"/>

<form name="metricConfigAddSumbit" id="form" method="post" action="${model.pageUri}?op=metricConfigAddSumbit">
	
	<h4 class="text-center text-error" id="state">&nbsp;</h4>
	<h4 class="text-center text-error">修改业务监控节点配置信息</h4>
	<input name="productLineName" value="${payload.productLineName}" type="hidden"/>
	<table class="table table-striped table-bordered table-condensed">
		<tr>
			<td style="text-align:right" class="text-success">项目名称</td>
			<td colspan='3'>
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
		</tr>
		<tr>
			<td style="text-align:right" class="text-success">类型</td>
			<td colspan='3'>
				<c:if test="${not empty model.metricItemConfig.domain}">
					<input name="metricItemConfig.type" value="${model.metricItemConfig.type}" readonly required/>
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
			<td colspan='3'>
				<c:if test="${not empty model.metricItemConfig.domain}">
					<input name="metricItemConfig.metricKey" value="${model.metricItemConfig.metricKey}" readonly required/>
				</c:if>
				<c:if test="${empty  model.metricItemConfig.type}">
					<input name="metricItemConfig.metricKey" value="${model.metricItemConfig.metricKey}" required/>
				</c:if>
			</td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success">显示顺序</td>
			<td colspan='3'><input  name="metricItemConfig.viewOrder" value="${model.metricItemConfig.viewOrder}" required/></td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success">显示标题</td>
			<td colspan='3'><input name="metricItemConfig.title" value="${model.metricItemConfig.title}" required/></td>
		</tr>
		<tr>
			<td style="text-align:right" class="text-success">显示次数曲线</td>
			<td>
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
			</td><td style="text-align:right" class="text-success">显示监控大盘</td>
			<td>
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
			<td style="text-align:right" class="text-success">显示平均曲线</td>
			<td>
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
			</td><td style="text-align:right" class="text-success">显示监控大盘</td>
			<td>
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
			<td style="text-align:right" class="text-success">显示求和曲线</td>
			<td>
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
			</td><td style="text-align:right" class="text-success">显示监控大盘</td>
			<td>
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
			<td style="text-align:center" colspan='4'><input class='btn btn-primary' id="addOrUpdateNodeSubmit" type="submit" name="submit" value="提交" /></td>
		</tr>
	</table>
</form>