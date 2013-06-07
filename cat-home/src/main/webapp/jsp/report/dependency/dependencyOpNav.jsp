<%@ page contentType="text/html; charset=utf-8" %>
<div class="text-center">
	<a class="btn btn-danger  btn-primary" href="?minute=${model.minute}&domain=${model.domain}&date=${model.date}">实时趋势图</a>
	<a class="btn btn-danger  btn-primary" href="?op=dependencyGraph&minute=${model.minute}&domain=${model.domain}&date=${model.date}">实时拓扑图</a>
	<a class="btn btn-danger  btn-primary" href="?op=productLine&minute=${model.minute}&domain=${model.domain}&date=${model.date}">产品线监控</a>
	<a class="btn btn-danger  btn-primary" href="?op=dashboard&minute=${model.minute}&domain=${model.domain}&date=${model.date}">应用监控大盘</a>
</div>