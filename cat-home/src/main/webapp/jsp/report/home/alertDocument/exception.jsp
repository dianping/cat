<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-info">对所有应用异常进行监控，通过对异常次数的判断选择进行警告的发送，并统计出异常警告报表。</h4>
<br/>
<h4 class="text-success">1. 异常阈值<a href="/cat/s/config?op=exception">配置</a></h4>
<table style="width:90%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="20%">参数</th><th>说明</th></tr>
	<tr>
		<td>域名</td>
		<td>项目组名称，<span class="text-danger">必需，</span>当设置为“Default”时，适用于所有项目组。</td>
	<tr>
		<td>异常名称</td>
		<td>异常名称，<span class="text-danger">必需，</span>当设置为“Total”时，是针对当前项目组所有异常总数阈值进行设置；当设置为特定异常名称时，针对当前项目组所有同名的异常阈值进行设定</td>
	</tr>
	<tr>
		<td>Warning阈值</td>
		<td>认定为Warning级别的阈值，<span class="text-danger">必需，</span>当异常数小于该阈值时，不做任何警报；当超过该阈值，小于Error阈值时，做Warning状态设置，做相应警warning告警</td>
	</tr>
	<tr>
		<td>Error阈值</td>
		<td>认定为Error级别的阈值，<span class="text-danger">必需，</span>当异常数超过该阈值，做Error状态设置，做相应警Error告警</td>
	</tr>
</table>
<br/>
<h4 class="text-success">2. 异常过滤<a href="/cat/s/config?op=exception">配置</a></h4>
<p>对于不想进行异常告警的异常，可以在异常过滤配置里进行设置</p>
<table style="width:90%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="20%">参数</th><th>说明</th></tr>
	<tr>
		<td>域名</td>
		<td>项目组名称，<span class="text-danger">必需，</span>当设置为“Default”时，适用于所有项目组。</td>
	<tr>
		<td>异常名称</td>
		<td>异常名称，<span class="text-danger">必需，</span>当设置为特定异常名称时，过滤当前项目组所有同名的异常</td>
	</tr>
</table>
<br/>
<h4 class="text-success">3. 异常告警<a href="/cat/s/config?op=projects">组邮件配置</a></h4>
<p>针对每个项目组，可以进行组邮件和联系人的配置，CAT将根据此配置进行邮件的发送。</p>
<p class="text-danger">注意：不设置组邮件的话，您将错过第一时间知晓异常的机会！</p>
<br/>
<h4 class="text-success">4. 异常告警</h4>
<h5>(1) 实时监测当前的应用异常情况，对于超过阈值设置的异常，Warning级别的仅发送邮件，Error级别的同时发送邮件和短信。</h5>
<h5>(2) 监测异常并发送警报的周期为：<span class="text-danger">一分钟</span></h5>
<h5>(3) 异常判定规则</h5>
<xmp class="well">
a) 监测到的所有异常总数，达到该项目组设定的异常总数阈值时，进行告警：
   总数大于Warning阈值，小于Error阈值，进行Warning级别告警；大于Error阈值，进行Error级别告警。
   只关心周期内异常出现次数总和，一个周期内最多只发送该告警一次。
     
b) 当监测到特定异常总数，达到该项目组设定的该异常阈值时，进行告警：
   总数大于Warning阈值，小于Error阈值，进行Warning级别告警；大于Error阈值，进行Error级别告警。
   如果特定异常在一个周期内出现多次超过阈值的情况，不会立即告警，而是周期结束时根据异常总和大小来判定并告警，故一个周期内最多只发送该告警一次。
</xmp>
<br/>
<h4 class="text-success">5. 异常告警统计<a href="/cat/r/statistics?domain=Cat&op=alert">报表</a></h4>
<p>a) 将异常告警的情况进行统计，按Error警告的次数从大到小进行排序，呈现出错警告及排行的详细情况。</p>
<p/>
<p>b) 点击“<a href="/cat/r/statistics?domain=Cat&op=alert">Detail</a>”查看详细异常情况</p>