<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model" scope="request" />

<form name="alarmModify" id="form" method="post" action="${model.pageUri}?op=alarmRuleUpdateSubmit">
	<table border="1" align="center" width="100%" rules="all">
		<input type="hidden" name="alarmRuleId" value="${model.alarmRule.id}" />
		<tr>
			<td>项目名称</td>
			<td colspan='2'>${model.alarmRule.domain}</td>
		</tr>
		<tr>
			<td>样本内容</td>
			<td><textarea style="height:200px;width:600px" readonly>${model.alarmTemplate.content}</textarea></td>
			<td>
		     min-最小值、max-最大值，interval表示时间段、alarm-interval表示在此时间段内告警一次，alarm表示告警类型，暂时支持EMAIL和SMS，多中用逗号隔开。
		     <xmp><duration id="duration1" min="50" max="250" interval="3" alarm-interval="30" alarm="EMAIL"/></xmp>
		     duration1表示在一定的3分钟时间内错误出现在50到250之间之间，会发email告警，在30分钟内只会告警一次。
		    <xmp> <duration id="duration2" min="250" max="2500000" interval="3" alarm-interval="120" alarm="SMS"/></xmp>
		     duration2表示在一定的3分钟时间内错误出现在250到2500000之间之间，会发SMS告警，在120分钟内只会告警一次。（目前SMS不启作用，仍然会用邮件，邮件标题有SMS)
		     </td>
		</tr>
		<tr>
			<td>自定义内容</td>
			<td><textarea style="height:200px;width:600px" id="content" name="content">${model.alarmRule.content}</textarea></td>
			<td>可以Coyp样本内容，修改duration1或者duration2中的min、max、interval、alarm-interval值即可</td>
		</tr>
		<tr>
			<td colspan="3" align="center"><input type="submit" name="submit" value="submit" /></td>
		</tr>
	</table>
</form>