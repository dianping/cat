<%@ page session="false" language="java" pageEncoding="UTF-8" %>
</br>
</br>
</br>
<h4 class="text-error">【2014-04-08】</h4>

</br>
<h4 class="text-error">CAT监控内部</h4>
<div>
	<a id="navdashboard" class="btn  btn-small btn-danger" href="/cat/s/config">修改项目分组</a>
	<a id="navdashboard" class="btn  btn-small btn-primary" href="/cat/r/dependency?op=dashboard&domain=${model.domain}&date=${model.date}">应用监控仪表盘</a>
	<a id="navbussiness" class="btn  btn-small btn-primary" href="/cat/r/metric?op=dashboard&domain=${model.domain}&date=${model.date}">业务监控仪表盘</a>
</div>
</br>
<h4 class="text-error">CAT其他环境</h4>
<div>
	<a class="btn btn-small btn-primary" href="http://cat.qa.dianpingoa.com/cat/r">测试环境</a>
	<a class="btn btn-small btn-primary" href="http://cat-ppe01.hm/cat/r">PPE环境</a>
	<a class="btn btn-small btn-primary" href="http://cat.dianpingoa.com/cat/r">生产环境</a>
</div>
