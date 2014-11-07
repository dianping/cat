<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-error">APP异常日志监控文档</h4>
<h5 class="text-info"> a). 安卓版APP日志发送到<span class="text-error">AndroidCrashLog</span>，苹果版APP日志发送到<span class="text-error">iOSCrashLog</span>。</h5>
<h5 class="text-info"> b). 监控维度：平台类型、APP版本、平台版本、模块、错误级别</h5>


</br>

<h4 class="text-success">KEY的格式定义</h5>
	<h5>以英文冒号隔开各维度的信息：【<span class="text-error">APP版本:平台版本:模块:错误级别</span>】</h5>
	<p>如下表：</p>
	<table style="width:40%" class="table table-striped table-bordered table-condensed">
		<tr><th>字段</th><th>值</th></tr>	
		<tr><td>平台类型</td><td>Android</td></tr>
		<tr><td>APP版本</td><td>6.9</td></tr>
		<tr><td>平台版本</td><td>4.4.2</td></tr>
		<tr><td>模块</td><td>moudle</td></tr>
		<tr><td>错误级别</td><td>error</td></tr>
	</table>
	<pre>
Transaction t = Cat.newTransaction("CrashLog", "Android");
Cat.logError(new RuntimeException("CrashLogTest"));
MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
((DefaultMessageTree) tree).setIpAddress("<span class="text-error">6.9:4.4.2:module:error</span>"); <span class="text-success">//红色部分为KEY</span>
((DefaultMessageTree) tree).setDomain("AndroidCrashLog");  <span class="text-success">//iOS版APP日志发送到iOSCrashLog</span>
t.complete();
	</pre>	