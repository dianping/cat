<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">Event实时报表</h4>
<h5>功能：记录程序中一个事件记录了多少次，错误了多少次。相比于Transaction，Event没有运行时间统计。</h5>
<h5 class="text-sucess">a）Type统计界面</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/event01.png" width="770px"/>
<h5 class="text-sucess">b）Name统计界面</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/event02.png" width="770px"/>
<h5 class="text-sucess">c）一个小时内详细指标统计</h5>
<p>1. HitOverTime、Averager Duration Over Time,Failures Over Time 纵轴都是以5分钟为单位，HitOverTime表示5分钟内的访问次数。</p>
<p>2. Failures Over Time表示5分钟内的Transaction失败次数。</p>
<br/>
<img  class="img-polaroid"  src="${model.webapp}/images/event05.png" width="100%"/>
<h4 class="text-success">Event历史报表</h4>
<p class="text-danger">a）Transaction\Event月报表支持每天的趋势图，以天为单位,如下图</p>
<img  class="img-polaroid"  src="${model.webapp}/images/event04.png" width="100%"/>
<p class="text-danger">b）Transaction\Event报表日报表、周报表支持同比、环比对比,时间精度为5分钟</p>
<img  class="img-polaroid"  src="${model.webapp}/images/event03.png" width="100%"/>