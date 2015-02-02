<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">Logview总体介绍</h4>
<img  class="img-polaroid"  src="${model.webapp}/images/logviewAll01.png"/>
<h4 class="text-success">可视化Logview</h4>
<img  class="img-polaroid"  src="${model.webapp}/images/logviewAll02.png"/>
<h4 class="text-success">分布式Logview</h4>
<img  class="img-polaroid"  src="${model.webapp}/images/logviewAll03.png"/>
<h4 class="text-success">Transaction</h4>
<p>a）Tansaction元素包括开始时间、结束时间、类型（type）、名称（name）、状态、处理时间、以及记录的数据（data）。</p>
<img  class="img-polaroid"  src="${model.webapp}/images/logview01.jpg"/>
<p>b）Transctio成功状态默认不显示，下图就是失败状态，用红色表示，此状态一般记录为异常。</p>
<img  class="img-polaroid"  src="${model.webapp}/images/logview02.jpg"/>
<p>c）Transaction是有执行时间的，它里面可以嵌套其他的Transaction，Event。</p>
<img  class="img-polaroid"  src="${model.webapp}/images/logview03.jpg"/>
<h4 class="text-success">Event</h4>
<p>Event元素包括开始时间、类型（type）、名称（name）、状态以及数据（data）。</p>
<img  class="img-polaroid"  src="${model.webapp}/images/logview04.jpg"/>
<h4 class="text-success">Heartbeat</h4>
<p>Heartbeat元素包括开始时间、类型（type）、名称（name）、状态以及数据（data）。</p>
<img  class="img-polaroid"  src="${model.webapp}/images/logview05.jpg"/>
