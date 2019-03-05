<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">服务端使用说明&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/r/server?domain=cat">访问链接</a></h4>
<p><strong>监控服务端的各项系统指标，包括：数据库监控、Paas系统监控、网络监控等</strong></p>
</br>

<h5 class="text-success"><strong>1.可以按照既定模板进行指标查看，下图显示服务端监控的模板化指标查看</strong></h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/serverMonitor/view01.png"/>

<h5 class="text-success"><strong>2.可以自定义指标展示模板，如下图所示</strong></h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/serverMonitor/view02.png"/>
<h5>（1）可以根据EndPoint（监控对象的唯一ID）关键字或tag来进行搜索</h5>
<h5>（2）点击“刷新指标列表”来获取当前所有指标（Measurement）</h5>
<h5>（3）勾选至少一个Measurement，点击看图，选择Endpoint视角或easure视角</h5>