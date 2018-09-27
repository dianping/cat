<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">Storage实时报表</h4>
<h5>功能：监控一段代码运行情况：运行时间统计、次数、错误次数等等。系统默认的有URL、Cache、SQL、PigeonCall、PigeonService</h5>
<p class="text-danger">由于计算95line需要数据较多，为了减少内存开销，系统做了一些优化，在单个小时内某一台机器，95Line误差是1ms，但是合并成1一天，1周，1个月误差较大（数据仅供参考）</p>
<h5 class="text-sucess">a）Type统计界面</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/transaction01.jpg"/>
<h5 class="text-sucess">b）Name统计界面</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/transaction02.jpg"/>
<h5 class="text-sucess">c）一个小时内详细指标统计</h5>
<p>1. Duration Distribution表示transaction的执行时间分布，这个图可以看出，大部分shopcheckin是在16-64毫秒完成，还有很少部分在512-1024毫秒完成。</p>
<p>2. HitOverTime、Averager Duration Over Time,Failures Over Time 纵轴都是以5分钟为单位，HitOverTime表示5分钟内的访问次数。</p>
<p>3. Averager Duration Over Time表示5分钟内的平均处理时间。</p>
<p>4. Failures Over Time表示5分钟内的Transaction失败次数。</p>
<img  class="img-polaroid"  src="${model.webapp}/images/transaction03.png" width="770px"/>
<br/>
<h4 class="text-success">Transaction历史统计报表</h4>
<p class="text-danger">a）Transaction\Event月报表支持每天的趋势图，以天为单位,如下图</p>
<img  class="img-polaroid"  src="${model.webapp}/images/transaction06.png" width="770px"/>
<p class="text-danger">b）Transaction\Event报表日报表、周报表支持同比、环比对比,时间精度为5分钟</p>
<img  class="img-polaroid"  src="${model.webapp}/images/transaction05.png" width="770px"/>