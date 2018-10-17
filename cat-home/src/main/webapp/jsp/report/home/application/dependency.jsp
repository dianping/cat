<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">Dependency实时报表</h4>
<table style="width:50%" class="table table-striped table-bordered table-condensed   table-hover">
	<tr><td>时间统计粒度</td><td class='text-danger'>分钟</td></tr>
	<tr><td>形状：圆形</td><td class='text-danger'>SOA的一个服务或者一个Web</td ></tr>
	<tr><td>形状：矩形</td><td class='text-danger'>数据库一个实例</td></tr>
	<tr><td>形状：菱形</td><td class='text-danger'>缓存一种集群（MemoryCached）</td></tr>
	<tr><td>状态：红色</td><td class='text-danger'>Error</td></tr>
	<tr><td>状态：黄色</td><td class='text-danger'>Warning</td></tr>
	<tr><td>状态：绿色</td><td class='text-danger'>OK</td></tr>
	<tr><td>浮层</td><td>点击节点可以直接查询这一分钟内这个节点的详细状态</td></tr>
</table>
<img  class="img-polaroid"  src="${model.webapp}/images/dependency01.png" width="700" />
<h4 class="text-success">应用监控大盘</h4>
<h5>把所有核心项目用监控大盘方式展示，能全局看到项目目前问题。</h4>
<img  class="img-polaroid"  src="${model.webapp}/images/dependency02.png" width="100%"/>
<img  class="img-polaroid"  src="${model.webapp}/images/dependency03.png" width="100%"/>
