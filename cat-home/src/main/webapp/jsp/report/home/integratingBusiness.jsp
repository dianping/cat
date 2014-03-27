<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h3 class="text-error">业务监控接入文档</h3>
</br>
<h4 class="text-info">强调两个名词，业务监控以及业务分析</h4>
<h5>a).业务分析，产品线有很多指标，来确定产品是否能满足用户需求，这部分DW在负责。</h5>
<h5>b).业务监控，它关注于最重要的业务指标，业务监控目的是快速发现业务是否存在问题，一旦出现问题，这类问题对于业务的影响有多大。</h5>
<h5>c).业务监控和业务分析有部分的交叉，业务监控数据可能是不准确的，比如销售额，他仅仅用于监控，用于发现业务是否正常。建议产品线的核心指标不超过6个。</h5>

</br>
<p>1.业务指标是反映业务运行状态，<span class="text-error">建议使用count作为业务指标</span>，比如订单次数，支付次数，count作为指标他的趋势比较固定，告警也相当准确。</p>
<p>2.有些业务是用了失败作为一个指标，<span class="text-error">比如会员卡加卡失败，这个不应该作为业务指标，这个应该封装为一个Exception</span>。在监控大盘中可以针对一个类型的异常进行告警。</p>
<p>3.有些使用使用了平均响应时间作为指标，<span class="text-error">这也不建议使用，因为这些指标其实是性能指标。</span>响应指标其实和count很相关，响应时间出了问题，访问量都会出问题，但是访问量出问题（比如CDN挂了），响应时间还是正常的。</p>
<p>4.一个业务核心指标往往就那么非常关键的几个，请尽量使用count指标。比如团购业务中，重要三个指标，订单数，支付数，验券数</p>

</br>
</br>
<h4 class="text-success">第一步:确定业务指标</h4>
<h5>1).每个指标都有一个String作为它的唯一KEY，这个KEY在整个产品线中，不能重复。产品线的配置参考第三步。</h5>
<p>比如团购业务中，有两个核心指标，一个订单数量，一个是支付数量</p>
<p>对这两个指标定义两个唯一的String，OrderCount 和 PayCount</p>
</br>

<h4 class="text-success">第二步:业务代码埋点</h4>
<h5 class='text-error'> Metric一共有三个API，分别用来记录次数、平均、总和，统一粒度为一分钟</h5>
<h5>1.Java API调用方式</h5>
<p> 1).logMetricForCount用于记录一个指标值出现的次数</p>
<p> 2).logMetricForDuration用于记录一个指标出现的平均值</p>
<p> 3).logMetricForSum用于记录一个指标出现的总和</p>
<p class='text-error'> 4).OrderCount，PayCount记录次数选用logMetricForCount这个API</p>
<p> 5).集成代码可能是如下所示</p>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/business04.png"/>
<h5>2.HTTP API调用方式</h5>
<p>接口调用请求说明</p>
<pre>
	http请求方式: GET（请使用http协议）
	http://cat.dianpingoa.com/cat/r/systemMonitor?
</pre>
<p>参数说明</p>
<table style="width:50%" class="table table-striped table-bordered table-condensed">
	<tr><th width="30%">参数</th><th>说明</th></tr>
	<tr><td>group</td><td>监控组唯一ID名称，<span class="text-error">必需</span></td></tr>
	<tr><td>domain</td><td>应用唯一ID名称，<span class="text-error">必需</span></td></tr>
	<tr><td>key</td><td>监控业务唯一ID名称，<span class="text-error">必需</span></td></tr>
	<tr><td>op</td><td>sum，avg，count[<span class="text-error">默认</span>]</td></tr>
	<tr><td>count</td><td>op=count时所需，<span class="text-error">默认为1</span></td></tr>
	<tr><td>sum</td><td>op=sum时所需，<span class="text-error">默认为0</span></td></tr>
	<tr><td>avg</td><td>op=avg时所需，<span class="text-error">默认为0</span></td></tr>
</table>

<p> 1).op = count时，用于记录一个指标值出现的次数</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?group=myGroup&domain=myApp&key=myKey&op=count
</pre>
<p> 2).op = avg时，用于记录一个指标出现的平均值</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?group=myGroup&domain=myApp&key=myKey&op=avg&avg=500
</pre>
<p> 3).op = sum时，用于记录一个指标出现的总和</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?group=myGroup&domain=myApp&key=myKey&op=sum&sum=500
</pre>
<p>返回说明</p>
<pre>
	<span class="text-error">{"statusCode":"-1","errorMsg":"Unknown [ domain,group,key ] name!"} ——> 失败 [必需参数缺失]</span>
	<span class="text-success">{"statusCode":"0"} ——> 成功</span>
</pre>
</br>
</br> 
<h4 class="text-success">第三步:产品线配置</h4>
<p>业务监控展示的是一个产品线下所有的业务指标信息，CAT提供了产品的配置信息</p>
<p><span class='text-error'>1、必须把项目加入到一个产品线，这样项目下所有指标才能在这个产品线正确展示</span></p>
<p><span class='text-error'>2、告警邮件:当这个产品线下的业务指标出现异常时，会发送邮件到此邮箱</span></p>
<p><span class='text-error'>3、告警短信:当这个产品线下的业务指标出现异常时，会发送邮件到此号码</span></p>
<h4 class="text-error">url : <a href="" target="_blank">链接</a></h4>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/business01.png"/>
</br> 
<h4 class="text-success">第四步:图形展示以及告警配置</h4>
<p>当程序埋点好，后端的Metric指标的数据都是自动插入到CAT数据库中，不需要用户进行新建业务指标，用户直接修改即可。</p>
<p>此时已经能展示基本的业务监控曲线，如果需要一些其他的配置，比如业务监控图形顺序，展示标题等。</p>
<h4 class="text-error">url : <a href="/cat/s/config?op=metricConfigList" target="_blank">链接</a></h4>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/business02.png"/>
</br> 
<h4 class="text-success">第五步:配置公司级别业务大盘【运维配置】</h4>
<p>业务大盘讲各个产品线重要的业务指标进行汇总，统一展示在一个监控大盘中。</p>
<h4 class="text-error">url : <a href="/cat/s/config?op=metricConfigList" target="_blank">链接</a></h4>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/business03.png"/>


