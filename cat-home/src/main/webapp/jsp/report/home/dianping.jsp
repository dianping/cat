<%@ page session="false" language="java" pageEncoding="UTF-8" %>


</br>
<h4 class="text-error">CAT QA环境机器调整通知</h4>
1、线下的CAT的环境是一台虚拟机(ip:192.168.7.70)，无法满足线下的需求，目前已经正式替换为一台物理机。</br>

2、目前现有机器上/data/appdatas/cat/client.xml需要重新配置。</br>

3、Beta的环境我们会和OPS统一进行替换，Alpha环境我们后续会尝试统一修改。</br>

4、如果是dev或者Alpha机器并使用到CAT，请手动修改此文件，需要将192.168.7.70替换为192.168.213.115</br>
<xmp>
  	<config mode="client">
          <servers>
             <server ip="192.168.213.115" port="2280" />
         </servers>
         <domain id="MobileApi" enabled="true"/>
      </config>
</xmp>

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
	<a class="btn btn-small btn-primary" href="http://10.1.8.64:8080/cat/r">预发环境</a>
	<a class="btn btn-small btn-primary" href="http://cat.dianpingoa.com/cat/r">生产环境</a>
	<a class="btn btn-small btn-primary" href="http://10.1.8.152:8080/cat/r">BA后台环境</a>
</div>
