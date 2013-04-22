<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model" scope="request" />

<a:body>
<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js"/>

	<div class="row-fluid">
		<div class="span6">
			<form name="alarmModify" id="form" method="post"
				action="${model.pageUri}?op=alarmRuleUpdateSubmit">
				<table class="table table table-striped table-bordered">
					<input type="hidden" name="alarmRuleId"
						value="${model.alarmRule.id}" />
					<tr>
						<td>项目名称</td>
						<td>${model.alarmRule.domain}</td>
					</tr>
					<tr>
						<td>自定义内容</td>
						<td><textarea style="height: 500px; width: 500px"
								id="content" name="content">${model.alarmRule.content}</textarea></td>
					</tr>
					<tr>
						<td colspan="2" style="text-align: center"><input
							type="submit" class='btn-small btn-primary' name="submit"
							value="submit" /></td>
					</tr>
				</table>
			</form>
		</div>
		<div class="span6">
			<h3 class='text-error text-error'>样本内容(可以copy右侧内容修改简单参数即可)</h3>
			<xmp>${model.alarmTemplate.content}</xmp>
			<h3 class='text-error'>说明事项</h3>
			<table class="table table table-striped table-bordered">
				<tr>
					<td class="text-error">interval</td>
					<td>定义一段时间</td>
				</tr>
				<tr>
					<td class="text-error">min</td>
					<td>一段时间内出现错误的最小值</td>
				</tr>
				<tr>
					<td class="text-error">max</td>
					<td>一段时间内出现错误的最大值</td>
				</tr>
				<tr>
					<td class="text-error">alarm-interval</td>
					<td>表示在此时间段内告警一次</td>
				</tr>
				<tr>
					<td class="text-error">alarm</td>
					<td>表示告警类型，暂时支持EMAIL和SMS，多种用逗号隔开</td>
				</tr>
			</table>
			<h3 class='text-error'>解释说明duration1</h3>
			<xmp><duration id="duration1" min="50" max="250" interval="3"
				alarm-interval="30" alarm="EMAIL" /></xmp>
			<p class="text-warning">duration1表示在一定的3分钟时间内错误出现在50到250之间之间，会发email告警，在30分钟内只会告警一次。</p>
			<h3 class='text-error'>解释说明duration2</h3>
			<xmp><duration id="duration2" min="250" max="2500000"
				interval="3" alarm-interval="120" alarm="SMS" /></xmp>
			<p class="text-warning">duration2表示在一定的3分钟时间内错误出现在250到2500000之间之间，会发SMS告警，在120分钟内只会告警一次。（目前SMS不启作用，仍然会用邮件，邮件标题有SMS)</p>
		</div>
	</div>
</a:body>
