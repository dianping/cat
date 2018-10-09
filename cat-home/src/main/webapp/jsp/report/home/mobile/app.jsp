<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">1、APP端到端使用说明&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/r/app?domain=cat&ip=All&reportType=&op=view">访问链接</a></h4>
<p>监控点评APP的接口调用情况，这个是从用户手机APP采集的数据，从用户角度看点评接口的访问速度。</p>
<p>监控的分析的维度有返回码、网络类型、APP版本、平台、地区、运营商等。</p>
<br/>

<h5 class="text-danger">Sample1：Api访问趋势示例图如下方所示：</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor01.png"/>
<br/><br/>

<pre>
当点击<span class="text-danger">查询</span>按钮时，cat网页会根据筛选选项进行数据查询和图表绘制。

<span class="text-danger">选择对比</span>复选框是网络监控的一个高级功能，用于两种查询的对比展示。

图表上方有<span class="text-danger">请求数/成功率/成功延时</span>的三个切换按钮，用于绘制按照三种数据得到的图表。

图标下方是数据汇总，其中<span class="text-danger">成功率、总请求数和成功平均延迟</span>是三项重要指标。
</pre><br/><br/>

<h5 class="text-danger">Sample2：下图显示可以按照不同维度展开的OLAP功能，下图按照运营商维度展开，看不同接口的访问情况。</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor03.png"/>

<br/><br/>

<h4 class="text-danger">2、APP端到端告警&nbsp;&nbsp;&nbsp;&nbsp;<a href="/cat/s/config?op=appRule">访问链接</a></h4>
<h5 class="text-success">A) 配置一览表</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor06.png"/>
<h5 class="text-success">B) 配置告警规则</h5>
<p>（1）告警名自定义，方便区分告警项。可对<span class="text-danger">请求数、访问成功率、响应时间</span>进行监控。</p>
<p>（2）告警维度：可以根据每个维度进行选择，可以选择<span class="text-danger">All（某一维度的所有聚合），*（某一维度任意条件，如地区中*是对所有地区分别进行告警），具体条件（如地区维度中选择上海市）</span></p>
<p>（3）多个监控规则构成了告警的主体，分别对不同时间段进行配置，以方便准确地进行告警。</p>
<p>（4）监控规则诠释着某个时间段内如何进行告警，由任意多个监控条件组成。任何一条监控条件触发都会引起监控规则触发，从而告警。</p>
<p>（5）监控条件诠释着什么条件会触发监控规则，由任意多个监控子条件组成。当所有子条件同时被触发时，才会触发该监控规则。</p>

<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor07.png"/>



