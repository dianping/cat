<%@ page session="false" language="java" pageEncoding="UTF-8" %>

<h3 class="text-error">常见问题说明</h3>
<table class='table table-striped table-bordered table-condensed'>
	<tr class="text-success"><th>常见问题</th><th>问题解答</th></tr>
	<tr  style="color:red"><td>Job打包出错</<td><td style="white-space:normal;">升级maven-assembly-plugin至2.2-beta-5版本</td></tr>
	<tr><td>95Line</<td><td style="white-space:normal;">由于计算95line需要数据较多，为了减少内存开销，系统做了一些优化，在单个小时内某一台机器，95Line误差是1ms，但是合并成1一天，1周，1个月误差较大（数据仅供参考）</td></tr>
	<tr><td>默认告警，邮件订阅</<td><td>请项目负责人到Alarm标签下，订阅相关异常告警、服务调用失败告警、日常邮件，Hawk会逐步下线中。</td></tr>
	<tr><td>Problem、Hearbeat报表区间</<td><td>Problem、Heartbeat历史报表，图表X轴以1分钟为刻度，Y轴表示每1分钟的值</td></tr>
	<tr><td>Transaction、Event报表区间</<td><td>Transaction、Event历史报表，图表X轴以5分钟为刻度，Y轴表示每5分钟的值</td></tr>
	<tr><td>TestCase跑不起来</td><td>升级至CAT的0.4.0的版本，Pigeon版本至1.6.3以上版本</td></tr>
</table>