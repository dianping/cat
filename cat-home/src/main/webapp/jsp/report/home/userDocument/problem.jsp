<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">Problem功能介绍</h4>
<h5> Problem记录整个项目在运行过程中出现的问题，包括一些错误、访问较长的行为。Problem的类型如下：</h5>
<table class="table table-bordered table-striped table-condensed   table-hover">
	<tr><td>error</td><td>Log4j记录的错误异常</td></tr>
	<tr><td>call</td><td>表示在远程调用中transaction中出错</td></tr>
	<tr><td>sql</td><td>表示在数据库的调用中transaction中出错</td></tr>
	<tr><td>url</td><td>表示在url请求中调用transaction中出错</td></tr>
	<tr><td>failure</td><td>业务程序Transaction的失败(除了call\sql\url之外)</td></tr>
	<tr><td>heartbeat</td><td>心跳消息</td></tr>
	<tr><td>long-url</td><td>执行慢的url请求（可以进行进行时间筛选）</td></tr>
	<tr><td>long-service</td><td>执行慢的service请求（可以进行进行时间筛选）</td></tr>
	<tr><td>long-sql</td><td>执行慢的sql请求（可以进行进行时间筛选）</td></tr>
</table>
<h4 class="text-success">Problem实时报表</h4>
<h5>All的错误界面</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/problem01.jpg"/>
<h5>错误一个小时内的实时趋势图</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/problem03.png" width="550px"/>
<h5>点击机器IP，进入某一台机器出现的具体问题，这个包括了All中出现的所有错误统计之外，还增加了以分钟和线程为单位的错误分布图，具体如下：</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/problem02.jpg"/>
<h4 class="text-success">Problem历史报表</h4>
<h5>1）在选择了特定的域、报表类型、时间和IP之后，点击[:: show ::] 查看某一Type下的Problem出现次数的分布图。(当前这一天、上一天、上周这一天)</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/problem04.png"  width="370px"/>
<h5>2）进一步，可以查看该Type下，某个Status的Problem出现次数的分布图。(当前这一天、上一天、上周这一天)</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/problem05.png"   width="100%"/>
