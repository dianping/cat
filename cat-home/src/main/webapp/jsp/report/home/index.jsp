<%@ page session="false" language="java" pageEncoding="UTF-8" %>
</br>
<h4 class="text-danger">CAT监控大盘</h4>
<div>
	<a id="navmetricDashboard" class="btn btn-sm btn-primary" href="/cat/r/dependency?op=metricDashboard&domain=${model.domain}">系统报错大盘</a>
	<a id="navdashboard" class="btn btn-sm btn-primary" href="/cat/r/dependency?op=dashboard&domain=${model.domain}">应用监控大盘</a>
	<a id="navbussiness" class="btn btn-sm btn-primary" href="/cat/r/metric?op=dashboard&domain=${model.domain}">业务监控大盘</a>
	<a id="navbussiness" class="btn btn-sm btn-primary" href="/cat/r/network?op=dashboard&domain=${model.domain}">网络监控大盘</a>
</div>
</br>
<h4 class="text-danger">CAT其他环境</h4>
<div>
	<a class="btn btn-sm btn-primary" href="http://cat.qa.dianpingoa.com/cat/r/">测试环境</a>
	<a class="btn btn-sm btn-primary" href="http://ppe.cat.dp/cat/r/">PPE环境</a>
	<a class="btn btn-sm btn-primary" href="http://cat.dianpingoa.com/cat/r/">生产环境</a>
</div>
</br>
<h4 class="text-danger">使用CAT的公司列表，新使用的公司请邮件至 youyong205@126.com ，为开源出力！！！</h4>
<ul>
	<li><a target="_blank" href="http://www.dianping.com/"><h5>大众点评</h5></a></li>
	<li><a target="_blank" href="https://www.lufax.com/"><h5>陆金所</h5></a></li>
	<li><a target="_blank" href="http://www.ly.com/"><h5>同程旅游</h5></a></li>
	<li><a target="_blank" href="http://www.shangpin.com/"><h5>尚品网</h5></a></li>
	<li><a target="_blank" href="http://www.travelzen.com/flight/"><h5>真旅网</h5></a></li>
	<li><a href="#"><h5>......这里等着你</h5></a></li>
</ul>