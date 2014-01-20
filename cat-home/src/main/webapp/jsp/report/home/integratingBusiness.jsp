<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h3 class="text-error">业务监控接入文档</h3>
<h4 class="text-success">第一步:确定业务指标</h4>
<h4 class="text-error">1).每个指标都有一个String作为它的唯一KEY，这个KEY在整个产品线中，不能重复。产品线的配置参考第二步。</h4>
<p>比如团购业务中，有两个核心指标，一个订单数量，一个是销售总金额</p>
<p>对这两个指标定义两个唯一的String，PayCount 和 PayAmount</p>
</br>

<h4 class="text-success">第二步:业务代码埋点</h4>
<h5 class='text-error'> Metric一共有三个API，分别用来记录次数、平均、总和，统一粒度为一分钟</h5>
<p> 1).logMetricForCount用于记录一个指标值出现的次数</p>
<p> 2).logMetricForDuration用于记录一个指标出现的平均值</p>
<p> 3).logMetricForSum用于记录一个指标出现的总和</p>
<p class='text-error'> 4).PayCount记录次数选用logMetricForCount这个API，PayAmount记录总和选用logMetricForSum这个API</p>
<p> 5).集成代码可能是如下所示</p>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/business04.png"/>

</br>

</br> 
<h4 class="text-success">第三步:产品线配置</h4>
<p>业务监控展示的是一个产品线下所有的业务指标信息，CAT提供了产品的配置信息</p>
<h4 class="text-error">url : <a href="" target="_blank">链接</a></h4>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/business01.png"/>
</br> 
<h4 class="text-success">第四步:图形展示配置</h4>
<p>当程序埋点好，后端的Metric指标的数据都是自动插入到CAT数据库中，不需要用户进行新建业务指标，用户直接修改即可。</p>
<p>此时已经能展示基本的业务监控曲线，如果需要一些其他的配置，比如业务监控图形顺序，展示标题等。</p>
<h4 class="text-error">url : <a href="/cat/s/config?op=metricConfigList" target="_blank">链接</a></h4>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/business02.png"/>
</br> 
<h4 class="text-success">第五步:配置公司级别业务大盘【运维配置】</h4>
<p>业务大盘讲各个产品线重要的业务指标进行汇总，统一展示在一个监控大盘中。</p>
<h4 class="text-error">url : <a href="/cat/s/config?op=metricConfigList" target="_blank">链接</a></h4>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/business03.png"/>


