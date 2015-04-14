<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-info">对所有应用的Transaction进行监控，通过对响应时间的判断选择进行警告的发送。</h4>
<br/>
<h4 class="text-success">1.响应时间告警</h4>
<p>a） 监控对象：一个transaction或者某个类型下所有transaction在一分钟内的平均响应时间</p>
<p>b） 告警规则：根据已设定的一分钟内平均响应时间的阈值，对监控对象进行选择性告警</p>
<p>c） 告警周期：一分钟</p>
<br/>
<h4 class="text-success">2. 响应时间阈值<a href="/cat/s/config?op=transactionRule">配置</a></h4>
<table style="width:90%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="20%">参数</th><th>说明</th></tr>
	<tr>
		<td>域名</td>
		<td>项目组名称，<span class="text-danger">必需</span></td>
	<tr>
		<td>Type</td>
		<td>Transaction类型，<span class="text-danger">必需，</span>某一类transaction的组别</td>
	</tr>
	<tr>
		<td>Name</td>
		<td>Transaction名称，<span class="text-danger">非必需，</span>默认为All。当为All时，监控目标位为当前Type下所有transaction在一分钟内的平均响应时间；当为具体一个name时，监控某该transaction在一分钟内的平均响应时间。</td>
	</tr>
	<tr>
		<td>告警阈值</td>
		<td>目前只有waring级别告警，<span class="text-danger">必需，</span>当平均响应时间数超过该阈值，做warning状态设置，做相应告警。（单位：毫秒）</td>
	</tr>
</table>
<br/>
<h4 class="text-success">3. 配置示例说明</h4>
<h5>A) 配置一览表</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/alert/transaction01.png"/>
<h5>B) 配置告警规则</h5>
<p>（1）告警名自定义，方便区分告警项。可对<span class="text-danger">响应时间</span>进行监控。</p>
<p>（2）多个监控规则构成了告警的主体，分别对不同时间段进行配置，以方便准确地进行告警。</p>
<p>（3）监控规则诠释着某个时间段内如何进行告警，由任意多个监控条件组成。任何一条监控条件触发都会引起监控规则触发，从而告警。</p>
<p>（4）监控条件诠释着什么条件会触发监控规则，由任意多个监控子条件组成。当所有子条件同时被触发时，才会触发该监控规则。</p>

<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/alert/transaction02.png"/>
