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
			<td colspan='4'>
				<p class='text-error'>1、默认配置在业务大盘都进行告警。如果需要告警，请自行配置是否告警true。</p>
				<p class='text-error'>2、目前告警仅仅在次数曲线上，平均和总和暂不支持告警，平均和总和波动较大，比如销售额。</p>
				<p class='text-error'>3、有一些为Fail，比如加卡失败，建议抛出业务异常，这不作为一个业务指标，加卡成功已经能反映业务状态了。</p>
				<p class='text-error'>4、告警阈值配置需满足基线下降百分比和阈值下降绝对值两个条件，两个条件都满足才发出告警。</p>
			</td>
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
			<td colspan='2'  style="text-align:right" class="text-success">Count告警阈值
				<span class="text-error">[和基线比下降百分比，50%填写50]</span></td>
			<td colspan='2'>
				<c:choose>
					<c:when test="${model.metricItemConfig.decreasePercentage eq 0}">
						<input name="metricItemConfig.decreasePercentage" value="50" required/>
					</c:when>
					<c:otherwise>
						<input name="metricItemConfig.decreasePercentage" value="${model.metricItemConfig.decreasePercentage}" required/>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<td colspan='2'  style="text-align:right" class="text-success">Count告警阈值<span class="text-error">[和基线比下降绝对值，比如100]</span></td>
			<td colspan='2'>
				
				<c:choose>
					<c:when test="${model.metricItemConfig.decreasePercentage eq 0}">
						<input name="metricItemConfig.decreaseValue" value="100" required/>
					</c:when>
					<c:otherwise>
						<input name="metricItemConfig.decreaseValue" value="${model.metricItemConfig.decreaseValue}" required/>
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