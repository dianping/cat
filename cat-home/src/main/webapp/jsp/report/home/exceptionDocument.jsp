<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h3 class="text-error">异常监控文档</h3>
</br>
<h4 class="text-info">对所有应用异常进行监控，通过对异常次数的判断选择进行警告的发送，并统计出异常警告报表。</h4>
</br>
<h4 class="text-success">1. 异常监控配置设置</h4>
<table style="width:90%" class="table table-striped table-bordered table-condensed">
	<tr><th width="20%">参数</th><th>说明</th></tr>
	<tr>
		<td>域名</td>
		<td>项目组名称，<span class="text-error">必需，</span>当设置为“Default”时，适用于所有项目组。</td>
	<tr>
		<td>异常名称</td>
		<td>具体异常名称，<span class="text-error">必需，</span>当设置为“Total”时，是针对当前项目组所有异常总数阈值进行设置；当设置为特定异常名称时，针对当前项目组所有同名的异常阈值进行设定</td>
	</tr>
	<tr>
		<td>Warning阈值</td>
		<td>认定为Warning级别的阈值，<span class="text-error">必需，</span>当异常数小于该阈值时，不做任何警报；当超过该阈值，小于Error阈值时，做Warning状态设置，做相应警warning告警</td>
	</tr>
	<tr>
		<td>Error阈值</td>
		<td>认定为Error级别的阈值，<span class="text-error">必需，</span>当异常数超过该阈值，做Error状态设置，做相应警Error告警</td>
	</tr>
</table>
<h4 class="text-success">2. 异常告警</h4>
<p>实时监控当前的应用异常情况，对于超过阈值设置的异常进行邮件、短信形式告警，根据不同的出错级别进行相应的告警，提醒相关开发维护人员及时进行修复。</p>
<h4 class="text-success">3. 异常告警统计报表</h4>
<p>a) 将异常告警的情况进行统计，按Error警告的次数从大到小进行排序，呈现出错警告的详细情况。</p>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/exceptionReport.jpeg"/>
<p/>
<p>b) 点击“Detail”查看详细异常情况</p>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/exception.jpeg"/>
</br>