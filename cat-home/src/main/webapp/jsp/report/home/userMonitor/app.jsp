<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-error">1、APP端到端使用说明&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/r/app?domain=cat&ip=All&reportType=&op=view">访问链接</a></h4>
<p>监控点评APP的接口调用情况，这个是从用户手机APP采集的数据，从用户角度看点评接口的访问速度。</p>
<p>监控的分析的维度有返回码、网络类型、APP版本、平台、地区、运营商等。</p>
</br>

<h5 class="text-error">Sample1：下图显示了shop.bin在不同平台的访问量的对比情况。</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor01.png"/>

<h5 class="text-error">Sample2：下图显示可以按照不同维度展开的OLAP功能，下图按照运营商维度展开，看不同接口的访问情况。</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor03.png"/>

</br></br>
<h4 class="text-error">2、APP端到端配置&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/s/config?op=appConfigUpdate">访问链接</a> </h4>
<p>用户只需要在command节点后面增加需要监控的命令字节点即可。</p>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor05.png"/>
</br></br>
<h4 class="text-error">3、APP端到端告警&nbsp;&nbsp;&nbsp;&nbsp;<a href="/cat/s/config?op=appRule">访问链接</a></h4>

