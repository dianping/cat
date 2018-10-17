<%@ page session="false" language="java" pageEncoding="UTF-8" %>

<h5 class="text-danger">APP页面测速</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor10.png"/>
<br/><br/>

<pre>
具体的筛选选项详解如下：

<span class="text-danger">日期：</span>时间纬度以天为标准，可以查看每天页面加载、程序启动速度。

<span class="text-danger">页面：</span>被监控的页面或app launch阶段。选择不同页面，点击查询，可以展示不同页面或app的加载速度、方式。

<span class="text-danger">阶段：</span>如果是模块化页面，可以监控、查看各模块被加载出来的时间。

其他：见接口监控。

每5分钟进行一次计算，对5分钟内所有所有数据求和再取平均值。每日页面测速数值由所选天288个5分钟点数据求和再求平均值。

具体页面所对应的具体配置可以在 Config/App测速中查看。
</pre>
<br/><br/>