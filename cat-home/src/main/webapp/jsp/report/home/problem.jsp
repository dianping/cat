<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<table class='table table-striped table-bordered table-condensed  '>
	<tr class="text-success"><th>常见问题</th><th>问题解答</th></tr>
	<tr><td>在cat上显示两个不同项目名</<td><td>由于phoenix灰度升级，业务可以直接升级至1.2.2，解决问题。</td></tr>
	<tr><td>Job打包不能执行</<td><td>请检查系统临时目录，运行程序是否有写的权限</td></tr>
	<tr><td>Job打包出错</<td><td>升级maven-assembly-plugin至2.2-beta-5版本</td></tr>
	<tr><td>95Line</<td><td>由于计算95line需要数据较多，为了减少内存开销，系统做了一些优化，在单个小时内某一台机器，95Line误差是5ms（数据仅供参考）</td></tr>
	<tr><td>默认告警，邮件订阅</<td><td>请项目负责人到Alarm标签下，订阅相关异常告警、服务调用失败告警、日常邮件，Hawk会逐步下线中。</td></tr>
	<tr><td>Problem、Hearbeat报表区间</<td><td>Problem、Heartbeat历史报表，图表X轴以1分钟为刻度，Y轴表示每1分钟的值</td></tr>
	<tr><td>Transaction、Event报表区间</<td><td>Transaction、Event历史报表，图表X轴以1分钟为刻度，Y轴表示每1分钟的值</td></tr>
	<tr><td>TestCase跑不起来</td><td>升级至CAT的0.4.0的版本，Pigeon版本至1.6.3以上版本</td></tr>
</table>

<h4 class="text-danger">JOB埋点问题</h4>

<p>最近生产环境的JOB越来越多，JOB目前没有统一的框架，导致很多JOB直接使用中间件的JAR包时候，会出现很多的零散的消息。</p>
<p>这些消息都是独立的消息，比如SQL，Cache。消息的数量非常多。</p>
<p>以ba-finance-report-generate-job  为例子，每分钟会发出大概约3w个消息。后端服务还处理了很多其他的项目，导致一些不稳定。</p>
<img  class="img-polaroid"  width='40%' src="${model.webapp}/images/job01.png"/>

<h4 class="text-danger">JOB埋点优化办法</h4>
<xmp class="well">
 Transaction  t = Cat.newTransaction("Job","JobName"); //JOB都是一些后台线程，在Job开始加入一个埋点
   //这样在Job中产生的消息就在一个logview中，有效减少消息数量，并能监控到JOB执行时间
 t.complete.  //job结束的地方加入
</xmp>
