<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">Storage实时报表</h4>
<h5>功能：监控一段时间内数据库、Cache访问情况：各种操作访问次数、响应时间、错误次数、长时间访问量等等。</h5>
<p class="text-danger">长时间访问定义：操作响应时间超过1秒（数据库），操作响应时间超过50毫秒（cache）</p>
<h4 class="text-sucess">a）统计报表</h4>
<h5>&nbsp;&nbsp;&nbsp;&nbsp;(1) 可以选择相应操作，查看该操作的各项访问指标。数据库默认操作：<span class="text-danger">select,update,delete,insert；</span>cache默认操作：<span class="text-danger">add,get,mGet,remove</span></h5>
<h5>&nbsp;&nbsp;&nbsp;&nbsp;(2) Domain是访问该数据库或cache的应用名，All是所有应用操作数据汇总</h5>
<h5>&nbsp;&nbsp;&nbsp;&nbsp;(3) Count: 操作数；Long：长时间操作数；Avg：响应时间；Error：操作错误数</h5>
<h5>&nbsp;&nbsp;&nbsp;&nbsp;(4) 点击查询可以在当前报表上过滤不同操作访问情况，方便分析数据</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/storage/storageDatabaseReport.png" width="90%"/>
<h5 class="text-sucess">b）统计曲线图</h5>
<h5>&nbsp;&nbsp;&nbsp;&nbsp;(1) 图表展示当前应用操作数据库或cache情况</h5>
<h5>&nbsp;&nbsp;&nbsp;&nbsp;(2) 图表展示的操作种类，与查询报表中的操作对应一致</h5>
<img  class="img-polaroid"  src="${model.webapp}/images/storage/storageDatabaseLinechart.png" width="90%"/>