<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">Heartbeat实时报表</h4>
<h5>Heartbeart是CAT客户端一分钟一次向服务器发送自身的状态信息。Machine是当前项目下所有的部署机器。Heartbeat包括：</h5>
<h5 class="text-info">Thread信息包括</h5>
<table style="width:50%" class="table table-bordered table-striped table-condensed   table-hover">
	<tr><td>Active Thread</td><td> 系统当前活动线程</td></tr>
	<tr><td>Daemon Thread</td><td>系统后台线程</td></tr>
	<tr><td>Total Started Thread</td><td> 系统总共开启线程</td></tr>
	<tr><td>Started Thread</td><td>系统每分钟新启动的线程</td></tr>
	<tr><td>Cat Started Thread </td><td>系统中CAT客户端启动线程</td></tr>
	<tr><td>Pigeon Started Thread</td><td> 系统中Pigeon客户端启动线程数</td></tr>
</table>
<h5 class="text-info">System Info信息包括</h5>
<table style="width:50%" class="table table-bordered table-striped table-condensed   table-hover">
	<tr><td>NewGc Count</td><td>新生代GC次数</td></tr>
	<tr><td>OldGc Count</td><td>旧生代GC次数</td></tr>
	<tr><td>System Load Average</td><td>系统Load详细信息</td></tr>
</table>
<h5 class="text-info">Memery Info信息包括</h5>
<table style="width:50%" class="table table-bordered table-striped table-condensed   table-hover">
	<tr><td>Memory Free</td><td>系统memoryFree情况</td></tr>
	<tr><td>Heap Usage</td><td>Java虚拟机堆的使用情况</td></tr>
	<tr><td>None Heap Usage</td><td>Java虚拟机Perm的使用情况</td></tr>
</table>
<h5 class="text-info">Disk Info信息包括</h5>
<table style="width:50%" class="table table-bordered table-striped table-condensed   table-hover">
	<tr><td>/根的使用情况</td></tr>
	<tr><td>/data盘的使用情况</td></tr>
</table>
<h5 class="text-info">Cat Info信息包括</h5>
<table style="width:50%" class="table table-bordered table-striped table-condensed   table-hover">
	<tr><td>Cat每分钟产生消息数</td></tr>
	<tr><td>Cat每分钟丢掉的消息数</td></tr>
	<tr><td>Cat每分钟产生消息大小</td></tr>
</table>
<img  class="img-polaroid"  src="${model.webapp}/images/heartbeat01.png"  width="100%"/>