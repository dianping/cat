<%@ page contentType="text/html; charset=utf-8"%>
<div class="content-left">
	<p align="center">
		<script type="text/javascript">
			d = new dTree('d');
			d.add(0, -1, 'CAT告警', '');
			d.add(1, 0, '个人邮件记录', '#');
			d.add(2, 1, '告警邮件记录', 'javascript:getAlarmMails()');
			d.add(3, 1, '报表邮件记录', 'javascript:getReportMails()');
			d.add(4, 0, '报表告警订阅', '#');
			d.add(5, 4, '异常告警订阅', 'javascript:exceptionAlarmRules()');
			d.add(6, 4, '服务告警订阅', 'javascript:serviceAlarmRules()');
			d.add(7, 4, '日常报表订阅', 'javascript:scheduledReports()');
			d.add(8, 0, '告警模板配置', '#');
			d.add(9, 8, '异常模板配置', 'javascript:exceptionTemplateDetail()');
			d.add(10, 8, '服务调用配置', 'javascript:serviceTemplateDetail()');
			document.write(d);
			d.closeAll();
		</script>
</div>