<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">1、Browser使用说明&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/r/browser">访问链接</a></h4>
<p>监控点评Web页面的ajax的接口调用情况，这个是从用户Web浏览器采集的数据，从用户角度看点评接口的访问速度。</p>
<p>分析的维度有地区、运营商等。</p>
<p>此部分需要前端框架cortext配合，进行前端打点。</p>
</br>

<p>Sample：下图显示了shopweb-suggestShop一段时间的访问情况。</p>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor02.png"/>

</br></br>
<h4 class="text-danger">2、配置&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/s/web?op=urlPatterns">访问链接</a> </h4>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor04.png"/>
</br></br>
<h4 class="text-danger">3、告警&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/s/web?op=webRule">访问链接</a> </h4>
<h5 class="text-success">A) 配置一览表</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor08.png"/>
<h5 class="text-success">B) 配置告警规则</h5>
<p>（1）告警名自定义，方便区分告警项。可对<span class="text-danger">请求数、访问成功率、响应时间</span>进行监控。</p>
<p>（2）多个监控规则构成了告警的主体，分别对不同时间段进行配置，以方便准确地进行告警。</p>
<p>（3）监控规则诠释着某个时间段内如何进行告警，由任意多个监控条件组成。任何一条监控条件触发都会引起监控规则触发，从而告警。</p>
<p>（4）监控条件诠释着什么条件会触发监控规则，由任意多个监控子条件组成。当所有子条件同时被触发时，才会触发该监控规则。</p>

<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor09.png"/>



